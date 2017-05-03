/**
 * 
 */
package org.aksw.deer.workflow.rdfspecs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.deer.helper.datastructure.RunContext;
import org.aksw.deer.helper.vocabularies.SPECS;
import org.aksw.deer.io.Reader;
import org.aksw.deer.io.Writer;
import org.aksw.deer.modules.dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;
import org.aksw.deer.operators.MergeOperator;
import org.aksw.deer.operators.CloneOperator;
import org.apache.log4j.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * @author sherif
 *
 */
public class RFDConfigExecutor {

    private static final Logger logger = Logger.getLogger(RFDConfigExecutor.class.getName());

	private Model configModel;
	private RunContext context;
	private Reader reader;

	public RFDConfigExecutor(String modelUrl, RunContext context) throws IOException{
	    this.context = context;
	    this.reader = new Reader(context.getSubDir());
		this.configModel = reader.readModel(modelUrl);
	}

	/**
	 * execute the input RDF config file and return set of all enriched dataset
	 * @param inputFile 
	 * @author sherif
	 * @throws IOException 
	 */
	public Set<Model> execute() throws IOException{
		Set<Model> result = new HashSet<Model>();
		//		configModel.write(System.out,"TTL");
		List<Resource> finalDatasets = getFinalDatasets();
		logger.info("Found " + finalDatasets.size() + " output Datasets: " + finalDatasets);
		for(Resource finalDataset : finalDatasets){
			result.add(readDataset(finalDataset));
		}
		return result;
	}

	/**
	 * execute the input RDF configuration file and return only the first enriched dataset
	 * Suitable for simple liner configuration file 
	 * @param config
	 * @return
	 * @throws IOException
	 * @author sherif
	 */
	public Model simpleExecute() throws IOException{
		return execute().iterator().next();
	}


