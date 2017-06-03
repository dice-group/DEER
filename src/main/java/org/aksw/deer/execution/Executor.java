package org.aksw.deer.execution;

import static org.aksw.deer.util.QueryHelper.exists;
import static org.aksw.deer.util.QueryHelper.forEachResultOf;
import static org.aksw.deer.util.QueryHelper.hasEmptyResult;
import static org.aksw.deer.util.QueryHelper.mapResultOf;
import static org.aksw.deer.util.QueryHelper.not;
import static org.aksw.deer.util.QueryHelper.triple;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.util.IEnrichmentFunction;
import org.aksw.deer.util.IOperator;
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
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
public class Executor {


  private static final Logger logger = Logger.getLogger(Executor.class.getName());
  private Model configModel;
  private RunContext context;
  private ModelReader modelReader;
  private ModelWriter modelWriter;
  private PluginFactory<IOperator> operatorPluginFactory;
  private PluginFactory<IEnrichmentFunction> enrichmentFunctionPluginFactory;
  private int maxSubGraphId = 0;

  public Executor(String modelUrl, RunContext context) throws IOException {
    this(context);
    this.configModel = modelReader.readModel(modelUrl);
  }

  public Executor(Model model) throws IOException {
    this(new RunContext(0, ""));
    this.configModel = model;
  }

  private Executor(RunContext context) throws IOException {
    this.context = context;
    this.modelReader = new ModelReader(context.getSubDir());
    this.modelWriter = new ModelWriter(context.getSubDir());
    this.operatorPluginFactory = new PluginFactory<>(IOperator.class);
    this.enrichmentFunctionPluginFactory = new PluginFactory<>(IEnrichmentFunction.class);
  }

  /**
   * execute the input RDF config file and return set of all enriched dataset
   */
  public Set<Model> execute() throws IOException {
    annotateModelForExecution(getFinalDatasets());
    configModel.write(System.out,"TTL");
    Set<Model> result = new HashSet<>();
    Map<Integer, Resource> startDatasets = getStartDatasets();
    Map<Integer, Model> startModels = new HashMap<>();
    for (Integer i : startDatasets.keySet()) {
      Resource ds = startDatasets.get(i);
      Model model = readDataset(ds);
      startModels.put(i, model);
      while (true) {

        if (true) {
          break;
        }
      }
      //@todo: implement abstraction for generation of pipelines from list of enrichment & their parameters
      //      CompletableFuture<List<Model>> q = CompletableFuture.completedFuture(Collections.singletonList(model));


    }

//    List<Resource> finalDatasets = getFinalDatasets();
//    logger.info("Found " + finalDatasets.size() + " output Datasets: " + finalDatasets);
//    for (Resource finalDataset : finalDatasets) {
//      result.add(readDataset(finalDataset));
//    }
    return result;
  }

  /**
   * execute the input RDF configuration file and return only the first enriched dataset
   * Suitable for simple liner configuration file
   */
  public Model simpleExecute() throws IOException {
    return execute().iterator().next();
  }

