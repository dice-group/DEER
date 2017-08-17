package org.aksw.deer.parameter;

import java.util.Set;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public interface ParameterMap {

  Set<Parameter> getAllParameters();

  boolean isInitialized();

  void init();

  void init(Resource parameterRoot);

}