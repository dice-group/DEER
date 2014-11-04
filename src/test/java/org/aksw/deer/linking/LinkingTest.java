package org.aksw.deer.linking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.modules.Dereferencing.DereferencingModule;
import org.aksw.deer.modules.linking.LinkingModule;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


public class LinkingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> parameters=new HashMap<String, String>();
		System.out.println("Start processing......");
		parameters.put("datasetFilePath",args[0]);// The path to the dataset file to be loaded
		parameters.put("specFilePath",args[1]);//The path to the spec.xml file contains the linking specifications
		parameters.put("linksFilePath",args[2]);// The path to the file contains the resulted links
		parameters.put("linksPart",args[3]);//The position of the Original URI to be enriched in the links generated (right side or left side), so the otherside is the link partner to be added to it
		
		Model model=org.aksw.deer.io.Reader.readModel(parameters.get("datasetFilePath"));
		LinkingModule l= new LinkingModule();
		l.process(model, parameters);
		try {
			org.aksw.deer.io.Writer.writeModel(model, "TTL", "src/main/resources/linking/datasetUpdated.nt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		DereferencingModule d= new DereferencingModule();
		Map<String, String> parameters2= new HashMap<String, String>();
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#lat", "http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#long", "http://www.w3.org/2003/01/geo/wgs84_pos#long");
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#geometry", "http://www.w3.org/2003/01/geo/wgs84_pos#geometry");
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#lat_long", "http://www.w3.org/2003/01/geo/wgs84_pos#lat_long");
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#line", "http://www.w3.org/2003/01/geo/wgs84_pos#line");
		parameters2.put("http://www.w3.org/2003/01/geo/wgs84_pos#polygon", "http://www.w3.org/2003/01/geo/wgs84_pos#polygon");
			
		model=d.process(model, parameters2);
		try {
			org.aksw.deer.io.Writer.writeModel(model, "TTL", "src/main/resources/linking/datasetLinkingDereferenced.nt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		System.out.println("Finished");
	}
	public static void getDataset(String filepath)
	{
		Model model= ModelFactory.createDefaultModel();
		List<String> uris= getURIs(filepath);
		for (String uri : uris) 
		{
			try
        	{
            	String sparqlQuery="select distinct * where { "+uri+" ?p  ?o .}";
    	        Query query = QueryFactory.create(sparqlQuery);
    			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://linkedgeodata.org/sparql/", query);
    			com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
    			com.hp.hpl.jena.query.QuerySolution binding=null;
    		    while (results.hasNext()) 
    		    {
    		    	binding = results.next();
    		    	String propertyString=binding.getResource("?p").toString();
    		    	String value= binding.get("?o").toString() ;
    		    	System.out.println(value);
    		    	//create property
    		    	com.hp.hpl.jena.rdf.model.Property property = ResourceFactory.createProperty(propertyString);
    	    		Resource resource = model.createResource(uri);
    	    		model.add(resource, property, value);
    		    }
    		    qexec.close() ;
        	}
        	catch (Exception e)
    	 	  {
    		        System.out.println(e.getMessage());
    		       
    	 	  }
		}
		try {
			org.aksw.deer.io.Writer.writeModel(model, "TTL", "/home/mofeed/Projects/GeoLift/src/main/resources/dataset.nt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static List<String> getURIs(String file)
    {
    	List<String> configurationInfo = new ArrayList<String>(); 
    	BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) 
            {
            	String statement[]= line.split("\\s+");
            	configurationInfo.add(statement[2]);
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

}
