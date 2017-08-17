package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.Property;

/**
 * @author Kevin Dreßler
 */
public class ParameterImpl implements Parameter {

  private Property property;
  private String description;
  private boolean required;

  public ParameterImpl(Property property, String description, boolean required) {
    this.property = property;
    this.description = description;
    this.required = required;
  }

  @Override
  public Property getProperty() {
    return property;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isRequired() {
    return required;
  }
}
