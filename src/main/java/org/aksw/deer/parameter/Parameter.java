package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 */
public interface Parameter {

  boolean isRequired();

  Property getProperty();

  RDFNode applySerialization(Object object);

  Object applyDeserialization(RDFNode node);

}