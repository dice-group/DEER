package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

/**
 * Default implementation of {@code ParameterMap}
 */

public class ParameterMapImpl implements ParameterMap {

  /**
   * Initialized state variable
   */
  private boolean initialized = false;
  /**
   * Set of allowed parameters
   */
  private Set<Parameter> parameters = new HashSet<>();
  /**
   * Map of parameter values
   */
  private Map<Parameter, Object> values = new HashMap<>();

  /**
   * Construct this {@code ParameterMapImpl} for the given {@code Parameter... p}
   * @param p  an arbitrary number of parameters as a vararg
   */
  public ParameterMapImpl(Parameter...p) {
    this.parameters.addAll(Arrays.asList(p));
  }

  @Override
  public Set<Parameter> getAllParameters() {
    return parameters;
  }


  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(Parameter p, T defaultValue) throws IllegalStateException {
    if (!initialized) {
      throw new IllegalStateException("ParameterMap needs to be initialized before usage!");
    }
    try {
      if (values.containsKey(p)) {
        return (T) values.get(p);
      } else {
        return defaultValue;
      }
    } catch (ClassCastException e) {
      ClassCastException ee = new ClassCastException("Unable to retrieve parameter " + p +
        " as instance of required type.");
      ee.initCause(e);
      throw ee;
    }
  }

  @Override
  public <T> T getValue(Parameter p) throws IllegalStateException {
    return getValue(p, null);
  }

  @Override
  public ParameterMap init(Resource r) {
    for (Parameter p : parameters) {
      if (r != null && r.hasProperty(p.getProperty())) {
        RDFNode node = r.getProperty(p.getProperty()).getObject();
        setValue(p, p.applyDeserialization(node));
      }
      if (p.isRequired() && values.get(p) == null) {
        throw new RuntimeException("Required parameter '" + p + "' not defined!");
      }
    }
    initialized = true;
    return this;
  }

  @Override
  public ParameterMap setValue(Parameter p, Object o) throws IllegalStateException {
    if (initialized) {
      throw new IllegalStateException("ParameterMap can not set values after being initialized!");
    }
    values.put(p, o);
    return this;
  }

}