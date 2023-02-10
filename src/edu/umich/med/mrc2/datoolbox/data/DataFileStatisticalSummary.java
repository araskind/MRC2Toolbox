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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataFileStatisticalSummary implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -383674273020565936L;
	private DataFile file;
	private TreeMap<DataSetQcField, Number> properties;
	private BoxAndWhiskerItem boxplotItem;

	public DataFileStatisticalSummary(DataFile datafile) {

		file = datafile;
		properties = new TreeMap<DataSetQcField, Number>();
	}

	public void calculateFileStat() {

		Matrix datamatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment()
				.getDataMatrixForDataPipeline(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline());
		Matrix featureRows = datamatrix.selectRows(Ret.LINK, datamatrix.getRowForLabel(file));
		double[] fValues = StreamSupport.stream(featureRows.allValues().spliterator(), false).
				filter(o -> Objects.nonNull(o)).
				filter(o -> Double.valueOf((double)o) > 0.0d).
				map(Double.class::cast).mapToDouble(Double::valueOf).toArray();
		int totalFeatureCount = 
				Long.valueOf(datamatrix.getMetaDataDimensionMatrix(0).getValueCount()).intValue();
		
		DescriptiveStatistics stats = new DescriptiveStatistics(fValues);
		
		properties.put(DataSetQcField.MIN, stats.getMin());
		properties.put(DataSetQcField.MAX, stats.getMax());
		properties.put(DataSetQcField.MEAN, stats.getMean());
		properties.put(DataSetQcField.MEDIAN, stats.getPercentile(50.0));
		properties.put(DataSetQcField.SD, stats.getStandardDeviation());
		properties.put(DataSetQcField.OBSERVATIONS, (int) stats.getN());
		properties.put(DataSetQcField.MISSING, totalFeatureCount - (int) stats.getN());
		properties.put(DataSetQcField.OUTLIERS, countOutliers(stats));

		double lowerLimit = stats.getPercentile(5.0d);
		double upperLimit = stats.getPercentile(95.0d);
		double[] trimmedValues = DoubleStream.of(fValues).
			filter(v -> v >= lowerLimit).
			filter(v -> v <= upperLimit).boxed().
			mapToDouble(Double::valueOf).toArray();
		DescriptiveStatistics trimmedStats = new DescriptiveStatistics(trimmedValues);
		
		properties.put(DataSetQcField.MEAN_TRIM, trimmedStats.getMean());
		properties.put(DataSetQcField.SD_TRIM, trimmedStats.getStandardDeviation());
		properties.put(DataSetQcField.RSD, stats.getStandardDeviation() / stats.getMean());
		properties.put(DataSetQcField.RSD_TRIM, trimmedStats.getStandardDeviation() / trimmedStats.getMean());

		Double[] values = ArrayUtils.toObject(stats.getValues());
		boxplotItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(Arrays.asList(values), true);
	}

	public int countOutliers(DescriptiveStatistics stats) {

		int count = 0;

		double q1 = stats.getPercentile(25.0d);
		double q3 = stats.getPercentile(75.0d);
		double iqr = q3 - q1;
		double lowerLimit = q1 - 1.5 * iqr;
		double upperLimit = q3 + 1.5 * iqr;

		for (double value : stats.getSortedValues()) {

			if (value < lowerLimit || value > upperLimit)
				count++;
		}
		return count;
	}

	public BoxAndWhiskerItem getBoxplotItem() {
		return boxplotItem;
	}

	public DataFile getFile() {
		return file;
	}

	public double getMax() {
		return (double) properties.get(DataSetQcField.MAX);
	}

	public double getMean() {
		return (double) properties.get(DataSetQcField.MEAN);
	}

	public double getMeanTrimmed() {
		return (double) properties.get(DataSetQcField.MEAN_TRIM);
	}

	public double getMedian() {
		return (double) properties.get(DataSetQcField.MEDIAN);
	}

	public double getMin() {
		return (double) properties.get(DataSetQcField.MIN);
	}

	public int getMissing() {
		return (int) properties.get(DataSetQcField.MISSING);
	}

	public int getObservations() {
		return (int) properties.get(DataSetQcField.OBSERVATIONS);
	}

	public int getOutliers() {
		return (int) properties.get(DataSetQcField.OUTLIERS);
	}

	public Number getProperty(DataSetQcField property) {

		return properties.get(property);
	}

	public double getRsd() {
		return (double) properties.get(DataSetQcField.RSD);
	}

	public double getRsdTrimmed() {
		return (double) properties.get(DataSetQcField.RSD_TRIM);
	}

	public double getSd() {
		return (double) properties.get(DataSetQcField.SD);
	}

	public double getSdTrimmed() {
		return (double) properties.get(DataSetQcField.SD_TRIM);
	}

}
