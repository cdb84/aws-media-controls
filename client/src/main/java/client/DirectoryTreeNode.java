package client;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

public class DirectoryTreeNode<T> {
	public T value;
	private List<DirectoryTreeNode<T>> children;
	private DirectoryTreeNode<T> parent;

	DirectoryTreeNode() {
		this.value = null;
		this.children = new ArrayList<DirectoryTreeNode<T>>();
	}

	DirectoryTreeNode(T value) {
		this.value = value;
		this.children = new ArrayList<DirectoryTreeNode<T>>();
	}

	DirectoryTreeNode(T value, DirectoryTreeNode<T> parent){
		this(value);
		this.parent = parent;
	}

	public void addChild(DirectoryTreeNode<T> child) {
		children.add(child);
	}

	public void addPath(Stack<T> pathItems) {
		if (pathItems.size() <= 0) {
			return;
		}
		// first we should look at the first item in the front of the stack and check
		// to see if any of our children have it
		T curItem = pathItems.pop();
		for (DirectoryTreeNode<T> child : children) {
			if (child.value != null && child.value.equals(curItem)) {
				child.addPath(pathItems);
				return;
			}
		}
		// otherwise, we need to add a bunch of new nodes on!
		DirectoryTreeNode<T> newChild = new DirectoryTreeNode<T>(curItem, this);
		this.children.add(newChild);
		newChild.addPath(pathItems);
	}

	public DirectoryTreeNode<T> findValue(T needle) {
		if (this.value != null && this.value.equals(needle)) {
			return this;
		} else {
			DirectoryTreeNode<T> found = null;
			for (DirectoryTreeNode<T> child : children) {
				if (found == null) {
					found = child.findValue(needle);
				} else {
					break;
				}
			}
			return found;
		}
	}

	public void printTree() {
		System.out.println(System.identityHashCode(this) + ": " + this.value);
		for (DirectoryTreeNode<T> child : children) {
			child.printTree();
		}
	}

	public ArrayList<T> toArrayList() {
		ArrayList<T> res = new ArrayList<T>();
		res.add(value);
		for (DirectoryTreeNode<T> child : children) {
			res.addAll(child.toArrayList());
		}
		return res;
	}

	public String toString() {
		return this.value.toString();
	}

	public List<DirectoryTreeNode<T>> getChildren() {
		return new ArrayList<DirectoryTreeNode<T>>(this.children);
	}

	public List<DirectoryTreeNode<T>> getParents(){
		List<DirectoryTreeNode<T>> parents = new ArrayList<DirectoryTreeNode<T>>();
		if (this.parent != null){
			parents.add(this.parent);
			parents.addAll(this.parent.getParents());
		}
		return parents;
	}

	public Stack<T> getPath(){
		Stack<T> res = new Stack<T>();
		res.push(this.value);
		if (this.parent != null){
			for (T upperValues : this.parent.getPath()){
				res.push(upperValues);
			}
		}
		return res;
	}
}