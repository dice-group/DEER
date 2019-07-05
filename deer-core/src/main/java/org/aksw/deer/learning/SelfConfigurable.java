package org.aksw.deer.learning;

import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 *
 *
 */
public interface SelfConfigurable extends Learnable, Parameterized {

  ValidatableParameterMap learnParameterMap(List<Model> inputs, Model target, ValidatableParameterMap prototype);

}