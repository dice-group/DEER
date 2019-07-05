package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.RandomUtil;

/**
 *
 */
public class InputsMutator extends AbstractMutator {

  @Override
  protected void mutateRow(Genotype g, short i) {
    short[] mutatedRow = g.getRow(i);
    for (int j = 0; j < mutatedRow[0]; j++) {
      mutatedRow[2+j*2] = (short) RandomUtil.get(i);
    }
    g.addRow(i, g.getRawNode(i), mutatedRow);
  }
}
