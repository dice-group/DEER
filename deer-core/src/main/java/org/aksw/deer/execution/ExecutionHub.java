package org.aksw.deer.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;
import org.aksw.deer.enrichment.EnrichmentOperator;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A non-linear node in the execution graph, ties together at least three linear nodes.
 */
class ExecutionHub {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionHub.class);

  private List<ExecutionPipeline> inPipes;
  private List<ExecutionPipeline> outPipes;
  private List<Model> inModels;
  private List<Model> outModels;
  private EnrichmentOperator operator;
  private int launchLatch;

  /**
   * Constructor.
   *
   * @param operator {@code EnrichmentOperator} with at least two in or outputs.
   * @param inPipes  List of {@code ExecutionPipeline}s floating into this {@code ExecutionHub}.
   * @param outPipes  List of {@code ExecutionPipeline}s floating from this {@code ExecutionHub}.
   */
  ExecutionHub(EnrichmentOperator operator, List<ExecutionPipeline> inPipes, List<ExecutionPipeline> outPipes) {
    this.operator = operator;
    this.inPipes = inPipes;
    this.outPipes = outPipes;
    this.inModels = new ArrayList<>();
    this.outModels = new ArrayList<>();
    this.launchLatch = inPipes.size();
    for (int i = 0; i < inPipes.size(); i++) {
      int finalI = i;
      inPipes.get(i).setCallback(m -> this.consume(finalI, m));
      inModels.add(null);
    }
  }

  /**
   * Consume a model and place it at index {@code i} in the {@code inModels}.
   *
   * @param i  index the consumed model should be assigned
   * @param model  model to be consumed
   */
  private synchronized void consume(int i, Model model) {
    inModels.set(i, model);
    logger.info("Pipe gives model to hub!");
    if (--launchLatch == 0) {
      logger.info("Hub executes!");
      execute();
    }
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
    CompletableFuture<Void> trigger = new CompletableFuture<>();
    List<CompletableFuture<Model>> lst = new ArrayList<>();
    ListIterator<ExecutionPipeline> pipeIt = outPipes.listIterator();
    for (Model outModel : outModels) {
      ExecutionPipeline outPipe = pipeIt.next();
      lst.add(trigger.thenApplyAsync($ -> outModel).thenApplyAsync(outPipe));
    }
    trigger.complete(null);
    //@todo: really necessary? if yes, need to extend?
//    for (CompletableFuture<Model> cf : lst) {
//      cf.join();
//    }
  }

}
