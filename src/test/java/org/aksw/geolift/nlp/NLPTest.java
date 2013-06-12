package org.aksw.geolift.nlp;
import java.io.FileWriter;
import java.io.IOException;

import org.aksw.geolift.modules.nlp.LiteralPropertyRanker;
import org.aksw.geolift.modules.nlp.NlpGeoEnricher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Unit test for simple App.
 */
public class NLPTest {
    
    public static void main(String args[]) throws IOException {
		//		NLP app= new NLP(args[0], args[1]);

		NlpGeoEnricher nlpEnricher = new NlpGeoEnricher();
		Model m = nlpEnricher.loadModel(args[0]);
		LiteralPropertyRanker lpr = new LiteralPropertyRanker(m)	;
		Property topRankedLetralProperty = lpr.getTopRankedLetralProperty();
		System.out.println("Top founded Literal Property: " + topRankedLetralProperty); 
		nlpEnricher.setLitralProperty(topRankedLetralProperty);
		FileWriter outFile = new FileWriter(args[1]);		

		Model enrichedModel = nlpEnricher.nlpEnrichGeoTriples();

		System.out.println("Enriched MODEL:");
		System.out.println("---------------");
		enrichedModel.write(System.out,"TTL");
		enrichedModel.write(outFile,"TURTLE");
	}

}