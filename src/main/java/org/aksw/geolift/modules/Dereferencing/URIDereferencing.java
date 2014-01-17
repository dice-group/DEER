package org.aksw.geolift.modules.Dereferencing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

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
	//list of parameters passed to the module
	List<String> parametersList= new ArrayList<String>();
	//static Map<String,Map<String, String>> objectsDerefInfo= new HashMap<String, Map<String,String>>();
	static Map<String,Resource> objectsDerefModelAdded= new HashMap<String, Resource>();
	private static final Logger logger = Logger.getLogger(URIDereferencing.class.getName());
	/* (non-Javadoc)
	 * This method starts processing to retrieve information from the interesting predicates
	 */
	public Model process(Model model, Map<String, String> parameters) {
		if(model!= null){
			//Assign the local model of the class to the model read by the reader module
			localModel = model;
			//Add the gn:Feature predicate and it can be used to add other predicates
			setPrefixes();
		    //Extend the model with the required information of interesting predicates
	   		URIDereferencing.putAdditionalInfo2(parameters);
		}
		return localModel;
	}
	public List<String> getParameters() 
	{
		// TODO Auto-generated method stub
		if(parametersList.size() > 0)
		{
			parametersList.add("input");
			return parametersList;
		}
		else
			return null;
	}
	
	/*private static Map<String, String> getURIInfo2(String uri,Map<String,String> predicates)
	{
		//to store each predicate and its value
		Map<String, String> resourceFocusedInfo = new HashMap<String, String>();
		//define local model to have the data of the uri and extract focused info through built sparql query

	    String value = null;
	    try 
	    {
		   URLConnection conn = new URL(uri).openConnection();
		   conn.setRequestProperty("Accept", "application/rdf+xml");
		   Model model = ModelFactory.createDefaultModel();
		   InputStream in = conn.getInputStream();
		   model.read(in, null);
		   for(String predicate: predicates.values()) 
		   {  
			   QueryExecution qe = QueryExecutionFactory.create(query, model);
			   ResultSet results = qe.execSelect();
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
*/
	
	/**
	 * @param uri : the URI to be dereferenced
	 * @param predicates : targeted predicates to be added to enrich the model
	 * @return 
	 * This method retrieves list of values for targeted predicates for a URI-typed object
	 * for each URI-typed object, through content negotioation an open connection is done retrieving its predicates/values. 
	 * An iteration is made over targeted predicates. For each predicate list of statements with the targeted predicate is 
	 * retrieved and extracting its value in order to be added to hashmap<predicate,Value>
	 */
	private static HashMap<String, RDFNode> getURIInfo(String uri,Map<String,String> predicates)
	{
		//to store each predicate and its value
		HashMap<String, RDFNode> resourceFocusedInfo = new HashMap<String, RDFNode>();
		//define local model to have the data of the uri and extract focused info through built sparql query

	    RDFNode value = null;
	    try 
	    {
		   URLConnection conn = new URL(uri).openConnection();
		   conn.setRequestProperty("Accept", "application/rdf+xml");
		   Model model = ModelFactory.createDefaultModel();
		   InputStream in = conn.getInputStream();
		   model.read(in, null);
		   for(String predicate: predicates.values())
		   {
			   for(Statement st : model.listStatements(model.getResource(uri),ResourceFactory.createProperty(predicate) , (RDFNode)null).toList())
			   {
				   value = st.getObject();
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
	private static void putAdditionalInfo(Map<String,String> predicates)
	{
		//list will contain triples having URIs as their Objects
		List<Triple> triplesURIsObjects=null;
		
		// retrieve list of all triples having URIs as Objects
		triplesURIsObjects= getObjectsAreURI();
		if(triplesURIsObjects.size()>0)
		{
			Map<String, RDFNode> resourceInterestingInfoExtension= new HashMap<String, RDFNode>();
			Resource object=null;
			//iterate over each triple to dereference each URI object and add its information to its resource subject
			for (Triple triple : triplesURIsObjects) 
			{
					// for a URI object get the required information about it (e.g. Leipzig uri is dereferenced as rdf/xml and its information are extracted)
					if(objectsDerefModelAdded.containsKey(triple.Object))
					{
						//resourceInterestingInfoExtension=objectsDerefInfo.get(triple.Object);
						object=objectsDerefModelAdded.get(triple.Object);
						Resource resource= localModel.getResource(triple.subject);
						resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
					}
					else
					{
						//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
						object=localModel.createResource();
						resourceInterestingInfoExtension= URIDereferencing.getURIInfo(triple.Object,predicates);
						for (String key : resourceInterestingInfoExtension.keySet())
						{
							//add the new properties to the new triple
							object.addProperty(ResourceFactory.createProperty(key), resourceInterestingInfoExtension.get(key));
						}
						objectsDerefModelAdded.put(triple.Object, object);
						//add the empty node as an object to the enriched subject
						Resource resource= localModel.getResource(triple.subject);
						resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
						resourceInterestingInfoExtension= null;
					}
					//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
					//Resource object=localModel.createResource();
					//iterate over the retrieved information extension required predicate:object
					/*for (String key : resourceInterestingInfoExtension.keySet())
					{
						//add the new properties to the new triple
						object.addProperty(ResourceFactory.createProperty(key), resourceInterestingInfoExtension.get(key));
					}
					//add the empty node as an object to the enriched subject
					Resource resource= localModel.getResource(triple.subject);
					resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
					resourceInterestingInfoExtension= null;*/
			}
		}
	}
	private static List<String> getObjectsAreURI2()
	{		
		List<String> objectsURIs = new ArrayList<String>(); 
		//create a query to retrieve URIs objects
		String queryString =  "SELECT DISTINCT ?o WHERE {  ?s ?p ?o . FILTER (isURI(?o)) . FILTER (STRSTARTS(STR(?o), \"http://dbpedia.org/resource\"))}";
    	Query query = QueryFactory.create(queryString);
    	/*try {
			org.aksw.geolift.io.Writer.writeModel(localModel, "TTL", "/home/mofeed/Desktop/dummy.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    	QueryExecution exec = QueryExecutionFactory.create(query, localModel);
    	ResultSet rs = exec.execSelect();
    	while(rs.hasNext())
    	{
    		QuerySolution sol = rs.next();
   			String object=sol.get("?o").toString();
   			objectsURIs.add(object);   	
    	}
    	return objectsURIs;
	}
	
	/**
	 * @param predicates represents list of predicates interesting to extend the model
	 */
	private static void putAdditionalInfo2(Map<String,String> predicates)
	{
		//List to save all distinct URI objects in the data source
		List<String> urisObjects=null;
		//Map <predicate,value> save each interesting predicate of the URI object
		Map<String, RDFNode> resourceInterestingInfoExtension= new HashMap<String, RDFNode>();
		//Map<object,objectResourceData> to save each object with its related data resource and be retrieved whenever a URI object data needed to be added for extension 
		Map<String,Resource> objectFilledResource= new HashMap<String, Resource>();
		//Get list of unique URI objects in the data source as http://dbpedia.org/resource/XXXX 
		urisObjects= getObjectsAreURI2();
		//Get information for each single distinct objectURI according to interesting predicates
		logger.info("Number of unique URI object to find extension: "+urisObjects.size());
		if(urisObjects.size()>0) 
		{
			//The object resource that will have each URI object extended data
			Resource object=null;
			int count=1; 
			//For each URI object a resource is created, filled with information,add the object with its data resource into map 
			for (String uriObject : urisObjects)  
			{
				logger.info("Predicate "+ count++ +" of "+ urisObjects.size()+":"+ uriObject);
				// Create a resource with empty node
				object=localModel.createResource();
				//Retrieve all interesting <predicate,object> info. for such URI object
				resourceInterestingInfoExtension= URIDereferencing.getURIInfo(uriObject,predicates);
				//Add information to the resource 
				for (String key : resourceInterestingInfoExtension.keySet())
				{
					//add the new properties to the new triple
					RDFNode subject = resourceInterestingInfoExtension.get(key);
					if(subject.isLiteral()){
						object.addProperty(ResourceFactory.createProperty(key), resourceInterestingInfoExtension.get(key).asLiteral().toString());
					}else{
						object.addProperty(ResourceFactory.createProperty(key), subject);
					}
				}
				//Add to list of object's resource that is filled with information
				objectFilledResource.put(uriObject, object);
			}
		}
		else //othrewise no URI objects to be extended
			return;
		//List of all statements have URI object it include for each entry <s,p,o>
		List<Triple> triplesURIsObjects=null;
		//Get list of all statement containing URI objects
		triplesURIsObjects = getObjectsAreURI();
		logger.info("Starting model enriching");
		if(triplesURIsObjects.size()>0)
		{
			//The resource of URI object will be to the subject of each statement with predicate gn:Feature
			Resource object=null;
			//iterate over each triple to dereference each URI object and add its information to its resource subject
			for (Triple triple : triplesURIsObjects) 
			{
				//Check if this object's resource was added to the model before as a resource with empty node using gn:Feature,
				//so attach it to this subject (if the subject has the same object with different predicates that will happen repeatedly)
					/*if(objectsDerefModelAdded.containsKey(triple.Object)) 
					{
						//Get the object's resource
						object=objectsDerefModelAdded.get(triple.Object);
						//Attach the object's resource to this subject
						Resource resource= localModel.getResource(triple.subject);
						resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
					}
					else //otherwise create a resource for it*/
					{
						//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
						if(!objectFilledResource.containsKey(triple.subject))
						{
							object=objectFilledResource.get(triple.Object);
							objectsDerefModelAdded.put(triple.Object, object);
							//Attach the object's resource to this subject
							Resource resource= localModel.getResource(triple.subject);
							resource.addProperty(ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
							/*Statement s = ResourceFactory.createStatement(resource, ResourceFactory.createProperty("http://www.geonames.org/ontology#Feature"), object);
							localModel.add(s);*/
							resourceInterestingInfoExtension= null;
						}
					}
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
    			logger.info(("<"+sol.get("?o"))+"> ");
    		else
    			logger.info(("\""+sol.get("?o"))+"\"");

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
		//create a query to retrieve URIs objects
		String queryString =  "SELECT * WHERE { ?s ?p ?o . FILTER (isURI(?o)) . FILTER (STRSTARTS(STR(?o), \"http://dbpedia.org/resource\"))}";
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
	//This method get the Dereferencing parameters from the given file by the user
	private static Map<String,String> getConfigurations(String file)
	    {	
		Map<String,String> configurationInfo = new HashMap<String, String>();
	    	BufferedReader br=null;
			try {
				br = new BufferedReader(new FileReader(file));
	            StringBuilder sb = new StringBuilder();
	            String line = br.readLine();

	            while (line != null) 
	            {
	            	String[] predicateLine= line.split(",");
	            	configurationInfo.put(predicateLine[0],predicateLine[1]);
	                line = br.readLine();
	            }
	        }
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} finally {
	            try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			return configurationInfo;
	    }
	private static Map<String,String> list2map (List<String> Predicates)
	{
		Map<String,String> predicatesMap= new HashMap<String, String>();
		int i=1;
		for (String predicateLine : Predicates) 
    	{
    		predicatesMap.put("predicate"+i++, predicateLine);
		}
		return predicatesMap;
	}
	public static void main( String[] args ) 
    {	
		String datasetSource="";
		String datasetOutput="";
		Map<String,String> predicates=null;
		logger.info("Start Dereferencing module.");
		logger.info("Reading parameters......");
		if(args.length > 0)
		{
			for(int i=0;i<args.length;i+=2)
			{
				if(args[i].equals("-d") || args[i].equals("--data"))
					datasetSource = args[i+1];
				if(args[i].equals("-o") || args[i].equals("--output"))
					datasetOutput = args[i+1];
				if(args[i].equals("-p") || args[i].equals("--predicate"))
					predicates= getConfigurations(args[i+1]);
			}
		}
		else
			logger.error("Missed parameter");	
    	try 
    	{ 
			logger.info("Loading resource information into model");
			//First parameter: model is loaded with dataset from specified file/resource
    		Model model=org.aksw.geolift.io.Reader.readModel(datasetSource);
	    	//Create Dereferencing object to start the process
	    	URIDereferencing URID = new URIDereferencing();
	    	// run the dereferencing process it requires model contains the dataset and list of targeted predicates to enrich the model
	    	Model resultedModel = URID.process(model, predicates);
	    	logger.info("Saving enriched model into file");
	    	org.aksw.geolift.io.Writer.writeModel(resultedModel, "TTL", datasetOutput);
	    	
	    } catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Finished");
    }
	//data members
	private static Model localModel=null;
	
}
