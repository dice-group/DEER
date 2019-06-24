package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class NIFCORE {

  public static final String url = "http://ns.aksw.org/scms/annotations/";
  public static final Property LOCATION = property("LOCATION");
  public static final Property ORGANIZATION = property("ORGANIZATION");
  public static final Property PERSON = property("PERSON");
  public static final Property MEANS = property("means");

  private static Property property(String name) {
    Property result = ResourceFactory.createProperty(url + name);
    return result;
  }

  public static String getURI() {
    return url;
  }

}