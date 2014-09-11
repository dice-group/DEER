/**
 * 
 */
package org.aksw.geolift.workflow.rdfspecs;

import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;
import org.aksw.geolift.modules.Dereferencing.DereferencingModule;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.modules.linking.LinkingModule;
import org.aksw.geolift.modules.nlp.NLPModule;
import org.aksw.geolift.operators.GeoLiftOperator;
import org.aksw.geolift.operators.MergeOperator;
import org.aksw.geolift.operators.SplitOperator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigWriter{
	private static final Logger logger 	= Logger.getLogger(RDFConfigWriter.class.getName());
	private static long moduleNr 			= 1;
	private static long parameterNr 		= 1;
	Model config = ModelFactory.createDefaultModel();
	
	Model getConfModel(){
		return config;
	}
	
	/**
	 * 
	 *@author sherif
	 */
	public RDFConfigWriter(Model config) {
		this.config = config;
	}

	/**
	 * 
	 *@author sherif
	 */
	public RDFConfigWriter() {
		// TODO Auto-generated constructor stub
	}

	public Model addModule(GeoLiftModule module, Map<String, String> parameters, Resource inputDataset, Resource outputDataset){
		Resource s = ResourceFactory.createResource();
		Resource parameterType = ResourceFactory.createResource();
		if(module instanceof ConformationModule){
			s = ResourceFactory.createResource(SpecsOntology.uri + "conformation_module_" + moduleNr++);
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.ConformationModule);
			parameterType = SpecsOntology.ConformationModuleParameter;
		}
		else if(module instanceof DereferencingModule){
			s = ResourceFactory.createResource(SpecsOntology.uri + "dereferencing_module_" + moduleNr++);
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.DereferencingModule);
			parameterType = SpecsOntology.DereferencingModuleParameter;
		}
		else if(module instanceof FilterModule){
			s = ResourceFactory.createResource(SpecsOntology.uri + "filter_module_" + moduleNr++);
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.FilterModule);
			parameterType = SpecsOntology.FilterModuleParameter;
		}
		else if(module instanceof LinkingModule){
			s = ResourceFactory.createResource(SpecsOntology.uri + "linking_module_" + moduleNr++);
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.LinkingModule);
			parameterType = SpecsOntology.LinkingModuleParameter;
		}
		else if(module instanceof NLPModule){
			s = ResourceFactory.createResource(SpecsOntology.uri + "nlp_module_" + moduleNr++);
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.NLPModule);
			parameterType = SpecsOntology.NLPModuleParameter;
		}else{
			logger.error("Module " + module.getClass().getName() + " NOT implemented yet!, Exit with error.");
			System.exit(1);
		}
		addDataset(inputDataset);
		addDataset(outputDataset);
		config.add(s, SpecsOntology.hasInput, inputDataset);
		config.add(s, SpecsOntology.hasOutput, outputDataset);
		for(String key : parameters.keySet()){
			String value = parameters.get(key);
			Resource param = ResourceFactory.createResource(SpecsOntology.uri + "parameter_" + parameterNr++);
			config.add(s, SpecsOntology.hasParameter, param);
			config.add(param, RDF.type, SpecsOntology.ModuleParameter);
			config.add(param, RDF.type, parameterType);
			config.add(param, SpecsOntology.hasKey, key);
			config.add(param, SpecsOntology.hasValue, value);
		}
		return config;
	}
	
	public Model addOperator(GeoLiftOperator operator, Map<String, String> parameters, List<Resource> inputDatasets, List<Resource> outputDatasets){
		Resource s = ResourceFactory.createResource();
		Resource parameterType = ResourceFactory.createResource();
		if(operator instanceof SplitOperator){
			s = ResourceFactory.createResource(SpecsOntology.uri + "split_operator_" + System.currentTimeMillis());
			config.add(s, RDF.type, SpecsOntology.Operator);
			config.add(s, RDF.type, SpecsOntology.SplitOperator);
			parameterType = SpecsOntology.SplitOperatorParameter;
		}
		else if(operator instanceof MergeOperator){
			s = ResourceFactory.createResource(SpecsOntology.uri +"merge_operator_" + System.currentTimeMillis());
			config.add(s, RDF.type, SpecsOntology.Module);
			config.add(s, RDF.type, SpecsOntology.MergeOperator);
			parameterType = SpecsOntology.MergeOperatorParameter;
		}else{
			logger.error("Operator " + operator.getClass().getName() + " NOT implemented yet!, Exit with error.");
			System.exit(1);
		}
		for(Resource inputDataset : inputDatasets){
			addDataset(inputDataset);
			config.add(s, SpecsOntology.hasInput, inputDataset);
		}
		for(Resource outputDataset : outputDatasets){
			addDataset(outputDataset);
			config.add(s, SpecsOntology.hasOutput, outputDataset);
		}
		for(String key : parameters.keySet()){
			String value = parameters.get(key);
			Resource param = ResourceFactory.createResource(SpecsOntology.uri + "Parameter_" + parameterNr++);
			config.add(s, SpecsOntology.hasParameter, param);
			config.add(param, RDF.type, SpecsOntology.OperatorParameter);
			config.add(param, RDF.type, parameterType);
			config.add(param, SpecsOntology.hasKey, key);
			config.add(param, SpecsOntology.hasValue, value);
		}
		return config;
	}
	
	public void addDataset(Resource dataset){
		config.add(dataset, RDF.type, SpecsOntology.Dataset);
	}
	
	public void addDataset(Resource dataset, Resource uri, Resource endpoint){
		addDataset(dataset);
		
	}
	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
