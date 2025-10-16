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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class DataCleaningOptionsPanel extends JPanel implements ItemListener, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private JFormattedTextField outlierSDdeviationField;
	private JFormattedTextField missingRemovalThresholdField;
	private JCheckBox logTransformCheckBox;
	private JCheckBox zeroAsMissingCheckBox;
	private JCheckBox deisotopeCheckBox;
	private JCheckBox deisoMassDiffDistrCheckBox;
	private JFormattedTextField deisotopingMassToleranceField;
	private JFormattedTextField deisotopingRTtoleranceField;
	private JFormattedTextField deisotopingCorrCutoffField;

	public DataCleaningOptionsPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{219, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Remove data points if they are more than");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		outlierSDdeviationField = new JFormattedTextField(
				NumberFormatStore.getDecimalFormatWithPrecision(1));
		outlierSDdeviationField.setMinimumSize(new Dimension(80, 20));
		outlierSDdeviationField.setPreferredSize(new Dimension(80, 20));
		outlierSDdeviationField.setColumns(10);
		GridBagConstraints gbc_outlierSDdeviationField = new GridBagConstraints();
		gbc_outlierSDdeviationField.insets = new Insets(0, 0, 5, 5);
		gbc_outlierSDdeviationField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outlierSDdeviationField.gridx = 1;
		gbc_outlierSDdeviationField.gridy = 0;
		add(outlierSDdeviationField, gbc_outlierSDdeviationField);
		
		JLabel lblNewLabel_1 = new JLabel("standard deviations away from mean value");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblRemoveFeatureIf = new JLabel("Remove feature if data missing in more than");
		GridBagConstraints gbc_lblRemoveFeatureIf = new GridBagConstraints();
		gbc_lblRemoveFeatureIf.anchor = GridBagConstraints.EAST;
		gbc_lblRemoveFeatureIf.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoveFeatureIf.gridx = 0;
		gbc_lblRemoveFeatureIf.gridy = 1;
		add(lblRemoveFeatureIf, gbc_lblRemoveFeatureIf);
		
		missingRemovalThresholdField = new JFormattedTextField(
				NumberFormatStore.getDecimalFormatWithPrecision(1));
		missingRemovalThresholdField.setPreferredSize(new Dimension(80, 20));
		missingRemovalThresholdField.setMinimumSize(new Dimension(80, 20));
		missingRemovalThresholdField.setColumns(10);
		GridBagConstraints gbc_outlierSDdeviationField_1 = new GridBagConstraints();
		gbc_outlierSDdeviationField_1.insets = new Insets(0, 0, 5, 5);
		gbc_outlierSDdeviationField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_outlierSDdeviationField_1.gridx = 1;
		gbc_outlierSDdeviationField_1.gridy = 1;
		add(missingRemovalThresholdField, gbc_outlierSDdeviationField_1);
		
		JLabel lblNewLabel_2 = new JLabel("% of the samples");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 1;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		logTransformCheckBox = new JCheckBox("Log-transform the data [ X ->  ln(1 + X) ]");
		GridBagConstraints gbc_logTransformCheckBox = new GridBagConstraints();
		gbc_logTransformCheckBox.anchor = GridBagConstraints.WEST;
		gbc_logTransformCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_logTransformCheckBox.gridx = 0;
		gbc_logTransformCheckBox.gridy = 2;
		add(logTransformCheckBox, gbc_logTransformCheckBox);
		
		zeroAsMissingCheckBox = new JCheckBox("Treat zero values as missing");
		GridBagConstraints gbc_zeroAsMissingCheckBox = new GridBagConstraints();
		gbc_zeroAsMissingCheckBox.anchor = GridBagConstraints.WEST;
		gbc_zeroAsMissingCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_zeroAsMissingCheckBox.gridx = 0;
		gbc_zeroAsMissingCheckBox.gridy = 3;
		add(zeroAsMissingCheckBox, gbc_zeroAsMissingCheckBox);
		
		deisotopeCheckBox = new JCheckBox("Deisotope the data");
		GridBagConstraints gbc_deisotopeCheckBox = new GridBagConstraints();
		gbc_deisotopeCheckBox.anchor = GridBagConstraints.WEST;
		gbc_deisotopeCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_deisotopeCheckBox.gridx = 0;
		gbc_deisotopeCheckBox.gridy = 4;
		add(deisotopeCheckBox, gbc_deisotopeCheckBox);
		deisotopeCheckBox.addItemListener(this);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Deisotoping parameters", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		deisoMassDiffDistrCheckBox = 
				new JCheckBox("Deisotope mass difference distribution");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 0;
		panel.add(deisoMassDiffDistrCheckBox, gbc_chckbxNewCheckBox);
		
		JLabel lblNewLabel_3 = new JLabel("Mass tolerance");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 1;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		deisotopingMassToleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultMZformat());
		deisotopingMassToleranceField.setColumns(10);
		deisotopingMassToleranceField.setPreferredSize(new Dimension(80, 20));
		deisotopingMassToleranceField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_deisotopingMassToleranceField = new GridBagConstraints();
		gbc_deisotopingMassToleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_deisotopingMassToleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_deisotopingMassToleranceField.gridx = 1;
		gbc_deisotopingMassToleranceField.gridy = 1;
		panel.add(deisotopingMassToleranceField, gbc_deisotopingMassToleranceField);
		
		JLabel lblNewLabel_4 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 1;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("RT tolerance");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 2;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		deisotopingRTtoleranceField = 
				new JFormattedTextField(NumberFormatStore.getDefaultRTformat());
		deisotopingRTtoleranceField.setColumns(10);
		deisotopingRTtoleranceField.setPreferredSize(new Dimension(80, 20));
		deisotopingRTtoleranceField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_deisotopingRTtoleranceField = new GridBagConstraints();
		gbc_deisotopingRTtoleranceField.insets = new Insets(0, 0, 5, 5);
		gbc_deisotopingRTtoleranceField.fill = GridBagConstraints.HORIZONTAL;
		gbc_deisotopingRTtoleranceField.gridx = 1;
		gbc_deisotopingRTtoleranceField.gridy = 2;
		panel.add(deisotopingRTtoleranceField, gbc_deisotopingRTtoleranceField);
		
		JLabel lblNewLabel_6 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 2;
		panel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		JLabel lblNewLabel_7 = new JLabel("Correlation cutoff");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 3;
		panel.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		deisotopingCorrCutoffField = new JFormattedTextField(
				NumberFormatStore.getDecimalFormatWithPrecision(2));
		deisotopingCorrCutoffField.setPreferredSize(new Dimension(80, 20));
		deisotopingCorrCutoffField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 3;
		panel.add(deisotopingCorrCutoffField, gbc_formattedTextField);
		
		toggleDeisotopePreferencesBlock(deisotopeCheckBox.isSelected());
	}
	
	public double getOutlierSDdeviation() {
		return Double.parseDouble(outlierSDdeviationField.getText());
	}
	
	public void setOutlierSDdeviation(double outlierSDdeviation) {
		outlierSDdeviationField.setText(Double.toString(outlierSDdeviation));
	}

	public double getMissingRemovalThreshold() {
		return Double.parseDouble(missingRemovalThresholdField.getText());
	}
	
	public void setMissingRemovalThreshold(double missingRemovalThreshold) {
		missingRemovalThresholdField.setText(
				Double.toString(missingRemovalThreshold));
	}
	
	public boolean treatZerosAsMisssing() {
		return zeroAsMissingCheckBox.isSelected();
	}
	
	public void setTreatZerosAsMisssing(boolean zeroAsMissing) {
		zeroAsMissingCheckBox.setSelected(zeroAsMissing);
	}
	
	public boolean logTransformData() {
		return logTransformCheckBox.isSelected();
	}
	
	public void setLogTransformData(boolean transform) {
		logTransformCheckBox.setSelected(transform);
	}
	
	public boolean deisotopeData() {
		return deisotopeCheckBox.isSelected();
	}
	
	public void setDeisotopeData(boolean deisotope) {
		deisotopeCheckBox.setSelected(deisotope);
	}
	
	public boolean deisotopeMassDifferenceDistribution() {
		return deisoMassDiffDistrCheckBox.isSelected();
	}
	
	public void setDeisotopeMassDifferenceDistribution(boolean mdDistro) {
		deisoMassDiffDistrCheckBox.setSelected(mdDistro);
	}
	
	public double getDeisotopingMassTolerance() {
		return Double.parseDouble(deisotopingMassToleranceField.getText());
	}
	
	public void setDeisotopingMassTolerance(double value) {
		deisotopingMassToleranceField.setText(Double.toString(value));
	}
	
	public double getDeisotopingRTtolerance() {
		return Double.parseDouble(deisotopingRTtoleranceField.getText());
	}
	
	public void setDeisotopingRTtolerance(double value) {
		deisotopingRTtoleranceField.setText(Double.toString(value));
	}
	
	public double getDeisotopingCorrelationCutoff() {
		return Double.parseDouble(deisotopingCorrCutoffField.getText());
	}
	
	public void setDeisotopingCorrelationCutoff(double value) {
		deisotopingCorrCutoffField.setText(Double.toString(value));
	}
	
	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		
		if(getOutlierSDdeviation() <= 0.0d)
			errors.add("Outlier detection threshold must be > 0");
		
		if(getMissingRemovalThreshold() <= 0.0d)
			errors.add("Missing data removal threshold must be > 0");
		
		if(deisotopeData()) {
			
			if(getDeisotopingMassTolerance() <= 0.0d)
				errors.add("Mass tolerance for deisotoping must be > 0");
			
			if(getDeisotopingRTtolerance() <= 0.0d)
				errors.add("RT tolerance for deisotoping must be > 0");
			
			if(getDeisotopingCorrelationCutoff() < 0.0d 
					|| getDeisotopingCorrelationCutoff() > 1.0d)
				errors.add("Correlation cutoff for deisotoping must be between 0.0 and 1.0");
		}
		return errors;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if(e.getItem().equals(deisotopeCheckBox))
			toggleDeisotopePreferencesBlock(deisotopeCheckBox.isSelected());
	}

	private void toggleDeisotopePreferencesBlock(boolean selected) {
		
		deisoMassDiffDistrCheckBox.setSelected(selected);
		deisoMassDiffDistrCheckBox.setEnabled(selected);
		deisotopingMassToleranceField.setEnabled(selected);
		deisotopingRTtoleranceField.setEnabled(selected);
		deisotopingCorrCutoffField.setEnabled(selected);
	}
}












