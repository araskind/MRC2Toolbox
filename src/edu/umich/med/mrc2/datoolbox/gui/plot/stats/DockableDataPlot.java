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

package edu.umich.med.mrc2.datoolbox.gui.plot.stats;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDataPlot extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("boxplot", 16);

	private MultiPanelDataPlot dataPlot;
	private MultiPanelDataPlotToolbar plotToolbar;

	public DockableDataPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		dataPlot = new MultiPanelDataPlot(StatsPlotType.BARCHART, PlotDataType.FEATURE_DATA);
		dataPlot.setSortingOrder(FileSortingOrder.SAMPLE_NAME);
		add(dataPlot, BorderLayout.CENTER);
		plotToolbar = new MultiPanelDataPlotToolbar(dataPlot);
		add(plotToolbar, BorderLayout.NORTH);
	}

	public FileSortingOrder getFileOrder() {
		return plotToolbar.getFileOrder();
	}

	public StatsPlotType getPlotType() {
		return plotToolbar.getPlotType();
	}

	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		dataPlot.loadDataSetStats(dataSetStats2);
	}

	public void loadMultipleFeatureData(
			Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap) {

		if(!selectedFeaturesMap.isEmpty()) {

			dataPlot.loadMultipleFeatureData(
					selectedFeaturesMap,
					plotToolbar.getDataGroupingType(),
					plotToolbar.getCategory(),
					plotToolbar.getSubCategory());
		}
	}

	public void clearPlotPanel() {

		dataPlot.clearPlotPanel();
//		plotToolbar.setPlotType(StatsPlotType.BARCHART);
//		plotToolbar.setFileOrder(FileSortingOrder.SAMPLE_NAME);
	}

	public void setFileOrder(FileSortingOrder order) {

		plotToolbar.setFileOrder(order);
	}

	public void setPlotType(StatsPlotType type) {

		plotToolbar.setPlotType(type);
	}

	public void setActiveDesign(ExperimentDesignSubset activeSubset) {

		plotToolbar.populateCategories(activeSubset);
		dataPlot.redrawPlot();
	}
}
