package client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDirectoryNode{
    @Test
    public void testConstructor(){
        DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
        assertEquals(node.value, null);
        node = new DirectoryTreeNode<String>("hello world!");
        assertEquals(node.value, "hello world!");
    }
}