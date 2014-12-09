/**
 * 
 */
package org.aksw.deer.workflow;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.aksw.deer.io.Reader;
import org.aksw.deer.workflow.rdfspecs.RDFConfigExecuter;
import org.aksw.deer.workflow.rdfspecs.RDFConfigReader;
import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author sherif
 *
 */
public class Deer {
	private static final Logger logger = Logger.getLogger(Deer.class.getName());
	private static final String helpMessage = 
			"To run DEER from command-line, provide the RDf configuration file as " +
					"the only one parameter for the DEER jar file. \n" +
					"Example: deer.jar config.ttl \n" +
					"For details about the configuration file see DEER manual at " +
					"https://github.com/GeoKnow/DEER/blob/master/DEER_manual/deer_manual.pdf ";


	/**
	 * 
	 *@author sherif
	 */
	public Deer() {
	}


	/**
	 * run GeoLift through command line
	 * @param args
	 * @throws IOException
	 * @author sherif
	 */
	public static void runDeer(String args[]) throws IOException{
		long startTime = System.currentTimeMillis();
		String inputFile	= "";
		String configFile	= "";
		String outputFile	= "";

		for(int i=0; i<args.length; i+=2){
			if(args[i].equals("-i") || args[i].toLowerCase().equals("--input")){
				inputFile = args[i+1];
			}
			if(args[i].equals("-c") || args[i].toLowerCase().equals("--config")){
				configFile = args[i+1];
			}
			if(args[i].equals("-o") || args[i].toLowerCase().equals("--output")){
				outputFile = args[i+1];
			}
		} 

		Model startModel =  Reader.readModel(inputFile);
		Multimap<String, Map<String, String>> parameters = HashMultimap.create();
		if(configFile.toLowerCase().endsWith(".csv") || configFile.toLowerCase().endsWith(".tsv")){
			parameters = TSVConfigReader.getParameters(configFile);
		} else { // read RDF config file
			parameters = RDFConfigReader.getParameters(configFile);
		}
		WorkflowHandler wfh = new WorkflowHandler(startModel, parameters);

		if(!outputFile.equals("")){
			wfh.getEnrichedModel().write(new FileWriter(outputFile), "TTL");
		}else{
			wfh.getEnrichedModel().write(System.out, "TTL");
		}
		Long totalTime = System.currentTimeMillis() - startTime;
		logger.info("***** Done in " + totalTime + "ms *****");
	}

	public static void run(String args[]) throws IOException {            
		if(args.length == 0 || args[0].equals("-?") || args[0].toLowerCase().equals("--help")) {
			//show help message
			logger.info(Deer.helpMessage);
			System.exit(0);
		}
		if(args[0].equals("-l") || args[0].toLowerCase().equals("--list")) {
			org.aksw.deer.json.JSONConfigWriter.write();
			System.exit(0);
		}
		//program didn't terminate until here so run TSV config mode
		long startTime = System.currentTimeMillis();
		RDFConfigExecuter.main(args);
		Long totalTime = System.currentTimeMillis() - startTime;
		logger.info("Running DEER Done in " + totalTime + "ms");
	}

	/**
	 * @param args
	 * @author sherif
	 */
	public static void main(String args[]) throws IOException{
		run(args);
	}
}
