package org.aksw.deer.learning.old;

import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;

/**
 *
 *
 *
 */
public interface SelfConfigurator {

  ValidatableParameterMap selfConfig(Model source, Model target);

}
