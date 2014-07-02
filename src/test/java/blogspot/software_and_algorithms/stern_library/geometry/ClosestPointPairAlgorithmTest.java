package blogspot.software_and_algorithms.stern_library.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
 * Test class for ClosestPointPairAlgorithm.
 * 
 * @author Kevin L. Stern
 */
public class ClosestPointPairAlgorithmTest {
  private static Point2D[] sort(Point2D[] points) {
    Arrays.sort(points, new Comparator<Point2D>() {
      @Override
      public int compare(Point2D o1, Point2D o2) {
        double d = o1.getX() - o2.getX();
        if (d == 0) {
          d = o1.getY() - o2.getY();
        }
        return d < 0 ? -1 : d > 0 ? 1 : 0;
      }
    });
    return points;
  }

  @Test
  public void testBaseCase2Points() {
    Point2D p1 = new Point2D.Double(.49, .5);
    Point2D p2 = new Point2D.Double(.5, .2);

    List<Point2D> list = new ArrayList<Point2D>();
    list.add(p1);
    list.add(p2);
    Assert.assertTrue(Arrays.equals(new Point2D[] { p1, p2 },
                                    sort(new ClosestPointPairAlgorithm(list)
                                        .execute())));

  }

  @Test
  public void testBaseCase3Points() {
    Point2D p1 = new Point2D.Double(.49, .5);
    Point2D p2 = new Point2D.Double(.5, .2);
    Point2D p3 = new Point2D.Double(.51, .5);

    List<Point2D> list = new ArrayList<Point2D>();
    list.add(p1);
    list.add(p2);
    list.add(p3);
    Assert.assertTrue(Arrays.equals(new Point2D[] { p1, p3 },
                                    sort(new ClosestPointPairAlgorithm(list)
                                        .execute())));

  }

  @Test
  public void testBasic() {
    Point2D p1 = new Point2D.Double(.49, .5);
    Point2D p2 = new Point2D.Double(.51, .5);

    List<Point2D> list = new ArrayList<Point2D>();
    list.add(p1);
    list.add(p2);
    list.add(new Point2D.Double(.6, .5));
    list.add(new Point2D.Double(.7, .5));
    list.add(new Point2D.Double(.8, .5));
    Assert.assertTrue(Arrays.equals(new Point2D[] { p1, p2 },
                                    sort(new ClosestPointPairAlgorithm(list)
                                        .execute())));
  }

  @Test
  public void testSplitMergeProcedure() {
    Point2D p1 = new Point2D.Double(.49, .5);
    Point2D p2 = new Point2D.Double(.51, .5);

    List<Point2D> list = new ArrayList<Point2D>();
    list.add(p1);
    list.add(p2);
    list.add(new Point2D.Double(.5, .2));
    list.add(new Point2D.Double(.4, .5));
    list.add(new Point2D.Double(.6, .5));
    Assert.assertTrue(Arrays.equals(new Point2D[] { p1, p2 },
                                    sort(new ClosestPointPairAlgorithm(list)
                                        .execute())));
  }
}
