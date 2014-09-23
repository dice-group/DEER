package org.aksw.geolift.modules.Dereferencing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.geolift.modules.GeoLiftModule;
import org.apache.log4j.Logger;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import org.aksw.geolift.json.ParameterType;
/**
 * @author mofeed
 * This class includes methods in order to load dataset in Model from file.
 * It enriches such model with additional information specified by list of
 * targeted predicates to be added. This is done by following each URI-typed 
 * objects in the model and query for such information in the dereferenced target
 */
public class DereferencingModule implements GeoLiftModule
{
	private static final Logger logger = Logger.getLogger(DereferencingModule.class.getName());
	//list of parameters passed to the module
	List<String> parametersList= new ArrayList<String>();
	static Map<RDFNode,Resource> objectsDerefModelAdded = new HashMap<RDFNode, Resource>();
	public static boolean useCache = false;
	public static boolean useBlankNodes = false;
	public static List<Property> inputProperties = new ArrayList<Property>();
	public static List<Property> outputProperties = new ArrayList<Property>();
	public static Property defaultOutputProperty = ResourceFactory.createProperty("http://geoknow.org/ontology/relatedTo");
	private static Model localModel = ModelFactory.createDefaultModel();

	/**
	 * @param parameters
	 * @author sherif
	 */
	private void readParameters(Map<String, String> parameters) {
		for(String key : parameters.keySet()){
			if(key.toLowerCase().equals("useblanknodes")){
				useBlankNodes = Boolean.parseBoolean(parameters.get(key));
			}if(key.toLowerCase().equals("usecache")){
				useCache = Boolean.parseBoolean(parameters.get(key));
			}else if(key.toLowerCase().startsWith("inputproperty")){
				inputProperties.add(ResourceFactory.createProperty(parameters.get(key)));
			}else if(key.toLowerCase().startsWith("outputproperty")){
				outputProperties.add(ResourceFactory.createProperty(parameters.get(key)));
			}else{
				logger.error("Invalid parameter key: " + key + ", allowed parameters for the dereferencing module are: " + getParameters());
				logger.error("Exit GeoLift");
				System.exit(1);
			}
		}
		if(inputProperties.size() == 0){
			logger.error("The inputProperty is a mandatory parameter(s) for the dereferencing module");
			logger.error("No inputProperty provided, Exit GeoLift");
			System.exit(1);
		}
	}

	/**
	 * @author sherif
	 */
	public List<String> getParameters() 
	{
		List<String> parameters = new ArrayList<String>();
		parameters.add("inputProperty<n>");
		parameters.add("outputProperty<n>");
		parameters.add("useBlankNodes");
		//		parameters.add("useCache");
		return parameters;
	}

	/**
	 * Self configuration
	 * Properties(target) - Properties(source)
	 * Find properties in target which are not in source   
	 * @param source
	 * @param target
	 * @return Map of (key, value) pairs of self configured parameters
	 * @author sherif
	 */
	public Map<String, String> selfConfig(Model source, Model target) {
		Map<String, String> parameters = new HashMap<String, String>();
		Set<Property> properties = getPropertyDifference(source, target);
		int propertyNr = 1;
		for(Property p : properties){
			System.out.println("inputProperty" + propertyNr + " :" + p);
			parameters.put("inputProperty" + propertyNr, p.toString());
			parameters.put("outputProperty" + propertyNr, p.toString());
			propertyNr++;
		}
		logger.info("Self configuration: " + parameters);
		if(parameters.size() == 0){
			return null;
		}
		return parameters;
	}

	/**
	 * Properties(target) - Properties(source) - Properties(ignoredProperties)
	 * @param source
	 * @param target
	 * @return properties which are in target and not in source 
	 * @author sherif
	 */
	private Set<Property> getPropertyDifference(Model source, Model target) {
		Set<Property> sProperties = new HashSet<Property>(); 
		StmtIterator sItr = source.listStatements();
		while (sItr.hasNext()) {
			sProperties.add(sItr.nextStatement().getPredicate());
		}
		Set<Property> tProperties = new HashSet<Property>(); 
		StmtIterator tItr = target.listStatements();
		while (tItr.hasNext()) {
			tProperties.add(tItr.nextStatement().getPredicate());
		}
		Set<Property> diffProperties = new HashSet<Property>();
		Sets.difference(tProperties, sProperties).copyInto(diffProperties);
		diffProperties = removeUnwantedProperties(diffProperties);
		//		logger.info("Self configured Properties:" + diffProperties);
		return diffProperties;
	}

