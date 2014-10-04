package blogspot.software_and_algorithms.stern_library.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
 * An implementation of a divide-and-conquer algorithm for computing the closest
 * pair of elements of a set of points. The algorithm consists of constructing
 * an ordered list of points, then recursively dividing the list into a left and
 * right sublist towards finding the closest point pairs for each sublist. The
 * two sub-results are merged by selecting the optimal among them and all closer
 * point pairs that cross the boundary of separation. Happily, only a linear
 * amount of work is required to find all closer point pairs that cross the
 * boundary, giving a total runtime of O(n*log(n)) for the algorithm.
 * 
 * @author Kevin L. Stern
 */
public class ClosestPointPairAlgorithm {
  /**
   * Find the closest pair of points among p1, p2 and p3.
   */
  private static PairStructure closestPair(Point2D p1, Point2D p2, Point2D p3) {
    double d1 = p1.distanceSq(p2);
    double d2 = p2.distanceSq(p3);
    double d3 = p1.distanceSq(p3);
    if (d1 < d2) {
      if (d1 < d3) {
        return new PairStructure(p1, p2, d1);
      } else {
        return new PairStructure(p1, p3, d3);
      }
    } else {
      if (d2 < d3) {
        return new PairStructure(p2, p3, d2);
      } else {
        return new PairStructure(p1, p3, d3);
      }
    }
  }

  private List<Point2D> pointsOrderedByXCoordinate, pointsOrderedByYCoordinate;

  /**
   * Construct an instance of the algorithm for the specified point Collection.
   * 
   * @param points
   *          the Collection of points through which to search for the closest
   *          pair.
   */
  public ClosestPointPairAlgorithm(Collection<Point2D> points) {
    if (points == null) {
      throw new NullPointerException("points is null");
    }
    if (points.size() < 2) {
      throw new IllegalArgumentException("points is too small");
    }
    pointsOrderedByXCoordinate = new ArrayList<Point2D>(points);
    Collections.sort(pointsOrderedByXCoordinate, (o1, o2) -> {
      double delta = o1.getX() - o2.getX();
      if (delta == 0.0) {
        delta = o1.getY() - o2.getY();
      }
      return delta < 0 ? -1 : delta > 0 ? 1 : 0;
    });
    pointsOrderedByYCoordinate = new ArrayList<Point2D>(points);
    Collections.sort(pointsOrderedByYCoordinate, (o1, o2) -> {
      double delta = o1.getY() - o2.getY();
      if (delta == 0.0) {
        delta = o1.getX() - o2.getX();
      }
      return delta < 0 ? -1 : delta > 0 ? 1 : 0;
    });
  }

  /**
   * Internal helper method which implements the closest point pair algorithm.
   * 
   * @param low
   *          the starting index, inclusive, of the sublist in which to search
   *          for the closest point pair.
   * @param high
   *          the ending index, exclusive, of the sublist in which to search for
   *          the closest point pair.
   * @param localPointsOrderedByYCoordinate
   *          the points from the target sublist, ordered by y coordinate.
   * @return a PairStructure containing the closest point pair among elements of
   *         the target sublist.
   */
  protected PairStructure closestPair(int low, int high,
      List<Point2D> localPointsOrderedByYCoordinate) {
    int size = high - low;
    if (size == 3) {
      return closestPair(
          pointsOrderedByXCoordinate.get(low),
          pointsOrderedByXCoordinate.get(low + 1),
          pointsOrderedByXCoordinate.get(low + 2));
    } else if (size == 2) {
      Point2D p1 = pointsOrderedByXCoordinate.get(low);
      Point2D p2 = pointsOrderedByXCoordinate.get(low + 1);
      return new PairStructure(p1, p2, p1.distanceSq(p2));
    }

    int mid = (low >> 1) + (high >> 1) /* low / 2 + high / 2 */;
    Set<Point2D> leftSubtreeMemberSet = new HashSet<Point2D>(mid - low);
    for (int j = low; j < mid; j++) {
      leftSubtreeMemberSet.add(pointsOrderedByXCoordinate.get(j));
    }

    /*
     * Construct the lists of points ordered by y coordinate for the left and
     * right subtrees in linear time by drawing upon the master list of points
     * ordered by y coordinate.
     */
    List<Point2D> leftPointsOrderedByYCoordinate = new ArrayList<Point2D>(mid
        - low);
    List<Point2D> rightPointsOrderedByYCoordinate = new ArrayList<Point2D>(high
        - mid);
    for (Point2D next : localPointsOrderedByYCoordinate) {
      if (leftSubtreeMemberSet.contains(next)) {
        leftPointsOrderedByYCoordinate.add(next);
      } else {
        rightPointsOrderedByYCoordinate.add(next);
      }
    }

    PairStructure leftSubtreeResult = closestPair(
        low,
        mid,
        leftPointsOrderedByYCoordinate);
    PairStructure rightSubtreeResult = closestPair(
        mid,
        high,
        rightPointsOrderedByYCoordinate);
    PairStructure result = leftSubtreeResult.distanceSq < rightSubtreeResult.distanceSq
        ? leftSubtreeResult
        : rightSubtreeResult;

    List<Point2D> boundaryPointsOrderedByYCoordinate = new ArrayList<Point2D>();
    double midXCoordinate = pointsOrderedByXCoordinate.get(mid).getX();
    for (Point2D next : localPointsOrderedByYCoordinate) {
      double v = next.getX() - midXCoordinate;
      if (v * v < result.distanceSq) {
        boundaryPointsOrderedByYCoordinate.add(next);
      }
    }
    for (int i = 0; i < boundaryPointsOrderedByYCoordinate.size(); ++i) {
      Point2D currentPoint = boundaryPointsOrderedByYCoordinate.get(i);
      int index;
      for (int j = 1; (index = i + j) < boundaryPointsOrderedByYCoordinate
          .size(); ++j) {
        Point2D testPoint = boundaryPointsOrderedByYCoordinate.get(index);
        /*
         * The number of points that can be situated within the boundary so that
         * their y coordinate is within the minimum of the result distances for
         * the left and right subtrees from currentPoint.getY() is bounded by a
         * constant, since that distance value spatially limits the number of
         * points that can be packed near one another on each side of the
         * boundary.
         */
        double v = testPoint.getY() - currentPoint.getY();
        if (v * v >= result.distanceSq) {
          break;
        }
        double testDistance = currentPoint.distanceSq(testPoint);
        if (testDistance < result.distanceSq) {
          result = new PairStructure(currentPoint, testPoint, testDistance);
        }
      }
    }

    return result;
  }

  /**
   * Execute the algorithm.
   * 
   * @return a Point2D[] containing exactly two elements which are the closest
   *         pair of points among those in the collection used to construct this
   *         instance.
   */
  public Point2D[] execute() {
    PairStructure result = closestPair(
        0,
        pointsOrderedByXCoordinate.size(),
        pointsOrderedByYCoordinate);
    return new Point2D[] { result.p1, result.p2 };
  }

  /**
   * Convenience data structure to hold a pair of points along with their
   * distance from one another.
   */
  protected static class PairStructure {
    private Point2D p1, p2;
    private double distanceSq;

    /**
     * Constructor.
     * 
     * @param p1
     *          the first point.
     * @param p2
     *          the second point.
     * @param distanceSq
     *          the distance between p1 and p2, squared.
     */
    public PairStructure(Point2D p1, Point2D p2, double distanceSq) {
      this.p1 = p1;
      this.p2 = p2;
      this.distanceSq = distanceSq;
    }
  }
}
