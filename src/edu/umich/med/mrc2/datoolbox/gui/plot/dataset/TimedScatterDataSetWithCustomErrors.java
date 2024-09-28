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

import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class TimedScatterDataSetWithCustomErrors extends TimedScatterDataSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	//	Single series
	public TimedScatterDataSetWithCustomErrors(Map<DataFile,SimpleMsFeature>dataFileFeatureMap) {
		
		super();
		
	}
		
	//	Series by sample type
	public TimedScatterDataSetWithCustomErrors(
			Map<DataFile,SimpleMsFeature>dataFileFeatureMap,
			DataAnalysisProject currentExperiment,
			DataPipeline pipeline) {
		super();
		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign() == null)
			return;

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples().isEmpty())
			return;
		
		Map<String, DataFile[]>seriesMap = PlotDataSetUtils.mapSeriesIgnoreDesign(
				pipeline,
				FileSortingOrder.TIMESTAMP,
				currentExperiment.getExperimentDesign().getActiveDesignSubset());
	}
	
	private void createSeriesBySampleType() {
		
	}
	
	
}
