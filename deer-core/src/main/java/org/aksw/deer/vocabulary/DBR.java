package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 */
public class DBR {
  public static final String uri = "http://dbpedia.org/resource/";

  private static Property property(String name) {
    Property result = ResourceFactory.createProperty(uri + name);
    return result;
  }

  public static String getURI() {
    return uri;
  }

}
