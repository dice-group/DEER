package org.aksw.deer.learning;

import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface SelfConfigurator {

  @NotNull
  ParameterMap selfConfig(Model source, Model target);

}
