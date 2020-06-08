package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.RandomUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class TournamentSelector {

  private int size;
  private double p;

  TournamentSelector(int size, double p) {
    this.size = size;
    this.p = p;
  }

  Genotype select(Population population) {
    List<Genotype> tournament = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      tournament.add(population.getRandomGenotype());
    }
    tournament.sort(Comparator.comparingDouble(Genotype::getBestFitness).reversed());
    for (int i = 0; i < size; i++) {
      if (RandomUtil.get() < p) {
        return tournament.get(i);
      }
    }
    return tournament.get(size - 1);
  }

}
