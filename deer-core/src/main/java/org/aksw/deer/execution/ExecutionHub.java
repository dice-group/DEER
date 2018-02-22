package org.aksw.deer.execution;

import org.aksw.deer.enrichment.EnrichmentOperator;
import org.aksw.deer.util.CompletableFutureFactory;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A non-linear node in the execution graph, ties together at least three linear nodes.
 */
class ExecutionHub {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionHub.class);

  private List<ExecutionPipeline> outPipes;
  private List<Model> inModels = new ArrayList<>();
  private List<Model> outModels = new ArrayList<>();
  private EnrichmentOperator operator;
  private int launchLatch;
  private CompletableFuture<Void> trigger = CompletableFutureFactory.get();
  private CompletableFuture<Model> completion = CompletableFutureFactory.getCompleted(null);

  /**
   * Constructor.
   *
   * @param operator {@code EnrichmentOperator} with at least two in or outputs.
   * @param inPipes  List of {@code ExecutionPipeline}s floating into this {@code ExecutionHub}.
   * @param outPipes  List of {@code ExecutionPipeline}s floating from this {@code ExecutionHub}.
   */
  ExecutionHub(EnrichmentOperator operator, List<ExecutionPipeline> inPipes, List<ExecutionPipeline> outPipes) {
    this.operator = operator;
    this.outPipes = outPipes;
    this.launchLatch = inPipes.size();
    for (int i = 0; i < inPipes.size(); i++) {
      int j = i;
      inPipes.get(i).setCallback(m -> this.consume(j, m));
      inModels.add(null);
    }
    boolean first = true;
    CompletableFuture<Model> x;
    for (int i = 0; i < outPipes.size(); i++) {
      final int j = i;
      if (first) {
        first = false;
        x = trigger.thenApply($ -> outModels.get(j)).thenCompose(outPipes.get(i));
        completion = completion.thenCombine(x, (a, b) -> b);
      } else {
        x = trigger.thenApplyAsync($ -> outModels.get(j)).thenCompose(outPipes.get(i));
        completion = completion.thenCombine(x, (a, b) -> b);
      }
    }
  }

  /**
   * Consume a model and place it at index {@code i} in the {@code inModels}.
   *
   * @param i  index the consumed model should be assigned
   * @param model  model to be consumed
   */
  private synchronized CompletableFuture<Model> consume(int i, Model model) {
    inModels.set(i, model);
    logger.info("Pipe gives model to hub!");
    if (--launchLatch == 0) {
      logger.info("Hub executes!");
      execute();
      return completion;
    }
    return CompletableFutureFactory.getCompleted(null);
  }


  /**
   * Execute this {@code ExecutionHub}, passing all the input models to the
   * encapsulated operator and in turn passing that operators output models as input to the
   * outgoing {@code ExecutionPipeline}s.
   */
  private void execute() {
    this.outModels = operator.apply(inModels);
    if (outModels.size() != outPipes.size()) {
      throw new RuntimeException("Unexpected arity of generated output models from operator "
        + operator.getClass().getSimpleName() + "(Expected: " + outPipes.size() + ", Actual: "
        + outModels.size() + ")");
    }
    trigger.complete(null);
  }

}
