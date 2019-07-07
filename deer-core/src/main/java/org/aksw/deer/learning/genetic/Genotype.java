package org.aksw.deer.learning.genetic;

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
import java.util.function.Function;

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

  Genotype getEvaluatedCopy() {
    Genotype copy = new Genotype(this);
    copy.results = Arrays.copyOf(this.results, this.results.length);
    copy.bestFitness = this.bestFitness;
    copy.bestResultRow = this.bestResultRow;
    return copy;
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

  public CompletableFuture<EvaluationResult> getBestEvaluationResult(FitnessFunction f) {
    if (!evaluated) {
      results = new EvaluationResult[getSize()];
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
    NavigableSet<Integer> relevantRows = new TreeSet<>();
    relevantRows.add(this.bestResultRow);
    for(int i = this.bestResultRow; i >= 0; i--) {
      if (i < getNumberOfInputs()) {
        relevantRows.add(i);
        continue;
      }
      if (relevantRows.contains(i)) {
        int[] e = this.getRow(i);
        for (int j = 0; j < e[0]; j++) {
          relevantRows.add(e[2 + j * 2]);
        }
      }
    }
    Map<Integer, Integer> skipMap = new HashMap<>();
    // strip no ops
    List<Model> trainingSources = this.trainingData.getTrainingSources();
    Function<Integer, Model> getResultModel = i -> {
      if (i < trainingSources.size()) {
        return trainingSources.get(i);
      }
      return this.results[i].getResultModel();
    };

    for (int i : relevantRows) {
      if (i >= this.getNumberOfInputs()) { //ignore readers
        int[] row = this.getRow(i);
        int arity = row[0];
        for (int j = 0; j < arity; j++) {
          int input = row[2+j*2];
          if (skipMap.containsKey(input)) {
            input = skipMap.get(input);
          }
          if (getResultModel.apply(input).isIsomorphicWith(getResultModel.apply(i))) {
            // no op
            skipMap.put(i, input);
            break;
          }
        }
      }
    }
    Genotype compacted;
    if (shrink) {
      compacted = new Genotype(relevantRows.size()+1-skipMap.size());
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

  Set<Resource> getSmell() {
    Set<Resource> smell = new HashSet<>();
    for (int i = getNumberOfInputs(); i <= bestResultRow; i++) {
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

}