package org.aksw.deer.execution;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.aksw.deer.enrichment.AbstractEnrichmentOperator;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.parameter.ParameterMap;
import org.aksw.deer.util.EnrichmentOperator;
import org.aksw.deer.util.PluginFactory;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jetbrains.annotations.Nullable;

public class ExecutionModelGenerator {
  private ExecutionGraph executionGraph;
  private List<ExecutionPipelineBuilder> pipeBuilders;
  private List<Resource> hubs;
  private PluginFactory<AbstractEnrichmentOperator> pluginFactory;

  public ExecutionModelGenerator(Model model) throws IOException {
    this();
    this.executionGraph = new ExecutionGraph(model);
  }

  private ExecutionModelGenerator() throws IOException {
    this.pluginFactory = new PluginFactory<>(AbstractEnrichmentOperator.class);
    this.pipeBuilders = new ArrayList<>();
    this.hubs = new ArrayList<>();
  }

  public ExecutionModel generate() {
    // first step: build pipelines
    List<ExecutionPipeline> pipes = buildPipelines();
    // second step: glue them together using hubs and return resulting executionModel
    return gluePipelines(pipes);
  }

  /**
   * Do a depth-first search starting at input dataset nodes in the given configuration graph.
   */
  private List<ExecutionPipeline> buildPipelines() {
    Set<Resource> datasets = executionGraph.getStartDatasets();
    Deque<Resource> stack = new ArrayDeque<>();
    for (Resource ds : datasets) {
      executionGraph.setSubGraphId(ds, pipeBuilders.size());
      stack.push(ds);
      pipeBuilders.add(new ExecutionPipelineBuilder().writeFirstUsing(getWriter(ds)));
    }
    while (!stack.isEmpty()) {
      Set<Resource> links = traverse(stack.pop());
      for (Resource link : links) {
        stack.push(link);
      }
    }
    return pipeBuilders.stream().map(ExecutionPipelineBuilder::build).collect(Collectors.toList());
  }

  /**
   * Get datasets connected to this dataset with one enrichment operator inbetween.
   * @param ds Input dataset
   * @return Set of datasets connected to this dataset with one enrichment or operator inbetween.
   */
  @SuppressWarnings("unchecked")
  private Set<Resource> traverse(Resource ds) {
    Set<Resource> links = new HashSet<>();
    List<Resource> operators = executionGraph.getDatasetConsumers(ds);
    int currentSubGraphId = executionGraph.getSubGraphId(ds);
    for (Resource operator : operators) {
      if (!executionGraph.isVisited(operator)) {
        if (executionGraph.isHub(operator)) {
          hubs.add(operator);
          for (Resource dataset : executionGraph.getOperatorOutputs(operator)) {
            // set subgraph id (visited state) and add to links
            executionGraph.setSubGraphId(dataset, pipeBuilders.size());
            // create new pipelines
            pipeBuilders.add(new ExecutionPipelineBuilder().writeFirstUsing(getWriter(dataset)));
            links.add(dataset);
          }
        } else {
          Resource dataset = executionGraph.getOperatorOutputs(operator).get(0);
          // add enrichment function to pipe
          EnrichmentOperator fn = getOperator(operator);
          ParameterMap parameterMap = fn.createParameterMap();
          parameterMap.init(operator);
          fn.init(parameterMap, 1, 1);
          pipeBuilders.get(currentSubGraphId).chain(fn, getWriter(dataset));
          // set subgraph id (visited state) and add to links
          executionGraph.setSubGraphId(dataset, currentSubGraphId);
          links.add(dataset);
        }
        executionGraph.visit(operator);
      }
    }
    return links;
  }

  @SuppressWarnings("unchecked")
  private ExecutionModel gluePipelines(List<ExecutionPipeline> pipes) {
    for (Resource operatorHub : hubs) {
      List<Resource> operatorInputs = executionGraph.getOperatorInputs(operatorHub);
      List<Resource> operatorOutputs = executionGraph.getOperatorOutputs(operatorHub);
      EnrichmentOperator operator = getOperator(operatorHub);
      ParameterMap parameterMap = operator.createParameterMap();
      parameterMap.init(operatorHub);
      operator.init(parameterMap, operatorInputs.size(), operatorOutputs.size());
      ExecutionHub hub = new ExecutionHub(operator);
      for (Resource ds : operatorInputs) {
        hub.addInPipe(pipes.get(executionGraph.getSubGraphId(ds)));
      }
      for (Resource ds : operatorOutputs) {
        hub.addOutPipe(pipes.get(executionGraph.getSubGraphId(ds)));
      }
      hub.glue();
    }
    ExecutionModel executionModel = new ExecutionModel();
    for (Resource startDs : executionGraph.getStartDatasets()) {
      executionModel.addStartPipe(pipes.get(executionGraph.getSubGraphId(startDs)), readDataset(startDs));
    }
    return executionModel;
  }


  /**
   * @return dataset model from file/uri/endpoint
   */
  @SuppressWarnings("Duplicates")
  private Model readDataset(Resource dataset) {
    Model model;
    if (dataset.hasProperty(DEER.fromEndPoint)) {
      model = ModelReader
        .readModelFromEndPoint(dataset, dataset.getProperty(DEER.fromEndPoint).getObject().toString());
    } else {
      String s = null;
      if (dataset.hasProperty(DEER.hasUri)) {
        s = dataset.getProperty(DEER.hasUri).getObject().toString();
      } else if (dataset.hasProperty(DEER.inputFile)) {
        s = dataset.getProperty(DEER.inputFile).getObject().toString();
      }
      if (s == null) {
        //@todo: introduce MalformedConfigurationException
        throw new RuntimeException("Encountered root dataset without source declaration: " + dataset);
      }
      model = new ModelReader().readModel(s);
    }
    return model;
  }

  /**
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private EnrichmentOperator getOperator(Resource operator) {
    Resource implementation = operator.getPropertyResourceValue(DEER.implementedIn);
    if (implementation == null) {
      throw new RuntimeException("Implementation type of enrichment " + operator + " is not specified!");
    }
    return pluginFactory.create(implementation.getURI());
  }

  /**
   * Return instance of ModelWriter for a dataset, configured by relevant RDF properties
   * @param datasetUri URI of the Resource describing the dataset and its configuration
   * @return Configured Instance of ModelWriter
   */
  @Nullable
  private Consumer<Model> getWriter(Resource datasetUri) {
    ModelWriter writer = new ModelWriter();
    Statement fileName = datasetUri.getProperty(DEER.outputFile);
    if (fileName == null) {
      return null;
    }
    Statement fileFormat = datasetUri.getProperty(DEER.outputFormat);
    writer.init(fileFormat == null ? "TTL" : fileFormat.getString(), fileName.getString());
    return writer;
  }

}
