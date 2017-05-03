/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.vocabularies.SPECS;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.operators.DeerOperator;
import org.aksw.deer.operators.MergeOperator;
import org.aksw.deer.operators.CloneOperator;
import org.apache.log4j.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * @author sherif
 *
 */
public class RDFConfigWriter{
	private final static Logger logger 	= Logger.getLogger(RDFConfigWriter.class.getName());
	private static long moduleNr 			= 1;
	private static long parameterNr 		= 1;


	/**
	 * @param module
	 * @param parameters
	 * @param inputConfig
	 * @param inputDataset
	 * @param outputDataset
	 * @return add configuration for the input module to the input configuration model, 
	 * 			the returned configuration model  is independent of the input configuration model
	 * @author sherif
	 */
	public static Model addModule(DeerModule module, Map<String, String> parameters, final Model inputConfig, Resource inputDataset, Resource outputDataset){
		Model config = ModelFactory.createDefaultModel(); 
		Resource s = ResourceFactory.createResource();
		Resource parameterType = ResourceFactory.createResource();
		if(module instanceof AuthorityConformationModule){
			s = ResourceFactory.createResource(SPECS.uri + "authority_conformation_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.AuthorityConformationModule);
			parameterType = SPECS.AuthorityConformationModuleParameter;
		}
		else if(module instanceof PredicateConformationModule){
			s = ResourceFactory.createResource(SPECS.uri + "predicate_conformation_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.PredicateConformationModule);
			parameterType = SPECS.PredicateConformationModuleParameter;
		}
		else if(module instanceof DereferencingModule){
			s = ResourceFactory.createResource(SPECS.uri + "dereferencing_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.DereferencingModule);
			parameterType = SPECS.DereferencingModuleParameter;
		}
		else if(module instanceof FilterModule){
			s = ResourceFactory.createResource(SPECS.uri + "filter_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.FilterModule);
			parameterType = SPECS.FilterModuleParameter;
		}
		else if(module instanceof LinkingModule){
			s = ResourceFactory.createResource(SPECS.uri + "linking_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.LinkingModule);
			parameterType = SPECS.LinkingModuleParameter;
		}
		else if(module instanceof NLPModule){
			s = ResourceFactory.createResource(SPECS.uri + "nlp_module_" + moduleNr++);
			config.add(s, RDF.type, SPECS.Module);
			config.add(s, RDF.type, SPECS.NLPModule);
			parameterType = SPECS.NLPModuleParameter;
		}else{
			logger.error("Module " + module.getClass().getName() + " NOT implemented yet!, Exit with error.");
			System.exit(1);
		}
		addDataset(config, inputDataset);
		addDataset(config, outputDataset);
		config.add(s, SPECS.hasInput, inputDataset);
		config.add(s, SPECS.hasOutput, outputDataset);
		for(String key : parameters.keySet()){
			String value = parameters.get(key);
			Resource param = ResourceFactory.createResource(SPECS.uri + "parameter_" + parameterNr++);
			config.add(s, SPECS.hasParameter, param);
			config.add(param, RDF.type, SPECS.ModuleParameter);
			config.add(param, RDF.type, parameterType);
			config.add(param, SPECS.hasKey, key);
			config.add(param, SPECS.hasValue, value);
		}
		config = config.union(inputConfig);
		config.setNsPrefix(SPECS.prefix, SPECS.getURI());
		config.setNsPrefix("RDFS", RDFS.getURI());
		return config;
	}

	
	/**
	 * @param operator
	 * @param parameters
	 * @param inputDatasets
	 * @param outputDatasets
	 * @param inputConfig
	 * @return add configuration for the input operator to the input configuration model, 
	 * 			the returned configuration model  is independent of the input configuration model
	 * @author sherif
	 */
	public static Model addOperator(DeerOperator operator, Map<String, String> parameters, final List<Model> inputConfigs, List<Resource> inputDatasets, List<Resource> outputDatasets){
		Model config = ModelFactory.createDefaultModel(); 
		Resource s = ResourceFactory.createResource();
		Resource parameterType = ResourceFactory.createResource();
		if(operator instanceof CloneOperator){
			s = ResourceFactory.createResource(SPECS.uri + "clone_operator_" + parameterNr++);
			config.add(s, RDF.type, SPECS.Operator);
			config.add(s, RDF.type, SPECS.CloneOperator);
			parameterType = SPECS.CloneOperatorParameter;
		}
		else if(operator instanceof MergeOperator){
			s = ResourceFactory.createResource(SPECS.uri +"merge_operator_" + parameterNr++);
			config.add(s, RDF.type, SPECS.Operator);
			config.add(s, RDF.type, SPECS.MergeOperator);
			parameterType = SPECS.MergeOperatorParameter;
		}else{
			logger.error("Operator " + operator.getClass().getName() + " NOT implemented yet!, Exit with error.");
			System.exit(1);
		}
		for(Resource inputDataset : inputDatasets){
			addDataset(config, inputDataset);
			config.add(s, SPECS.hasInput, inputDataset);
		}
		for(Resource outputDataset : outputDatasets){
			addDataset(config, outputDataset);
			config.add(s, SPECS.hasOutput, outputDataset);
		}
		if(parameters!= null){
			for(String key : parameters.keySet()){
				String value = parameters.get(key);
				Resource param = ResourceFactory.createResource(SPECS.uri + "Parameter_" + parameterNr++);
				config.add(s, SPECS.hasParameter, param);
				config.add(param, RDF.type, SPECS.OperatorParameter);
				config.add(param, RDF.type, parameterType);
				config.add(param, SPECS.hasKey, key);
				config.add(param, SPECS.hasValue, value);
			}
		}
		for(Model inputConfig : inputConfigs){
			config = config.union(inputConfig);
		}
		config.setNsPrefix(SPECS.prefix, SPECS.getURI());
		config.setNsPrefix("RDFS", RDFS.getURI());
		return config;
	}
	
	/**
	 * @param dataset
	 * @author sherif
	 */
	public static Model addDataset(Model config, Resource dataset){
		return config.add(dataset, RDF.type, SPECS.Dataset);
	}
	
	/**
	 * @param dataset
	 * @param uri
	 * @param endpoint
	 * @author sherif
	 */
	public static Model addDataset(Model config, Resource dataset, Resource uri, Resource endpoint){
		addDataset(config, dataset);
		config.add(dataset, SPECS.fromEndPoint, endpoint);
		config.add(dataset, SPECS.hasUri, uri);
		return config;
	}
	
	
	/**
	 * @param config
	 * @param dataset
	 * @param uri
	 * @param datasetFile
	 * @return
	 * @author sherif
	 */
	public static Model addDataset(Model config, Resource dataset, String datasetFile){
		addDataset(config, dataset);
		config.add(dataset, SPECS.inputFile, datasetFile);
		return config;
	}
	
	
	/**
	 * @param config
	 * @param moduleUri
	 * @param inputDatasetUri
	 * @param outputDatasetUri
	 * @return
	 * @author sherif
	 */
	public static Model changeModuleInputOutput(Model config, Resource moduleUri, Resource inputDatasetUri, Resource outputDatasetUri){
		config.removeAll(moduleUri, SPECS.hasInput, null);
		config.add(moduleUri, SPECS.hasInput, inputDatasetUri);
		config.removeAll(moduleUri, SPECS.hasOutput, null);
		config.add(moduleUri, SPECS.hasOutput, outputDatasetUri);
		return config;
	}
	

	/**
	 * @param config
	 * @param moduleOrOperatorUri
	 * @param outputDatasetUri
	 * @return
	 * @author sherif
	 */
	public static Model changeInputDatasetUri(Model config, Resource moduleOrOperatorUri, Resource oldInputDatasetUri, Resource outputDatasetUri){
		config.removeAll(moduleOrOperatorUri, SPECS.hasInput, oldInputDatasetUri);
		config.add(moduleOrOperatorUri, SPECS.hasInput, outputDatasetUri);
		return config;
	}
	


	
	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
