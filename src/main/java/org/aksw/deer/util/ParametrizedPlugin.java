package org.aksw.deer.util;

import org.aksw.deer.parameter.ParameterMap;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Base interface for parametrized plugins, defines contract.
 * <p>
 * A {@code ParametrizedPlugin} is a class that
 */
public interface ParametrizedPlugin extends Consumer<ParameterMap> {

  @NotNull
  ParameterMap getParameterMap();

  @NotNull
  ParameterMap createParameterMap();

  @NotNull
  Resource getType();

  void accept(@NotNull ParameterMap params);

}
