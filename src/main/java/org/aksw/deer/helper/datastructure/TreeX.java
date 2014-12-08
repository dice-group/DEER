/**
 * 
 */
package org.aksw.deer.helper.datastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * @author sherif
 *
 * @param <T>
 */
public class TreeX<T> {
	private List<TreeX<T>> children = null;//new ArrayList<Tree<T>>();

	private List<TreeX<T>> parents;
	private T value;


	public TreeX(List<TreeX<T>> parents, T value, List<TreeX<T>> childrenlist) {
		this.parents = parents;
		this.value = value;
		for(TreeX<T> parent : parents){
			if (parent.children == null) {
				parent.children = new ArrayList<TreeX<T>>();
			}
			parent.children.add(this);
		}
		if (childrenlist != null) {
			for (TreeX<T> child : childrenlist) {
				children.add(new TreeX<T>(this, child.value, child.children));
			}
		}
	}

	public TreeX(TreeX<T> parent, T value, List<TreeX<T>> childrenlist) {
		if (parents == null) {
			parents = new ArrayList<TreeX<T>>();
		}
		if(parent != null){
			this.parents.add(parent);
			if (parent.children == null) {
				parent.children = new ArrayList<TreeX<T>>();
			}
			parent.children.add(this);
		}
		this.value = value;
		if (childrenlist != null) {
			for (TreeX<T> child : childrenlist) {
				children.add(new TreeX<T>(this, child.value, child.children));
			}
		}
	}

	public TreeX(T value) {
		this.parents = null;
		this.value = value;
		children = null;
	}
	/**
	 * 
	 *@author sherif
	 */
	public TreeX() {
		children = null;
		parents   = null;
		value    = null;
	}

	public Set<TreeX<T>> getLeaves(){
		Set<TreeX<T>> leaves = new HashSet<TreeX<T>>();
		for(TreeX<T> child : this.children){
			if(child.children == null){
				leaves.add(child) ;
			}else{
				Set<TreeX<T>> childrenLeaves = child.getLeaves();
				for(TreeX<T> l : childrenLeaves){
					leaves.add(l);
				}
			}
		}
		return leaves;
	}

	public TreeX<T> addChild(TreeX<T> child){
		if(children == null){
			children = new ArrayList<TreeX<T>>();
		}
		if(child.parents == null){
			child.parents = new ArrayList<TreeX<T>>();
		}
		children.add(child);
		child.parents.add(this);
		return child;
	}

	public void removeChild(TreeX<T> child){
		children.remove(child);
	}

	public TreeX<T> getParent() {
		return parents.get(0);
	}

	public List<TreeX<T>> getParents() {
		return parents;
	}

	public List<TreeX<T>> getchildren() {
		return children;
	}

	public T getValue() {
		return value;
	}

	//	public void print(Tree<T> root){
	//		print(root, "");
	//	}
	//	
	//	private void print(Tree<T> root, String prefix){
	//		if(root == null){
	//			return;
	//		}//├── "└── "
	//		System.out.println(prefix + //		List<TreeX<Integer>> parents = new ArrayList<TreeX<Integer>>();
	//	parents.add(s1);
	//	parents.add(s2);
	//	TreeX<Integer> t1 = new TreeX<Integer>(parents,31,null);
	//	TreeX<Integer> f1 = new TreeX<Integer>(t1,41,null);
	//	TreeX<Integer> f2 = new TreeX<Integer>(t1,42,null);"├── " + ((root.parent == null) ? "ROOT(⟂)" : root.value));
	////		System.out.print((root.status == NodeStatus.DEAD)? "DEAD" : "");
	//		if(root.children != null){
	//			prefix = "│\t" + prefix;
	//			for(Tree<T> child: root.children){
	//				print(child, prefix);
	//			}
	//			prefix = prefix.substring(1);
	//		}
	//	}

	public void print() {
		print("", true);
	}

	Set<TreeX<Object>> PrintedNodes = new HashSet<TreeX<Object>>();

	private void print(String prefix, boolean isTail) {
		if(!PrintedNodes.contains(this)){
			Object value = (this.parents == null || this.parents.isEmpty()) ? "ROOT(⟂)" : this.value;
			boolean isMerge = false;
			String branch = "";
			if(isTail){
				if(this.parents != null &&  this.parents.size() > 1){
					branch = "╠══ ";
					isMerge = true;
				}else{
					branch = "└── ";
				}
			}else{
				branch = "├── ";
			}
			//			String branch = isTail ? ((this.parents != null &&  this.parents.size() > 1) ? "╠══ ": "└── ") : "├── ";
			System.out.println(prefix + branch + value);
			PrintedNodes.add((TreeX<Object>) this);
			if(children != null){
				for (int i = 0; i < children.size() - 1; i++) {
					//					if(children.get(i).parents.size() == 1){
					children.get(i).print(prefix + (isTail ? ((isMerge)? "║   " :"    ") : "│   "), false);
					//					}
				}
				if (children.size() > 0) {
					children.get(children.size() - 1).print(prefix + (isTail ? ((isMerge)? "║   " :"    ") : "│   "), true);
				}
			}
		}
		new HashSet<TreeX<Object>>();new HashSet<Object>();
	}



	public long size(){
		long size = 0;
		if(children == null || children.size() == 0){
			return 1;
		}
		for(TreeX<T> child : children){
			size += child.size();
		}
		return 1 + size;
	}

	public long depth(){
		if(children == null || children.size() == 0){
			return 1;
		}
		long maxDepth = 0;
		for(TreeX<T> child : children){
			long d = child.depth();
			if(maxDepth < d ){
				maxDepth = d;
			}
		}
		return maxDepth + 1;
	}

	public long level(){
		long level = 0;
		TreeX<T> t = this;
		while(t.parents != null){
			level++;
			t = t.parents.get(0);
		}
		return level;
	}

	public static void main(String args[]){
		TreeX<Integer> t = new TreeX<Integer>(1);
		TreeX<Integer> s1 = new TreeX<Integer>(t,21,null);
		TreeX<Integer> s2 = new TreeX<Integer>(t,22,null);
		TreeX<Integer> s3 = new TreeX<Integer>(t,23,null);
		//		TreeX<Integer> s4 = new TreeX<Integer>(t,24,null);
		//		TreeX<Integer> t1 = new TreeX<Integer>(s1,31,null);
		//		TreeX<Integer> t2 = new TreeX<Integer>(s1,32,null);
		List<TreeX<Integer>> parents = new ArrayList<TreeX<Integer>>();
		parents.add(s1);
		parents.add(s2);
		TreeX<Integer> t1 = new TreeX<Integer>(parents,31,null);
		TreeX<Integer> f1 = new TreeX<Integer>(t1,41,null);
		TreeX<Integer> f2 = new TreeX<Integer>(t1,42,null);
		List<TreeX<Integer>> parents2 = new ArrayList<TreeX<Integer>>();
		parents2.add(f1);
		parents2.add(f2);
		TreeX<Integer> f3 = new TreeX<Integer>(parents2,51,null);
		t.print();
	}



}
