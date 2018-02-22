package org.aksw.deer.execution;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.aksw.deer.enrichment.EnrichmentOperator;
import org.aksw.deer.util.CompletableFutureFactory;
import org.apache.jena.rdf.model.Model;

/**
 */
class ExecutionPipelineBuilder {

  private Deque<EnrichmentContainer> fnStack = new ArrayDeque<>();
  private UnaryOperator<Model> writeFirst = null;

  ExecutionPipelineBuilder writeFirstUsing(Consumer<Model> writer) {
    this.writeFirst = wrapConsumer(writer);
    return this;
  }

  ExecutionPipelineBuilder chain(EnrichmentOperator fn) {
    return chain(fn, null);
  }

  ExecutionPipelineBuilder chain(EnrichmentOperator fn, Consumer<Model> writer) {
    this.fnStack.addLast(new EnrichmentContainer(fn, writer));
    return this;
  }

  EnrichmentContainer unchain() {
    return this.fnStack.pollLast();
  }

  ExecutionPipeline build() {
    CompletableFuture<Model> trigger = CompletableFutureFactory.get();
    CompletableFuture<Model> cfn = trigger;
    if (writeFirst != null) {
      cfn = cfn.thenApply(writeFirst);
    }
    for (EnrichmentContainer enrichmentContainer : fnStack) {
      cfn = cfn.thenApply(enrichmentContainer.getFn())
        .thenApply(enrichmentContainer.getWriter());
    }
    return new ExecutionPipeline(trigger, cfn);
  }

  private static UnaryOperator<Model> wrapConsumer(Consumer<Model> x) {
    if (x == null) {
      return UnaryOperator.identity();
    }
    return m -> {x.accept(m); return m;};
  }

  private static class EnrichmentContainer {
    private EnrichmentOperator fn;
    private UnaryOperator<Model> writer;

    EnrichmentContainer(EnrichmentOperator fn, Consumer<Model> writer) {
      this.fn = fn;
      this.writer = wrapConsumer(writer);
    }

    UnaryOperator<Model> getWriter() {
      return writer;
    }

    EnrichmentOperator getFn() {
      return fn;
    }

  }

}