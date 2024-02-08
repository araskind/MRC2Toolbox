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

package edu.umich.med.mrc2.datoolbox.gui.datexp.hm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;

import org.ujmp.core.Matrix;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.HeatChartType;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.JFHeatChart;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.FeatureHeatMapDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DockableFeatureHeatMapPanel extends DefaultSingleCDockable implements ActionListener, ItemListener {

	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 16);
	private MsFeatureSet featureSet;
	private JFHeatChart heatChart;
	private FeatureHeatchartToolbar toolbar;
	private FeatureHeatchartSettingsPanel chartSettingsPanel;
	
	public DockableFeatureHeatMapPanel() {

		super("DockableFeatureHeatMapPanel", heatmapIcon, 
				"Feature heatmap", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);		
		setLayout(new BorderLayout(0, 0));
		heatChart = new JFHeatChart(HeatChartType.FeatureSetHeatmap);
		add(heatChart, BorderLayout.CENTER);
		
		toolbar = new FeatureHeatchartToolbar(heatChart, this);
		add(toolbar, BorderLayout.NORTH);
		
		chartSettingsPanel = new FeatureHeatchartSettingsPanel(this, this);
		add(chartSettingsPanel, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			chartSettingsPanel.setVisible(true);
				
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName())) 
			chartSettingsPanel.setVisible(false);
	}
	
	public void clearPanel() {
		//	TODO
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			
			RedrawPlotTask task = new RedrawPlotTask(e.getItem());
			IndeterminateProgressDialog idp = 
					new IndeterminateProgressDialog(
							"Updating plot ...", this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
		}
	}

	public void createFeatureHeatMap(MsFeatureSet subset) {

		RedrawPlotTask task = new RedrawPlotTask(subset);
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog(
						"Loading feature set " + subset.getName(), this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class RedrawPlotTask extends LongUpdateTask {

		Object redrawTrigger;
		
		public RedrawPlotTask(Object redrawTrigger) {
			this.redrawTrigger = redrawTrigger;
		}

		@Override
		public Void doInBackground() {

			if(redrawTrigger instanceof MsFeatureSet) {
				loadFeatureCollection((MsFeatureSet)redrawTrigger);
			}	
			else if (redrawTrigger instanceof TableRowSubset) {
				//	TODO;
			}
			else {
				redrawPlot();
			}
			return null;
		}
	}
	
	private void loadFeatureCollection(MsFeatureSet featureSet) {

		heatChart.removeAllDataSets();
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Matrix featureSubsetMatrix = 
				experiment.getDataMatrixForFeatureSetAndDesign(
						featureSet, 
						experiment.getExperimentDesign().getActiveDesignSubset(), 
						experiment.getActiveDataPipeline());
		FeatureHeatMapDataSet dataSet = 
				new FeatureHeatMapDataSet(featureSubsetMatrix);
		
		MZRTPlotParameterObject plotParams = chartSettingsPanel.getPlotParameters();				
		heatChart.showFeatureHeatMap(dataSet, plotParams);
	}
	
	private void redrawPlot() {
		// TODO Auto-generated method stub
		
	}
}







