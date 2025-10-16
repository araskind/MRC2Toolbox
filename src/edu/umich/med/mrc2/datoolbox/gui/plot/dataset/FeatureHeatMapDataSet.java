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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.data.DomainInfo;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.DefaultXYZDataset;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.utils.NormalizationUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureHeatMapDataSet extends DefaultXYZDataset implements RangeInfo, DomainInfo, IHeatMapDataSet{

	private static final long serialVersionUID = -2535216261141143315L;

	private DataPipeline dataPipeline;
	private Matrix featureSubsetMatrix;
	private double[][] data;
	private String[]rowLabels;
	private String[]columnLabels;
	private DataScale dataScale;
	private Range dataRange;
	private List<MsFeature>features;
	private List<DataFile>dataFiles;
	private MZRTPlotParameterObject params;

	public FeatureHeatMapDataSet(
			DataPipeline dataPipeline,
			Matrix featureSubsetMatrix,
			MZRTPlotParameterObject params) {

		super();
		this.dataPipeline = dataPipeline;
		this.featureSubsetMatrix = featureSubsetMatrix;
		this.params = params;
		this.dataScale = params.getDataScale();
		createDataSet();
	}
	
	private void createDataSet() {	
		
		if (featureSubsetMatrix == null)
			return;
		
		try {
			createFileAndFeatureLists();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			updateDataSetWithParameters(params, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createFileAndFeatureLists() {
		
		Matrix featureMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(0);
		Matrix fileMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(1);	
		features = new ArrayList<MsFeature>();
		dataFiles = new ArrayList<DataFile>();
		long[] coordinates = new long[2];
		coordinates[0] = 0;
		for (long i = 0; i < featureSubsetMatrix.getRowCount(); i++) {
			
			coordinates[1] = i;
			dataFiles.add((DataFile) fileMatrix.getAsObject(coordinates));
		}
		for (long j = 0; j < featureSubsetMatrix.getColumnCount(); j++) {
			
			coordinates[1] = j;
			features.add((MsFeature) featureMatrix.getAsObject(coordinates));
		}
	}
	
	public void updateDataSetWithParameters(MZRTPlotParameterObject newParams, boolean notify) {	

		if (featureSubsetMatrix == null)
			return;	
		
		params = newParams;
		List<MsFeature>filteredFeatures = filterAndSortFeatures(			
				params.getMzRange(), params.getRtRange(), params.getFeatureSortingOrder());
		List<Long>featureCoordinates = 
				filteredFeatures.stream().
				map(f -> featureSubsetMatrix.getColumnForLabel(f)).
				collect(Collectors.toList());
		Matrix featureMetadataMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(0);
		Matrix newFeatureMetadataMatrix = featureMetadataMatrix.selectColumns(Ret.NEW, featureCoordinates);
		List<DataFile>filteredDataFiles = newParams.getActiveSamples().stream().
				flatMap(s -> s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).stream()).
				filter(f -> f.isEnabled()).
				distinct().
				sorted(new DataFileComparator(params.getFileSortingOrder())).
				collect(Collectors.toList());
				
		List<Long>dataFileCoordinates = 
				filteredDataFiles.stream().
				map(f -> featureSubsetMatrix.getRowForLabel(f)).
				collect(Collectors.toList());
		Matrix fileMetadataMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(1);
		Matrix newFileMetadataMatrix = fileMetadataMatrix.selectRows(Ret.NEW, dataFileCoordinates);
		
		long[] featureCoordinatesArray = featureCoordinates.stream().mapToLong(d -> d).toArray();
		long[] fileCoordinatesArray = dataFileCoordinates.stream().mapToLong(d -> d).toArray();

		Matrix filteredDataMatrix = featureSubsetMatrix.select(
						Ret.NEW, fileCoordinatesArray, featureCoordinatesArray);
		filteredDataMatrix.setMetaDataDimensionMatrix(0, newFeatureMetadataMatrix);
		filteredDataMatrix.setMetaDataDimensionMatrix(1, newFileMetadataMatrix);
		
		data = filteredDataMatrix.toDoubleArray();
		dataScale = params.getDataScale();
		if(!dataScale.isDirectCalculation())
			data = NormalizationUtils.scale2Ddata(data, dataScale, true);
				
		rowLabels = new String[(int) filteredDataMatrix.getRowCount()];
		columnLabels = new String[(int) filteredDataMatrix.getColumnCount()];	
		
        double[] xvalues = new double[1];
        double[] yvalues = new double[1];
        double[] zvalues = new double[1];
        double[][]placeholder = new double[][] {xvalues, yvalues, zvalues};

		for (long i = 0; i < filteredDataMatrix.getRowCount(); i++) {
			
			String rowLabel = filteredDataMatrix.getRowLabel(i);
			rowLabels[(int)i] = rowLabel;
			super.addSeries(rowLabel, placeholder);
		}
		for (long j = 0; j < filteredDataMatrix.getColumnCount(); j++)			
			columnLabels[(int)j] = filteredDataMatrix.getColumnLabel(j);

		calculateDataRange();
		
		if(notify)
			notifyListeners(new DatasetChangeEvent(this, this));
	}
	
	private List<MsFeature>filterAndSortFeatures(			
			Range mzRange,
			Range rtRange,
			SortProperty featureSortingOrder){
		
		List<MsFeature>filteredFeatures = new ArrayList<MsFeature>();
		if(features == null || features.isEmpty())
			return filteredFeatures;
		
		filteredFeatures.addAll(features);
		if(mzRange != null && mzRange.getSize() > 0) {
			
			filteredFeatures = filteredFeatures.stream().
				filter(f -> mzRange.contains(f.getMonoisotopicMz())).
				collect(Collectors.toList());
		}
		if(rtRange != null && rtRange.getSize() > 0) {
			
			filteredFeatures = filteredFeatures.stream().
				filter(f -> rtRange.contains(f.getRetentionTime())).
				collect(Collectors.toList());
		}
		return filteredFeatures.stream().
				sorted(new MsFeatureComparator(featureSortingOrder)).
				collect(Collectors.toList());
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

	private void calculateDataRange() {
		
		dataRange = NormalizationUtils.getDataRangeFrom2Darray(data);
		if(dataScale.equals(DataScale.LN)) {
			
			dataRange = new Range(
					Math.log1p(dataRange.getMin()),  
					Math.log1p(dataRange.getMax()));
			return;
		}
		else if(dataScale.equals(DataScale.LOG10)) {

			dataRange = new Range(
					Math.log10(dataRange.getMin()),  
					Math.log10(dataRange.getMax()));
			return;
		}
		else if(dataScale.equals(DataScale.SQRT)) {
			
			dataRange = new Range(
					Math.sqrt(dataRange.getMin()),  
					Math.sqrt(dataRange.getMax()));
			return;
		}		
	}

	public void setDataScale(DataScale newScale) {
		
		if(newScale.isDirectCalculation() && !dataScale.equals(newScale)) {
				
			this.dataScale = newScale;
			calculateDataRange();
			notifyListeners(new DatasetChangeEvent(this, this));		
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
	
	private void createDataSetOld() {		

		if (featureSubsetMatrix == null)
			return;
		
		Matrix featureMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(0);
		Matrix fileMatrix = featureSubsetMatrix.getMetaDataDimensionMatrix(1);	
		features = new ArrayList<MsFeature>();
		dataFiles = new ArrayList<DataFile>();
		long[] coordinates = new long[2];
		coordinates[0] = 0;
		
		data = featureSubsetMatrix.toDoubleArray();
		//	System.out.println(Integer.toString(data[0].length) + " X " + Integer.toString(data[1].length));
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
			
			coordinates[1] = i;
			dataFiles.add((DataFile) fileMatrix.getAsObject(coordinates));
		}
		for (long j = 0; j < featureSubsetMatrix.getColumnCount(); j++) {
			
			columnLabels[(int)j] = featureSubsetMatrix.getColumnLabel(j);
			
			coordinates[1] = j;
			features.add((MsFeature) featureMatrix.getAsObject(coordinates));
		}	
		calculateDataRange();
	}
}

















