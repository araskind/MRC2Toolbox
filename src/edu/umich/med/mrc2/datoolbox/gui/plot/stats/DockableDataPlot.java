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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.TwoDqcPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableDataPlot extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("boxplot", 16);

	private MultiPanelDataPlot dataPlot;
	private TwoDqcPlotToolbar plotToolbar;
	private DataPlotControlsPanel dataPlotControlsPanel;

	public DockableDataPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		dataPlot = new MultiPanelDataPlot();
		add(dataPlot, BorderLayout.CENTER);
		
		plotToolbar = new TwoDqcPlotToolbar(dataPlot, this, false);
		dataPlot.setToolbar(plotToolbar);
		add(plotToolbar, BorderLayout.NORTH);
		
		dataPlotControlsPanel = new DataPlotControlsPanel(dataPlot);
		add(dataPlotControlsPanel, BorderLayout.EAST);
		dataPlot.setDataPlotControlsPanel(dataPlotControlsPanel);
		
		dataPlot.updateParametersFromControls();
	}	
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(false);
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
	}	
	
	public void setSidePanelVisible(boolean b) {
		dataPlotControlsPanel.setVisible(b);
	}


	public void loadMultipleFeatureData(
			Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap) {

		if(selectedFeaturesMap.isEmpty()) {
			dataPlot.clearPlotPanel();
			return;
		}
		dataPlot.loadMultipleFeatureData(selectedFeaturesMap);		
	}

	public void clearPlotPanel() {
		dataPlot.clearPlotPanel();
	}

	public void setActiveDesign(ExperimentDesignSubset activeSubset) {

		dataPlotControlsPanel.populateCategories(activeSubset);
		dataPlot.redrawPlot();
	}
}
