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
import java.awt.event.ActionListener;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;

public class DockableDataVariationPlotPanel extends DockableMSFeatureQCPlotPanel implements ActionListener{

	private LCMSPlotType plotType;	
	protected FeaturePropertiesTimelinePlot featurePropertiesTimelinePlot;
	
	public DockableDataVariationPlotPanel(LCMSPlotType plotType) {

		super("DockableDataVariationPlotPanel" + plotType.name(), "", sortByTimeIcon);
		this.plotType = plotType;
		if(this.plotType.equals(LCMSPlotType.MZ)) {
			
	       setTitleIcon(msmsIcon);
	       setTitleText("MZ values for individual features");
		}
		if(this.plotType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {
			
		       setTitleIcon(peakIcon);
		       setTitleText("RT and peak width values for individual features");
		}
		if(this.plotType.equals(LCMSPlotType.FEATURE_QUALITY)) {
			
		       setTitleIcon(qualityIcon);
		       setTitleText("Feature quality scores");
		}
		initButtons(true);
		
		featurePropertiesTimelinePlot = new FeaturePropertiesTimelinePlot(plotType);
		add(featurePropertiesTimelinePlot, BorderLayout.CENTER);
		toggleLegendIcon(featurePropertiesTimelinePlot.isLegendVisible());
		featurePropertiesTimelinePlot.hideAnnotations();
		
		dataPlotControlsPanel = new DataPlotControlsPanel(featurePropertiesTimelinePlot);
		add(dataPlotControlsPanel, BorderLayout.EAST);
		featurePropertiesTimelinePlot.setDataPlotControlsPanel(dataPlotControlsPanel);
		featurePropertiesTimelinePlot.updateParametersFromControls();
		
		sortingOrder = FileSortingOrder.TIMESTAMP;
		chartColorOption = ChartColorOption.BY_SAMPLE_TYPE;		
	}

	public void clearPanel() {
		featurePropertiesTimelinePlot.removeAllDataSets();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		super.actionPerformed(e);
		
		String command = e.getActionCommand();
	}
	
	public void loadFeatureData(
			MsFeature feature, 
			Map<DataFile, SimpleMsFeature> fileFeatureMap) {

		super.loadFeatureData(feature, fileFeatureMap);
		featurePropertiesTimelinePlot.showFeatureData(
				currentExperiment, plotParametersObject);
	}
	
	protected void updatePlot() {
		
		super.loadFeatureData(activeFeature, activeFileFeatureMap);
		featurePropertiesTimelinePlot.showFeatureData(
				currentExperiment, plotParametersObject);
	}

	@Override
	protected void restorePlotAutoBounds() {

		featurePropertiesTimelinePlot.restoreAutoBounds();
	}

	@Override
	protected void showPlotLegend(boolean doShow) {

		if(doShow) {			
			featurePropertiesTimelinePlot.showLegend();
			toggleLegendIcon(true);
		}
		else {
			featurePropertiesTimelinePlot.hideLegend();
			toggleLegendIcon(false);
		}		
	}

	@Override
	protected void copyPlotToClipboard() {

		try {
			featurePropertiesTimelinePlot.doCopy();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}
	}

	public FeaturePropertiesTimelinePlot getPlotPanel() {
		return featurePropertiesTimelinePlot;
	}
}
