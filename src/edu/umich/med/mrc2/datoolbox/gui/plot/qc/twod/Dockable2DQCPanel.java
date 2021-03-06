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

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class Dockable2DQCPanel extends DefaultSingleCDockable{

	private TwoDqcPlot plotPanel;
	private TwoDqcPlotToolbar plotToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("poxplot", 16);

	public Dockable2DQCPanel(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		plotPanel = new TwoDqcPlot();
		add(plotPanel, BorderLayout.CENTER);
		plotToolbar = new TwoDqcPlotToolbar(plotPanel);
		add(plotToolbar, BorderLayout.NORTH);
		plotPanel.updateParametersFromToolbar();
	}

	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		plotPanel.loadDataSetStats(dataSetStats2);
	}

	public synchronized void clearPanel() {
		plotPanel.removeAllDataSets();
	}

	public void updateGuiFromProjectAndDataPipeline(
			DataAnalysisProject currentProject, DataPipeline activeDataPipeline) {
		plotPanel.removeAllDataSets();
	}
}
