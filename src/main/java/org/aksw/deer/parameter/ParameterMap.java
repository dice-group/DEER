package org.aksw.deer.parameter;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public interface ParameterMap {

  ParameterMap EMPTY_INSTANCE = new DefaultParameterMap().init(null);

  ParameterMap addParameter(Parameter p);

  Set<Parameter> getAllParameters();

  ParameterMap setValue(Parameter p, Object node);

  <T> T getValue(Parameter p);

  ParameterMap init(Resource r);

}