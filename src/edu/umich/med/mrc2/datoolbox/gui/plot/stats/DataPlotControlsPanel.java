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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeName;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.plot.ControlledStatsPlot;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class DataPlotControlsPanel extends JPanel implements ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6747485797254654577L;

	private JComboBox groupByComboBox;
	private JComboBox categoryComboBox;
	private JComboBox subCategoryComboBox;	

	private ControlledStatsPlot plot;
	
	@SuppressWarnings("unchecked")
	public DataPlotControlsPanel(ControlledStatsPlot parentPlot) {
		super();

		this.plot = parentPlot;
		
		setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(10, 10, 10, 10)));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Group data by");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		groupByComboBox = new JComboBox<PlotDataGrouping>();
		groupByComboBox.setModel(
				new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
		groupByComboBox.setSelectedItem(PlotDataGrouping.IGNORE_DESIGN);
		groupByComboBox.setMaximumSize(new Dimension(120, 26));
		
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		add(groupByComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Category");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		categoryComboBox = new JComboBox<ExperimentDesignFactor>();
		categoryComboBox.setName(DataTypeName.CATEGORY.name());
		categoryComboBox.setModel(
				new DefaultComboBoxModel<ExperimentDesignFactor>());
		categoryComboBox.setMaximumSize(new Dimension(250, 26));
		categoryComboBox.setEnabled(false);
		
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		add(categoryComboBox, gbc_comboBox_1);
		
		JLabel lblNewLabel_2 = new JLabel("Subcategory");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		subCategoryComboBox = new JComboBox<ExperimentDesignFactor>();
		subCategoryComboBox.setName(DataTypeName.SUB_CATEGORY.name());
		subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
		subCategoryComboBox.setMaximumSize(new Dimension(250, 26));
		subCategoryComboBox.setEnabled(false);
		
		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 1;
		gbc_comboBox_2.gridy = 2;
		add(subCategoryComboBox, gbc_comboBox_2);
		
		toggleItemListeners(true);
	}

	private void toggleItemListeners(boolean enabled) {

		if (enabled) {

			groupByComboBox.addItemListener(this);
			categoryComboBox.addItemListener(this);
			subCategoryComboBox.addItemListener(this);
		} else {
			groupByComboBox.removeItemListener(this);
			categoryComboBox.removeItemListener(this);
			subCategoryComboBox.removeItemListener(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void populateCategories(ExperimentDesignSubset activeSubset) {

		ExperimentDesignFactor[] factors = new ExperimentDesignFactor[0];

		if (activeSubset != null) {

			factors = activeSubset.getOrderedDesign().keySet()
					.toArray(new ExperimentDesignFactor[activeSubset.getOrderedDesign().size()]);
			categoryComboBox.setModel(new SortedComboBoxModel<ExperimentDesignFactor>(factors));
			categoryComboBox.setSelectedItem(factors[0]);

			if (factors.length > 1) {

				subCategoryComboBox.setModel(new SortedComboBoxModel<ExperimentDesignFactor>(factors));
				subCategoryComboBox.setSelectedItem(factors[1]);
				subCategoryComboBox.setEnabled(true);
			} else {
				subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
				subCategoryComboBox.setEnabled(false);
			}
		} else {
			categoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
			subCategoryComboBox.setEnabled(false);
		}
		updateFactorSelectors();
	}
	
	@SuppressWarnings("unchecked")
	public void updatePlotGroupingOptions(StatsPlotType plotType) {

		toggleItemListeners(false);
		PlotDataGrouping grouping = getDataGroupingType();

		if (plotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE) 
				|| plotType.equals(StatsPlotType.BOXPLOT_BY_GROUP)) {

			groupByComboBox.setModel(
					new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
			groupByComboBox.removeItem(PlotDataGrouping.IGNORE_DESIGN);
			groupByComboBox.setSelectedItem(PlotDataGrouping.EACH_FACTOR);
			categoryComboBox.setSelectedIndex(-1);
			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setEnabled(false);
			updateFactorSelectors();
		}
		if (plotType.equals(StatsPlotType.BARCHART)) {

			groupByComboBox.setModel(
					new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
			groupByComboBox.setSelectedItem(grouping);
			updateFactorSelectors();
		}
		if (plotType.equals(StatsPlotType.LINES) || plotType.equals(StatsPlotType.SCATTER)) {

			groupByComboBox.setModel(new DefaultComboBoxModel<PlotDataGrouping>(
					new PlotDataGrouping[] { PlotDataGrouping.IGNORE_DESIGN }));
			groupByComboBox.setSelectedItem(PlotDataGrouping.IGNORE_DESIGN);
			updateFactorSelectors();
		}		
		toggleItemListeners(true);
	}
	
	private void updateFactorSelectors() {

		PlotDataGrouping grouping = getDataGroupingType();

		if (grouping.equals(PlotDataGrouping.IGNORE_DESIGN) 
				|| grouping.equals(PlotDataGrouping.EACH_FACTOR)
				|| !groupByComboBox.isEnabled()) {

			categoryComboBox.setSelectedIndex(-1);
			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setEnabled(false);
		}
		if (grouping.equals(PlotDataGrouping.ONE_FACTOR)) {

			if (categoryComboBox.getModel().getSize() > 0)
				categoryComboBox.setSelectedIndex(0);

			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(true);
			subCategoryComboBox.setEnabled(false);
		}
		if (grouping.equals(PlotDataGrouping.TWO_FACTORS)) {

			if (categoryComboBox.getModel().getSize() > 0) {
				categoryComboBox.setSelectedIndex(0);
				categoryComboBox.setEnabled(true);
			}
			else
				categoryComboBox.setEnabled(false);

			if (subCategoryComboBox.getModel().getSize() > 0) {
				subCategoryComboBox.setSelectedIndex(0);
				subCategoryComboBox.setEnabled(true);
			}
			else
				subCategoryComboBox.setEnabled(false);
		}
	}
	
	public PlotDataGrouping getDataGroupingType() {
		return (PlotDataGrouping)groupByComboBox.getSelectedItem();
	}
	
	public ExperimentDesignFactor getCategory() {
		return (ExperimentDesignFactor)categoryComboBox.getSelectedItem();
	}
	
	public ExperimentDesignFactor getSububCategory() {
		return (ExperimentDesignFactor)subCategoryComboBox.getSelectedItem();
	}
	
//	public boolean isSplitByBatch() {
//		return splitByBatchCheckBox.isSelected();
//	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getSource().equals(groupByComboBox)) {
				
				toggleItemListeners(false);
				updateFactorSelectors();
				toggleItemListeners(true);
			}
			updatePlot();
		}
//		if(e.getStateChange() == ItemEvent.DESELECTED 
//				&& e.getSource().equals(splitByBatchCheckBox)) {
//			updatePlot();
//		}
	}
	
	private void updatePlot() {
		
		plot.updateParametersFromControls();
		plot.redrawPlot();
	}
}
