package org.aksw.deer.learning;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 *
 */
public class FitnessFunction {

  private final double[] weights;
  private final double beta;

  public FitnessFunction(int[] weights, double beta) {
    if (weights.length != 4) {
      throw new IllegalArgumentException("Weights array must have exactly length 4");
    }
    double sum = Arrays.stream(weights).sum();
    this.weights = Arrays.stream(weights).mapToDouble(w -> (double)w/sum).toArray();
    this.beta = beta;
  }

  public double getFitness(EvaluationResult evaluationResult) {
    double[] fMeasures = evaluationResult.getIndividualFMeasures(beta);
    return getFitness(fMeasures);
  }

  public double getFitness(double[] scores) {
    return IntStream.range(0, 4)
      .mapToDouble(i -> scores[i] * weights[i])
      .sum();
  }


}
