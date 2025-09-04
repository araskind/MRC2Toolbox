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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MSQualityDataPlotParameterObject extends TwoDimDataPlotParameterObject {
	
	private MsFeature msFeature;
	private Map<DataFile, SimpleMsFeature> fileFeatureMap;
	
	public MSQualityDataPlotParameterObject(
			MsFeature msFeature,
			Map<DataFile, SimpleMsFeature> fileFeatureMap,
			DataAnalysisProject experiment, 
			DataPipeline pipeline,
			ExperimentDesignSubset activeDesign, 
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory,
			PlotDataGrouping groupingType, 
			FileSortingOrder sortingOrder, 
			DataScale dataScale, 
			StatsPlotType statPlotType,
			ChartColorOption chartColorOption) {
		super(experiment, 
				pipeline, 
				activeDesign, 
				category, 
				subCategory, 
				groupingType, 
				sortingOrder, 
				dataScale, 
				statPlotType, 
				chartColorOption);
		this.msFeature = msFeature;
		this.fileFeatureMap = fileFeatureMap;
	}

	public MSQualityDataPlotParameterObject(
			MsFeature msFeature,
			Map<DataFile, SimpleMsFeature> fileFeatureMap,
			ExperimentDesignFactor category,
			ExperimentDesignFactor subCategory,
			PlotDataGrouping groupingType, 
			FileSortingOrder sortingOrder, 
			ChartColorOption chartColorOption) {
		
		super(category, 
			subCategory, 
			groupingType,
			sortingOrder,
			DataScale.RAW,			
			null,
			chartColorOption);
		
		this.msFeature = msFeature;
		this.fileFeatureMap = fileFeatureMap;
	}

	public MsFeature getMsFeature() {
		return msFeature;
	}

	public Map<DataFile, SimpleMsFeature> getFileFeatureMap() {
		return fileFeatureMap;
	}
}
