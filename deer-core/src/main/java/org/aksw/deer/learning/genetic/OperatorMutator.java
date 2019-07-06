package org.aksw.deer.learning.genetic;

/**
 *
 */
public class OperatorMutator extends AbstractMutator {

  @Override
  protected void mutateRow(Genotype g, int i) {
    g.addRow(i, RandomOperatorFactory.getForArity(g.getRow(i)[0]), g.getRow(i));
  }
}
