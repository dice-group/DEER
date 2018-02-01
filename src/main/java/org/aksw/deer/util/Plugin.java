package org.aksw.deer.util;

import java.util.function.Consumer;
import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Resource;

/**
 * @author Kevin Dreßler
 */
public interface Plugin extends Consumer<ParameterMap> {

  ParameterMap getParameterMap();

  ParameterMap createParameterMap();

  Resource getType();

}
