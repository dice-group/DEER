package org.aksw.deer.dereferencing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.deer.modules.dereferencing.DereferencingModule;

import org.apache.jena.rdf.model.Model;


/**
 * 
 *
 */
public class DereferencingTest
{
    public static void main( String[] args ) throws MalformedURLException, IOException
    {	
    	execDereferencing(args);
    }
   //This function is created in order to test the URIDereference class functionalities 
    public static void execDereferencing(String[] args)
    {
    	long startTime = System.currentTimeMillis();// to measure execution time in milliseconds
    	/**
    	 * reading the configurations file path from user, these configurations include:
    	 * dataset file : includes dataset to be read in Model that dereferencing work on its statements
    	 * predicates list: include list of predicates in interest to be added and enrich the dataset with
    	 */
    	
    	List<String> configurations= getConfigured(args[0]);
    	//load model with required dataset
    	Model model=org.aksw.deer.io.Reader.readModel(configurations.get(0));//model is loaded with dataset from specified file
    	List<String> predicates=configurations.subList(1, configurations.size());//load targeted predicates to be added to enrich information in dataset
    	//Collect list of targeted predicates into Map
    	Map<String, String> parameters= new HashMap<String, String>();
    	for (String predicate : predicates) 
    	{
    		parameters.put(predicate, predicate);
		}
    	
    	DereferencingModule u = new DereferencingModule();
    	Model resultedModel = u.process(model, parameters);// run the dereferencing process it requires model contains the dataset and list of targeted predicates to enrich the model
		long stopTime = System.currentTimeMillis();
		try { 
			org.aksw.deer.io.Writer.writeModel(resultedModel, "TTL", "src/main/resources/dereferencing/DereferencingEnriched.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Elapsed time = "+ elapsedTime/1000.0);
		System.out.println("Finished");
    }
    public static List<String> getConfigured(String file)
    {
    	List<String> configurationInfo = new ArrayList<String>(); 
    	BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(file));
            String line = br.readLine();

            while (line != null) 
            {
            	configurationInfo.add(line);
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
