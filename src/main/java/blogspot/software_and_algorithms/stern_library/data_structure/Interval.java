package blogspot.software_and_algorithms.stern_library.data_structure;

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
 * An <em>interval</em> is the subset of elements which fall between (with
 * respect to a total order) two endpoint elements of a set. An interval that
 * contains its endpoints is <em>closed</em>, an interval that contains one of
 * its endpoints but not the other is <em>half open</em> and an interval that
 * does not contain either of its endpoints is <em>open</em>. This class
 * encapsulates the concept of an interval and uses a class's natural order.
 * 
 * @author Kevin L. Stern
 */
public class Interval<T extends Comparable<T>> implements
		Comparable<Interval<T>> {
	private T low, high;
	private boolean isClosedOnLow, isClosedOnHigh;
	private int hashCode = 0;

	/**
	 * Construct a new instance with the specified low and high endpoints.
	 * 
	 * @param low
	 *            the low endpoint.
	 * @param isClosedOnLow
	 *            true if this interval contains its low endpoint, false
	 *            otherwise.
	 * @param high
	 *            the high endpoint.
	 * @param isClosedOnHigh
	 *            true if this interval contains its high endpoint, false
	 *            otherwise.
	 */
	public Interval(T low, boolean isClosedOnLow, T high, boolean isClosedOnHigh) {
		if (low == null) {
			throw new NullPointerException("low endpoint is null");
		} else if (high == null) {
			throw new NullPointerException("high endpoint is null");
		}
		this.low = low;
		this.isClosedOnLow = isClosedOnLow;
		this.high = high;
		this.isClosedOnHigh = isClosedOnHigh;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Interval<T> o) {
		int result = low.compareTo(o.low);
		if (result == 0) {
			if (isClosedOnLow != o.isClosedOnLow) {
				result = isClosedOnLow ? -1 : 1;
			} else {
				result = high.compareTo(o.high);
				if (result == 0) {
					if (isClosedOnHigh != o.isClosedOnHigh) {
						result = isClosedOnHigh ? -1 : 1;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Test whether or not this interval contains the specified interval. An
	 * interval is contained by another precisely when all of its values are
	 * contained by the other.
	 * 
	 * @param interval
	 *            the query interval, non-null.
	 * @return true if this interval contains the specified interval, false
	 *         otherwise.
	 */
	public boolean contains(Interval<T> interval) {
		boolean lowIsLowerBound = low.equals(interval.low)
				&& (isClosedOnLow || !interval.isClosedOnLow)
				|| low.compareTo(interval.low) < 0;
		boolean highIsUpperBound = high.equals(interval.high)
				&& (isClosedOnHigh || !interval.isClosedOnHigh)
				|| high.compareTo(interval.high) > 0;
		return lowIsLowerBound && highIsUpperBound;
	}

	/**
	 * Test whether or not this interval contains the specified value.
	 * 
	 * @param value
	 *            the query value, non-null.
	 * @return true if this interval contains the specified value, false
	 *         otherwise.
	 */
	public boolean contains(T value) {
		return value.equals(low) && isClosedOnLow || value.equals(high)
				&& isClosedOnHigh || low.compareTo(value) < 0
				&& value.compareTo(high) < 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interval other = (Interval) obj;
		if (high == null) {
			if (other.high != null)
				return false;
		} else if (!high.equals(other.high))
			return false;
		if (isClosedOnHigh != other.isClosedOnHigh)
			return false;
		if (low == null) {
			if (other.low != null)
				return false;
		} else if (!low.equals(other.low))
			return false;
		if (isClosedOnLow != other.isClosedOnLow)
			return false;
		return true;
	}

	/**
	 * Get the high endpoint.
	 * 
	 * @return the high endpoint.
	 */
	public T getHigh() {
		return high;
	}

	/**
	 * Get the low endpoint.
	 * 
	 * @return the low endpoint.
	 */
	public T getLow() {
		return low;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((high == null) ? 0 : high.hashCode());
			result = prime * result + (isClosedOnHigh ? 1231 : 1237);
			result = prime * result + ((low == null) ? 0 : low.hashCode());
			result = prime * result + (isClosedOnLow ? 1231 : 1237);
			hashCode = result;
		}
		return hashCode;
	}

	/**
	 * 
	 * @return true if this interval is closed at its high endpoint, false
	 *         otherwise.
	 */
	public boolean isClosedOnHigh() {
		return isClosedOnHigh;
	}

	/**
	 * 
	 * @return true if the interval is closed at its low endpoint, false
	 *         otherwise.
	 */
	public boolean isClosedOnLow() {
		return isClosedOnLow;
	}

	/**
	 * Test whether or not this interval and the specified interval overlap. Two
	 * intervals overlap precisely when their intersection is non-empty.
	 * 
	 * @param interval
	 *            the query interval.
	 * @return true if this interval and the specified interval overlap, false
	 *         otherwise.
	 */
	public boolean overlaps(Interval<T> interval) {
		if (interval.isClosedOnLow && contains(interval.low) || isClosedOnLow
				&& interval.contains(low)) {
			return true;
		}
		if (!interval.isClosedOnLow && low.compareTo(interval.low) <= 0
				&& interval.low.compareTo(high) < 0 || !isClosedOnLow
				&& interval.low.compareTo(low) <= 0
				&& low.compareTo(interval.high) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String format;
		if (isClosedOnLow) {
			if (isClosedOnHigh) {
				format = "[%s, %s]";
			} else {
				format = "[%s, %s)";
			}
		} else {
			if (isClosedOnHigh) {
				format = "(%s, %s]";
			} else {
				format = "(%s, %s)";
			}
		}
		return String.format(format, low.toString(), high.toString());
	}
}