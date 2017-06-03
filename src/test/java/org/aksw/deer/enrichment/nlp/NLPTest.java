package org.aksw.deer.enrichment.nlp;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

/**
 * Unit test for simple App.
 */
public class NLPTest {
    
    public static void main(String args[]) throws IOException {
		NLPEnrichmentFunction geoEnricher= new NLPEnrichmentFunction();

		Map<String, String> parameters = new HashMap<String, String>();
		
		parameters.put("useFoxLight", "true");
		parameters.put("askEndPoint", "false");
		parameters.put("inputFile",   args[0]);
		parameters.put("outputFile",  args[1]);
		
		Model enrichedModel = geoEnricher.apply(null, parameters);
		
		System.out.println("Enriched MODEL:");
		System.out.println("---------------");
		enrichedModel.write(System.out,"TTL");
	}

}