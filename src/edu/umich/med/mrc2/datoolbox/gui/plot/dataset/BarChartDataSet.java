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
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class BarChartDataSet extends DefaultCategoryDataset {

	/**
	 *
	 */
	private static final long serialVersionUID = 8775114218336997418L;

	private MsFeature[] featuresToPlot;
	private Map<DataPipeline, DataFile[]> dataFileMap;
	private Map<Integer,Paint>seriesPaintMap;
	
	public BarChartDataSet(			
			MsFeature msf,
			DataScale dataScale,
			TwoDimDataPlotParameterObject plotParameters){

			this(msf,
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getActiveDesignSubset(),
				dataScale,
				plotParameters);
	}
	
	public BarChartDataSet(
			MsFeature msf,
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			ExperimentDesignSubset activeDesign,
			DataScale dataScale,
			TwoDimDataPlotParameterObject plotParameters){
			this(msf,
				experiment,
				pipeline,
				plotParameters.getSortingOrder(),
				dataScale,
				activeDesign,
				plotParameters.getGroupingType(),
				plotParameters.getCategory(),
				plotParameters.getSubCategory());
	}

 	public BarChartDataSet(
			MsFeature msf,
			DataAnalysisProject experiment,
			DataPipeline pipeline,
			FileSortingOrder sortingOrder,
			DataScale dataScale,
			ExperimentDesignSubset activeDesign,
			PlotDataGrouping groupingType,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory) {

		super();
		featuresToPlot = new MsFeature[] {msf};

		//	Collect data	
		Set<DataFile>files = 
				DataSetUtils.getActiveFilesForPipelineAndDesignSubset(
						experiment,pipeline,activeDesign,sortingOrder);
		
		Map<DataFile, Double> dataMap = 
				PlotDataSetUtils.getScaledPeakAreasForFeature(
						experiment, msf, pipeline, files, dataScale);
		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(experiment, pipeline, sortingOrder, 
						activeDesign, groupingType, category, subCategory);
		Map<String,Paint>seriesPaintNameMap = new TreeMap<String,Paint>();
		seriesPaintMap = new TreeMap<Integer,Paint>();
		int sCount = 0;
		for(String sName : seriesFileMap.keySet()) {

			seriesPaintNameMap.put(sName, MasterPlotPanel.getBrewerColor(sCount));
			sCount++;
		}
		//	Add data
		Integer rowCount = 0;
		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			for(DataFile df : entry.getValue()) {

				//TODO handle through custom object to allow proper labels
				//df.getFileName()

				addValue(dataMap.get(df), rowCount, entry.getKey());
				seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
				rowCount++;
			}
		}
	}

	public MsFeature[] getPlottedFeatures() {
		return featuresToPlot;
	}

	/**
	 * @return the seriesPaintMap
	 */
	public Map<Integer, Paint> getSeriesPaintMap() {
		return seriesPaintMap;
	}
}


























