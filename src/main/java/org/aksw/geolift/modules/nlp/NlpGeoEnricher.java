package org.aksw.geolift.modules.nlp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.aksw.geolift.modules.GeoLiftModule;
//import org.junit.Test;
import org.apache.log4j.Logger;

/**
 *
 * @author sherif
 */
public class NlpGeoEnricher implements GeoLiftModule{

	private static final Logger logger = Logger.getLogger(NlpGeoEnricher.class.getName());
	private Model model;

	// parameters list
	private Property 	LiteralProperty;
	private String 	useFoxLight			= "OFF"; //"org.aksw.fox.nertools.NERStanford"; ;
	private boolean 	askEndPoint 	= false;
	private String 		foxType 		= "TEXT";
	private String 		foxTask 		= "NER";
	private String 		foxInput 		= "";
	private String 		foxOutput		= "Turtle";
	private boolean 	foxUseNif		= false;
	private boolean 	foxReturnHtml 	= false;
	private String 		inputFile		= "";
	private String 		outputFile		= "";
	private Property 	addedGeoProperty= ResourceFactory.createProperty("http://geoknow.org/ontology/relatedTo");


	/**
	 * @return the relatedToProperty
	 */
	public Property getRelatedToProperty() {
		return addedGeoProperty;
	}

	/**
	 * @param relatedToProperty the relatedToProperty to set
	 */
	public void setRelatedToProperty(Property relatedToProperty) {
		this.addedGeoProperty = relatedToProperty;
	}

	/**
	 * @return the outputFile
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @return the foxType
	 */
	public String getFoxType() {
		return foxType;
	}

	/**
	 * @return the foxTask
	 */
	public String getFoxTask() {
		return foxTask;
	}

	/**
	 * @return the foxInput
	 */
	public String getFoxInput() {
		return foxInput;
	}

	/**
	 * @return the foxOutput
	 */
	public String getFoxOutput() {
		return foxOutput;
	}

	/**
	 * @return the foxUseNif
	 */
	public boolean isFoxUseNif() {
		return foxUseNif;
	}

	/**
	 * @return the foxReturnHtml
	 */
	public boolean isFoxReturnHtml() {
		return foxReturnHtml;
	}

	/**
	 * @param foxType the foxType to set
	 */
	public void setFoxType(String foxType) {
		this.foxType = foxType;
	}

	/**
	 * @param foxTask the foxTask to set
	 */
	public void setFoxTask(String foxTask) {
		this.foxTask = foxTask;
	}

	/**
	 * @param foxInput the foxInput to set
	 */
	public void setFoxInput(String foxInput) {
		this.foxInput = foxInput;
	}

	/**
	 * @param foxOutput the foxOutput to set
	 */
	public void setFoxOutput(String foxOutput) {
		this.foxOutput = foxOutput;
	}

	/**
	 * @param foxUseNif the foxUseNif to set
	 */
	public void setFoxUseNif(boolean foxUseNif) {
		this.foxUseNif = foxUseNif;
	}

	/**
	 * @param foxReturnHtml the foxReturnHtml to set
	 */
	public void setFoxReturnHtml(boolean foxReturnHtml) {
		this.foxReturnHtml = foxReturnHtml;
	}


	/**
	 * @return the askEndPoint
	 */
	public boolean isAskEndPoint() {
		return askEndPoint;
	}

	/**
	 * @param askEndPoint the askEndPoint to set
	 */
	public void setAskEndPoint(boolean askEndPoint) {
		this.askEndPoint = askEndPoint;
	}

	/**
	 * @return the useFoxLight
	 */
	public String getUseFoxLight() {
		return useFoxLight;
	}

	/**
	 * @param useFoxLight the useFoxLight to set
	 */
	public void setUseFoxLight(String useFL) {
		useFoxLight = useFL;
	}

	/**
	 * @param model
	 * @param literalProperty
	 *@author sherif
	 */
	public NlpGeoEnricher(Model model, Property literalProperty) {
		super();
		this.model = model;
		this.LiteralProperty = literalProperty;
	}

