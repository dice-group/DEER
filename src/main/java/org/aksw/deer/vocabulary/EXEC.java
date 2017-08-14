package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class EXEC {

  public static final String uri = "http://aksw.org/deer/execution/ontology#";
  public static final String prefix = "dexec";
  public static final Property subGraphId = ResourceFactory.createProperty(uri + "subGraphId");

}