package org.aksw.deer.execution;

import static org.aksw.deer.util.QueryHelper.exists;
import static org.aksw.deer.util.QueryHelper.forEachResultOf;
import static org.aksw.deer.util.QueryHelper.not;
import static org.aksw.deer.util.QueryHelper.triple;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.aksw.deer.enrichment.AEnrichmentOperator;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.util.IEnrichmentOperator;
import org.aksw.deer.util.Parameters;
import org.aksw.deer.util.PluginFactory;
import org.aksw.deer.vocabulary.EXEC;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;

public class ExecutionModelGenerator {
  private Model model;
  private ModelReader modelReader;
  private List<ExecutionPipeline> pipes;
  private List<Resource> hubs;
  private PluginFactory<AEnrichmentOperator> pluginFactory;

  public ExecutionModelGenerator(String modelUrl, RunContext context) throws IOException {
    this(context);
    this.model = modelReader.readModel(modelUrl);
  }

  public ExecutionModelGenerator(Model model) throws IOException {
    this(new RunContext(0, ""));
    this.model = model;
  }

  private ExecutionModelGenerator(RunContext context) throws IOException {
    this.modelReader = new ModelReader(context.getSubDir());
    this.pluginFactory = new PluginFactory<>(AEnrichmentOperator.class);
    this.pipes = new ArrayList<>();
    this.hubs = new ArrayList<>();
  }

  public ExecutionModel generate() {
    // first step: build pipelines
    ExecutionModel executionModel = buildPipelines();
    // second step: glue them together using hubs
    gluePipelines();
    //    model.write(System.out,"TTL");
    return executionModel;
  }

  private void gluePipelines() {
    SelectBuilder sb = new SelectBuilder()
      .setDistinct(true)
      .addVar("?id").addVar("?ds")
      .addWhere("?ds", EXEC.subGraphId, "?id");
    for (Resource operatorHub : hubs) {
      IEnrichmentOperator operator = getOperator(operatorHub);
      ExecutionHub hub = new ExecutionHub(operator);
      Query inQuery = sb.clone().addWhere(operatorHub, SPECS.hasInput, "?ds").build();
      AtomicInteger inCount = new AtomicInteger();
      forEachResultOf(inQuery, model, (sqs) -> {
        int subGraphId = sqs.getLiteral("?id").getInt();
        hub.addInPipe(pipes.get(subGraphId));
        inCount.incrementAndGet();
      });
      AtomicInteger outCount = new AtomicInteger();
      Query outQuery = sb.clone().addWhere(operatorHub, SPECS.hasOutput, "?ds").build();
      forEachResultOf(outQuery, model, (sqs) -> {
        int subGraphId = sqs.getLiteral("?id").getInt();
        hub.addOutPipe(pipes.get(subGraphId));
        outCount.incrementAndGet();
      });
      operator.init(Parameters.getParameters(operatorHub), inCount.get(), outCount.get());
      hub.glue();
    }
  }

  /**
   * Do a depth-first search starting at input dataset nodes in the given configuration graph.
   */
  private ExecutionModel buildPipelines() {
    ExecutionModel executionModel = new ExecutionModel();
    Collection<Resource> datasets = getStartDatasets();
    model.setNsPrefix(EXEC.prefix, EXEC.uri);
    Deque<Resource> stack = new ArrayDeque<>();
    for (Resource ds : datasets) {
      ds.addLiteral(EXEC.subGraphId, pipes.size());
      stack.push(ds);
      ExecutionPipeline pipe = new ExecutionPipeline(getWriter(ds));
      pipes.add(pipe);
      executionModel.addStartPipe(pipe, readDataset(ds));
    }
    while (!stack.isEmpty()) {
      Resource ds = stack.pop();
      int subGraphId = ds.getProperty(EXEC.subGraphId).getLiteral().getInt();
      Set<Resource> links = traverse(ds, subGraphId);
      for (Resource link : links) {
        stack.push(link);
      }
    }
    return executionModel;
  }

