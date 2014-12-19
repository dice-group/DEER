/**
 * 
 */
package org.aksw.deer.workflow.specslearner.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.vacabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.ModuleFactory;
import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.operators.DeerOperator;
import org.aksw.deer.operators.OperatorFactory;
import org.aksw.deer.workflow.rdfspecs.RDFConfigAnalyzer;
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author sherif
 *
 */
public class AutoSpecsGenerator {
	private static final Logger logger = Logger.getLogger(AutoSpecsGenerator.class.getName());
	private static int datasetIndex = 1;
	public 	 static Model specsModel = ModelFactory.createDefaultModel();
	private static List<DeerModule> deerModules = ModuleFactory.getImplementations();
	private static List<DeerOperator> deerOperators = OperatorFactory.getImplementations();


	/**
	 * @param inputDataModel
	 * @param size (number of modules included in the resulted configuration)
	 * @param complexity [0,1], 0 means only modules, 1 means only operators 
	 * @return a random configuration file with complexity ≤ complexity 
	 * @author sherif
	 */
	public Model generateSpecs(Model inputDataModel, int size, double complexity){	
		Resource inputDatasetUri  = generateDatasetURI();
		do{
			Resource outputDatasetUri = generateDatasetURI();
			// fix specs file for in/out datasets URIs
			ResIterator moduleToChangeInput = specsModel.listSubjectsWithProperty(SPECS.hasInput, inputDatasetUri);
			if(moduleToChangeInput.hasNext()) {
				Resource r = moduleToChangeInput.next();
				RDFConfigWriter.changeInputDatasetUri(specsModel, r, inputDatasetUri, outputDatasetUri);
//				specsModel.write(System.out,"TTL");
			}
			if(Math.random() >= complexity){
				// Create module  
				DeerModule module = getRandomModule();
				logger.info("Generating Module: " + module.getType());
				Map<String, String> parameters = generateRandomParameters(module, inputDataModel);
				logger.info("With parameters: " + parameters);
				if(parameters != null){
					specsModel = RDFConfigWriter.addModule(module, parameters, specsModel, inputDatasetUri, outputDatasetUri);
					specsModel.write(System.out,"TTL");
				}
				inputDatasetUri = getRandomDataset();
			}else{ // Create clone - merge sequence
				List<Resource> outputDatasetstUris = addCloneOperator(inputDatasetUri);
				addMergeOperator(outputDatasetstUris,outputDatasetUri);
				specsModel.write(System.out,"TTL");
				// in order not to create an empty clone merge sequence
				inputDatasetUri = outputDatasetstUris.get(0);
			}
			System.out.println("---------------------------" +RDFConfigAnalyzer.getModules(specsModel).size());
		}while(RDFConfigAnalyzer.getModules(specsModel).size() < size);
		return specsModel;
	}

	/**
	 * @param specsModel2
	 * @return
	 * @author sherif
	 */
	private static Resource getRandomDataset() {
		List<Resource> datasets = new ArrayList<Resource>(RDFConfigAnalyzer.getDatasets(specsModel));
		if(datasets.size() == 0){
			return null;
		}
		int i = (int) (Math.random() * (datasets.size() -1));
		return datasets.get(i);
	}

	/**
	 * @param inputDatasetUri
	 * @author sherif
	 * @return 
	 */
	private List<Resource> addCloneOperator(Resource inputDatasetUri) {
		DeerOperator clone = OperatorFactory.createOperator(OperatorFactory.CLONE_OPERATOR);
		List<Model> confModels = new ArrayList<Model>(Arrays.asList(specsModel));
		List<Resource> inputDatasets = new ArrayList<Resource>(Arrays.asList(inputDatasetUri));
		List<Resource> outputDatasets = new ArrayList<Resource>(Arrays.asList(generateDatasetURI(),generateDatasetURI()));
		specsModel = RDFConfigWriter.addOperator(clone, null, confModels, inputDatasets, outputDatasets);
		return outputDatasets;
	}
	
	/**
	 * @param inputDatasetUri
	 * @author sherif
	 * @return 
	 */
	private Resource addMergeOperator(List<Resource> inputDatasetUris, Resource outputDatasetUri) {
		List<Resource> outputDatasetsUris = new ArrayList<Resource>(Arrays.asList(outputDatasetUri));
		DeerOperator merge = OperatorFactory.createOperator(OperatorFactory.MERGE_OPERATOR);
		List<Model> confModels = new ArrayList<Model>(Arrays.asList(specsModel));
		specsModel = RDFConfigWriter.addOperator(merge, null, confModels, inputDatasetUris, outputDatasetsUris);
		return outputDatasetUri;
	}

	/**
	 * @param module
	 * @return
	 * @author sherif
	 */
	private Map<String, String> generateRandomParameters(DeerModule module, Model inputDataset) {
		if(module instanceof DereferencingModule){
			return DereferencingModuleRandomParameter(inputDataset);
		}else if(module instanceof NLPModule){
			return nlpModuleRandomParameter();
		}else if(module instanceof AuthorityConformationModule){
			return authorityConformationModuleRandomParameter(inputDataset);
		}else if(module instanceof PredicateConformationModule){
			return predicateConformationModuleRandomParameter(inputDataset);
		}else if(module instanceof FilterModule){
			return filterModuleRandomParameter(inputDataset);
		}else if(module instanceof LinkingModule){
			return linkingModuleRandomParameter(inputDataset);
		}
		return null;
	}

