package org.aksw.deer.execution;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.util.IEnrichmentFunction;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionPipeline implements UnaryOperator<Model> {

  private Deque<Enrichment> fnStack;
  private Consumer<Model> callBack;
  private ModelWriter writeFirst;

  private static class Enrichment {
    private IEnrichmentFunction fn;
    private Consumer<Model> writer;


    private Enrichment(IEnrichmentFunction fn, Consumer<Model> writer) {
      this.fn = fn;
      this.writer = writer;
    }

    public Consumer<Model> getWriter() {
      return writer;
    }

    public IEnrichmentFunction getFn() {
      return fn;
    }

    public CompletableFuture<Model> appendToPipeline(CompletableFuture<Model> fn) {
      CompletableFuture<Model> cfn = fn.thenApplyAsync(this.fn);
      if (writer != null) {
        cfn.thenAcceptAsync(writer);
      }
      return cfn;
    }
  }

  public ExecutionPipeline(ModelWriter writer) {
    this();
    this.writeFirst = writer;
  }

  public ExecutionPipeline() {
    this.fnStack = new ArrayDeque<>();
    this.callBack = null;
  }

  public ExecutionPipeline chain(IEnrichmentFunction fn) {
    return chain(fn, null);
  }

  public ExecutionPipeline chain(IEnrichmentFunction fn, Consumer<Model> writer) {
    this.fnStack.addLast(new Enrichment(fn, writer));
    return this;
  }

  public Enrichment unchain() {
    return this.fnStack.pollLast();
  }

  @Override
  public Model apply(Model model) {
    CompletableFuture<Model> trigger = new CompletableFuture<>();
    CompletableFuture<Model> cfn = buildComposedFunction(trigger);
    trigger.complete(model);
    return cfn.join();
  }

  private CompletableFuture<Model> buildComposedFunction(CompletableFuture<Model> trigger) {
    CompletableFuture<Model> cfn = trigger.thenApplyAsync((m)->m);
    if (writeFirst != null) {
      cfn.thenAcceptAsync(writeFirst);
    }
    for (Enrichment enrichment : fnStack) {
      cfn = enrichment.appendToPipeline(cfn);
    }
    if (callBack != null) {
      cfn.thenAcceptAsync(callBack);
    }
    return cfn;
  }

  public void setCallback(Consumer<Model> consumer) {
    this.callBack = consumer;
  }

}
