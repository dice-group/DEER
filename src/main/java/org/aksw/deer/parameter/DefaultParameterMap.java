package org.aksw.deer.parameter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

/**
 * @author Kevin Dreßler
 */

public class DefaultParameterMap implements ParameterMap {

  private Set<Parameter> parameters = new HashSet<>();
  private boolean initialized = false;
  private Map<String, Object> values = new HashMap<>();

  public DefaultParameterMap(Parameter...p) {
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
      String key = p.getProperty().getURI();
      if (values.containsKey(key)) {
        return (T) values.get(key);
      } else {
        return defaultValue;
      }
    } catch (ClassCastException e) {
      ClassCastException ee = new ClassCastException("Unable to retrieve parameter " + p.getProperty().getURI() + " of instance " + "");
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
      if (p.isRequired() && values.get(p.getProperty().getURI()) == null) {
        throw new RuntimeException("Required parameter " + p.getProperty().getURI() + " not defined!");
      }
    }
    initialized = true;
    return this;
  }

  public ParameterMap setValue(Parameter p, Object o) {
    values.put(p.getProperty().getURI(),o);
    return this;
  }

}