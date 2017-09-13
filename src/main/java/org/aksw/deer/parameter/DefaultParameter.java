package org.aksw.deer.parameter;

import org.aksw.deer.vocabulary.DEER;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Kevin Dreßler
 */
public class DefaultParameter implements Parameter {

  private Property property;
  private String description;
  private boolean required;
  private ParameterConversion conversion;

  public DefaultParameter(String propertyName, String description,
                          ParameterConversion conversion, boolean required) {
    this(DEER.property(propertyName), description, conversion, required);
  }

  public DefaultParameter(String propertyName, String description) {
    this(DEER.property(propertyName), description, StringParameterConversion.INSTANCE, true);
  }

  private DefaultParameter(Property property, String description,
                           ParameterConversion conversion, boolean required) {
    this.property = property;
    this.description = description;
    this.required = required;
    this.conversion = conversion;
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
  public RDFNode applySerialization(Object object) {
    return conversion.serialize(object);
  }

  @Override
  public Object applyDeserialization(RDFNode node) {
    return conversion.deserialize(node);
  }

  @Override
  public boolean isRequired() {
    return required;
  }
}