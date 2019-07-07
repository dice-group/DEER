package org.aksw.deer.learning.genetic;

/**
 *
 */
public class AllMutator extends AbstractMutator {

  @Override
  protected void mutateRow(Genotype g, int i) {
    RandomGenotype.addRandomRow(g, i);
  }
}