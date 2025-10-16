/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import javax.swing.Icon;

import org.jfree.chart.ChartPanel;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.DataPlotControlsPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public abstract class DockableMSFeatureQCPlotPanel extends DefaultSingleCDockable implements ActionListener {

	protected static final Icon sortByNameIcon = GuiUtils.getIcon("sortByClusterName", 16);
	protected static final Icon sortByTimeIcon = GuiUtils.getIcon("sortByTime", 16);
	protected static final Icon colorByFileIcon = GuiUtils.getIcon("barChart", 16);
	protected static final Icon colorBySampleTypeIcon = GuiUtils.getIcon("barChartGrouped", 16);
	protected static final Icon sidePanelShowIcon = GuiUtils.getIcon("sidePanelShow", 16);
	protected static final Icon sidePanelHideIcon = GuiUtils.getIcon("sidePanelHide", 16);	
	protected static final Icon msmsIcon = GuiUtils.getIcon("msms", 16);	
	protected static final Icon peakIcon = GuiUtils.getIcon("smoothChromatogram", 16);
	protected static final Icon qualityIcon = GuiUtils.getIcon("dial", 16);	
	
	protected static final Icon showLegendIcon = GuiUtils.getIcon("showLegend", 24);
	protected static final Icon hideLegendIcon = GuiUtils.getIcon("hiddenLegend", 24);
	protected static final Icon autoRangeIcon = GuiUtils.getIcon("fitAll", 16);
	protected static final Icon copyIcon = GuiUtils.getIcon("clipBoard", 16);	
	
	protected SimpleButtonAction sortOrderButton;
	protected SimpleButtonAction colorOptionButton;
	protected SimpleButtonAction sidePanelButton;
	protected SimpleButtonAction toggleLegendButton;
	
	protected FileSortingOrder sortingOrder; 
	protected ChartColorOption chartColorOption;
	protected DataAnalysisProject currentExperiment;
	protected DataPipeline dataPipeline;
	protected DataPlotControlsPanel dataPlotControlsPanel;
	
	protected MsFeature activeFeature;
	protected Map<DataFile, SimpleMsFeature> activeFileFeatureMap;
	protected MSQualityDataPlotParameterObject plotParametersObject;
	
	public DockableMSFeatureQCPlotPanel(String id, String title, Icon icon) {
		
		super(id, icon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
	}
	
	protected void initButtons(boolean isSinglePlot) {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));
		
		SimpleButtonAction autorangeButton = GuiUtils.setupButtonAction(
				"Autorange",
				ChartPanel.ZOOM_RESET_BOTH_COMMAND, 
				autoRangeIcon, this);
		actions.add(autorangeButton);
		
		if(isSinglePlot) {
			
			SimpleButtonAction copyButton = GuiUtils.setupButtonAction(
					ChartPanel.COPY_COMMAND,
					ChartPanel.COPY_COMMAND,
					copyIcon, this);
			actions.add(copyButton);
			
			toggleLegendButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName(), 
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName(), 
					hideLegendIcon, this);
			actions.add(toggleLegendButton);
		}
		actions.addSeparator();
		
		sortOrderButton = GuiUtils.setupButtonAction(
				MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName(), 
				MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName(), 
				sortByTimeIcon, this);
		actions.add(sortOrderButton);
		
		colorOptionButton = GuiUtils.setupButtonAction(
				MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName(), 
				MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName(), 
				colorBySampleTypeIcon, this);		
		actions.add(colorOptionButton);
		
		actions.addSeparator();
		
		sidePanelButton= GuiUtils.setupButtonAction(
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				sidePanelHideIcon, this);
		actions.add(sidePanelButton);
		
		actions.addSeparator();
		
		intern().setActionOffers(actions);
	}	
	
	protected void toggleLegendIcon(boolean isLegendVisible) {

		if(toggleLegendButton == null)
			return;
		
		if (isLegendVisible) {

			toggleLegendButton.setIcon(showLegendIcon);
			toggleLegendButton.setCommand(
					MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName());
			toggleLegendButton.setText(
					MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName());
		} 
		else {			
			toggleLegendButton.setIcon(hideLegendIcon);
			toggleLegendButton.setCommand(
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName());
			toggleLegendButton.setText(
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName());
		}		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals(ChartPanel.ZOOM_RESET_BOTH_COMMAND))
			restorePlotAutoBounds();
		
		if (command.equals(ChartPanel.COPY_COMMAND))
			copyPlotToClipboard();

		if (command.equals(MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName())) {
			showPlotLegend(true);
			toggleLegendIcon(true);
		}
		if (command.equals(MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName())) { 
			showPlotLegend(false);
			toggleLegendIcon(false);
		}
		
		if(command.equals(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName()))
			sortDataByFileName();
		
		if(command.equals(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName()))
			sortDataByInjectionTime();
			
		if(command.equals(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName()))
			colorByDataFile();
		
		if(command.equals(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName()))
			colorBySampleType();
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(false);
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
	}
	
	protected void setSidePanelVisible(boolean b) {
		
		dataPlotControlsPanel.setVisible(b);
		if(b) {
			sidePanelButton.setIcon(sidePanelHideIcon);
			sidePanelButton.setCommand(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setText(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setTooltip(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
		}
		else {
			sidePanelButton.setIcon(sidePanelShowIcon);
			sidePanelButton.setCommand(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setText(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setTooltip(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
		}
	}
	
	protected void sortDataByFileName(){
		
		sortOrderButton.setIcon(sortByNameIcon);
		sortOrderButton.setCommand(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		sortOrderButton.setText(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		sortOrderButton.setTooltip(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		sortingOrder = FileSortingOrder.NAME;
		updatePlot();
	}

	protected void sortDataByInjectionTime(){
		
		sortOrderButton.setIcon(sortByTimeIcon);
		sortOrderButton.setCommand(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		sortOrderButton.setText(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		sortOrderButton.setTooltip(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		sortingOrder = FileSortingOrder.TIMESTAMP;
		updatePlot();
	}
	
	protected void colorByDataFile(){
		
		colorOptionButton.setIcon(colorByFileIcon);
		colorOptionButton.setCommand(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName());
		colorOptionButton.setText(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName());
		colorOptionButton.setTooltip(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName());
		chartColorOption = ChartColorOption.BY_FILE;
		updatePlot();
	}
	
	private void colorBySampleType(){
		
		colorOptionButton.setIcon(colorBySampleTypeIcon);
		colorOptionButton.setCommand(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName());
		colorOptionButton.setText(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName());
		colorOptionButton.setTooltip(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName());
		chartColorOption = ChartColorOption.BY_SAMPLE_TYPE;
		updatePlot();
	}
	
	public void setCurrentExperiment(DataAnalysisProject currentExperiment) {
		this.currentExperiment = currentExperiment;
	}

	public void setDataPipeline(DataPipeline dataPipeline) {
		this.dataPipeline = dataPipeline;
	}
	
	protected abstract void restorePlotAutoBounds();
	
	protected abstract void showPlotLegend(boolean doShow);
	
	protected abstract void copyPlotToClipboard();
	
	public abstract void clearPanel();
	
	public void loadFeatureData(
			MsFeature feature, 
			Map<DataFile, SimpleMsFeature> fileFeatureMap) {
		
		this.activeFeature = feature;
		this.activeFileFeatureMap = fileFeatureMap;		
		plotParametersObject = new MSQualityDataPlotParameterObject(
				activeFeature,
				activeFileFeatureMap,
				dataPlotControlsPanel.getCategory(),
				dataPlotControlsPanel.getSububCategory(),
				dataPlotControlsPanel.getDataGroupingType(), 
				sortingOrder, 
				chartColorOption);
	}
	
	protected abstract void updatePlot();
}
