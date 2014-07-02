package blogspot.software_and_algorithms.stern_library.string;

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
 * Test class for KnuthMorrisPrattAlgorithm.
 * 
 * @author Kevin L. Stern
 */
public class KnuthMorrisPrattAlgorithmTest {
  @Test
  public void test1() {
    String needle = "needle";
    String haystack = "It's like searching for a needle in a haystack.";
    Assert
        .assertEquals(haystack.indexOf(needle), new KnuthMorrisPrattAlgorithm(
            needle).execute(haystack));
  }

  @Test
  public void test2() {
    String needle = "01012";
    String haystack = "010101012";
    Assert
        .assertEquals(haystack.indexOf(needle), new KnuthMorrisPrattAlgorithm(
            needle).execute(haystack));
  }

  @Test
  public void test3() {
    String needle = "0101";
    String haystack = "0102020101";
    Assert
        .assertEquals(haystack.indexOf(needle), new KnuthMorrisPrattAlgorithm(
            needle).execute(haystack));
  }

  @Test
  public void test4() {
    String needle = "aaaaaaa";
    String haystack = "aaaaaab";
    Assert
        .assertEquals(haystack.indexOf(needle), new KnuthMorrisPrattAlgorithm(
            needle).execute(haystack));
  }

  @Test
  public void test5() {
    String needle = "aaaaaaa";
    String haystack = "aaaaaaa";
    Assert.assertEquals(haystack.indexOf(needle, 1),
                        new KnuthMorrisPrattAlgorithm(needle).execute(haystack,
                                                                      1));
  }

  @Test
  public void test6() {
    String needle = "aa";
    String haystack = "aaaaaaa";
    Assert.assertEquals(haystack.indexOf(needle, 1),
                        new KnuthMorrisPrattAlgorithm(needle).execute(haystack,
                                                                      1));
  }
}
