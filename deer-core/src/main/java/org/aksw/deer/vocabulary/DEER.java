package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://ns.aksw.org/deer/#
 */
public class DEER {

  public static final String NS = "http://w3id.org/deer/";

  public static final String PREFIX = "deer";


  public static Property property(String localName) {
    return ResourceFactory.createProperty(NS + localName);
  }

  public static Resource resource(String localName) {
    return ResourceFactory.createResource(NS + localName);
  }

  public static String getURI() {
    return NS;
  }

}