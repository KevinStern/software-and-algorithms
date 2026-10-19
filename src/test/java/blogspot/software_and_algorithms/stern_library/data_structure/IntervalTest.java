package blogspot.software_and_algorithms.stern_library.data_structure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
 * Test class for Interval.
 * 
 * @author Kevin L. Stern
 */
public class IntervalTest {
  @Test
  public void testCompareTo() {
    Interval<Double> i1 = new Interval<Double>(0.0, true, 10.0, true);
    Interval<Double> i2 = new Interval<Double>(0.0, true, 10.0, false);
    Interval<Double> i3 = new Interval<Double>(0.0, false, 10.0, true);
    Interval<Double> i4 = new Interval<Double>(1.0, true, 10.0, true);
    Interval<Double> i5 = new Interval<Double>(0.0, true, 11.0, true);

    Assertions.assertTrue(i1.compareTo(i1) == 0);

    Assertions.assertTrue(i1.compareTo(i2) < 0);
    Assertions.assertTrue(i1.compareTo(i3) < 0);
    Assertions.assertTrue(i1.compareTo(i4) < 0);
    Assertions.assertTrue(i1.compareTo(i5) < 0);

    Assertions.assertTrue(i2.compareTo(i1) > 0);
    Assertions.assertTrue(i3.compareTo(i1) > 0);
    Assertions.assertTrue(i4.compareTo(i1) > 0);
    Assertions.assertTrue(i5.compareTo(i1) > 0);
  }

  @Test
  public void testContainsInterval() {
    Interval<Double> i1 = new Interval<Double>(0.0, true, 10.0, true);
    Interval<Double> i2 = new Interval<Double>(0.0, true, 10.0, false);
    Interval<Double> i3 = new Interval<Double>(0.0, false, 10.0, true);
    Interval<Double> i4 = new Interval<Double>(0.0, false, 10.0, false);
    Interval<Double> i5 = new Interval<Double>(1.0, true, 9.0, true);

    Assertions.assertTrue(i1.contains(i1));
    Assertions.assertTrue(i1.contains(i2));
    Assertions.assertTrue(i1.contains(i3));
    Assertions.assertTrue(i1.contains(i4));
    Assertions.assertTrue(i1.contains(i5));

    Assertions.assertFalse(i2.contains(i1));
    Assertions.assertFalse(i3.contains(i1));
    Assertions.assertFalse(i4.contains(i1));
    Assertions.assertFalse(i5.contains(i1));
  }

  @Test
  public void testContainsPoint() {
    Interval<Double> i;
    i = new Interval<Double>(0.0, true, 10.0, true);
    for (int j = 0; j < 10; j++) {
      Assertions.assertTrue(i.contains((double) j));
    }
    i = new Interval<Double>(0.0, false, 10.0, true);
    Assertions.assertFalse(i.contains((double) 0));
    for (int j = 1; j < 10; j++) {
      Assertions.assertTrue(i.contains((double) j));
    }
    i = new Interval<Double>(0.0, false, 10.0, false);
    Assertions.assertFalse(i.contains((double) 0));
    Assertions.assertFalse(i.contains((double) 10));
    for (int j = 1; j < 9; j++) {
      Assertions.assertTrue(i.contains((double) j));
    }
  }

  @Test
  public void testEquals() {
    Interval<Double> i1 = new Interval<Double>(0.0, true, 10.0, true);
    Interval<Double> i2 = new Interval<Double>(0.0, true, 10.0, false);
    Interval<Double> i3 = new Interval<Double>(0.0, false, 10.0, true);
    Interval<Double> i4 = new Interval<Double>(0.0, false, 10.0, false);
    Interval<Double> i5 = new Interval<Double>(1.0, true, 9.0, true);

    Assertions.assertTrue(i1.equals(new Interval<Double>(i1.getLow(), i1
        .isClosedOnLow(), i1.getHigh(), i1.isClosedOnHigh())));

    Assertions.assertFalse(i1.equals(i2));
    Assertions.assertFalse(i1.equals(i3));
    Assertions.assertFalse(i1.equals(i4));
    Assertions.assertFalse(i1.equals(i5));

    Assertions.assertFalse(i2.equals(i1));
    Assertions.assertFalse(i3.equals(i1));
    Assertions.assertFalse(i4.equals(i1));
    Assertions.assertFalse(i5.equals(i1));
  }

  @Test
  public void testHashcode() {
    Interval<Double> i = new Interval<Double>(0.0, true, 10.0, true);
    Assertions.assertFalse(i.hashCode() == new Interval<Double>(1.0, true, 10.0,
        true).hashCode());
    Assertions.assertFalse(i.hashCode() == new Interval<Double>(0.0, true, 11.0,
        true).hashCode());
    Assertions.assertFalse(i.hashCode() == new Interval<Double>(0.0, true, 10.0,
        false).hashCode());
    Assertions.assertFalse(i.hashCode() == new Interval<Double>(0.0, false, 10.0,
        true).hashCode());
    Assertions.assertFalse(i.hashCode() == new Interval<Double>(0.0, false, 10.0,
        false).hashCode());
  }

  @Test
  public void testOverlaps() {
    Interval<Double> i1 = new Interval<Double>(5.0, true, 10.0, true);
    Interval<Double> i2 = new Interval<Double>(10.0, true, 15.0, true);
    Interval<Double> i3 = new Interval<Double>(10.0, false, 15.0, true);
    Interval<Double> i4 = new Interval<Double>(0.0, true, 5.0, true);
    Interval<Double> i5 = new Interval<Double>(0.0, true, 5.0, false);
    Interval<Double> i6 = new Interval<Double>(6.0, false, 9.0, false);

    Assertions.assertTrue(i1.overlaps(i1));

    Assertions.assertTrue(i1.overlaps(i2));
    Assertions.assertFalse(i1.overlaps(i3));
    Assertions.assertTrue(i1.overlaps(i4));
    Assertions.assertFalse(i1.overlaps(i5));
    Assertions.assertTrue(i1.overlaps(i6));

    Assertions.assertTrue(i2.overlaps(i1));
    Assertions.assertFalse(i3.overlaps(i1));
    Assertions.assertTrue(i4.overlaps(i1));
    Assertions.assertFalse(i5.overlaps(i1));
    Assertions.assertTrue(i6.overlaps(i1));
  }

  @Test
  public void testToString() {
    Assertions.assertEquals("(0, 1)", new Interval(0, false, 1, false).toString());
    Assertions.assertEquals("(0, 1]", new Interval(0, false, 1, true).toString());
    Assertions.assertEquals("[0, 1)", new Interval(0, true, 1, false).toString());
    Assertions.assertEquals("[0, 1]", new Interval(0, true, 1, true).toString());
  }
}
