package org.aksw.deer.learning;

import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface SelfConfigurator {

  @NotNull
  ValidatableParameterMap selfConfig(Model source, Model target);

}
