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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartPanel;

import edu.umich.med.mrc2.datoolbox.data.compare.ChartColorOption;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.StatsPlotType;
import edu.umich.med.mrc2.datoolbox.gui.utils.ComboBoxRendererWithIcons;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MultiPanelDataPlotToolbar extends PlotToolbar implements ItemListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1141069124870103442L;
	
	protected static final Icon sortIcon = GuiUtils.getIcon("sortBy", 24);
	protected static final Icon sortBySampleNameIcon = GuiUtils.getIcon("sortByClusterName", 24);
	protected static final Icon sortByTimeIcon = GuiUtils.getIcon("clock", 24);
	protected static final Icon sortByFileNameIcon = GuiUtils.getIcon("sortByFileName", 24);
	protected static final Icon colorByFileIcon = GuiUtils.getIcon("barChart", 24);
	protected static final Icon colorBySampleTypeIcon = GuiUtils.getIcon("barChartGrouped", 24);
	protected static final Icon sidePanelShowIcon = GuiUtils.getIcon("sidePanelShow", 24);
	protected static final Icon sidePanelHideIcon = GuiUtils.getIcon("sidePanelHide", 24);				

	private JComboBox<DataSetQcField> statParameterComboBox;
	private AbstractControlledDataPlot plot;
	private JComboBox<StatsPlotType> plotTypeComboBox;
	private JComboBox<DataScale> dataScaleComboBox;
	private FileSortingOrder fileSortingOrder;
	private ChartColorOption chartColorOption;
	private JButton sortOrderButton;
	private JButton colorOptionButton;
	private JButton sidePanelButton;
	
	private JPopupMenu orderMenu;
	private JMenuItem sortByInjectionTimeMenuItem;
	private JMenuItem sortByFileNameMenuItem;
	private JMenuItem sortBySampleNameMenuItem;
	
	public MultiPanelDataPlotToolbar(
			AbstractControlledDataPlot plot, 
			ActionListener secondaryListener,
			boolean includeQcFieldSelector) {

		super(plot);
		this.plot = plot;
		xAxisUnits = "design";

		createLegendToggle();
		addSeparator(buttonDimension);

		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, 
				ChartPanel.ZOOM_RESET_BOTH_COMMAND, "Reset zoom", buttonDimension);
		addSeparator(buttonDimension);
		
		createServiceBlock();
		toggleLegendIcon(plot.isLegendVisible());		
		addSeparator(buttonDimension);	
		
		createPlotTypeBlock();
		createDataScaleBlock();		
		addSeparator(buttonDimension);
		
		createColorControlBlock();		
		createSortingBlock();		
		addSeparator(buttonDimension);
		
		if(includeQcFieldSelector)
			createQcFieldSelector();
		
		add(Box.createHorizontalGlue());
		
		sidePanelButton = GuiUtils.addButton(
				this, null, sidePanelHideIcon, secondaryListener, 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				buttonDimension);
		sidePanelButton.addActionListener(this);
		add(sidePanelButton);
	}
	
	private void createPlotTypeBlock() {
		
		plotTypeComboBox = new JComboBox<>();
		plotTypeComboBox.setModel(
				new DefaultComboBoxModel<>(new StatsPlotType[] {
						StatsPlotType.BARCHART,
						StatsPlotType.LINES,
						StatsPlotType.SCATTER,
						StatsPlotType.BOXPLOT
						}));
		plotTypeComboBox.setSelectedItem(StatsPlotType.BARCHART);
		plotTypeComboBox.addItemListener(this);
		plotTypeComboBox.setMaximumSize(new Dimension(120, 26));
		
		Map<Object,Icon> imageMap = new HashMap<>();
		imageMap.put(StatsPlotType.BARCHART, GuiUtils.getIcon("barChart", 24));
		imageMap.put(StatsPlotType.BOXPLOT, GuiUtils.getIcon("boxplot", 24));
		imageMap.put(StatsPlotType.LINES, GuiUtils.getIcon("lines", 24));
		imageMap.put(StatsPlotType.SCATTER, GuiUtils.getIcon("scatter", 24));
		
		ComboBoxRendererWithIcons qcPlotTypeRenderer = 
				new ComboBoxRendererWithIcons(imageMap);
		plotTypeComboBox.setRenderer(qcPlotTypeRenderer);
		
		add(plotTypeComboBox);
	}
	
	private void createDataScaleBlock(){
		
		add(new JLabel("  Scale: "));
		dataScaleComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(DataScale.values()));
		dataScaleComboBox.setSelectedItem(DataScale.RAW);
		dataScaleComboBox.addItemListener(this);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));
		add(dataScaleComboBox);
	}
	
	private void createColorControlBlock() {
		
		chartColorOption = ChartColorOption.BY_SAMPLE_TYPE;
		colorOptionButton = GuiUtils.addButton(
				this, null, colorBySampleTypeIcon, this, 
				MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName(), 
				MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName(), 
				buttonDimension);
		
	}
	
	private void createSortingBlock(){
		
		orderMenu = new JPopupMenu("Data order");
		fileSortingOrder = FileSortingOrder.NAME;
		
		sortByFileNameMenuItem = GuiUtils.addMenuItem(orderMenu,
				MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName(), this,
				MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName(), sortByFileNameIcon);
		
		sortByInjectionTimeMenuItem = GuiUtils.addMenuItem(orderMenu,
				MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName(), this,
				MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName(), sortByTimeIcon);

		sortBySampleNameMenuItem = GuiUtils.addMenuItem(orderMenu,
				MainActionCommands.SORT_BY_SAMPLE_NAME_COMMAND.getName(), this,
				MainActionCommands.SORT_BY_SAMPLE_NAME_COMMAND.getName(), sortBySampleNameIcon);

		sortOrderButton = GuiUtils.addButton(
				this, "Data order", sortByFileNameIcon, null, null, null, new Dimension(105, 35));
		
		sortOrderButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				orderMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	private void createQcFieldSelector() {
		
		add(new JLabel("  Parameter: "));
		statParameterComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(DataSetQcField.values()));
		statParameterComboBox.setSelectedItem(DataSetQcField.OBSERVATIONS);
		statParameterComboBox.addItemListener(this);
		statParameterComboBox.setMaximumSize(new Dimension(120, 26));
		add(statParameterComboBox);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName()))
			sortDataByFileName();
		
		if(command.equals(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName()))
			sortDataByInjectionTime();
			
		if(command.equals(MainActionCommands.SORT_BY_SAMPLE_NAME_COMMAND.getName()))
			sortDataBySampleName();
			
		if(command.equals(MainActionCommands.COLOR_BY_FILE_NAME_COMMAND.getName()))
			colorByDataFile();
		
		if(command.equals(MainActionCommands.COLOR_BY_SAMPLE_TYPE_COMMAND.getName()))
			colorBySampleType();
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName())) 
			setSidePanelVisible(false);
		
		super.actionPerformed(e);
	}
	
	private void sortDataByFileName(){
		
		sortOrderButton.setIcon(sortByFileNameIcon);
		sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		fileSortingOrder = FileSortingOrder.NAME;
		updatePlot();
	}
	
	private void sortDataByInjectionTime(){
		
		sortOrderButton.setIcon(sortByTimeIcon);
		sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		fileSortingOrder = FileSortingOrder.TIMESTAMP;
		updatePlot();
	}
	
	private void sortDataBySampleName(){
		
		sortOrderButton.setIcon(sortBySampleNameIcon);
		sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_SAMPLE_NAME_COMMAND.getName());
		fileSortingOrder = FileSortingOrder.SAMPLE_NAME;
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

	private void toggleItemListeners(boolean enabled) {
		
		if(enabled) {			
			plotTypeComboBox.addItemListener(this);
			dataScaleComboBox.addItemListener(this);
			
			if(statParameterComboBox != null)
				statParameterComboBox.addItemListener(this);	
		}
		else {
			plotTypeComboBox.removeItemListener(this);
			dataScaleComboBox.removeItemListener(this);
			
			if(statParameterComboBox != null)
				statParameterComboBox.removeItemListener(this);	
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			toggleItemListeners(false);
			
			if(e.getSource().equals(plotTypeComboBox)) {
				
				DataScale scale = getDataScale();

				if(getStatsPlotType().equals(StatsPlotType.BOXPLOT)) {
					
					if(statParameterComboBox != null)
						statParameterComboBox.setModel(
								new DefaultComboBoxModel<DataSetQcField>(
										new DataSetQcField[] {DataSetQcField.RAW_VALUES}));
					
					dataScaleComboBox.setSelectedItem(DataScale.RAW);
				}
				else {
					if(statParameterComboBox != null) {
						
						statParameterComboBox.setModel(
								new DefaultComboBoxModel<DataSetQcField>(DataSetQcField.values()));
						statParameterComboBox.removeItem(DataSetQcField.RAW_VALUES);
					}
					dataScaleComboBox.setSelectedItem(scale);
				}
			}	
			if(e.getSource().equals(dataScaleComboBox)) {
				
				if(!getDataScale().equals(DataScale.RAW)  && getStatsPlotType().equals(StatsPlotType.BOXPLOT))
					plotTypeComboBox.setSelectedItem(StatsPlotType.BARCHART);				
			}
			toggleItemListeners(true);
			updatePlot();
		}		
	}
	
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
			sortOrderButton.setIcon(sortByFileNameIcon);
			sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_FILE_NAME_COMMAND.getName());
		}
		if(fileSortingOrder.equals(FileSortingOrder.TIMESTAMP)) {
			sortOrderButton.setIcon(sortByTimeIcon);
			sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_INJECTION_TIME_COMMAND.getName());
		}
		if(fileSortingOrder.equals(FileSortingOrder.SAMPLE_NAME)) {
			sortOrderButton.setIcon(sortBySampleNameIcon);
			sortOrderButton.setToolTipText(MainActionCommands.SORT_BY_SAMPLE_NAME_COMMAND.getName());
		}		
	}
		
	public DataScale getDataScale() {
		return (DataScale) dataScaleComboBox.getSelectedItem();
	}
	
	public void setDataScale(DataScale scale) {
		dataScaleComboBox.setSelectedItem(scale);
	}
	
	public DataSetQcField getStatParameter() {
		
		if(statParameterComboBox != null)
			return (DataSetQcField) statParameterComboBox.getSelectedItem();
		else
			return null;
	}
	
	public void setStatParameter(DataSetQcField field) {
		
		if(statParameterComboBox != null)
			statParameterComboBox.setSelectedItem(field);
	}

	public ChartColorOption getChartColorOption() {
		return chartColorOption;
	}

	public void setChartColorOption(ChartColorOption chartColorOption) {
		this.chartColorOption = chartColorOption;
	}
}













