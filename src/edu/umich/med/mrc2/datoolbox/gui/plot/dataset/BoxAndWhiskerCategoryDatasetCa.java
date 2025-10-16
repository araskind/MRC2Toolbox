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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.TwoDimFeatureDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.ArrayUtils;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class BoxAndWhiskerCategoryDatasetCa extends DefaultBoxAndWhiskerCategoryDataset{

	/**
	 *
	 */
	private static final long serialVersionUID = 7334649551613689395L;
	private MsFeature[] featuresToPlot;
	
	public BoxAndWhiskerCategoryDatasetCa(
			TwoDimFeatureDataPlotParameterObject plotParameters,
			DataAnalysisProject experiment,
			ExperimentDesignSubset activeDesign) {
		
		this(plotParameters.getFeaturesMap(),
			experiment,
			activeDesign,
			plotParameters.getStatPlotType(),
			plotParameters.getSortingOrder(),
			plotParameters.getDataScale(),			
			plotParameters.getGroupingType(),
			plotParameters.getCategory(),
			plotParameters.getSubCategory());
	}

	public BoxAndWhiskerCategoryDatasetCa(
			Map<DataPipeline,Collection<MsFeature>> selectedFeaturesMap,
			DataAnalysisProject experiment,
			ExperimentDesignSubset activeDesign,
			StatsPlotType statPlotType,
			FileSortingOrder sortingOrder,
			DataScale dataScale,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory) {

		super();
		if(experiment== null || activeDesign == null 
				|| selectedFeaturesMap.isEmpty())
			return;
		
		Map<DataPipeline,Set<DataFile>>pipelineDataFilesMap = 
				new TreeMap<DataPipeline,Set<DataFile>>();
		Map<DataFile, Double> dataMap = 
				new TreeMap<DataFile,Double>(new DataFileComparator(sortingOrder));
		
		Map<String, DataFile[]> seriesFileMap = new LinkedHashMap<String, DataFile[]>();
		
		for(DataPipeline pipeline : selectedFeaturesMap.keySet()) {
			
			Set<DataFile>pipelineFiles = 
					DataSetUtils.getActiveFilesForPipelineAndDesignSubset(
					experiment, pipeline, activeDesign, sortingOrder);
			
			if(!pipelineFiles.isEmpty()) { 

				pipelineDataFilesMap.put(pipeline,pipelineFiles);
				Map<String, DataFile[]> pipelineSeriesFileMap = 
						PlotDataSetUtils.createSeriesFileMap(experiment, pipeline, sortingOrder, 
								activeDesign, groupingType, category, subCategory);
				
				for(Entry<String, DataFile[]> psme : pipelineSeriesFileMap.entrySet()) {
					
					if(seriesFileMap.containsKey(psme.getKey())) {
						
						DataFile[]combined = 
								ArrayUtils.concatObjectArrays
										(seriesFileMap.get(psme.getKey()), 
										pipelineSeriesFileMap.get(psme.getKey()));
						seriesFileMap.put(psme.getKey(), combined);
					}
					else {
						seriesFileMap.put(psme.getKey(), psme.getValue());
					}
				}
			}
		}
		//	TODO ...
		
//		featuresToPlot = selectedFeaturesMap.values().stream().
//				flatMap(c -> c.stream()).toArray(size -> new MsFeature[size]);
//
//		//	Collect data
//		Collection<ExperimentalSample> samples = 
//				experiment.getExperimentDesign().getSamplesForDesignSubset(activeDesign, true);
//
//		for (Entry<DataPipeline, Collection<MsFeature>> entry : selectedFeaturesMap.entrySet()) {
//			
//			for(MsFeature msf : entry.getValue()) {
//
//				Map<DataFile, Double> dataMap = 
//						PlotDataSetUtils.getScaledPeakAreasForFeature(
//								experiment, msf, entry.getKey(),allDataFiles, dataScale);
//				
//				Map<String, DataFile[]> seriesFileMap = 
//						PlotDataSetUtils.createSeriesFileMap(entry.getKey(), sortingOrder, 
//								activeDesign, groupingType, category, subCategory);
//
//				//	Add data
//				for (Entry<String, DataFile[]> seriesEntry : seriesFileMap.entrySet()) {
//
//					Integer count = 1;
//					ArrayList<Double>seriesValues = new ArrayList<Double>();
//
//					for(DataFile df : seriesEntry.getValue())
//						seriesValues.add(dataMap.get(df));
//
//					if(boxplotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE))
//						add(seriesValues, entry.getKey(), msf.getName());
//
//					if(boxplotType.equals(StatsPlotType.BOXPLOT_BY_GROUP))
//						add(seriesValues, msf.getName(), entry.getKey());
//				}
//			}
//		}
	}
	
	public MsFeature[] getFeaturesToPlot() {
		return featuresToPlot;
	}
}

























