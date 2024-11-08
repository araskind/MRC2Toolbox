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
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class PlotValuesStats {

	private DescriptiveStatistics valueStats;
	private DescriptiveStatistics upperBorderStats;
	private DescriptiveStatistics lowerBorderStats;
	
	public PlotValuesStats() {
		super();
	}
	
	public void setValues(double[]values) {
		valueStats = new DescriptiveStatistics(values);
	}
	
	public void setUpperBorderValues(double[]values) {
		upperBorderStats = new DescriptiveStatistics(values);
	}
	
	public void setLowperBorderValues(double[]values) {
		lowerBorderStats = new DescriptiveStatistics(values);
	}

	public DescriptiveStatistics getValueStats() {
		return valueStats;
	}

	public DescriptiveStatistics getUpperBorderStats() {
		return upperBorderStats;
	}

	public DescriptiveStatistics getLowerBorderStats() {
		return lowerBorderStats;
	}
}
