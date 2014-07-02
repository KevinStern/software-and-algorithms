package blogspot.software_and_algorithms.stern_library.data_structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
 * A <em>static interval tree</em> is a balanced binary search tree which is
 * built to store a pre-specified set of intervals so that both point queries
 * (queries that return intervals from the set which contain a query point) and
 * overlapping interval queries (queries that return intervals from the set
 * which overlap a query interval) can be completed in time O(log(n)+k), where n
 * is the size of the pre-specified set of intervals and k is the size of the
 * result set from the query.
 * <p>
 * While this implementation of a static interval tree is built to support a
 * pre-specified set of intervals, intervals from this set may be added to and
 * removed from the tree at will, giving a semi-static nature to the tree. The
 * construction process completes in time O(n*log(n)) where n is the size of the
 * set of intervals with which the tree is built.
 * <p>
 * Insertion and deletion of intervals to and from a constructed tree completes
 * in time O(log(n)) where n is the size of the set of intervals with which the
 * tree was built.
 * <p>
 * A constructed tree consumes linear space in the size of the set of intervals
 * with which the tree was built.
 * <p>
 * Note that this implementation supports all three closed, open and half-open
 * intervals.
 * 
 * @author Kevin L. Stern
 */
public class StaticIntervalTree<U extends Comparable<U>, T extends Interval<U>> {
  private Node<U, T> root;
  private int size;

  /**
   * Default constructor.
   */
  public StaticIntervalTree() {
    size = 0;
  }

  /**
   * Internal helper method to construct a subtree structure capable of holding
   * the elements of the specified portion of the specified list of intervals.
   * 
   * @param intervalList
   *          the list of intervals with which to build the subtree; must be
   *          ordered by low endpoint.
   * @param low
   *          the low index of the portion of intervalList to consider,
   *          inclusive.
   * @param high
   *          the high index of the portion of intervalList to consider,
   *          exclusive.
   */
  private Node<U, T> buildSubtree(List<T> intervalList, int low, int high) {
    U point = intervalList.get((low + high) >>> 1).getLow();
    Node<U, T> result = new Node<U, T>(point);

    int lowPointer = low;
    int highPointer = high;
    for (int j = low; j < highPointer; j++) {
      T next = intervalList.get(j);
      if (next.getHigh().compareTo(point) < 0) {
        Collections.swap(intervalList, lowPointer++, j);
      } else if (next.getLow().compareTo(point) > 0) {
        highPointer = j;
      }
    }
    if (low < lowPointer) {
      result.setLeft(buildSubtree(intervalList, low, lowPointer));
    }
    if (highPointer < high) {
      result.setRight(buildSubtree(intervalList, highPointer, high));
    }

    return result;
  }

