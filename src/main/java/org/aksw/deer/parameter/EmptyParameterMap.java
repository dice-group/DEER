package org.aksw.deer.parameter;

import java.util.Collections;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public class EmptyParameterMap implements ParameterMap {

  public static final EmptyParameterMap INSTANCE = new EmptyParameterMap();

  private EmptyParameterMap() { }

  @Override
  public Set<Parameter> getAllParameters() {
    return Collections.emptySet();
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void init() {

  }

  @Override
  public void init(Resource parameterRoot) {

  }
}
