/**
 * 
 */
package org.aksw.geolift.workflow.specslearner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * @author sherif
 *
 * @param <T>
 */
public class Tree<T> {

	private Set<Tree<T>> children = null;//new ArrayList<Tree<T>>();
	private Tree<T> parent;
	private T value;

	Tree(Tree<T> parent, T value, Set<Tree<T>> childrenlist) {
		this.parent = parent;
		this.value = value;
		if (childrenlist != null) {
			for (Tree<T> child : childrenlist) {
				children.add(new Tree<T>(this, child.value, child.children));
			}
		}
	}

	Tree(T value) {
		this.parent = null;
		this.value = value;
		children = null;
	}
	/**
	 * 
	 *@author sherif
	 */
	public Tree() {
		children = null;
		parent   = null;
		value    = null;
	}

	public void addChild(Tree<T> child){
		if(children == null){
			children = new HashSet<Tree<T>>();
		}
		children.add(child);
		child.parent = this;
	}

	public void removeChild(Tree<T> child){
		children.remove(child);
	}

	public Tree<T> getParent() {
		return parent;
	}

	public Set<Tree<T>> getchildren() {
		return children;
	}

	public T getValue() {
		return value;
	}
	
	private String levelPrefix = "";
	
	void print(Tree<T> root){
		if(root == null){
			return;
		}
		System.out.println(levelPrefix + "└── " + root.value);
		if(root.children != null){
			levelPrefix = "\t" + levelPrefix;
			for(Tree<T> child: root.children){
				print(child);
			}
			levelPrefix = levelPrefix.substring(1);
		}
	}



}