  /**
   * @return a list of all final output datasets, which are included as output of some
   * operator/models and not as input to any operator/model
   */
  private List<Resource> getFinalDatasets() {
    List<Resource> result = new ArrayList<>();
    try {
      SelectBuilder sb = new SelectBuilder()
        .setDistinct(true)
        .addVar("?d")
        .addWhere("?s1", SPECS.hasOutput, "?d")
        .addFilter(not(exists(triple("?s2", SPECS.hasInput, "?d"))));
      forEachResultOf(sb.build(), configModel,
        (qs) -> result.add(qs.getResource("?d")));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   * @return a list of all start output datasets, which are marked during
   */
  private Map<Integer, Resource> getStartDatasets() {
    Map<Integer, Resource> result = new HashMap<>();
    SelectBuilder sb = new SelectBuilder()
      .setDistinct(true)
      .addVar("?s1")
      .addVar("?d")
      .addVar("?id")
      .addWhere("?s1", EXEC.isStartNode, "?d")
      .addWhere("?s1", EXEC.subGraphId, "?id");
    forEachResultOf(sb.build(), configModel,
      (qs) -> result.put(qs.getLiteral("?id").getInt(), qs.getResource("?s1")));
    return result;
  }

  /**
   * Do a depth-first search starting at final dataset nodes in the given configuration graph.
   * All nodes are marked with a subgraph id. Finally, input datasets are marked as start nodes.
   * @param datasets Collection of datasets from which to start the search
   */
  private void annotateModelForExecution(Collection<Resource> datasets) {
    Deque<Resource> stack = new ArrayDeque<>();
    for (Resource ds : datasets) {
      ds.addLiteral(EXEC.subGraphId, maxSubGraphId);
      stack.push(ds);
      maxSubGraphId++;
    }
    while (!stack.isEmpty()) {
      Resource ds = stack.pop();
      int subGraphId = ds.getProperty(EXEC.subGraphId).getLiteral().getInt();
      Set<Resource> links = getLinks(ds, subGraphId);
      for (Resource link : links) {
        if (testStart(link)) {
          link.addLiteral(EXEC.isStartNode, true);
        }
        stack.push(link);
      }
    }
  }

  /**
   * Get datasets connected to this dataset with one enrichment or operator inbetween.
   * Also assigns new subgraph ids in case of merge / split operator.
   * @param resource Input dataset
   * @return Set of datasets connected to this dataset with one enrichment or operator inbetween.
   */
  private Set<Resource> getLinks (Resource resource, int subGraphId) {
    Set<Resource> result = new HashSet<>();
    SelectBuilder subSelectMerge, subSelectClone;
    subSelectClone = getLinksSb(resource, SPECS.hasInput, SPECS.hasOutput, SPECS.CloneOperator);
    subSelectMerge = getLinksSb(resource, SPECS.hasOutput, SPECS.hasInput, SPECS.MergeOperator);
    Query query = subSelectClone.addUnion(subSelectMerge).build();
    forEachResultOf(query, configModel,
      (qs) -> {
        boolean newSubGraph = qs.getLiteral("?newSubGraph").getBoolean();
        Resource ds = qs.getResource("?o");
        ds.addLiteral(EXEC.subGraphId, newSubGraph ? maxSubGraphId++ : subGraphId);
        result.add(ds);
      });
    return result;
  }

  private SelectBuilder getLinksSb(Resource resource, Object p1, Object p2, Object op) {
    try {
      return new SelectBuilder()
        .setDistinct(true)
        .addVar("?s").addVar("?o").addVar("?newSubGraph")
        .addWhere("?s", p1, resource)
        .addWhere("?s", p2, "?o")
        .addOptional("?o", EXEC.subGraphId, "?q")
        .addBind(exists(triple("?s", RDF.type, (Resource) op)), "?newSubGraph")
        .addFilter("!bound(?q)");
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean testStart(Resource ds) {
    try {
      SelectBuilder sb = new SelectBuilder()
        .setDistinct(true)
        .addVar("?s")
        .addWhere("?s", SPECS.hasInput, ds)
        .addFilter(not(exists(triple("?s2", SPECS.hasOutput, ds))));
      return !hasEmptyResult(sb.build(), configModel);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private IEnrichmentFunction getModule (Resource module) {
    NodeIterator typeItr = configModel.listObjectsOfProperty(module, RDF.type);
    while (typeItr.hasNext()) {
      RDFNode type = typeItr.next();
      if (type.equals(SPECS.Module)) {
        continue;
      }
      return enrichmentFunctionPluginFactory.create(type.toString());
    }
    throw new RuntimeException("Implementation type of enrichment " + module + " is not specified!");
  }


  /**
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private IOperator getOperator(Resource operator) {
    NodeIterator typeItr = configModel.listObjectsOfProperty(operator, RDF.type);
    while (typeItr.hasNext()) {
      RDFNode type = typeItr.next();
      if (type.equals(SPECS.Operator)) {
        continue;
      }
      return operatorPluginFactory.create(type.toString());
    }
    throw new RuntimeException("Implementation type of enrichment " + operator + " is not specified!");
  }

  /**
   * @return dataset model from file/uri/endpoint
   */
  private Model readDataset(Resource dataset) throws IOException {
    Model cbd;
    if (dataset.hasProperty(SPECS.fromEndPoint)) {
      cbd = ModelReader
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
      cbd = modelReader.readModel(s);
    }
    writeDataset(dataset, cbd);
    return cbd;
  }

  //    recursive case: read dataset from previous enrichment/operator output
  //    Resource moduleOrOperator = getModuleOrOperator(null, dataset);
  //    Model outputModel = executeModuleOrOperator(moduleOrOperator);
  //    writeDataset(dataset, outputModel);
  //    return outputModel;



  /**
   * @param datasetUri
   * @param dataSetModel
   * @throws IOException
   */
  private void writeDataset(Resource datasetUri, Model dataSetModel) throws IOException {
    Statement fileName = datasetUri.getProperty(SPECS.outputFile);
    if (fileName != null) {
      Statement fileFormat = datasetUri.getProperty(SPECS.outputFile);
      modelWriter
        .writeModel(dataSetModel, fileFormat == null ? "TTL" : fileFormat.getString(), fileName.getString());
    }
  }

  /**
   * @return a list of all input datasets to a certain enrichment/operator
   */
  private List<Resource> getInputDatasetsUris(Resource moduleOrOperator) {
    List<Resource> result = new ArrayList<>();
    SelectBuilder sb = new SelectBuilder()
      .setDistinct(true)
      .addVar("?d")
      .addWhere(moduleOrOperator, SPECS.hasInput, "?d");
    forEachResultOf(sb.build(), configModel, (qs) -> result.add(qs.getResource("?d")));
    return result;
  }



  /**
   * @return enrichment/operator for a given input/output dataset.
   */
  private Resource getModuleOrOperator(Resource inputDataset, Resource outputDataset) {
    if (inputDataset == null && outputDataset == null) {
      return null;
    }
    SelectBuilder sb = new SelectBuilder()
      .setDistinct(true)
      .addVar("?s")
      .addWhere("?s", "?p", "?ds");
    if (inputDataset == null) {
      sb.setVar("?p", SPECS.hasOutput);
      sb.setVar("?ds", outputDataset);
    } else {
      sb.setVar("?p", SPECS.hasInput);
      sb.setVar("?ds", inputDataset);
      if (outputDataset != null) {
        sb.addWhere("?s", SPECS.hasOutput, outputDataset);
      }
    }
    return mapResultOf(sb.build(), configModel, (qs) -> qs.getResource("?s")).get(0);
  }

  /**
   * @param moduleOrOperator
   * @throws IOException
   */
  private Model executeModuleOrOperator(Resource moduleOrOperator) throws IOException {
    List<Resource> inputDatasetsUris = getInputDatasetsUris(moduleOrOperator);
    List<Model> inputDatasetsModels = new ArrayList<>();
    for (Resource inputDatasetUri : inputDatasetsUris) {
      inputDatasetsModels.add(readDataset(inputDatasetUri));
    }
    NodeIterator typeItr = configModel.listObjectsOfProperty(moduleOrOperator, RDF.type);
    while (typeItr.hasNext()) {
      RDFNode type = typeItr.next();
      if (type.equals(SPECS.Module)) {
//        return executeModule(moduleOrOperator, inputDatasetsModels);
      } else if (type.equals(SPECS.Operator)) {
//        return executeOperator(moduleOrOperator, inputDatasetsModels);
      }
    }
    return null;
  }

}
