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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class AnnotationsSelectorPanel extends JPanel implements ValidatableForm{

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JFormattedTextField annotRTToleranceField;
	private JFormattedTextField annotMassToleranceField;
	private JCheckBox neutMasForChargeCarrierCheckBox;
	private JCheckBox varChargeCheckBox;

	public AnnotationsSelectorPanel() {
		super(new BorderLayout(0, 0));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Annotation assignment parameters", TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Mass tolerance");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		annotMassToleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultMZformat());
		annotMassToleranceField.setColumns(10);
		annotMassToleranceField.setPreferredSize(new Dimension(80, 20));
		annotMassToleranceField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_annotMassToleranceField = new GridBagConstraints();
		gbc_annotMassToleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_annotMassToleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_annotMassToleranceField.gridx = 1;
		gbc_annotMassToleranceField.gridy = 0;
		panel.add(annotMassToleranceField, gbc_annotMassToleranceField);
		
		JLabel lblNewLabel_3 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 0;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		JLabel lblNewLabel_1 = new JLabel("RT tolerance");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		annotRTToleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultRTformat());
		annotRTToleranceField.setPreferredSize(new Dimension(80, 20));
		annotRTToleranceField.setMinimumSize(new Dimension(80, 20));
		annotRTToleranceField.setColumns(10);
		GridBagConstraints gbc_annotRTToleranceField = new GridBagConstraints();
		gbc_annotRTToleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_annotRTToleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_annotRTToleranceField.gridx = 1;
		gbc_annotRTToleranceField.gridy = 1;
		panel.add(annotRTToleranceField, gbc_annotRTToleranceField);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		neutMasForChargeCarrierCheckBox = 
				new JCheckBox("Use neutral masses to help determine best charge carrier");
		GridBagConstraints gbc_neutMasForChargeCarrierCheckBox = new GridBagConstraints();
		gbc_neutMasForChargeCarrierCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_neutMasForChargeCarrierCheckBox.gridwidth = 3;
		gbc_neutMasForChargeCarrierCheckBox.anchor = GridBagConstraints.WEST;
		gbc_neutMasForChargeCarrierCheckBox.gridx = 0;
		gbc_neutMasForChargeCarrierCheckBox.gridy = 2;
		panel.add(neutMasForChargeCarrierCheckBox, gbc_neutMasForChargeCarrierCheckBox);
		
		varChargeCheckBox = 
				new JCheckBox("Allow variable charge without isotope information");
		GridBagConstraints gbc_varChargeCheckBox = new GridBagConstraints();
		gbc_varChargeCheckBox.anchor = GridBagConstraints.WEST;
		gbc_varChargeCheckBox.gridwidth = 3;
		gbc_varChargeCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_varChargeCheckBox.gridx = 0;
		gbc_varChargeCheckBox.gridy = 3;
		panel.add(varChargeCheckBox, gbc_varChargeCheckBox);
		
		JPanel tableWrap = new JPanel(new BorderLayout(0, 0));
		tableWrap.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Available annotations list", 
				TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(10, 0, 0, 0)));
		table = new JTable();
		tableWrap.add(new JScrollPane(table), BorderLayout.CENTER);
		add(tableWrap, BorderLayout.CENTER);
	}

	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}
}
