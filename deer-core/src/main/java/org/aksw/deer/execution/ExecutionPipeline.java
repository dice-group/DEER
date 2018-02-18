package org.aksw.deer.execution;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@code ExecutionPipeline} encapsulates a linear sequence of {@code EnrichmentOperator}s
 */
class ExecutionPipeline implements UnaryOperator<Model> {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionPipeline.class);

  private CompletableFuture<Model> trigger;
  private CompletableFuture<Model> result;
  private Consumer<Model> callBack;

  /**
   * Construct an {@code ExecutionPipeline}
   *
   * @param trigger  the trigger used to fire execution
   * @param result  the {@code CompletableFuture} representing the result of this
   *                {@code ExecutionPipeline}
   */
  ExecutionPipeline(CompletableFuture<Model> trigger, CompletableFuture<Model> result) {
    this.trigger = trigger;
    this.result = result;
    this.callBack = null;
    result.thenAcceptAsync(this::callBack);
  }

  /**
   * Set the callback method reference.
   * The referenced method is called with the result of this {@code ExecutionPipeline} upon its
   * completion.
   * Has no effect, if called after this {@code ExecutionPipeline}s execution finished.
   *
   * @param cb the callback method reference, must satisfy the {@code Consumer<Model>} functional
   *           interface
   */
  void setCallback(Consumer<Model> cb) {
    this.callBack = cb;
  }

  /**
   * Wrapper method around callback. Allows chaining of callback at instantiation although the
   * actual callback function is only supplied later on.
   *
   * @param m  resulting model from this {@code ExecutionPipeline}s execution
   */
  private void callBack(Model m) {
    if (this.callBack != null) {
      this.callBack.accept(m);
    } else {
      logger.info("No callback provided: leaf encountered!");
    }
  }

  /**
   *
   *
   * @param model  input model to this {@code ExecutionPipeline}
   * @return  resulting Model
   */
  @Override
  public Model apply(Model model) {
    trigger.complete(model);
    return result.join();
  }
}