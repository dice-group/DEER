package org.aksw.deer.execution;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */

public class ExecutionPipeline implements UnaryOperator<Model> {

  private CompletableFuture<Model> trigger;
  private CompletableFuture<Model> result;
  private Consumer<Model> callBack;

  public ExecutionPipeline(CompletableFuture<Model> trigger, CompletableFuture<Model> result) {
    this.trigger = trigger;
    this.result = result;
    this.callBack = null;
    result.thenAcceptAsync(this::callBack);
  }

  public void setCallback(Consumer<Model> cb) {
    this.callBack = cb;
  }

  private void callBack(Model m) {
    if (this.callBack != null) {
      this.callBack.accept(m);
    } else {
      System.out.println("No callback provided: leaf encountered!");
    }
  }

  @Override
  public Model apply(Model model) {
    trigger.complete(model);
    return result.join();
  }
}