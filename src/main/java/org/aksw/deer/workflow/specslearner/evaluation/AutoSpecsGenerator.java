/**
 * 
 */
package org.aksw.deer.workflow.specslearner.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.helper.vacabularies.SPECS;
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
import org.aksw.deer.workflow.rdfspecs.RDFConfigWriter;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
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
	 * @param inputDataset
	 * @param complexity (the maximum number of modules/operators included in the resulted configuration)
	 * @param isComplex (if set generates a configuration file contains operators + modules,
	 * 			otherwise the configuration file will contain only modules) 
	 * @return a random configuration file with complexity ≤ complexity 
	 * @author sherif
	 */
	public static Model generateSpecs(Model inputDataset, int complexity, boolean isComplex){	
		if(!isComplex){
			Resource inputDatasetUri = generateDatasetURI();
			for(int i = 0 ; i < complexity ; i++){
				DeerModule module = getRandomModule(inputDataset);
				Map<String, String> parameters = generateRandomParameters(module, inputDataset);
				Resource outputDatasetUri = generateDatasetURI();
				RDFConfigWriter.addModule(module, parameters, specsModel, inputDatasetUri, outputDatasetUri);	
				outputDatasetUri = inputDatasetUri;
			}
		}
		return specsModel;
	}

	/**
	 * @param module
	 * @return
	 * @author sherif
	 */
	private static Map<String, String> generateRandomParameters(DeerModule module, Model inputDataset) {
		Map<String, String> parameters = new HashMap<String, String>();
		if(module instanceof LinkingModule){
			return null;
		}else 
			if(module instanceof DereferencingModule){
			int l = (int) (1 + Math.random() * 5);
			String sparqlQueryString= "SELECT DISTINCT ?p {?s ?p ?o} LIMIT " + l;
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, inputDataset);
			ResultSet queryResults = qexec.execSelect();
			int i = 1;
			while(queryResults.hasNext()){
				QuerySolution qs=queryResults.nextSolution();
				String p = qs.getResource("?p").toString();
				parameters.put(DereferencingModule.INPUT_PROPERTY + i++, p);
			}
			qexec.close() ;
		}
		else 
			if(module instanceof NLPModule){
			double r = Math.random();
			if(r > 0.75){
				parameters.put(NLPModule.NER_TYPE, NLPModule.LOCATION);
			}if(r > 0.5){
				parameters.put(NLPModule.NER_TYPE, NLPModule.PERSON);
			}if(r > 0.25){
				parameters.put(NLPModule.NER_TYPE, NLPModule.ORGANIZATION);
			}else{
				parameters.put(NLPModule.NER_TYPE, NLPModule.ALL);
			}
			
		}else 
			if(module instanceof AuthorityConformationModule){
			return null;
		}else if(module instanceof PredicateConformationModule){
			return null;
		}else if(module instanceof FilterModule){
			return null;
		}
		//TODO add a random parameter generator for new modules here
		return parameters;
	}

	/**
	 * @param inputDataset
	 * @return
	 * @author sherif
	 */
	private static DeerModule getRandomModule(Model inputDataset) {
		int i = (int) (Math.random() * deerModules.size());
		return deerModules.get(i);
	}
	
	private static Resource generateDatasetURI() {
		return ResourceFactory.createResource(SPECS.uri + "dataset_" + datasetIndex++);
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
