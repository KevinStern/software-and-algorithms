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
 * can be completed in time O(k*log(n)), where n is the number of intervals
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
          temp.computeSubtreeSpan();
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
        temp.computeSubtreeSpan();
        temp = temp.getParent();
      }
      super.fixAfterDeletion(node);
    }

    @Override
    protected void fixAfterInsertion(RedBlackTree.Node<T> node) {
      DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node
          .getParent();
      while (temp != null) {
        temp.computeSubtreeSpan();
        temp = temp.getParent();
      }
      super.fixAfterInsertion(node);
    }

    @Override
    protected void leftRotate(RedBlackTree.Node<T> node) {
      super.leftRotate(node);
      DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node;
      temp.computeSubtreeSpan();
      temp.getParent().computeSubtreeSpan();
    }

    @Override
    protected void rightRotate(RedBlackTree.Node<T> node) {
      super.rightRotate(node);
      DynamicIntervalTree.Node<U, T> temp = (DynamicIntervalTree.Node<U, T>) node;
      temp.computeSubtreeSpan();
      temp.getParent().computeSubtreeSpan();
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
   *          the interval to delete.
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
   *          the query point.
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
        int cmp = leftChild.getSubtreeSpanHigh().compareTo(queryPoint);
        if (cmp > 0 || cmp == 0 && leftChild.isClosedOnSubtreeSpanHigh()) {
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
   *          the query point.
   * @return a Collection of all intervals containing the specified point.
   */
  public Collection<T> fetchContainingIntervals(U queryPoint) {
    if (queryPoint == null) {
      throw new NullPointerException("queryPoint is null");
    }
    List<T> result = new ArrayList<T>();
    Node<U, T> node = (Node<U, T>) binarySearchTree.getRoot();
    List<Node<U, T>> queue = new ArrayList<Node<U, T>>();
    if (node != null) {
      queue.add(node);
    }
    while (!queue.isEmpty()) {
      node = queue.remove(queue.size() - 1);
      if (node.getValue().contains(queryPoint)) {
        result.add(node.getValue());
      }
      Node<U, T> child = node.getLeft();
      if (child != null) {
        int cmp = child.getSubtreeSpanHigh().compareTo(queryPoint);
        if (cmp > 0 || cmp == 0 && child.isClosedOnSubtreeSpanHigh()) {
          queue.add(child);
        }
      }
      child = node.getRight();
      if (child != null) {
        int cmp = child.getSubtreeSpanLow().compareTo(queryPoint);
        if (cmp < 0 || cmp == 0 && child.isClosedOnSubtreeSpanLow()) {
          queue.add(child);
        }
      }
    }
    return result;
  }

  /**
   * Fetch an interval overlapping the specified interval.
   * 
   * @param queryInterval
   *          the query interval.
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
        int cmp = leftChild.getSubtreeSpanHigh().compareTo(queryInterval
                                                               .getLow());
        if (cmp > 0 || cmp == 0 && leftChild.isClosedOnSubtreeSpanHigh()
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
   *          the query interval.
   * @return a Collection of all intervals overlapping the specified point.
   */
  public Collection<T> fetchOverlappingIntervals(T queryInterval) {
    if (queryInterval == null) {
      throw new NullPointerException("queryInterval is null");
    }
    List<T> result = new ArrayList<T>();
    Node<U, T> node = (Node<U, T>) binarySearchTree.getRoot();
    List<Node<U, T>> queue = new ArrayList<Node<U, T>>();
    if (node != null) {
      queue.add(node);
    }
    while (!queue.isEmpty()) {
      node = queue.remove(queue.size() - 1);
      if (node.getValue().overlaps(queryInterval)) {
        result.add(node.getValue());
      }
      Node<U, T> child = node.getLeft();
      if (child != null) {
        int cmp = child.getSubtreeSpanHigh().compareTo(queryInterval.getLow());
        if (cmp > 0 || cmp == 0 && child.isClosedOnSubtreeSpanHigh()
            && queryInterval.isClosedOnLow()) {
          queue.add(child);
        }
      }
      child = node.getRight();
      if (child != null) {
        int cmp = child.getSubtreeSpanLow().compareTo(queryInterval.getHigh());
        if (cmp < 0 || cmp == 0 && child.isClosedOnSubtreeSpanLow()
            && queryInterval.isClosedOnHigh()) {
          queue.add(child);
        }
      }
    }
    return result;
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
   *          the interval to insert.
   * @return true if an element was inserted as a result of this call, false
   *         otherwise.
   */
  public boolean insert(T interval) {
    return binarySearchTree.insert(interval) != null;
  }

  /**
   * A <em>node</em> for a dynamic interval tree is a red-black tree node
   * augmented to store the maximum high and minimum low endpoints among
   * intervals stored within the subtree rooted at the node.
   */
  protected static class Node<U extends Comparable<U>, T extends Interval<U>>
      extends RedBlackTree.Node<T> {
    private U subtreeSpanLow, subtreeSpanHigh;
    private boolean isClosedOnSubtreeSpanLow, isClosedOnSubtreeSpanHigh;

    /**
     * Construct a new node associated with the specified interval.
     * 
     * @param interval
     *          the interval with which this node is associated.
     */
    public Node(T interval) {
      super(interval);
      subtreeSpanLow = interval.getLow();
      subtreeSpanHigh = interval.getHigh();
      isClosedOnSubtreeSpanLow = interval.isClosedOnLow();
      isClosedOnSubtreeSpanHigh = interval.isClosedOnHigh();
    }

    /**
     * Compute the maximum high and minimum low endpoints among intervals stored
     * within the subtree rooted at this node and correct values up the tree.
     */
    protected void computeSubtreeSpan() {
      U subtreeSpanLow = getValue().getLow();
      U subtreeSpanHigh = getValue().getHigh();
      boolean isClosedOnSubtreeSpanLow = getValue().isClosedOnLow();
      boolean isClosedOnSubtreeSpanHigh = getValue().isClosedOnHigh();
      Node<U, T> child;
      child = getLeft();
      if (child != null) {
        int cmp = child.subtreeSpanLow.compareTo(subtreeSpanLow);
        if (cmp < 0 || cmp == 0 && child.isClosedOnSubtreeSpanLow) {
          subtreeSpanLow = child.subtreeSpanLow;
          isClosedOnSubtreeSpanLow = child.isClosedOnSubtreeSpanLow;
        }
        cmp = child.subtreeSpanHigh.compareTo(subtreeSpanHigh);
        if (cmp > 0 || cmp == 0 && child.isClosedOnSubtreeSpanHigh) {
          subtreeSpanHigh = child.subtreeSpanHigh;
          isClosedOnSubtreeSpanHigh = child.isClosedOnSubtreeSpanHigh;
        }
      }
      child = getRight();
      if (child != null) {
        int cmp = child.subtreeSpanLow.compareTo(subtreeSpanLow);
        if (cmp < 0 || cmp == 0 && child.isClosedOnSubtreeSpanLow) {
          subtreeSpanLow = child.subtreeSpanLow;
          isClosedOnSubtreeSpanLow = child.isClosedOnSubtreeSpanLow;
        }
        cmp = child.subtreeSpanHigh.compareTo(subtreeSpanHigh);
        if (cmp > 0 || cmp == 0 && child.isClosedOnSubtreeSpanHigh) {
          subtreeSpanHigh = child.subtreeSpanHigh;
          isClosedOnSubtreeSpanHigh = child.isClosedOnSubtreeSpanHigh;
        }
      }
      this.subtreeSpanLow = subtreeSpanLow;
      this.isClosedOnSubtreeSpanLow = isClosedOnSubtreeSpanLow;
      this.subtreeSpanHigh = subtreeSpanHigh;
      this.isClosedOnSubtreeSpanHigh = isClosedOnSubtreeSpanHigh;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node<U, T> getLeft() {
      return (Node<U, T>) super.getLeft();
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

    public U getSubtreeSpanHigh() {
      return subtreeSpanHigh;
    }

    public U getSubtreeSpanLow() {
      return subtreeSpanLow;
    }

    public boolean isClosedOnSubtreeSpanHigh() {
      return isClosedOnSubtreeSpanHigh;
    }

    public boolean isClosedOnSubtreeSpanLow() {
      return isClosedOnSubtreeSpanLow;
    }
  }
}
