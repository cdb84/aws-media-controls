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

    public DirectoryTreeNode<T> findValue(T needle){
        if (this.value != null && this.value.equals(needle)){
            return this;
        }
        else{
            for(DirectoryTreeNode<T> child : children){
                return child.findValue(needle);
            }
            return null;
        }
    }

    public String toString(){
        return this.value.toString();
    }
}