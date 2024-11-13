/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 * 
 * Modified from MZMine2
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.Serializable;
import java.text.NumberFormat;

import umich.ms.util.DoubleRange;

/**
 * This class represents a range of doubles.
 */
public class Range implements Serializable, Comparable<Range> {

	/**
	 *
	 */
	private static final long serialVersionUID = 3763295574749359374L;
	private double min, max;

	/**
	 * Create a range with only one value, representing both minimum and
	 * maximum. Such range can later be extended using extendRange().
	 * 
	 * @param minAndMax
	 *            Range minimum and maximum
	 */
	public Range(double minAndMax) {
		this(minAndMax, minAndMax);
	}

	/**
	 * Create a range from min to max.
	 * 
	 * @param min Range minimum
	 * @param max Range maximum
	 */
	public Range(double min, double max) {
		
		if (min > max) 
			throw (new IllegalArgumentException(
					"Range minimum (" + min + ") must be <= maximum (" + max + ")"));
		
		this.min = min;
		this.max = max;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param range Range to copy
	 */
	public Range(Range range) {
		this(range.getMin(), range.getMax());
	}

	public Range(DoubleRange mzRange) {
		this(mzRange.getLo(), mzRange.getHi());
	}
	
	public Range(org.jfree.data.Range mzRange) {
		this(mzRange.getLowerBound(), mzRange.getUpperBound());
	}

	/**
	 * Compares two Ranges
	 */
	public int compareWidth(Range range2) {
		
		Double value1 = getSize();
		Double value2 = range2.getSize();

		return value1.compareTo(value2);
	}

	/**
	 * Returns true if this range contains given value.
	 * 
	 * @param value Value to check
	 * @return True if range contains this value
	 */
	public boolean contains(double value) {
		return ((min <= value) && (max >= value));
	}
	
	public boolean containsExcludingUpperBorder(double value) {
		return ((min <= value) && (max > value));
	}

	/**
	 * Returns true if this range contains the whole given range as a subset.
	 * 
	 * @param checkMin Minimum of given range
	 * @param checkMax Maximum of given range
	 * @return True if this range contains given range
	 */
	public boolean containsRange(double checkMin, double checkMax) {
		return ((checkMin >= min) && (checkMax <= max));
	}

	/**
	 * Returns true if this range contains the whole given range as a subset.
	 * 
	 * @param checkRange Given range
	 * @return True if this range contains given range
	 */
	public boolean containsRange(Range checkRange) {
		return containsRange(checkRange.getMin(), checkRange.getMax());
	}

	/**
	 * Extends this range (if necessary) to include the given value
	 * 
	 * @param value Value to extends this range
	 */
	public void extendRange(double value) {
		if (min > value)
			min = value;
		if (max < value)
			max = value;
	}

	/**
	 * Extends this range (if necessary) to include the given range
	 * 
	 * @param extension
	 *            Range to extends this range
	 */
	public void extendRange(Range extension) {
		if (min > extension.getMin())
			min = extension.getMin();
		if (max < extension.getMax())
			max = extension.getMax();
	}

	/**
	 * Returns the average point of this range.
	 * 
	 * @return Average
	 */
	public double getAverage() {
		return ((min + max) / 2);
	}

	/**
	 * @return Range maximum
	 */
	public double getMax() {
		return max;
	}

	/**
	 * @return Range minimun
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Returns the size of this range.
	 * 
	 * @return Size of this range
	 */
	public double getSize() {
		return (max - min);
	}

	/**
	 * Returns true if this range lies within the given range.
	 * 
	 * @param checkMin Minimum of given range
	 * @param checkMax Maximum of given range
	 * @return True if this range lies within given range
	 */
	public boolean isWithin(double checkMin, double checkMax) {
		return ((checkMin <= min) && (checkMax >= max));
	}

	/**
	 * Returns true if this range lies within the given range.
	 * 
	 * @param checkRange Given range
	 * @return True if this range lies within given range
	 */
	public boolean isWithin(Range checkRange) {
		return isWithin(checkRange.getMin(), checkRange.getMax());
	}

	public boolean overlapsWith(Range checkRange) {

		if (this.contains(checkRange.getMin()) || this.contains(checkRange.getMax()))
			return true;
		else
			return false;
	}
	
	public boolean overlapsWith(Range checkRange, double fractionOverlap) {

		if (!overlapsWith(checkRange))
			return false;

		if(isWithin(checkRange) || checkRange.isWithin(this))
			return true;
		
		double overlapSize = 0.0d;
		if(max >= checkRange.getMin() && min <= checkRange.getMin())
			overlapSize = max - checkRange.getMin();
			
		if(min < checkRange.getMax() && max > checkRange.getMax())
			overlapSize = checkRange.getMax() - min;
		
		if((overlapSize / this.getSize()) > fractionOverlap || 
				(overlapSize / checkRange.getSize()) > fractionOverlap)
			return true;
		else 
			return false;
	}

	/**
	 * Returns the String representation. We use the '~' character for
	 * separation, not '-', to avoid ranges like 1E-1-2E-1.
	 * 
	 * @return This range as string
	 */
	public String toString() {
		return String.valueOf(min) + " ~ " + String.valueOf(max);
	}
	
	public String getFormattedString(NumberFormat numFormat) {
		return numFormat.format(min) + " ~ " + numFormat.format(max);
	}
	
    @Override
    public boolean equals(Object obj) {
    	
        if (obj == null)
            return false;
        
        if (!Range.class.isAssignableFrom(obj.getClass()))
            return false;
        
        final Range other = (Range) obj;
        
        if(min != other.getMin() || max != other.getMax())  
        	return false;
        
		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {
    	
        int hash = 3;
        hash = 53 * hash + (this.toString() != null ? this.toString().hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(Range o) {		
		return Double.compare(this.getAverage(), o.getAverage());
	}
	
	public String getStorableString() {
		return MsUtils.spectrumMzExportFormat.format(min) + "_" + MsUtils.spectrumMzExportFormat.format(max);
	}
	
	public Range(String rtRangeString) {
		if(rtRangeString == null || rtRangeString.isEmpty() || !rtRangeString.contains("_"))
			throw (new IllegalArgumentException("Invalid string"));
		
		String[]parts = rtRangeString.split("_");
		if(parts.length != 2)
			throw (new IllegalArgumentException("Invalid string"));
		
		min = Double.parseDouble(parts[0]);
		max = Double.parseDouble(parts[1]);
		if(min > max) 
			throw (new IllegalArgumentException("Range minimum (" + min + ") must be <= maximum (" + max + ")"));
	}
}




