package org.aksw.deer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 
 */
public class TreeX<T> {

  private List<TreeX<T>> children = null;//new ArrayList<Tree<T>>();

  private List<TreeX<T>> parents;
  private T value;
  private boolean isPrinted = false;


  /**
   * create tree node with many parents and many children
   *
   * 
   */
  public TreeX(List<TreeX<T>> parents, T value, List<TreeX<T>> childrenlist) {
    //		this.parents = parents;
    this.value = value;
    if (parents != null) {
      for (TreeX<T> parent : parents) {
        this.addParent(parent);
      }
    }
    if (childrenlist != null) {
      for (TreeX<T> child : childrenlist) {
        this.addChild(child);
      }
    }
  }

  /**
   * Copy constructor
   *
   * 
   */
  public TreeX(TreeX<T> root) {
    this(root.parents, root.value, root.children);
  }

  /**
   * create tree node with a single parent and many children
   *
   * 
   */
  public TreeX(TreeX<T> parent, T value, List<TreeX<T>> childrenlist) {
    if (parent != null) {
      this.addParent(parent);
    }
    this.value = value;
    if (childrenlist != null) {
      for (TreeX<T> child : childrenlist) {
        this.addChild(child);
      }
    }
  }

  /**
   * create tree node with many parents and a single child
   *
   * 
   */
  public TreeX(List<TreeX<T>> parents, T value, TreeX<T> child) {
    this.value = value;
    for (TreeX<T> parent : parents) {
      this.addParent(parent);
    }
    if (child != null) {
      this.addChild(child);
    }
  }

  /**
   * Create a tree node with the given value with a null parents and children
   *
   * 
   */
  public TreeX(T value) {
    this.parents = null;
    this.value = value;
    children = null;
  }

  /**
   * Create an empty tree node
   *
   * 
   */
  public TreeX() {
    children = null;
    parents = null;
    value = null;
  }

  public static void main(String args[]) {
//		TreeX<String> t = new TreeX<String>("root");
//		TreeX<String> c = new TreeX<String>(t,"clone",null);
//		TreeX<String> l = c.addChild(new TreeX<String>("left"));
//		TreeX<String> r = c.addChild(new TreeX<String>("right"));
//		ArrayList<TreeX<String>> p = new ArrayList<TreeX<String>>(Arrays.asList(l, r));
//		TreeX<String> m  = new TreeX<String>(p,"merge", (TreeX<String>)null);
//		TreeX<String> l1 = new TreeX<String>(m,"leaf1",null);
//		TreeX<String> l2 = new TreeX<String>(m,"leaf2",null);
//		TreeX<String> l3 = new TreeX<String>(m,"leaf3",null);
//
//		TreeX<String> c2 = new TreeX<String>(l1,"clone",null);
//		TreeX<String> c2l1 = c2.addChild(new TreeX<String>("left"));
//		TreeX<String> c2l2 = c2.addChild(new TreeX<String>("right"));
//		ArrayList<TreeX<String>> p2 = new ArrayList<TreeX<String>>(Arrays.asList(c2l1, c2l2));
//		TreeX<String> m2 = new TreeX<String>(p2,"merge", (TreeX<String>)null);
//
//		t.print();
//		t.print();
  }

  /**
   * @return the children
   */
  public List<TreeX<T>> getChildren() {
    return children;
  }

  /**
   * @param children
   */
  public void setChildren(List<TreeX<T>> children) {
    this.children = children;
  }

  /**
   * returns tree leaves
   *
   * 
   */
  public Set<TreeX<T>> getLeaves() {
    Set<TreeX<T>> leaves = new HashSet<TreeX<T>>();
    for (TreeX<T> child : this.children) {
      if (child.children == null) {
        leaves.add(child);
      } else {
        Set<TreeX<T>> childrenLeaves = child.getLeaves();
        for (TreeX<T> l : childrenLeaves) {
          leaves.add(l);
        }
      }
    }
    return leaves;
  }

