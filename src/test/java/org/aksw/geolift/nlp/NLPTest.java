package org.aksw.geolift.nlp;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aksw.geolift.modules.nlp.LiteralPropertyRanker;
import org.aksw.geolift.modules.nlp.NlpModule;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Unit test for simple App.
 */
public class NLPTest {
    
    public static void main(String args[]) throws IOException {
		NlpModule geoEnricher= new NlpModule();

		Map<String, String> parameters = new HashMap<String, String>();
		
		parameters.put("useFoxLight", "true");
		parameters.put("askEndPoint", "false");
		parameters.put("inputFile",   args[0]);
		parameters.put("outputFile",  args[1]);
		
		Model enrichedModel = geoEnricher.process(null, parameters);
		
		System.out.println("Enriched MODEL:");
		System.out.println("---------------");
		enrichedModel.write(System.out,"TTL");
	}

}