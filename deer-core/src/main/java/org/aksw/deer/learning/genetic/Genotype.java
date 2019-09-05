package org.aksw.deer.learning.genetic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.aksw.deer.DeerExecutionNode;
import org.aksw.deer.ParameterizedDeerExecutionNode;
import org.aksw.deer.enrichments.EnrichmentOperator;
import org.aksw.deer.learning.EvaluationResult;
import org.aksw.deer.learning.FitnessFunction;
import org.aksw.faraday_cage.engine.CompiledExecutionGraph;
import org.aksw.faraday_cage.engine.ExecutionGraph;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ThreadlocalInheritingCompletableFuture;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class Genotype extends ExecutionGraph<Model> {

  public static int SIZE = 30;

  protected TrainingData trainingData;

  int bestResultRow = -1;

  private double bestFitness = -1;

  EvaluationResult[] results = null;

  boolean evaluated = false;

  private Multimap<Integer, Integer> transitiveHull = null;

  private Set<Integer> outputs = null;

  protected Genotype() {
    super(SIZE);
  }

  protected Genotype(int size) {
    super(size);
  }

  protected Genotype(TrainingData trainingData) {
    super(SIZE);
    this.trainingData = trainingData;
    int currentRow = 0;
    for (ParameterizedDeerExecutionNode reader : trainingData.getTrainingReaders()) {
      addRow(currentRow++, reader, new int[]{0,1});
    }
  }

  public Genotype(Genotype other) {
    super(other.getSize());
    this.trainingData = other.trainingData;
    for (int i = 0; i < getSize(); i++) {
      entries[i] = Arrays.copyOf(other.entries[i], other.entries[i].length);
      if (i >= getNumberOfInputs()) {
        ops.set(i, RandomOperatorFactory.reproduce((EnrichmentOperator)other.ops.get(i)));
      } else {
        ops.set(i, other.ops.get(i));
      }
    }
  }

  Genotype getEvaluatedCopy(boolean evaluated) {
    Genotype copy = new Genotype(this);
    copy.results = Arrays.copyOf(this.results, this.results.length);
    copy.bestFitness = this.bestFitness;
    copy.bestResultRow = this.bestResultRow;
    copy.evaluated = evaluated;
    return copy;
  }

  Genotype getEvaluatedCopy() {
    return getEvaluatedCopy(true);
  }

  ExecutionNode<Model> getRawNode(int i) {
    return ops.get(i);
  }

  public ExecutionNode<Model> getNode(int i) {
    return getWrappedNode(i);
  }

  private ExecutionNode<Model> getWrappedNode(int i) {
    if (i < getNumberOfInputs()) {
      return ops.get(i);
    } else {
      return SelfConfigurationWrapper.wrap((DeerExecutionNode) ops.get(i),
        evaluationResult -> results[i] = evaluationResult,
        trainingData.getTrainingTarget());
    }
  }

  private CompletableFuture<EvaluationResult> evaluate(FitnessFunction f) {
    CompiledExecutionGraph compiled = CompiledExecutionGraph.of(this);
    CompletableFuture<Void> completionStage = compiled.getCompletionStage();
    CompletableFuture<EvaluationResult> result = completionStage.thenApply(x -> findAndSetBestEvaluationResult(f));
    compiled.run();
    return result;
  }

  private EvaluationResult findAndSetBestEvaluationResult(FitnessFunction f) {
    for (int i = getNumberOfInputs(); i < results.length; i++) {
      double fitness = f.getFitness(results[i]);
      if (fitness > bestFitness) {
        bestFitness = fitness;
        bestResultRow  = i;
      }
    }
    evaluated = true;
    return results[bestResultRow];
  }

  public CompletableFuture<EvaluationResult> getBestEvaluationResult() {
   return getBestEvaluationResult(trainingData.getFitnessFunction());
  }

  public CompletableFuture<EvaluationResult> getBestEvaluationResult(FitnessFunction f) {
    if (!evaluated) {
      results = new EvaluationResult[getSize()];
      bestResultRow = -1;
      bestFitness = -1;
      return evaluate(f);
    }
    return ThreadlocalInheritingCompletableFuture.completedFuture(results[bestResultRow]);
  }

  public double getBestFitness() {
    return bestFitness;
  }

  int getNumberOfInputs() {
    return trainingData.getTrainingSources().size();
  }


  Genotype compactBestResult(boolean shrink, int rightShiftSize) {
    NavigableSet<Integer> relevantRows = new TreeSet<>(getRelevantRows(this.bestResultRow));
    relevantRows.add(this.bestResultRow);
    for (int i = 0; i < getNumberOfInputs(); i++) {
      relevantRows.add(i);
    }
    Map<Integer, Integer> skipMap = new HashMap<>();
    // strip no ops
    List<Model> trainingSources = this.trainingData.getTrainingSources();

    for (int i : relevantRows) {
      if (i >= this.getNumberOfInputs()) { //ignore readers
        for (int input : getInputs(i)) {
          if (skipMap.containsKey(input)) {
            input = skipMap.get(input);
          }
          if (getResultModel(input).isIsomorphicWith(getResultModel(i))) {
            // no op
            skipMap.put(i, input);
            break;
          }
        }
      }
    }
    Genotype compacted;
    if (shrink) {
      compacted = new Genotype(relevantRows.size()-skipMap.size());
    } else {
      compacted = new Genotype();
    }
    if (rightShiftSize < 0) {
      rightShiftSize = 0;
    }
    compacted.trainingData = trainingData;
    compacted.results = new EvaluationResult[compacted.getSize()];
    int k = 0;
    List<ParameterizedDeerExecutionNode> readers = this.trainingData.getEvaluationReaders();
    int[] rowMapping = new int[getSize()];
    for (int i : relevantRows) {
      if (k < readers.size()) {
        rowMapping[i] = k;
        compacted.addRow(k, readers.get(k++), this.getRow(i));
      } else if (!skipMap.containsKey(i)) {
        rowMapping[i] = k + rightShiftSize;
        int[] row = Arrays.copyOf(this.getRow(i), this.getRow(i).length);
        int arity = row[0];
        for (int j = 0; j < arity; j++) {
          int inputIndex = row[2 + 2 * j];
          if (skipMap.containsKey(inputIndex)) {
            inputIndex = skipMap.get(inputIndex);
          }
          inputIndex = rowMapping[inputIndex];
          row[2+2*j] = inputIndex;
        }
        compacted.results[rightShiftSize + k] = results[i];
        compacted.addRow(rightShiftSize + k++, RandomOperatorFactory.reproduce((EnrichmentOperator)this.getRawNode(i)), row);
      }
    }
    compacted.bestResultRow = rightShiftSize + relevantRows.size()-1 - skipMap.size();
    compacted.bestFitness = bestFitness;
    for (int i = 0; i < compacted.getSize(); i++) {
      if (compacted.getRawNode(i) == null) {
        RandomGenotype.addRandomRow(compacted, i);
      }
    }
    return compacted;
  }

  private Model getResultModel(int i) {
      if (i < trainingData.getTrainingSources().size()) {
        return trainingData.getTrainingSources().get(i);
      }
      return this.results[i].getResultModel();
  }

  Set<Resource> getSmell() {
    Set<Resource> smell = new HashSet<>();
    for (int i = getNumberOfInputs(); i <= bestResultRow; i++) {
      smell.add(getRawNode(i).getType());
    }
    return smell;
  }

  List<Resource> getSmell(boolean full) {
    List<Resource> smell = new ArrayList<>();
    for (int i = getNumberOfInputs(); i <= (full ? getSize() : bestResultRow); i++) {
      smell.add(getRawNode(i).getType());
    }
    return smell;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < getSize(); i++) {
      sb.append(ops.get(i).getType().getLocalName());
      sb.append(" ");
      sb.append(entries[i][0]);
      sb.append(" ");
      sb.append(entries[i][1]);
      sb.append("\n");
    }
    return sb.toString();
  }

  public List<Integer> getInputs(int i) {
    int[] row = getRow(i);
    int n = row[0];
    List<Integer> result = new ArrayList<>(n);
    for (int j = 0; j < n; j++) {
      result.add(row[2+2*j]);
    }
    return result;
  }

  public List<Model> getInputModels(int i) {
    return getInputs(i).stream().map(this::getResultModel)
      .collect(Collectors.toList());
  }

  public Collection<Integer> getRelevantRows(int i) {
    if (transitiveHull == null) {
      computeTransitiveHullAndOutputs();
    }
    return transitiveHull.get(i);
  }

  private void computeTransitiveHullAndOutputs() {
    this.transitiveHull = HashMultimap.create();
    this.outputs = IntStream.range(0,getSize()).boxed().collect(Collectors.toSet());
    for (int i = getNumberOfInputs(); i < getSize(); i++) {
      List<Integer> inputs = getInputs(i);
      for (int j : inputs) {
        if (transitiveHull.containsKey(j)) {
          transitiveHull.putAll(i, transitiveHull.get(j));
        }
      }
      transitiveHull.putAll(i, inputs);
      outputs.removeAll(transitiveHull.get(i));
    }
  }

}