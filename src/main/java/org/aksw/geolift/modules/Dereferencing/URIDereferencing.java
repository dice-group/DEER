package org.aksw.geolift.modules.Dereferencing;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author mofeed
 * This class includes methods in order to load dataset in Model from file.
 * It enriches such model with additional information specified by list of
 * targeted predicates to be added. This is done by following each URI-typed 
 * objects in the model and query for such information in the dereferenced target
 */
public class URIDereferencing implements GeoLiftModule
{
	/**
	 * @param model : the model to be enriched
	 * @param parameters : list of predicates used to enrich information in model returns model after enrichment
	 * @return  model after enrichment
	 * This method assigns the localmodel inside the class to the one given, collects targeted predicates into list, 
	 * and calls another method putAdditionalInfo to make the enrichment
	 */
	public Model process(Model model, Map<String, String> parameters) {
		// TODO Auto-generated method stub
		if(model!= null)
		{
			localModel = model;
			setPrefixes();
			List<String> predicates= new ArrayList<String>();
			Iterator it = parameters.entrySet().iterator();
		    while (it.hasNext()) 
		    {
		        Map.Entry pairs = (Map.Entry)it.next();
		        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
		        predicates.add(pairs.getValue().toString());
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		    
	   		URIDereferencing.putAdditionalInfo(predicates);
		}
		return localModel;
	}
	public List<String> getParameters() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @param uri : the URI to be dereferenced
	 * @param predicates : targeted predicates to be added to enrich the model
	 * @return 
	 * This method retrieves list of values for targeted predicates for a URI-typed object
	 * for each URI-typed object, through content negotioation an open connection is done retrieving its predicates/values. 
	 * An iteration is made over targeted predicates. For each predicate list of statements with the targeted predicate is 
	 * retrieved and extracting its value in order to be added to hashmap<predicate,Value>
	 */
	private static HashMap<String, String> getURIInfo(String uri,List<String> predicates)
	{
		//to store each predicate and its value
		HashMap<String, String> resourceFocusedInfo = new HashMap<String, String>();
		//define local model to have the data of the uri and extract focused info through built sparql query

	    String value = null;
	    try 
	    {
		   URLConnection conn = new URL(uri).openConnection();
		   conn.setRequestProperty("Accept", "application/rdf+xml");
		   Model model = ModelFactory.createDefaultModel();
		   InputStream in = conn.getInputStream();
		   model.read(in, null);
		   for(String predicate: predicates)
		   {
			   for(Statement st : model.listStatements(model.getResource(uri),ResourceFactory.createProperty(predicate) , (RDFNode)null).toList())
			   {
				   value=st.getObject().asLiteral().toString();
				   resourceFocusedInfo.put(predicate, value);
			   }
		   }
	    } catch (MalformedURLException e) 
	    {
	    	e.printStackTrace();
	    } catch (IOException e) 
	    {
	    	e.printStackTrace();
	    }catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	     return resourceFocusedInfo;
	}
	
	/**
	 * @param predicates: list of targeted predicates to enrich the model
	 * It calls getObjectsAreURI() method retrieving list of triples in model having URI-typed objects. 
	 * For each object of them, it is checked if it is in dbpedia (can be extended later) then calls getURIInfo() 
	 * method to dereference the URI-typed object in hashmap and retrieve the targeted predicates values "if exist", 
	 * it iterates over the hashmap and add them to the resources in the model.
	 */
	private static void putAdditionalInfo(List<String> predicates)
	{
		List<Triple> lst=null;
		// retrieve list of all triples having URIs as Objects
		lst= getObjectsAreURI();
		HashMap<String, String> resourceFocusedInfo= null;
	
		for (Triple triple : lst) 
		{
			if(triple.Object.contains("dbpedia")) // This is to check for the dereferencing to the dbpedia URI-typed objects as dbpedia is biggest dataset
			{
				// for a URI object get the required information about it (Leipzig uri is dereferenced as rdf/xml and its information are extracted)
				resourceFocusedInfo= URIDereferencing.getURIInfo(triple.Object,predicates);
				//create the empty node representing such information
				//Resource object= ResourceFactory.createResource();
				Resource object=localModel.createResource();
				//iterate over the retrieved the required predicate:object
				for (String key : resourceFocusedInfo.keySet())
				{
					//add the new properties to the empty node
					object.addProperty(ResourceFactory.createProperty(key), resourceFocusedInfo.get(key));
					//Resource resource= localModel.getResource(triple.subject);
					//resource.addProperty(ResourceFactory.createProperty(key), resourceFocusedInfo.get(key));
				}
				//add the empty node as an object to the enriched subject
				Resource resource= localModel.getResource(triple.subject);
				resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
			}
		}
	}
		
	//This function displays all the triples in the model
	private static List<String> displayModelTriples()
	{
		List<String> triples=new ArrayList<String>();
		
		String queryString =  "SELECT ?s ?p ?o WHERE  { ?s ?p ?o}";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution exec = QueryExecutionFactory.create(query, localModel);
    	ResultSet rs = exec.execSelect();
    	while(rs.hasNext()){
    		QuerySolution sol = rs.next();
    		System.out.print(("<"+sol.getResource("?s"))+"> ");
    		System.out.print(("<"+sol.getResource("?p"))+"> ");
    		if(sol.get("?o").isResource())
    			System.out.println(("<"+sol.get("?o"))+"> ");
    		else
    			System.out.println(("\""+sol.get("?o"))+"\"");

    	}
    return triples;
	}
	
	
	/**
	 * @return list of triples having URI-typed objects
	 * it queries  the model for all its URI-typed objects as it check every object if it is a resource (URI).
	 *  if so then add them to the list.
	 */
	private static List<Triple> getObjectsAreURI()
	{
		List<Triple> objectsURIs = new ArrayList<Triple>(); 
		String queryString =  "SELECT ?s ?p ?o WHERE  { ?s ?p ?o . FILTER isURI(?o)}";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution exec = QueryExecutionFactory.create(query, localModel);
    	ResultSet rs = exec.execSelect();
    	while(rs.hasNext())
    	{
    		QuerySolution sol = rs.next();
/*    		if(sol.get("?o").isResource())
    		{*/
    			String s= sol.get("?s").toString();
    			String p=sol.get("?p").toString();
    			String o=sol.get("?o").toString();
    			Triple triple= new Triple(s,p,o);
    			objectsURIs.add(triple);
/*    		}*/
    	
    	}	
		return objectsURIs;
	}

	private static void querySparqlService(String service,String query)
	{
        QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(System.out, results);
     // end method
	}
	
	private void setPrefixes()
	{
		String gn = "http://www.geonames.org/ontology#";
		localModel.setNsPrefix( "gn", gn );
	}

	
	//data members
	private static Model localModel=null;
	
}
