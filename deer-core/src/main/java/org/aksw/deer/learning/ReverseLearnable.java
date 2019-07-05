package org.aksw.deer.learning;

import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 */
public interface ReverseLearnable extends Learnable {

  double predictApplicability(List<Model> inputs, Model target);

  List<Model> reverseApply(List<Model> inputs, Model target);

}