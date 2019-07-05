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

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class Genotype extends ExecutionGraph<Model> {

  public static int SIZE = 30;

  protected TrainingData trainingData;

  protected short currentRow = 0;

  short bestResultRow = -1;

  private double bestFitness = -1;

  EvaluationResult[] results = null;


  protected Genotype(TrainingData trainingData) {
    super(SIZE);
    this.trainingData = trainingData;
    for (ParameterizedDeerExecutionNode reader : trainingData.getTrainingReaders()) {
      addRow(currentRow++, reader, new short[]{0,1});
    }
  }

  public Genotype(Genotype other) {
    super(other.getSize());
    this.trainingData = other.trainingData;
    for (int i = 0; i < entries.length; i++) {
      entries[i] = Arrays.copyOf(other.entries[i], other.entries[i].length);
      if (i >= getInputSize()) {
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
    copy.currentRow = this.currentRow;
    return copy;
  }

  ExecutionNode<Model> getRawNode(int i) {
    return ops.get(i);
  }

  public ExecutionNode<Model> getNode(int i) {
    return getWrappedNode(i);
  }

  private ExecutionNode<Model> getWrappedNode(int i) {
    if (i < trainingData.getTrainingSources().size()) {
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
    compiled.getCompletionStage().exceptionally(t -> {
      if (t instanceof ConcurrentModificationException) {
        System.out.println(this);
      }
      return null;
    });
    return result;
  }

  private EvaluationResult findAndSetBestEvaluationResult(FitnessFunction f) {
    for (short i = (short)trainingData.getTrainingSources().size(); i < results.length; i++) {
      double fitness = f.getFitness(results[i]);
      if (fitness > bestFitness) {
        bestFitness = fitness;
        bestResultRow  = i;
      }
    }
    return results[bestResultRow];
  }

  public CompletableFuture<EvaluationResult> getBestEvaluationResult(FitnessFunction f) {
    if (results == null) {
      results = new EvaluationResult[getSize()];
      return evaluate(f);
    }
    return ThreadlocalInheritingCompletableFuture.completedFuture(results[bestResultRow]);
  }

  public double getBestFitness() {
    return bestFitness;
  }

  short getInputSize() {
    return (short) trainingData.getTrainingSources().size();
  }
}