	/**
	 * @return
	 * @author sherif
	 */
	private Set<Property> removeUnwantedProperties(Set<Property> diffProperties) {
		Set<Property> ignoreProperties = new HashSet<Property>();
		ignoreProperties.add(OWL.sameAs);
		for(Property p : ignoreProperties){
			diffProperties.remove(p);
		}
		return diffProperties;
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.modules.GeoLiftModule#getNecessaryParameters()
	 */
	@Override
	public List<String> getNecessaryParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("inputProperty<n>");
		return parameters;
	}

	/**
	 * @param model : the model to be enriched
	 * @param parameters : list of predicates used to enrich information in model returns model after enrichment
	 * @return  model after enrichment
	 * This method assigns the localmodel inside the class to the one given, collects targeted predicates into list, 
	 * and calls another method putAdditionalInfoUsingBlankNode to make the enrichment
	 */

	/* (non-Javadoc)
	 * This method starts processing to retrieve information from the interesting predicates
	 */
	public Model process(Model model, Map<String, String> parameters) {
		logger.info("--------------- Dereferencing Module ---------------");
		if(model!= null)
		{
			readParameters(parameters);
			localModel = localModel.union(model);
			setPrefixes();
			if(useBlankNodes){
				addAdditionalPropertiesUsingBlankNode(parameters);
			}else{
				addAdditionalProperties();
			}
		}
		return localModel;
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
	 * for each URI-typed object, through content negotiation an open connection is done retrieving its predicates/values. 
	 * An iteration is made over targeted predicates. For each predicate list of statements with the targeted predicate is 
	 * retrieved and extracting its value in order to be added to hashmap<predicate,Value>
	 */
	private static HashMap<Property, List<RDFNode>> getURIInfo(RDFNode p)
	{
		String uri = p.asResource().getURI(); 
		//to store each predicate and its value
		HashMap<Property, List<RDFNode>> resourceFocusedInfo = new HashMap<Property, List<RDFNode>>();

		//		//Deserialize the results if exists (For Demo purpose)
		//		if(useCache){
		//			try {
		//				HashMap<String, List<String>> ser = new HashMap<String, List<String>>();
		//				File file = new File("resourceFocusedInfo.ser");
		//				if(file.exists()){
		//					ObjectInputStream in;
		//					in = new ObjectInputStream(new FileInputStream(file));
		//					ser = (HashMap<String, List<String>>) in.readObject();
		//					// convert every object back from string
		//					for(String prop : ser.keySet()){
		//						List<String> l = ser.get(prop);
		//						List<RDFNode> nodes = new ArrayList<RDFNode>();
		//						for(String n : l){
		//							nodes.add(ResourceFactory.createResource(n));
		//						}
		//						resourceFocusedInfo.put(ResourceFactory.createProperty(prop), nodes);
		//					}
		//					return resourceFocusedInfo;
		//				}
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}

		//define local model to have the data of the uri and extract focused info through built sparql query
		List<RDFNode> values = new ArrayList<RDFNode>();
		try 
		{
			URLConnection conn = new URL(uri).openConnection();
			conn.setRequestProperty("Accept", "application/rdf+xml");
			conn.setRequestProperty("Accept-Language", "en");
			Model model = ModelFactory.createDefaultModel();
			InputStream in = conn.getInputStream();
			model.read(in, null);
			for(Property inputProperty: inputProperties)
			{	
				for(Statement st : model.listStatements(model.getResource(uri), inputProperty , (RDFNode) null).toList())
				{
					RDFNode value = st.getObject();
					if(value.isLiteral()){
						if(value.asLiteral().getLanguage().toLowerCase().equals("en") || value.asLiteral().getLanguage().toLowerCase().equals(""))
							values.add(value);
					}else{
						values.add(value);
					}

				}
				resourceFocusedInfo.put(inputProperty, values);
				values = new ArrayList<RDFNode>();//create new list for new predicate
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//		//serialize the output (for Demo purpose)
		//		try {
		//			HashMap<String, List<String>> ser = new HashMap<String, List<String>>();
		//			FileOutputStream fileOut = new FileOutputStream("resourceFocusedInfo.ser");
		//			ObjectOutputStream out = new ObjectOutputStream(fileOut);
		//			//convert to Serializabe Strings
		//			for(Property prop : resourceFocusedInfo.keySet()){
		//				List<String> l = new ArrayList<String>();
		//				for(RDFNode n : resourceFocusedInfo.get(prop)){
		//					l.add(n.toString());
		//				}
		//				ser.put(prop.toString(), l);
		//			}
		//			out.writeObject(ser);
		//		} catch (Exception e2) {
		//			e2.printStackTrace();
		//		}

		return resourceFocusedInfo;
	}

	/**
	 * @param predicates: list of targeted predicates to enrich the model
	 * It calls getTriplesWithObjectsAreURI() method retrieving list of triples in model having URI-typed objects. 
	 * For each object of them, it is checked if it is in dbpedia (can be extended later) then calls getURIInfo() 
	 * method to dereference the URI-typed object in hashmap and retrieve the targeted predicates values "if exist", 
	 * it iterates over the hashmap and add them to the resources in the model.
	 */
	/*private static void putAdditionalInfo(Map<String,String> predicates)
	{
		//list will contain triples having URIs as their Objects
		List<Triple> triplesURIsObjects=null;

		// retrieve list of all triples having URIs as Objects
		triplesURIsObjects= getTriplesWithObjectsAreURI();
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
					resource.addProperty(addedProperty, object);
				}
				else
				{
					//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
					object=localModel.createResource();//here 
					resourceInterestingInfoExtension= URIDereferencing.getURIInfo(triple.Object,predicates);
					for (String key : resourceInterestingInfoExtension.keySet())
					{
						//add the new properties to the new triple
						object.addProperty(ResourceFactory.createProperty(key), resourceInterestingInfoExtension.get(key));
					}
					objectsDerefModelAdded.put(triple.Object, object);
					//add the empty node as an object to the enriched subject
					Resource resource= localModel.getResource(triple.subject);
					resource.addProperty(addedProperty, object);
					resourceInterestingInfoExtension= null;
				}
				//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
				//Resource object=localModel.createResource();
				//iterate over the retrieved information extension required predicate:object
				for (String key : resourceInterestingInfoExtension.keySet())
					{
						//add the new properties to the new triple
						object.addProperty(ResourceFactory.createProperty(key), resourceInterestingInfoExtension.get(key));
					}
					//add the empty node as an object to the enriched subject
					Resource resource= localModel.getResource(triple.subject);
					resource.addProperty(addedProperty, object);
					resourceInterestingInfoExtension= null;
			}
		}
	}*/
	private static List<RDFNode> getURIObjects()
	{	
		List<RDFNode> objectsURIs = new ArrayList<RDFNode>();
		//		//Deserialize the results if exists (For Demo purpose)
		//		if(useCache){
		//			try {
		//				List<String> ser = new ArrayList<String>();
		//				File file = new File("URIObjects.ser");
		//				if(file.exists()){
		//					ObjectInputStream in;
		//					in = new ObjectInputStream(new FileInputStream(file));
		//					ser = (List<String>) in.readObject();
		//					// convert every object back from string
		//					for(String n : ser){
		//						objectsURIs.add(ResourceFactory.createResource(n));
		//					}
		//					return objectsURIs;
		//				}
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}

		//create a query to retrieve URIs objects
		String queryString =  "SELECT DISTINCT ?o WHERE {  ?s ?p ?o . FILTER (isURI(?o)) . FILTER (STRSTARTS(STR(?o), \"http://dbpedia.org/resource\"))}";// 
		Query query = QueryFactory.create(queryString);
		QueryExecution exec = QueryExecutionFactory.create(query, localModel);
		ResultSet rs = exec.execSelect();
		while(rs.hasNext())
		{
			QuerySolution sol    = rs.next();
			RDFNode 	  object = sol.get("?o");
			objectsURIs.add(object);   	
		}

		//		//serialize the output (for Demo purpose)
		//		try {
		//			FileOutputStream fileOut = new FileOutputStream("URIObjects.ser");
		//			ObjectOutputStream out = new ObjectOutputStream(fileOut);
		//			//convert to Serializabe Strings
		//			List<String> l = new ArrayList<String>();
		//			for(RDFNode n : objectsURIs){
		//				l.add(n.toString());
		//			}
		//			out.writeObject(l);
		//		} catch (Exception e2) {
		//			e2.printStackTrace();
		//		}

		return objectsURIs;
	}

	/**
	 * @param predicates: list of targeted predicates to enrich the model
	 * It calls getTriplesWithObjectsAreURI() method retrieving list of triples in model having URI-typed objects. 
	 * For each object of them, it is checked if it is in dbpedia (can be extended later) then calls getURIInfo() 
	 * method to dereference the URI-typed object in hashmap and retrieve the targeted predicates values "if exist", 
	 * it iterates over the hashmap and add them to the resources in the model.
	 */
	private static void addAdditionalPropertiesUsingBlankNode(Map<String,String> predicates)
	{
		//List to save all distinct URI objects in the data source
		List<RDFNode> urisObjects = new ArrayList<RDFNode>();
		//Map <predicate,value> save each interesting predicate of the URI object
		Map<Property, List<RDFNode>> resourceInterestingInfoExtension= new HashMap<Property, List<RDFNode>>();
		//Map<object,objectResourceData> to save each object with its related data resource and be retrieved whenever a URI object data needed to be added for extension 
		Map<RDFNode,Resource> objectFilledResource= new HashMap<RDFNode, Resource>();
		//Get list of unique URI objects in the data source as http://dbpedia.org/resource/XXXX 
		urisObjects= getURIObjects();
		//Get information for each single distinct objectURI according to interesting predicates
		logger.info("Number of unique URI object to find extension: "+urisObjects.size());
		if(urisObjects.size()>0) 
		{
			//The object resource that will have each URI object extended data
			Resource object=null;
			int count=1; 
			//For each URI object a resource is created, filled with information,add the object with its data resource into map 
			for (RDFNode uriObject : urisObjects)  
			{
				logger.info("Predicate "+ count++ +" of "+ urisObjects.size()+":"+ uriObject);
				// Create a resource with empty node
				object = localModel.createResource();
				//Retrieve all interesting <predicate,object> info. for such URI object
				resourceInterestingInfoExtension= DereferencingModule.getURIInfo(uriObject);
				//Add information to the resource 
				for (Property key : resourceInterestingInfoExtension.keySet())
				{
					//add the new properties to the new triple
					List<RDFNode> subjects = resourceInterestingInfoExtension.get(key);
					for(RDFNode subject: subjects){
						if(subject.isLiteral()){
							object.addProperty(key, subject.asLiteral().toString());
						}else{
							object.addProperty(key, subject);
						}
					}
				}
				//Add to list of object's resource that is filled with information
				objectFilledResource.put(uriObject, object);
			}
		}
		else //othrewise no URI objects to be extended
			return;
		//List of all statements have URI object it include for each entry <s,p,o>
		List<Statement> triplesURIsObjects=null;
		//Get list of all statement containing URI objects
		triplesURIsObjects = getTriplesWithURIObjects();
		logger.info("Starting model enriching");
		if(triplesURIsObjects.size()>0)
		{
			Resource object=null;
			//iterate over each triple to dereference each URI object and add its information to its resource subject
			for (Statement triple : triplesURIsObjects) 
			{
				//Check if this object's resource was added to the model before as a resource with empty node using addedProperty,
				//so attach it to this subject (if the subject has the same object with different predicates that will happen repeatedly)
				/*if(objectsDerefModelAdded.containsKey(triple.Object)) 
					{
						//Get the object's resource
						object=objectsDerefModelAdded.get(triple.Object);
						//Attach the object's resource to this subject
						Resource resource= localModel.getResource(triple.subject);
						resource.addProperty(addedProperty, object);
					}
					else //otherwise create a resource for it*/
				{
					//create new triple with empty node as its subject where this subject will be an object of the targeted resource to be extended
					if(!objectFilledResource.containsKey(triple.getSubject()))
					{
						object = objectFilledResource.get(triple.getObject());
						objectsDerefModelAdded.put(triple.getObject(), object);
						//Attach the object's resource to this subject
						Resource resource= localModel.getResource(triple.getSubject().getURI());
						resource.addProperty(defaultOutputProperty, object);
						/*Statement s = ResourceFactory.createStatement(resource, addedProperty, object);
							localModel.add(s);*/
						resourceInterestingInfoExtension= null;
					}
				}
			}
		}
	}

	private static void addAdditionalProperties()
	{
		//List to save all distinct URI objects in the data source
		List<RDFNode> urisObjects = null;
		//Map <predicate,value> save each interesting predicate of the URI object
		Map<Property, List<RDFNode>> resourceInterestingInfoExtension= new HashMap<Property, List<RDFNode>>();
		//Map<object,objectResourceData> to save each object with its related data resource and be retrieved whenever a URI object data needed to be added for extension 
		Map<RDFNode,Map<Property, List<RDFNode>>> objectWithInfoAttached= new HashMap<RDFNode, Map<Property, List<RDFNode>>>();
		//Get list of unique URI objects in the data source as http://dbpedia.org/resource/XXXX 
		urisObjects= getURIObjects();
		//Get information for each single distinct objectURI according to interesting predicates
		logger.info("Number of unique URI object to find extension: "+ urisObjects.size());
		if(urisObjects.size()>0) 
		{
			int count=1; 
			//For each unique URI object, its predicate-value pairs are retrieved then add them attached to their object in a map 
			for (RDFNode uriObject : urisObjects)  
			{
				//				logger.info("Enriching " + uriObject + "(" + count++ + "/" + urisObjects.size()+")");
				//Retrieve all interesting <predicate,object> info. for current URI object
				resourceInterestingInfoExtension = getURIInfo(uriObject);
				//Add retrieved predicate-value pair attached to the object in the map 
				objectWithInfoAttached.put(uriObject, resourceInterestingInfoExtension);//enriched list of objects
			}
		}
		else //othrewise no URI objects to be extended
			return;
		//List of all statements have URI-object
		List<Statement> triplesURIsObjects = null;
		//Get list of all statement containing URI-objects
		triplesURIsObjects = getTriplesWithURIObjects();
		logger.info("Starting model enriching");
		if(triplesURIsObjects.size() > 0)
		{
			//iterate over each triple to add each URI object information to its resource subject
			for (Statement triple : triplesURIsObjects) 
			{
				//put a hand over the required subject
				Resource mainResource= triple.getSubject();
				Resource enrichedResource = (Resource) triple.getObject();
				//for the subject's related object in the enriched list get the related predicate-value pairs
				Map<Property, List<RDFNode>> objectPredicateValuePairs = objectWithInfoAttached.get(triple.getObject());
				int i = 0;
				for (Property predicate : objectPredicateValuePairs.keySet()) {
					for(RDFNode value : objectPredicateValuePairs.get(predicate)){
						Property outputProperty = (i < outputProperties.size()) ? outputProperties.get(i) : defaultOutputProperty; 
						if(value.isLiteral()){
							//mainResource.addProperty(ResourceFactory.createProperty(predicate), objectPredicateValuePairs.get(predicate).asLiteral().toString());
							localModel.add(enrichedResource, outputProperty, value.asLiteral().toString());
							logger.info("Triple found: <" + enrichedResource + "> <" + outputProperty + "> \"" + value.toString() + "\"");
						}else{
							//mainResource.addProperty(ResourceFactory.createProperty(predicate), objectPredicateValuePairs.get(predicate));
							localModel.add(enrichedResource, outputProperty, value);
							logger.info("Triple found: <" + enrichedResource + "> <" + outputProperty + "> <" + value + ">");
						}
					}
					i++;
				}

			}
		}
	}


	/**
	 * @return list of triples having URI-typed objects
	 * it queries  the model for all its URI-typed objects as it check every object if it is a resource (URI).
	 *  if so then add them to the list.
	 */
	private static List<Statement> getTriplesWithURIObjects()
	{
		List<Statement> triplesWithURIObjects = new ArrayList<Statement>(); 

		//		//Deserialize the results if exists (For Demo purpose)
		//		if(useCache){
		//			try {
		//				List<String> ser = new ArrayList<String>();
		//				File file = new File("triplesWithURIObjects.ser");
		//				if(file.exists()){
		//					ObjectInputStream in;
		//					in = new ObjectInputStream(new FileInputStream(file));
		//					ser = (List<String>) in.readObject();
		//					// convert every object back from string
		//					for(String st : ser){
		//						triplesWithURIObjects.add(ResourceFactory.createStatement(
		//								ResourceFactory.createResource(st.split(" ")[0]), 
		//								ResourceFactory.createProperty(st.split(" ")[1]), 
		//								ResourceFactory.createResource(st.split(" ")[2])));
		//					}
		//					return triplesWithURIObjects;
		//				}
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}

		//		}

		//create a query to retrieve URIs objects
		String queryString =  "SELECT * WHERE { ?s ?p ?o . FILTER (isURI(?o)) . FILTER (STRSTARTS(STR(?o), \"http://dbpedia.org/resource\"))}";
		Query query = QueryFactory.create(queryString);
		QueryExecution exec = QueryExecutionFactory.create(query, localModel);
		ResultSet rs = exec.execSelect();
		while(rs.hasNext())
		{
			QuerySolution sol = rs.next();
			RDFNode s = sol.get("?s");
			RDFNode p = sol.get("?p");
			RDFNode o = sol.get("?o");
			Statement triple = ResourceFactory.createStatement(s.asResource(), ResourceFactory.createProperty(p.toString()), o);
			triplesWithURIObjects.add(triple);
		}	

		//		//serialize the output (for Demo purpose)
		//		try {
		//			FileOutputStream fileOut = new FileOutputStream("triplesWithURIObjects.ser");
		//			ObjectOutputStream out = new ObjectOutputStream(fileOut);
		//			List<String> l = new ArrayList<String>();
		//			for(Statement s : triplesWithURIObjects){
		//				l.add(s.getSubject() + " " + s.getPredicate() + " " + s.getObject());
		//			}
		//			out.writeObject(l);
		//		} catch (Exception e2) {
		//			e2.printStackTrace();
		//		}

		return triplesWithURIObjects;
	}



