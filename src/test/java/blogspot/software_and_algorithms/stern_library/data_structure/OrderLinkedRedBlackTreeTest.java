package blogspot.software_and_algorithms.stern_library.data_structure;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/* Copyright (C) 2012 Kevin L. Stern.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Test class for OrderLinkedRedBlackTree.
 * 
 * @author Kevin L. Stern
 */
public class OrderLinkedRedBlackTreeTest {
  @Test
  public void testNull() {
    OrderLinkedRedBlackTree<Integer> tree = new OrderLinkedRedBlackTree<Integer>();
    try {
      tree.insert(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    try {
      tree.getPredecessor(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    try {
      tree.getSuccessor(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    Assert.assertNull(tree.delete(null));
    Assert.assertNull(tree.getNode(null));
    Assert.assertFalse(tree.contains(null));

    tree.insert(0);
    try {
      tree.insert(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    try {
      tree.getPredecessor(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    try {
      tree.getSuccessor(null);
      Assert.fail();
    } catch (NullPointerException e) {

    }
    Assert.assertNull(tree.delete(null));
    Assert.assertNull(tree.getNode(null));
    Assert.assertFalse(tree.contains(null));
  }

  @Test
  public void testPredecessor() {
    List<Integer> master = new ArrayList<Integer>();
    OrderLinkedRedBlackTree<Integer> tree = new OrderLinkedRedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
      master.add(j);
    }
    while (!master.isEmpty()) {
      for (int j = 1; j < master.size(); j++) {
        Assert.assertEquals(master.get(j - 1),
                            tree.getPredecessor(tree.getNode(master.get(j)))
                                .getValue());
      }
      Assert.assertNull(tree.getPredecessor(tree.getNode(master.get(0))));
      int index = master.size() >> 1;
      tree.delete(master.get(index));
      master.remove(index);
    }
  }

  @Test
  public void testSuccessor() {
    List<Integer> master = new ArrayList<Integer>();
    OrderLinkedRedBlackTree<Integer> tree = new OrderLinkedRedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
      master.add(j);
    }
    while (!master.isEmpty()) {
      for (int j = 0; j < master.size() - 1; j++) {
        Assert.assertEquals(master.get(j + 1),
                            tree.getSuccessor(tree.getNode(master.get(j)))
                                .getValue());
      }
      Assert
          .assertNull(tree.getSuccessor(tree.getNode(master.get(master.size() - 1))));
      int index = master.size() >> 1;
      tree.delete(master.get(index));
      master.remove(index);
    }
  }
}