	/**
	 * @param inputDataset
	 * @return
	 * @author sherif
	 */
	private Map<String, String> filterModuleRandomParameter(Model inputDataset) {
		Map<String, String> parameters = new HashMap<String, String>();
		int l = (int) (1 + Math.random() * 5);
		List<String> predicates = getPredicates(inputDataset, l);
		if(predicates.size() == 0){
			return null;
		}
		String triplePattern = "";
		for (String p : predicates) {
			triplePattern += p + " ";
		}
		parameters.put(FilterModule.TRIPLES_PATTERN, triplePattern);
		return parameters;
	}

	/**
	 * @return
	 * @author sherif
	 * @param inputDataset 
	 */
	private Map<String, String> predicateConformationModuleRandomParameter(Model inputDataset) {
		Map<String, String> parameters = new HashMap<String, String>();
		int l = (int) (1 + Math.random() * 5);
		List<String> predicates = getPredicates(inputDataset, l);
		if(predicates.size() == 0){
			return null;
		}
		int i = 1;
		for (String p : predicates) {
			parameters.put(PredicateConformationModule.SOURCE_PROPERTY + i++, p);
			parameters.put(PredicateConformationModule.TARGET_PROPERTY + i++, p + i);
		}
		return parameters;
	}

	/**
	 * @param inputDataset
	 * @param limit number of returned predicates, all predicates returned if set to -1
	 * @return List of predicates in inputDataset
	 * @author sherif
	 */
	public List<String> getPredicates(Model inputDataset, int limit){
		String sparqlQueryString= "SELECT DISTINCT ?p {?s ?p ?o} " +
				((limit == -1) ? "" : "LIMIT " + limit);
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, inputDataset);
		ResultSet queryResults = qexec.execSelect();
		List<String> predicates = new ArrayList<String>();
		while(queryResults.hasNext()){
			QuerySolution qs=queryResults.nextSolution();
			String p = qs.getResource("?p").toString();
			predicates.add(p);
		}qexec.close() ;
		return predicates;
	}

	/**
	 * @return
	 * @author sherif
	 * @param inputDataset 
	 */
	private Map<String, String> authorityConformationModuleRandomParameter(Model inputDataset) {
		Map<String, String> parameters = new HashMap<String, String>();
		String authority = "";
		long offset = (long) (Math.random() * (inputDataset.size() -1));
		String sparqlQueryString= "SELECT ?s {?s ?p ?o} LIMIT 1 OFFSET " + offset;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, inputDataset);
		ResultSet queryResults = qexec.execSelect();
		if(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			authority = qs.getResource("?s").toString();
		}else{
			qexec.close() ;
			return null;
		}
		qexec.close() ;
		if(authority.contains("#")){
			authority = authority.substring(0, authority.lastIndexOf("#"));
		}else{
			authority = authority.substring(0, authority.lastIndexOf("/"));
		}
		parameters.put(AuthorityConformationModule.SOURCE_SUBJET_AUTHORITY, authority);
		parameters.put(AuthorityConformationModule.TARGET_SUBJET_AUTHORITY, "http://example.com/resource/");
		return parameters;
	}

	/**
	 * @return
	 * @author sherif
	 * @param inputDataset 
	 */
	private static Map<String, String> linkingModuleRandomParameter(Model inputDataset) {
		return null;
	}

	/**
	 * @return
	 * @author sherif
	 * @param inputDataset 
	 */
	private Map<String, String> nlpModuleRandomParameter() {
		Map<String, String> parameters = new HashMap<String, String>();
		double r = Math.random();
		if(r > 0.75){
			parameters.put(NLPModule.NER_TYPE, NLPModule.LOCATION);
		}else if(r > 0.5){
			parameters.put(NLPModule.NER_TYPE, NLPModule.PERSON);
		}else if(r > 0.25){
			parameters.put(NLPModule.NER_TYPE, NLPModule.ORGANIZATION);
		}else{
			parameters.put(NLPModule.NER_TYPE, NLPModule.ALL);
		}
		return parameters;
	}

	/**
	 * @param inputDataset
	 * @return
	 * @author sherif
	 */
	private Map<String, String> DereferencingModuleRandomParameter(Model inputDataset) {
		Map<String, String> parameters = new HashMap<String, String>();
		int l = (int) (1 + Math.random() * 5);
		List<String> predicates = getPredicates(inputDataset, l);
		int i = 1;
		for (String p : predicates) {
			parameters.put(DereferencingModule.INPUT_PROPERTY + i++,  p);
			parameters.put(DereferencingModule.OUTPUT_PROPERTY + i++, p + i);
		}
		return parameters;
	}

	/**
	 * @return
	 * @author sherif
	 */
	private static DeerModule getRandomModule() {
		int i = (int) (Math.random() * deerModules.size());
		return deerModules.get(i);
	}

	private static Object getRandomModuleOrOperator(Model inputDataset) {
		if(Math.random()> 0.5){
			return  getRandomModule();
		}else{
			int i = (int) (Math.random() * deerOperators.size());
			return deerOperators.get(i);	
		}
	}

	private static Resource generateDatasetURI() {
		return ResourceFactory.createResource(SPECS.uri + "dataset_" + datasetIndex++);
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		AutoSpecsGenerator g = new AutoSpecsGenerator();
		Model kb = Reader.readModel(args[0]);
		Model m = g.generateSpecs(kb, 5, 0.5);
		m.write(System.out, "TTL");

	}

}
