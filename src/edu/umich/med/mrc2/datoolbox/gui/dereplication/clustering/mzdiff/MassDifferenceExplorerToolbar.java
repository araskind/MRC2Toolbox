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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.mzdiff;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MassDifferenceExplorerToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2839582922881510839L;
	protected static final Icon extractDeltasIcon = GuiUtils.getIcon("extractDeltas", 32);

	protected JButton
		filterClustersButton,
		resetFilterButton,
		extractDeltasButton;

	private JFormattedTextField minDiffTextField;
	private JFormattedTextField maxDiffTextField;
	private JFormattedTextField binningTextField;
	private JSpinner maxClusterSizeSpinner;
	private JSpinner minFrequencySpinner;

	public MassDifferenceExplorerToolbar(ActionListener commandListener) {
		super(commandListener);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblMin = new JLabel("Min.");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblMin.anchor = GridBagConstraints.EAST;
		gbc_lblMin.gridx = 0;
		gbc_lblMin.gridy = 0;
		panel.add(lblMin, gbc_lblMin);

		minDiffTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		minDiffTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel.add(minDiffTextField, gbc_formattedTextField);

		JLabel lblMax = new JLabel("Max.");
		GridBagConstraints gbc_lblMax = new GridBagConstraints();
		gbc_lblMax.insets = new Insets(0, 0, 0, 5);
		gbc_lblMax.anchor = GridBagConstraints.EAST;
		gbc_lblMax.gridx = 2;
		gbc_lblMax.gridy = 0;
		panel.add(lblMax, gbc_lblMax);

		maxDiffTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		maxDiffTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 0;
		panel.add(maxDiffTextField, gbc_formattedTextField_1);

		JLabel lblBinningWindowMda = new JLabel("Binning window, mDa");
		GridBagConstraints gbc_lblBinningWindowMda = new GridBagConstraints();
		gbc_lblBinningWindowMda.insets = new Insets(0, 0, 0, 5);
		gbc_lblBinningWindowMda.anchor = GridBagConstraints.EAST;
		gbc_lblBinningWindowMda.gridx = 4;
		gbc_lblBinningWindowMda.gridy = 0;
		panel.add(lblBinningWindowMda, gbc_lblBinningWindowMda);

		binningTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		binningTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_2 = new GridBagConstraints();
		gbc_formattedTextField_2.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_2.gridx = 5;
		gbc_formattedTextField_2.gridy = 0;
		panel.add(binningTextField, gbc_formattedTextField_2);

		JLabel lblMinFerquency = new JLabel("Min. ferquency");
		GridBagConstraints gbc_lblMinFerquency = new GridBagConstraints();
		gbc_lblMinFerquency.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinFerquency.gridx = 6;
		gbc_lblMinFerquency.gridy = 0;
		panel.add(lblMinFerquency, gbc_lblMinFerquency);

		minFrequencySpinner = new JSpinner();
		minFrequencySpinner.setPreferredSize(new Dimension(50, 20));
		minFrequencySpinner.setSize(new Dimension(50, 20));
		minFrequencySpinner.setMinimumSize(new Dimension(50, 20));
		minFrequencySpinner.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), null, new Integer(1)));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 7;
		gbc_spinner.gridy = 0;
		panel.add(minFrequencySpinner, gbc_spinner);

		JLabel lblExcludeClustersLarger = new JLabel("Exclude clusters larger than");
		GridBagConstraints gbc_lblExcludeClustersLarger = new GridBagConstraints();
		gbc_lblExcludeClustersLarger.insets = new Insets(0, 0, 0, 5);
		gbc_lblExcludeClustersLarger.gridx = 8;
		gbc_lblExcludeClustersLarger.gridy = 0;
		panel.add(lblExcludeClustersLarger, gbc_lblExcludeClustersLarger);

		maxClusterSizeSpinner = new JSpinner();
		maxClusterSizeSpinner.setPreferredSize(new Dimension(50, 20));
		maxClusterSizeSpinner.setSize(new Dimension(50, 20));
		maxClusterSizeSpinner.setMinimumSize(new Dimension(50, 20));
		maxClusterSizeSpinner.setModel(new SpinnerNumberModel(new Integer(50), new Integer(2), null, new Integer(1)));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.gridx = 9;
		gbc_spinner_1.gridy = 0;
		panel.add(maxClusterSizeSpinner, gbc_spinner_1);

		extractDeltasButton = GuiUtils.addButton(this, null, extractDeltasIcon, commandListener,
				MainActionCommands.EXTRACT_MASS_DIFFERENCES_COMMAND.getName(),
				MainActionCommands.EXTRACT_MASS_DIFFERENCES_COMMAND.getName(), buttonDimension);
	}

	public double getMinDifference() {

		if(minDiffTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(minDiffTextField.getText().trim());
	}

	public double getMaxDifference() {

		if(maxDiffTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(maxDiffTextField.getText().trim());
	}

	public double getBinningWindow() {

		if(binningTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(binningTextField.getText().trim());
	}

	public void setMinDifference(double minDifference) {
		minDiffTextField.setText(Double.toString(minDifference));
	}

	public void setMaxDifference(double maxDifference) {
		maxDiffTextField.setText(Double.toString(maxDifference));
	}

	public void setBinningWindow(double binningWindow) {
		binningTextField.setText(Double.toString(binningWindow));
	}

	public int getMinFrequency() {
		return (int) minFrequencySpinner.getValue();
	}

	public void setMinFrequency(int minFrequency) {
		minFrequencySpinner.setValue(minFrequency);
	}

	public int getMaxClusterSize() {
		return (int) maxClusterSizeSpinner.getValue();
	}

	public void setMaxClusterSize(int maxClusterSize) {
		maxClusterSizeSpinner.setValue(maxClusterSize);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}


}


























