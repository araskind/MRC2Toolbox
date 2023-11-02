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

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;

public class TwoDimDataPlotParameterObject {

	protected FileSortingOrder sortingOrder; 
	protected ChartColorOption chartColorOption;
	protected PlotDataGrouping groupingType;
	protected ExperimentDesignFactor category;
	protected ExperimentDesignFactor subCategory;
	
	public TwoDimDataPlotParameterObject(
			FileSortingOrder sortingOrder, 
			ChartColorOption chartColorOption,
			PlotDataGrouping groupingType, 
			ExperimentDesignFactor category, 
			ExperimentDesignFactor subCategory) {
		super();
		this.sortingOrder = sortingOrder;
		this.chartColorOption = chartColorOption;
		this.groupingType = groupingType;
		this.category = category;
		this.subCategory = subCategory;
	}

	public FileSortingOrder getSortingOrder() {
		return sortingOrder;
	}

	public void setSortingOrder(FileSortingOrder sortingOrder) {
		this.sortingOrder = sortingOrder;
	}

	public ChartColorOption getChartColorOption() {
		return chartColorOption;
	}

	public void setChartColorOption(ChartColorOption chartColorOption) {
		this.chartColorOption = chartColorOption;
	}

	public PlotDataGrouping getGroupingType() {
		return groupingType;
	}

	public void setGroupingType(PlotDataGrouping groupingType) {
		this.groupingType = groupingType;
	}

	public ExperimentDesignFactor getCategory() {
		return category;
	}

	public void setCategory(ExperimentDesignFactor category) {
		this.category = category;
	}

	public ExperimentDesignFactor getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(ExperimentDesignFactor subCategory) {
		this.subCategory = subCategory;
	}
}


