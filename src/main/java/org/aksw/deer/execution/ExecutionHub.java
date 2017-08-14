package org.aksw.deer.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;
import org.aksw.deer.util.IEnrichmentOperator;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionHub {

  private List<ExecutionPipeline> inPipes;
  private List<ExecutionPipeline> outPipes;
  private List<Model> inModels;
  private List<Model> outModels;
  private IEnrichmentOperator operator;
  private int launchLatch;

  public ExecutionHub(IEnrichmentOperator operator) {
    this.operator = operator;
    this.inPipes = new ArrayList<>();
    this.outPipes = new ArrayList<>();
    this.inModels = new ArrayList<>();
    this.outModels = new ArrayList<>();
  }

  public void addInPipe(ExecutionPipeline in) {
    inPipes.add(in);
  }

  public void addOutPipe(ExecutionPipeline in) {
    outPipes.add(in);
  }

  public void glue() {
    this.launchLatch = inPipes.size();
    for (int i = 0; i < inPipes.size(); i++) {
      int finalI = i;
      inPipes.get(i).setCallback(m -> this.consume(finalI, m));
      inModels.add(null);
    }
  }

  private synchronized void consume(int index, Model model) {
    inModels.set(index, model);
    System.out.println("Pipe gives model to hub!");
    if (--launchLatch == 0) {
      System.out.println("Hub executes!");
      execute();
    }
  }

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
    for (CompletableFuture<Model> cf : lst) {
      cf.join();
    }
  }

}
