package org.aksw.deer.parameter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */

public class BaseParameterMap implements ParameterMap {

  protected Set<Parameter> parameters = new HashSet<>();
  protected Map<String, String> valueMap = new HashMap<>();
  protected boolean initialized = false;

  @Override
  public Set<Parameter> getAllParameters() {
    return parameters;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  public void setValue(Parameter parameter, String value) {
    valueMap.put(parameter.getProperty().getURI(), value);
  }

  public String getValue(Parameter parameter) {
    return valueMap.get(parameter.getProperty().getURI());
  }

  public void init() {
    for (Parameter p : parameters) {
      if (p.isRequired() && valueMap.get(p.getProperty().getURI()) == null) {
        //@todo: implement exception
        throw new RuntimeException("Required parameter " + p.getProperty().getURI() + " not defined!");
      }
    }
    initialized = true;
  }

  @Override
  public void init(Resource parameterRoot) {
    init();
  }

}
