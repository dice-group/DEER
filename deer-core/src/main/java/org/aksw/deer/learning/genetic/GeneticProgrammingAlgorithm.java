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
    final List<PopulationEvaluationResult> evolutionHistory = new ArrayList<>();
    evolutionHistory.add(startingPopulation.evaluate(fitnessFunction));
    Population currentPopulation = startingPopulation;
    while (!mustTerminate(generation, evolutionHistory.get(generation).getBest())) {
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
        () -> selector.select(selectionPopulation).getEvaluatedCopy());
      // mutation, preserve elite
      currentPopulation = nextPopulation
        .getMutatedPopulation(this::getMutator, mutationProbability, mutationRate, g -> g == bestInGeneration);
      // evaluation & storage of results
      evolutionHistory.add(currentPopulation.evaluate(fitnessFunction));
      // repeat
      generation++;
    }
    return evolutionHistory;
  }

  private boolean mustTerminate(int generation, Genotype bestInGeneration) {
    return generation >= 5000-1 || bestInGeneration.getBestFitness() == 1.0;
  }

  private Mutator getMutator() {
    return mutators.get(RandomUtil.get(mutators.size()));
  }

  private Recombinator getRecombinator() {
    return recombinators.get(RandomUtil.get(recombinators.size()));
  }

}
