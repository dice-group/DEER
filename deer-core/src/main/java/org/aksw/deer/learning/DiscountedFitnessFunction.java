package org.aksw.deer.learning;

/**
 *
 */
public class DiscountedFitnessFunction extends FitnessFunction {

  private final double omega;

  public DiscountedFitnessFunction(int[] weights, double beta, double omega) {
    super(weights, beta);
    this.omega = omega;
  }

  public double getFitness(EvaluationResult evaluationResult, double complexity) {
    return getFitness(evaluationResult) - omega * complexity;
  }

}
