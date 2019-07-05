package org.aksw.deer.vocabulary;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.uuid.UUID_V1;

/**
 *
 */
public class DEERA {

  public static final String NS = "http://w3id.org/deer/config-autogen/";

  public static final String PREFIX = "deera";


  public static Property property(String localName) {
    return ResourceFactory.createProperty(NS + localName);
  }

  public static Resource resource(String localName) {
    return ResourceFactory.createResource(NS + localName);
  }

  public static Resource forExecutionNode(ExecutionNode<Model> executionNode) {
    return ResourceFactory.createResource(NS + executionNode.getClass().getSimpleName() + "-" + UUID_V1.generate().asString());
  }

  public static String getURI() {
    return NS;
  }

}
