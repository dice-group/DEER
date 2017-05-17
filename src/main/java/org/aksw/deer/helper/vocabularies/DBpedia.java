/**
 *
 */
package org.aksw.deer.helper.vocabularies;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class DBpedia {

  public static final String endPoint = "http://dbpedia.org/sparql";
  public static final String liveEndPoint = "http://live.dbpedia.org/sparql";
  public static final String uri = "http://dbpedia.org/";
  public static final String resourceUri = "http://dbpedia.org/resource/";
  public static final String ontologyUri = "http://dbpedia.org/ontology/";
  public static final Property Place = property("Place");

  private static Property property(String name) {
    Property result = ResourceFactory.createProperty(ontologyUri + name);
    return result;
  }

  public static String getURI() {
    return uri;
  }

}