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
	private Status status;


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
		status = Status.LEAF;
	}
	/**
	 * 
	 *@author sherif
	 */
	public Tree() {
		children = null;
		parent   = null;
		value    = null;
		status = Status.LEAF;
	}
	
	public Set<Tree<T>> getLeaves(){
		Set<Tree<T>> leaves = new HashSet<Tree<T>>();
		for(Tree<T> child : this.children){
			if(child.children == null){
				leaves.add(child) ;
			}else{
				Set<Tree<T>> childrenLeaves = child.getLeaves();
				for(Tree<T> l : childrenLeaves){
					leaves.add(l);
				}
			}
		}
		return leaves;
	}

	public void addChild(Tree<T> child){
		if(children == null){
			children = new HashSet<Tree<T>>();
		}
		children.add(child);
		child.parent = this;
		child.status = Status.LEAF;
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
	
	public Status getStatus(){
		return status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public void print(Tree<T> root){
		print(root, "");
	}
	
	private void print(Tree<T> root, String prefix){
		if(root == null){
			return;
		}
		System.out.println(prefix + "└── " + ((root.parent == null) ? "ROOT(⟂)" : root.value));
		System.out.print((root.status == Status.DEAD)? "DEAD" : "");
		if(root.children != null){
			prefix = "\t" + prefix;
			for(Tree<T> child: root.children){
				print(child, prefix);
			}
			prefix = prefix.substring(1);
		}
	}
	
	public long size(){
		long size = 0;
		if(children == null || children.size() == 0){
			return 1;
		}
		for(Tree<T> child : children){
			size += child.size();
		}
		return 1 + size;
	}





}
