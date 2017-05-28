package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class EXEC {

  public static final String uri = "http://geoknow.org/executionontology/";
  public static final String prefix = "DEEREX";
  public static final Property subGraphId = property("subGraphId");
  public static final Property isStartNode = property("isStartNode");


  private static Property property(String name) {
    return ResourceFactory.createProperty(uri + name);
  }

  protected static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

}