package org.aksw.deer.execution;

import org.aksw.deer.util.CompletableFutureFactory;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
  private List<CompletableFuture<Model>> pipes = new ArrayList<>();

  /**
   * Default Constructor
   */
  ExecutionModel() {
    this.trigger = CompletableFutureFactory.get();
  }

  /**
   * Execute this {@code ExecutionModel}
   */
  public void execute() {
    CompletableFuture<Model> result = trigger.thenApply($ -> null);
    for (CompletableFuture<Model> pipe : pipes) {
      result = result.thenCombine(pipe, (a, b) -> {
        System.err.println("finished a pipe!");
        return b;
      });
    }
    trigger.complete(null);
    result.join();
    if (result.isCompletedExceptionally()) {
      try {
        result.get();
      } catch (Exception e) {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  /**
   * Add a {@code ExecutionPipeline} to this {@code ExecutionModel}
   *
   * @param pipe  an {@code ExecutionPipeline} to be triggered by this {@code ExecutionModel}
   * @param modelSupplier  a reader that supplies the initial {@code Model} for the {@code pipe}
   */
  void addPipeline(ExecutionPipeline pipe, Supplier<Model> modelSupplier) {
    pipes.add(trigger.thenApplyAsync($ -> modelSupplier.get()).thenCompose(pipe));
  }
}