	/**
	 * @param module
	 * @param inputDatasets
	 * @return model resulted after executing the input module
	 * @author sherif
	 */
	private Model executeModule(Resource module, List<Model> inputDatasets) {
		Model enrichedModel = ModelFactory.createDefaultModel();
		Map<String, String> moduleParameters = getParameters(module);
		NodeIterator typeItr = configModel.listObjectsOfProperty(module, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Module)){
				continue;
			}
			if(type.equals(SPECS.NLPModule)){
				NLPModule enricher = new NLPModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.LinkingModule)){
				LinkingModule enricher = new LinkingModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.DereferencingModule)){
				DereferencingModule enricher = new DereferencingModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.AuthorityConformationModule)){
				AuthorityConformationModule enricher = new AuthorityConformationModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.PredicateConformationModule)){
				PredicateConformationModule enricher = new PredicateConformationModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
			if(type.equals(SPECS.FilterModule)){
				FilterModule enricher = new FilterModule();
				enrichedModel = enricher.process(inputDatasets.get(0), moduleParameters);
				return enrichedModel;
			}
		}
		logger.error(module + " module is not yet implemented,\n" +
				"Currently,the nlp, linking, dereferencing, filter, authority conformation " +
				"and predicate conformation modules are only implemented\n" +
				"Exit with error ...");
		System.exit(1);
		return null;
	}


	/**
	 * @param moduleOrOperator
	 * @param inputDatasetsModels
	 * @return model resulted after executing the input operator
	 * @author sherif
	 */
	private Model executeOperator(Resource operator, List<Model> inputDatasets) {
		Map<String, String> moduleParameters = getParameters(operator);
		NodeIterator typeItr = configModel.listObjectsOfProperty(operator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Operator)){
				continue;
			}
			if(type.equals(SPECS.MergeOperator)){
				MergeOperator mergeOperator = new MergeOperator();
				List<Model> resultModels = mergeOperator.process(inputDatasets, moduleParameters);
				return resultModels.get(0);
			}
			if(type.equals(SPECS.CloneOperator)){
				CloneOperator splitOperator = new CloneOperator();
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
	 * @param moduleOrOperator
	 * @return map of mudule parameters
	 * @author sherif
	 */
	private Map<String, String> getParameters(RDFNode moduleOrOperator) {
		String key = null;
		String value = null;
		Map<String, String> moduleParameters = new HashMap<String, String>();
		StmtIterator stItr = configModel.listStatements((Resource) moduleOrOperator, SPECS.hasParameter, (RDFNode) null);
		while(stItr.hasNext()){
			RDFNode parameter =  stItr.next().getObject(); 
			StmtIterator keyItr = configModel.listStatements((Resource) parameter, SPECS.hasKey, (RDFNode) null);
			if(keyItr.hasNext()){
				key =  keyItr.next().getObject().toString(); 
			}
			StmtIterator valueItr = configModel.listStatements((Resource) parameter, SPECS.hasValue, (RDFNode) null);
			if(valueItr.hasNext()){
				value =  valueItr.next().getObject().toString(); 
			}
			moduleParameters.put(key, value);
		}
		return moduleParameters;
	}

	/**
	 * @param dataset
	 * @return dataset model from file/uri/endpoint
	 * @author sherif
	 * @throws IOException 
	 */
	private Model readDataset(Resource dataset) throws IOException {
		// trivial case: read dataset from file/uri/endpoint
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SPECS.fromEndPoint);
		if(uriItr.hasNext()){
			//			Model cbd = readCBD(dataset, uriItr.next().toString());
			Model cbd = readDatasetFromEndPoint(dataset, uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}
		uriItr = configModel.listObjectsOfProperty(dataset, SPECS.hasUri);
		if(uriItr.hasNext()){
			Model cbd = reader.readModel(uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}
		uriItr = configModel.listObjectsOfProperty(dataset, SPECS.inputFile);
		if(uriItr.hasNext()){
			Model cbd = reader.readModel(uriItr.next().toString());
			writeDataset(dataset,cbd);
			return cbd;
		}

		// recursive case: read dataset from previous module/operator output
		Resource moduleOrOperator = getModuleOrOperator(null, dataset);
		Model outputModel = executeModuleOrOperator(moduleOrOperator);
		writeDataset(dataset,outputModel);
		return outputModel;
	}

	/**
	 * @return
	 * @author sherif
	 * @param string 
	 * @param dataset 
	 */
	private Model readDatasetFromEndPoint(Resource dataset, String endpointUri) {
		long startTime = System.currentTimeMillis();
		Model result = ModelFactory.createDefaultModel();
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SPECS.fromGraph);
		if(uriItr.hasNext()){
			String graphUri = uriItr.next().toString();
			uriItr = configModel.listObjectsOfProperty(dataset, SPECS.graphTriplePattern);
			String triplePattern = "?s ?p ?o";
			if(uriItr.hasNext()){
				triplePattern = uriItr.next().toString();
			}
			String sparqlQueryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + graphUri +"> { " + triplePattern + " } . }";
			logger.info("Reading dataset  " + dataset + " from " + endpointUri + " using SPARQL: " + sparqlQueryString);
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUri, sparqlQueryString);
			result = qexec.execConstruct();
			qexec.close() ;
			logger.info("Dataset reading is done in " + (System.currentTimeMillis() - startTime) + "ms, " + 
					result.size() + " triples found.");
		}else{
			uriItr = configModel.listObjectsOfProperty(dataset, SPECS.hasUri);
			if(uriItr.hasNext()){
				String uri = uriItr.next().toString();
				logger.info("Generating CBD for " + uri + " from " + endpointUri+ "...");
				String sparqlQueryString = "DESCRIBE <" + uri + ">";
				QueryFactory.create(sparqlQueryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUri, sparqlQueryString);
				result = qexec.execDescribe();
				qexec.close() ;
				logger.info("Generating CBD is done in " + (System.currentTimeMillis() - startTime) + "ms, " + 
						result.size() + " triples found.");
			}else{
				logger.error("Neither " + SPECS.hasUri + " nor " + SPECS.fromGraph + 
						" defined to generate dataset " + dataset +" from " + endpointUri 
						+ ", exit with error." );
				System.exit(1);
			}
		}
		return result;
	}

	/**
	 * @param datasetUri
	 * @param dataSetModel
	 * @throws IOException
	 * @author sherif
	 */
	private void writeDataset(Resource datasetUri, Model dataSetModel) throws IOException{
		NodeIterator uriItr = configModel.listObjectsOfProperty(datasetUri, SPECS.outputFile);
		if(uriItr.hasNext()){
			String outputFile = uriItr.next().toString();
			String outputFormat = "TTL"; // turtle is default format
			uriItr = configModel.listObjectsOfProperty(datasetUri, SPECS.outputFormat);
			if(uriItr.hasNext()){
				outputFormat = uriItr.next().toString();
			}
            (new Writer(context.getSubDir())).writeModel(dataSetModel, outputFormat, outputFile);
		}else{ 
			return;
		}
	}

	/**
	 * @param string
	 * @return
	 * @author sherif
	 */
	@SuppressWarnings("unused")
	private Model readCBD(Resource dataset, String endpointUri) {
		long startTime = System.currentTimeMillis();
		Model result = ModelFactory.createDefaultModel();
		NodeIterator uriItr = configModel.listObjectsOfProperty(dataset, SPECS.hasUri);
		if(uriItr.hasNext()){
			String uri = uriItr.next().toString();
			logger.info("Generating CBD for " + uri + " from " + endpointUri+ "...");
			String sparqlQueryString = "DESCRIBE <" + uri + ">";
			QueryFactory.create(sparqlQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUri, sparqlQueryString);
			result = qexec.execDescribe();
			qexec.close() ;
			logger.info("Generating CBD is done in " + (System.currentTimeMillis() - startTime) + "ms, " + 
					result.size() + " triples found.");
		}else{
			logger.error("No " + SPECS.hasUri + " defined to generate CBD from " + endpointUri 
					+ ", exit with error." );
			System.exit(1);
		}
		return result;
	}

	/**
	 * @param moduleOrOperator
	 * @author sherif
	 * @throws IOException 
	 */
	private Model executeModuleOrOperator(Resource moduleOrOperator) throws IOException {
		List<Resource> inputDatasetsUris = getInputDatasetsUris(moduleOrOperator);
		List<Model> inputDatasetsModels = new ArrayList<Model>();
		for(Resource inputdatasetUri : inputDatasetsUris){
			inputDatasetsModels.add(readDataset(inputdatasetUri));
		}
		NodeIterator typeItr = configModel.listObjectsOfProperty(moduleOrOperator, RDF.type);
		while(typeItr.hasNext()){
			RDFNode type = typeItr.next();
			if(type.equals(SPECS.Module)){
				return executeModule(moduleOrOperator, inputDatasetsModels);
			}
			else if(type.equals(SPECS.Operator)){
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
	public List<Resource> getFinalDatasets(){
		List<Resource> result = new ArrayList<Resource>();
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {?s1 <" + SPECS.hasOutput + "> ?d. " +
						"FILTER (NOT EXISTS {?s2 <" + SPECS.hasInput + "> ?d . } )}";
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
				"SELECT DISTINCT ?d {"+ s + " <" + SPECS.hasInput + "> ?d. }";
		//		System.out.println("sparqlQueryString: " + sparqlQueryString);
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
	@SuppressWarnings("unused")
	private List<Resource> getOutputDatasets(Resource moduleOrOperator){
		List<Resource> result = new ArrayList<Resource>();
		String s = "<" + moduleOrOperator + ">";
		String sparqlQueryString = 
				"SELECT DISTINCT ?d {" + s + " <" + SPECS.hasOutput + "> ?d. }";
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
	 * @return 	module/operator for a given input/output dataset. 
	 * @author sherif
	 */
	private Resource getModuleOrOperator(Resource inputDataset, Resource outputDataset){
		if(inputDataset == null && outputDataset == null){
			return null;
		}
		Resource result = ResourceFactory.createResource();
		if(inputDataset == null){
			String q = "<" + outputDataset + ">";
			String sparqlQueryString = 
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasOutput + "> " + q + ". }";
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
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasInput + "> " + q + ". }";
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
					"SELECT DISTINCT ?s { ?s <" + SPECS.hasInput + "> " + in + ". " +
							"?s <" + SPECS.hasOutput + "> " + out + ".}";
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

	//	public static DeerModule getLastModule(){
	//		List<Resource> finalDatasets = getFinalDatasets();
	//		return (DeerModule) getModuleOrOperator(null, finalDatasets.get(0));
	//	}

}
