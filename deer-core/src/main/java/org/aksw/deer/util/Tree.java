package org.aksw.deer.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 */
public class Tree<T> {

  @Nullable
  private List<Tree<T>> children = null;//new ArrayList<Tree<T>>();
  @Nullable
  private Tree<T> parent;
  @Nullable
  private T value;


  public Tree(Tree<T> parent, T value, @Nullable List<Tree<T>> childrenlist) {
    this.parent = parent;
    this.value = value;
    if (childrenlist != null) {
      for (Tree<T> child : childrenlist) {
        children.add(new Tree<T>(this, child.value, child.children));
      }
    }
  }

  public Tree(T value) {
    this.parent = null;
    this.value = value;
    children = null;
  }

  /**
   */
  public Tree() {
    children = null;
    parent = null;
    value = null;
  }

  public static void main(String args[]) {
    Tree<Integer> t = new Tree<Integer>(1);
    Tree<Integer> child = new Tree<Integer>(2);
    new Tree<Integer>(t, 2, null);
    t.addChild(child);
//		t.addChild(new TreeX<Integer>(4));
    t.print();
  }

  public Set<Tree<T>> getLeaves() {
    Set<Tree<T>> leaves = new HashSet<Tree<T>>();
    for (Tree<T> child : this.children) {
      if (child.children == null) {
        leaves.add(child);
      } else {
        Set<Tree<T>> childrenLeaves = child.getLeaves();
        for (Tree<T> l : childrenLeaves) {
          leaves.add(l);
        }
      }
    }
    return leaves;
  }

  public void addChild(@NotNull Tree<T> child) {
    if (children == null) {
      children = new ArrayList<Tree<T>>();
    }
    children.add(child);
    child.parent = this;
  }

  public void removeChild(Tree<T> child) {
    children.remove(child);
  }

  @Nullable
  public Tree<T> getParent() {
    return parent;
  }

  @Nullable
  public List<Tree<T>> getchildren() {
    return children;
  }

//	public void print(Tree<T> root){
//		print(root, "");
//	}
//	
//	private void print(Tree<T> root, String PREFIX){
//		if(root == null){
//			return;
//		}//├── "└── "
//		System.out.println(PREFIX + "├── " + ((root.parent == null) ? "ROOT(⟂)" : root.value));
////		System.out.print((root.status == NodeStatus.DEAD)? "DEAD" : "");
//		if(root.children != null){
//			PREFIX = "│\t" + PREFIX;
//			for(Tree<T> child: root.children){
//				print(child, PREFIX);
//			}
//			PREFIX = PREFIX.substring(1);
//		}
//	}

  @Nullable
  public T getValue() {
    return value;
  }

  public void print() {
    print("", true);
  }

  private void print(String prefix, boolean isTail) {
    System.out.println(
      prefix + (isTail ? "└── " : "├── ") + ((this.parent == null) ? "ROOT(⟂)" : this.value));
    if (children != null) {
      for (int i = 0; i < children.size() - 1; i++) {
        children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
      }
      if (children.size() > 0) {
        children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
      }
    }
  }

  public long size() {
    long size = 0;
    if (children == null || children.size() == 0) {
      return 1;
    }
    for (Tree<T> child : children) {
      size += child.size();
    }
    return 1 + size;
  }

  public long depth() {
    if (children == null || children.size() == 0) {
      return 1;
    }
    long maxDepth = 0;
    for (Tree<T> child : children) {
      long d = child.depth();
      if (maxDepth < d) {
        maxDepth = d;
      }
    }
    return maxDepth + 1;
  }

  public long level() {
    long level = 0;
    Tree<T> t = this;
    while (t.parent != null) {
      level++;
      t = t.parent;
    }
    return level;
  }


}
