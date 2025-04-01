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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;

public class TwoDimDataPlotParameterObject {

	protected FileSortingOrder sortingOrder; 
	protected ChartColorOption chartColorOption;
	protected PlotDataGrouping groupingType;
	protected DataScale dataScale;
	protected ExperimentDesignFactor category;
	protected ExperimentDesignFactor subCategory;
	protected StatsPlotType statPlotType;
	
	public TwoDimDataPlotParameterObject(
			FileSortingOrder sortingOrder, 
			DataScale dataScale,
			ChartColorOption chartColorOption,
			PlotDataGrouping groupingType, 
			ExperimentDesignFactor category, 
			ExperimentDesignFactor subCategory,
			StatsPlotType statPlotType) {
		super();
		this.sortingOrder = sortingOrder;
		this.dataScale = dataScale;
		this.chartColorOption = chartColorOption;
		this.groupingType = groupingType;
		this.category = category;
		this.subCategory = subCategory;
		if(this.groupingType.equals(PlotDataGrouping.TWO_FACTORS)
				&& this.subCategory == null)
			this.groupingType = PlotDataGrouping.ONE_FACTOR;
		
		this.statPlotType = statPlotType;
	}

	public FileSortingOrder getSortingOrder() {
		return sortingOrder;
	}

	public ChartColorOption getChartColorOption() {
		return chartColorOption;
	}

	public PlotDataGrouping getGroupingType() {
		return groupingType;
	}

	public ExperimentDesignFactor getCategory() {
		return category;
	}

	public ExperimentDesignFactor getSubCategory() {
		return subCategory;
	}

	public DataScale getDataScale() {
		return dataScale;
	}
	
	public StatsPlotType getStatPlotType() {
		return statPlotType;
	}

	public void setStatPlotType(StatsPlotType statPlotType) {
		this.statPlotType = statPlotType;
	}
	
	public String getDesignDescriptor() {
		
		if(groupingType.equals(PlotDataGrouping.IGNORE_DESIGN))
			return "orderedBy_" + sortingOrder.getName();;
		
		String descriptor = "by_";
		if(category != null)
			descriptor += category.getName();

		if(subCategory != null)
			descriptor += "_and_" + subCategory.getName();
		
		descriptor += "_orderedBy_" + sortingOrder.getName();
		
		return descriptor;
	}
}


