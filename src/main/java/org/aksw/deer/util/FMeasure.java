package org.aksw.deer.util;

import org.apache.jena.rdf.model.Model;

/**
 * Helper class for FMeasure computation.
 * Deprecated, due to be replaced with LIMES implementation.
 */
@Deprecated
public class FMeasure {

  private double p;
  private double r;
  private double f;

  public FMeasure(Model current, Model target) {
    this(computePrecision(current, target), computeRecall(current, target));
  }

  /**
   */
  public FMeasure(double p, double r) {
    this.p = p;
    this.r = r;
    this.f = 2 * p * r / (p + r);
  }

  public double precision() {
    return p;
  }

  public double recall() {
    return r;
  }

  public double fMeasure() {
    return f;
  }

  private static double computePrecision(Model current, Model target) {
    return (double) current.intersection(target).size() / (double) current.size();
  }

  private static double computeRecall(Model current, Model target) {
    return (double) current.intersection(target).size() / (double) target.size();
  }

  @Override
  public String toString() {
    return "FMeasure [p=" + p + ", r=" + r + ", f=" + f + "]";
  }

}
