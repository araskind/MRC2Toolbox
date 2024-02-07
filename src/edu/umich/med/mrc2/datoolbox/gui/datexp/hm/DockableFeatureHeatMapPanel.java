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

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.HeatChartType;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.JFHeatChart;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

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
		// TODO Auto-generated method stub
		if (e.getStateChange() == ItemEvent.SELECTED) {
			
		}
	}

	public void createFeatureHeatMap(MsFeatureSet subset) {
		// TODO Auto-generated method stub
		
	}
}







