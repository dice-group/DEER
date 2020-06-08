package org.aksw.deer.learning.genetic;

/**
 *
 */
public interface Mutator {

  Genotype mutate(Genotype original, double mutationRate);

}