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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jfree.chart.ChartPanel;

import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.QcPlotType;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class TwoDqcPlotToolbar extends PlotToolbar implements ItemListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1141069124870103442L;

	private JComboBox<DataSetQcField> statParameterComboBox;
	private ItemListener itemListener;
	private TwoDqcPlot plot;
	private JComboBox<QcPlotType> plotTypeComboBox;
	private JComboBox<FileSortingOrder> fileSortComboBox;
	private JComboBox<DataScale> dataScaleComboBox;

	public TwoDqcPlotToolbar(TwoDqcPlot parentPlot) {

		super(parentPlot);

		plot = parentPlot;
		plot.setToolbar(this);
		commandListener = parentPlot;
		itemListener = parentPlot;

		xAxisUnits = "design";

		createLegendToggle();

		addSeparator(buttonDimension);

		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, ChartPanel.ZOOM_RESET_BOTH_COMMAND,
				"Reset zoom", buttonDimension);

		addSeparator(buttonDimension);
		
		createServiceBlock();

		toggleLegendIcon(parentPlot.isLegendVisible());

		// Add plot type options
		addSeparator(buttonDimension);
		add(new JLabel("Plot type: "));
		plotTypeComboBox = new JComboBox<QcPlotType>();
		plotTypeComboBox.setModel(
				new DefaultComboBoxModel<QcPlotType>(QcPlotType.values()));
		plotTypeComboBox.setSelectedItem(QcPlotType.BARCHART);
		plotTypeComboBox.addItemListener(this);
		plotTypeComboBox.setMaximumSize(new Dimension(120, 26));
		add(plotTypeComboBox);

		// Add file sorting options
		add(new JLabel("  Sort files by: "));
		fileSortComboBox = new JComboBox<FileSortingOrder>();
		fileSortComboBox.setModel(
				new DefaultComboBoxModel<FileSortingOrder>(
						new FileSortingOrder[] {
								FileSortingOrder.NAME, 
								FileSortingOrder.TIMESTAMP}));
		fileSortComboBox.setSelectedItem(FileSortingOrder.NAME);
		fileSortComboBox.addItemListener(this);
		fileSortComboBox.setMaximumSize(new Dimension(120, 26));
		add(fileSortComboBox);

		// Add data scale options
		add(new JLabel("  Scale: "));
		dataScaleComboBox = new JComboBox<DataScale>();
		dataScaleComboBox.setModel(
				new DefaultComboBoxModel<DataScale>(DataScale.values()));
		dataScaleComboBox.setSelectedItem(DataScale.RAW);
		dataScaleComboBox.addItemListener(this);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));
		add(dataScaleComboBox);
		
		addSeparator(buttonDimension);
		//	QC fields
		add(new JLabel("  Parameter: "));
		statParameterComboBox = new JComboBox<DataSetQcField>();
		statParameterComboBox.setModel(
				new DefaultComboBoxModel<DataSetQcField>(DataSetQcField.values()));	
		statParameterComboBox.setSelectedItem(DataSetQcField.OBSERVATIONS);
		statParameterComboBox.addItemListener(this);
		statParameterComboBox.setMaximumSize(new Dimension(120, 26));
		add(statParameterComboBox);
		
		toggleItemListeners(true);
	}
	
	private void toggleItemListeners(boolean enabled) {
		
		if(enabled) {
			
			plotTypeComboBox.addItemListener(this);
			fileSortComboBox.addItemListener(this);
			dataScaleComboBox.addItemListener(this);
			statParameterComboBox.addItemListener(this);	
		}
		else {
			plotTypeComboBox.removeItemListener(this);
			fileSortComboBox.removeItemListener(this);
			dataScaleComboBox.removeItemListener(this);
			statParameterComboBox.removeItemListener(this);	
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			toggleItemListeners(false);
			
			if(e.getSource().equals(plotTypeComboBox)) {
				
				DataScale scale = getDataScale();

				if(getQcPlotType().equals(QcPlotType.BOXPLOT)) {
					
					statParameterComboBox.setModel(
							new DefaultComboBoxModel<DataSetQcField>(
									new DataSetQcField[] {DataSetQcField.RAW_VALUES}));
					dataScaleComboBox.setSelectedItem(DataScale.RAW);
				}
				else {
					statParameterComboBox.setModel(
							new DefaultComboBoxModel<DataSetQcField>(DataSetQcField.values()));
					statParameterComboBox.removeItem(DataSetQcField.RAW_VALUES);
					dataScaleComboBox.setSelectedItem(scale);
				}
			}	
			if(e.getSource().equals(dataScaleComboBox)) {
				
				if(!getDataScale().equals(DataScale.RAW)  && getQcPlotType().equals(QcPlotType.BOXPLOT))
					plotTypeComboBox.setSelectedItem(QcPlotType.BARCHART);				
			}
			toggleItemListeners(true);
			updatePlot();
		}		
	}
	
	private void updatePlot() {
		
		plot.updateParametersFromToolbar();
		plot.redrawPlot();
	}

	public QcPlotType getQcPlotType() {

		return (QcPlotType) plotTypeComboBox.getSelectedItem();
	}

	public void setQcPlotType(QcPlotType type) {

		plotTypeComboBox.setSelectedItem(type);
	}

	public FileSortingOrder getSortingOrder() {

		return (FileSortingOrder) fileSortComboBox.getSelectedItem();
	}

	public void setSortingOrder(FileSortingOrder order) {

		fileSortComboBox.setSelectedItem(order);
	}
		
	public DataScale getDataScale() {

		return (DataScale) dataScaleComboBox.getSelectedItem();
	}
	
	public void setDataScale(DataScale scale) {

		dataScaleComboBox.setSelectedItem(scale);
	}
	
	public DataSetQcField getStatParameter() {

		return (DataSetQcField) statParameterComboBox.getSelectedItem();
	}
	
	public void setStatParameter(DataSetQcField field) {

		statParameterComboBox.setSelectedItem(field);
	}
}













