package client;

import org.junit.jupiter.api.Test;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDirectoryNode {
	@Test
	public void testToString() {
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>("hello world!");
		assertEquals("hello world!", node.toString());
	}

	@Test
	public void testConstructor() {
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		assertEquals(node.value, null);
		node = new DirectoryTreeNode<String>("hello world!");
		assertEquals("hello world!", node.value);
	}

	@Test
	public void testAddChild() {
		String value = "22";
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		DirectoryTreeNode<String> child = new DirectoryTreeNode<String>(value);
		node.addChild(child);
		List<DirectoryTreeNode<String>> children = node.getChildren();
		assertEquals(1, children.size());
		assertSame(child, children.get(0));
	}

	@Test
	public void testFind() {
		String value = "22";
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		node.addChild(new DirectoryTreeNode<String>(value));
		DirectoryTreeNode<String> actual;
		try {
			actual = node.findValue(value);
		} catch (ValueNotFoundError e) {
			fail();
			return;
		}
		assertEquals(value, actual.value);

	}

	@Test
	public void testDeeperFind() {
		String value = "22";
		String sndValue = "23";
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		node.addChild(new DirectoryTreeNode<String>(value));
		DirectoryTreeNode<String> actual;
		try {
			actual = node.findValue(value);
		} catch (ValueNotFoundError e) {
			fail();
			return;
		}
		actual.addChild(new DirectoryTreeNode<String>(sndValue));
		DirectoryTreeNode<String> found;
		try {
			found = node.findValue(sndValue);
		} catch (ValueNotFoundError e) {
			fail();
			return;
		}
		assertNotNull(found);
		assertEquals(sndValue, found.value);
	}

	@Test
	public void testNullFind() {
		String value = "22";
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>("olleh drowedrdl!");
		node.addChild(new DirectoryTreeNode<String>("hello world!"));
		node.addChild(new DirectoryTreeNode<String>("Also hello world!"));
		DirectoryTreeNode<String> actual;
		try {
			assertNotNull(node.findValue("hello world!"));
		} catch (ValueNotFoundError e) {
			fail();
		}
		try {
			actual = node.findValue(value);
			fail(); // should not get here.
		} catch (ValueNotFoundError e) {
			// this is good
		}
	}

	@Test
	public void testAddPath() {
		Stack<String> pathStck = new Stack<String>();
		String pathString = "etc/bin/urs/share/lol";

		String[] pathArray = pathString.split("/");
		for (int i = pathArray.length - 1; i >= 0; i--) {
			pathStck.push(pathArray[i]);
		}

		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		node.addPath(pathStck);

		// this is smoke testing, because it doesn't actually ensure they were
		// added in the right in order
		for (String item : pathArray) {
			DirectoryTreeNode<String> found;
			try {
				found = node.findValue(item);
			} catch (ValueNotFoundError e) {
				fail("Was not able to find " + item);
				found = null;
			}
		}

		ArrayList<String> treeRep = node.toArrayList();
		// off by one since the head node is nulled
		assertEquals(pathArray.length + 1, treeRep.size());
		for (int i = 0; i < pathArray.length; i++) {
			// skip our head node
			assertEquals(pathArray[i], treeRep.get(i + 1));
		}
	}

	@Test
	public void testAddSimilarPath() {
		Stack<String> pathStckA = new Stack<String>();
		Stack<String> pathStckB = new Stack<String>();
		String pathStringA = "etc/bin/urs/share/lol";
		String pathStringB = "etc/bin/urs/nothing/no";
		String[] pathArrayA = pathStringA.split("/");
		String[] pathArrayB = pathStringB.split("/");

		for (int i = pathArrayA.length - 1; i >= 0; i--) {
			pathStckA.push(pathArrayA[i]);
			pathStckB.push(pathArrayB[i]);
		}
		DirectoryTreeNode<String> node = new DirectoryTreeNode<String>();
		node.addPath(pathStckA);
		node.addPath(pathStckB);
		// node.printTree();
		// a quick smoke test to ensure we can find everything that
		// went into the tree

		DirectoryTreeNode<String> foundA;
		try {
			foundA = node.findValue("lol");
		} catch (ValueNotFoundError e) {
			// e.printStackTrace();
			foundA = null;
		}
		DirectoryTreeNode<String> foundB;
		try {
			foundB = node.findValue("no");
		} catch (ValueNotFoundError e) {
			// e.printStackTrace();
			foundB = null;
		}

		assertNotNull(foundA);
		assertNotNull(foundB);

		ArrayList<String> treeRep = node.toArrayList();

		// off by one because head is null
		assertEquals(8, treeRep.size());
	}
}