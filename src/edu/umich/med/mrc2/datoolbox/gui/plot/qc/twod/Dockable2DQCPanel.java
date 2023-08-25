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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class Dockable2DQCPanel extends DefaultSingleCDockable implements ActionListener{

	private TwoDqcPlot plotPanel;
	private TwoDqcPlotToolbar plotToolbar;
	private DataPlotControlsPanel dataPlotControlsPanel;

	private static final Icon componentIcon = GuiUtils.getIcon("poxplot", 16);

	public Dockable2DQCPanel(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		plotPanel = new TwoDqcPlot();
		add(plotPanel, BorderLayout.CENTER);
		plotToolbar = new TwoDqcPlotToolbar(this);
		add(plotToolbar, BorderLayout.NORTH);

		dataPlotControlsPanel = new DataPlotControlsPanel(plotPanel);
		add(dataPlotControlsPanel, BorderLayout.EAST);
		
		plotPanel.updateParametersFromToolbar();
		//	TODO update from control panel
	}

	public void loadDataSetStats(Collection<DataFileStatisticalSummary> dataSetStats2) {
		plotPanel.loadDataSetStats(dataSetStats2);
	}

	public synchronized void clearPanel() {
		plotPanel.removeAllDataSets();
	}

	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline activeDataPipeline) {
		plotPanel.removeAllDataSets();
	}

	public TwoDqcPlot getPlotPanel() {
		return plotPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void setSidePanelVisible(boolean b) {
		dataPlotControlsPanel.setVisible(b);
	}
}