	public NlpGeoEnricher(String fileNameOrUri, String literalPropartyUri) {
		super();
		this.model = loadModel(fileNameOrUri);
		this.LiteralProperty = ResourceFactory.createProperty(literalPropartyUri);
	}

	/**
	 * 
	 *@author sherif
	 */
	public NlpGeoEnricher() {
		super();
		this.model = null;
		this.LiteralProperty = null;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}


	/**
	 * @return the literalProperty
	 */
	public Property getliteralProperty() {
		return LiteralProperty;
	}


	/**
	 * @param model the model to setModel
	 */
	public void setModel(Model model) {
		this.model = model;
	}


	/**
	 * @param LiteralProperty the literalProperty to set
	 */
	public void setliteralProperty(Property p) {
		this.LiteralProperty = p;
	}


	public Model getNamedEntityModel( String inputText){
		String buffer = getNamedEntity(foxType, foxTask, foxOutput, inputText, useFoxLight, foxUseNif, foxReturnHtml);

		ByteArrayInputStream stream=null;
		try {
			stream = new ByteArrayInputStream(buffer.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Model NamedEntitymodel = ModelFactory.createDefaultModel();
		if(buffer.contains("<!--")){
			return NamedEntitymodel;
		}
		NamedEntitymodel.read(stream, "", "TTL");
		return NamedEntitymodel;
	}




	public String refineString(String inputString){
		String outputString=inputString;
		outputString.replace("<", "").replace(">", "").replace("//", "");
		return outputString;
	}


	private String getNamedEntity_old_FOX(String type, String task, String output, String text){
		String buffer = "", line; 
		boolean error = true;
		while (error) {
			try {
				text=refineString(text);
				// Construct data
				String data = URLEncoder.encode("type",	 	"UTF-8") 	+ "=" + URLEncoder.encode(type, 	"UTF-8");
				data += "&" + URLEncoder.encode("task",		"UTF-8") 	+ "=" + URLEncoder.encode(task,	 	"UTF-8");
				data += "&" + URLEncoder.encode("output", 	"UTF-8") 	+ "=" + URLEncoder.encode(output, 	"UTF-8");
				data += "&" + URLEncoder.encode("text", 	"UTF-8") 	+ "=" + URLEncoder.encode(text, 	"UTF-8");
				// Send data
				URL url = new URL("http://139.18.2.164:4444/api");
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();

				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				while ((line = rd.readLine()) != null) {
					buffer = buffer + line + "\n";
				}
				wr.close();
				rd.close();
				error = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return buffer;
	}



	/**
	 * @param type: text or an url (e.g.: `G. W. Leibniz was born in Leipzig`, `http://en.wikipedia.org/wiki/Leipzig_University`)
	 * @param task: { NER }
	 * @param output: { JSON-LD | N-Triples | RDF/{ JSON | XML } | Turtle | TriG | N-Quads}
	 * @param input: text or an url
	 * @param foxlight: an implemented INER class name (e.g.: `org.aksw.fox.nertools.NEROpenNLP`) or `OFF`. 
	 * 		org.aksw.fox.nertools.NERIllinoisExtended
	 * 		org.aksw.fox.nertools.NEROpenNLP
	 * 		org.aksw.fox.nertools.NERBalie
	 * 		org.aksw.fox.nertools.NERStanford
	 * @param nif: { true | false }
	 * @param returnHtml: { true | false }
	 * @return Named entity buffer containing annotation of the input text
	 * @author sherif
	 */
	private String getNamedEntity(String type, String task, String output, String input, String foxlight, boolean nif, boolean returnHtml){
		String buffer = "", line; 
		boolean error = true;
		while (error) {
			try {
				input=refineString(input);
				// Construct data
				String data = URLEncoder.encode("type",	 	"UTF-8") 	+ "=" + URLEncoder.encode(type,						    "UTF-8");
				data += "&" + URLEncoder.encode("task",		"UTF-8") 	+ "=" + URLEncoder.encode(task, 						"UTF-8");
				data += "&" + URLEncoder.encode("output", 	"UTF-8") 	+ "=" + URLEncoder.encode(output,						"UTF-8");
				data += "&" + URLEncoder.encode("input", 	"UTF-8") 	+ "=" + URLEncoder.encode(input, 						"UTF-8");
				data += "&" + URLEncoder.encode("foxlight", "UTF-8")	+ "=" + URLEncoder.encode(foxlight,						"UTF-8");
				data += "&" + URLEncoder.encode("nif", 		"UTF-8")	+ "=" + URLEncoder.encode((nif)       ? "TRUE":"FALSE", "UTF-8");
				data += "&" + URLEncoder.encode("returnHtml", "UTF-8")	+ "=" + URLEncoder.encode((returnHtml)? "TRUE":"FALSE", "UTF-8");

				// Send data
				URL url = new URL("http://139.18.2.164:4444/api");
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();

				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				while ((line = rd.readLine()) != null) {
					buffer = buffer + line + "\n";
				}
				wr.close();
				rd.close();
				error = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//TODO use a JASON parser

		buffer= URLDecoder.decode(buffer); 
		buffer = buffer.substring(buffer.indexOf("@"), buffer.lastIndexOf("log")-4).toString();

		return buffer;
	}


	/**
	 * @param namedEntityModel
	 * @param subject
	 * @return model of places contained in the input model 
	 * @author sherif
	 */
	public Model getPlaces(Model namedEntityModel, RDFNode subject){

		Model resultModel = ModelFactory.createDefaultModel();

		String sparqlQueryString= 	"CONSTRUCT {?s ?p ?o} " +
				" WHERE {?s a <http://ns.aksw.org/scms/annotations/LOCATION>. ?s ?p ?o} " ;
		QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(sparqlQueryString, namedEntityModel);
		Model locationsModel =qexec.execConstruct();

		Property meansProperty = ResourceFactory.createProperty("http://ns.aksw.org/scms/means");
		NodeIterator objectsIter = locationsModel.listObjectsOfProperty(meansProperty);

		if(askEndPoint){
			while (objectsIter.hasNext()) {
				RDFNode object = objectsIter.nextNode();
				if(object.isResource()){
					if(isPlace(object)){
						resultModel.add( (Resource) subject , addedGeoProperty, object);
						//					TODO add more data ??
						logger.info("<" + subject.toString() + "> <" + addedGeoProperty + "> <" + object + ">");
					}	
				}
			}
		}
		else{
			while (objectsIter.hasNext()) {
				RDFNode object = objectsIter.nextNode();
				if(object.isResource()){

					resultModel.add( (Resource) subject , addedGeoProperty, object);
					//					TODO add more data ??
					logger.info("<" + subject.toString() + "> <" + addedGeoProperty + "> <" + object + ">");
				}
			}
		}
		return resultModel;
	}

	/**
	 * @param fileNameOrUri
	 * @return loaded model from input file/URI
	 * @author sherif
	 */
	public Model loadModel(String fileNameOrUri){
		model=ModelFactory.createDefaultModel();
		java.io.InputStream in = FileManager.get().open( fileNameOrUri );
		if (in == null) {
			throw new IllegalArgumentException(
					"File: " + fileNameOrUri + " not found");
		}
		if(fileNameOrUri.contains(".ttl")){
			logger.info("Opening Turtle file ...");
			model.read(in, null, "TTL");
		}else if(fileNameOrUri.contains(".rdf")){
			logger.info("Opening RDF/XML file ...");
			model.read(in, null);
		}else if(fileNameOrUri.contains(".nt")){
			logger.info("Opening N-Triples file ...");
			model.read(in, null, "N-TRIPLE");
		}else{
			logger.info("Content negotiation to get RDF/XML from " + fileNameOrUri + " ...");
			model.read(fileNameOrUri);
		}
		logger.info("Loading "+ fileNameOrUri + " is done!!");
		return model;
	}



	/**
	 * @param uri
	 * @return wither is the input URI is a place of not
	 * @author sherif
	 */
	private boolean isPlace(RDFNode uri){
		boolean result=false;
		if(uri.toString().contains("http://ns.aksw.org/scms/"))
			return false;
		String queryString="ask {<" +uri.toString() + "> a <http://dbpedia.org/ontology/Place>}";
		logger.info("Asking DBpedia for: "+ queryString);
		Query query = QueryFactory.create(queryString);
		//		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
		result = qexec.execAsk();
		logger.info("Answer: " + result);
		return result;
	}


	/**
	 * @param limit
	 * @return just a TEST 
	 * @author sherif
	 */
	public List<String> getDBpediaAbstaracts(Integer limit){
		List<String> result = new ArrayList<String>();

		String queryString = "SELECT distinct ?o WHERE {" +
				"?s a <http://dbpedia.org/ontology/Place>." +
				"?s <http://dbpedia.org/ontology/abstract> ?o } LIMIT " + limit.toString();
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		ResultSet queryResults = qexec.execSelect();  
		while(queryResults.hasNext()){
			QuerySolution qs=queryResults.nextSolution();
			result.add( qs.getLiteral("o").toString());
		}
		qexec.close() ;
		return result;
	}

	/**
	 * @return Geo-spatial enriched model
	 * @author sherif
	 */
	public Model nlpEnrichGeoTriples(){

		Model resultModel = model;
		StmtIterator stItr = model.listStatements(null, LiteralProperty, (RDFNode) null);
		logger.info("--------------- Added triples through  NLP ---------------");
		while (stItr.hasNext()) {
			Statement st = stItr.nextStatement();
			RDFNode object = st.getObject();
			RDFNode subject = st.getSubject();
			if(object.isLiteral()){
				if(!object.asLiteral().toString().contains("^^")){
					Model namedEntityModel = getNamedEntityModel(object.toString().substring(0,object.toString().lastIndexOf("@")));

					if(!namedEntityModel.isEmpty()){
						resultModel= resultModel.union(getPlaces(namedEntityModel, subject));
					}				
				}
			}
		}
		return resultModel;
	}


	public Model enrichModel(){
		return model.union(nlpEnrichGeoTriples());
	}


	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#process(com.hp.hpl.jena.rdf.model.Model, java.util.Map)
	 */
	public Model process(Model inputModel, Map<String, String> parameters){
		logger.info("--------------- NLP Module ---------------");
		model = inputModel;
		if( parameters.containsKey("input")){
			inputFile = parameters.get("input");
			model = loadModel(inputFile);
		}	
		if( parameters.containsKey("literalProperty"))
			LiteralProperty = ResourceFactory.createProperty(parameters.get("literalProperty"));
		else{
			LiteralPropertyRanker lpr = new LiteralPropertyRanker(model)	;
			LiteralProperty = lpr.getTopRankedLiteralProperty();
			logger.info("Top ranked Literal Property: " + LiteralProperty); 
		}
		if( parameters.containsKey("addedGeoProperty"))
			addedGeoProperty = ResourceFactory.createProperty("addedGeoProperty");
		if( parameters.containsKey("useFoxLight"))
			useFoxLight = parameters.get("useFoxLight").toLowerCase();
		if( parameters.containsKey("askEndPoint"))
			askEndPoint = parameters.get("askEndPoint").toLowerCase().equals("true")? true : false;
		if( parameters.containsKey("foxType"))
			foxType = parameters.get("foxType").toUpperCase();
		if( parameters.containsKey("foxTask"))
			foxTask = parameters.get("foxTask").toUpperCase();
		if( parameters.containsKey("foxInput"))
			foxInput = parameters.get("foxInput");
		if( parameters.containsKey("foxOutput"))
			foxOutput = parameters.get("foxOutput");
		if( parameters.containsKey("foxUseNif"))
			foxUseNif = parameters.get("foxUseNif").toLowerCase().equals("true")? true : false;
		if( parameters.containsKey("foxReturnHtml"))
			foxReturnHtml = parameters.get("foxReturnHtml").toLowerCase().equals("true")? true : false;

		Model enrichedModel = nlpEnrichGeoTriples();

		if( parameters.containsKey("output")){
			outputFile = parameters.get("output");
			FileWriter outFile = null;
			try {
				outFile = new FileWriter(outputFile);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			enrichedModel.write(outFile,"TURTLE");
		}
		return enrichedModel;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<String>();
//		parameters.add("input");
//		parameters.add("output");
		parameters.add("literalProperty");
		parameters.add("useFoxLight");
		parameters.add("askEndPoint");
		parameters.add("foxType");
		parameters.add("foxTask");
		parameters.add("foxInput");
		parameters.add("foxOutput");
		parameters.add("foxUseNif");
		parameters.add("foxReturnHtml");
		parameters.add("addedGeoProperty");
		return parameters;
	}
	
	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		return parameters;
	}

	public static void main(String args[]) throws IOException {
		NlpGeoEnricher geoEnricher= new NlpGeoEnricher();

		Map<String, String> parameters = new HashMap<String, String>();

		// set parameters from command line
		for(int i=0; i<args.length; i+=2){
			if(args[i].equals("-i") || args[i].toLowerCase().equals("--input")){
				parameters.put("input",   args[i+1]);
			}
			if(args[i].equals("-o") || args[i].toLowerCase().equals("--output")){
				parameters.put("output",   args[i+1]);
			}
			if(args[i].equals("-p") || args[i].toLowerCase().equals("--literalProperty")){
				parameters.put("literalProperty",   args[i+1]);
			}
			if(args[i].equals("-l") || args[i].toLowerCase().equals("--useFoxLight")){
				parameters.put("useFoxLight",   args[i+1]);
			}
			if(args[i].equals("-e") || args[i].toLowerCase().equals("--askEndPoint")){
				parameters.put("askEndPoint",   args[i+1]);
			}
			if(args[i].equals("-p") || args[i].toLowerCase().equals("--literalProperty")){
				parameters.put("LiteralProperty",   args[i+1]);}
			if(args[i].equals("-?") || args[i].toLowerCase().equals("--help")){
				logger.info(
						"Basic parameters:\n" +
								"\t-i --input: input file/URI" + "\n" +
								"\t-o --output: output file/URI" + "\n" +
								"\t-p --literalProperty: literal property used for NER" + "\n" +
								"\t-l --useFoxLight: foxlight: an implemented INER class name (e.g.: `org.aksw.fox.nertools.NEROpenNLP`) or `OFF`." + "\n" +
										  		"\t org.aksw.fox.nertools.NERIllinoisExtended"+ "\n" +
										  		"\t org.aksw.fox.nertools.NEROpenNLP"+ "\n" +
										  		"\t org.aksw.fox.nertools.NERBalie"+ "\n" +
										  		"\t org.aksw.fox.nertools.NERStanford" + "\n" +
								"\t-e --askEndPoint: { true | false}"+ "\n" +
								"Fox parameters (current version use always default values, which is the first one):\n"+
								"\t--foxType: { text | url }" + "\n" +
								"\t--foxTask: { NER }" + "\n" +
								"\t--foxInput: text or an url" + "\n" +
								"\t--foxOutput: { JSON-LD | N-Triples | RDF/{ JSON | XML } | Turtle | TriG | N-Quads}" + "\n" +
								"\t--foxUseNif: { false | true }" + "\n" +
								"\t--foxReturnHtml: { false | true }" );
				System.exit(0);
			}
		} 
		if(!parameters.containsKey("input")){
			logger.error("No input file/URI, Exit with error!!");
			System.exit(1);
		}

		Model enrichedModel = geoEnricher.process(null, parameters);

		if(!parameters.containsKey("output")){
			logger.info("Enriched MODEL:");
			logger.info("---------------");
			enrichedModel.write(System.out,"TTL");
		}
	}
}