	private static void querySparqlService(String service,String query)
	{
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		ResultSet results = qe.execSelect();
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
			DereferencingModule URID = new DereferencingModule();
			// run the dereferencing process it requires model contains the dataset and list of targeted predicates to enrich the model
			Model resultedModel = URID.process(model, predicates);
			logger.info("Saving enriched model into file");
			org.aksw.geolift.io.Writer.writeModel(resultedModel, "TTL", datasetOutput);

		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Finished");
	}

    @Override
    public List<ParameterType> getParameterWithTypes() {
        List<ParameterType> parameters = new ArrayList<ParameterType>();
        
        String predicateDescription = "interesting predicate to enrich the model, e.g. 'predicate1'";
        String objectDescription = "The predicate object value, e.g. 'http://www.w3.org/2003/01/geo/wgs84_pos#lat\'";
        String outputPropertyDescription = "The enriched output property. By default this parameter is set to 'http://geoknow.org/ontology/relatedTo'";
        String useBlankNodeDescription = "Use blank node in output dataset. By default, this parameter is set to false";
        
        parameters.add(new ParameterType(ParameterType.STRING, "predicate", predicateDescription, true));
        parameters.add(new ParameterType(ParameterType.STRING, "predicate value", objectDescription, true));
        parameters.add(new ParameterType(ParameterType.STRING, "outputProperty", outputPropertyDescription, false));
        parameters.add(new ParameterType(ParameterType.BOOLEAN, "useBlankNodes", useBlankNodeDescription, false));
        return parameters;
    }

}
