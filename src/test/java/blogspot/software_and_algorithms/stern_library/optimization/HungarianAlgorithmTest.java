package blogspot.software_and_algorithms.stern_library.optimization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
 * Test class for HungarianAlgorithm.
 * 
 * @author Kevin L. Stern
 */
public class HungarianAlgorithmTest {
  private static double computeCost(double[][] matrix, int[] match) {
    double result = 0;
    Set<Integer> visited = new HashSet<Integer>();
    for (int i = 0; i < matrix.length; i++) {
      if (match[i] == -1) {
        continue;
      }
      if (!visited.add(match[i])) {
        Assertions.fail();
      }
      result += matrix[i][match[i]];
    }
    return result;
  }

  @Test
  public void test1() {
    double[][] matrix = new double[][] { new double[] { 4.0, 1.5, 4.0 },
        new double[] { 4.0, 4.5, 6.0 }, new double[] { 3.0, 2.25, 3.0 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 1, 0, 2 }, match));
    Assertions.assertEquals(8.5, computeCost(matrix, match), 0.0000001);
  }

  @Test
  public void test2() {
    double[][] matrix = new double[][] { new double[] { 1.0, 1.0, 0.8 },
        new double[] { 0.9, 0.8, 0.1 }, new double[] { 0.9, 0.7, 0.4 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 0, 2, 1 }, match));
    Assertions.assertEquals(1.8, computeCost(matrix, match), 0.0000001);
  }

  @Test
  public void test3() {
    double[][] matrix = new double[][] { new double[] { 6.0, 0.0, 7.0, 5.0 },
        new double[] { 2.0, 6.0, 2.0, 6.0 },
        new double[] { 2.0, 7.0, 2.0, 1.0 },
        new double[] { 9.0, 4.0, 7.0, 1.0 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 1, 0, 2, 3 }, match));
    Assertions.assertEquals(5, computeCost(matrix, match), 0.0000001);
  }

  @Test
  public void testInvalidInput() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3 } });
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3, Double.POSITIVE_INFINITY } });
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3, 1.0 / 0.0 } });
    });
    Assertions.assertThrows(NullPointerException.class, () -> {
      new HungarianAlgorithm(null);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3, 4 } }, -0.001);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3, 4 } }, Double.NaN);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new HungarianAlgorithm(new double[][] { new double[] { 1, 2 },
          new double[] { 3, 4 } }, Double.POSITIVE_INFINITY);
    });
  }

  @Test
  public void testEpsilon() {
    double[][] matrix = new double[][] {
        new double[] { 0.1 + 0.2, 0.5 },
        new double[] { 0.3, 0.4 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix, 1e-9);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 0, 1 }, match)
        || Arrays.equals(new int[] { 1, 0 }, match));
    Assertions.assertEquals(0.7, computeCost(matrix, match), 0.0000001);
  }

  @Test
  public void testUnassignedJob() {
    double[][] matrix = new double[][] {
        new double[] { 6.0, 0.0, 7.0, 5.0, 2.0 },
        new double[] { 2.0, 6.0, 2.0, 6.0, 7.0 },
        new double[] { 2.0, 7.0, 2.0, 1.0, 1.0 },
        new double[] { 9.0, 4.0, 7.0, 1.0, 0.0 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 1, 0, 3, 4 }, match));
    Assertions.assertEquals(3, computeCost(matrix, match), 0.0000001);
  }

  @Test
  public void testUnassignedWorker() {
    double[][] matrix = new double[][] { new double[] { 6.0, 0.0, 7.0, 5.0 },
        new double[] { 2.0, 6.0, 2.0, 6.0 },
        new double[] { 2.0, 7.0, 2.0, 1.0 },
        new double[] { 9.0, 4.0, 7.0, 1.0 },
        new double[] { 0.0, 0.0, 0.0, 0.0 } };
    HungarianAlgorithm b = new HungarianAlgorithm(matrix);
    int[] match = b.execute();
    Assertions.assertTrue(Arrays.equals(new int[] { 1, -1, 2, 3, 0 }, match));
    Assertions.assertEquals(3, computeCost(matrix, match), 0.0000001);
  }
}
