/**
 * 
 */
package org.aksw.geolift.TMP;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.security.auth.login.Configuration;

import org.aksw.geolift.modules.nlp.DBpedia;
import org.aksw.geolift.modules.nlp.NamedEntity;
import org.aksw.geolift.modules.nlp.NerTool;
import org.junit.Test;
import org.omg.CORBA.portable.InputStream;

/**
 *
 * @author sherif
 */
public class nlpTest implements NerTool {

	Model model;
	Property litralProperty;

	/**
	 * @param model
	 * @param litralProperty
	 *@author sherif
	 */
	public nlpTest(Model model, Property litralProperty) {
		super();
		this.model = model;
		this.litralProperty = litralProperty;
	}

	public nlpTest(String fileNameOrUri, String litralPropartyUri) {
		super();
		this.model = loadModel(fileNameOrUri);
		this.litralProperty = ResourceFactory.createProperty(litralPropartyUri);
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
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}


	/**
	 * @param litralProperty the litralProperty to set
	 */
	public void setLitralProperty(Property litralProperty) {
		this.litralProperty = litralProperty;
	}


	public Model getNamedEntityModel( String inputText){
		String buffer = getNamedEntity("text","NER","turtle", inputText);
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
				String data = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8");
				data += "&" + URLEncoder.encode("task", "UTF-8") + "=" + URLEncoder.encode(task, "UTF-8");
				data += "&" + URLEncoder.encode("output", "UTF-8") + "=" + URLEncoder.encode(output, "UTF-8");
				data += "&" + URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(text, "UTF-8");
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


	public Model getGeoObjects(RDFNode subject){

		Model resultModel=ModelFactory.createDefaultModel();
		NodeIterator objectsIter = model.listObjects();

		while (objectsIter.hasNext()) {
			RDFNode object = objectsIter.nextNode();
			if(object.isResource()){
				if(isPlace(object)){
					Property property= ResourceFactory.createProperty("http://geoknow.org/ontology/relatedTo");
					resultModel.add( (Resource) subject , property, object);
					//TODO add more data ??
//					System.out.println(subject.toString() + property +object);
				}	
			}
		}
		return resultModel;
	}

	public Model loadModel(String fileNameOrUri){
		Model model=ModelFactory.createDefaultModel();
		java.io.InputStream in = FileManager.get().open( fileNameOrUri );
		if (in == null) {
			throw new IllegalArgumentException(
					"File: " + fileNameOrUri + " not found");
		}
		// read the RDF/XML file

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
		String queryString="ask{<" +uri.toString() + "> a <http://dbpedia.org/ontology/Place>}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(DBpedia.endPoint, query);
		result = qexec.execAsk();
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
		//		NodeIterator objectsIter = inputModel.listObjectsOfProperty(litralProperty);
		StmtIterator stItr = model.listStatements(null, litralProperty, (RDFNode) null);
		while (stItr.hasNext()) {
			com.hp.hpl.jena.rdf.model.Statement st = stItr.nextStatement();
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
					resultModel= resultModel.union(getGeoObjects(subject));	
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


		nlpTest app= new nlpTest(args[0], args[1]);	

		FileWriter outFile = new FileWriter(args[2]);		


		Model enrichedModel=app.nlpEnrichGeoTriples();
		enrichedModel.write(System.out,"TTL");
		System.out.println("Enriched MODEL:");
		System.out.println("---------------");
		enrichedModel.write(outFile,"TURTLE");
	}



















	/* (non-Javadoc)
	 * @see org.aksw.geolift.nlp.NerTool#getNEs(java.lang.String)
	 */
	public TreeSet<NamedEntity> getNEs(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.nlp.NerTool#getNEs(org.aksw.geolift.nlp.Configuration)
	 */
	public TreeSet<NamedEntity> getNEs(Configuration c) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.nlp.NerTool#getNEs(org.aksw.geolift.modules.nlp.Configuration)
	 */
	public TreeSet<NamedEntity> getNEs(
			org.aksw.geolift.modules.nlp.Configuration c) {
		// TODO Auto-generated method stub
		return null;
	}
}





