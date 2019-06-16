/**
 *
 */
package org.aksw.deer.learning.old;

import org.aksw.deer.util.Tree;

import java.util.List;
import java.util.Set;

/**
 * @author sherif
 */
public class RefinementTree extends Tree<RefinementNodeOld> {

  public static final double CHILDREN_MULTIPLIER = 1;

  /**
   * @author sherif
   */
  public RefinementTree() {
    super();
  }

  /**
   * @author sherif
   */
  public RefinementTree(RefinementNodeOld value) {
    super(value);
  }

  /**
   * @author sherif
   */
  public RefinementTree(Tree<RefinementNodeOld> parent, RefinementNodeOld value,
    List<Tree<RefinementNodeOld>> childrenlist) {
    super(parent, value, childrenlist);
  }

  @SuppressWarnings("unused")
  private void setFitness(Tree<RefinementNodeOld> root, double fitness) {
    long rootChildrenCount = root.size() - 1;
    root.getValue().fitness += fitness + CHILDREN_MULTIPLIER * rootChildrenCount;
    root = root.getParent();
    while (root != null) {
      root.getValue().fitness += CHILDREN_MULTIPLIER * rootChildrenCount;
      root = root.getParent();
    }

  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#getLeaves()
   */
  @Override
  public Set<Tree<RefinementNodeOld>> getLeaves() {
    return super.getLeaves();
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#addChild(org.aksw.geolift.execution.specslearner.Tree)
   */
  @Override
  public void addChild(Tree<RefinementNodeOld> child) {
    super.addChild(child);
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#removeChild(org.aksw.geolift.execution.specslearner.Tree)
   */
  @Override
  public void removeChild(Tree<RefinementNodeOld> child) {
    super.removeChild(child);
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#getParent()
   */
  @Override
  public Tree<RefinementNodeOld> getParent() {
    return super.getParent();
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#getchildren()
   */
  @Override
  public List<Tree<RefinementNodeOld>> getchildren() {
    return super.getchildren();
  }

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#getValue()
   */
  @Override
  public RefinementNodeOld getValue() {
    return super.getValue();
  }

//	/* (non-Javadoc)
//	 * @see org.aksw.geolift.execution.specslearner.Tree#print(org.aksw.geolift.execution.specslearner.Tree)
//	 */
//	@Override
//	public void print(Tree<RefinementNode> root) {
//		super.print(root);
//	}

  /* (non-Javadoc)
   * @see org.aksw.geolift.execution.specslearner.Tree#size()
   */
  @Override
  public long size() {
    return super.size();
  }

}
