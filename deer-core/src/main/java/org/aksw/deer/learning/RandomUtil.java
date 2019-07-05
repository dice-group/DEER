package org.aksw.deer.learning;

import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.Random64;

/**
 */
public class RandomUtil {

  private static Random64 rng = new MT19937_64Random.ThreadSafe();
  private static final Object lock = new Object();

  public static int get(int min, int max) {
    return rng.nextInt(min, max);
  }

  public static int get(int max) {
    return rng.nextInt(max);
  }

  public static double get() {
    return rng.nextDouble();
  }

  // not thread safe, do only use in serial computations, i.e. when there are no other threads accessing
  // RandomUtil for the time of the execution of temporaryWithSeed
  public static void temporaryWithSeed(long seed, Runnable function) {
    Random64 tmp = rng;
    rng = new MT19937_64Random.ThreadSafe(seed);
    function.run();
    rng = tmp;
  }

}