package org.aksw.deer.logging;

import org.aksw.deer.util.CompletableFutureFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * This custom future implementation enables inheritance of MDC.
 */
public class MdcCompletableFuture<T> extends CompletableFuture<T> {


  private static final int PARALLELISM;
  private static final String DEER_PARALLELISM_LEVEL = "deerParallelismLevel";

  static {
    int parallelism = Runtime.getRuntime().availableProcessors();
    if (System.getProperty(DEER_PARALLELISM_LEVEL) != null) {
      parallelism = Integer.parseInt(System.getProperty(DEER_PARALLELISM_LEVEL));
    }
    PARALLELISM = parallelism;
  }

  private static final Map<String,Executor> MDC_EXEC_MAP = new HashMap<>();

  private static final Executor DEFAULT_EXEC = MdcThreadPoolExecutor.newWithInheritedMdc(PARALLELISM);

  public static class Factory implements CompletableFutureFactory {

    public static final CompletableFutureFactory INSTANCE = new Factory();

    private Factory() {

    }

    @Override
    public <T> CompletableFuture<T> getInstance() {
      return new MdcCompletableFuture<T>();
    }

    @Override
    public <T> CompletableFuture<T> getCompletedInstance(T value) {
      return MdcCompletableFuture.completedFuture(value);
    }
  }


  /**
   * Returns a new CompletableFuture that is already completed with
   * the given value.
   *
   * @param value the value
   * @param <U> the type of the value
   * @return the completed CompletableFuture
   */
  public static <U> CompletableFuture<U> completedFuture(U value) {
    MdcCompletableFuture<U> x = new MdcCompletableFuture<U>();
    x.complete(value);
    return x;
  }

  @Override
  public Executor defaultExecutor() {
    String requestId = MDC.get("requestId");
    if (MDC.get("requestId") == null) {
      return DEFAULT_EXEC;
    }
    if (!MDC_EXEC_MAP.containsKey(requestId)) {
      MDC_EXEC_MAP.put(requestId, MdcThreadPoolExecutor.newWithInheritedMdc(PARALLELISM));
    }
    return MDC_EXEC_MAP.get(requestId);
  }

  public <U> CompletableFuture<U> newIncompleteFuture() {
    return new MdcCompletableFuture<U>();
  }

}