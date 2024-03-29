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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jfree.data.category.DefaultCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.TwoDimFeatureDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsFeatureBarChartDataSet extends DefaultCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7176808805153148751L;

	private Map<Integer,Paint>seriesPaintMap;
	
	public MsFeatureBarChartDataSet(
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
						files,
						plotParameters.getSortingOrder(), 
						experiment.getExperimentDesign().getCompleteDesignSubset(), 
						plotParameters.getGroupingType(), 
						plotParameters.getCategory(), 
						plotParameters.getSubCategory());
		
		Map<String,Paint>seriesPaintNameMap = 
				createSeriesPaintMap(seriesFileMap, plotParameters.getGroupingType(), 
						plotParameters.getChartColorOption());
		Integer rowCount = 0;
		Map<DataFile, Double> dataMap = 
				PlotDataSetUtils.getNormalizedDataForFeature(
						experiment, feature, pipeline, files, plotParameters.getDataScale());

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
		
		Map<String,Paint>seriesPaintNameMap = new TreeMap<String,Paint>();
		seriesPaintMap = new TreeMap<Integer,Paint>();
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

	public Map<Integer, Paint> getSeriesPaintMap() {
		return seriesPaintMap;
	}
}


















