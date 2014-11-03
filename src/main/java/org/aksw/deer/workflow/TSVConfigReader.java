/**
 * 
 */
package org.aksw.deer.workflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.util.StringTokenizer;

/**
 * @author sherif
 *
 */
public class TSVConfigReader {
	private static final Logger logger = Logger.getLogger(TSVConfigReader.class.getName());

	public static  Multimap<String, Map<String, String>> parameters = HashMultimap.create();
	public static String separator = " ";
	
	
	public TSVConfigReader(String inputFile) throws IOException{
		getParameters(inputFile);
	}
	
	
	/**
	 * read parameters from TSV or CSV input File
	 * @param inputFile
	 * @return Multimap<String, Map<String, String>> map of parameters
	 * @throws IOException
	 * @author sherif
	 */
	public static Multimap<String, Map<String, String>> getParameters(String inputFile) throws IOException{
		if(inputFile.endsWith(".csv")){
			separator = ",";
		}else if(inputFile.endsWith(".tsv")){
			separator = " ";
		}else {
			logger.error("Unrecognized configuration file only csv and tsv are acceptable. Exit with error. ");
			System.exit(1);
		}
			
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));  
		String line = "";
		String moduleId ="1";
		String moduleName = "";
		String parameterKey = "";
		String parameterValue = "";

		while ((line = reader.readLine()) != null) {  
			if(line.length() == 0)
				continue;
			Map<String, String> param = new HashMap<String, String>();
			StringTokenizer tokenizer = new StringTokenizer(line,separator);  
			moduleId = tokenizer.nextToken();  
			moduleName = tokenizer.nextToken();
			parameterKey = tokenizer.nextToken();
			parameterValue = tokenizer.nextToken();
			param.put(parameterKey, parameterValue);
			parameters.put(moduleId + "_" + moduleName, param);
		}
		return parameters;
	}
	
	public static void main(String args[]) throws IOException{
		System.out.println(TSVConfigReader.getParameters(args[0]));
	}
}
















