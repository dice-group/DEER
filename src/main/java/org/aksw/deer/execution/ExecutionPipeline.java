package org.aksw.deer.execution;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.aksw.deer.io.ModelWriter;
import org.aksw.deer.util.IEnrichmentOperator;
import org.apache.jena.rdf.model.Model;

/**
 * @author Kevin Dreßler
 */
public class ExecutionPipeline implements UnaryOperator<List<Model>> {

  private Deque<Enrichment> fnStack;
  private Consumer<List<Model>> callBack;
  private ModelWriter writeFirst;

  private static class Enrichment {
    private IEnrichmentOperator fn;
    private Consumer<Model> writer;

    private Enrichment(IEnrichmentOperator fn, Consumer<Model> writer) {
      this.fn = fn;
      this.writer = writer;
    }

    public Consumer<Model> getWriter() {
      return writer;
    }

    public IEnrichmentOperator getFn() {
      return fn;
    }

    public CompletableFuture<List<Model>> appendToPipeline(CompletableFuture<List<Model>> fn) {
      CompletableFuture<List<Model>> cfn = fn.thenApplyAsync(this.fn);
      if (writer != null) {
        cfn.thenApplyAsync((list->list.get(0))).thenAcceptAsync(writer);
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

  public ExecutionPipeline chain(IEnrichmentOperator fn) {
    return chain(fn, null);
  }

  public ExecutionPipeline chain(IEnrichmentOperator fn, Consumer<Model> writer) {
    this.fnStack.addLast(new Enrichment(fn, writer));
    return this;
  }

  public Enrichment unchain() {
    return this.fnStack.pollLast();
  }

  @Override
  public List<Model> apply(List<Model> model) {
    CompletableFuture<List<Model>> trigger = new CompletableFuture<>();
    CompletableFuture<List<Model>> cfn = buildComposedFunction(trigger);
    trigger.complete(model);
    return cfn.join();
  }

  private CompletableFuture<List<Model>> buildComposedFunction(CompletableFuture<List<Model>> trigger) {
    CompletableFuture<List<Model>> cfn = trigger.thenApplyAsync((m)->m);
    if (writeFirst != null) {
      cfn.thenApplyAsync((list->list.get(0))).thenAcceptAsync(writeFirst);
    }
    for (Enrichment enrichment : fnStack) {
      cfn = enrichment.appendToPipeline(cfn);
    }
    if (callBack != null) {
      cfn.thenAcceptAsync(callBack);
    }
    return cfn;
  }

  public void setCallback(Consumer<List<Model>> consumer) {
    this.callBack = consumer;
  }

}