  /**
   * @return a list of all final output datasets, which are included as output of some
   * operator/models and not as input to any operator/model
   */
  private List<Resource> getStartDatasets() {
    List<Resource> result = new ArrayList<>();
    try {
      SelectBuilder sb = new SelectBuilder()
        .setDistinct(true)
        .addVar("?d")
        .addWhere("?s1", SPECS.hasInput, "?d")
        .addFilter(not(exists(triple("?s2", SPECS.hasOutput, "?d"))));
      forEachResultOf(sb.build(), model,
        (qs) -> result.add(qs.getResource("?d")));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   * Get datasets connected to this dataset with one enrichment or operator inbetween.
   * Also assigns new subgraph ids in case of merge / split operator.
   * @param resource Input dataset
   * @return Set of datasets connected to this dataset with one enrichment or operator inbetween.
   */
  private Set<Resource> traverse(Resource resource, int subGraphId) {
    try {
      Set<Resource> links = new HashSet<>();
      Query query = new SelectBuilder()
        .setDistinct(true)
        .addVar("?s")
        .addVar("?o")
        .addWhere("?s", SPECS.hasInput, resource)
        .addWhere("?s", SPECS.hasOutput, "?o")
        .addOptional("?o", EXEC.subGraphId, "?q")
        .addFilter("!bound(?q)")
        .build();
      forEachResultOf(query, model,
        (qs) -> {
          Resource ds = qs.getResource("?o");
          Resource node = qs.getResource("?s");
          int inCount = model.listObjectsOfProperty(node, SPECS.hasInput).toSet().size();
          int outCount = model.listObjectsOfProperty(node, SPECS.hasOutput).toSet().size();
          boolean isHub = inCount > 1 || outCount > 1;
          if (isHub) {
            // create new pipeline
            ds.addLiteral(EXEC.subGraphId, pipes.size());
            pipes.add(new ExecutionPipeline(getWriter(ds)));
            hubs.add(node);
          } else {
            // add enrichment function to pipe
            IEnrichmentOperator fn = getEnrichmentFunction(node);
            fn.init(new Parameters(node).get(), 1, 1);
            pipes.get(subGraphId).chain(fn, getWriter(ds));
            ds.addLiteral(EXEC.subGraphId, subGraphId);
          }
          links.add(ds);
        });
      return links;
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return dataset model from file/uri/endpoint
   */
  @SuppressWarnings("Duplicates")
  private Model readDataset(Resource dataset) {
    Model model;
    if (dataset.hasProperty(SPECS.fromEndPoint)) {
      model = ModelReader
        .readModelFromEndPoint(dataset, dataset.getProperty(SPECS.fromEndPoint).getObject().toString());
    } else {
      String s = null;
      if (dataset.hasProperty(SPECS.hasUri)) {
        s = dataset.getProperty(SPECS.hasUri).getObject().toString();
      } else if (dataset.hasProperty(SPECS.inputFile)) {
        s = dataset.getProperty(SPECS.inputFile).getObject().toString();
      }
      if (s == null) {
        //@todo: introduce MalformedConfigurationException
        throw new RuntimeException("Encountered root dataset without source declaration: " + dataset);
      }
      model = modelReader.readModel(s);
    }
    ModelWriter writer = getWriter(dataset);
    if (writer != null) {
      writer.accept(model);
    }
    return model;
  }

  /**
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private IEnrichmentOperator getEnrichmentFunction(Resource enrichmentFunctionNode) {
    NodeIterator typeItr = model.listObjectsOfProperty(enrichmentFunctionNode, RDF.type);
    while (typeItr.hasNext()) {
      RDFNode type = typeItr.next();
      if (type.equals(SPECS.Module)) {
        continue;
      }
      return pluginFactory.create(type.toString());
    }
    throw new RuntimeException("Implementation type of enrichment " + enrichmentFunctionNode + " is not specified!");
  }

  /**
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private IEnrichmentOperator getOperator(Resource operator) {
    NodeIterator typeItr = model.listObjectsOfProperty(operator, RDF.type);
    while (typeItr.hasNext()) {
      RDFNode type = typeItr.next();
      if (type.equals(SPECS.Operator)) {
        continue;
      }
      return pluginFactory.create(type.toString());
    }
    throw new RuntimeException("Implementation type of enrichment " + operator + " is not specified!");
  }

  /**
   * Return instance of ModelWriter for a dataset, configured by relevant RDF properties
   * @param datasetUri URI of the Resource describing the dataset and its configuration
   * @return Configured Instance of ModelWriter
   */
  private ModelWriter getWriter(Resource datasetUri) {
    ModelWriter writer = new ModelWriter();
    Statement fileName = datasetUri.getProperty(SPECS.outputFile);
    if (fileName == null) {
      return null;
    }
    Statement fileFormat = datasetUri.getProperty(SPECS.outputFormat);
    writer.init(fileFormat == null ? "TTL" : fileFormat.getString(), fileName.getString());
    return writer;
  }

}
