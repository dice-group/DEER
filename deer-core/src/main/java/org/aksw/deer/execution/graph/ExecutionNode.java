package org.aksw.deer.execution.graph;

import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 *
 *
 *
 */
public interface ExecutionNode {

  class DegreeBounds {

    private final int minIn;
    private final int maxIn;
    private final int minOut;
    private final int maxOut;

    public DegreeBounds(int minIn, int maxIn, int minOut, int maxOut) {
      this.minIn = minIn;
      this.maxIn = maxIn;
      this.minOut = minOut;
      this.maxOut = maxOut;
    }

    public boolean satisfiedBy(int in, int out) {
      boolean inBounds;
      inBounds  = in >= minIn();
      inBounds &= in <= maxIn();
      inBounds &= out >= minOut();
      // we only need to check for maxOut if it is greater than 1 as for 1 we apply implicit cloning.
      if (maxOut() > 1) {
        inBounds &= out <= maxOut();
      }
      return inBounds;
    }

    public int minIn() {
      return minIn;
    }

    public int maxIn() {
      return maxIn;
    }

    public int minOut() {
      return minOut;
    }

    public int maxOut() {
      return maxOut;
    }

  }

  default DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,1,0,1);
  }

  int getInDegree();

  int getOutDegree();

  void init(int inDegree, int outDegree);

  Model apply(Model model);

  List<Model> apply(List<Model> models);

  /**
   * Get initialization status
   * @return  {@code true}, if initialized; {@code false}, else.
   */
  boolean isInitialized();

}
