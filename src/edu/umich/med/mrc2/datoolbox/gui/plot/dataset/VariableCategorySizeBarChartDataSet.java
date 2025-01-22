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

import java.awt.Paint;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.TwoDqcPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.TwoDimFeatureDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class VariableCategorySizeBarChartDataSet extends AbstractDataset implements CategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7176808805153148751L;

	private Map<Integer,Paint>seriesPaintMap;
	private Map<Comparable,Integer>rowMap;
	private Map<Comparable,Integer>columnMap;
	private Double[][]data; 
	private List rowKeys, columnKeys;
	private int[]categoryItemCount;
	
	public VariableCategorySizeBarChartDataSet(
			Collection<DataFileStatisticalSummary> dataSetStats, 			
			DataSetQcField statsField,
			FileSortingOrder sortingOrder, 
			ChartColorOption chartColorOption,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory,
			boolean splitByBatch) {
		
		if (statsField == DataSetQcField.RAW_VALUES)
			return;
		
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			return;
		
		DataPipeline pipeline = experiment.getActiveDataPipeline();
		
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamples();
		HashSet<DataFile> files = samples.stream().
				flatMap(s -> s.getDataFilesForMethod(pipeline.getAcquisitionMethod()).stream()).
				filter(s -> s.isEnabled()).collect(Collectors.toCollection(HashSet::new));

		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(
						pipeline, 
						sortingOrder, 
						experiment.getExperimentDesign().getCompleteDesignSubset(), 
						groupingType, 
						category, 
						subCategory);
		calculateCategoryItemCount(seriesFileMap);
		
		Map<String,Paint>seriesPaintNameMap = new HashMap<String,Paint>();
		seriesPaintMap = new HashMap<Integer,Paint>();
		rowMap = new LinkedHashMap<Comparable,Integer>();
		columnMap = new LinkedHashMap<Comparable,Integer>();
		
		int sCount = 0;
		for(String sName : seriesFileMap.keySet()) {
			seriesPaintNameMap.put(sName, MasterPlotPanel.getBrewerColor(sCount));
			sCount++;
		}
		data = new Double[files.size()][seriesFileMap.size()];
		Integer rowCount = 0;
		Integer columnCount = 0;
		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			columnMap.put(entry.getKey(), columnCount);
			for(DataFile df : entry.getValue()) {
				
				
				DataFileStatisticalSummary fileSummary = 
						dataSetStats.stream().
						filter(st -> st.getFile().equals(df)).
						findFirst().orElse(null);
				if(fileSummary != null) {
					//	addValue(fileSummary.getProperty(statsField).doubleValue(), df.getName(), entry.getKey());
					data[rowCount][columnCount] = fileSummary.getProperty(statsField).doubleValue();
					rowMap.put(df.getName(), rowCount);
					seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
					rowCount++;
				}				
			}
			columnCount++;
		}
		rowKeys = rowMap.keySet().stream().collect(Collectors.toList());
		columnKeys = columnMap.keySet().stream().collect(Collectors.toList());		
	}
	
	public VariableCategorySizeBarChartDataSet(TwoDqcPlotParameterObject plotParameters) {
		
		if (plotParameters.getStatsField().equals(DataSetQcField.RAW_VALUES))
			return;
		
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			return;
		
		DataPipeline pipeline = experiment.getActiveDataPipeline();
		
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamples();
		HashSet<DataFile> files = samples.stream().
				flatMap(s -> s.getDataFilesForMethod(pipeline.getAcquisitionMethod()).stream()).
				filter(s -> s.isEnabled()).collect(Collectors.toCollection(HashSet::new));

		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(
						pipeline, 
						plotParameters.getSortingOrder(), 
						experiment.getExperimentDesign().getCompleteDesignSubset(), 
						plotParameters.getGroupingType(), 
						plotParameters.getCategory(), 
						plotParameters.getSubCategory());
		calculateCategoryItemCount(seriesFileMap);
		
		Map<String,Paint>seriesPaintNameMap = 
				createSeriesPaintMap(seriesFileMap, plotParameters.getGroupingType(), 
						plotParameters.getChartColorOption());
		
		rowMap = new LinkedHashMap<Comparable,Integer>();
		columnMap = new LinkedHashMap<Comparable,Integer>();
		Integer rowCount = 0;
		Integer columnCount = 0;
		data = new Double[files.size()][seriesFileMap.size()];
		
		DataSetQcField sf = plotParameters.getStatsField();
		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			columnMap.put(entry.getKey(), columnCount);			
			for(DataFile df : entry.getValue()) {
				
				DataFileStatisticalSummary fileSummary = 
						plotParameters.getDataSetStats().stream().
						filter(st -> st.getFile().equals(df)).
						findFirst().orElse(null);
				if(fileSummary != null) {
					//	addValue(fileSummary.getProperty(sf).doubleValue(), df, entry.getKey());
					data[rowCount][columnCount] = fileSummary.getProperty(sf).doubleValue();
					rowMap.put(df, rowCount);
					seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
					rowCount++;
				}				
			}
			columnCount++;
		}
		rowKeys = rowMap.keySet().stream().collect(Collectors.toList());
		columnKeys = columnMap.keySet().stream().collect(Collectors.toList());
	}
	
	public VariableCategorySizeBarChartDataSet(
			MsFeature feature, 
			TwoDimFeatureDataPlotParameterObject plotParameters) {
				
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(experiment == null)
			return;
		
		DataPipeline pipeline = experiment.getActiveDataPipeline();
		
		Collection<ExperimentalSample> samples = 
				experiment.getExperimentDesign().getSamples();
		HashSet<DataFile> files = samples.stream().
				flatMap(s -> s.getDataFilesForMethod(pipeline.getAcquisitionMethod()).stream()).
				filter(s -> s.isEnabled()).collect(Collectors.toCollection(HashSet::new));

		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(
						pipeline, 
						plotParameters.getSortingOrder(), 
						experiment.getExperimentDesign().getCompleteDesignSubset(), 
						plotParameters.getGroupingType(), 
						plotParameters.getCategory(), 
						plotParameters.getSubCategory());
		//	calculateCategoryItemCount(seriesFileMap);
		categoryItemCount = new int[seriesFileMap.size()];
		
		int fileCount = IntStream.of(categoryItemCount).sum();

		Map<String,Paint>seriesPaintNameMap = 
				createSeriesPaintMap(seriesFileMap, plotParameters.getGroupingType(), 
						plotParameters.getChartColorOption());
		
		rowMap = new LinkedHashMap<Comparable,Integer>();
		columnMap = new LinkedHashMap<Comparable,Integer>();
		Integer rowCount = 0;
		Integer columnCount = 0;
		
		//data = new Double[files.size()][seriesFileMap.size()];
		data = new Double[fileCount][seriesFileMap.size()];
		
		Map<DataFile, Double> dataMap = 
				PlotDataSetUtils.getScaledDataForFeature(
						experiment, feature, pipeline, files, plotParameters.getDataScale());

		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			columnMap.put(entry.getKey(), columnCount);
			int itemCount = 0;
			
			for(DataFile df : entry.getValue()) {

				Double val = dataMap.get(df);
				data[rowCount][columnCount] = val;
				if(val != null)
					itemCount++;
				
				rowMap.put(df, rowCount);
				seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
				rowCount++;								
			}
			categoryItemCount[columnCount] = itemCount;
			columnCount++;
		}
		rowKeys = rowMap.keySet().stream().collect(Collectors.toList());
		columnKeys = columnMap.keySet().stream().collect(Collectors.toList());
	}
		
	private Map<String,Paint> createSeriesPaintMap(
			Map<String, DataFile[]> seriesFileMap, 
			PlotDataGrouping groupingType,
			ChartColorOption chartColorOption) {
		
		Map<String,Paint>seriesPaintNameMap = new TreeMap<String,Paint>();
		seriesPaintMap = new HashMap<Integer,Paint>();
		int sCount = 0;
		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)
				&& chartColorOption.equals(ChartColorOption.BY_SAMPLE_TYPE)) {
			
			Collection<ExperimentDesignLevel> levelList = 
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
					getExperimentDesign().getFactorByName(StandardFactors.SAMPLE_CONTROL_TYPE.getName()).
					getLevels();
			Map<ExperimentDesignLevel,Paint>levelPaintMap = 
					new TreeMap<ExperimentDesignLevel,Paint>();
			
			int lCount = 0;
			for(ExperimentDesignLevel l : levelList) {
				levelPaintMap.put(l, MasterPlotPanel.getBrewerColor(lCount));
				lCount++;
			}					
			for(Entry<String, DataFile[]> sfentry : seriesFileMap.entrySet()) {
				
				if(sfentry.getValue().length == 0)
					continue;
				
				ExperimentDesignLevel sampleType = 
						sfentry.getValue()[0].getParentSample().getSampleType();
				if(sampleType != null) 
					seriesPaintNameMap.put(sfentry.getKey(), levelPaintMap.get(sampleType));
			}
		}
		else {
			for(String sName : seriesFileMap.keySet()) {
				seriesPaintNameMap.put(sName, MasterPlotPanel.getBrewerColor(sCount));
				sCount++;
			}
		}
		return seriesPaintNameMap;
	}
	
	private void calculateCategoryItemCount(Map<String, DataFile[]> seriesFileMap) {

		categoryItemCount = new int[seriesFileMap.size()];
		int count = 0;
		for(Entry<String, DataFile[]> e : seriesFileMap.entrySet()) {			
			categoryItemCount[count] = e.getValue().length;
			count++;
		}
	}

	public Map<Integer, Paint> getSeriesPaintMap() {
		return seriesPaintMap;
	}
	
    @Override
    public int getRowIndex(Comparable key) {
        return this.rowMap.get(key);
    }
    
    @Override
    public int getColumnIndex(Comparable key) {
        return this.columnMap.get(key);
    }
    
    @Override
    public Number getValue(Comparable rowKey, Comparable columnKey) {
    	
    	int row = rowMap.get(rowKey);
    	int col = columnMap.get(columnKey);
    	return data[row][col];
    }

	@Override
	public Comparable getRowKey(int row) {
		
		return rowMap.entrySet().stream().
				filter(e -> e.getValue().equals(row)).
				map(e -> e.getKey()).
				findFirst().orElse(null);
	}

	@Override
	public List getRowKeys() {
		return rowKeys;
	}

	@Override
	public Comparable getColumnKey(int column) {
	
		return columnMap.entrySet().stream().
				filter(e -> e.getValue().equals(column)).
				map(e -> e.getKey()).
				findFirst().orElse(null);
	}

	@Override
	public List getColumnKeys() {
		return columnKeys;
	}

	@Override
	public int getRowCount() {
		return rowKeys.size();
	}

	@Override
	public int getColumnCount() {
		return columnKeys.size();
	}

	@Override
	public Number getValue(int row, int column) {
		return data[row][column];
	}

	public int[] getCategoryItemCounts() {
		return categoryItemCount;
	}
}


















