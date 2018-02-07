package org.aksw.deer.execution;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.aksw.deer.enrichment.EnrichmentOperator;
import org.apache.jena.rdf.model.Model;

/**
 */
public class ExecutionPipelineBuilder {

  private Deque<EnrichmentContainer> fnStack;
  private Consumer<Model> writeFirst;

  public ExecutionPipelineBuilder() {
    this.fnStack = new ArrayDeque<>();
    this.writeFirst = null;
  }

  public ExecutionPipelineBuilder writeFirstUsing(Consumer<Model> writer) {
    this.writeFirst = writer;
    return this;
  }

  public ExecutionPipelineBuilder chain(EnrichmentOperator fn) {
    return chain(fn, null);
  }

  public ExecutionPipelineBuilder chain(EnrichmentOperator fn, Consumer<Model> writer) {
    this.fnStack.addLast(new EnrichmentContainer(fn, writer));
    return this;
  }

  public EnrichmentContainer unchain() {
    return this.fnStack.pollLast();
  }

  public ExecutionPipeline build() {
    CompletableFuture<Model> trigger = new CompletableFuture<>();
    CompletableFuture<Model> cfn = trigger.thenApplyAsync((m)->m);
    if (writeFirst != null) {
      cfn.thenAcceptAsync(writeFirst);
    }
    for (EnrichmentContainer enrichmentContainer : fnStack) {
      cfn = cfn.thenApplyAsync(enrichmentContainer.getFn());
      if (enrichmentContainer.getWriter() != null) {
        cfn.thenAcceptAsync(enrichmentContainer.getWriter());
      }
    }
    return new ExecutionPipeline(trigger, cfn);
  }


  private static class EnrichmentContainer {
    private EnrichmentOperator fn;
    private Consumer<Model> writer;

    private EnrichmentContainer(EnrichmentOperator fn, Consumer<Model> writer) {
      this.fn = fn;
      this.writer = writer;
    }

    public Consumer<Model> getWriter() {
      return writer;
    }

    public EnrichmentOperator getFn() {
      return fn;
    }
  }

}