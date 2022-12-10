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

package edu.umich.med.mrc2.datoolbox.gui.plot.qc.threed;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jfree.chart3d.Chart3DPanel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.DataTypeName;
import edu.umich.med.mrc2.datoolbox.data.enums.ImageExportFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.PlotDataGrouping;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

@SuppressWarnings("rawtypes")
public class ThreeDplotToolbar extends PlotToolbar implements ItemListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -5318936795577158101L;

	protected static final Icon zoomInIcon = GuiUtils.getIcon("zoomIn", 24);
	protected static final Icon zoomOutIcon = GuiUtils.getIcon("zoomOut", 24);
	protected static final Icon rotateLeftIcon = GuiUtils.getIcon("rotateLeft", 24);
	protected static final Icon rotateRightIcon = GuiUtils.getIcon("rotateRight", 24);
	protected static final Icon rollBackwardIcon = GuiUtils.getIcon("rollBackward", 24);
	protected static final Icon rollForwardIcon = GuiUtils.getIcon("rollForward", 24);
	protected static final Icon rollLeftIcon = GuiUtils.getIcon("rollLeft", 24);
	protected static final Icon rollRightIcon = GuiUtils.getIcon("rollRight", 24);

	private JComboBox groupByComboBox;
	private JComboBox categoryComboBox;
	private JComboBox subCategoryComboBox;

	private Dockable3DChartPanel parentPlotPanel;

	private JCheckBox splitByBatchCheckBox;

	@SuppressWarnings("unchecked")
	public ThreeDplotToolbar(Dockable3DChartPanel parentPlotPanel) {

		super(parentPlotPanel);
		this.parentPlotPanel = parentPlotPanel;

		GuiUtils.addButton(this, null, copyIcon, commandListener, MainActionCommands.COPY_AS_IMAGE.getName(),
				MainActionCommands.COPY_AS_IMAGE.getName(), buttonDimension);

		saveAs = new JPopupMenu("Save as");

		saveAsPngMenuItem = new JMenuItem(MainActionCommands.SAVE_AS_PNG.getName());
		saveAsPngMenuItem.setIcon(savePngIcon);
		saveAs.add(saveAsPngMenuItem);

		saveAsPdfMenuItem = new JMenuItem(MainActionCommands.SAVE_AS_PDF.getName());
		saveAsPdfMenuItem.setIcon(savePdfIcon);
		saveAs.add(saveAsPdfMenuItem);

		saveAsSvgMenuItem = new JMenuItem(MainActionCommands.SAVE_AS_SVG.getName());
		saveAsSvgMenuItem.setIcon(saveSvgIcon);
		saveAs.add(saveAsSvgMenuItem);

		saveButton = GuiUtils.addButton(this, "Save plot", saveIcon, null, null, null, new Dimension(105, 35));
		saveButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				saveAs.show(e.getComponent(), e.getX(), e.getY());
			}
		});
