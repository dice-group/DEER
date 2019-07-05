package org.aksw.deer.learning.genetic;

/**
 *
 */
public interface Recombinator {

  Genotype[] recombinate(Genotype parentA, Genotype parentB);

}