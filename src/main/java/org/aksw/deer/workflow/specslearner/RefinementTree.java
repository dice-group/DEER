/**
 * 
 */
package org.aksw.deer.workflow.specslearner;

import java.util.List;
import java.util.Set;

import org.aksw.deer.helper.datastructure.Tree;

/**
 * @author sherif
 *
 */
public class RefinementTree extends Tree<RefinementNodeOld> {
	public static final double CHILDREN_MULTIPLIER = 1;
	
	private void setFitness(Tree<RefinementNodeOld> root, double fitness){
		long rootChildrenCount = root.size() - 1;
		root.getValue().fitness += fitness + CHILDREN_MULTIPLIER * rootChildrenCount;
		root = root.getParent();
		while(root != null){
			root.getValue().fitness += CHILDREN_MULTIPLIER * rootChildrenCount;
			root = root.getParent();
		}
		
	}

	/**
	 * 
	 *@author sherif
	 */
	public RefinementTree() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 *@author sherif
	 */
	public RefinementTree(RefinementNodeOld value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param value
	 * @param childrenlist
	 *@author sherif
	 */
	public RefinementTree(Tree<RefinementNodeOld> parent, RefinementNodeOld value,
			List<Tree<RefinementNodeOld>> childrenlist) {
		super(parent, value, childrenlist);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getLeaves()
	 */
	@Override
	public Set<Tree<RefinementNodeOld>> getLeaves() {
		// TODO Auto-generated method stub
		return super.getLeaves();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#addChild(org.aksw.geolift.workflow.specslearner.Tree)
	 */
	@Override
	public void addChild(Tree<RefinementNodeOld> child) {
		// TODO Auto-generated method stub
		super.addChild(child);
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#removeChild(org.aksw.geolift.workflow.specslearner.Tree)
	 */
	@Override
	public void removeChild(Tree<RefinementNodeOld> child) {
		// TODO Auto-generated method stub
		super.removeChild(child);
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getParent()
	 */
	@Override
	public Tree<RefinementNodeOld> getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getchildren()
	 */
	@Override
	public List<Tree<RefinementNodeOld>> getchildren() {
		// TODO Auto-generated method stub
		return super.getchildren();
	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#getValue()
	 */
	@Override
	public RefinementNodeOld getValue() {
		// TODO Auto-generated method stub
		return super.getValue();
	}


//	/* (non-Javadoc)
//	 * @see org.aksw.geolift.workflow.specslearner.Tree#print(org.aksw.geolift.workflow.specslearner.Tree)
//	 */
//	@Override
//	public void print(Tree<RefinementNode> root) {
//		// TODO Auto-generated method stub
//		super.print(root);
//	}

	/* (non-Javadoc)
	 * @see org.aksw.geolift.workflow.specslearner.Tree#size()
	 */
	@Override
	public long size() {
		// TODO Auto-generated method stub
		return super.size();
	}
	
}
