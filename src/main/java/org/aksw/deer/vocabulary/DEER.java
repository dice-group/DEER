package org.aksw.deer.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class DEER {

  public static final String uri = "http://aksw.org/deer/ontology#";
  public static final String prefix = ":";
  public static final Resource Dataset = resource("Dataset");
  public static final Resource Operator = resource("Operator");
  public static final Resource Parameter = resource("Parameter");
  public static final Property implementedIn = property("implementedIn");
  public static final Property hasInput = property("hasInput");
  public static final Property hasOutput = property("hasOutput");
  public static final Property hasParameter = property("hasParameter");
  public static final Property hasKey = property("hasKey");
  public static final Property hasValue = property("hasValue");
  public static final Property inputFile = property("inputFile");
  public static final Property outputFile = property("outputFile");
  public static final Property outputFormat = property("outputFormat");
  public static final Property hasUri = property("hasUri");
  public static final Property fromEndPoint = property("fromEndPoint");
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
