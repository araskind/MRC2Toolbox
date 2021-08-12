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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableMiscCalculationsPanel extends DefaultSingleCDockable implements DocumentListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5510806263375906828L;

	private JLabel label;
	private JLabel massOneLabel;
	private JFormattedTextField massOneValueField;

	private JLabel massTwoLabel;
	private JFormattedTextField massTwoValueField;
	private JLabel ppmValueLabel;
	private JLabel ppmNameLabel;
	private double mz1;

	private double mz2;
	private double diff;
	private JLabel lblMassErrorEstimate;

	private static final Icon componentIcon = GuiUtils.getIcon("calculator", 16);

	public DockableMiscCalculationsPanel() {

		super("DockableMiscCalculationsPanel", componentIcon, "Misc calculations", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 71, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 23, 0, 33, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_panel);

		initMassDiffCalc();
	}

	private void initMassDiffCalc() {

		lblMassErrorEstimate = new JLabel("Mass error estimate:");
		lblMassErrorEstimate.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblMassErrorEstimate = new GridBagConstraints();
		gbc_lblMassErrorEstimate.gridwidth = 2;
		gbc_lblMassErrorEstimate.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassErrorEstimate.gridx = 0;
		gbc_lblMassErrorEstimate.gridy = 1;
		add(lblMassErrorEstimate, gbc_lblMassErrorEstimate);

		label = new JLabel("  ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 2;
		this.add(label, gbc_label);

		massOneLabel = new JLabel("Mass 1:");
		GridBagConstraints gbc_lblMass = new GridBagConstraints();
		gbc_lblMass.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass.anchor = GridBagConstraints.EAST;
		gbc_lblMass.gridx = 0;
		gbc_lblMass.gridy = 3;
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
		gbc_formattedTextField.gridy = 3;
		this.add(massOneValueField, gbc_formattedTextField);

		massTwoLabel = new JLabel("Mass 2:");
		GridBagConstraints gbc_lblMass_1 = new GridBagConstraints();
		gbc_lblMass_1.anchor = GridBagConstraints.EAST;
		gbc_lblMass_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMass_1.gridx = 2;
		gbc_lblMass_1.gridy = 3;
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
		gbc_formattedTextField_1.gridy = 3;
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
		gbc_lblNewLabel.gridy = 3;
		this.add(ppmValueLabel, gbc_lblNewLabel);

		ppmNameLabel = new JLabel("  ppm");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 5;
		gbc_lblNewLabel_1.gridy = 3;
		this.add(ppmNameLabel, gbc_lblNewLabel_1);
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

	private void passCalc(DocumentEvent arg0) {

		JFormattedTextField textField = (JFormattedTextField) arg0.getDocument().getProperty("parent");

		if (textField.equals(massOneValueField) || textField.equals(massTwoValueField))
			calculatePpmDifference();

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
