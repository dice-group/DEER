package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class FOXO {

  public static final String url = "http://ns.aksw.org/fox/ontology#";
  public static final Resource LOCATION = resource("LOCATION");
  public static final Resource ORGANIZATION = resource("ORGANIZATION");
  public static final Resource PERSON = resource("PERSON");
  public static final Property RELATED_TO = property("relatedTo");

  private static Property property(String name) {
    Property result = ResourceFactory.createProperty(url + name);
    return result;
  }

  private static Resource resource(String name) {
    Resource result = ResourceFactory.createResource(url + name);
    return result;
  }

  public static String getURI() {
    return url;
  }

}