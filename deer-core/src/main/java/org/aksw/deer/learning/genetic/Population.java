package org.aksw.deer.learning.genetic;

import org.aksw.deer.learning.EvaluationResult;
import org.aksw.deer.learning.FitnessFunction;
import org.aksw.deer.learning.RandomUtil;
import org.aksw.faraday_cage.engine.ThreadlocalInheritingCompletableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 */
public class Population {

  private List<Genotype> backingList;
  private PopulationEvaluationResult evaluationResult = null;

  Population(int size, Supplier<Genotype> genotypeSupplier) {
    this(size);
    fillPopulation(size, genotypeSupplier);
  }

  Population(int size) {
    backingList = new ArrayList<>(size);
  }

  Population(Population population) {
    this.backingList = new ArrayList<>(population.backingList);
  }

  public PopulationEvaluationResult evaluate(FitnessFunction f) {
    if (Objects.isNull(evaluationResult)) {
      CompletableFuture<EvaluationResult> joiner = ThreadlocalInheritingCompletableFuture.completedFuture(null);
      backingList.stream()
        .map(g -> g.getBestEvaluationResult(f))
        .reduce(joiner, (g, h) -> g.thenCombine(h, (x, y) -> null))
        .join();
      evaluationResult = new PopulationEvaluationResult(this.backingList);
    }
    return evaluationResult;
  }

  public void importPopulation(int limit, Supplier<Collection<Genotype>> genotypeSupplier) {
    while (backingList.size() < limit) {
      backingList.addAll(genotypeSupplier.get());
    }
  }

  public void fillPopulation(int limit, Supplier<Genotype> genotypeSupplier) {
    while (backingList.size() < limit) {
      backingList.add(genotypeSupplier.get());
    }
    backingList = new ArrayList<>(backingList);
  }

  public Population getMutatedPopulation(Supplier<Mutator> mutator, double mutationProbability, double mutationRate, Predicate<Genotype> exclude) {
    Population mutated = new Population(backingList.size());
    backingList.forEach(g -> mutated.backingList.add(
      (RandomUtil.get() < mutationProbability && !exclude.test(g)) ? mutator.get().mutate(g, mutationRate) : g
      ));
    return mutated;
  }

  public Genotype getRandomGenotype() {
    return backingList.get(RandomUtil.get(backingList.size()));
  }

  public int size() {
    return backingList.size();
  }

}