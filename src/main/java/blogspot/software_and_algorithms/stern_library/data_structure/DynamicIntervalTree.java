package blogspot.software_and_algorithms.stern_library.data_structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import blogspot.software_and_algorithms.stern_library.data_structure.RedBlackTree.Node.NodeColor;

/* Copyright (c) 2012 Kevin L. Stern
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
 * A <em>dynamic interval tree</em> is a balanced binary search tree which
 * stores intervals so that both point queries (queries that return intervals
 * from the set which contain a query point) and overlapping interval queries
 * (queries that return intervals from the set which overlap a query interval)
 * can be completed in time O(log(n) + k), where n is the number of intervals
 * stored in the tree and k is the size of the result set from the query.
 * <p>
 * Insertion and deletion of intervals to and from this tree completes in time
 * O(log(n)) where n is the number of intervals stored in the tree.
 * <p>
 * This tree consumes linear space in the number of intervals stored in the
 * tree.
 * <p>
 * Note that this implementation supports all three closed, open and half-open
 * intervals.
 * 
 * @author Kevin L. Stern
 */
public class DynamicIntervalTree<U extends Comparable<U>, T extends Interval<U>> {
	public RedBlackTree<T> binarySearchTree = new RedBlackTree<T>(
			new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					int result = o1.getLow().compareTo(o2.getLow());
					if (result == 0) {
						if (o1.isClosedOnLow() != o2.isClosedOnLow()) {
							result = o1.isClosedOnLow() ? -1 : 1;
						} else {
							result = o1.compareTo(o2);
						}
					}
					return result;
				}
			}) {
		@Override
		protected RedBlackTree.Node<T> createNewNode(T value) {
			return new DynamicIntervalTree.Node<U, T>(value);
		}

		@Override
		public RedBlackTree.Node<T> delete(T value) {
			RedBlackTree.Node<T> node = super.delete(value);
			if (node != null && node.getColor() != NodeColor.BLACK) {
				DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node
						.getParent();
				while (temp != null) {
					temp.computeMaximumHighEndpoint();
					temp = temp.getParent();
				}
			}
			return node;
		}

		@Override
		protected void fixAfterDeletion(RedBlackTree.Node<T> node) {
			DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node
					.getParent();
			while (temp != null) {
				temp.computeMaximumHighEndpoint();
				temp = temp.getParent();
			}
			super.fixAfterDeletion(node);
		}

		@Override
		protected void fixAfterInsertion(RedBlackTree.Node<T> node) {
			DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node
					.getParent();
			while (temp != null) {
				temp.computeMaximumHighEndpoint();
				temp = temp.getParent();
			}
			super.fixAfterInsertion(node);
		}

		@Override
		protected void leftRotate(RedBlackTree.Node<T> node) {
			super.leftRotate(node);
			DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node;
			temp.computeMaximumHighEndpoint();
			temp.getParent().computeMaximumHighEndpoint();
		}

		@Override
		protected void rightRotate(RedBlackTree.Node<T> node) {
			super.rightRotate(node);
			DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node;
			temp.computeMaximumHighEndpoint();
			temp.getParent().computeMaximumHighEndpoint();
		}
	};

	/**
	 * Clear the contents of the tree.
	 */
	public void clear() {
		binarySearchTree.clear();
	}

	/**
	 * Delete the specified interval from this tree.
	 * 
	 * @param interval
	 *            the interval to delete.
	 * @return true if an element was deleted as a result of this call, false
	 *         otherwise.
	 */
	public boolean delete(T interval) {
		return binarySearchTree.delete(interval) != null;
	}

	/**
	 * Fetch an interval containing the specified point.
	 * 
	 * @param queryPoint
	 *            the query point.
	 * @return an interval containing the specified point, null if none.
	 */
	protected T fetchContainingInterval(U queryPoint) {
		Node<U, T> node = (Node<U, T>) binarySearchTree.getRoot();
		while (node != null) {
			if (node.getValue().contains(queryPoint)) {
				return node.getValue();
			}
			Node<U, T> leftChild = node.getLeft();
			node = node.getRight();
			if (leftChild != null) {
				int cmp = leftChild.getMaximumHighEndpoint().compareTo(
						queryPoint);
				if (cmp > 0 || cmp == 0 && leftChild.isClosedOnEndpoint()) {
					node = leftChild;
				}
			}
		}
		return null;
	}

	/**
	 * Fetch intervals containing the specified point.
	 * 
	 * @param queryPoint
	 *            the query point.
	 * @return a Collection of all intervals containing the specified point.
	 */
	public Collection<T> fetchContainingIntervals(U queryPoint) {
		if (queryPoint == null) {
			throw new NullPointerException("queryPoint is null");
		}
		List<T> result = new ArrayList<T>();
		fetchContainingIntervals(queryPoint, 
				(Node<U, T>) binarySearchTree.getRoot(), result);

		return result;
	}

	/**
	 * Retrieving all intervals containing queryPoint in recursive way
	 */
	private void fetchContainingIntervals(U queryPoint, Node<U, T> root,
			List<T> result) {
		if(root == null)
			return;
		
		// If queryPoint is to the right of the rightmost point of any interval
	    // in this node and all children, there won't be any matches.
		if(queryPoint.compareTo(root.getMaximumHighEndpoint()) > 0)
			return;
		
		// Search left children
		fetchContainingIntervals(queryPoint, root.getLeft(), result);

		// Check this node
		if (root.getValue().contains(queryPoint))
			result.add(root.getValue());

		// If queryPoint is to the left of the start of this interval,
		// then it can't be in any child to the right.
		if (queryPoint.compareTo(root.getValue().getLow()) < 0)
			return;

		// Otherwise, search right children
		fetchContainingIntervals(queryPoint, root.getRight(), result);
	}

	/**
	 * Fetch an interval overlapping the specified interval.
	 * 
	 * @param queryInterval
	 *            the query interval.
	 * @return an interval overlapping the specified interval, null if none.
	 */
	protected T fetchOverlappingInterval(T queryInterval) {
		Node<U, T> node = (Node<U, T>) binarySearchTree.getRoot();
		while (node != null) {
			if (node.getValue().overlaps(queryInterval)) {
				return node.getValue();
			}
			Node<U, T> leftChild = node.getLeft();
			node = node.getRight();
			if (leftChild != null) {
				int cmp = leftChild.getMaximumHighEndpoint().compareTo(
						queryInterval.getLow());
				if (cmp > 0 || cmp == 0 && leftChild.isClosedOnEndpoint()
						&& queryInterval.isClosedOnLow()) {
					node = leftChild;
				}
			}
		}
		return null;
	}

	/**
	 * Fetch intervals overlapping the specified interval.
	 * 
	 * @param queryInterval
	 *            the query interval.
	 * @return a Collection of all intervals overlapping the specified point.
	 */
	public Collection<T> fetchOverlappingIntervals(T queryInterval) {
		if (queryInterval == null) {
			throw new NullPointerException("queryInterval is null");
		}
		List<T> result = new ArrayList<T>();
		fetchOverlappingIntervals(queryInterval, 
				(Node<U, T>) binarySearchTree.getRoot(), result);
		
		return result;
	}
	
	/**
	 * Retrieve all overlapping intervals in recursive way
	 */
	private void fetchOverlappingIntervals(T queryInterval, 
			Node<U, T> root, List<T> result){
		if(root == null)
			return;
		
		// If queryInterval is to the right of the rightmost point of any interval
	    // in this node and all children, there won't be any matches.
		if(queryInterval.getLow().compareTo(root.getMaximumHighEndpoint()) > 0)
			return;
		
		// Search left children
		fetchOverlappingIntervals(queryInterval, root.getLeft(), result);

		// Check this node
		if (root.getValue().overlaps(queryInterval))
			result.add(root.getValue());

		// If queryInterval is to the left of the start of this interval,
		// then it can't be in any child to the right.
		if (queryInterval.getHigh().compareTo(root.getValue().getLow()) < 0)
			return;

		// Otherwise, search right children
		fetchOverlappingIntervals(queryInterval, root.getRight(), result);
	}

	/**
	 * Get the number of intervals being stored in the tree.
	 * 
	 * @return the number of intervals being stored in the tree.
	 */
	public int getSize() {
		return binarySearchTree.getSize();
	}

	/**
	 * Insert the specified interval into this tree.
	 * 
	 * @param interval
	 *            the interval to insert.
	 * @return true if an element was inserted as a result of this call, false
	 *         otherwise.
	 */
	public boolean insert(T interval) {
		return binarySearchTree.insert(interval) != null;
	}

	/**
	 * A <em>node</em> for a dynamic interval tree is a red-black tree node
	 * augmented to store the maximum high endpoint among intervals stored
	 * within the subtree rooted at the node.
	 */
	protected static class Node<U extends Comparable<U>, T extends Interval<U>>
			extends RedBlackTree.Node<T> {
		private U maximumHighEndpoint;
		private boolean isClosedOnEndpoint;

		/**
		 * Construct a new node associated with the specified interval.
		 * 
		 * @param interval
		 *            the interval with which this node is associated.
		 */
		public Node(T interval) {
			super(interval);
			maximumHighEndpoint = interval.getHigh();
			isClosedOnEndpoint = interval.isClosedOnHigh();
		}

		/**
		 * Compute the maximum high endpoint among intervals stored within the
		 * subtree rooted at this node and correct values up the tree.
		 */
		protected void computeMaximumHighEndpoint() {
			U maximumHighEndpoint = getValue().getHigh();
			boolean isClosedOnEndpoint = getValue().isClosedOnHigh();
			Node<U, T> child;
			child = getLeft();
			if (child != null) {
				int cmp = child.maximumHighEndpoint
						.compareTo(maximumHighEndpoint);
				if (cmp > 0 || cmp == 0 && child.isClosedOnEndpoint) {
					maximumHighEndpoint = child.maximumHighEndpoint;
					isClosedOnEndpoint = child.isClosedOnEndpoint;
				}
			}
			child = getRight();
			if (child != null) {
				int cmp = child.maximumHighEndpoint
						.compareTo(maximumHighEndpoint);
				if (cmp > 0 || cmp == 0 && child.isClosedOnEndpoint) {
					maximumHighEndpoint = child.maximumHighEndpoint;
					isClosedOnEndpoint = child.isClosedOnEndpoint;
				}
			}
			this.maximumHighEndpoint = maximumHighEndpoint;
			this.isClosedOnEndpoint = isClosedOnEndpoint;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Node<U, T> getLeft() {
			return (Node<U, T>) super.getLeft();
		}

		public U getMaximumHighEndpoint() {
			return maximumHighEndpoint;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Node<U, T> getParent() {
			return (Node<U, T>) super.getParent();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Node<U, T> getRight() {
			return (Node<U, T>) super.getRight();
		}

		public boolean isClosedOnEndpoint() {
			return isClosedOnEndpoint;
		}
	}
}

