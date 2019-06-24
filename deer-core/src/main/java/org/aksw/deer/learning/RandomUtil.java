package org.aksw.deer.learning;

import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.Random64;

/**
 */
public class RandomUtil {

  private static final Random64 rng;

  static {
    rng = new MT19937_64Random.ThreadSafe();
  }

  public static long get(long min, long max) {
    return rng.nextLong(min, max);
  }

  public static long get(long max) {
    return rng.nextLong(max);
  }

}