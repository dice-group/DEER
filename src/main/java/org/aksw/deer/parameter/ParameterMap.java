package org.aksw.deer.parameter;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

/**
 */
public interface ParameterMap {

  /**
   * An empty instance to be passed
   */
  ParameterMap EMPTY_INSTANCE = new ParameterMapImpl().init(null);

  ParameterMap addParameter(Parameter p);

  Set<Parameter> getAllParameters();

  ParameterMap setValue(Parameter p, Object node);

  <T> T getValue(Parameter p);

  <T> T getValue(Parameter p, T defaultValue);

  ParameterMap init(Resource r);

}