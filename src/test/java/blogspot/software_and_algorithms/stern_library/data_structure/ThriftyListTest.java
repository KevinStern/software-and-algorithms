package blogspot.software_and_algorithms.stern_library.data_structure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

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
 * Test case for ThriftyList.
 * 
 * @author Kevin L. Stern
 */
public class ThriftyListTest {
  @Test
  public void testAddAll() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    list.addAll(0, Collections.<Integer> emptyList());
    list.addAll(Arrays.asList(10, 11, 12, 13, 14));
    list.addAll(0, Arrays.asList(0, 1, 2, 3, 4));
    list.addAll(5, Arrays.asList(5, 6, 7, 8, 9));
    list.addAll(0, Collections.<Integer> emptyList());
    list.addAll(5, Collections.<Integer> emptyList());
    list.addAll(Collections.<Integer> emptyList());

    Assertions.assertEquals(15, list.size());
    for (int j = 0; j < 15; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
  }

  @Test
  public void testAddFirstThenRemoveFirst() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();

    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.addFirst(j);
    }

    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertEquals(Integer.valueOf(j), list.removeFirst());
    }

    Assertions.assertEquals(0, list.size());
  }

  @Test
  public void testAddLastThenRemoveLast() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.addLast(j);
    }

    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertEquals(Integer.valueOf(j), list.removeLast());
    }

    Assertions.assertEquals(0, list.size());
  }

  @Test
  public void testClone() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    ThriftyList<Integer> cloneList = (ThriftyList<Integer>) list.clone();

    Assertions.assertEquals(testCount, cloneList.size());
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), cloneList.get(j));
    }
  }

  @Test
  public void testContains() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertTrue(list.contains(j));
    }
    Assertions.assertFalse(list
        .contains("This String is certainly not in the list"));

    Integer[] allValues = new Integer[testCount];
    for (int j = 0; j < testCount; j++) {
      allValues[j] = j;
    }
    Assertions.assertTrue(list.containsAll(Arrays.asList(allValues)));
    allValues[0] = -1;
    Assertions.assertFalse(list.containsAll(Arrays.asList(allValues)));
  }

  @Test
  public void testDescendingIterator() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    Iterator<Integer> emptyRi = list.descendingIterator();
    Assertions.assertFalse(emptyRi.hasNext());
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      emptyRi.next();
    });
    Assertions.assertThrows(IllegalStateException.class, () -> {
      emptyRi.remove();
    });

    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }

    Iterator<Integer> ri1 = list.descendingIterator();
    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertTrue(ri1.hasNext());
      Assertions.assertEquals(Integer.valueOf(j), ri1.next());
    }

    Iterator<Integer> ri2 = list.descendingIterator();
    Assertions.assertThrows(IllegalStateException.class, () -> {
      ri2.remove();
    });
    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertTrue(ri2.hasNext());
      Assertions.assertEquals(Integer.valueOf(j), ri2.next());
      ri2.remove();
    }
    Assertions.assertFalse(ri2.hasNext());
    Assertions.assertEquals(0, list.size());
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      ri2.next();
    });
  }

  @Test
  public void testGet() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
    Assertions.assertEquals(Integer.valueOf(0), list.getFirst());
    Assertions.assertEquals(Integer.valueOf(list.get(list.size() - 1)),
                        list.getLast());
  }

  @Test
  public void testIndexOfAndLastIndexOf() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(j, list.indexOf(list.get(j)));
      Assertions.assertEquals(j, list.lastIndexOf(list.get(j)));
    }

    Assertions.assertEquals(-1, list
        .indexOf("This String is certainly not in the list"));
    Assertions.assertEquals(-1, list
        .lastIndexOf("This String is certainly not in the list"));

    list.add(0);
    Assertions.assertEquals(0, list.indexOf(0));
    Assertions.assertEquals(list.size() - 1, list.lastIndexOf(0));
  }

  @Test
  public void testInsertToBack() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.add(j, j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
  }

  @Test
  public void testInsertToFront() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    for (int j = testCount; j >= 0; j--) {
      list.add(0, j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
  }

  @Test
  public void testInsertToMiddle() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    list.add(0);
    list.add(testCount - 2);
    list.add(testCount - 1);
    for (int j = 1; j < testCount - 2; j++) {
      list.add(j, j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
  }

  @Test
  public void testIterator() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();

    ListIterator<Integer> emptyIter = list.listIterator();
    Assertions.assertThrows(NoSuchElementException.class, () -> {
      emptyIter.next();
    });
    Assertions.assertThrows(IllegalStateException.class, () -> {
      emptyIter.remove();
    });

    int testCount = 100;
    ListIterator<Integer> i = list.listIterator();
    for (int j = 0; j < testCount; j++) {
      i.add(j);
    }

    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertEquals(j, i.previousIndex());
      Assertions.assertEquals(Integer.valueOf(j), i.previous());
    }

    for (int j = 0; j < testCount; j++) {
      Assertions.assertTrue(i.hasNext());
      Integer o = list.get(j);
      Assertions.assertEquals(j, i.nextIndex());
      Assertions.assertEquals(o, i.next());
      i.remove();
      Assertions.assertEquals(testCount - 1, list.size());
      i.add(o);
      Assertions.assertEquals(testCount, list.size());
      Assertions.assertEquals(o, i.previous());
      i.next();
    }

    int arbitraryIndex = testCount / 2;
    Integer arbitraryValue = -1;
    i = list.listIterator(arbitraryIndex);
    i.next();
    i.set(arbitraryValue);
    for (int j = 0; j < testCount; j++) {
      if (j == arbitraryIndex) {
        Assertions.assertEquals(arbitraryValue, list.get(j));
        continue;
      }
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }

    i = list.listIterator(list.size());
    for (int j = list.size() - 1; i.hasPrevious(); j--) {
      Integer o = list.get(j);
      Assertions.assertEquals(j, i.previousIndex());
      Assertions.assertEquals(o, i.previous());
      i.remove();
      Assertions.assertEquals(testCount - 1, list.size());
      i.add(o);
      Assertions.assertEquals(testCount, list.size());
      Assertions.assertEquals(o, i.previous());
    }
  }

  @Test
  public void testPeek() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    Assertions.assertNull(list.peek());
    Assertions.assertNull(list.peekFirst());
    Assertions.assertNull(list.peekLast());
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    Assertions.assertEquals(list.getFirst(), list.peek());
    Assertions.assertEquals(list.getFirst(), list.peekFirst());
    Assertions.assertEquals(list.getLast(), list.peekLast());
    Assertions.assertEquals(testCount, list.size());
  }

  @Test
  public void testPoll() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    Assertions.assertNull(list.poll());
    Assertions.assertNull(list.pollFirst());
    Assertions.assertNull(list.pollLast());
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    Assertions.assertEquals(list.getFirst(), list.poll());
    Assertions.assertEquals(list.getFirst(), list.pollFirst());
    Assertions.assertEquals(list.getLast(), list.pollLast());
    Assertions.assertEquals(testCount - 3, list.size());
  }

  @Test
  public void testRemoveBack() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    for (int j = testCount - 1; j >= 0; j--) {
      Assertions.assertEquals(Integer.valueOf(j), list.remove(j));
    }
  }

  @Test
  public void testRemoveFirstAndLastOccurrence() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    list.add(0);
    list.removeFirstOccurrence(0);
    Assertions.assertNotSame(Integer.valueOf(0), list.get(0));
    Assertions.assertEquals(Integer.valueOf(0), list.peekLast());
    list.add(0, 0);
    list.removeLastOccurrence(0);
    Assertions.assertNotSame(Integer.valueOf(0), list.peekLast());
    Assertions.assertEquals(Integer.valueOf(0), list.peekFirst());
  }

  @Test
  public void testRemoveFront() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 100;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.remove(0));
    }
  }

  @Test
  public void testSerialize() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(out);
      try {
        os.writeObject(list);
      } finally {
        os.close();
      }
      ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(
          out.toByteArray()));
      try {
        list = (ThriftyList<Integer>) is.readObject();
      } finally {
        is.close();
      }
    } catch (Exception e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      Assertions.fail(writer.toString());
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }

    list = new ThriftyList<Integer>();
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(out);
      try {
        os.writeObject(list);
      } finally {
        os.close();
      }
      ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(
          out.toByteArray()));
      try {
        list = (ThriftyList<Integer>) is.readObject();
      } finally {
        is.close();
      }
    } catch (Exception e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      Assertions.fail(writer.toString());
    }
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void testSet() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    Integer dummyValue = -1;
    for (int j = 0; j < testCount; j++) {
      list.add(dummyValue);
    }
    for (int j = 0; j < testCount; j++) {
      list.set(j, Integer.valueOf(j));
    }
    for (int j = 0; j < testCount; j++) {
      Assertions.assertEquals(Integer.valueOf(j), list.get(j));
    }
  }

  @Test
  public void testSizeAndClear() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    int testCount = 10;
    for (int j = 0; j < testCount; j++) {
      list.add(j);
    }
    Assertions.assertEquals(testCount, list.size());
    list.clear();
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void testReversed() {
    ThriftyList<Integer> list = new ThriftyList<Integer>();
    ThriftyList<Integer> r = list.reversed();
    Assertions.assertTrue(r.isEmpty());
    Assertions.assertSame(list, r.reversed());

    for (int j = 0; j < 5; j++) {
      list.add(j);
    }
    Assertions.assertEquals(5, r.size());
    Assertions.assertEquals(Integer.valueOf(4), r.getFirst());
    Assertions.assertEquals(Integer.valueOf(0), r.getLast());
    Assertions.assertEquals(Integer.valueOf(4), r.get(0));
    Assertions.assertEquals(Integer.valueOf(0), r.get(4));

    r.addFirst(10);
    Assertions.assertEquals(Integer.valueOf(10), list.getLast());
    Assertions.assertEquals(Integer.valueOf(10), r.removeFirst());
    Assertions.assertEquals(Integer.valueOf(4), list.getLast());

    r.set(0, 40);
    Assertions.assertEquals(Integer.valueOf(40), list.get(4));

    Iterator<Integer> iter = r.iterator();
    Assertions.assertEquals(Integer.valueOf(40), iter.next());
    Assertions.assertEquals(Integer.valueOf(3), iter.next());

    r.clear();
    Assertions.assertTrue(list.isEmpty());
  }
}
