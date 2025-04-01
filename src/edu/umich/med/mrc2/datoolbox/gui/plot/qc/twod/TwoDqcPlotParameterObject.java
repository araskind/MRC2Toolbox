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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.plot.TwoDimDataPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;

public class TwoDqcPlotParameterObject extends TwoDimDataPlotParameterObject{

	private Collection<DataFileStatisticalSummary> dataSetStats;
	private DataSetQcField statsField;

	public TwoDqcPlotParameterObject(
			Collection<DataFileStatisticalSummary> dataSetStats,
			DataSetQcField statsField,
			FileSortingOrder sortingOrder, 
			DataScale dataScale,
			ChartColorOption chartColorOption,
			PlotDataGrouping groupingType, 
			ExperimentDesignFactor category, 
			ExperimentDesignFactor subCategory,
			StatsPlotType statPlotType) {
		super(sortingOrder, dataScale, chartColorOption, 
				groupingType, category, subCategory, statPlotType);
		this.dataSetStats = dataSetStats;
		this.statsField = statsField;
	}

	public Collection<DataFileStatisticalSummary> getDataSetStats() {
		return dataSetStats;
	}

	public void setDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats) {
		this.dataSetStats = dataSetStats;
	}

	public DataSetQcField getStatsField() {
		return statsField;
	}

	public void setStatsField(DataSetQcField statsField) {
		this.statsField = statsField;
	}
	
	@Override
	public String getDesignDescriptor() {
		
		String descriptor = statsField.getName() 
				+ "_" + super.getDesignDescriptor();
				
		return descriptor;
	}
}


