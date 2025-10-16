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

package edu.umich.med.mrc2.datoolbox.gui.plot.stats;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jfree.chart.ChartPanel;

import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.ComboBoxRendererWithIcons;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MultiPanelDataPlotToolbar extends PlotToolbar implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1141069124870103442L;
	
	protected static final Icon sortByNameIcon = GuiUtils.getIcon("sortByClusterName", 24);
	protected static final Icon sortByTimeIcon = GuiUtils.getIcon("sortByTime", 24);
	protected static final Icon colorByFileIcon = GuiUtils.getIcon("barChart", 24);
	protected static final Icon colorBySampleTypeIcon = GuiUtils.getIcon("barChartGrouped", 24);
	protected static final Icon sidePanelShowIcon = GuiUtils.getIcon("sidePanelShow", 24);
	protected static final Icon sidePanelHideIcon = GuiUtils.getIcon("sidePanelHide", 24);		

	private MultiPanelDataPlot plot;
	private JComboBox<StatsPlotType> plotTypeComboBox;
	private JComboBox<DataScale> dataScaleComboBox;
	private FileSortingOrder fileSortingOrder;
	private ChartColorOption chartColorOption;
	private JButton sortOrderButton;
	private JButton colorOptionButton;
	private JButton sidePanelButton;
	
	public MultiPanelDataPlotToolbar(
			MultiPanelDataPlot plot, 
			ActionListener secondaryListener,
			ItemListener itemListener) {

		super(plot);
		this.plot = plot;
		xAxisUnits = "design";

		createLegendToggle();

		addSeparator(buttonDimension);

		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, ChartPanel.ZOOM_RESET_BOTH_COMMAND,
				"Reset zoom", buttonDimension);

		addSeparator(buttonDimension);
		
		createServiceBlock();

		toggleLegendIcon(plot.isLegendVisible());

		// Add plot type options
		addSeparator(buttonDimension);
		//	add(new JLabel("Plot type: "));
		plotTypeComboBox = new JComboBox<StatsPlotType>();
		plotTypeComboBox.setModel(
				new DefaultComboBoxModel<StatsPlotType>(new StatsPlotType[] {
						StatsPlotType.BARCHART,
						StatsPlotType.LINES,
						StatsPlotType.SCATTER,
//						StatsPlotType.BOXPLOT_BY_FEATURE,
//						StatsPlotType.BOXPLOT_BY_GROUP,						
						}));	//	TODO fix boxplot
		plotTypeComboBox.setSelectedItem(StatsPlotType.LINES);
		plotTypeComboBox.addItemListener(itemListener);
		plotTypeComboBox.setMaximumSize(new Dimension(120, 26));
		
		Map<Object,Icon> imageMap = new HashMap<Object,Icon>();
		imageMap.put(StatsPlotType.BARCHART, GuiUtils.getIcon("barChart", 24));
		imageMap.put(StatsPlotType.LINES, GuiUtils.getIcon("lines", 24));
		imageMap.put(StatsPlotType.SCATTER, GuiUtils.getIcon("scatter", 24));
		imageMap.put(StatsPlotType.BOXPLOT_BY_FEATURE, GuiUtils.getIcon("boxplot", 24));
		imageMap.put(StatsPlotType.BOXPLOT_BY_GROUP, GuiUtils.getIcon("boxplot", 24));
		
		plotTypeComboBox.setRenderer(
				new ComboBoxRendererWithIcons(imageMap));
		
		add(plotTypeComboBox);		

		// Add data scale options
		add(new JLabel("  Scale: "));
		dataScaleComboBox = new JComboBox<DataScale>();
		dataScaleComboBox.setModel(
				new DefaultComboBoxModel<DataScale>(DataScale.values()));
		dataScaleComboBox.setSelectedItem(DataScale.RAW);
		dataScaleComboBox.addItemListener(itemListener);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));
		add(dataScaleComboBox);
		
		addSeparator(buttonDimension);
		
		// Add file sorting options
		fileSortingOrder = FileSortingOrder.NAME;
		sortOrderButton = GuiUtils.addButton(
				this, null, sortByNameIcon, this, 
				MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName(), 
				MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName(), 
				buttonDimension);
		add(sortOrderButton);
		
		chartColorOption = ChartColorOption.BY_SAMPLE_TYPE;
		colorOptionButton = GuiUtils.addButton(
				this, null, colorBySampleTypeIcon, this, 
				MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName(), 
				MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName(), 
				buttonDimension);
		add(sortOrderButton);
		
		addSeparator(buttonDimension);
		
		add(Box.createHorizontalGlue());
		
		sidePanelButton = GuiUtils.addButton(
				this, null, sidePanelHideIcon, secondaryListener, 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				buttonDimension);
		sidePanelButton.addActionListener(this);
		add(sidePanelButton);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName()))
			sortDataByFileName();
		
		if(command.equals(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName()))
			sortDataByInjectionTime();
			
		if(command.equals(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName()))
			colorByDataFile();
		
		if(command.equals(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName()))
			colorBySampleType();
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName())) 
			setSidePanelVisible(false);
	}
	
	private void sortDataByFileName(){
		
		sortOrderButton.setIcon(sortByNameIcon);
		sortOrderButton.setActionCommand(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		fileSortingOrder = FileSortingOrder.NAME;
		updatePlot();
	}
	
	private void sortDataByInjectionTime(){
		
		sortOrderButton.setIcon(sortByTimeIcon);
		sortOrderButton.setActionCommand(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		fileSortingOrder = FileSortingOrder.TIMESTAMP;
		updatePlot();
	}
	
	private void colorByDataFile(){
		
		colorOptionButton.setIcon(colorByFileIcon);
		colorOptionButton.setActionCommand(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName());
		colorOptionButton.setToolTipText(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName());
		chartColorOption = ChartColorOption.BY_FILE;
		updatePlot();
	}
	
	private void colorBySampleType(){
		
		colorOptionButton.setIcon(colorBySampleTypeIcon);
		colorOptionButton.setActionCommand(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName());
		colorOptionButton.setToolTipText(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName());
		chartColorOption = ChartColorOption.BY_SAMPLE_TYPE;
		updatePlot();
	}
	
	private void setSidePanelVisible(boolean b){
		
		if(b) {
			sidePanelButton.setIcon(sidePanelHideIcon);
			sidePanelButton.setActionCommand(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setToolTipText(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
		}
		else {
			sidePanelButton.setIcon(sidePanelShowIcon);
			sidePanelButton.setActionCommand(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setToolTipText(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
		}
	}

//	private void toggleItemListeners(boolean enabled) {
//		
//		if(enabled) {			
//			plotTypeComboBox.addItemListener(this);
//			dataScaleComboBox.addItemListener(this);
//		}
//		else {
//			plotTypeComboBox.removeItemListener(this);
//			dataScaleComboBox.removeItemListener(this);
//		}
//	}

//	@Override
//	public void itemStateChanged(ItemEvent e) {
//		
//		if(e.getStateChange() == ItemEvent.SELECTED) {
//			
//			toggleItemListeners(false);
//			updatePlotType();
//			toggleItemListeners(true);			
//		}		
//	}
	
//	private void updatePlotType() {
//		
//		plot.updatePlotType();
//		plot.redrawPlot();
//	}
	
	private void updatePlot() {
		
		plot.updateParametersFromControls();
		plot.redrawPlot();
	}

	public StatsPlotType getStatsPlotType() {
		return (StatsPlotType) plotTypeComboBox.getSelectedItem();
	}

	public void setStatsPlotType(StatsPlotType type) {
		plotTypeComboBox.setSelectedItem(type);
	}

	public FileSortingOrder getSortingOrder() {
		return fileSortingOrder;
	}

	public void setSortingOrder(FileSortingOrder order) {

		fileSortingOrder = order;
		if(fileSortingOrder.equals(FileSortingOrder.NAME)) {
			sortOrderButton.setIcon(sortByNameIcon);
			sortOrderButton.setActionCommand(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
			sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		}
		if(fileSortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			sortOrderButton.setIcon(sortByTimeIcon);
			sortOrderButton.setActionCommand(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
			sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		}
	}
		
	public DataScale getDataScale() {
		return (DataScale) dataScaleComboBox.getSelectedItem();
	}
	
	public void setDataScale(DataScale scale) {
		dataScaleComboBox.setSelectedItem(scale);
	}
	
	public ChartColorOption getChartColorOption() {
		return chartColorOption;
	}

	public void setChartColorOption(ChartColorOption chartColorOption) {
		this.chartColorOption = chartColorOption;
	}
}













