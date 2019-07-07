package org.aksw.deer.learning.genetic;

import org.aksw.deer.enrichments.EnrichmentOperator;

/**
 *
 */
public class SemanticRecombinator implements Recombinator {
  @Override
  public Genotype[] recombinate(Genotype parentA, Genotype parentB) {
    parentA = parentA.compactBestResult(false, 0);
    parentB = parentB.compactBestResult(false, 0);
    if (parentB.bestResultRow + 1 + parentA.bestResultRow + 1 - parentA.getNumberOfInputs() + 1 <= Genotype.SIZE) {
      parentB = parentB.compactBestResult(false, parentA.bestResultRow + 1 - parentA.getNumberOfInputs());
      for (int i = parentA.getNumberOfInputs(); i <= parentB.bestResultRow; i++) {
        if (i <= parentA.bestResultRow) {
          parentB.addRow(i, RandomOperatorFactory.reproduce((EnrichmentOperator) parentA.getRawNode(i)), parentA.getRow(i));
        } else {
          parentA.addRow(i, RandomOperatorFactory.reproduce((EnrichmentOperator) parentB.getRawNode(i)), parentB.getRow(i));
        }
      }
      parentA.addRow(parentB.bestResultRow + 1, RandomOperatorFactory.getForArity(2), new int[]{2, 1, parentA.bestResultRow, 0, parentB.bestResultRow, 0});
      parentB.addRow(parentB.bestResultRow + 1, RandomOperatorFactory.getForArity(2), new int[]{2, 1, parentA.bestResultRow, 0, parentB.bestResultRow, 0});
    }
    return new Genotype[]{parentA, parentB};
  }
}
