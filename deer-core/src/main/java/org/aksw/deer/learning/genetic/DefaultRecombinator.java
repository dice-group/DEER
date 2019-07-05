package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.RandomUtil;

/**
 *
 */
public class DefaultRecombinator implements Recombinator {
  @Override
  public Genotype[] recombinate(Genotype parentA, Genotype parentB) {
    Genotype[] childs = new Genotype[2];
    childs[0] = new Genotype(parentA);
    childs[1] = new Genotype(parentB);
    short crossoverPoint = (short) RandomUtil.get(parentA.getInputSize()+1, parentA.getSize()-1);
    for (short i = crossoverPoint; i < parentA.getSize(); i++) {
      childs[0].addRow(i, parentB.getRawNode(i), parentB.getRow(i));
      childs[1].addRow(i, parentA.getRawNode(i), parentA.getRow(i));
    }
    return childs;
  }
}
