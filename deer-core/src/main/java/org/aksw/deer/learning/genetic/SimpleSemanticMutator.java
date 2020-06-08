package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.learning.RandomUtil;
import org.aksw.deer.learning.ReverseLearnable;
import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 */
public class SimpleSemanticMutator extends AbstractMutator {
  @Override
  protected void mutateRow(Genotype g, int i) {
    List<EnrichmentOperator> all = RandomOperatorFactory.getAll();
    double[] applicabilities = new double[all.size()];
    boolean[] getFirst = new boolean[all.size()];
    double sum = 0;
    Model target = g.trainingData.getTrainingTarget();
    for (int k = 0; k < all.size(); k++) {
      EnrichmentOperator op = all.get(k);
      List<Model> inputModels = g.getInputModels(i);
      if (inputModels.size() > op.getInDegree()) {
        double applicability1 = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(0)), target);
        double applicability2 = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(1)), target);
        applicabilities[k] = Math.max(applicability1, applicability2);
        getFirst[k] = applicability1 >= applicability2;
      } else if (inputModels.size() == op.getInDegree()) {
        applicabilities[k] = ((ReverseLearnable)op).predictApplicability(inputModels, target);
      } else {
        applicabilities[k] = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(0), inputModels.get(0)), target);
      }
      sum += applicabilities[k];
    }

    double lo = 0;
    double hi = 0;
    double rand = RandomUtil.get();
    int bestK = 0;
    for (int k = 0; k < all.size(); k++) {
      lo = hi;
      hi += applicabilities[k]/sum;
      if (lo <= rand && hi > rand) {
        bestK = k;
        break;
      }
    }
    EnrichmentOperator op = all.get(bestK);
    List<Integer> inputs = g.getInputs(i);
    if (inputs.size() > op.getInDegree()) {
      if (getFirst[bestK]) {
        g.addRow(i, op, new int[]{1,1,inputs.get(0),0});
      } else {
        g.addRow(i, op, new int[]{1,1,inputs.get(1),0});
      }
    } else if (inputs.size() < op.getInDegree()) {
      g.addRow(i, op, new int[]{2,1,inputs.get(0),0,inputs.get(0),0});
    } else if (inputs.size() == 2) {
      g.addRow(i, op, new int[]{2,1,inputs.get(0),0,inputs.get(1),0});
    } else {
      g.addRow(i, op, new int[]{1,1,inputs.get(0),0});
    }
  }
}
