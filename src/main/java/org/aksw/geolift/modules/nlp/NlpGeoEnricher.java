package org.aksw.geolift.modules.nlp;


import java.util.ArrayList;
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
import java.net.URLEncoder;
import java.util.TreeSet;

import org.aksw.geolift.modules.GeoLiftModule;
import org.junit.Test;

/**
 *
 * @author sherif
 */
public class NlpGeoEnricher implements GeoLiftModule{

	Model model;
	Property litralProperty;

	/**
	 * @param model
	 * @param litralProperty
	 *@author sherif
	 */
	public NlpGeoEnricher(Model model, Property litralProperty) {
		super();
		this.model = model;
		this.litralProperty = litralProperty;
	}

	public NlpGeoEnricher(String fileNameOrUri, String litralPropartyUri) {
		super();
		this.model = loadModel(fileNameOrUri);
		this.litralProperty = ResourceFactory.createProperty(litralPropartyUri);
	}

	/**
	 * 
	 *@author sherif
	 */
	public NlpGeoEnricher() {
		super();
		this.model = null;
		this.litralProperty = null;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}


	/**
	 * @return the litralProperty
	 */
	public Property getLitralProperty() {
		return litralProperty;
	}


	/**
	 * @param model the model to setModel
	 */
	public void setModel(Model model) {
		this.model = model;
	}


	/**
	 * @param litralProperty the litralProperty to set
	 */
	public void setLitralProperty(Property p) {
		this.litralProperty = p;
	}


	public Model getNamedEntityModel( String inputText){
		String buffer = getNamedEntity("text","NER","turtle", inputText);

		System.out.println(buffer);

		ByteArrayInputStream stream=null;
		try {
			stream = new ByteArrayInputStream(buffer.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Model NamedEntitymodel = ModelFactory.createDefaultModel();
		//		System.out.println(buffer);
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


	private String getNamedEntity(String type, String task, String output, String text){
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


	public Model getPlaces(Model namedEntityModel, RDFNode subject){

		Model resultModel=ModelFactory.createDefaultModel();
		//		NodeIterator objectsIter = namedEntityModel.listObjects();
		Property meansProperty = ResourceFactory.createProperty("http://ns.aksw.org/scms/means");
		NodeIterator objectsIter = namedEntityModel.listObjectsOfProperty(meansProperty);
		while (objectsIter.hasNext()) {
			RDFNode object = objectsIter.nextNode();

			System.out.println("OBJECT:"+object);

			if(object.isResource()){
				if(isPlace(object)){
					Property property= ResourceFactory.createProperty("http://geoknow.org/ontology/relatedTo");
					resultModel.add( (Resource) subject , property, object);
					//TODO add more data ??
					System.out.println("--------------->"+subject.toString() + property +object);
				}	
			}
		}
		return resultModel;
	}

	public Model loadModel(String fileNameOrUri){
		model=ModelFactory.createDefaultModel();
		java.io.InputStream in = FileManager.get().open( fileNameOrUri );
		if (in == null) {
			throw new IllegalArgumentException(
					"File: " + fileNameOrUri + " not found");
		}
		if(fileNameOrUri.contains(".ttl")){
			System.out.println("Opening Turtle file");
			model.read(in, null, "TTL");
		}else if(fileNameOrUri.contains(".rdf")){
			System.out.println("Opening RDFXML file");
			model.read(in, null);
		}else if(fileNameOrUri.contains(".nt")){
			System.out.println("Opening N-Triples file");
			model.read(in, null, "N-TRIPLE");
		}else{
			System.out.println("Content negotiation to get RDFXML from " + fileNameOrUri);
			model.read(fileNameOrUri);
		}
		System.out.println("loading "+ fileNameOrUri + " is done!!");
		return model;
	}



	private boolean isPlace(RDFNode uri){
		boolean result=false;
		if(uri.toString().contains("http://ns.aksw.org/scms/"))
			return false;
		String queryString="ask {<" +uri.toString() + "> a <http://dbpedia.org/ontology/Place>}";
		System.out.println("================================queryString="+ queryString);
		Query query = QueryFactory.create(queryString);
		//		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
		result = qexec.execAsk();
		System.out.println("ASK="+result);
		return result;
	}


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

	public Model nlpEnrichGeoTriples(){
		Model resultModel=ModelFactory.createDefaultModel();
		StmtIterator stItr = model.listStatements(null, litralProperty, (RDFNode) null);
		while (stItr.hasNext()) {
			Statement st = stItr.nextStatement();
			RDFNode object = st.getObject();
			RDFNode subject = st.getSubject();
			System.out.println("Subject: " + subject);
			System.out.println("Object:  " + object);
			if(object.isLiteral()){

				//				System.out.println("Object="+object);

				Model namedEntityModel = getNamedEntityModel(object.toString());
				//				System.out.println("Named Entity Model");
				//				namedEntityModel.write(System.out,"TTL");
				if(!namedEntityModel.isEmpty()){
					resultModel= resultModel.union(getPlaces(namedEntityModel, subject));	
				}				
				//				System.out.println("result Model");
				//				resultModel.write(System.out,"TTL");

			}
		}

		return resultModel;
	}

	public Model enrichModel(){
		return model.union(nlpEnrichGeoTriples());
	}
	//	public Model nlpEnrichGeoTriples(List<String> inputText){
	//		Model resultModel=ModelFactory.createDefaultModel();
	//		for(String it: inputText) {
	//			System.out.println("Text=" + it);
	//			Model namedEntityModel = getNamedEntityModel(it);
	//			if(!namedEntityModel.isEmpty()){
	//				resultModel = resultModel.union(getGeoObjects(namedEntityModel));	
	//			}
	//		}
	//		return resultModel;
	//	}

	@Test
	//	public void DBpediaAbstractTest(){
	//		List<String> abstracts=getDBpediaAbstaracts(10);
	//		nlpEnrichGeoTriples(abstracts).write(System.out,"TTL");
	//	}





	public static void main(String args[]) throws IOException {
		//		NLP app= new NLP(args[0], args[1]);

		NlpGeoEnricher app= new NlpGeoEnricher();
		Model m= app.loadModel(args[0]);
		LiteralPropertyRanker lpr=new LiteralPropertyRanker(m)	;
		Property p=lpr.getTopRankedLetralProperty();
		System.out.println("Top founded Literal Property: " + p); 
		app.setLitralProperty(p);
		FileWriter outFile = new FileWriter(args[1]);		

		Model enrichedModel=app.nlpEnrichGeoTriples();

		System.out.println("Enriched MODEL:");
		System.out.println("---------------");
		enrichedModel.write(System.out,"TTL");
		enrichedModel.write(outFile,"TURTLE");
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#process(com.hp.hpl.jena.rdf.model.Model, java.util.Map)
	 */
	public Model process(Model model, Map<String, String> parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getParameters()
	 */
	public List<String> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}