  /**
   * Build the interval tree to support the elements of the specified set of
   * intervals. Note that this method does not insert intervals into the tree
   * (this must be done via {@link #insert(Interval)} after the tree is built).
   * 
   * @param intervals
   *          the set of intervals for which the tree is to be built.
   */
  public void buildTree(Set<T> intervals) {
    List<T> intervalList = new ArrayList<T>(intervals);
    Collections.sort(intervalList, new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return o1.getLow().compareTo(o2.getLow());
      }
    });
    root = buildSubtree(intervalList, 0, intervals.size());
    size = 0;
  }

  /**
   * Clear the contents of the tree, leaving the tree structure intact.
   */
  public void clear() {
    if (root != null) {
      List<Node<U, T>> stack = new ArrayList<Node<U, T>>();
      stack.add(root);
      while (!stack.isEmpty()) {
        Node<U, T> next = stack.remove(stack.size() - 1);
        next.clear();
        Node<U, T> temp;
        if ((temp = next.getLeft()) != null) {
          stack.add(temp);
        }
        if ((temp = next.getRight()) != null) {
          stack.add(temp);
        }
      }
    }
    size = 0;
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
    if (interval == null) {
      return false;
    }
    Node<U, T> node = root;
    while (node != null) {
      U temp = node.getPoint();
      if (interval.getLow().compareTo(temp) <= 0
          && temp.compareTo(interval.getHigh()) <= 0) {
        if (node.delete(interval)) {
          size -= 1;
          return true;
        }
      } else if (interval.getHigh().compareTo(temp) < 0) {
        node = node.getLeft();
      } else {
        node = node.getRight();
      }
    }
    return false;
  }

  /**
   * Fetch intervals containing the specified point.
   * 
   * @param target
   *          the target Collection into which to place the desired intervals.
   * @param queryPoint
   *          the query point.
   * @return the target Collection.
   */
  public <V extends Collection<T>> V fetchContainingIntervals(V target,
                                                              U queryPoint) {
    if (target == null) {
      throw new NullPointerException("target is null");
    }
    if (queryPoint == null) {
      throw new NullPointerException("queryPoint is null");
    }
    Node<U, T> node = root;
    while (node != null) {
      U temp = node.getPoint();
      if (queryPoint.equals(temp)) {
        node.fetchIntervalsContainingNodePoint(target);
        node = null;
      } else if (queryPoint.compareTo(temp) < 0) {
        node.fetchIntervalsContainingPointLow(target, queryPoint, true);
        node = node.getLeft();
      } else {
        node.fetchIntervalsContainingPointHigh(target, queryPoint, true);
        node = node.getRight();
      }
    }
    return target;
  }

  /**
   * Fetch intervals overlapping the specified interval.
   * 
   * @param target
   *          the target Collection into which to place the desired intervals.
   * @param queryInterval
   *          the query interval.
   * @return the target Collection.
   */
  public <V extends Collection<T>> V fetchOverlappingIntervals(V target,
                                                               T queryInterval) {
    if (target == null) {
      throw new NullPointerException("target is null");
    }
    if (queryInterval == null) {
      throw new NullPointerException("queryInterval is null");
    }
    List<Node<U, T>> stack = new ArrayList<Node<U, T>>();
    if (root != null) {
      stack.add(root);
    }
    while (!stack.isEmpty()) {
      Node<U, T> node = stack.remove(stack.size() - 1);
      U temp = node.getPoint();
      if (queryInterval.getLow().compareTo(temp) <= 0
          && temp.compareTo(queryInterval.getHigh()) <= 0) {
        node.fetchOverlappingIntervals(target, queryInterval);
        if (node.getLeft() != null) {
          stack.add(node.getLeft());
        }
        if (node.getRight() != null) {
          stack.add(node.getRight());
        }
      } else if (queryInterval.getHigh().compareTo(temp) < 0) {
        node.fetchIntervalsContainingPointLow(target, queryInterval.getHigh(),
                                              queryInterval.isClosedOnHigh());
        if (node.getLeft() != null) {
          stack.add(node.getLeft());
        }
      } else {
        node.fetchIntervalsContainingPointHigh(target, queryInterval.getLow(),
                                               queryInterval.isClosedOnLow());
        if (node.getRight() != null) {
          stack.add(node.getRight());
        }
      }
    }
    return target;
  }

  /**
   * Get the number of intervals being stored in the tree.
   * 
   * @return the number of intervals being stored in the tree.
   */
  public int getSize() {
    return size;
  }

  /**
   * Insert the specified interval into this tree. Behavior is undefined when
   * the interval was not included in the set of intervals presented at the most
   * recent call to {@link #buildTree(Collection)}.
   * 
   * @param interval
   *          the interval to insert.
   * @return true if an element was inserted as a result of this call, false
   *         otherwise.
   */
  public boolean insert(T interval) {
    Node<U, T> node = root;
    while (node != null) {
      U temp = node.getPoint();
      if (interval.getLow().compareTo(temp) <= 0
          && temp.compareTo(interval.getHigh()) <= 0) {
        if (node.insert(interval)) {
          size++;
          return true;
        }
      } else if (interval.getHigh().compareTo(temp) < 0) {
        node = node.getLeft();
      } else {
        node = node.getRight();
      }
    }
    return false;
  }

  /**
   * A <em>node</em> for a static interval tree is a binary tree node augmented
   * with an associated point value and the ability to store intervals.
   * <p>
   * Intervals that are stored within this node either contain the node's point
   * value or are open at an endpoint that equals the node's point value.
   * Intervals are stored so that these two cases are easily distinguished from
   * one another: Each such class of interval is stored in two structures, one
   * is a tree sorted by low endpoint and the other is a tree sorted by high
   * endpoint. This enables efficient point queries as well as insertions and
   * deletions from the node.
   */
  protected static class Node<U extends Comparable<U>, T extends Interval<U>> {
    private RedBlackTree<T> highOrderedContainingIntervals = new OrderLinkedRedBlackTree<T>(
        new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            int result = o1.getHigh().compareTo(o2.getHigh());
            if (result == 0) {
              if (o1.isClosedOnHigh() != o2.isClosedOnHigh()) {
                result = o1.isClosedOnHigh() ? 1 : -1;
              } else {
                result = o1.compareTo(o2);
              }
            }
            return result > 0 ? -1 : result < 0 ? 1 : 0;
          }
        });
    private RedBlackTree<T> lowOrderedContainingIntervals = new OrderLinkedRedBlackTree<T>(
        new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            int result = o1.getLow().compareTo(o2.getLow());
            if (result == 0) {
              result = o1.compareTo(o2);
            }
            return result > 0 ? 1 : result < 0 ? -1 : 0;
          }
        });
    private RedBlackTree<T> highOrderedExcludingIntervals = new OrderLinkedRedBlackTree<T>(
        new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            int result = o1.getHigh().compareTo(o2.getHigh());
            if (result == 0) {
              if (o1.isClosedOnHigh() != o2.isClosedOnHigh()) {
                result = o1.isClosedOnHigh() ? 1 : -1;
              } else {
                result = o1.compareTo(o2);
              }
            }
            return result > 0 ? -1 : result < 0 ? 1 : 0;
          }
        });
    private RedBlackTree<T> lowOrderedExcludingIntervals = new OrderLinkedRedBlackTree<T>(
        new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            int result = o1.getLow().compareTo(o2.getLow());
            if (result == 0) {
              result = o1.compareTo(o2);
            }
            return result > 0 ? 1 : result < 0 ? -1 : 0;
          }
        });
    private Node<U, T> left, right;
    private U point;

    /**
     * Construct a new node associated with the specified point.
     * 
     * @param point
     *          the point with which this node is associated.
     */
    public Node(U point) {
      if (point == null) {
        throw new NullPointerException("point is null");
      }
      this.point = point;
    }

    /**
     * Clear the elements of this node.
     */
    public void clear() {
      lowOrderedContainingIntervals.clear();
      highOrderedContainingIntervals.clear();
      lowOrderedExcludingIntervals.clear();
      highOrderedExcludingIntervals.clear();
    }

    /**
     * Delete the specified interval from this node.
     * 
     * @param interval
     *          the interval to delete.
     * @return true if an element was deleted as a result of this call, false
     *         otherwise.
     */
    public boolean delete(T interval) {
      if (interval.contains(point)) {
        if (lowOrderedContainingIntervals.delete(interval) == null) {
          return false;
        }
        highOrderedContainingIntervals.delete(interval);
      } else {
        if (lowOrderedExcludingIntervals.delete(interval) == null) {
          return false;
        }
        highOrderedExcludingIntervals.delete(interval);
      }
      return true;
    }

    /**
     * Fetch all intervals from this node that contain the node's point.
     * 
     * @param target
     *          the target Collection into which to place the desired intervals.
     */
    public void fetchIntervalsContainingNodePoint(Collection<T> target) {
      if (highOrderedContainingIntervals.getSize() > 0) {
        RedBlackTree.Node<T> temp = highOrderedContainingIntervals
            .getFirstNode();
        while (temp != null) {
          target.add(temp.getValue());
          temp = highOrderedContainingIntervals.getSuccessor(temp);
        }
      }
    }

    /**
     * Fetch intervals containing the specified value. This method is called
     * when the specified value is greater than this node's point.
     * 
     * @param target
     *          the target Collection into which to place the desired intervals.
     * @param queryPoint
     *          the query value.
     * @param isClosedOnValue
     *          true if the search is inclusive of the specified value, false
     *          otherwise.
     */
    public void fetchIntervalsContainingPointHigh(Collection<T> target,
                                                  U queryPoint,
                                                  boolean isClosedOnValue) {
      for (Iterator<T> i = highOrderedContainingIntervals.iterator(); i
          .hasNext();) {
        T next = i.next();
        int cmp = next.getHigh().compareTo(queryPoint);
        if (cmp < 0 || cmp == 0 && (!isClosedOnValue || !next.isClosedOnHigh())) {
          break;
        }
        target.add(next);
      }
      for (Iterator<T> i = highOrderedExcludingIntervals.iterator(); i
          .hasNext();) {
        T next = i.next();
        int cmp = next.getHigh().compareTo(queryPoint);
        if (cmp < 0 || cmp == 0 && (!isClosedOnValue || !next.isClosedOnHigh())) {
          break;
        }
        target.add(next);
      }
    }

    /**
     * Fetch intervals containing the specified value. This method is called
     * when the specified value is less than this node's point.
     * 
     * @param target
     *          the target Collection into which to place the desired intervals.
     * @param queryPoint
     *          the query value.
     * @param isClosedOnValue
     *          true if the search is inclusive of the specified value, false
     *          otherwise.
     */
    public void fetchIntervalsContainingPointLow(Collection<T> target,
                                                 U queryPoint,
                                                 boolean isClosedOnValue) {
      for (Iterator<T> i = lowOrderedContainingIntervals.iterator(); i
          .hasNext();) {
        T next = i.next();
        int cmp = next.getLow().compareTo(queryPoint);
        if (cmp > 0 || cmp == 0 && (!isClosedOnValue || !next.isClosedOnLow()))
          break;
        target.add(next);
      }
      for (Iterator<T> i = lowOrderedExcludingIntervals.iterator(); i.hasNext();) {
        T next = i.next();
        int cmp = next.getLow().compareTo(queryPoint);
        if (cmp > 0 || cmp == 0 && (!isClosedOnValue || !next.isClosedOnLow()))
          break;
        target.add(next);
      }
    }

    /**
     * Fetch all intervals from this node which overlap the specified interval.
     * By contract, the interval must be such that {@link Interval#getLow()}
     * <code><=</code> {@link Node#getPoint()} <code><=</code>
     * {@link Interval#getHigh()}.
     * 
     * @param target
     *          the target Collection into which to place the desired intervals.
     * @param queryInterval
     *          the query interval.
     */
    public void fetchOverlappingIntervals(Collection<T> target,
                                          Interval<U> queryInterval) {
      if (queryInterval.getLow().compareTo(point) == 0) {
        fetchIntervalsContainingPointHigh(target, queryInterval.getLow(),
                                          queryInterval.isClosedOnLow());
      } else if (queryInterval.getHigh().compareTo(point) == 0) {
        fetchIntervalsContainingPointLow(target, queryInterval.getHigh(),
                                         queryInterval.isClosedOnHigh());
      } else {
        fetchIntervalsContainingNodePoint(target);
        if (highOrderedExcludingIntervals.getSize() > 0) {
          RedBlackTree.Node<T> temp = highOrderedExcludingIntervals
              .getFirstNode();
          while (temp != null) {
            target.add(temp.getValue());
            temp = highOrderedExcludingIntervals.getSuccessor(temp);
          }
        }
      }
    }

    /**
     * Get the left child.
     * 
     * @return the left child, null if none exists.
     */
    public Node<U, T> getLeft() {
      return left;
    }

    /**
     * Get the point associated with this node.
     * 
     * @return the point associated with this node.
     */
    public U getPoint() {
      return point;
    }

    /**
     * Get the right child.
     * 
     * @return the right child, null if none exists.
     */
    public Node<U, T> getRight() {
      return right;
    }

    /**
     * Insert the specified interval into this node. By contract, the interval
     * must be such that {@link Interval#getLow()} <code><=</code>
     * {@link Node#getPoint()} <code><=</code> {@link Interval#getHigh()}.
     * 
     * @param interval
     *          the interval to insert.
     * @return true if an element was inserted as a result of this call, false
     *         otherwise.
     */
    private boolean insert(T interval) {
      if (interval.contains(point)) {
        if (lowOrderedContainingIntervals.insert(interval) == null) {
          return false;
        }
        highOrderedContainingIntervals.insert(interval);
      } else {
        if (lowOrderedExcludingIntervals.insert(interval) == null) {
          return false;
        }
        highOrderedExcludingIntervals.insert(interval);
      }
      return true;
    }

    /**
     * Set the left child to the specified node.
     * 
     * @param node
     *          the left child.
     */
    private void setLeft(Node<U, T> node) {
      left = node;
    }

    /**
     * Set the right child to the specified node.
     * 
     * @param node
     *          the right child.
     */
    private void setRight(Node<U, T> node) {
      right = node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("{");
      for (Iterator<T> i = lowOrderedContainingIntervals.iterator();;) {
        builder.append(i.next().toString());
        if (i.hasNext()) {
          builder.append(", ");
        } else {
          break;
        }
      }
      for (Iterator<T> i = lowOrderedExcludingIntervals.iterator(); i.hasNext();) {
        builder.append(", ").append(i.next().toString());
      }
      builder.append("}");
      return builder.toString();
    }
  }
}