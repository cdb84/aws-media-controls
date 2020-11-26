package client;

import java.util.List;
import java.util.ArrayList;

public class DirectoryTreeNode<T>{
    public T value;
    private List<DirectoryTreeNode<T>> children;

    DirectoryTreeNode(){
        this.value = null;
        this.children = new ArrayList<DirectoryTreeNode<T>>();
    }
    DirectoryTreeNode(T value){
        this.value = value;
        this.children = new ArrayList<DirectoryTreeNode<T>>();
    }

    public void addChild(DirectoryTreeNode<T> child){
        children.add(child);
    }

    public DirectoryTreeNode<T> findValue(T value){
        if (this.value.toString() == value.toString()){
            return this;
        }
        else {
            for(DirectoryTreeNode<T> child : children){
                return child.findValue(value);
            }
        }
        return null;
    }
}