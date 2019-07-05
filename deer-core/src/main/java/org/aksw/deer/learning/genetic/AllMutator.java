package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.RandomUtil;

/**
 *
 */
public class AllMutator extends AbstractMutator {

  @Override
  protected void mutateRow(Genotype g, short i) {
    short newArity = (short) RandomUtil.get(1, RandomOperatorFactory.getMaxArity()+1);
    short[] mutatedRow = new short[2+2*newArity];
    mutatedRow[0] = newArity;
    mutatedRow[1] = 1;
    for (int j = 0; j < mutatedRow[0]; j++) {
      mutatedRow[2+j*2] = (short) RandomUtil.get(i);
    }
    g.addRow(i, RandomOperatorFactory.getForArity(newArity), mutatedRow);
  }
}