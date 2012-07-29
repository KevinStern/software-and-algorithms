package blogspot.software_and_algorithms.stern_library.data_structure;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

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
 * A dynamically sized compact data structure implementing both the
 * <tt>Deque</tt> and <tt>List</tt> interfaces. Internally, this data structure
 * maintains multiple sublists each having size O(sqrt(this.size())), giving a
 * reasonable upper bound on the size of any one contiguous memory block
 * consumed by an instance. Furthermore, the resizing approach taken by this
 * data structure guarantees a memory overhead of O(sqrt(this.size())) instead
 * of the O(this.size()) overhead that is typical of resizable array
 * implementations (including ArrayList) while maintaining O(1) amortized time
 * add and remove operations; the data structure is <em>compact</em> in the
 * sense that this is theoretically optimal [ResizableArraysTR]. In addition,
 * this data structure achieves O(sqrt(this.size())) time middle
 * insertion/deletion by maintaining circular lists throughout.
 * <p>
 * 
 * @author Kevin L. Stern
 * 
 * @see ArrayList
 * @see ArrayDeque
 * @techreport{ResizableArraysTR, author = {Andrej Brodnik and Svante Carlsson
 *                                and Erik D. Demaine and J. Ian Munro and
 *                                Robert Sedgewick}, title = {Resizable Arrays
 *                                in Optimal Time and Space}, institution =
 *                                {Department of Computer Science, University of
 *                                Waterloo}, institutionurl =
 *                                {http://www.cs.uwaterloo.ca/}, number =
 *                                {CS-99-09}, numberurl =
 *                                {http://www.cs.uwaterloo
 *                                .ca/research/tr/1999/09/CS-99-09.pdf}, year =
 *                                {1999}}
 * @misc{Goodrich_tieredvectors:, author = {Michael T. Goodrich and John G.
 *                                Kloss and II}, title = {Tiered Vectors:
 *                                Efficient Dynamic Arrays for Rank-Based
 *                                Sequences}, year = {} }
 */
public class ThriftyList<T> extends AbstractList<T> implements List<T>,
		Deque<T>, Serializable, Cloneable {
	/**
	 * Helper function to copy all data from the source to the destination and
	 * return the destination.
	 * 
	 * @param source
	 *            the source list.
	 * @param destination
	 *            the destination list.
	 * @return the destination list.
	 */
	public static <T, E extends ListInternal<T>> E copyTo(E source,
			E destination) {
		assert destination.capacity() >= source.size();
		destination.addAll(source);
		return destination;
	}

	/**
	 * Helper function to merge two {@link ListInternal} instances into the
	 * target {@link ListInternal} instance. The target capacity should be equal
	 * to the sum of the capacities of its constituent lists.
	 * 
	 * @param l1
	 *            the first constituent list.
	 * @param l2
	 *            the second constituent list.
	 * @return the target {@link ListInternal} instance containing the data of
	 *         l1 followed by the data of l2.
	 */
	public static <T, E extends ListInternal<T>> E merge(E l1, E l2, E target) {
		assert target.capacity() == l1.capacity() + l2.capacity();
		target.addAll(l1);
		target.addAll(l2);
		return target;
	}

	/**
	 * Helper function to split the data of the source list between two
	 * destination lists. The sum of the individual capacities of the
	 * destination lists should be equal to the capacity of the source list.
	 * 
	 * @param alignRight
	 *            true for right alignment of the data in the src to the two
	 *            destinations, false for left alignment.
	 */
	public static <T> void split(ListInternal<T> src, ListInternal<T> dst1,
			ListInternal<T> dst2, boolean alignRight) {
		assert dst1.capacity() + dst2.capacity() == src.capacity();

		if (alignRight) {
			int splitIndex = src.size() - dst2.capacity();
			dst2.addSome(src, Math.max(0, splitIndex),
					Math.min(src.size(), dst2.capacity()));
			dst1.addSome(src, 0, splitIndex);
		} else {
			dst1.addSome(src, 0, Math.min(src.size(), dst1.capacity()));
			dst2.addSome(src, dst1.capacity(), src.size() - dst1.capacity());
		}
	}

	private FixedListInternal<CircularListInternal<T>> sublists;
	private int size;
	private int capacity;
	private int smallSublistCount;
	private int smallSublistSizeExp, largeSublistSizeExp;
	private int headSublistIndex, tailSublistIndex;
	private int freeCapacityHead;
	private int halveCapacityLimit, doubleCapacityLimit;

	/**
	 * Construct an empty instance of {@link ThriftyList} with the default
	 * capacity.
	 */
	public ThriftyList() {
		sublists = new FixedListInternal<CircularListInternal<T>>(4);
		sublists.addTail(new CircularListInternal<T>(2));
		sublists.addTail(new CircularListInternal<T>(4));
		sublists.addTail(new CircularListInternal<T>(4));

		capacity = 10;
		smallSublistSizeExp = 1;
		largeSublistSizeExp = 2;
		halveCapacityLimit = 4;
		doubleCapacityLimit = 16;
		smallSublistCount = 1;
		size = 0;
		headSublistIndex = tailSublistIndex = 1;
		freeCapacityHead = 6;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(int index, T item) {
		if (0 > index || index > size) {
			throw new IndexOutOfBoundsException();
		}

		int sublistIndex, sublistOffset;
		int projectedIndex = index + freeCapacityHead;
		int smallListCapacity = smallSublistCount << smallSublistSizeExp;
		if (projectedIndex < smallListCapacity) {
			sublistIndex = projectedIndex >>> smallSublistSizeExp;
			sublistOffset = sublistIndex == headSublistIndex ? index
					: projectedIndex & ((1 << smallSublistSizeExp) - 1);
		} else {
			int largeListOffset = projectedIndex - smallListCapacity;
			sublistIndex = smallSublistCount
					+ (largeListOffset >>> largeSublistSizeExp);
			sublistOffset = sublistIndex == headSublistIndex ? index
					: largeListOffset & ((1 << largeSublistSizeExp) - 1);
		}
		addImpl(index, sublistIndex, sublistOffset, item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(T item) {
		CircularListInternal<T> sublist = sublists.get(tailSublistIndex);
		if (sublist.isFull()) {
			if (tailSublistIndex != sublists.size() - 1 || growTail()) {
				tailSublistIndex += 1;
			}
			sublist = sublists.get(tailSublistIndex);
		}
		sublist.addTail(item);
		if (tailSublistIndex == headSublistIndex) {
			freeCapacityHead -= 1;
		}
		size++;
		assert checkListState(false, false);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFirst(T item) {
		CircularListInternal<T> sublist = sublists.get(headSublistIndex);
		if (sublist.isFull()) {
			if (headSublistIndex == 0) {
				growHead();
			} else {
				headSublistIndex -= 1;
			}
			sublist = sublists.get(headSublistIndex);
		}
		sublist.addHead(item);
		freeCapacityHead -= 1;
		size++;
		assert checkListState(false, false);
	}

	/**
	 * Internal add method requiring the sublist index and offset be
	 * pre-calculated.
	 * 
	 */
	protected void addImpl(int index, int sublistIndex, int sublistOffset,
			T item) {
		if (sublistIndex < headSublistIndex + (calculateSublistsUsed() >>> 1)) {
			if (index == 0) {
				addFirst(item);
			} else {
				CircularListInternal<T> prev = sublists.get(headSublistIndex);
				if (sublistOffset == 0) {
					sublistIndex -= 1;
					sublistOffset = sublists.get(sublistIndex).size() - 1;
				} else {
					sublistOffset -= 1;
				}
				T carryItem = prev.removeHead();
				CircularListInternal<T> next = prev;
				for (int j = headSublistIndex + 1; j <= sublistIndex; j++) {
					next = sublists.get(j);
					prev.addTail(next.removeHead());
					prev = next;
				}
				next.add(sublistOffset, item);
				addFirst(carryItem);
			}
		} else {
			if (index == size) {
				add(item);
			} else {
				CircularListInternal<T> prev = sublists.get(tailSublistIndex);
				T carryItem = prev.removeTail();
				CircularListInternal<T> next = prev;
				for (int j = tailSublistIndex - 1; j >= sublistIndex; j--) {
					next = sublists.get(j);
					prev.addHead(next.removeTail());
					prev = next;
				}
				next.add(sublistOffset, item);
				add(carryItem);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLast(T item) {
		add(item);
	}

	/**
	 * Helper method to calculate the free capacity at the head end of the
	 * sublists.
	 */
	protected int calculateFreeCapacityHead() {
		return headSublistIndex == 0 ? sublists.get(0).calculateFreeCapacity()
				: sublists.get(0).calculateFreeCapacity()
						+ sublists.get(1).calculateFreeCapacity();
	}

	/**
	 * Helper method to fetch the count of used sublists.
	 */
	protected int calculateSublistsUsed() {
		return tailSublistIndex - headSublistIndex + 1;
	}

	/**
	 * Manipulate the state of this instance appropriately if its capacity has
	 * fallen outside of either capacity limit.
	 */
	protected void checkCapacity() {
		if (capacity >= doubleCapacityLimit) {
			assert checkListState(false, true);
			largeSublistSizeExp += 1;
			smallSublistSizeExp += 1;
			halveCapacityLimit = doubleCapacityLimit;
			doubleCapacityLimit <<= 2;
			smallSublistCount = sublists.size();
		} else if (capacity < halveCapacityLimit) {
			assert checkListState(true, false);
			largeSublistSizeExp -= 1;
			smallSublistSizeExp -= 1;
			doubleCapacityLimit = halveCapacityLimit;
			halveCapacityLimit >>>= 2;
			smallSublistCount = 0;
		}
	}

	private boolean checkListState(boolean checkAllSmall, boolean checkAllLarge) {
		if (checkAllLarge) {
			for (int j = 0; j < sublists.size(); j++) {
				assert sublists.get(j).capacity() == (1 << largeSublistSizeExp);
			}
		} else if (checkAllSmall) {
			for (int j = 0; j < sublists.size(); j++) {
				assert sublists.get(j).capacity() == (1 << smallSublistSizeExp);
			}
		} else {
			assert headSublistIndex + 1 <= 2;
			assert sublists.size() - tailSublistIndex <= 2;
			assert sublists.size() >= (sublists.capacity() >>> 2);
			assert freeCapacityHead == calculateFreeCapacityHead();

			int localCapacity = 0;
			for (int j = 0; j < sublists.size(); j++) {
				localCapacity += sublists.get(j).capacity();
				if (j < headSublistIndex || j > tailSublistIndex) {
					assert sublists.get(j).isEmpty();
				} else if (size > 0) {
					if (j == headSublistIndex || j == tailSublistIndex) {
						assert !sublists.get(j).isEmpty();
					} else {
						assert sublists.get(j).calculateFreeCapacity() == 0;
					}
				}
				if (j < smallSublistCount) {
					assert sublists.get(j).capacity() == (1 << smallSublistSizeExp);
				} else {
					assert sublists.get(j).capacity() == (1 << largeSublistSizeExp);
				}
			}
			assert localCapacity == capacity;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		sublists = new FixedListInternal<CircularListInternal<T>>(4);
		sublists.addTail(new CircularListInternal<T>(2));
		sublists.addTail(new CircularListInternal<T>(4));
		sublists.addTail(new CircularListInternal<T>(4));

		capacity = 10;
		smallSublistSizeExp = 1;
		largeSublistSizeExp = 2;
		halveCapacityLimit = 4;
		doubleCapacityLimit = 16;
		smallSublistCount = 1;
		size = 0;
		headSublistIndex = tailSublistIndex = 1;
		freeCapacityHead = 6;
	}

	/**
	 * Clone this list.
	 */
	@Override
	public Object clone() {
		try {
			ThriftyList<T> result = (ThriftyList<T>) super.clone();
			result.sublists = (FixedListInternal<CircularListInternal<T>>) sublists
					.clone();
			for (int j = 0; j < result.sublists.size(); j++) {
				result.sublists.set(j,
						(CircularListInternal<T>) result.sublists.get(j)
								.clone());
			}
			return result;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> descendingIterator() {
		return new ReverseIter(size - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T element() {
		return getFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(int index) {
		if (0 > index || index >= size) {
			throw new IndexOutOfBoundsException();
		}

		int sublistIndex, sublistOffset;
		int projectedIndex = index + freeCapacityHead;
		int smallListCapacity = smallSublistCount << smallSublistSizeExp;
		if (projectedIndex < smallListCapacity) {
			sublistIndex = projectedIndex >>> smallSublistSizeExp;
			sublistOffset = sublistIndex == headSublistIndex ? index
					: projectedIndex & ((1 << smallSublistSizeExp) - 1);
		} else {
			int largeListOffset = projectedIndex - smallListCapacity;
			sublistIndex = smallSublistCount
					+ (largeListOffset >>> largeSublistSizeExp);
			sublistOffset = sublistIndex == headSublistIndex ? index
					: largeListOffset & ((1 << largeSublistSizeExp) - 1);
		}

		return sublists.get(sublistIndex).get(sublistOffset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getFirst() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return sublists.get(headSublistIndex).getHead();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getLast() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return sublists.get(tailSublistIndex).getTail();
	}

	/**
	 * Grow the head of this list by adding capacity of
	 * {@link #smallSublistSizeExp} to the beginning. Note that each time
	 * capacity is added to this {@link ThriftyList}, small lists will be merged
	 * to large lists.
	 * 
	 * @return true if a new sublist was added, false otherwise.
	 */
	protected boolean growHead() {
		assert sublists.getHead().isFull();
		mergeNextSmallSublists();
		checkCapacity();
		if (!sublists.getHead().isFull()) {
			return false;
		}
		if (sublists.isFull()) {
			sublists = copyTo(
					sublists,
					new FixedListInternal<CircularListInternal<T>>(sublists
							.capacity() << 1));
		}
		int smallListSize = 1 << smallSublistSizeExp;
		sublists.addHead(new CircularListInternal<T>(smallListSize));
		smallSublistCount += 1;
		capacity += smallListSize;
		freeCapacityHead += smallListSize;
		tailSublistIndex += 1;
		return true;
	}

	/**
	 * Grow the tail of this list. Note that each time capacity is added to this
	 * {@link ThriftyList}, small lists will be merged to large lists.
	 * 
	 * @return true if a new sublist was added, false otherwise.
	 */
	protected boolean growTail() {
		assert sublists.getTail().isFull();
		mergeNextSmallSublists();
		checkCapacity();
		if (!sublists.getTail().isFull()) {
			return false;
		}
		if (sublists.isFull()) {
			sublists = copyTo(
					sublists,
					new FixedListInternal<CircularListInternal<T>>(sublists
							.capacity() << 1));
		}
		int largeListSize = 1 << largeSublistSizeExp;
		sublists.addTail(new CircularListInternal<T>(largeListSize));
		capacity += largeListSize;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int indexOf(Object o) {
		int total = 0;
		for (int j = headSublistIndex; j <= tailSublistIndex; j++) {
			CircularListInternal<T> next = sublists.get(j);
			int index = next.indexOf(o);
			if (index != -1) {
				return total + index;
			}
			total += next.size();
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iter(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int lastIndexOf(Object o) {
		int total = 0;
		for (int j = tailSublistIndex; j >= headSublistIndex; j--) {
			CircularListInternal<T> next = sublists.get(j);
			total += next.size();
			int index = next.lastIndexOf(o);
			if (index != -1) {
				return size - total + index;
			}
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListIterator<T> listIterator() {
		return listIterator(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		if (0 > index || index > size) {
			throw new IndexOutOfBoundsException();
		}

		return new Iter(index);
	}

	/**
	 * Merge two small sublists to a single large sublist if two small sublists
	 * exist, or double the capacity of the single small sublist to that of a
	 * large sublist if only a single small sublist exists. Do nothing if no
	 * small sublists exist.
	 */
	protected void mergeNextSmallSublists() {
		if (smallSublistCount >= 2) {
			CircularListInternal<T> small2 = sublists
					.remove(--smallSublistCount);
			CircularListInternal<T> small1 = sublists.get(--smallSublistCount);
			sublists.set(
					smallSublistCount,
					merge(small1, small2,
							new CircularListInternal<T>(small1.capacity()
									+ small2.capacity())));
			if (sublists.size() <= (sublists.capacity() >>> 2)) {
				sublists = copyTo(
						sublists,
						new FixedListInternal<CircularListInternal<T>>(sublists
								.capacity() >>> 1));
			}
			if (size > 0) {
				headSublistIndex = sublists.getHead().isEmpty() ? 1 : 0;
				tailSublistIndex = sublists.getTail().isEmpty() ? sublists
						.size() - 2 : sublists.size() - 1;
			} else {
				headSublistIndex = tailSublistIndex = 0;
			}
			if (headSublistIndex == tailSublistIndex) {
				freeCapacityHead = calculateFreeCapacityHead();
			}
		} else if (smallSublistCount == 1) {
			CircularListInternal<T> small = sublists.get(--smallSublistCount);
			sublists.set(
					smallSublistCount,
					copyTo(small, new CircularListInternal<T>(
							small.capacity() << 1)));
			int smallSublistSize = (1 << smallSublistSizeExp);
			capacity += smallSublistSize;
			freeCapacityHead += smallSublistSize;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offer(T e) {
		return offerLast(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerFirst(T e) {
		addFirst(e);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean offerLast(T e) {
		add(e);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T peek() {
		return peekFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T peekFirst() {
		if (size == 0) {
			return null;
		}
		return sublists.get(headSublistIndex).getHead();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T peekLast() {
		if (size == 0) {
			return null;
		}
		return sublists.get(tailSublistIndex).getTail();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T poll() {
		return pollFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T pollFirst() {
		if (size == 0) {
			return null;
		}
		return removeFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T pollLast() {
		if (size == 0) {
			return null;
		}
		return removeLast();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T pop() {
		return removeFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void push(T e) {
		addFirst(e);
	}

	/**
	 * Initializes this instance from the specified stream.
	 */
	private void readObject(ObjectInputStream s) throws java.io.IOException,
			ClassNotFoundException {
		clear();
		int localSize = s.readInt();
		for (int j = 0; j < localSize; j++) {
			add((T) s.readObject());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T remove() {
		return removeFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T remove(int index) {
		if (0 > index || index >= size) {
			throw new IndexOutOfBoundsException();
		}

		int sublistIndex, sublistOffset;
		int projectedIndex = index + freeCapacityHead;
		int smallListCapacity = smallSublistCount << smallSublistSizeExp;
		if (projectedIndex < smallListCapacity) {
			sublistIndex = projectedIndex >>> smallSublistSizeExp;
			sublistOffset = sublistIndex == headSublistIndex ? index
					: projectedIndex & ((1 << smallSublistSizeExp) - 1);
		} else {
			int largeListOffset = projectedIndex - smallListCapacity;
			sublistIndex = smallSublistCount
					+ (largeListOffset >>> largeSublistSizeExp);
			sublistOffset = sublistIndex == headSublistIndex ? index
					: largeListOffset & ((1 << largeSublistSizeExp) - 1);
		}

		return removeImpl(sublistIndex, sublistOffset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T removeFirst() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return removeImpl(headSublistIndex, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeFirstOccurrence(Object o) {
		int index = indexOf(o);
		if (index == -1) {
			return false;
		}
		remove(index);
		return true;
	}

	/**
	 * Internal remove method requiring the sublist index and offset be
	 * pre-calculated.
	 * 
	 * @return the item removed from this list.
	 */
	protected T removeImpl(int sublistIndex, int sublistOffset) {
		T result;
		if (sublistIndex < headSublistIndex + (calculateSublistsUsed() >>> 1)) {
			CircularListInternal<T> prev = sublists.get(sublistIndex);
			result = prev.remove(sublistOffset);
			size -= 1;
			for (int j = sublistIndex - 1; j >= headSublistIndex; j--) {
				CircularListInternal<T> next = sublists.get(j);
				prev.addHead(next.removeTail());
				prev = next;
			}
			freeCapacityHead += 1;
			if (sublists.get(headSublistIndex).isEmpty()) {
				if (headSublistIndex < tailSublistIndex) {
					headSublistIndex += 1;
					if (headSublistIndex == tailSublistIndex) {
						freeCapacityHead += sublists.get(tailSublistIndex)
								.calculateFreeCapacity();
					}
				}
				shrinkHead();
			}
		} else {
			CircularListInternal<T> prev = sublists.get(sublistIndex);
			result = prev.remove(sublistOffset);
			size -= 1;
			if (sublistIndex <= headSublistIndex) {
				freeCapacityHead += 1;
			}
			for (int j = sublistIndex + 1; j <= tailSublistIndex; j++) {
				CircularListInternal<T> next = sublists.get(j);
				prev.addTail(next.removeHead());
				prev = next;
			}
			if (sublists.get(tailSublistIndex).isEmpty()) {
				tailSublistIndex = Math.max(headSublistIndex,
						tailSublistIndex - 1);
				shrinkTail();
			}
		}
		assert checkListState(false, false);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T removeLast() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		CircularListInternal<T> sublist = sublists.get(tailSublistIndex);
		return removeImpl(tailSublistIndex, sublist.size() - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeLastOccurrence(Object o) {
		int index = lastIndexOf(o);
		if (index == -1) {
			return false;
		}
		remove(index);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T set(int index, T item) {
		if (0 > index || index >= size) {
			throw new IndexOutOfBoundsException();
		}

		int sublistIndex, sublistOffset;
		int projectedIndex = index + freeCapacityHead;
		int smallListCapacity = smallSublistCount << smallSublistSizeExp;
		if (projectedIndex < smallListCapacity) {
			sublistIndex = projectedIndex >>> smallSublistSizeExp;
			sublistOffset = sublistIndex == headSublistIndex ? index
					: projectedIndex & ((1 << smallSublistSizeExp) - 1);
		} else {
			int largeListOffset = projectedIndex - smallListCapacity;
			sublistIndex = smallSublistCount
					+ (largeListOffset >>> largeSublistSizeExp);
			sublistOffset = sublistIndex == headSublistIndex ? index
					: largeListOffset & ((1 << largeSublistSizeExp) - 1);
		}
		return setImpl(sublistIndex, sublistOffset, item);
	}

	/**
	 * Internal set method requiring the sublist index and offset be
	 * pre-calculated.
	 * 
	 * @return the element of this list overwritten by the specified item.
	 */
	protected T setImpl(int sublistIndex, int sublistOffset, T item) {
		CircularListInternal<T> sublist = sublists.get(sublistIndex);
		return sublist.set(sublistOffset, item);
	}

	/**
	 * Shrink the head of this list. Note that each time capacity is removed
	 * from this {@link ThriftyList}, large lists will be split to small lists.
	 */
	protected void shrinkHead() {
		while (headSublistIndex >= 2) {
			splitNextLargeSublist();
			CircularListInternal<T> head = sublists.removeHead();
			assert head.isEmpty();
			if (sublists.size() <= (sublists.capacity() >>> 2)) {
				sublists = copyTo(
						sublists,
						new FixedListInternal<CircularListInternal<T>>(sublists
								.capacity() >>> 1));
			}
			capacity -= head.capacity();
			headSublistIndex = Math.max(headSublistIndex - 1, 0);
			tailSublistIndex -= 1;
			freeCapacityHead = calculateFreeCapacityHead();
			if (head.capacity() == (1 << smallSublistSizeExp)) {
				smallSublistCount -= 1;
			}
			checkCapacity();
			shrinkTail();
		}
	}

	/**
	 * Shrink the tail of this list. Note that each time capacity is removed
	 * from this {@link ThriftyList}, large lists will be split to small lists.
	 */
	protected void shrinkTail() {
		while (sublists.size() - tailSublistIndex > 2) {
			splitNextLargeSublist();
			CircularListInternal<T> tail = sublists.removeTail();
			assert tail.isEmpty();
			if (sublists.size() <= (sublists.capacity() >>> 2)) {
				sublists = copyTo(
						sublists,
						new FixedListInternal<CircularListInternal<T>>(sublists
								.capacity() >>> 1));
			}
			capacity -= tail.capacity();
			freeCapacityHead = calculateFreeCapacityHead();
			if (tail.capacity() == (1 << smallSublistSizeExp)) {
				smallSublistCount -= 1;
			}
			checkCapacity();
			shrinkHead();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Splits a large sublist into two small sublists. If one of the small
	 * sublists is empty, then it is discarded. No operation is performed if no
	 * large sublists exist.
	 */
	protected void splitNextLargeSublist() {
		if (smallSublistCount < sublists.size()) {
			if (sublists.isFull()) {
				sublists = copyTo(
						sublists,
						new FixedListInternal<CircularListInternal<T>>(sublists
								.capacity() << 1));
			}
			int firstLargeSublistIndex = smallSublistCount;
			boolean isHeadList = firstLargeSublistIndex <= headSublistIndex;
			boolean isTailList = firstLargeSublistIndex >= tailSublistIndex;
			CircularListInternal<T> large = sublists
					.get(firstLargeSublistIndex);
			assert large.capacity() == (1 << largeSublistSizeExp);
			CircularListInternal<T> small1 = new CircularListInternal<T>(
					large.capacity() >>> 1);
			CircularListInternal<T> small2 = new CircularListInternal<T>(
					large.capacity() >>> 1);
			split(large, small1, small2, isHeadList);
			if (isHeadList) {
				sublists.set(firstLargeSublistIndex, small2);
				sublists.add(firstLargeSublistIndex, small1);
				smallSublistCount += 2;
				if (small1.isEmpty()) {
					headSublistIndex += 1;
				}
				tailSublistIndex += 1;
			} else if (isTailList) {
				sublists.set(firstLargeSublistIndex, small1);
				sublists.add(firstLargeSublistIndex + 1, small2);
				smallSublistCount += 2;
				if (!small2.isEmpty()) {
					tailSublistIndex += 1;
				}
			} else {
				sublists.set(firstLargeSublistIndex, small2);
				sublists.add(firstLargeSublistIndex, small1);
				smallSublistCount += 2;
				tailSublistIndex += 1;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T[] toArray() {
		return toArray(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <U> U[] toArray(U[] target) {
		if (target == null || target.length < size) {
			target = (U[]) new Object[size];
		}
		int index = 0;
		for (int j = headSublistIndex; j <= tailSublistIndex; j++) {
			CircularListInternal<T> sublist = sublists.get(j);
			sublist.fill((T[]) target, index, 0, sublist.size());
			index += sublist.size();
		}
		if (target.length > size) {
			target[size] = null;
		}
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int j = headSublistIndex; j <= tailSublistIndex; j++) {
			builder.append(sublists.get(j).toString()).append(",");
		}
		builder.setCharAt(builder.length() - 1, ']');
		return builder.toString();
	}

	/**
	 * Writes this instance to the specified stream.
	 */
	private void writeObject(ObjectOutputStream s) throws java.io.IOException {
		s.writeInt(size);
		for (T next : this) {
			s.writeObject(next);
		}
	}

	/**
	 * A fixed capacity circular list. This is strictly an internal helper class
	 * to {@link ThriftyList} as it performs no bounds checking, delegating all
	 * such responsibilities to {@link ThriftyList} itself.
	 */
	protected static class CircularListInternal<T> implements ListInternal<T>,
			Cloneable {
		protected Object[] array;
		protected int head;
		protected int size;

		/**
		 * Construct an empty instance with the specified capacity.
		 * 
		 * @param capacity
		 *            the capacity of the list, must be a power of two.
		 */
		public CircularListInternal(int capacity) {
			assert (capacity & (capacity - 1)) == 0;
			array = new Object[capacity];
			head = size = 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void add(int index, T item) {
			assert 0 <= index && index <= size;
			assert size < capacity();

			int mask = array.length - 1;
			if (index <= (size >>> 1)) {
				if (index == 0) {
					addHead(item);
				} else {
					int i = (head - 1 + index) & mask;
					if (head <= i) {
						if (head == 0) {
							array[mask] = array[0];
							System.arraycopy(array, 1, array, 0, index - 1);
							head = mask;
						} else {
							System.arraycopy(array, head, array, head - 1,
									index);
							head -= 1;
						}
					} else {
						int amount = array.length - head;
						System.arraycopy(array, head, array, head - 1, amount);
						array[mask] = array[0];
						System.arraycopy(array, 1, array, 0, i);
						head -= 1;
					}
					array[i] = item;
					size += 1;
				}
			} else {
				if (index == size) {
					addTail(item);
				} else {
					int i = (head + index) & mask;
					int tail = (head + size) & mask;
					if (i <= tail) {
						System.arraycopy(array, i, array, i + 1, tail - i);
						tail = (tail + 1) & mask;
					} else {
						System.arraycopy(array, 0, array, 1, tail);
						array[0] = array[mask];
						System.arraycopy(array, i, array, i + 1, mask - i);
						tail += 1;
					}
					array[i] = item;
					size += 1;
				}
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addAll(ListInternal<T> source) {
			addSome(source, 0, source.size());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addHead(T item) {
			assert size < capacity();

			head = (head - 1) & (array.length - 1);
			array[head] = item;
			size += 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addSome(ListInternal<T> source, int index, int count) {
			assert size + count <= capacity();

			int mask = array.length - 1;
			int tail = (head + size) & mask;
			while (count > 0) {
				int batch = Math.min(count, array.length - tail);
				source.fill((T[]) array, tail, index, batch);
				tail = (tail + batch) & mask;
				index += batch;
				count -= batch;
				size += batch;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addTail(T item) {
			assert size < capacity();

			array[(head + size) & (array.length - 1)] = item;
			size += 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int calculateFreeCapacity() {
			return array.length - size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int capacity() {
			return array.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			Arrays.fill(array, null);
			head = size = 0;
		}

		/**
		 * Clone this list.
		 */
		@Override
		public Object clone() {
			try {
				CircularListInternal<T> result = (CircularListInternal<T>) super
						.clone();
				result.array = Arrays.copyOf(array, array.length);
				return result;
			} catch (CloneNotSupportedException e) {
				throw new InternalError();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fill(T[] target, int targetIndex, int index, int count) {
			assert (target.length - targetIndex) >= count;
			assert count <= size;
			if (count == 0) {
				return;
			}
			index = (head + index) & (array.length - 1);
			int tail = (index + count) & (array.length - 1);
			if (index < tail) {
				System.arraycopy(array, index, target, targetIndex, count);
			} else {
				int c = array.length - index;
				System.arraycopy(array, index, target, targetIndex, c);
				count -= c;
				targetIndex += c;
				System.arraycopy(array, 0, target, targetIndex, count);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T get(int index) {
			assert 0 <= index && index < size;

			return (T) array[(head + index) & (array.length - 1)];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getHead() {
			assert size > 0;

			return (T) array[head];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getTail() {
			assert size > 0;

			return (T) array[(head + size - 1) & (array.length - 1)];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int indexOf(Object o) {
			if (o == null) {
				for (int j = 0; j < size; j++) {
					if (get(j) == null) {
						return j;
					}
				}
			} else {
				for (int j = 0; j < size; j++) {
					if (o.equals(get(j))) {
						return j;
					}
				}
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isFull() {
			return size == array.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int lastIndexOf(Object o) {
			if (o == null) {
				for (int j = size - 1; j >= 0; j--) {
					if (get(j) == null) {
						return j;
					}
				}
			} else {
				for (int j = size - 1; j >= 0; j--) {
					if (o.equals(get(j))) {
						return j;
					}
				}
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T remove(int index) {
			assert 0 <= index && index < size;

			int mask = array.length - 1;
			int i = (head + index) & mask;
			T result = (T) array[i];
			if (index < (size >>> 1)) {
				if (head <= i) {
					System.arraycopy(array, head, array, head + 1, index);
				} else {
					System.arraycopy(array, 0, array, 1, i);
					array[0] = array[mask];
					System.arraycopy(array, head, array, head + 1, mask - head);
				}
				array[head] = null;
				head = (head + 1) & mask;
			} else {
				int tail = (head + size) & mask;
				if (i < tail) {
					System.arraycopy(array, i + 1, array, i, tail - i - 1);
					tail -= 1;
				} else {
					System.arraycopy(array, i + 1, array, i, mask - i);
					array[mask] = array[0];
					if (tail > 0) {
						System.arraycopy(array, 1, array, 0, tail - 1);
					}
					tail = (tail - 1) & mask;
				}
				array[tail] = null;
			}
			size -= 1;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T removeHead() {
			assert size > 0;

			T result = (T) array[head];
			array[head] = null;
			head = (head + 1) & (array.length - 1);
			size -= 1;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T removeTail() {
			assert size > 0;

			int tail = (head + size - 1) & (array.length - 1);
			T result = (T) array[tail];
			array[tail] = null;
			size -= 1;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T set(int index, T item) {
			assert 0 <= index && index < size;

			int i = (head + index) & (array.length - 1);
			T result = (T) array[i];
			array[i] = item;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < size; j++) {
				builder.append(get(j)).append(",");
			}
			return builder.length() == 0 ? "" : builder.substring(0,
					builder.length() - 1);
		}
	}

	/**
	 * A fixed capacity list. This is strictly an internal helper class to
	 * {@link ThriftyList} as it performs no bounds checking, delegating all
	 * such responsibilities to {@link ThriftyList} itself.
	 */
	protected static class FixedListInternal<T> implements ListInternal<T>,
			Cloneable {
		protected Object[] array;
		protected int size;

		/**
		 * Construct an empty instance with the specified capacity.
		 * 
		 * @param capacity
		 *            the capacity of the list, must be a power of two.
		 */
		public FixedListInternal(int capacity) {
			assert (capacity & (capacity - 1)) == 0;
			array = new Object[capacity];
			size = 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void add(int index, T item) {
			assert 0 <= index && index <= size;
			assert size < capacity();
			System.arraycopy(array, index, array, index + 1, size - index);
			array[index] = item;
			size += 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addAll(ListInternal<T> source) {
			addSome(source, 0, source.size());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addHead(T item) {
			assert size < capacity();
			add(0, item);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addSome(ListInternal<T> source, int index, int count) {
			assert size + count <= capacity();

			if (count < 0) {
				return;
			}

			source.fill((T[]) array, size, index, count);
			size += count;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addTail(T item) {
			assert size < capacity();

			add(size, item);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int calculateFreeCapacity() {
			return array.length - size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int capacity() {
			return array.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			Arrays.fill(array, null);
			size = 0;
		}

		/**
		 * Clone this list.
		 */
		@Override
		public Object clone() {
			try {
				FixedListInternal<T> result = (FixedListInternal<T>) super
						.clone();
				result.array = Arrays.copyOf(array, array.length);
				return result;
			} catch (CloneNotSupportedException e) {
				throw new InternalError();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fill(T[] target, int targetIndex, int index, int count) {
			assert (target.length - targetIndex) >= count;
			assert count <= size;
			if (count == 0) {
				return;
			}
			System.arraycopy(array, index, target, targetIndex, count);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T get(int index) {
			assert 0 <= index && index < size;

			return (T) array[index];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getHead() {
			assert size > 0;

			return (T) array[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getTail() {
			assert size > 0;

			return (T) array[size - 1];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int indexOf(Object o) {
			if (o == null) {
				for (int j = 0; j < size; j++) {
					if (get(j) == null) {
						return j;
					}
				}
			} else {
				for (int j = 0; j < size; j++) {
					if (o.equals(get(j))) {
						return j;
					}
				}
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isFull() {
			return size == array.length;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int lastIndexOf(Object o) {
			if (o == null) {
				for (int j = size - 1; j >= 0; j--) {
					if (get(j) == null) {
						return j;
					}
				}
			} else {
				for (int j = size - 1; j >= 0; j--) {
					if (o.equals(get(j))) {
						return j;
					}
				}
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T remove(int index) {
			assert 0 <= index && index < size;

			T result = (T) array[index];
			System.arraycopy(array, index + 1, array, index, size - index - 1);
			size -= 1;
			array[size] = null;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T removeHead() {
			assert size > 0;

			return remove(0);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T removeTail() {
			assert size > 0;

			return remove(size - 1);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T set(int index, T item) {
			assert 0 <= index && index < size;

			T result = (T) array[index];
			array[index] = item;
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			return size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < size; j++) {
				builder.append(get(j)).append(",");
			}
			return builder.length() == 0 ? "" : builder.substring(0,
					builder.length() - 1);
		}
	}

	protected class Iter extends IterBase {
		public Iter(int index) {
			super(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return index < size;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int nextIndex() {
			return index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int previousIndex() {
			return index - 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void stepForward() {
			currentIndex = index;
			currentSublistIndex = sublistIndex;
			currentSublistOffset = sublistOffset;

			index += 1;
			sublistOffset += 1;
			if (sublistOffset == sublists.get(sublistIndex).size()
					&& sublistIndex < sublists.size() - 1) {
				sublistIndex += 1;
				sublistOffset = 0;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void stepReverse() {
			index -= 1;
			if (sublistOffset == 0 && sublistIndex > 0) {
				sublistIndex -= 1;
				sublistOffset = sublists.get(sublistIndex).size() - 1;
			} else {
				sublistOffset -= 1;
			}
			currentIndex = index;
			currentSublistIndex = sublistIndex;
			currentSublistOffset = sublistOffset;
		}
	}

	protected abstract class IterBase implements ListIterator<T> {
		protected int index, sublistIndex, sublistOffset;
		protected int currentIndex, currentSublistIndex, currentSublistOffset;

		public IterBase(int index) {
			this.index = index;
			this.currentIndex = -1;
			cursor();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void add(T item) {
			addImpl(index, sublistIndex, sublistOffset, item);
			cursor();
			stepForward();
			currentIndex = -1;
		}

		/**
		 * Calculates the sublist index/offset for the index.
		 */
		protected void cursor() {
			int projectedIndex = index + freeCapacityHead;
			int smallListCapacity = smallSublistCount << smallSublistSizeExp;
			if (projectedIndex < smallListCapacity) {
				sublistIndex = projectedIndex >>> smallSublistSizeExp;
				sublistOffset = sublistIndex == headSublistIndex ? index
						: projectedIndex & ((1 << smallSublistSizeExp) - 1);
			} else {
				int largeListOffset = projectedIndex - smallListCapacity;
				sublistIndex = smallSublistCount
						+ (largeListOffset >>> largeSublistSizeExp);
				sublistOffset = sublistIndex == headSublistIndex ? index
						: largeListOffset & ((1 << largeSublistSizeExp) - 1);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			T result = sublists.get(sublistIndex).get(sublistOffset);
			stepForward();
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}

			stepReverse();
			return sublists.get(sublistIndex).get(sublistOffset);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			if (currentIndex == -1) {
				throw new IllegalStateException();
			}
			if (currentIndex < index) {
				stepReverse();
			}
			removeImpl(currentSublistIndex, currentSublistOffset);
			cursor();
			currentIndex = -1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void set(T e) {
			if (currentIndex == -1) {
				throw new IllegalStateException();
			}
			setImpl(currentSublistIndex, currentSublistOffset, e);
		}

		protected abstract void stepForward();

		protected abstract void stepReverse();
	}

	/**
	 * <p>
	 * Interface for internal helper lists to {@link ThriftyList} as instances
	 * are not required to perform bounds checking (all such responsibilities
	 * are delegated to {@link ThriftyList} itself).
	 * 
	 * <p>
	 * This interface exists primarily to support the generic static helper
	 * functions
	 * {@link ThriftyList#merge(ListInternal, ListInternal, ListInternal)},
	 * {@link ThriftyList#split(ListInternal, ListInternal, ListInternal, boolean)}
	 * and {@link ThriftyList#copyTo(ListInternal, ListInternal)}.
	 */
	protected static interface ListInternal<T> {
		/**
		 * Add the specified item to this list at the specified index, shifting
		 * elements of this list if necessary.
		 * 
		 * @param index
		 *            the index at which to add the item.
		 * @param item
		 *            the item to add.
		 */
		public void add(int index, T item);

		/**
		 * Add all elements of the specified source list to this list.
		 * 
		 * @param source
		 *            the source list.
		 */
		public void addAll(ListInternal<T> source);

		/**
		 * Add the specified item to the beginning of this list.
		 * 
		 * @param item
		 *            the item to add to this list.
		 */
		public void addHead(T item);

		/**
		 * Add a number of elements from the specified source list, beginning
		 * with the element at the specified index and ending once the specified
		 * count of added elements has been reached, to this list.
		 * 
		 * @param source
		 *            the source list.
		 * @param index
		 *            the index at which to begin adding elements to this list.
		 * @param count
		 *            the number of elements to add to this list.
		 */
		public void addSome(ListInternal<T> source, int index, int count);

		/**
		 * Add the specified item to the end of this list.
		 * 
		 * @param item
		 *            the item to add to this list.
		 */
		public void addTail(T item);

		/**
		 * 
		 * @return the amount of free capacity.
		 */
		public int calculateFreeCapacity();

		/**
		 * 
		 * @return the capacity of the list.
		 */
		public int capacity();

		/**
		 * Clear all elements from this list.
		 */
		public void clear();

		/**
		 * Fill the specified array, beginning with the specified index, with
		 * the data of this list.
		 * 
		 * @param target
		 *            the target array.
		 * @param targetIndex
		 *            the array index at which to begin filling the target array
		 *            with this list's data.
		 * @param index
		 *            the index of this list at which to begin filling the
		 *            target array.
		 * @param count
		 *            the number of elements to copy to the target array.
		 */
		public void fill(T[] target, int targetIndex, int index, int count);

		/**
		 * Get the element of this list at the specified index.
		 * 
		 * @param index
		 *            the index of the desired element.
		 * @return the element at the specified index.
		 */
		public T get(int index);

		/**
		 * 
		 * @return the first element of this list.
		 */
		public T getHead();

		/**
		 * 
		 * @return the last element of this list.
		 */
		public T getTail();

		/**
		 * Get the index of the first list element equal to the specified
		 * Object.
		 * 
		 * @param o
		 *            the Object for which to search.
		 * @return the index of the first element equal to the specified Object,
		 *         -1 if none.
		 */
		public int indexOf(Object o);

		/**
		 * 
		 * @return true if this list is empty, false otherwise.
		 */
		public boolean isEmpty();

		/**
		 * 
		 * @return true if this list has no free capacity, false otherwise.
		 */
		public boolean isFull();

		/**
		 * Get the index of the last element equal to the specified Object.
		 * 
		 * @param o
		 *            the Object for which to search.
		 * @return the index of the last element equal to the specified Object,
		 *         -1 if none.
		 */
		public int lastIndexOf(Object o);

		/**
		 * Remove and return the element of this list at the specified index.
		 * 
		 * @param index
		 *            the index of the element to remove.
		 * @return the item removed from this list.
		 */
		public T remove(int index);

		/**
		 * Remove the head element of this list.
		 * 
		 * @return the item removed from this list.
		 */
		public T removeHead();

		/**
		 * Remove the tail element of this list.
		 * 
		 * @return the item removed from this list.
		 */
		public T removeTail();

		/**
		 * Set the element of this list at the specified index to the specified
		 * item.
		 * 
		 * @param index
		 *            the index at which to set the item.
		 * @param item
		 *            the item to set to this list.
		 * @return the element currently at the specified index.
		 */
		public T set(int index, T item);

		/**
		 * 
		 * @return the number of elements in the list.
		 */
		public int size();
	}

	protected class ReverseIter extends IterBase {
		public ReverseIter(int index) {
			super(index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return index >= 0;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasPrevious() {
			return index < size - 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int nextIndex() {
			return index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int previousIndex() {
			return index + 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void stepForward() {
			currentIndex = index;
			currentSublistIndex = sublistIndex;
			currentSublistOffset = sublistOffset;
			index -= 1;
			if (sublistOffset == 0 && sublistIndex > 0) {
				sublistIndex -= 1;
				sublistOffset = sublists.get(sublistIndex).size() - 1;
			} else {
				sublistOffset -= 1;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void stepReverse() {
			index += 1;
			sublistOffset += 1;
			if (sublistOffset == sublists.get(sublistIndex).size()
					&& sublistIndex < sublists.size() - 1) {
				sublistIndex += 1;
				sublistOffset = 0;
			}
			currentIndex = index;
			currentSublistIndex = sublistIndex;
			currentSublistOffset = sublistOffset;
		}
	}
}