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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class DockableMiscCalculationsPanel extends DefaultSingleCDockable implements DocumentListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5510806263375906828L;

	private JFormattedTextField massOneValueField;
	private JFormattedTextField massTwoValueField;
	private JLabel ppmValueLabel;
	private double mz1;
	private double mz2;
	private double diff;
	private JFormattedTextField isotopeOneMassValueField;
	private JFormattedTextField isotopeTwoMassValueField;
	private JLabel numCarbonsValue;
	private JLabel ppmCarbonsValue;
	int rowCount;

	private static final Icon componentIcon = GuiUtils.getIcon("calculator", 16);

	public DockableMiscCalculationsPanel() {

		super("DockableMiscCalculationsPanel", componentIcon, "Misc calculations", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 71, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 23, 0, 33, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gridBagLayout);

		rowCount = 1;
		initMassDiffCalc();
		initCarbonDiffCalc();
		
		gridBagLayout.rowHeights = new int[rowCount + 2];
		Arrays.fill(gridBagLayout.rowHeights, 0);
		
		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		gridBagLayout.rowWeights[rowCount + 1] = Double.MIN_VALUE;
	}

	private void initMassDiffCalc() {

		JLabel lblMassErrorEstimate = new JLabel("Mass error estimate:");
		lblMassErrorEstimate.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblMassErrorEstimate = new GridBagConstraints();
		gbc_lblMassErrorEstimate.gridwidth = 2;
		gbc_lblMassErrorEstimate.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassErrorEstimate.gridx = 0;
		gbc_lblMassErrorEstimate.gridy = rowCount;
		add(lblMassErrorEstimate, gbc_lblMassErrorEstimate);
		
		rowCount++;

		JLabel label = new JLabel("  ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = rowCount;
		this.add(label, gbc_label);
		
		rowCount++;

		JLabel massOneLabel = new JLabel("Mass 1:");
		GridBagConstraints gbc_lblMass = new GridBagConstraints();
		gbc_lblMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass.anchor = GridBagConstraints.EAST;
		gbc_lblMass.gridx = 0;
		gbc_lblMass.gridy = rowCount;
		this.add(massOneLabel, gbc_lblMass);

		massOneValueField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		massOneValueField.setPreferredSize(new Dimension(50, 20));
		massOneValueField.setMinimumSize(new Dimension(50, 20));
		massOneValueField.getDocument().addDocumentListener(this);
		massOneValueField.getDocument().putProperty("parent", massOneValueField);

		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = rowCount;
		this.add(massOneValueField, gbc_formattedTextField);

		JLabel massTwoLabel = new JLabel("Mass 2:");
		GridBagConstraints gbc_lblMass_1 = new GridBagConstraints();
		gbc_lblMass_1.anchor = GridBagConstraints.EAST;
		gbc_lblMass_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass_1.gridx = 2;
		gbc_lblMass_1.gridy = rowCount;
		this.add(massTwoLabel, gbc_lblMass_1);

		massTwoValueField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		massTwoValueField.setPreferredSize(new Dimension(50, 20));
		massTwoValueField.setMinimumSize(new Dimension(50, 20));
		massTwoValueField.getDocument().addDocumentListener(this);
		massTwoValueField.getDocument().putProperty("parent", massTwoValueField);

		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = rowCount;
		this.add(massTwoValueField, gbc_formattedTextField_1);

		ppmValueLabel = new JLabel("");
		ppmValueLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		ppmValueLabel.setPreferredSize(new Dimension(50, 14));
		ppmValueLabel.setMinimumSize(new Dimension(50, 14));
		ppmValueLabel.setToolTipText("");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 4;
		gbc_lblNewLabel.gridy = rowCount;
		this.add(ppmValueLabel, gbc_lblNewLabel);

		JLabel ppmNameLabel = new JLabel("  ppm");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 5;
		gbc_lblNewLabel_1.gridy = rowCount;
		this.add(ppmNameLabel, gbc_lblNewLabel_1);
		
		rowCount++;
	}
	
	private void initCarbonDiffCalc(){
		
		JLabel label = new JLabel("   ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = rowCount;
		this.add(label, gbc_label);
		
		rowCount++;
		
		JLabel lblCarbonDistanceError = new JLabel("Carbon distance error estimate:");
		lblCarbonDistanceError.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblCarbonDistanceError = new GridBagConstraints();
		gbc_lblCarbonDistanceError.gridwidth = 3;
		gbc_lblCarbonDistanceError.insets = new Insets(0, 0, 5, 5);
		gbc_lblCarbonDistanceError.gridx = 0;
		gbc_lblCarbonDistanceError.gridy = rowCount;
		this.add(lblCarbonDistanceError, gbc_lblCarbonDistanceError);
		
		rowCount++;
		
		JLabel iroaMassOneLabel = new JLabel("Mass 1:");
		GridBagConstraints gbc_lblMassOne = new GridBagConstraints();
		gbc_lblMassOne.anchor = GridBagConstraints.EAST;
		gbc_lblMassOne.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassOne.gridx = 0;
		gbc_lblMassOne.gridy = rowCount;
		this.add(iroaMassOneLabel, gbc_lblMassOne);
		
		isotopeOneMassValueField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		isotopeOneMassValueField.setPreferredSize(new Dimension(50, 20));
		isotopeOneMassValueField.setMinimumSize(new Dimension(50, 20));
		isotopeOneMassValueField.getDocument().addDocumentListener(this);
		isotopeOneMassValueField.getDocument().putProperty("parent", isotopeOneMassValueField);
		
		GridBagConstraints gbc_formattedTextField2 = new GridBagConstraints();
		gbc_formattedTextField2.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField2.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField2.gridx = 1;
		gbc_formattedTextField2.gridy = rowCount;
		this.add(isotopeOneMassValueField, gbc_formattedTextField2);
		
		JLabel iroaMassTwoLabel = new JLabel("Mass 2:");
		GridBagConstraints gbc_lblMassTwo = new GridBagConstraints();
		gbc_lblMassTwo.anchor = GridBagConstraints.EAST;
		gbc_lblMassTwo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassTwo.gridx = 2;
		gbc_lblMassTwo.gridy = rowCount;
		this.add(iroaMassTwoLabel, gbc_lblMassTwo);
		
		isotopeTwoMassValueField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		isotopeTwoMassValueField.setPreferredSize(new Dimension(50, 20));
		isotopeTwoMassValueField.setMinimumSize(new Dimension(50, 20));
		isotopeTwoMassValueField.getDocument().addDocumentListener(this);
		isotopeTwoMassValueField.getDocument().putProperty("parent", isotopeTwoMassValueField);
		
		GridBagConstraints gbc_formattedTextField3 = new GridBagConstraints();
		gbc_formattedTextField3.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField3.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField3.gridx = 3;
		gbc_formattedTextField3.gridy = rowCount;
		this.add(isotopeTwoMassValueField, gbc_formattedTextField3);
		
		rowCount++;
		
		JLabel numCarbonsName = new JLabel("# Carbons: ");		
		GridBagConstraints gbc_numCarbName = new GridBagConstraints();
		gbc_numCarbName.anchor = GridBagConstraints.NORTHEAST;
		gbc_numCarbName.insets = new Insets(0, 0, 0, 5);
		gbc_numCarbName.gridx = 0;
		gbc_numCarbName.gridy = rowCount;
		this.add(numCarbonsName, gbc_numCarbName);
		
		numCarbonsValue = new JLabel("");
		numCarbonsValue.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		GridBagConstraints gbc_numCarbValue = new GridBagConstraints();
		gbc_numCarbValue.anchor = GridBagConstraints.NORTHWEST;
		gbc_numCarbValue.insets = new Insets(0, 0, 0, 5);
		gbc_numCarbValue.gridx = 1;
		gbc_numCarbValue.gridy = rowCount;
		this.add(numCarbonsValue, gbc_numCarbValue);
		
		JLabel ppmCarbonsName = new JLabel("Error, ppm: ");
		
		GridBagConstraints gbc_ppmCarbName = new GridBagConstraints();
		gbc_ppmCarbName.anchor = GridBagConstraints.NORTHEAST;
		gbc_ppmCarbName.insets = new Insets(0, 0, 0, 5);
		gbc_ppmCarbName.gridx = 2;
		gbc_ppmCarbName.gridy = rowCount;
		this.add(ppmCarbonsName, gbc_ppmCarbName);
		
		ppmCarbonsValue = new JLabel("");
		ppmCarbonsValue.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		GridBagConstraints gbc_ppmCarbValue = new GridBagConstraints();
		gbc_ppmCarbValue.anchor = GridBagConstraints.NORTHWEST;
		gbc_ppmCarbValue.insets = new Insets(0, 0, 0, 5);
		gbc_ppmCarbValue.gridx = 3;
		gbc_ppmCarbValue.gridy = rowCount;
		this.add(ppmCarbonsValue, gbc_ppmCarbValue);
		
		rowCount++;
	}

	private void calculatePpmDifference() {

		try {
			mz1 = Double.parseDouble(massOneValueField.getText());
			mz2 = Double.parseDouble(massTwoValueField.getText());

			if (mz1 > 0 && mz2 > 0) {

				diff = (mz1 - mz2) / mz1 * 1000000;
				ppmValueLabel.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(diff));
			}
		} catch (NumberFormatException e) {

		}
	}
	
	private void calculateIsotopeDifferenceAndError() {

		try {
			mz1 = Double.parseDouble(isotopeOneMassValueField.getText());
			mz2 = Double.parseDouble(isotopeTwoMassValueField.getText());
			
			double minMass = Math.min(mz1, mz2);
			double maxMass = Math.max(mz1, mz2);
			
			int deltac = (int)Math.round(maxMass - minMass);
			numCarbonsValue.setText(Integer.toString(deltac));
			
			diff = (maxMass - minMass - deltac * MsUtils.NEUTRON_MASS) / maxMass * 1000000;
			ppmCarbonsValue.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(diff));
			
		} catch (NumberFormatException e) {

		}
	}

	private void passCalc(DocumentEvent arg0) {

		JFormattedTextField textField = (JFormattedTextField) arg0.getDocument().getProperty("parent");

		if (textField.equals(massOneValueField) || textField.equals(massTwoValueField))
			calculatePpmDifference();
		
		if(textField.equals(isotopeOneMassValueField) || textField.equals(isotopeTwoMassValueField))
			calculateIsotopeDifferenceAndError();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		passCalc(arg0);
	}
	
	@Override
	public void insertUpdate(DocumentEvent arg0) {
		passCalc(arg0);
	}
	@Override
	public void removeUpdate(DocumentEvent arg0) {

		passCalc(arg0);
	}

}
