/**
 * 
 */
package org.aksw.geolift.workflow;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.scilab.forge.jlatexmath.NewCommandMacro;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.pfunction.library.container;
import com.ibm.icu.util.StringTokenizer;

/**
 * @author sherif
 *
 */
public class TSVConfigReader {

	static public Multimap<String, Map<String, String>> parameters = HashMultimap.create();
	public String separator = " ";
	
	public static void main(String args[]) throws IOException{
//		parameters = getParameters(args[0]);
		TSVConfigReader cr= new TSVConfigReader(args[0]);
		System.out.println(cr.parameters);
	}
	
	public void setSeparator(String s){
		separator = s;
	}
	
	public TSVConfigReader(String inputFile) throws IOException{
		if(inputFile.endsWith(".csv"))
			separator = ",";
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
	}
	
	
	
	public static Multimap<String, Map<String, String>> getParameters(){
		return parameters;

	}
}
















