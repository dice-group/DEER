package org.aksw.deer.parameter;

/**
 * @author Kevin Dreßler
 */
import org.apache.jena.rdf.model.Property;

public interface Parameter {

  Property getProperty();

  String getDescription();

  boolean isRequired();

}