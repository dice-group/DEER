package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

/**
 * Default implementation of {@code ParameterMap}
 */

public class ParameterMapImpl implements ParameterMap {

  private boolean initialized = false;
  private Set<Parameter> parameters = new HashSet<>();
  private Map<Parameter, Object> values = new HashMap<>();

  public ParameterMapImpl(Parameter...p) {
    this.parameters.addAll(Arrays.asList(p));
  }

  @Override
  public Set<Parameter> getAllParameters() {
    return parameters;
  }

  @Override
  public ParameterMap addParameter(Parameter p) {
    parameters.add(p);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(Parameter p, T defaultValue) {
    if (!initialized) {
      throw new RuntimeException("ParameterMap needs to be initialized before usage!");
    }
    try {
      if (values.containsKey(p)) {
        return (T) values.get(p);
      } else {
        return defaultValue;
      }
    } catch (ClassCastException e) {
      ClassCastException ee = new ClassCastException("Unable to retrieve parameter " + p +
        " of type " + defaultValue.getClass().getSimpleName());
      ee.initCause(e);
      throw ee;
    }
  }

  public <T> T getValue(Parameter p) {
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

  public ParameterMap setValue(Parameter p, Object o) {
    values.put(p, o);
    return this;
  }

}