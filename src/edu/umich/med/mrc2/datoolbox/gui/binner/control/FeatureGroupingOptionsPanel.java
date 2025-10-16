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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.main.config.NumberFormatStore;

public class FeatureGroupingOptionsPanel extends JPanel implements ItemListener, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private JComboBox<CorrelationFunctionType> correlationTypeComboBox;
	private JFormattedTextField rtGapField;
	private JComboBox<ClusterGroupingMethod> clusterGroupingMethodComboBox;
	private JFormattedTextField minSubclusterRTgapField;
	private JFormattedTextField maxSubclusterRTgapField;
	private JLabel alwaysBreakLabel;
	private JComboBox<BinClusteringCutoffType> binClusteringCutoffTypeComboBox;
	private JCheckBox limitBinSizeForAnalysisCheckBox;
	private JFormattedTextField binSizeLimitForAnalysisField;
	private JCheckBox limitBinSizeForOutputCheckBox;
	private JFormattedTextField binSizeLimitForOutputField;	
	private JSlider binClusteringCutoffValueSlider;
	
	public FeatureGroupingOptionsPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel_7 = new JLabel("Correlation function type");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 0;
		add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		correlationTypeComboBox = new JComboBox<CorrelationFunctionType>(
				new DefaultComboBoxModel<CorrelationFunctionType>(CorrelationFunctionType.values()));
		GridBagConstraints gbc_correlationTypeComboBox = new GridBagConstraints();
		gbc_correlationTypeComboBox.gridwidth = 2;
		gbc_correlationTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_correlationTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_correlationTypeComboBox.gridx = 1;
		gbc_correlationTypeComboBox.gridy = 0;
		add(correlationTypeComboBox, gbc_correlationTypeComboBox);
		
		JLabel lblNewLabel = new JLabel("Retention time gap size");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		rtGapField = new JFormattedTextField(
				NumberFormatStore.getDefaultRTformat());
		rtGapField.setPreferredSize(new Dimension(80, 20));
		rtGapField.setMinimumSize(new Dimension(80, 20));
		rtGapField.setColumns(10);
		GridBagConstraints gbc_rtGapField = new GridBagConstraints();
		gbc_rtGapField.insets = new Insets(0, 0, 5, 5);
		gbc_rtGapField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtGapField.gridx = 1;
		gbc_rtGapField.gridy = 1;
		add(rtGapField, gbc_rtGapField);
		
		JLabel lblNewLabel_1 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 1;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Cluster sub-division parameters", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_2 = new JLabel("Grouping method");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		clusterGroupingMethodComboBox = new JComboBox<ClusterGroupingMethod>(
				new DefaultComboBoxModel<ClusterGroupingMethod>(ClusterGroupingMethod.values()));
		clusterGroupingMethodComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_clusterGroupingMethodComboBox = new GridBagConstraints();
		gbc_clusterGroupingMethodComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_clusterGroupingMethodComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_clusterGroupingMethodComboBox.gridx = 1;
		gbc_clusterGroupingMethodComboBox.gridy = 0;
		panel.add(clusterGroupingMethodComboBox, gbc_clusterGroupingMethodComboBox);
		clusterGroupingMethodComboBox.addItemListener(this);
		
		alwaysBreakLabel = new JLabel(
				"Always break on gaps larger than Retention time gap size");
		alwaysBreakLabel.setForeground(Color.BLUE);
		GridBagConstraints gbc_alwaysBreakLabel = new GridBagConstraints();
		gbc_alwaysBreakLabel.gridwidth = 3;
		gbc_alwaysBreakLabel.anchor = GridBagConstraints.WEST;
		gbc_alwaysBreakLabel.insets = new Insets(0, 0, 5, 5);
		gbc_alwaysBreakLabel.gridx = 0;
		gbc_alwaysBreakLabel.gridy = 1;
		panel.add(alwaysBreakLabel, gbc_alwaysBreakLabel);
		
		JLabel lblNewLabel_3 = new JLabel("No gaps smaller than");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		minSubclusterRTgapField = 
				new JFormattedTextField(NumberFormatStore.getDefaultRTformat());
		minSubclusterRTgapField.setMinimumSize(new Dimension(80, 20));
		minSubclusterRTgapField.setPreferredSize(new Dimension(80, 20));
		minSubclusterRTgapField.setColumns(10);
		GridBagConstraints gbc_minSubclusterRTgapField = new GridBagConstraints();
		gbc_minSubclusterRTgapField.insets = new Insets(0, 0, 5, 5);
		gbc_minSubclusterRTgapField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minSubclusterRTgapField.gridx = 1;
		gbc_minSubclusterRTgapField.gridy = 2;
		panel.add(minSubclusterRTgapField, gbc_minSubclusterRTgapField);
		
		JLabel lblNewLabel_4 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 2;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("Always break on gaps larger than");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		maxSubclusterRTgapField = 
				new JFormattedTextField(NumberFormatStore.getDefaultRTformat());
		maxSubclusterRTgapField.setPreferredSize(new Dimension(80, 20));
		maxSubclusterRTgapField.setMinimumSize(new Dimension(80, 20));
		maxSubclusterRTgapField.setColumns(10);
		GridBagConstraints gbc_maxSubclusterRTgapField = new GridBagConstraints();
		gbc_maxSubclusterRTgapField.insets = new Insets(0, 0, 0, 5);
		gbc_maxSubclusterRTgapField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxSubclusterRTgapField.gridx = 1;
		gbc_maxSubclusterRTgapField.gridy = 3;
		panel.add(maxSubclusterRTgapField, gbc_maxSubclusterRTgapField);
		
		JLabel lblNewLabel_6 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 3;
		panel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Bins to cluster", 
						TitledBorder.LEADING, TitledBorder.TOP, null, 
						new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel_8 = new JLabel("Cutoff type");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = 0;
		panel_1.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		binClusteringCutoffTypeComboBox = new JComboBox<BinClusteringCutoffType>(
				new DefaultComboBoxModel<BinClusteringCutoffType>(BinClusteringCutoffType.values()));
		binClusteringCutoffTypeComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_binClusteringCutoffTypeComboBox = new GridBagConstraints();
		gbc_binClusteringCutoffTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_binClusteringCutoffTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_binClusteringCutoffTypeComboBox.gridx = 1;
		gbc_binClusteringCutoffTypeComboBox.gridy = 0;
		panel_1.add(binClusteringCutoffTypeComboBox, gbc_binClusteringCutoffTypeComboBox);
		binClusteringCutoffTypeComboBox.addItemListener(this);
		
		JLabel lblNewLabel_9 = new JLabel("Cutoff value");
		GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
		gbc_lblNewLabel_9.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_9.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_9.gridx = 0;
		gbc_lblNewLabel_9.gridy = 1;
		panel_1.add(lblNewLabel_9, gbc_lblNewLabel_9);
		
		binClusteringCutoffValueSlider = new JSlider(0, 10, 2);
		binClusteringCutoffValueSlider.setSnapToTicks(true);
		binClusteringCutoffValueSlider.setPaintLabels(true);
		binClusteringCutoffValueSlider.setPaintTicks(true);
		binClusteringCutoffValueSlider.setMajorTickSpacing(1);

		GridBagConstraints gbc_binClusteringCutoffValueSlider = new GridBagConstraints();
		gbc_binClusteringCutoffValueSlider.fill = GridBagConstraints.BOTH;
		gbc_binClusteringCutoffValueSlider.gridx = 1;
		gbc_binClusteringCutoffValueSlider.gridy = 1;
		panel_1.add(binClusteringCutoffValueSlider, gbc_binClusteringCutoffValueSlider);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Bin size limit override", 
						TitledBorder.LEADING, TitledBorder.TOP, null, 
						new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 3;
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 4;
		add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		limitBinSizeForAnalysisCheckBox = 
				new JCheckBox("Limit bin size for analysis to ");
		GridBagConstraints gbc_limitBinSizeForAnalysisCheckBox = new GridBagConstraints();
		gbc_limitBinSizeForAnalysisCheckBox.anchor = GridBagConstraints.WEST;
		gbc_limitBinSizeForAnalysisCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitBinSizeForAnalysisCheckBox.gridx = 0;
		gbc_limitBinSizeForAnalysisCheckBox.gridy = 0;
		panel_2.add(limitBinSizeForAnalysisCheckBox, gbc_limitBinSizeForAnalysisCheckBox);
		limitBinSizeForAnalysisCheckBox.addItemListener(this);
		binSizeLimitForAnalysisField = 
				new JFormattedTextField(NumberFormatStore.getIntegerFormat());
		binSizeLimitForAnalysisField.setColumns(15);
		binSizeLimitForAnalysisField.setMinimumSize(new Dimension(100, 20));
		binSizeLimitForAnalysisField.setPreferredSize(new Dimension(100, 20));
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel_2.add(binSizeLimitForAnalysisField, gbc_formattedTextField);
		binSizeLimitForAnalysisField.setEnabled(false);
		
		JLabel lblNewLabel_10 = new JLabel("features");
		GridBagConstraints gbc_lblNewLabel_10 = new GridBagConstraints();
		gbc_lblNewLabel_10.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_10.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_10.gridx = 2;
		gbc_lblNewLabel_10.gridy = 0;
		panel_2.add(lblNewLabel_10, gbc_lblNewLabel_10);
		
		limitBinSizeForOutputCheckBox = 
				new JCheckBox("Limit bin size for binwise output to ");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 1;
		panel_2.add(limitBinSizeForOutputCheckBox, gbc_chckbxNewCheckBox);
		limitBinSizeForOutputCheckBox.addItemListener(this);
		
		binSizeLimitForOutputField = 
				new JFormattedTextField(NumberFormatStore.getIntegerFormat());
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = 1;
		panel_2.add(binSizeLimitForOutputField, gbc_formattedTextField_1);
		binSizeLimitForOutputField.setEnabled(false);
		
		JLabel lblNewLabel_11 = new JLabel("features");
		GridBagConstraints gbc_lblNewLabel_11 = new GridBagConstraints();
		gbc_lblNewLabel_11.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_11.gridx = 2;
		gbc_lblNewLabel_11.gridy = 1;
		panel_2.add(lblNewLabel_11, gbc_lblNewLabel_11);
		
		//	To trigger proper option selection
		clusterGroupingMethodComboBox.setSelectedItem(
				ClusterGroupingMethod.CLUSTER_ON_RT);
		binClusteringCutoffTypeComboBox.setSelectedItem(
				BinClusteringCutoffType.BELOW_SCORE);
	}
	
	public CorrelationFunctionType getCorrelationFunctionType() {
		return (CorrelationFunctionType)correlationTypeComboBox.getSelectedItem();
	}
	
	public void setCorrelationFunctionType(CorrelationFunctionType corrFunction) {
		correlationTypeComboBox.setSelectedItem(corrFunction);
	}
	
	public double getRTgap() {
		return Double.parseDouble(rtGapField.getText());
	}
	
	public void setRTgap(double rtGapSize) {
		rtGapField.setText(Double.toString(rtGapSize));
	}
	
	public ClusterGroupingMethod getClusterGroupingMethod() {
		return (ClusterGroupingMethod)clusterGroupingMethodComboBox.getSelectedItem();
	}
	
	public void setClusterGroupingMethod(ClusterGroupingMethod cgm) {
		clusterGroupingMethodComboBox.setSelectedItem(cgm);
	}

	public double getMinSubclusterRTgap() {
		return Double.parseDouble(minSubclusterRTgapField.getText());
	}
	
	public void setMinSubclusterRTgap(double rtGapSize) {
		minSubclusterRTgapField.setText(Double.toString(rtGapSize));
	}
	
	public double getMaxSubclusterRTgap() {
		return Double.parseDouble(maxSubclusterRTgapField.getText());
	}
	
	public void setMaxSubclusterRTgap(double rtGapSize) {
		maxSubclusterRTgapField.setText(Double.toString(rtGapSize));
	}

	public BinClusteringCutoffType getBinClusteringCutoffType() {
		return (BinClusteringCutoffType)binClusteringCutoffTypeComboBox.getSelectedItem();
	}
	
	public void setBinClusteringCutoffType(BinClusteringCutoffType cutoffType) {
		binClusteringCutoffTypeComboBox.setSelectedItem(cutoffType);
	}
	
	public int getBinClusteringCutoff() {
		return binClusteringCutoffValueSlider.getValue();
	}
	
	public void setBinClusteringCutoff(int bcCutoff) {
		binClusteringCutoffValueSlider.setValue(bcCutoff);
	}

	public boolean limitMaxBinSizeForAnalysis() {
		return limitBinSizeForAnalysisCheckBox.isSelected();
	}
	
	public void setLimitMaxBinSizeForAnalysis(boolean limit) {
		limitBinSizeForAnalysisCheckBox.setSelected(limit);
	}
		
	public int getBinSizeLimitForAnalysis() {
		return Integer.parseInt(binSizeLimitForAnalysisField.getText());
	}
	
	public void setBinSizeLimitForAnalysis(int maxSize) {
		binSizeLimitForAnalysisField.setText(Integer.toString(maxSize));
	}
	
	public boolean limitMaxBinSizeForOutput() {
		return limitBinSizeForOutputCheckBox.isSelected();
	}
	
	public void setLimitMaxBinSizeForOutput(boolean limit) {
		limitBinSizeForOutputCheckBox.setSelected(limit);
	}
	
	public int getBinSizeLimitForOutput() {
		return Integer.parseInt(binSizeLimitForOutputField.getText());
	}
	
	public void setBinSizeLimitForOutput(int maxSize) {
		binSizeLimitForOutputField.setText(Integer.toString(maxSize));
	}
	
	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		if(getRTgap() <= 0.0d)
			errors.add("Retention time gap size must be > 0");
		
		if(getClusterGroupingMethod().equals(ClusterGroupingMethod.CLUSTER_ON_RT)) {
			
			if(getMinSubclusterRTgap() <= 0.0d)
				errors.add("\"No gaps smaller than\" value must be > 0");
			
			if(getMaxSubclusterRTgap() <= 0.0d)
				errors.add("\"Always break on gaps larger than\" value must be > 0");
		}
		if(!getBinClusteringCutoffType().equals(BinClusteringCutoffType.ALL)
				&& getBinClusteringCutoff() <= 0)
			errors.add("Bins to cluster cutoff value value must be > 0");
		
		return errors;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getSource().equals(clusterGroupingMethodComboBox))
				switchGapPolicyOptions(getClusterGroupingMethod());
			
			if(e.getSource().equals(binClusteringCutoffTypeComboBox))
				adjustDefaultBinClusteringCutoffValue(getBinClusteringCutoffType());
		}
		if(e.getSource().equals(limitBinSizeForAnalysisCheckBox))
			binSizeLimitForAnalysisField.setEnabled(limitBinSizeForAnalysisCheckBox.isSelected());
		
		if(e.getSource().equals(limitBinSizeForOutputCheckBox))
			binSizeLimitForOutputField.setEnabled(limitBinSizeForOutputCheckBox.isSelected());		
	}
	
	private void adjustDefaultBinClusteringCutoffValue(BinClusteringCutoffType cutoffType) {
		
		if(cutoffType.equals(BinClusteringCutoffType.BELOW_SCORE)) {
			
			binClusteringCutoffValueSlider.setEnabled(true);
			binClusteringCutoffValueSlider.setValue(2);
		}
		if(cutoffType.equals(BinClusteringCutoffType.ABOVE_SCORE)) {
			
			binClusteringCutoffValueSlider.setEnabled(true);
			binClusteringCutoffValueSlider.setValue(5);
		}
		if(cutoffType.equals(BinClusteringCutoffType.ALL)) {
			binClusteringCutoffValueSlider.setEnabled(false);
		}
	}

	private void switchGapPolicyOptions(ClusterGroupingMethod clusterGroupingMethod) {

		if(clusterGroupingMethod.equals(ClusterGroupingMethod.CLUSTER_ON_RT)) {
			
			minSubclusterRTgapField.setEnabled(true);
			maxSubclusterRTgapField.setEnabled(true);
			alwaysBreakLabel.setVisible(false);
		}
		if(clusterGroupingMethod.equals(ClusterGroupingMethod.REBIN_ON_RT)) {
			
			minSubclusterRTgapField.setEnabled(false);
			maxSubclusterRTgapField.setEnabled(false);
			alwaysBreakLabel.setVisible(true);
		}
	}
}












