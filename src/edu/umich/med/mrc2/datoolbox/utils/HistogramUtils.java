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

package edu.umich.med.mrc2.datoolbox.utils;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

public class HistogramUtils {
	
	public static SimpleHistogramDataset calcHistogram(
			double[] data, String title, boolean adjustForBinSize) {

		DescriptiveStatistics da = new DescriptiveStatistics(data);
		double iqr = da.getPercentile(75) - da.getPercentile(25);
		double bw = 2 * iqr / Math.pow((double)data.length, 1.0d/3.0d);
		final int BIN_COUNT = (int)Math.ceil((da.getMax() - da.getMin()) / bw);

		EmpiricalDistribution distribution = new EmpiricalDistribution(BIN_COUNT);
		distribution.load(data);
		
		SimpleHistogramDataset dataSet = new SimpleHistogramDataset(title);		
		for(SummaryStatistics stats: distribution.getBinStats()) {
			
			if(stats.getSum() == 0.0d || stats.getMin() == stats.getMax())
				continue;
			
			double min = stats.getMin();
			double max = stats.getMax();
			SimpleHistogramBin bin = 
					new SimpleHistogramBin(stats.getMin(), stats.getMax(), true, false);
			bin.setItemCount((int) stats.getN());
		    dataSet.addBin(bin);
		}			
		dataSet.setAdjustForBinSize(adjustForBinSize);
		
		return dataSet;
	}
}
