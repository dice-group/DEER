package org.aksw.deer.util;

import java.util.concurrent.CompletableFuture;

/**
 *
 *
 *
 */
public interface CompletableFutureFactory {

  class DefaultFutureFactory implements CompletableFutureFactory {

    static final CompletableFutureFactory INSTANCE = new DefaultFutureFactory();

    private DefaultFutureFactory() { }

    @Override
    public <T> CompletableFuture<T> getInstance() {
      return new CompletableFuture<>();
    }

    @Override
    public <T> CompletableFuture<T> getCompletedInstance(T value) {
      return CompletableFuture.completedFuture(value);
    }
  }

  class FactoryMemory {

    private static CompletableFutureFactory factory = DefaultFutureFactory.INSTANCE;

  }

  static void setImplementation(CompletableFutureFactory factory) {
    FactoryMemory.factory = factory;
  }


  static <T> CompletableFuture<T> get() {
    return FactoryMemory.factory.getInstance();
  }

  static <T> CompletableFuture<T> getCompleted(T value) {
    return FactoryMemory.factory.getCompletedInstance(value);
  }

  <T> CompletableFuture<T> getInstance();

  <T> CompletableFuture<T> getCompletedInstance(T value);

}
