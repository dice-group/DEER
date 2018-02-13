package org.aksw.deer.execution;

import java.util.concurrent.CompletableFuture;
import org.apache.jena.rdf.model.Model;

/**
 * An {@code ExecutionModel}, encapsulating the compiled {@code ExecutionGraph}.
 * <p>
 * An {@code ExecutionModel} consists of pairs of {@link ExecutionPipeline}s and their input
 * {@code Model}s.
 */
public class ExecutionModel {

  /**
   * A trigger to start the execution
   */
  private CompletableFuture<Void> trigger;

  /**
   * Default Constructor
   */
  ExecutionModel() {
    this.trigger = new CompletableFuture<>();
  }

  /**
   * Execute this {@code ExecutionModel}
   */
  public void execute() {
    trigger.complete(null);
  }

  /**
   * Add a {@code ExecutionPipeline} to this {@code ExecutionModel}
   *
   * @param pipe  an {@code ExecutionPipeline} to be triggered by this {@code ExecutionModel}
   * @param model  the {@code Model} to be fed into the given {@code pipe}
   */
  void addPipeline(ExecutionPipeline pipe, Model model) {
    trigger.thenApplyAsync($ -> model).thenApplyAsync(pipe);
  }
}
