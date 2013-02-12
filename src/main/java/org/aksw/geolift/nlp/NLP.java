package org.aksw.geolift.nlp;


import java.util.ArrayList;
import java.util.List;

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
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.omg.CORBA.portable.InputStream;

/**
 *
 * @author sherif
 */
public class NLP implements NerTool {

	public Model getNamedEntityModel( String inputText){
		String buffer = getNamedEntity("text","NER","turtle", inputText);
		ByteArrayInputStream stream=null;
		try {
			stream = new ByteArrayInputStream(buffer.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Model NamedEntitymodel = ModelFactory.createDefaultModel();
		System.out.println(buffer);
		if(buffer.contains("<!--")){
			return NamedEntitymodel;
		}
		NamedEntitymodel.read(stream, "", "TTL");
		return NamedEntitymodel;
	}

	private String getNamedEntity(String type, String task, String output, String text){
		String buffer = "", line; 
		boolean error = true;
		while (error) {
			try {

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


	public Model getGeoObjects(Model inputModel){
		Model resultModel=ModelFactory.createDefaultModel();
		NodeIterator objectsIter = inputModel.listObjects();
		while (objectsIter.hasNext()) {
			RDFNode object = objectsIter.nextNode();
			if(object.isResource()){
				if(isPlace(object)){
					resultModel.add((Resource) object,RDF.type, "<http://dbpedia.org/ontology/Place>");
					//TODO add more data ??
//					System.out.println("------------------------>"+object.toString());
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
		model.read(in, null, "TTL");
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

	public Model nlpEnrichGeoTriples(Model inputModel, Property litralProperty){
		Model resultModel=ModelFactory.createDefaultModel();
		NodeIterator objectsIter = inputModel.listObjectsOfProperty(litralProperty);
		
		while (objectsIter.hasNext()) {
			RDFNode object = objectsIter.nextNode();
			if(object.isLiteral()){
				
				System.out.println("Object="+object);
				
				Model namedEntityModel = getNamedEntityModel(object.toString());
//				System.out.println("Named Entity Model");
//				namedEntityModel.write(System.out,"TTL");
				if(!namedEntityModel.isEmpty()){
					resultModel= resultModel.union(getGeoObjects(namedEntityModel));	
				}
				
//				System.out.println("result Model");
//				resultModel.write(System.out,"TTL");
	
			}
		}
		
		return resultModel;
	}

	public static void main(String args[]) {
		NLP app= new NLP();
		
		Model m=app.loadModel(args[0]);
		Property biographyProperty=ResourceFactory.createProperty(args[1]);
		app.nlpEnrichGeoTriples(m, biographyProperty).write(System.out,"TTL");
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





















}
