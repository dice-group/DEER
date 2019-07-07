package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.RandomUtil;

/**
 *
 */
public abstract class AbstractMutator implements Mutator {
  @Override
  public Genotype mutate(Genotype original, double mutationRate) {
    Genotype m = original.getEvaluatedCopy();
    for (int i = m.getNumberOfInputs(); i < m.getSize(); i++) {
      if (RandomUtil.get() < mutationRate) {
        mutateRow(m, i);
      }
    }
    return m;
  }

  protected abstract void mutateRow(Genotype g, int i);

}
