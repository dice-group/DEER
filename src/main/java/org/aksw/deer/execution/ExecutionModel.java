package org.aksw.deer.execution;

import java.util.concurrent.CompletableFuture;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionModel {

  private CompletableFuture<Void> trigger;

  public ExecutionModel() {
    this.trigger = new CompletableFuture<>();
  }

  public void execute() {
    trigger.complete(null);
  }

  public void addStartPipe(ExecutionPipeline pipe, Model model) {
    trigger.thenApplyAsync($ -> model).thenApplyAsync(pipe);
  }
}
