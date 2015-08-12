/**
 * 
 */
package org.aksw.deer.operators.fusion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ibm.icu.util.StringTokenizer;

/**
 * @author sherif
 *
 */
public class Examples {
	private static final Logger logger = Logger.getLogger(Examples.class.getName());
	
	public static final String SEPARATOR = "\t";
	public Multimap<String, Multimap<Boolean, String>> examples = ArrayListMultimap.create();
	
	Examples(String inputFile) throws IOException{
		logger.info("Reading examples from the input file: " + inputFile);
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));  
		String line = "";

		while ((line = reader.readLine()) != null) {  
			if(line.length() == 0)
				continue;
			StringTokenizer tokenizer = new StringTokenizer(line,SEPARATOR);  
			Boolean pos = tokenizer.nextToken().equals("+1") ? true : false;  
			String lang = tokenizer.nextToken();
			String triple = tokenizer.nextToken(); 
			String s = triple.substring(1, triple.indexOf(">"));
			Multimap<Boolean, String> ex = ArrayListMultimap.create();
			ex.put(pos, s);
			examples.put(lang, ex);
		}
		reader.close();
		logger.info("Done reading " + examples.size() + " languages.");
	}
	
	Set<OWLIndividual> getPositiveExamples(String languageTag){
		Set<OWLIndividual> pos = new HashSet<>();
		Collection<String> collection = null ;
		Collection<Multimap<Boolean, String>> langExamples = examples.get(languageTag);
		for (Multimap<Boolean, String> multimap : langExamples) {
			collection = multimap.get(true);
			for (String s : collection) {
				pos.add(new OWLNamedIndividualImpl(IRI.create(s)));
			}
		}
		logger.info("Found " + pos.size() + " positive example for " + languageTag);
		return pos;
	}
	
	Set<OWLIndividual> getNrgativeExamples(String languageTag){
		Set<OWLIndividual> neg = new HashSet<>();
		Collection<String> collection = null ;
		Collection<Multimap<Boolean, String>> langExamples = examples.get(languageTag);
		for (Multimap<Boolean, String> multimap : langExamples) {
			collection = multimap.get(false);
			for (String s : collection) {
				neg.add(new OWLNamedIndividualImpl(IRI.create(s)));
			}
		}
		logger.info("Found " + neg.size() + " negative example for " + languageTag);
		return neg;
	}
	
	public static void main(String args[]){
		
		
	}

}
