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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.Icon;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableMultispectraPlotPanel extends DockableMSFeatureQCPlotPanel{

	private static final Icon multiSpectraIcon = GuiUtils.getIcon("multiSpectra", 16);	
	protected MultispectraPlotPanel mspPlotPanel;
	
	public DockableMultispectraPlotPanel() {

		super("DockableMultispectrumPlotPanel", "Mass spectra for individual features", multiSpectraIcon);

		mspPlotPanel = new MultispectraPlotPanel();
		add(mspPlotPanel, BorderLayout.CENTER);
		
		dataPlotControlsPanel = new DataPlotControlsPanel(mspPlotPanel);
		add(dataPlotControlsPanel, BorderLayout.EAST);
		mspPlotPanel.setDataPlotControlsPanel(dataPlotControlsPanel);
		mspPlotPanel.updateParametersFromControls();
		
		initButtons(false);
	}	
	
	@Override
	public void actionPerformed(ActionEvent e) {

		super.actionPerformed(e);
		
		String command = e.getActionCommand();
	}
	
	public void scrollToSelectedPlot(Object plottedObject) {		
		mspPlotPanel.scrollToSelectedPlot(plottedObject);
	}
	
	protected void updatePlot() {
		// TODO Auto-generated method stub
		mspPlotPanel.updateParametersFromControls();
	}

	public void loadFeatureData(
			MsFeature feature,
			Map<DataFile, SimpleMsFeature> fileFeatureMap) {

		super.loadFeatureData(feature, fileFeatureMap);
		
		mspPlotPanel.showFeatureData(currentExperiment, plotParametersObject);
	}
	
	public void clearPanel() {
		mspPlotPanel.clearPanel();
	}

	@Override
	protected void restorePlotAutoBounds() {
		mspPlotPanel.restoreAllPlotsAutoBounds();
	}

	@Override
	protected void showPlotLegend(boolean doShow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void copyPlotToClipboard() {
		// TODO Auto-generated method stub
		
	}
}
