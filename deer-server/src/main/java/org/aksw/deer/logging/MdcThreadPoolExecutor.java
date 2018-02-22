package org.aksw.deer.logging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A SLF4J MDC-compatible {@link ForkJoinPool}.
 * <p>
 * In general, MDC is used to store diagnostic information (e.g. a user's session id) in per-thread variables, to facilitate
 * logging. However, although MDC data is passed to thread children, this doesn't work when threads are reused in a
 * thread pool. This is a drop-in replacement for {@link ForkJoinPool} sets MDC data before each task appropriately.
 * <p>
 */
public class MdcThreadPoolExecutor extends ForkJoinPool {

  final private boolean useFixedContext;
  final private Map<String, String> fixedContext;

  /**
   * Pool where task threads take MDC from the submitting thread.
   */
  public static MdcThreadPoolExecutor newWithInheritedMdc(int parallelism) {
    return new MdcThreadPoolExecutor(null, parallelism);
  }

  /**
   * Pool where task threads take fixed MDC from the thread that creates the pool.
   */
  public static MdcThreadPoolExecutor newWithCurrentMdc(int parallelism) {
    return new MdcThreadPoolExecutor(MDC.getCopyOfContextMap(), parallelism);
  }

  /**
   * Pool where task threads always have a specified, fixed MDC.
   */
  public static MdcThreadPoolExecutor newWithFixedMdc(Map<String, String> fixedContext, int parallelism) {
    return new MdcThreadPoolExecutor(fixedContext, parallelism);
  }

  private MdcThreadPoolExecutor(Map<String, String> fixedContext, int parallelism) {
    super(parallelism);
    this.fixedContext = fixedContext;
    useFixedContext = (fixedContext != null);
  }

  /*
   * ------------------------------------------------------------------------------------------
   * All ExecutionService methods will have MDC injected or throw UnsupportedOperationException
   * ------------------------------------------------------------------------------------------
   */

  @Override
  public void execute(Runnable command) {
    super.execute(wrap(command, getContextForTask()));
  }

  @NotNull
  public <T> ForkJoinTask<T> submit(Callable<T> task) {
    return super.submit(wrap(task, getContextForTask()));
  }

  @NotNull
  public <T> ForkJoinTask<T> submit(Runnable task, T result) {
    return super.submit(wrap(task, getContextForTask()), result);
  }

  @NotNull
  public ForkJoinTask<?> submit(Runnable task) {
    return super.submit(wrap(task, getContextForTask()));
  }

  @NotNull
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                       long timeout, TimeUnit unit)
    throws InterruptedException {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                         long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  private static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      if (context == null) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        runnable.run();
      } finally {
        if (previous == null) {
          MDC.clear();
        } else {
          MDC.setContextMap(previous);
        }
      }
    };
  }

  private static <V> Callable<V> wrap(final Callable<V> callable, final Map<String, String> context) {
    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      if (context == null) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      V call;
      try {
        call = callable.call();
      } finally {
        if (previous == null) {
          MDC.clear();
        } else {
          MDC.setContextMap(previous);
        }
      }
      return call;
    };
  }

  private Map<String, String> getContextForTask() {
    return useFixedContext ? fixedContext : MDC.getCopyOfContextMap();
  }

}