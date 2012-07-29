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

import junit.framework.Assert;

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

		Assert.assertEquals(15, list.size());
		for (int j = 0; j < 15; j++) {
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
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
			Assert.assertEquals(Integer.valueOf(j), list.removeFirst());
		}

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testAddLastThenRemoveLast() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 100;
		for (int j = 0; j < testCount; j++) {
			list.addLast(j);
		}

		for (int j = testCount - 1; j >= 0; j--) {
			Assert.assertEquals(Integer.valueOf(j), list.removeLast());
		}

		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testClone() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 10;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		ThriftyList<Integer> cloneList = (ThriftyList<Integer>) list.clone();

		Assert.assertEquals(testCount, cloneList.size());
		for (int j = 0; j < testCount; j++) {
			Assert.assertEquals(Integer.valueOf(j), cloneList.get(j));
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
			Assert.assertTrue(list.contains(j));
		}
		Assert.assertFalse(list
				.contains("This String is certainly not in the list"));

		Integer[] allValues = new Integer[testCount];
		for (int j = 0; j < testCount; j++) {
			allValues[j] = j;
		}
		Assert.assertTrue(list.containsAll(Arrays.asList(allValues)));
		allValues[0] = -1;
		Assert.assertFalse(list.containsAll(Arrays.asList(allValues)));
	}

	@Test
	public void testDescendingIterator() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		Iterator<Integer> ri = list.descendingIterator();
		Assert.assertFalse(ri.hasNext());
		try {
			ri.next();
			Assert.fail();
		} catch (NoSuchElementException e) {

		}
		try {
			ri.remove();
			Assert.fail();
		} catch (IllegalStateException e) {

		}

		int testCount = 100;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}

		ri = list.descendingIterator();
		for (int j = testCount - 1; j >= 0; j--) {
			Assert.assertTrue(ri.hasNext());
			Assert.assertEquals(Integer.valueOf(j), ri.next());
		}

		ri = list.descendingIterator();
		try {
			ri.remove();
			Assert.fail();
		} catch (IllegalStateException e) {

		}
		for (int j = testCount - 1; j >= 0; j--) {
			Assert.assertTrue(ri.hasNext());
			Assert.assertEquals(Integer.valueOf(j), ri.next());
			ri.remove();
		}
		Assert.assertFalse(ri.hasNext());
		Assert.assertEquals(0, list.size());
		try {
			ri.next();
			Assert.fail();
		} catch (NoSuchElementException e) {

		}
	}

	@Test
	public void testGet() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 10;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		for (int j = 0; j < testCount; j++) {
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
		}
		Assert.assertEquals(Integer.valueOf(0), list.getFirst());
		Assert.assertEquals(Integer.valueOf(list.get(list.size() - 1)),
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
			Assert.assertEquals(j, list.indexOf(list.get(j)));
			Assert.assertEquals(j, list.lastIndexOf(list.get(j)));
		}

		Assert.assertEquals(-1,
				list.indexOf("This String is certainly not in the list"));
		Assert.assertEquals(-1,
				list.lastIndexOf("This String is certainly not in the list"));

		list.add(0);
		Assert.assertEquals(0, list.indexOf(0));
		Assert.assertEquals(list.size() - 1, list.lastIndexOf(0));
	}

	@Test
	public void testInsertToBack() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 100;
		for (int j = 0; j < testCount; j++) {
			list.add(j, j);
		}
		for (int j = 0; j < testCount; j++) {
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
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
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
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
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
		}
	}

	@Test
	public void testIterator() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();

		ListIterator<Integer> i = list.listIterator();
		try {
			i.next();
			Assert.fail();
		} catch (NoSuchElementException e) {

		}
		try {
			i.remove();
			Assert.fail();
		} catch (IllegalStateException e) {

		}

		int testCount = 100;
		i = list.listIterator();
		for (int j = 0; j < testCount; j++) {
			i.add(j);
		}

		for (int j = testCount - 1; j >= 0; j--) {
			Assert.assertEquals(j, i.previousIndex());
			Assert.assertEquals(Integer.valueOf(j), i.previous());
		}

		for (int j = 0; j < testCount; j++) {
			Assert.assertTrue(i.hasNext());
			Integer o = list.get(j);
			Assert.assertEquals(j, i.nextIndex());
			Assert.assertEquals(o, i.next());
			i.remove();
			Assert.assertEquals(testCount - 1, list.size());
			i.add(o);
			Assert.assertEquals(testCount, list.size());
			Assert.assertEquals(o, i.previous());
			i.next();
		}

		int arbitraryIndex = testCount / 2;
		Integer arbitraryValue = -1;
		i = list.listIterator(arbitraryIndex);
		i.next();
		i.set(arbitraryValue);
		for (int j = 0; j < testCount; j++) {
			if (j == arbitraryIndex) {
				Assert.assertEquals(arbitraryValue, list.get(j));
				continue;
			}
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
		}

		i = list.listIterator(list.size());
		for (int j = list.size() - 1; i.hasPrevious(); j--) {
			Integer o = list.get(j);
			Assert.assertEquals(j, i.previousIndex());
			Assert.assertEquals(o, i.previous());
			i.remove();
			Assert.assertEquals(testCount - 1, list.size());
			i.add(o);
			Assert.assertEquals(testCount, list.size());
			Assert.assertEquals(o, i.previous());
		}
	}

	@Test
	public void testPeek() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		Assert.assertNull(list.peek());
		Assert.assertNull(list.peekFirst());
		Assert.assertNull(list.peekLast());
		int testCount = 10;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		Assert.assertEquals(list.getFirst(), list.peek());
		Assert.assertEquals(list.getFirst(), list.peekFirst());
		Assert.assertEquals(list.getLast(), list.peekLast());
		Assert.assertEquals(testCount, list.size());
	}

	@Test
	public void testPoll() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		Assert.assertNull(list.poll());
		Assert.assertNull(list.pollFirst());
		Assert.assertNull(list.pollLast());
		int testCount = 10;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		Assert.assertEquals(list.getFirst(), list.poll());
		Assert.assertEquals(list.getFirst(), list.pollFirst());
		Assert.assertEquals(list.getLast(), list.pollLast());
		Assert.assertEquals(testCount - 3, list.size());
	}

	@Test
	public void testRemoveBack() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 100;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		for (int j = testCount - 1; j >= 0; j--) {
			Assert.assertEquals(Integer.valueOf(j), list.remove(j));
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
		Assert.assertNotSame(Integer.valueOf(0), list.get(0));
		Assert.assertEquals(Integer.valueOf(0), list.peekLast());
		list.add(0, 0);
		list.removeLastOccurrence(0);
		Assert.assertNotSame(Integer.valueOf(0), list.peekLast());
		Assert.assertEquals(Integer.valueOf(0), list.peekFirst());
	}

	@Test
	public void testRemoveFront() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 100;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		for (int j = 0; j < testCount; j++) {
			Assert.assertEquals(Integer.valueOf(j), list.remove(0));
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
			ObjectInputStream is = new ObjectInputStream(
					new ByteArrayInputStream(out.toByteArray()));
			try {
				list = (ThriftyList<Integer>) is.readObject();
			} finally {
				is.close();
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			Assert.fail(writer.toString());
		}
		for (int j = 0; j < testCount; j++) {
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
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
			ObjectInputStream is = new ObjectInputStream(
					new ByteArrayInputStream(out.toByteArray()));
			try {
				list = (ThriftyList<Integer>) is.readObject();
			} finally {
				is.close();
			}
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			Assert.fail(writer.toString());
		}
		Assert.assertTrue(list.isEmpty());
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
			Assert.assertEquals(Integer.valueOf(j), list.get(j));
		}
	}

	@Test
	public void testSizeAndClear() {
		ThriftyList<Integer> list = new ThriftyList<Integer>();
		int testCount = 10;
		for (int j = 0; j < testCount; j++) {
			list.add(j);
		}
		Assert.assertEquals(testCount, list.size());
		list.clear();
		Assert.assertTrue(list.isEmpty());
	}
}
