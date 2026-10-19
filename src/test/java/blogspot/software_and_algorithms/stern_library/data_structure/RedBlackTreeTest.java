package blogspot.software_and_algorithms.stern_library.data_structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
 * Test class for RedBlackTree.
 * 
 * @author Kevin L. Stern
 */
public class RedBlackTreeTest {
  private static <T> void equalsHelper(Collection<T> master,
                                       RedBlackTree<T> tree) {
    Assertions.assertEquals(master.size(), tree.getSize());
    Iterator<T> iMaster = master.iterator();
    Iterator<T> iTree = tree.iterator();
    while (iMaster.hasNext()) {
      Assertions.assertTrue(iTree.hasNext());
      Assertions.assertEquals(iMaster.next(), iTree.next());
    }
    Assertions.assertFalse(iTree.hasNext());
  }

  @Test
  public void testContains() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      Assertions.assertFalse(tree.contains(j));
      tree.insert(j);
      Assertions.assertTrue(tree.contains(j));
    }
  }

  @Test
  public void testDelete() {
    List<Integer> master = new ArrayList<Integer>();
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
      master.add(j);
    }
    while (!master.isEmpty()) {
      Integer val = master.get(master.size() >> 1);
      master.remove(val);
      tree.delete(val);
      equalsHelper(master, tree);
    }
  }

  @Test
  public void testInsert() {
    TreeSet<Integer> master = new TreeSet<Integer>();
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 99; j >= 0; j--) {
      tree.insert(j);
      master.add(j);
    }
    equalsHelper(master, tree);
  }

  @Test
  public void testIterator() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
    }
    Iterator<Integer> i = tree.iterator();
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      i.remove();
    });
    Assertions.assertTrue(i.hasNext());
    int test = 0;
    for (; i.hasNext();) {
      Integer val = i.next();
      Assertions.assertEquals(Integer.valueOf(test++), val);
      if ((val & 1) == 0) {
        i.remove();
      }
    }
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      i.next();
    });
    Iterator<Integer> i2 = tree.iterator();
    for (int j = 1; j < 100; j += 2) {
      Assertions.assertEquals(Integer.valueOf(j), i2.next());
      i2.remove();
    }
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      i2.remove();
    });
    Assertions.assertTrue(tree.isEmpty());
  }

  @Test
  public void testNull() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.insert(null);
    });
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.getPredecessor(null);
    });
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.getSuccessor(null);
    });
    Assertions.assertNull(tree.delete(null));
    Assertions.assertNull(tree.getNode(null));
    Assertions.assertFalse(tree.contains(null));

    tree.insert(0);
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.insert(null);
    });
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.getPredecessor(null);
    });
    Assertions.assertThrows(NullPointerException.class, () -> {
      tree.getSuccessor(null);
    });
    Assertions.assertNull(tree.delete(null));
    Assertions.assertNull(tree.getNode(null));
    Assertions.assertFalse(tree.contains(null));
  }

  @Test
  public void testPredecessor() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
    }
    for (int j = 1; j < 100; j++) {
      Assertions.assertEquals(Integer.valueOf(j - 1),
                          tree.getPredecessor(tree.getNode(j)).getValue());
    }
    Assertions.assertNull(tree.getPredecessor(tree.getNode(0)));
  }

  @Test
  public void testSuccessor() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    for (int j = 0; j < 100; j++) {
      tree.insert(j);
    }
    for (int j = 0; j < 99; j++) {
      Assertions.assertEquals(Integer.valueOf(j + 1),
                          tree.getSuccessor(tree.getNode(j)).getValue());
    }
    Assertions.assertNull(tree.getSuccessor(tree.getNode(99)));
  }

  @Test
  public void testToString() {
    RedBlackTree<Integer> tree = new RedBlackTree<Integer>();
    Assertions.assertEquals("{}", tree.toString());
    tree.insert(2);
    Assertions.assertEquals("{2}", tree.toString());
    tree.insert(3);
    Assertions.assertEquals("{2, 3}", tree.toString());
  }
}
