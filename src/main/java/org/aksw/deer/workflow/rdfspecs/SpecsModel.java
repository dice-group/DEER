///**
// *
// */
package org.aksw.deer.workflow.rdfspecs;
//
//import java.util.List;
//import java.util.Map;
//
//import org.aksw.deer.helper.vocabularies.SPECS;
//import org.aksw.deer.modules.DeerModule;
//import org.aksw.deer.modules.dereferencing.DereferencingModule;
//import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
//import org.aksw.deer.modules.filter.FilterModule;
//import org.aksw.deer.modules.linking.LinkingModule;
//import org.aksw.deer.modules.nlp.NLPModule;
//import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
//import org.aksw.deer.operators.DeerOperator;
//import org.aksw.deer.operators.MergeOperator;
//import org.aksw.deer.operators.CloneOperator;
//import org.apache.log4j.Logger;
//
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.rdf.model.ResourceFactory;
//import org.apache.jena.vocabulary.RDF;
//import org.apache.jena.vocabulary.RDFS;
//
///**
// * @author sherif
// *
// */
public class SpecsModel {
//	private final Logger logger 	= Logger.getLogger(SpecsModel.class.getName());
//	private static long moduleNr 			= 1;
//	private static long parameterNr 		= 1;
//	public Model specs;
//
//	Model getConfModel(){
//		return specs;
//	}
//
//	/**
//	 *
//	 *@author sherif
//	 */
//	public SpecsModel(Model config) {
//		this.specs = config;
//	}
//
//	/**
//	 *
//	 *@author sherif
//	 */
//	public SpecsModel() {
//		specs = ModelFactory.createDefaultModel();
//	}
//
//	/**
//	 * Add DEER module to specs
//	 * @param module
//	 * @param parameters
//	 * @param inputDataset
//	 * @param outputDataset
//	 * @return
//	 * @author sherif
//	 */
//	public  Model add(DeerModule module, Map<String, String> parameters, Resource inputDataset, Resource outputDataset){
//		Resource s = ResourceFactory.createResource();
//		Resource parameterType = ResourceFactory.createResource();
//		if(module instanceof AuthorityConformationModule){
//			s = ResourceFactory.createResource(SPECS.uri + "authority_conformation_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.AuthorityConformationModule);
//			parameterType = SPECS.AuthorityConformationModuleParameter;
//		}
//		else if(module instanceof PredicateConformationModule){
//			s = ResourceFactory.createResource(SPECS.uri + "predicate_conformation_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.PredicateConformationModule);
//			parameterType = SPECS.PredicateConformationModuleParameter;
//		}
//		else if(module instanceof DereferencingModule){
//			s = ResourceFactory.createResource(SPECS.uri + "dereferencing_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.DereferencingModule);
//			parameterType = SPECS.DereferencingModuleParameter;
//		}
//		else if(module instanceof FilterModule){
//			s = ResourceFactory.createResource(SPECS.uri + "filter_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.FilterModule);
//			parameterType = SPECS.FilterModuleParameter;
//		}
//		else if(module instanceof LinkingModule){
//			s = ResourceFactory.createResource(SPECS.uri + "linking_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.LinkingModule);
//			parameterType = SPECS.LinkingModuleParameter;
//		}
//		else if(module instanceof NLPModule){
//			s = ResourceFactory.createResource(SPECS.uri + "nlp_module_" + moduleNr++);
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.NLPModule);
//			parameterType = SPECS.NLPModuleParameter;
//		}else{
//			logger.error("Module " + module.getClass().getName() + " NOT implemented yet!, Exit with error.");
//			System.exit(1);
//		}
//		add(inputDataset);
//		add(outputDataset);
//		specs.add(s, SPECS.hasInput, inputDataset);
//		specs.add(s, SPECS.hasOutput, outputDataset);
//		for(String key : parameters.keySet()){
//			String value = parameters.get(key);
//			Resource param = ResourceFactory.createResource(SPECS.uri + "parameter_" + parameterNr++);
//			specs.add(s, SPECS.hasParameter, param);
//			specs.add(param, RDF.type, SPECS.ModuleParameter);
//			specs.add(param, RDF.type, parameterType);
//			specs.add(param, SPECS.hasKey, key);
//			specs.add(param, SPECS.hasValue, value);
//		}
//		specs.setNsPrefix("gl", SPECS.getURI());
//		specs.setNsPrefix("RDFS", RDFS.getURI());
//		return specs;
//	}
//
//	/**
//	 * Add DEER operator to specs
//	 * @param operator
//	 * @param parameters
//	 * @param inputDatasets
//	 * @param outputDatasets
//	 * @return
//	 * @author sherif
//	 */
//	public Model add(DeerOperator operator, Map<String, String> parameters, List<Resource> inputDatasets, List<Resource> outputDatasets){
//		Resource s = ResourceFactory.createResource();
//		Resource parameterType = ResourceFactory.createResource();
//		if(operator instanceof CloneOperator){
//			s = ResourceFactory.createResource(SPECS.uri + "split_operator_" + System.currentTimeMillis());
//			specs.add(s, RDF.type, SPECS.Operator);
//			specs.add(s, RDF.type, SPECS.CloneOperator);
//			parameterType = SPECS.CloneOperatorParameter;
//		}
//		else if(operator instanceof MergeOperator){
//			s = ResourceFactory.createResource(SPECS.uri +"merge_operator_" + System.currentTimeMillis());
//			specs.add(s, RDF.type, SPECS.Module);
//			specs.add(s, RDF.type, SPECS.MergeOperator);
//			parameterType = SPECS.MergeOperatorParameter;
//		}else{
//			logger.error("Operator " + operator.getClass().getName() + " NOT implemented yet!, Exit with error.");
//			System.exit(1);
//		}
//		for(Resource inputDataset : inputDatasets){
//			add(inputDataset);
//			specs.add(s, SPECS.hasInput, inputDataset);
//		}
//		for(Resource outputDataset : outputDatasets){
//			add(outputDataset);
//			specs.add(s, SPECS.hasOutput, outputDataset);
//		}
//		for(String key : parameters.keySet()){
//			String value = parameters.get(key);
//			Resource param = ResourceFactory.createResource(SPECS.uri + "Parameter_" + parameterNr++);
//			specs.add(s, SPECS.hasParameter, param);
//			specs.add(param, RDF.type, SPECS.OperatorParameter);
//			specs.add(param, RDF.type, parameterType);
//			specs.add(param, SPECS.hasKey, key);
//			specs.add(param, SPECS.hasValue, value);
//		}
//		return specs;
//	}
//
//	/**
//	 * Add DEER dataset to specs
//	 * @param dataset
//	 * @author sherif
//	 */
//	public void add(Resource dataset){
//		specs.add(dataset, RDF.type, SPECS.Dataset);
//	}
//
//	/**
//	 * Add DEER dataset to specs
//	 * @param dataset
//	 * @param uri
//	 * @param endpoint
//	 * @author sherif
//	 */
//	public void add(Resource dataset, Resource uri, Resource endpoint){
//		add(dataset);
//		specs.add(dataset, SPECS.fromEndPoint, endpoint);
//		specs.add(dataset, SPECS.hasUri, uri);
//	}
//
//
//	/**
//	 * @param args
//	 * @author sherif
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
//
}
