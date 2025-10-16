/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DoubleValueBin implements Serializable, Comparable<DoubleValueBin>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3056734259431827963L;
	private Range limits;
	private DescriptiveStatistics stats;
	private double window;

	public DoubleValueBin(double firstValue, double window) {
		super();

		this.window = window;
		stats = new DescriptiveStatistics();
		stats.addValue(firstValue);
		limits = new Range(firstValue - window, firstValue + window);
	}

	public boolean addValue(double newValue) {

		if(limits.contains(newValue)) {
			stats.addValue(newValue);
			limits = new Range(stats.getPercentile(50.0d) - window, stats.getPercentile(50.0d) + window);
			return true;
		}
		else
			return false;
	}

	public double[] getValues() {
		return stats.getValues();
	}

	public DescriptiveStatistics getStatistics() {
		return stats;
	}

	@Override
	public int compareTo(DoubleValueBin o) {
		return Double.compare(stats.getPercentile(50.0d), o.getStatistics().getPercentile(50.0d));
	}
}














