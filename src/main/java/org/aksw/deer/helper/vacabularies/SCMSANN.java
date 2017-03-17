/**
 * 
 */
package org.aksw.deer.helper.vacabularies;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class SCMSANN{
	public static final String scmsAnnotation	= "http://ns.aksw.org/scms/annotations/";
	public static final String scms 			= "http://ns.aksw.org/scms/";
	public static final String ann 			= "http://www.w3.org/2000/10/annotation-ns#";
	

	private static Property property(String name) {
		Property result = ResourceFactory.createProperty(scmsAnnotation + name);
		return result;
	}
	
	public static String getURI(){ return scmsAnnotation;	}
	
	public static final Property LOCATION 	= property("LOCATION");
	public static final Property ORGANIZATION	= property("ORGANIZATION");
	public static final Property PERSON 		= property("PERSON");

}