package org.aksw.deer.execution;

import static org.aksw.deer.util.QueryHelper.exists;
import static org.aksw.deer.util.QueryHelper.forEachResultOf;
import static org.aksw.deer.util.QueryHelper.mapResultOf;
import static org.aksw.deer.util.QueryHelper.not;
import static org.aksw.deer.util.QueryHelper.triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.util.IEnrichmentOperator;
import org.aksw.deer.util.PluginFactory;
import org.aksw.deer.vocabulary.EXEC;
import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.arq.querybuilder.SelectBuilder;
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
@Deprecated
public class OldExecution {
//
//  private static final Logger logger = Logger.getLogger(OldExecution.class.getName());
//
//
//
//  private Model model;
//  private RunContext context;
//  private ModelReader modelReader;
//  private ModelWriter modelWriter;
//  private PluginFactory<IEnrichmentOperator> operatorPluginFactory;
//  private PluginFactory<IEnrichmentOperator> enrichmentFunctionPluginFactory;
//  private List<ExecutionPipeline> pipes;
//
//
//  public OldExecution(String modelUrl, RunContext context) throws IOException {
//    this(context);
//    this.model = modelReader.readModel(modelUrl);
//  }
//
//  public OldExecution(Model model) throws IOException {
//    this(new RunContext(0, ""));
//    this.model = model;
//  }
//
//  private OldExecution(RunContext context) throws IOException {
//    this.modelReader = new ModelReader(context.getSubDir());
//    this.modelWriter = new ModelWriter(context.getSubDir());
//    this.operatorPluginFactory = new PluginFactory<>(IEnrichmentOperator.class);
//    this.enrichmentFunctionPluginFactory = new PluginFactory<>(IEnrichmentOperator.class);
//    this.pipes = new ArrayList<>();
//  }
//  //
////  /**
////   * execute the input RDF config file and return set of all enriched dataset
////   */
//  public Set<Model> execute() throws IOException {
//    Set<Model> result = new HashSet<>();
////    Map<Integer, Resource> startDatasets = getStartDatasets();
////    Map<Integer, Model> startModels = new HashMap<>();
////    for (Integer i : startDatasets.keySet()) {
////      Resource ds = startDatasets.get(i);
////      Model model = readDataset(ds);
////      startModels.put(i, model);
////      while (true) {
////
////        if (true) {
////          break;
////        }
////      }
////      //      CompletableFuture<List<Model>> q = CompletableFuture.completedFuture(Collections.singletonList(model));
////
////
////    }
////
//////    List<Resource> finalDatasets = getFinalDatasets();
//////    logger.info("Found " + finalDatasets.size() + " output Datasets: " + finalDatasets);
//////    for (Resource finalDataset : finalDatasets) {
//////      result.add(readDataset(finalDataset));
//////    }
//    return result;
//  }
//
//  /**
//   * execute the input RDF configuration file and return only the first enriched dataset
//   * Suitable for simple liner configuration file
//   */
//  public Model simpleExecute() throws IOException {
//    return execute().iterator().next();
//  }
//
//  private void test() {
//    try {
//      SelectBuilder sb = new SelectBuilder()
//        .setDistinct(true)
//        .addVar("?s").addVar("?id")
//        .addWhere("?s", EXEC.subGraphId, "?id")
//        .addFilter(exists(triple("?s", EXEC.isPipelineStartNode, "?x")));
//      forEachResultOf(sb.build(), model,
//        (qs) -> System.out.println(qs.getResource("?s"))
//      );
//    } catch (ParseException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  /**
//   * @return a list of all final output datasets, which are included as output of some
//   * operator/models and not as input to any operator/model
//   */
//  private List<Resource> getFinalDatasets() {
//    List<Resource> result = new ArrayList<>();
//    try {
//      SelectBuilder sb = new SelectBuilder()
//        .setDistinct(true)
//        .addVar("?d")
//        .addWhere("?s1", DEER.hasOutput, "?d")
//        .addFilter(not(exists(triple("?s2", DEER.hasInput, "?d"))));
//      forEachResultOf(sb.build(), model,
//        (qs) -> result.add(qs.getResource("?d")));
//    } catch (ParseException e) {
//      throw new RuntimeException(e);
//    }
//    return result;
//  }
//
//  /**
//   * @return a list of all start output datasets, which are marked during
//   */
//  private Map<Integer, Resource> getStartDatasets() {
//    Map<Integer, Resource> result = new HashMap<>();
//    SelectBuilder sb = new SelectBuilder()
//      .setDistinct(true)
//      .addVar("?s1")
//      .addVar("?d")
//      .addVar("?id")
//      .addWhere("?s1", EXEC.isStartNode, "?d")
//      .addWhere("?s1", EXEC.subGraphId, "?id");
//    forEachResultOf(sb.build(), model,
//      (qs) -> result.put(qs.getLiteral("?id").getInt(), qs.getResource("?s1")));
//    return result;
//  }
//
//
//  /**
//   * @return Implementation of IModule defined by the given resource's rdf:type
//   */
//  private IEnrichmentOperator getModule (Resource module) {
//    NodeIterator typeItr = model.listObjectsOfProperty(module, RDF.type);
//    while (typeItr.hasNext()) {
//      RDFNode type = typeItr.next();
//      if (type.equals(DEER.Module)) {
//        continue;
//      }
//      return enrichmentFunctionPluginFactory.create(type.toString());
//    }
//    throw new RuntimeException("Implementation type of enrichment " + module + " is not specified!");
//  }
//
//
//  /**
//   * @return Implementation of IModule defined by the given resource's rdf:type
//   */
//  private IEnrichmentOperator getOperator(Resource operator) {
//    NodeIterator typeItr = model.listObjectsOfProperty(operator, RDF.type);
//    while (typeItr.hasNext()) {
//      RDFNode type = typeItr.next();
//      if (type.equals(DEER.Operator)) {
//        continue;
//      }
//      return operatorPluginFactory.create(type.toString());
//    }
//    throw new RuntimeException("Implementation type of enrichment " + operator + " is not specified!");
//  }
//
//  /**
//   * @return dataset model from file/uri/endpoint
//   */
//  private Model readDataset(Resource dataset) throws IOException {
//    Model cbd;
//    if (dataset.hasProperty(DEER.fromEndPoint)) {
//      cbd = ModelReader
//        .readModelFromEndPoint(dataset, dataset.getProperty(DEER.fromEndPoint).getObject().toString());
//    } else {
//      String s = null;
//      if (dataset.hasProperty(DEER.hasUri)) {
//        s = dataset.getProperty(DEER.hasUri).getObject().toString();
//      } else if (dataset.hasProperty(DEER.inputFile)) {
//        s = dataset.getProperty(DEER.inputFile).getObject().toString();
//      }
//      if (s == null) {
//        throw new RuntimeException("Encountered root dataset without source declaration: " + dataset);
//      }
//      cbd = modelReader.readModel(s);
//    }
//    writeDataset(dataset, cbd);
//    return cbd;
//  }
//
//  //    recursive case: read dataset from previous enrichment/operator output
//  //    Resource moduleOrOperator = getModuleOrOperator(null, dataset);
//  //    Model outputModel = executeModuleOrOperator(moduleOrOperator);
//  //    writeDataset(dataset, outputModel);
//  //    return outputModel;
//
//
//
//  /**
//   * @param datasetUri
//   * @param dataSetModel
//   * @throws IOException
//   */
//  private void writeDataset(Resource datasetUri, Model dataSetModel) throws IOException {
//    Statement fileName = datasetUri.getProperty(DEER.outputFile);
//    if (fileName != null) {
//      Statement fileFormat = datasetUri.getProperty(DEER.outputFile);
//      modelWriter
//        .writeModel(dataSetModel, fileFormat == null ? "TTL" : fileFormat.getString(), fileName.getString());
//    }
//  }
//
//  /**
//   * @return a list of all input datasets to a certain enrichment/operator
//   */
//  private List<Resource> getInputDatasetsUris(Resource moduleOrOperator) {
//    List<Resource> result = new ArrayList<>();
//    SelectBuilder sb = new SelectBuilder()
//      .setDistinct(true)
//      .addVar("?d")
//      .addWhere(moduleOrOperator, DEER.hasInput, "?d");
//    forEachResultOf(sb.build(), model, (qs) -> result.add(qs.getResource("?d")));
//    return result;
//  }
//
//
//
//  /**
//   * @return enrichment/operator for a given input/output dataset.
//   */
//  private Resource getModuleOrOperator(Resource inputDataset, Resource outputDataset) {
//    if (inputDataset == null && outputDataset == null) {
//      return null;
//    }
//    SelectBuilder sb = new SelectBuilder()
//      .setDistinct(true)
//      .addVar("?s")
//      .addWhere("?s", "?p", "?ds");
//    if (inputDataset == null) {
//      sb.setVar("?p", DEER.hasOutput);
//      sb.setVar("?ds", outputDataset);
//    } else {
//      sb.setVar("?p", DEER.hasInput);
//      sb.setVar("?ds", inputDataset);
//      if (outputDataset != null) {
//        sb.addWhere("?s", DEER.hasOutput, outputDataset);
//      }
//    }
//    return mapResultOf(sb.build(), model, (qs) -> qs.getResource("?s")).get(0);
//  }
//
//  /**
//   * @param moduleOrOperator
//   * @throws IOException
//   */
//  private Model executeModuleOrOperator(Resource moduleOrOperator) throws IOException {
//    List<Resource> inputDatasetsUris = getInputDatasetsUris(moduleOrOperator);
//    List<Model> inputDatasetsModels = new ArrayList<>();
//    for (Resource inputDatasetUri : inputDatasetsUris) {
//      inputDatasetsModels.add(readDataset(inputDatasetUri));
//    }
//    NodeIterator typeItr = model.listObjectsOfProperty(moduleOrOperator, RDF.type);
//    while (typeItr.hasNext()) {
//      RDFNode type = typeItr.next();
//      if (type.equals(DEER.Module)) {
////        return executeModule(moduleOrOperator, inputDatasetsModels);
//      } else if (type.equals(DEER.Operator)) {
////        return executeOperator(moduleOrOperator, inputDatasetsModels);
//      }
//    }
//    return null;
//  }

}
