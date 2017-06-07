package org.aksw.deer.learning;

import java.util.List;
import java.util.Map;
import org.aksw.deer.util.IEnrichmentFunction;
import org.aksw.deer.util.IOperator;
import org.aksw.deer.vocabulary.SPECS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.Logger;

/**
 * @author sherif
 */
//@todo: change to configbuilder using builder pattern
public class ConfigBuilder {

  private final static Logger logger = Logger.getLogger(ConfigBuilder.class.getName());
  private long moduleNr = 1;
  private long parameterNr = 1;


  /**
   * @return add configuration for the input enrichment to the input configuration model, the returned
   * configuration model  is independent of the input configuration model
   * @author sherif
   */
  public Model addModule(IEnrichmentFunction module, Map<String, String> parameters, final Model inputConfig,
    Resource inputDataset, Resource outputDataset) {
    Model config = ModelFactory.createDefaultModel();
    Resource s;
    s = ResourceFactory.createResource(SPECS.uri + module.id() + moduleNr++);
    config.add(s, RDF.type, SPECS.Module);
    config.add(s, RDF.type, module.getType());
    addDataset(config, inputDataset);
    addDataset(config, outputDataset);
    config.add(s, SPECS.hasInput, inputDataset);
    config.add(s, SPECS.hasOutput, outputDataset);
    for (String key : parameters.keySet()) {
      String value = parameters.get(key);
      Resource param = ResourceFactory.createResource(SPECS.uri + "parameter_" + parameterNr++);
      config.add(s, SPECS.hasParameter, param);
      config.add(param, RDF.type, SPECS.ModuleParameter);
      config.add(param, SPECS.hasKey, key);
      config.add(param, SPECS.hasValue, value);
    }
    config = config.union(inputConfig);
    config.setNsPrefix(SPECS.prefix, SPECS.getURI());
    config.setNsPrefix("RDFS", RDFS.getURI());
    return config;
  }


  /**
   * @return add configuration for the input operator to the input configuration model, the returned
   * configuration model  is independent of the input configuration model
   * @author sherif
   */
  public Model addOperator(IOperator operator, Map<String, String> parameters,
    List<Model> inputConfigs, List<Resource> inputDatasets, List<Resource> outputDatasets) {
    Model config = ModelFactory.createDefaultModel();
    Resource s = ResourceFactory.createResource(SPECS.uri + operator.id() + moduleNr++);
    config.add(s, RDF.type, SPECS.Operator);
    config.add(s, RDF.type, operator.getType());
    for (Resource inputDataset : inputDatasets) {
      addDataset(config, inputDataset);
      config.add(s, SPECS.hasInput, inputDataset);
    }
    for (Resource outputDataset : outputDatasets) {
      addDataset(config, outputDataset);
      config.add(s, SPECS.hasOutput, outputDataset);
    }
    if (parameters != null) {
      for (String key : parameters.keySet()) {
        String value = parameters.get(key);
        Resource param = ResourceFactory.createResource(SPECS.uri + "Parameter_" + parameterNr++);
        config.add(s, SPECS.hasParameter, param);
        config.add(param, RDF.type, SPECS.OperatorParameter);
        config.add(param, SPECS.hasKey, key);
        config.add(param, SPECS.hasValue, value);
      }
    }
    for (Model inputConfig : inputConfigs) {
      config = config.union(inputConfig);
    }
    config.setNsPrefix(SPECS.prefix, SPECS.getURI());
    config.setNsPrefix("RDFS", RDFS.getURI());
    return config;
  }

  /**
   * @author sherif
   */
  public Model addDataset(Model config, Resource dataset) {
    return config.add(dataset, RDF.type, SPECS.Dataset);
  }

  /**
   * @author sherif
   */
  public Model addDataset(Model config, Resource dataset, Resource uri, Resource endpoint) {
    addDataset(config, dataset);
    config.add(dataset, SPECS.fromEndPoint, endpoint);
    config.add(dataset, SPECS.hasUri, uri);
    return config;
  }


  /**
   * @author sherif
   */
  public Model addDataset(Model config, Resource dataset, String datasetFile) {
    addDataset(config, dataset);
    config.add(dataset, SPECS.inputFile, datasetFile);
    return config;
  }


  /**
   * @author sherif
   */
  public Model changeModuleInputOutput(Model config, Resource moduleUri, Resource inputDatasetUri,
    Resource outputDatasetUri) {
    config.removeAll(moduleUri, SPECS.hasInput, null);
    config.add(moduleUri, SPECS.hasInput, inputDatasetUri);
    config.removeAll(moduleUri, SPECS.hasOutput, null);
    config.add(moduleUri, SPECS.hasOutput, outputDatasetUri);
    return config;
  }


  /**
   * @author sherif
   */
  public Model changeInputDatasetUri(Model config, Resource moduleOrOperatorUri,
    Resource oldInputDatasetUri, Resource outputDatasetUri) {
    config.removeAll(moduleOrOperatorUri, SPECS.hasInput, oldInputDatasetUri);
    config.add(moduleOrOperatorUri, SPECS.hasInput, outputDatasetUri);
    return config;
  }


}