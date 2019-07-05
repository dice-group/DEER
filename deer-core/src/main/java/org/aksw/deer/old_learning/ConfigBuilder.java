package org.aksw.deer.old_learning;

/**
 * @author sherif
 */
//@todo: change to configbuilder using builder pattern
public class ConfigBuilder {
//
//  private final static Logger logger = LoggerFactory.getLogger(ConfigBuilder.class);
//  private long moduleNr = 1;
//  private long parameterNr = 1;
//
//
//  /**
//   * @return add configuration for the input enrichments to the input configuration model, the returned
//   * configuration model  is independent of the input configuration model
//   * @author sherif
//   */
//  public Model addModule(EnrichmentOperator module, Map<String, String> parameters, final Model inputConfig,
//    Resource inputDataset, Resource outputDataset) {
//    Model config = ModelFactory.createDefaultModel();
//    Resource s;
//    s = ResourceFactory.createResource(module.getType().getURI() + moduleNr++);
//    config.add(s, RDF.type, DEER.Operator);
//    config.add(s, RDF.type, module.getType());
//    addDataset(config, inputDataset);
//    addDataset(config, outputDataset);
//    config.add(s, DEER.hasInput, inputDataset);
//    config.add(s, DEER.hasOutput, outputDataset);
//    for (String key : parameters.keySet()) {
//      String value = parameters.get(key);
//      Resource param = ResourceFactory.createResource(DEER.NS + "parameter_" + parameterNr++);
//      config.add(s, DEER.hasParameter, param);
//      config.add(param, RDF.type, DEER.Parameter);
//      config.add(param, DEER.hasKey, key);
//      config.add(param, DEER.hasValue, value);
//    }
//    config = config.union(inputConfig);
//    config.setNsPrefix(DEER.PREFIX, DEER.getURI());
//    config.setNsPrefix("RDFS", RDFS.getURI());
//    return config;
//  }
//
//
//  /**
//   * @return add configuration for the input operator to the input configuration model, the returned
//   * configuration model  is independent of the input configuration model
//   * @author sherif
//   */
//  public Model addOperator(EnrichmentOperator operator, Map<String, String> parameters,
//    List<Model> inputConfigs, List<Resource> inputDatasets, List<Resource> outputDatasets) {
//    Model config = ModelFactory.createDefaultModel();
//    Resource s = ResourceFactory.createResource(operator.getType().getURI() + moduleNr++);
//    config.add(s, RDF.type, DEER.Operator);
//    config.add(s, RDF.type, operator.getType());
//    for (Resource inputDataset : inputDatasets) {
//      addDataset(config, inputDataset);
//      config.add(s, DEER.hasInput, inputDataset);
//    }
//    for (Resource outputDataset : outputDatasets) {
//      addDataset(config, outputDataset);
//      config.add(s, DEER.hasOutput, outputDataset);
//    }
//    if (parameters != null) {
//      for (String key : parameters.keySet()) {
//        String value = parameters.get(key);
//        Resource param = ResourceFactory.createResource(DEER.NS + "Parameter_" + parameterNr++);
//        config.add(s, DEER.hasParameter, param);
//        config.add(param, RDF.type, DEER.Parameter);
//        config.add(param, DEER.hasKey, key);
//        config.add(param, DEER.hasValue, value);
//      }
//    }
//    for (Model inputConfig : inputConfigs) {
//      config = config.union(inputConfig);
//    }
//    config.setNsPrefix(DEER.PREFIX, DEER.getURI());
//    config.setNsPrefix("RDFS", RDFS.getURI());
//    return config;
//  }
//
//  /**
//   * @author sherif
//   */
//  public Model addDataset(Model config, Resource dataset) {
//    return config.add(dataset, RDF.type, DEER.Dataset);
//  }
//
//  /**
//   * @author sherif
//   */
//  public Model addDataset(Model config, Resource dataset, Resource NS, Resource endpoint) {
//    addDataset(config, dataset);
//    config.add(dataset, DEER.fromEndPoint, endpoint);
//    config.add(dataset, DEER.hasUri, NS);
//    return config;
//  }
//
//
//  /**
//   * @author sherif
//   */
//  public Model addDataset(Model config, Resource dataset, String datasetFile) {
//    addDataset(config, dataset);
//    config.add(dataset, DEER.inputFile, datasetFile);
//    return config;
//  }
//
//
//  /**
//   * @author sherif
//   */
//  public Model changeModuleInputOutput(Model config, Resource moduleUri, Resource inputDatasetUri,
//    Resource outputDatasetUri) {
//    config.removeAll(moduleUri, DEER.hasInput, null);
//    config.add(moduleUri, DEER.hasInput, inputDatasetUri);
//    config.removeAll(moduleUri, DEER.hasOutput, null);
//    config.add(moduleUri, DEER.hasOutput, outputDatasetUri);
//    return config;
//  }
//
//
//  /**
//   * @author sherif
//   */
//  public Model changeInputDatasetUri(Model config, Resource moduleOrOperatorUri,
//    Resource oldInputDatasetUri, Resource outputDatasetUri) {
//    config.removeAll(moduleOrOperatorUri, DEER.hasInput, oldInputDatasetUri);
//    config.add(moduleOrOperatorUri, DEER.hasInput, outputDatasetUri);
//    return config;
//  }


}
