package org.aksw.deer.learning;

import io.jenetics.prngine.MT19937_64Random;
import io.jenetics.prngine.Random64;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class RandomUtil {

  @NotNull
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

  public static void main(String[] args) {
    for (int i = 0; i < 1000; i++) {
      System.out.println(get(9999));
    }
  }

}