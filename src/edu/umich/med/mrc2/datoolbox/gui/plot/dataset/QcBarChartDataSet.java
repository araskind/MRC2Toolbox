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
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class QcBarChartDataSet extends DefaultCategoryDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7176808805153148751L;

	private Map<Integer,Paint>seriesPaintMap;
	
	public QcBarChartDataSet(
			Collection<DataFileStatisticalSummary> dataSetStats, 
			FileSortingOrder sortingOrder, 
			DataSetQcField statsField) {
		
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
//		Map<String, DataFile[]> seriesFileMap = 
//				PlotDataSetUtils.createSeriesFileMap(
//						pipeline, 
//						files,
//						sortingOrder, 
//						experiment.getExperimentDesign().getCompleteDesignSubset(), 
//						PlotDataGrouping.IGNORE_DESIGN,
//						null,
//						null, 
//						false);
		Map<String, DataFile[]> seriesFileMap = 
				PlotDataSetUtils.createSeriesFileMap(
						pipeline, 
						files,
						sortingOrder, 
						experiment.getExperimentDesign().getCompleteDesignSubset(), 
						PlotDataGrouping.ONE_FACTOR, 
						experiment.getExperimentDesign().getFactorByName(StandardFactors.SAMPLE_CONTROL_TYPE.getName()), 
						null, 
						false);
		Map<String,Paint>seriesPaintNameMap = new TreeMap<String,Paint>();
		seriesPaintMap = new TreeMap<Integer,Paint>();
		int sCount = 0;
		for(String sName : seriesFileMap.keySet()) {
			seriesPaintNameMap.put(sName, MasterPlotPanel.getBrewerColor(sCount));
			sCount++;
		}		
		Integer rowCount = 0;
		for (Entry<String, DataFile[]> entry : seriesFileMap.entrySet()) {

			for(DataFile df : entry.getValue()) {
				
				DataFileStatisticalSummary fileSummary = 
						dataSetStats.stream().
						filter(st -> st.getFile().equals(df)).
						findFirst().orElse(null);
				if(fileSummary != null) {
					addValue(fileSummary.getProperty(statsField).doubleValue(), df.getName(), entry.getKey());
					seriesPaintMap.put(rowCount, seriesPaintNameMap.get(entry.getKey()));
					rowCount++;
				}				
			}
		}		
	}
	
	public Map<Integer, Paint> getSeriesPaintMap() {
		return seriesPaintMap;
	}
}


















