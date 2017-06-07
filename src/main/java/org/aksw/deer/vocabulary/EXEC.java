package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class EXEC {

  public static final String uri = "http://geoknow.org/executionontology/";
  public static final String prefix = "dexec";
  public static final Property subGraphId = property("subGraphId");
  public static final Property isStartNode = property("isStartNode");
  public static final Property isPipelineStartNode = property("isPipelineStartNode");
  public static final Resource Hub = resource("Hub");

  private static Property property(String name) {
    return ResourceFactory.createProperty(uri + name);
  }

  protected static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

}