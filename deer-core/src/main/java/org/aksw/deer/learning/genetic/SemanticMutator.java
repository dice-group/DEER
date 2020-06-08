package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.learning.ReverseLearnable;
import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 */
public class SemanticMutator extends AbstractMutator {
  @Override
  protected void mutateRow(Genotype g, int i) {
    List<EnrichmentOperator> all = RandomOperatorFactory.getAll();
    Model target = g.trainingData.getTrainingTarget();
    int[] count = new int[all.size()];
    double[] sum = new double[all.size()];
    for (int k = 0; k < all.size(); k++) {
      EnrichmentOperator op = all.get(k);
      for (int j = g.getNumberOfInputs(); j < g.getSize(); j++) {
        List<Model> inputModels = g.getInputModels(j);
        double applicability;
        if (inputModels.size() > op.getInDegree()) {
          double applicability1 = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(0)), target);
          double applicability2 = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(1)), target);
          applicability = Math.max(applicability1, applicability2);
        } else if (inputModels.size() == op.getInDegree()) {
          applicability = ((ReverseLearnable)op).predictApplicability(inputModels, target);
        } else {
          applicability = ((ReverseLearnable)op).predictApplicability(List.of(inputModels.get(0), inputModels.get(0)), target);
        }
        if (applicability > 0) {
          sum[k] += applicability > 1 ? 1 : applicability;
          count[k]++;
        }
      }
    }
    // prefer high mean applicability and low count
    double bestScore = Double.NEGATIVE_INFINITY;
    int bestK = 0;
    for (int k = 0; k < all.size(); k++) {
      double score = sum[k] - (double) count[k];
      if (score > bestScore) {
        bestScore = score;
        bestK = k;
      }
    }
    EnrichmentOperator newOp = all.get(bestK);
    List<Integer> inputs = g.getInputs(i);
    if (inputs.size() > newOp.getInDegree()) {
      List<Model> inputModels = g.getInputModels(i);
      double applicability1 = ((ReverseLearnable)newOp).predictApplicability(List.of(inputModels.get(0)), target);
      double applicability2 = ((ReverseLearnable)newOp).predictApplicability(List.of(inputModels.get(1)), target);
      if (applicability1 > applicability2) {
        g.addRow(i, newOp, new int[]{1,1,inputs.get(0),0});
      } else {
        g.addRow(i, newOp, new int[]{1,1,inputs.get(1),0});
      }
    } else if (inputs.size() < newOp.getInDegree()) {
      g.addRow(i, newOp, new int[]{2,1,inputs.get(0),0,inputs.get(0),0});
    } else if (inputs.size() == 2) {
      g.addRow(i, newOp, new int[]{2,1,inputs.get(0),0,inputs.get(1),0});
    } else {
      g.addRow(i, newOp, new int[]{1,1,inputs.get(0),0});
    }
  }
}
