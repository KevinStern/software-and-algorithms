package blogspot.software_and_algorithms.stern_library.string;

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
 * Test class for DamerauLevenshteinAlgorithm.
 * 
 * @author Kevin L. Stern
 */
public class DamerauLevenshteinAlgorithmTest {
  @Test
  public void test() {
    Assertions.assertEquals(7, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("NawKtYu", ""));

    Assertions.assertEquals(7, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("", "NawKtYu"));

    Assertions.assertEquals(0, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("NawKtYu", "NawKtYu"));

    Assertions.assertEquals(6, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("NawKtYu", "tKNwYua"));

    Assertions.assertEquals(1, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("Jdc", "dJc"));

    Assertions.assertEquals(5, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("sUzSOwx", "zsSxUwO"));

    Assertions.assertEquals(7, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("eOqoHAta", "tAeaqHoO"));

    Assertions.assertEquals(1, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("glSbo", "lgSbo"));

    Assertions.assertEquals(4, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("NJtQKcJE", "cJEtQKJN"));

    Assertions.assertEquals(5, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("GitIEVs", "EGItVis"));

    Assertions.assertEquals(4, new DamerauLevenshteinAlgorithm(1, 1, 1, 1)
        .execute("MiWK", "WKiM"));
  }

  @Test
  public void testCosts() {
    /*
     * Test replace cost.
     */
    Assertions.assertEquals(1, new DamerauLevenshteinAlgorithm(100, 100, 1, 100)
        .execute("a", "b"));
    /*
     * Test swap cost.
     */
    Assertions.assertEquals(200,
                        new DamerauLevenshteinAlgorithm(100, 100, 100, 200)
                            .execute("ab", "ba"));
    /*
     * Test delete cost.
     */
    Assertions.assertEquals(1, new DamerauLevenshteinAlgorithm(1, 100, 100, 100)
        .execute("aa", "a"));
    /*
     * Test insert cost.
     */
    Assertions.assertEquals(1, new DamerauLevenshteinAlgorithm(100, 1, 100, 100)
        .execute("a", "aa"));
  }

  @Test
  public void testInvalidCosts() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new DamerauLevenshteinAlgorithm(1, 1, 1, 0);
    });
  }
}
