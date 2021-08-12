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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeName;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class MultiPanelDataPlotToolbar extends PlotToolbar implements ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3114901163421622675L;

	protected JComboBox<StatsPlotType> plotTypeComboBox;
	protected JComboBox<FileSortingOrder> fileSortComboBox;
	protected JComboBox<DataScale> dataScaleComboBox;

	protected MultiPanelDataPlot plot;
	private JComboBox categoryComboBox;
	private JComboBox subCategoryComboBox;
	private JComboBox groupByComboBox;
	private JCheckBox splitByBatchCheckBox;
	private JPanel panel;
	private JPanel panel_1;

	public MultiPanelDataPlotToolbar(MultiPanelDataPlot parentPlot) {

		super(parentPlot);
		plot = parentPlot;
		plot.setToolbar(this);
		xAxisUnits = "design";

		createLegendToggle();
		addSeparator(buttonDimension);

		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, ChartPanel.ZOOM_RESET_BOTH_COMMAND,
				"Fit to  " + xAxisUnits + "  and intensity ranges", buttonDimension);

		addSeparator(buttonDimension);
		createServiceBlock();
		toggleLegendIcon(parentPlot.isLegendVisible());
		addSeparator(buttonDimension);

		panel_1 = new JPanel();
		add(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		// Add plot type options
		JLabel lblPlotType = new JLabel("Plot type");
		GridBagConstraints gbc_lblPlotType = new GridBagConstraints();
		gbc_lblPlotType.anchor = GridBagConstraints.WEST;
		gbc_lblPlotType.insets = new Insets(0, 0, 5, 5);
		gbc_lblPlotType.gridx = 0;
		gbc_lblPlotType.gridy = 0;
		panel_1.add(lblPlotType, gbc_lblPlotType);

		plotTypeComboBox = new JComboBox<StatsPlotType>();
		plotTypeComboBox.setModel(new DefaultComboBoxModel(StatsPlotType.values()));
		plotTypeComboBox.setMaximumSize(new Dimension(120, 26));

		GridBagConstraints gbc_plotTypeComboBox = new GridBagConstraints();
		gbc_plotTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_plotTypeComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_plotTypeComboBox.gridx = 0;
		gbc_plotTypeComboBox.gridy = 1;
		panel_1.add(plotTypeComboBox, gbc_plotTypeComboBox);

		// Add file sorting options
		JLabel lblSortFilesBy = new JLabel("Sort files by");
		GridBagConstraints gbc_lblSortFilesBy = new GridBagConstraints();
		gbc_lblSortFilesBy.anchor = GridBagConstraints.WEST;
		gbc_lblSortFilesBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblSortFilesBy.gridx = 1;
		gbc_lblSortFilesBy.gridy = 0;
		panel_1.add(lblSortFilesBy, gbc_lblSortFilesBy);

		fileSortComboBox = new JComboBox<FileSortingOrder>();
		fileSortComboBox.setModel(new DefaultComboBoxModel(FileSortingOrder.values()));
		fileSortComboBox.setMaximumSize(new Dimension(120, 26));

		GridBagConstraints gbc_fileSortComboBox = new GridBagConstraints();
		gbc_fileSortComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileSortComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_fileSortComboBox.gridx = 1;
		gbc_fileSortComboBox.gridy = 1;
		panel_1.add(fileSortComboBox, gbc_fileSortComboBox);

		// Add data scale options
		JLabel lblScale = new JLabel("Scale");
		GridBagConstraints gbc_lblScale = new GridBagConstraints();
		gbc_lblScale.anchor = GridBagConstraints.WEST;
		gbc_lblScale.insets = new Insets(0, 0, 5, 0);
		gbc_lblScale.gridx = 2;
		gbc_lblScale.gridy = 0;
		panel_1.add(lblScale, gbc_lblScale);

		dataScaleComboBox = new JComboBox<DataScale>();
		dataScaleComboBox.setModel(new DefaultComboBoxModel(DataScale.values()));
		dataScaleComboBox.setSelectedItem(DataScale.RAW);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));

		GridBagConstraints gbc_dataScaleComboBox = new GridBagConstraints();
		gbc_dataScaleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataScaleComboBox.gridx = 2;
		gbc_dataScaleComboBox.gridy = 1;
		panel_1.add(dataScaleComboBox, gbc_dataScaleComboBox);

		addSeparator(buttonDimension);

		//	Data grouping
		panel = new JPanel();
		add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		//	Group by
		JLabel lblGroupBy = new JLabel("Group by");
		GridBagConstraints gbc_lblGroupBy = new GridBagConstraints();
		gbc_lblGroupBy.anchor = GridBagConstraints.WEST;
		gbc_lblGroupBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblGroupBy.gridx = 0;
		gbc_lblGroupBy.gridy = 0;
		panel.add(lblGroupBy, gbc_lblGroupBy);

		groupByComboBox = new JComboBox();
		groupByComboBox.setModel(new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
		groupByComboBox.setSelectedItem(PlotDataGrouping.IGNORE_DESIGN);
		groupByComboBox.setMaximumSize(new Dimension(120, 26));

		GridBagConstraints gbc_groupByComboBox = new GridBagConstraints();
		gbc_groupByComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_groupByComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_groupByComboBox.gridx = 0;
		gbc_groupByComboBox.gridy = 1;
		panel.add(groupByComboBox, gbc_groupByComboBox);

		//	Category
		JLabel lblCategory = new JLabel("Category");
		GridBagConstraints gbc_lblCategory = new GridBagConstraints();
		gbc_lblCategory.anchor = GridBagConstraints.WEST;
		gbc_lblCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblCategory.gridx = 1;
		gbc_lblCategory.gridy = 0;
		panel.add(lblCategory, gbc_lblCategory);

		categoryComboBox = new JComboBox();
		categoryComboBox.setName(DataTypeName.CATEGORY.name());
		categoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
		categoryComboBox.setMaximumSize(new Dimension(250, 26));
		categoryComboBox.setEnabled(false);

		GridBagConstraints gbc_categoryComboBox = new GridBagConstraints();
		gbc_categoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_categoryComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_categoryComboBox.gridx = 1;
		gbc_categoryComboBox.gridy = 1;
		panel.add(categoryComboBox, gbc_categoryComboBox);

		//	Sub-category
		JLabel lblSubcategory = new JLabel("Sub-category");
		GridBagConstraints gbc_lblSubcategory = new GridBagConstraints();
		gbc_lblSubcategory.anchor = GridBagConstraints.WEST;
		gbc_lblSubcategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubcategory.gridx = 2;
		gbc_lblSubcategory.gridy = 0;
		panel.add(lblSubcategory, gbc_lblSubcategory);

		subCategoryComboBox = new JComboBox();
		subCategoryComboBox.setName(DataTypeName.SUB_CATEGORY.name());
		subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
		subCategoryComboBox.setMaximumSize(new Dimension(250, 26));
		subCategoryComboBox.setEnabled(false);

		GridBagConstraints gbc_subCategoryComboBox = new GridBagConstraints();
		gbc_subCategoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_subCategoryComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_subCategoryComboBox.gridx = 2;
		gbc_subCategoryComboBox.gridy = 1;
		panel.add(subCategoryComboBox, gbc_subCategoryComboBox);

		//	Split by batch
		splitByBatchCheckBox = new JCheckBox("Split by batch");
		splitByBatchCheckBox.addItemListener(this);
		GridBagConstraints gbc_splitByBatchCheckBox = new GridBagConstraints();
		gbc_splitByBatchCheckBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_splitByBatchCheckBox.gridx = 3;
		gbc_splitByBatchCheckBox.gridy = 1;
		panel.add(splitByBatchCheckBox, gbc_splitByBatchCheckBox);

		setPlotType(plot.getPlotType());
		setFileOrder(plot.getSortingOrder());
		setPlotScale(plot.getDataScale());

		toggleItemListeners(true);
	}

	private void toggleItemListeners(boolean enabled) {

		if (enabled) {

			groupByComboBox.addItemListener(this);
			plotTypeComboBox.addItemListener(this);
			fileSortComboBox.addItemListener(this);
			dataScaleComboBox.addItemListener(this);
			categoryComboBox.addItemListener(this);
			subCategoryComboBox.addItemListener(this);
		} else {
			groupByComboBox.removeItemListener(this);
			plotTypeComboBox.removeItemListener(this);
			fileSortComboBox.removeItemListener(this);
			dataScaleComboBox.removeItemListener(this);
			categoryComboBox.removeItemListener(this);
			subCategoryComboBox.removeItemListener(this);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getSource() instanceof JComboBox &&
				e.getStateChange() == ItemEvent.SELECTED) {

			toggleItemListeners(false);

			if (e.getSource().equals(plotTypeComboBox)) {

				updatePlotGroupingOptions();
				updateFactorSelectors();
				updateFileSortingOptions();
			}
			if (e.getSource().equals(groupByComboBox)) {

				updateFactorSelectors();
				updateFileSortingOptions();
			}
			toggleItemListeners(true);
			updatePlot();
		}
		if (e.getSource() instanceof JCheckBox)
			updatePlot();
	}

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

	public ExperimentDesignFactor getCategory() {

		if (categoryComboBox.isEnabled())
			return (ExperimentDesignFactor) categoryComboBox.getSelectedItem();

		return null;
	}

	public ExperimentDesignFactor getSubCategory() {

		if (subCategoryComboBox.isEnabled())
			return (ExperimentDesignFactor) subCategoryComboBox.getSelectedItem();

		return null;
	}

	public FileSortingOrder getFileOrder() {

		return (FileSortingOrder) fileSortComboBox.getSelectedItem();
	}

	public StatsPlotType getPlotType() {

		return (StatsPlotType) plotTypeComboBox.getSelectedItem();
	}

	public void setFileOrder(FileSortingOrder order) {

		fileSortComboBox.setSelectedItem(order);
	}

	public void setPlotType(StatsPlotType type) {

		plotTypeComboBox.setSelectedItem(type);
	}

	public void setPlotScale(DataScale scale) {

		dataScaleComboBox.setSelectedItem(scale);
	}

	public DataScale getPlotScale() {

		return (DataScale) dataScaleComboBox.getSelectedItem();
	}

	public PlotDataGrouping getDataGroupingType() {

		return (PlotDataGrouping) groupByComboBox.getSelectedItem();
	}

	// Do not allow ignore design with BoxPlot
	private void updatePlotGroupingOptions() {

		PlotDataGrouping grouping = getDataGroupingType();
		StatsPlotType plotType = getPlotType();

		if (plotType.equals(StatsPlotType.BOXPLOT_BY_FEATURE) || plotType.equals(StatsPlotType.BOXPLOT_BY_GROUP)) {

			groupByComboBox.setModel(new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
			groupByComboBox.removeItem(PlotDataGrouping.IGNORE_DESIGN);
			groupByComboBox.setSelectedItem(PlotDataGrouping.EACH_FACTOR);
			categoryComboBox.setSelectedIndex(-1);
			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setEnabled(false);
		}
		if (plotType.equals(StatsPlotType.BARCHART)) {

			groupByComboBox.setModel(new DefaultComboBoxModel<PlotDataGrouping>(PlotDataGrouping.values()));
			groupByComboBox.setSelectedItem(grouping);
		}
		if (plotType.equals(StatsPlotType.LINES) || plotType.equals(StatsPlotType.SCATTER)) {

			groupByComboBox.setModel(new DefaultComboBoxModel<PlotDataGrouping>(
					new PlotDataGrouping[] { PlotDataGrouping.IGNORE_DESIGN }));
			groupByComboBox.setSelectedItem(PlotDataGrouping.IGNORE_DESIGN);
		}
	}

	private void updateFactorSelectors() {

		PlotDataGrouping grouping = getDataGroupingType();

		if (grouping.equals(PlotDataGrouping.IGNORE_DESIGN) || grouping.equals(PlotDataGrouping.EACH_FACTOR)
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

			if (categoryComboBox.getModel().getSize() > 0)
				categoryComboBox.setSelectedIndex(0);

			if (subCategoryComboBox.getModel().getSize() > 0)
				subCategoryComboBox.setSelectedIndex(0);

			categoryComboBox.setEnabled(true);
			subCategoryComboBox.setEnabled(true);
		}
	}

	// Allow sort by sample ID and sample name only with ignore design
	private void updateFileSortingOptions() {

		PlotDataGrouping grouping = getDataGroupingType();
		StatsPlotType plotType = getPlotType();

		if (!grouping.equals(PlotDataGrouping.IGNORE_DESIGN) || plotType.equals(StatsPlotType.LINES)
				|| plotType.equals(StatsPlotType.SCATTER)) {

			fileSortComboBox.setModel(new DefaultComboBoxModel(
					new FileSortingOrder[] { FileSortingOrder.NAME, FileSortingOrder.TIMESTAMP }));
			fileSortComboBox.setSelectedIndex(0);
		} else {
			fileSortComboBox.setModel(new DefaultComboBoxModel(FileSortingOrder.values()));
			fileSortComboBox.setSelectedIndex(0);
		}
	}

	private void updatePlot() {

		plot.updateParametersFromToolbar();
		plot.redrawPlot();
	}

	public boolean splitByBatch() {

		if (splitByBatchCheckBox.isEnabled())
			return splitByBatchCheckBox.isSelected();
		else
			return false;
	}
}
