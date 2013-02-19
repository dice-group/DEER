/**
 * 
 */
package org.aksw.geolift.io;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author sherif
 *
 */
public class Reader {

	public static Model readModel(String fileNameOrUri){
		Model model=ModelFactory.createDefaultModel();
		java.io.InputStream in = FileManager.get().open( fileNameOrUri );
		if (in == null) {
			throw new IllegalArgumentException(
					"File: " + fileNameOrUri + " not found");
		}
		if(fileNameOrUri.contains(".ttl")){
			System.out.println("Opening Turtle file");
			model.read(in, null, "TTL");
		}else if(fileNameOrUri.contains(".rdf")){
			System.out.println("Opening RDFXML file");
			model.read(in, null);
		}else if(fileNameOrUri.contains(".nt")){
			System.out.println("Opening N-Triples file");
			model.read(in, null, "N-TRIPLE");
		}else{
			System.out.println("Content negotiation to get RDFXML from " + fileNameOrUri);
			model.read(fileNameOrUri);
		}
		System.out.println("loading "+ fileNameOrUri + " is done!!");
		return model;
	}

}
