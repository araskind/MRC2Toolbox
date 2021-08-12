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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.PrimaryFeatureSelectionType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class BatchAnnotationPreferencesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5773051076832899350L;
	private JButton runButton, cancelButton;
	private JComboBox<PrimaryFeatureSelectionType> optionsComboBox;
	private JLabel lblExcludeClustersLarger;
	private JSpinner spinner;
	private JPanel panel_1;
	private JCheckBox generateAdductsCheckBox;
	private JSpinner chargeSpinner;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JSpinner oligomerSpinner;
	private JLabel lblNewLabel_2;
	private JFormattedTextField massErrorTextField;

	@SuppressWarnings("unchecked")
	public BatchAnnotationPreferencesDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Interpret modifications for all feature clusters", true);

		setSize(new Dimension(400, 220));
		setPreferredSize(new Dimension(400, 220));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 170, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblPrimaryFeatureSelection = new JLabel("Primary feature selection: ");
		GridBagConstraints gbc_lblPrimaryFeatureSelection = new GridBagConstraints();
		gbc_lblPrimaryFeatureSelection.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrimaryFeatureSelection.anchor = GridBagConstraints.EAST;
		gbc_lblPrimaryFeatureSelection.gridx = 0;
		gbc_lblPrimaryFeatureSelection.gridy = 0;
		panel.add(lblPrimaryFeatureSelection, gbc_lblPrimaryFeatureSelection);

		optionsComboBox = new JComboBox<PrimaryFeatureSelectionType>();
		@SuppressWarnings("rawtypes")
		ComboBoxModel model = new SortedComboBoxModel<PrimaryFeatureSelectionType>(PrimaryFeatureSelectionType.values());
		optionsComboBox.setModel(model);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel.add(optionsComboBox, gbc_comboBox);

		lblExcludeClustersLarger = new JLabel("Exclude clusters larger than: ");
		GridBagConstraints gbc_lblExcludeClustersLarger = new GridBagConstraints();
		gbc_lblExcludeClustersLarger.anchor = GridBagConstraints.EAST;
		gbc_lblExcludeClustersLarger.insets = new Insets(0, 0, 5, 5);
		gbc_lblExcludeClustersLarger.gridx = 0;
		gbc_lblExcludeClustersLarger.gridy = 1;
		panel.add(lblExcludeClustersLarger, gbc_lblExcludeClustersLarger);

		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(40, 3, 100, 1));
		spinner.setPreferredSize(new Dimension(100, 20));
		spinner.setMinimumSize(new Dimension(70, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 1;
		panel.add(spinner, gbc_spinner);

		lblNewLabel_2 = new JLabel("Mass error for annotation, ppm");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		massErrorTextField = new JFormattedTextField(new DecimalFormat("###.#"));
		massErrorTextField.setPreferredSize(new Dimension(70, 20));
		massErrorTextField.setMinimumSize(new Dimension(70, 20));
		GridBagConstraints gbc_formattedTextField2 = new GridBagConstraints();
		gbc_formattedTextField2.anchor = GridBagConstraints.WEST;
		gbc_formattedTextField2.insets = new Insets(0, 0, 5, 0);
		gbc_formattedTextField2.gridx = 1;
		gbc_formattedTextField2.gridy = 2;
		panel.add(massErrorTextField, gbc_formattedTextField2);

		panel_1 = new JPanel();
		panel_1.setBorder(
				new TitledBorder(null, "Auto-generate adducts", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		panel.add(panel_1, gbc_panel_1);

		generateAdductsCheckBox = new JCheckBox("Generate adducts");
		panel_1.add(generateAdductsCheckBox);

		lblNewLabel = new JLabel("Max charge");
		panel_1.add(lblNewLabel);

		chargeSpinner = new JSpinner();
		chargeSpinner.setToolTipText("Maximum absolute charge");
		chargeSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		panel_1.add(chargeSpinner);

		lblNewLabel_1 = new JLabel("Max oligomer");
		panel_1.add(lblNewLabel_1);

		oligomerSpinner = new JSpinner();
		oligomerSpinner.setModel(new SpinnerNumberModel(1, 1, 3, 1));
		panel_1.add(oligomerSpinner);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 4;
		panel.add(cancelButton, gbc_btnCancel);

		runButton = new JButton("Create annotations");
		runButton.addActionListener(listener);
		runButton.setActionCommand(MainActionCommands.BATCH_ANNOTATE_CLUSTERS_COMMAND.getName());
		GridBagConstraints gbc_btnCreateAnnotations = new GridBagConstraints();
		gbc_btnCreateAnnotations.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCreateAnnotations.gridx = 1;
		gbc_btnCreateAnnotations.gridy = 4;
		panel.add(runButton, gbc_btnCreateAnnotations);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(runButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(runButton);

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	public boolean autoGenerateAdducts() {

		return generateAdductsCheckBox.isSelected();
	}

	public double getMassError() {

		return Double.valueOf(massErrorTextField.getText());
	}

	public int getMaxAnnotatedClusterSize() {

		return (int) spinner.getValue();
	}

	public int getMaxChargeForGeneratedAdducts() {

		return (int) chargeSpinner.getValue();
	}

	public int getMaxOligomerForGeneratedAdducts() {

		return (int) oligomerSpinner.getValue();
	}

	public PrimaryFeatureSelectionType getPrimaryFeatureSelectionType() {

		return (PrimaryFeatureSelectionType) optionsComboBox.getSelectedItem();
	}

	@Override
	public void setVisible(boolean visible) {

		if (visible) {

			if (massErrorTextField.getText().isEmpty()) {
				double error = MRC2ToolBoxConfiguration.getMassAccuracy();
				massErrorTextField.setText(Double.toString(error));
			}
		}
		super.setVisible(visible);
	}
}