  /**
   * Add child node to the current tree node
   *
   * 
   */
  public TreeX<T> addChild(TreeX<T> child) {
    if (children == null) {
      children = new ArrayList<TreeX<T>>();
    }
    if (child.parents == null) {
      child.parents = new ArrayList<TreeX<T>>();
    }
    children.add(child);
    child.parents.add(this);
    return child;
  }

  /**
   * Add parent node to the current tree node
   *
   * @return the added parent node
   * 
   */
  public TreeX<T> addParent(TreeX<T> parent) {
    if (parents == null) {
      parents = new ArrayList<TreeX<T>>();
    }
    parents.add(parent);
    if (parent.children == null) {
      parent.children = new ArrayList<TreeX<T>>();
    }
    if (!parent.children.contains(this)) {
      parent.children.add(this);
    }
    return parent;
  }

  /**
   * Remove Child
   *
   * 
   */
  public void removeChild(TreeX<T> child) {
    children.remove(child);
  }

  /**
   * Return the first parent
   *
   * 
   */
  public TreeX<T> getParent() {
    return parents.get(0);
  }

  /**
   * @param parent
   */
  public void setParent(TreeX<T> parent) {
    if (this.parents == null) {
      this.parents = new ArrayList<TreeX<T>>();
    }
    this.parents.set(0, parent);
    if (parent.children == null) {
      parent.children = new ArrayList<TreeX<T>>();
    }
    parent.children.add(this);
  }

  /**
   * Return all parents
   *
   * 
   */
  public List<TreeX<T>> getParents() {
    return parents;
  }

  /**
   * @param parents
   */
  public void setParents(List<TreeX<T>> parents) {
    this.parents = parents;
  }

  /**
   * @return a list of all children nodes of the current tree node
   * 
   */
  public List<TreeX<T>> getchildren() {
    return children;
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

  /**
   * @return the data value of the current tree node
   * 
   */
  public T getValue() {
    return value;
  }

  /**
   * Print current tree in console
   *
   * 
   */
  public void print() {
    unsetPrintFlag();
    print("", true);
  }

  private void unsetPrintFlag() {
    isPrinted = false;
    if (children != null) {
      for (TreeX<T> child : children) {
        child.unsetPrintFlag();
      }
    }
  }

  private void print(String prefix, boolean isTail) {
    if (!isPrinted) {
      Object value = (this.parents == null || this.parents.isEmpty()) ? "ROOT(⟂)" : this.value;
      boolean isMerge = false;
      String branch = "";
      if (isTail) {
        if (this.parents != null && this.parents.size() > 1) {
          branch = "╠══ ";
          isMerge = true;
        } else {
          branch = "└── ";
        }
      } else {
        branch = "├── ";
      }
      System.out.println(prefix + branch + value);
      isPrinted = true;
      if (children != null) {
        for (int i = 0; i < children.size() - 1; i++) {
          children.get(i).print(prefix + (isTail ? ((isMerge) ? "║   " : "    ") : "│   "), false);
        }
        if (children.size() > 0) {
          children.get(children.size() - 1)
            .print(prefix + (isTail ? ((isMerge) ? "║   " : "    ") : "│   "), true);
        }
      }
    }
  }

  /**
   * @return tree size
   * 
   */
  public long size() {
    long size = 0;
    if (children == null || children.size() == 0) {
      return 1;
    }
    for (TreeX<T> child : children) {
      size += child.size();
    }
    return 1 + size;
  }

  /**
   * @return tree depth
   * 
   */
  public long depth() {
    if (children == null || children.size() == 0) {
      return 1;
    }
    long maxDepth = 0;
    for (TreeX<T> child : children) {
      long d = child.depth();
      if (maxDepth < d) {
        maxDepth = d;
      }
    }
    return maxDepth + 1;
  }

  /**
   * @return current node level
   * 
   */
  public long level() {
    long level = 0;
    TreeX<T> t = this;
    while (t.parents != null) {
      level++;
      t = t.parents.get(0);
    }
    return level;
  }


}
