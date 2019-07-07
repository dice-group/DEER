package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.learning.Learnable;
import org.aksw.deer.learning.RandomUtil;

/**
 *
 */
public class RandomGenotype extends Genotype {

  public RandomGenotype(TrainingData trainingData) {
    super(trainingData);
    int currentRow = getNumberOfInputs();
    while (currentRow < getSize()) {
      addRandomRow(this, currentRow++);
    }
  }

  static void addRandomRow(Genotype g, int i) {
    EnrichmentOperator op = RandomOperatorFactory.getForMaxArity(RandomUtil.get(1,Math.min(i, RandomOperatorFactory.getMaxArity())+1));
    int arity = ((Learnable) op).getLearnableDegreeBounds().minIn();
    op.initDegrees(arity, 1);
    int[] row = new int[2+arity*2];
    row[0] = arity;
    row[1] = 1;
    for (int j = 0; j < arity; j++) {
      row[2+j*2] = RandomUtil.get(i);
      row[2+j*2+1] = 0;
    }
    g.addRow(i, op, row);
  }

}