//		GuiUtils.addButton(this, null, printIcon, commandListener, ChartPanel.PRINT_COMMAND, "Print graph",
//				buttonDimension);
//
//		GuiUtils.addButton(this, null, settingsIcon, commandListener, ChartPanel.PROPERTIES_COMMAND,
//				"Setup graph properties", buttonDimension);

		addSeparator(buttonDimension);

		createZoomAndRotateBlock();

		// Add plot type options
		addSeparator(buttonDimension);

		JPanel panel = new JPanel();
		add(panel);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel_1);

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

		toggleItemListeners(true);
	}

	public void setChartPanel(Chart3DPanel chartPanel) {

		saveAsPngMenuItem.setAction(new ExportChartToFileAction(chartPanel, ImageExportFormat.PNG));
		saveAsPngMenuItem.setIcon(savePngIcon);

		saveAsPdfMenuItem.setAction(new ExportChartToFileAction(chartPanel, ImageExportFormat.PDF));
		saveAsPdfMenuItem.setIcon(savePdfIcon);

		saveAsSvgMenuItem.setAction(new ExportChartToFileAction(chartPanel, ImageExportFormat.SVG));
		saveAsSvgMenuItem.setIcon(saveSvgIcon);
	}

	private void createZoomAndRotateBlock() {

		// Zoom buttons
		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, MainActionCommands.RESET_ZOOM.getName(),
				MainActionCommands.RESET_ZOOM.getName(), buttonDimension);

		GuiUtils.addButton(this, null, zoomInIcon, commandListener, MainActionCommands.ZOOM_IN.getName(),
				MainActionCommands.ZOOM_IN.getName(), buttonDimension);

		GuiUtils.addButton(this, null, zoomOutIcon, commandListener, MainActionCommands.ZOOM_OUT.getName(),
				MainActionCommands.ZOOM_OUT.getName(), buttonDimension);

		addSeparator(buttonDimension);

		// Rotate buttons
		GuiUtils.addButton(this, null, rotateLeftIcon, commandListener, MainActionCommands.ROTATE_LEFT.getName(),
				MainActionCommands.ROTATE_LEFT.getName(), buttonDimension);

		GuiUtils.addButton(this, null, rotateRightIcon, commandListener, MainActionCommands.ROTATE_RIGHT.getName(),
				MainActionCommands.ROTATE_RIGHT.getName(), buttonDimension);

		GuiUtils.addButton(this, null, rollBackwardIcon, commandListener, MainActionCommands.ROTATE_BACKWARD.getName(),
				MainActionCommands.ROTATE_BACKWARD.getName(), buttonDimension);

		GuiUtils.addButton(this, null, rollForwardIcon, commandListener, MainActionCommands.ROTATE_FORWARD.getName(),
				MainActionCommands.ROTATE_FORWARD.getName(), buttonDimension);

		GuiUtils.addButton(this, null, rollLeftIcon, commandListener, MainActionCommands.ROLL_LEFT.getName(),
				MainActionCommands.ROLL_LEFT.getName(), buttonDimension);

		GuiUtils.addButton(this, null, rollRightIcon, commandListener, MainActionCommands.ROLL_RIGHT.getName(),
				MainActionCommands.ROLL_RIGHT.getName(), buttonDimension);
	}

	private void toggleItemListeners(boolean enabled) {

		if(enabled) {

			groupByComboBox.addItemListener(this);
			categoryComboBox.addItemListener(this);
			subCategoryComboBox.addItemListener(this);
		}
		else {
			groupByComboBox.removeItemListener(this);;
			categoryComboBox.removeItemListener(this);
			subCategoryComboBox.removeItemListener(this);
		}
	}

	public void populateCategories(ExperimentDesignSubset activeSubset) {

		ExperimentDesignFactor[] factors = new ExperimentDesignFactor[0];

		if(activeSubset != null) {

			factors = activeSubset.getOrderedDesign().keySet().toArray(new ExperimentDesignFactor[activeSubset.getOrderedDesign().size()]);
			categoryComboBox.setModel(new SortedComboBoxModel<ExperimentDesignFactor>(factors));
			categoryComboBox.setSelectedItem(factors[0]);

			if(factors.length > 1) {

				subCategoryComboBox.setModel(new SortedComboBoxModel<ExperimentDesignFactor>(factors));
				subCategoryComboBox.setSelectedItem(factors[1]);
				subCategoryComboBox.setEnabled(true);
			}
			else {
				subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
				subCategoryComboBox.setEnabled(false);
			}
		}
		else {
			categoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setModel(new DefaultComboBoxModel<ExperimentDesignFactor>());
			subCategoryComboBox.setEnabled(false);
		}
		updateFactorSelectors();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getSource() instanceof JComboBox &&
				e.getStateChange() == ItemEvent.SELECTED) {

			toggleItemListeners(false);

			if(e.getSource().equals(groupByComboBox))
				updateFactorSelectors();

			toggleItemListeners(true);
			updatePlot();
		}
		if (e.getSource() instanceof JCheckBox)
			updatePlot();
	}

	private void updateFactorSelectors() {

		PlotDataGrouping grouping = getDataGroupingType();

		if(grouping.equals(PlotDataGrouping.IGNORE_DESIGN)
				|| grouping.equals(PlotDataGrouping.EACH_FACTOR)
				|| !groupByComboBox.isEnabled()) {

			categoryComboBox.setSelectedIndex(-1);
			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(false);
			subCategoryComboBox.setEnabled(false);
		}
		if(grouping.equals(PlotDataGrouping.ONE_FACTOR)) {

			if(categoryComboBox.getModel().getSize() > 0)
				categoryComboBox.setSelectedIndex(0);

			subCategoryComboBox.setSelectedIndex(-1);
			categoryComboBox.setEnabled(true);
			subCategoryComboBox.setEnabled(false);
		}
		if(grouping.equals(PlotDataGrouping.TWO_FACTORS)) {

			if(categoryComboBox.getModel().getSize() > 0)
				categoryComboBox.setSelectedIndex(0);

			if(subCategoryComboBox.getModel().getSize() > 0)
				subCategoryComboBox.setSelectedIndex(0);

			categoryComboBox.setEnabled(true);
			subCategoryComboBox.setEnabled(true);
		}
	}

	private void updatePlot() {

		parentPlotPanel.updateParametersFromToolbar();
		parentPlotPanel.redrawPlot();
	}

	public PlotDataGrouping getDataGroupingType() {

		return (PlotDataGrouping)groupByComboBox.getSelectedItem();
	}

	public ExperimentDesignFactor getCategory() {

		if(categoryComboBox.isEnabled())
			return (ExperimentDesignFactor) categoryComboBox.getSelectedItem();

		return null;
	}

	public ExperimentDesignFactor getSubCategory() {

		if(subCategoryComboBox.isEnabled())
			return (ExperimentDesignFactor) subCategoryComboBox.getSelectedItem();

		return null;
	}

	public boolean splitByBatch() {

		if (splitByBatchCheckBox.isEnabled())
			return splitByBatchCheckBox.isSelected();
		else
			return false;
	}
}
































