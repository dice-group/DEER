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
    while (currentRow < getSize()) {
      int arity = RandomUtil.get(1, Math.min(currentRow, RandomOperatorFactory.getMaxArity())+1);
      EnrichmentOperator op = RandomOperatorFactory.getForMaxArity(arity);
      arity = ((Learnable) op).getLearnableDegreeBounds().minIn();
      op.initDegrees(arity, 1);
      short[] row = new short[2+arity*2];
      row[0] = (short) arity;
      row[1] = 1;
      for (int i = 0; i < arity; i++) {
        row[2+i*2] = (short) RandomUtil.get(currentRow);
        row[2+i*2+1] = 0;
      }
      addRow(currentRow++, op, row);
    }
  }

}
