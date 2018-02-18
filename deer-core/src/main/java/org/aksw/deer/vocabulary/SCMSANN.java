package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class SCMSANN {

  public static final String scmsAnnotation = "http://ns.aksw.org/scms/annotations/";
  public static final Property LOCATION = property("LOCATION");
  public static final Property ORGANIZATION = property("ORGANIZATION");
  public static final Property PERSON = property("PERSON");
  public static final Property MEANS = property("means");

  private static Property property(String name) {
    Property result = ResourceFactory.createProperty(scmsAnnotation + name);
    return result;
  }

  public static String getURI() {
    return scmsAnnotation;
  }

}