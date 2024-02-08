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

import org.jfree.data.DomainInfo;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.DefaultXYZDataset;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureHeatMapDataSet extends DefaultXYZDataset implements RangeInfo, DomainInfo, IHeatMapDataSet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2535216261141143315L;
	private Matrix featureSubsetMatrix;
	private double[][] data;
	private String[]rowLabels;
	private String[]columnLabels;
	private DataScale dataScale;
	private Range dataRange;

	public FeatureHeatMapDataSet(Matrix featureSubsetMatrix) {

		super();
		this.featureSubsetMatrix = featureSubsetMatrix;
		dataScale = DataScale.LN;
		createDataSet();
	}

	private void createDataSet() {		

		if (featureSubsetMatrix == null)
			throw new IllegalArgumentException("The 'data' is null.");
		
		data = featureSubsetMatrix.toDoubleArray();
		System.out.println(Integer.toString(data[0].length) + " X " + Integer.toString(data[01].length));
		rowLabels = new String[(int) featureSubsetMatrix.getRowCount()];
		columnLabels = new String[(int) featureSubsetMatrix.getColumnCount()];	
		
        double[] xvalues = new double[1];
        double[] yvalues = new double[1];
        double[] zvalues = new double[1];
        double[][]placeholder = new double[][] {xvalues, yvalues, zvalues};

		for (long i = 0; i < featureSubsetMatrix.getRowCount(); i++) {
			
			String rowLabel = featureSubsetMatrix.getRowLabel(i);
			rowLabels[(int)i] = rowLabel;
			super.addSeries(rowLabel, placeholder);
		}
		for (long j = 0; j < featureSubsetMatrix.getColumnCount(); j++)
			columnLabels[(int)j] = featureSubsetMatrix.getColumnLabel(j);
		
		dataRange = calculateDataRange();
	}
	
	@Override
	public int getItemCount(int series) {
		return columnLabels.length;
	}
	
	@Override
	public double getXValue(int series, int item) {
		return item;
	}

	@Override
	public Number getX(int series, int item) {
		return item;
	}

	@Override
	public double getYValue(int series, int item) {
		return series;
	}

	@Override
	public Number getY(int series, int item) {
		return series;
	}
	
	@Override
	public double getZValue(int series, int item) {

        double value = data[series][item];
 		
		if(dataScale.equals(DataScale.LN))
			return Math.log1p(value);

		else if(dataScale.equals(DataScale.LOG10))
			return Math.log10(value);

		else if(dataScale.equals(DataScale.SQRT))
			return Math.sqrt(value);
		
		else
			return value;
	}
	
	public double getRAWValue(int series, int item) {
		return data[series][item];
	}

	public String[] getRowLabels() {
		return rowLabels;
	}
	
	public String[] getColumnLabels() {
		return columnLabels;
	}

	public int getFeatureNumber() {
		return (int) featureSubsetMatrix.getColumnCount();
	}
	
	public int getFileNumber() {
		return (int) featureSubsetMatrix.getRowCount();
	}
	
	public Range getDataRange() {
		return dataRange;
	}

	public Range calculateDataRange() {
		
		double min = featureSubsetMatrix.getMinValue();
		double max = featureSubsetMatrix.getMaxValue();
		
		if(dataScale.equals(DataScale.LN)) {
			min = Math.log1p(min);
			max = Math.log1p(max);
		}
		else if(dataScale.equals(DataScale.LOG10)) {
			min = Math.log10(min);
			max = Math.log10(max);
		}
		else if(dataScale.equals(DataScale.SQRT)) {
			min = Math.sqrt(min);
			max = Math.sqrt(max);
		}
		return new Range(min, max);
	}

	public void setDataScale(DataScale newScale) {
		
		if(newScale.equals(DataScale.RAW) 
				|| newScale.equals(DataScale.LN)
				|| newScale.equals(DataScale.LOG10) 
				||  newScale.equals(DataScale.SQRT)) {
			boolean scaleChanged = false;
			
			if(!dataScale.equals(newScale))
				scaleChanged = true;
			
			this.dataScale = newScale;
			if(scaleChanged) {
				
				dataRange = calculateDataRange();
				notifyListeners(new DatasetChangeEvent(this, this));
			}
		}
	}

	@Override
	public double getRangeLowerBound(boolean includeInterval) {
		return 0;
	}

	@Override
	public double getRangeUpperBound(boolean includeInterval) {
		return rowLabels.length;
	}

	@Override
	public org.jfree.data.Range getRangeBounds(boolean includeInterval) {	
		return new org.jfree.data.Range(0, rowLabels.length);
	}

	@Override
	public double getDomainLowerBound(boolean includeInterval) {
		return 0;
	}

	@Override
	public double getDomainUpperBound(boolean includeInterval) {
		return columnLabels.length;
	}

	@Override
	public org.jfree.data.Range getDomainBounds(boolean includeInterval) {
		return new org.jfree.data.Range(0, columnLabels.length);
	}
}

















