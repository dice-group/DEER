package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.FitnessFunction;
import org.aksw.deer.learning.RandomUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class GeneticProgrammingAlgorithm {

  private final Population startingPopulation;
  private final FitnessFunction fitnessFunction;
  private final TournamentSelector selector;
  private final List<Recombinator> recombinators;
  private final double offspringFraction;
  private final List<Mutator> mutators;
  private final double mutationProbability;
  private final double mutationRate;


  public GeneticProgrammingAlgorithm(Population startingPopulation, FitnessFunction fitnessFunction,
                                     TournamentSelector selector, List<Recombinator> recombinators,
                                     double offspringFraction, List<Mutator> mutators,
                                     double mutationProbability, double mutationRate) {
    this.fitnessFunction = fitnessFunction;
    this.mutationProbability = mutationProbability;
    this.mutationRate = mutationRate;
    this.mutators = mutators;
    this.recombinators = recombinators;
    this.offspringFraction = offspringFraction;
    this.startingPopulation = startingPopulation;
    this.selector = selector;
  }

  public List<PopulationEvaluationResult> run() {
    int generation = 0;
    int convergenceCounter = 0;
    double localMP = mutationProbability;
    double localMR = mutationRate;
    final List<PopulationEvaluationResult> evolutionHistory = new ArrayList<>();
    evolutionHistory.add(startingPopulation.evaluate(fitnessFunction));
    Population currentPopulation = startingPopulation;
    while (!mustTerminate(generation, evolutionHistory.get(generation).getBest(), convergenceCounter)) {
      // find elite
      final Genotype bestInGeneration = evolutionHistory.get(generation).getBest();
      Population nextPopulation = new Population(1, () -> bestInGeneration);
      // generate offspring
      final Population selectionPopulation = currentPopulation;
      nextPopulation.importPopulation((int)((currentPopulation.size())*offspringFraction),
        () -> {
          Genotype parent1 = selector.select(selectionPopulation);
          Genotype parent2 = selector.select(selectionPopulation);
          Genotype[] childs = getRecombinator().recombinate(parent1, parent2);
          return Arrays.asList(childs);
        });
      // select survivors
      nextPopulation.fillPopulation(currentPopulation.size(),
        () -> {
        if (RandomUtil.get() < 0.5) {
          return selector.select(selectionPopulation).compactBestResult(false, 0);
        } else {
          return selector.select(selectionPopulation).getEvaluatedCopy();
        }
        });
      // mutation, preserve elite
      nextPopulation.evaluate(fitnessFunction);
      currentPopulation = nextPopulation
        .getMutatedPopulation(this::getMutator, localMP, localMR, g -> g == bestInGeneration);
      // evaluation & storage of results
      evolutionHistory.add(currentPopulation.evaluate(fitnessFunction));
      // repeat
      generation++;
      if (localMP != mutationProbability) {
        localMP = Math.max(mutationProbability, localMP/5*4);
      }
      if (localMR != mutationRate) {
        localMR = Math.max(mutationRate, localMR/5*4);
      }
      if (converged(evolutionHistory)) {
        convergenceCounter++;
        localMP = 1.0;
        localMR = 1.0;
      }
    }
    return evolutionHistory;
  }

  private boolean converged(List<PopulationEvaluationResult> history) {
    int lookAhead = 10;
    if (history.size() < lookAhead) {
      return false;
    }
    double f = history.get(history.size()-1).getMax();
//    List<Resource> smell = history.get(history.size()-1).getBest().getSmell(true);
    for (int i = history.size()-lookAhead; i < history.size(); i++) {
      if (history.get(i).getMax() != f || history.get(i).getStandardDeviation() > 0.1 )
        return false;
    }
    return true;
  }

  private boolean mustTerminate(int generation, Genotype bestInGeneration, int convergenceCounter) {
    return generation >= 2000-1 || bestInGeneration.getBestFitness() == 1.0 || convergenceCounter == 5;
  }

  private Mutator getMutator() {
    return mutators.get(RandomUtil.get(mutators.size()));
  }

  private Recombinator getRecombinator() {
    return recombinators.get(RandomUtil.get(recombinators.size()));
  }

}
