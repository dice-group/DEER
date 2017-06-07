package org.aksw.deer.execution;

import java.util.concurrent.CompletableFuture;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionModel {

  private CompletableFuture<Model> trigger;

  public ExecutionModel() {
    this.trigger = new CompletableFuture<>();
  }

  public void execute() {
    trigger.complete(null);
  }

  public void addStartPipe(ExecutionPipeline pipe, Model model) {
    trigger.thenApply((ignored)->model).thenApplyAsync(pipe);
  }
}
