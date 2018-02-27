package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

@Deprecated
public class DEER {

  public static final String uri = "http://deer.aksw.org/vocabulary/#";
  public static final String prefix = ":";
  public static final Property hasUri = property("hasUri");
  public static final Property fromGraph = property("fromGraph");
  public static final Property graphTriplePattern = property("graphTriplePattern");

  public static Property property(String name) {
    return ResourceFactory.createProperty(uri + name);
  }

  public static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

  public static String getURI() {
    return uri;
  }

}
