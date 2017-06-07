package org.aksw.deer.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.aksw.deer.util.IOperator;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionHub {

  private Collection<ExecutionPipeline> inPipes;
  private Collection<ExecutionPipeline> outPipes;
  private List<Model> inModels;
  private List<Model> outModels;
  private IOperator operator;

  public ExecutionHub(IOperator operator) {
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
    for (ExecutionPipeline in : inPipes) {
      in.setCallback(this::consume);
    }
  }

  private synchronized void consume(Model m) {
    inModels.add(m);
    if (inModels.size() == inPipes.size()) {
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
    CompletableFuture<Model> trigger = new CompletableFuture<>();
    CompletableFuture<Model> lst = new CompletableFuture<>();
    Iterator<ExecutionPipeline> pipeIt = outPipes.iterator();
    for (Model outModel : outModels) {
      ExecutionPipeline outPipe = pipeIt.next();
      lst = trigger.thenApplyAsync((m) -> outModel).thenApplyAsync(outPipe);
    }
    trigger.complete(null);
    lst.join();
  }
}
