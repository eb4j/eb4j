package io.github.eb4j.io;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by miurahr on 16/06/11.
 */
public class HuffmanNodeTest {
    private HuffmanNode root;
    private HuffmanNode nodeB;

    @Test(groups={"node"})
    void testMakeNode() {
        long value = 1;
        int freq = 2;
        int type = HuffmanNode.LEAF_32;
        HuffmanNode node = new HuffmanNode(value, freq, type);
        assertNotNull(node);
    }

    @Test(groups={"node"})
    void testGetType() {
        long value = 1;
        int freq = 2;
        int type = HuffmanNode.LEAF_32;
        HuffmanNode node = new HuffmanNode(value, freq, type);
        assertEquals(node.getLeafType(), type);
    }

    @Test(groups={"node"})
    void testGetValue() {
        long value = 1;
        int freq = 2;
        int type = HuffmanNode.LEAF_32;
        HuffmanNode node = new HuffmanNode(value, freq, type);
        assertEquals(node.getValue(), value);
    }

    @Test(groups={"node"})
    void testIsLeaf() {
         long value = 1;
        int freq = 2;
        int type = HuffmanNode.LEAF_32;
        HuffmanNode node = new HuffmanNode(value, freq, type);
        assertTrue(node.isLeaf());
    }

    @Test(groups={"tree"})
    void testMakeTree() {
        ArrayList<HuffmanNode> list = new ArrayList<>(10);
        list.add(new HuffmanNode('A', 2, HuffmanNode.LEAF_32));
        nodeB = new HuffmanNode('B', 5, HuffmanNode.LEAF_32);
        list.add(nodeB);
        list.add(new HuffmanNode('C', 3, HuffmanNode.LEAF_32));
        list.add(new HuffmanNode('D', 1, HuffmanNode.LEAF_32));
        list.add(new HuffmanNode('E', 1, HuffmanNode.LEAF_32));
        list.add(new HuffmanNode(256, 1, HuffmanNode.LEAF_EOF));
        root = HuffmanNode.makeTree(list);
        assertEquals(root.getFrequency(), 13);
   }

    @Test(groups={"tree"}, dependsOnMethods={"testMakeTree"})
    void testLeft() {
        HuffmanNode left = root.getLeft();
        assertTrue(left.isLeaf());
        assertEquals(left.getValue(), 'B');
        assertEquals(left.getFrequency(), 5);
        assertTrue(left.equals(nodeB));
    }

    @Test(groups={"tree"}, dependsOnMethods={"testMakeTree"})
    void testRight() {
        HuffmanNode right = root.getRight();
        assertEquals(right.getFrequency(), 8);
        assertFalse(right.isLeaf());
    }
}
