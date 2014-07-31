/**
 * 
 */
package org.aksw.geolift.workflow.rdfspecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aksw.geolift.io.Reader;
import org.aksw.geolift.modules.Dereferencing.URIDereferencing;
import org.aksw.geolift.modules.conformation.ConformationModule;
import org.aksw.geolift.modules.filter.FilterModule;
import org.aksw.geolift.modules.linking.Linking;
import org.aksw.geolift.modules.nlp.NlpGeoEnricher;
import org.aksw.geolift.operators.MergeOperator;
import org.aksw.geolift.operators.SplitOperator;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.pfunction.library.container;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RDFConfigHandler {
	private static final Logger logger = Logger.getLogger(RDFConfigHandler.class.getName());
	public static 	Model configModel;

	public static void main(String args[]){
		RDFConfigHandler RDFConfigHandler = new RDFConfigHandler();
		RDFConfigHandler.run(args[0]);
	}

	/**
	 * 
	 * @author sherif
	 */
	private void run(String inputFile) {
		configModel =  Reader.readModel(inputFile);
		//		configModel.write(System.out,"TTL");
		List<Resource> finalDatasets = getFinalDatasets();
		logger.info("Output Datasets: " + finalDatasets);
		for(Resource finalDataset : finalDatasets){
			Resource moduleOrOperator = getModuleOrOperator(null, finalDataset);
			List<Resource> inputDatasets = getInputDatasetsUris(moduleOrOperator);
			List<Model> datasetsModels = new ArrayList<Model>();
			for(Resource inputDataset : inputDatasets){
				datasetsModels.add(readDataset(inputDataset));
			}
			Model result = executeModuleOrOperator(moduleOrOperator, datasetsModels);
			logger.info("Lift " + finalDataset + " with " + result.size() + " triples");
//			result.write(System.out,"TTL");
		}
	}


	/**
	 * @param moduleOrOperator
	 * @author sherif
	 */
	private Model executeModuleOrOperator(Resource moduleOrOperator, List<Model> datasetsModels) {
		NodeIterator typeItr = configModel.listObjectsOfProperty(moduleOrOperator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SpecsOntology.Module)){
				return executeModule(moduleOrOperator, datasetsModels);
			}
			else if(type.equals(SpecsOntology.Operator)){
				return executeOperator(moduleOrOperator, datasetsModels);
			}
		}
		return null;
	}
	

	/**
	 * @param module
	 * @param inputDatasets
	 * @return
	 * @author sherif
	 */
	private Model executeModule(Resource module, List<Model> inputDatasets) {
		Model enrichedModel = ModelFactory.createDefaultModel();
		Map<String, String> moduleParameters = getModuleParameters(module);
		NodeIterator typeItr = configModel.listObjectsOfProperty(module, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SpecsOntology.Module)){
				continue;
			}
			if(type.equals(SpecsOntology.NLPModule)){
				NlpGeoEnricher geoEnricher = new NlpGeoEnricher();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SpecsOntology.LinkingModule)){
				Linking geoEnricher = new Linking();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SpecsOntology.DereferencengModule)){
				URIDereferencing geoEnricher = new URIDereferencing();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SpecsOntology.ConformationModule)){
				ConformationModule geoEnricher = new ConformationModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SpecsOntology.FilterModule)){
				FilterModule geoEnricher = new FilterModule();
				enrichedModel = geoEnricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
		}
		logger.error(module + " module is not yet implemented,\n" +
				"Currently,the nlp, linking, dereferencing, filter and conformation modules are only implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}
	

	/**
	 * @param moduleOrOperator
	 * @param inputDatasetsModels
	 * @return
	 * @author sherif
	 */
	private Model executeOperator(Resource operator, List<Model> inputDatasets) {
		Map<String, String> moduleParameters = getModuleParameters(operator);
		NodeIterator typeItr = configModel.listObjectsOfProperty(operator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SpecsOntology.Operator)){
				continue;
			}
			if(type.equals(SpecsOntology.MergeOperator)){
				MergeOperator mergeOperator = new MergeOperator();
				List<Model> resultModels = mergeOperator.process(inputDatasets, moduleParameters);
				return resultModels.get(0);
			}
			if(type.equals(SpecsOntology.SplitOperator)){
				SplitOperator splitOperator = new SplitOperator();
				List<Model> resultModels = splitOperator.process(inputDatasets, moduleParameters);
				return resultModels.get(0);
			}
		}
		logger.error(operator + " operator is not yet implemented,\n" +
				"Currently,the split and merge operators are only implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}

	/**
	 * @param module
	 * @return map of mudule parameters
	 * @author sherif
	 */
	private static Map<String, String> getModuleParameters(RDFNode module) {
		String key = null;
		String value = null;
		Map<String, String> moduleParameters = new HashMap<String, String>();
		StmtIterator stItr = configModel.listStatements((Resource) module, SpecsOntology.hasParameter, (RDFNode) null);
		while(stItr.hasNext()){
			RDFNode parameter =  stItr.next().getObject(); 
			StmtIterator keyItr = configModel.listStatements((Resource) parameter, SpecsOntology.hasKey, (RDFNode) null);
			if(keyItr.hasNext()){
				key =  keyItr.next().getObject().toString(); 
			}
			StmtIterator valueItr = configModel.listStatements((Resource) parameter, SpecsOntology.hasValue, (RDFNode) null);
			if(valueItr.hasNext()){
				value =  valueItr.next().getObject().toString(); 
			}
			moduleParameters.put(key, value);
		}
		return moduleParameters;
	}

	private Model readDataset(Resource dataset){
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SpecsOntology.hasUri);
		if(uriItr.hasNext()){
			// trivial case: read dataset from file/uri/endpoint
			return Reader.readModel(uriItr.next().toString());
		}
		// recursive case: read dataset from previous module/operator output
		Resource moduleOrOperator = getModuleOrOperator(null, dataset);
		Model outputModel = executeModuleOrOperator(moduleOrOperator);
		return outputModel;
	}

	/**
	 * @param moduleOrOperator
	 * @author sherif
	 */
	private Model executeModuleOrOperator(Resource moduleOrOperator) {
		List<Resource> inputDatasetsUris = getInputDatasetsUris(moduleOrOperator);
		List<Model> inputDatasetsModels = new ArrayList<Model>();
		for(Resource inputdatasetUri : inputDatasetsUris){
			inputDatasetsModels.add(readDataset(inputdatasetUri));
		}
		NodeIterator typeItr = configModel.listObjectsOfProperty(moduleOrOperator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SpecsOntology.Module)){
				return executeModule(moduleOrOperator, inputDatasetsModels);
			}
			else if(type.equals(SpecsOntology.Operator)){
				return executeOperator(moduleOrOperator, inputDatasetsModels);
			}
		}
		return null;
	}
	
	/**
	 * @return 	a list of all final output datasets, which are included as output 
	 * 			of some operators/models and not as input to any operator/model
	 * @author sherif
	 */
	private List<Resource> getFinalDatasets(){
		List<Resource> result = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?s1 <" + SpecsOntology.hasOutput + "> ?d. " +
						"FILTER (NOT EXISTS {?s2 <" + SpecsOntology.hasInput + "> ?d . } )}";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	a list of all input datasets to a certain module/operator
	 * @author sherif
	 */
	private List<Resource> getInputDatasetsUris(Resource moduleOrOperator){
		List<Resource> result = new ArrayList<Resource>();
		String s = "<" + moduleOrOperator + ">";
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {"+ s + " <" + SpecsOntology.hasInput + "> ?d. }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	a list of all output datasets to a certain module/operator
	 * @author sherif
	 */
	private List<Resource> getOutputDatasets(Resource moduleOrOperator){
		List<Resource> result = new ArrayList<Resource>();
		String s = "<" + moduleOrOperator + ">";
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {" + s + " <" + SpecsOntology.hasOutput + "> ?d. }";
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
		ResultSet queryResults = qexec.execSelect();
		while(queryResults.hasNext()){
			QuerySolution qs = queryResults.nextSolution();
			Resource dataset = qs.getResource("?d");
			result.add(dataset);
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return 	module/operator for a given in/output dataset. 
	 * @author sherif
	 */
	private Resource getModuleOrOperator(Resource inputDataset, Resource outputDataset){
		Resource result = ResourceFactory.createResource();
		if(inputDataset == null){
			String q = "<" + outputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SpecsOntology.hasOutput + "> " + q + ". }";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}
		else if(outputDataset == null){
			String q = "<" + inputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SpecsOntology.hasInput + "> " + q + ". }";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}else{
			String in = "<" + inputDataset + ">";
			String out = "<" + outputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SpecsOntology.hasInput + "> " + in + ". " +
							"?s <" + SpecsOntology.hasOutput + "> " + out + ".}";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, configModel);
			ResultSet queryResults = qexec.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.nextSolution();
				result = qs.getResource("?s");
			}
			qexec.close() ;
		}
		return result;
	}

}
