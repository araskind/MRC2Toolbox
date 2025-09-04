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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.data.category.DefaultCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.TwoDimFeatureDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class MsFeatureBarChartDataSet extends DefaultCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7176808805153148751L;

	private Map<Integer,Paint>seriesPaintMap;

	
	public MsFeatureBarChartDataSet(
			MsFeature feature, 
			TwoDimFeatureDataPlotParameterObject plotParameters) {

		if(feature == null || plotParameters.getExperiment() == null 
				|| plotParameters.getPipeline() == null 
				|| plotParameters.getActiveDesign() == null)
			return;

		Set<DataFile>activeFiles = 
				DataSetUtils.getActiveFilesForPipelineAndDesignSubset(plotParameters);
		
		if(activeFiles.isEmpty())
			return;

		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(plotParameters);
		
		Map<String,Paint>seriesPaintNameMap = createSeriesPaintMap(
						seriesFileMap, 
						plotParameters.getGroupingType(), 
						plotParameters.getChartColorOption());
		Integer rowCount = 0;
		Map<DataFile, Double> dataMap = 
				PlotDataSetUtils.getScaledPeakAreasForFeature(
						plotParameters.getExperiment(), 
						feature, 
						plotParameters.getPipeline(), 
						activeFiles, 
						plotParameters.getDataScale());

		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			for(DataFile df : entry.getValue()) {

				addValue(dataMap.get(df), df, entry.getKey());
				seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
				rowCount++;								
			}
		}
	}

	private Map<String,Paint> createSeriesPaintMap(
			Map<String, DataFile[]> seriesFileMap, 
			PlotDataGrouping groupingType,
			ChartColorOption chartColorOption) {
		
		Map<String,Paint>seriesPaintNameMap = new TreeMap<>();
		seriesPaintMap = new TreeMap<>();
		int sCount = 0;
		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN)
				&& chartColorOption.equals(ChartColorOption.BY_SAMPLE_TYPE)) {
			
			int lCount = 0;
			for(Entry<String, DataFile[]> sfentry : seriesFileMap.entrySet()) {
				
				if(sfentry.getValue().length == 0)
					continue;
				
				ExperimentDesignLevel sampleType = 
						sfentry.getValue()[0].getParentSample().getSampleType();
				if(sampleType != null) 
					seriesPaintNameMap.put(sfentry.getKey(), 
							MasterPlotPanel.getBrewerColor(lCount));
				
				lCount++;
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

	public Map<Integer, Paint> getSeriesPaintMap() {
		return seriesPaintMap;
	}
}


















