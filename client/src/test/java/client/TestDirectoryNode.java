package client;

import org.junit.jupiter.api.Test;

import jdk.jfr.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class TestDirectoryNode{
    @Test
    public void testConstructor(){
        DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
        assertEquals(node.value, null);
        node = new DirectoryTreeNode<String>("hello world!");
        assertEquals(node.value, "hello world!");
    }

    @Test
    public void testAddChild(){
        String value = "22";
        DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
        DirectoryTreeNode<String> child = new DirectoryTreeNode<String>(value);
        node.addChild(child);
        DirectoryTreeNode<String> actual = node.findValue(value);
        assertNotNull(actual);
        assertSame(actual, child);
    }

    @Test
    public void testFind(){
        String value = "22";
        DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
        node.addChild(new DirectoryTreeNode<String>(value));
        DirectoryTreeNode<String> actual = node.findValue(value);
        assertNotNull(actual);
        assertEquals(value, actual.value);
    }

    @Test
    public void testDeeperFind(){
        String value = "22";
        String sndValue = "23";
        DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
        node.addChild(new DirectoryTreeNode<String>(value));
        DirectoryTreeNode<String> actual = node.findValue(value);
        actual.addChild(new DirectoryTreeNode<String>(sndValue));
        DirectoryTreeNode<String> found = node.findValue(sndValue);
        assertNotNull(found);
        assertEquals(sndValue, found.value);

    }
}