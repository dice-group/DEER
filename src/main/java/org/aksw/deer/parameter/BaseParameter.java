package org.aksw.deer.parameter;

import org.aksw.deer.vocabulary.DEER;

/**
 * @author Kevin Dreßler
 */
public class BaseParameter extends ParameterImpl {

  public BaseParameter(String propertyName, String description,
    boolean required) {
    super(DEER.property(propertyName), description, required);
  }

}