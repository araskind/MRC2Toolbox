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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.BasicDialogWithPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.FormUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ClusteringByStructureSetupDialog extends BasicDialogWithPreferences{

	private static final long serialVersionUID = 1L;
	
	private Preferences preferences;
	private JFormattedTextField rtRangeStartField;
	private JFormattedTextField rtRangeEndField;
	private JFormattedTextField msmsRtGroupingWindowField;
	private JFormattedTextField minScoreTextField;

	private JComboBox<TableRowSubset> featureSubsetComboBox;
	private JRadioButton useOnlyPrimaryIdButton;
	private JRadioButton useIDwithScoreAboveButton;
	private JCheckBox useAssignedPrimaryIdsCheckBox;
	
	public static final String RT_START = "RT_START";
	public static final String RT_END = "RT_END";
	public static final String RT_WINDOW = "RT_WINDOW";
	public static final String PRIMARY_ID_ONLY = "PRIMARY_ID_ONLY";
	public static final String SCORE_CUTOFF = "SCORE_CUTOFF";
	public static final String USE_ASSIGNED_PRIMARY_IDS = "USE_ASSIGNED_PRIMARY_IDS";
	
	private JTextField clusterSetNameTextField;
	
	public static final String RESET_RT_RANGE = "RESET_RT_RANGE";
	
	public ClusteringByStructureSetupDialog(ActionListener actionListener) {
		super("Cluster features by structure",
				"clusterByStrructure",
				new Dimension(480, 350),
				actionListener);

		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		mainPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel_6 = new JLabel("Result name");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 0;
		mainPanel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		clusterSetNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 5;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		mainPanel.add(clusterSetNameTextField, gbc_textField);
		clusterSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Feature subset");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 1;
		mainPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		featureSubsetComboBox = new JComboBox<>(
				new DefaultComboBoxModel<>(TableRowSubset.values()));
		featureSubsetComboBox.setMinimumSize(new Dimension(200, 25));
		featureSubsetComboBox.setPreferredSize(new Dimension(200, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 5;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		mainPanel.add(featureSubsetComboBox, gbc_comboBox);
		
		JLabel lblNewLabel = new JLabel("RT range from ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		mainPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		rtRangeStartField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtRangeStartField.setPreferredSize(new Dimension(80, 20));
		rtRangeStartField.setMinimumSize(new Dimension(80, 20));
		rtRangeStartField.setColumns(10);
		GridBagConstraints gbc_rtRangeStartField = new GridBagConstraints();
		gbc_rtRangeStartField.insets = new Insets(0, 0, 5, 5);
		gbc_rtRangeStartField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtRangeStartField.gridx = 1;
		gbc_rtRangeStartField.gridy = 2;
		mainPanel.add(rtRangeStartField, gbc_rtRangeStartField);
		
		JLabel lblNewLabel_1 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 2;
		mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		rtRangeEndField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtRangeEndField.setMinimumSize(new Dimension(80, 20));
		rtRangeEndField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_rtRangeEndField = new GridBagConstraints();
		gbc_rtRangeEndField.insets = new Insets(0, 0, 5, 5);
		gbc_rtRangeEndField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtRangeEndField.gridx = 3;
		gbc_rtRangeEndField.gridy = 2;
		mainPanel.add(rtRangeEndField, gbc_rtRangeEndField);
		
		JLabel lblNewLabel_2 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 2;
		mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JButton btnNewButton = new JButton("Reset");
		btnNewButton.setActionCommand(RESET_RT_RANGE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 5;
		gbc_btnNewButton.gridy = 2;
		mainPanel.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_3 = new JLabel("MSMS grouping window");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 3;
		mainPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		msmsRtGroupingWindowField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		msmsRtGroupingWindowField.setPreferredSize(new Dimension(80, 20));
		msmsRtGroupingWindowField.setMinimumSize(new Dimension(80, 20));
		msmsRtGroupingWindowField.setColumns(10);
		GridBagConstraints gbc_rtRangeStartField_1 = new GridBagConstraints();
		gbc_rtRangeStartField_1.insets = new Insets(0, 0, 5, 5);
		gbc_rtRangeStartField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtRangeStartField_1.gridx = 1;
		gbc_rtRangeStartField_1.gridy = 3;
		mainPanel.add(msmsRtGroupingWindowField, gbc_rtRangeStartField_1);
		
		JLabel lblNewLabel_4 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 3;
		mainPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(null, "ID for matching", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 6;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		mainPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		ButtonGroup idSourceGroup = new ButtonGroup();
		useOnlyPrimaryIdButton = new JRadioButton("Use only primary ID");
		idSourceGroup.add(useOnlyPrimaryIdButton);
		GridBagConstraints gbc_useOnlyPrimaryIdButton = new GridBagConstraints();
		gbc_useOnlyPrimaryIdButton.anchor = GridBagConstraints.WEST;
		gbc_useOnlyPrimaryIdButton.insets = new Insets(0, 0, 5, 5);
		gbc_useOnlyPrimaryIdButton.gridx = 0;
		gbc_useOnlyPrimaryIdButton.gridy = 0;
		panel.add(useOnlyPrimaryIdButton, gbc_useOnlyPrimaryIdButton);
		
		useIDwithScoreAboveButton = new JRadioButton("Use any ID with entropy match score above");
		idSourceGroup.add(useIDwithScoreAboveButton);
		GridBagConstraints gbc_useIDwithScoreAboveButton = new GridBagConstraints();
		gbc_useIDwithScoreAboveButton.insets = new Insets(0, 0, 0, 5);
		gbc_useIDwithScoreAboveButton.anchor = GridBagConstraints.WEST;
		gbc_useIDwithScoreAboveButton.gridx = 0;
		gbc_useIDwithScoreAboveButton.gridy = 1;
		panel.add(useIDwithScoreAboveButton, gbc_useIDwithScoreAboveButton);
		
		minScoreTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		minScoreTextField.setMinimumSize(new Dimension(80, 20));
		minScoreTextField.setPreferredSize(new Dimension(80, 20));
		minScoreTextField.setColumns(10);
		GridBagConstraints gbc_minScoreTextField = new GridBagConstraints();
		gbc_minScoreTextField.insets = new Insets(0, 0, 0, 5);
		gbc_minScoreTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minScoreTextField.gridx = 1;
		gbc_minScoreTextField.gridy = 1;
		panel.add(minScoreTextField, gbc_minScoreTextField);
		
		JLabel lblNewLabel_7 = new JLabel("(0 - 1)");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.gridx = 2;
		gbc_lblNewLabel_7.gridy = 1;
		panel.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		useAssignedPrimaryIdsCheckBox = 
			new JCheckBox("Use features assigned primary IDs to determine cluster primary ID");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 4;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 5;
		mainPanel.add(useAssignedPrimaryIdsCheckBox, gbc_chckbxNewCheckBox);

		primaryActionButton.setText(
				MainActionCommands.CLUSTER_FEATURES_BY_STRUCTURE_COMMAND.getName());
		primaryActionButton.setActionCommand(
				MainActionCommands.CLUSTER_FEATURES_BY_STRUCTURE_COMMAND.getName());

		loadPreferences();				
		pack();
		primaryActionButton.requestFocus();
	}
	
	public String getClusterSetName() {
		return clusterSetNameTextField.getText().trim();
	}
	public TableRowSubset getFeatureSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}
	
	public Range getRtRange() {
		
		Range rtRange = null;
		double rtStart = FormUtils.getDoubleValueFromTextField(rtRangeStartField);
		double rtEnd = FormUtils.getDoubleValueFromTextField(rtRangeEndField);
		
		if(rtEnd > rtStart)
			rtRange = new Range(rtStart, rtEnd);
		
		return rtRange;
	}
		
	public double getMsmsRtGroupingWindow() {
		return FormUtils.getDoubleValueFromTextField(msmsRtGroupingWindowField);
	}
	
	public boolean useOnlyPrimaryId() {
		return useOnlyPrimaryIdButton.isSelected();
	}
	
	public double getEntropyScoreCutoff() {
		return FormUtils.getDoubleValueFromTextField(minScoreTextField);
	}
	
	public boolean useAssignedPrimaryIds() {
		return useAssignedPrimaryIdsCheckBox.isSelected();
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		double rtStart = preferences.getDouble(RT_START, 0.0d);
		rtRangeStartField.setText(Double.toString(rtStart));
		
		double rtEnd = preferences.getDouble(RT_END, 0.0d);
		rtRangeEndField.setText(Double.toString(rtEnd));
		
		double groupingWindow = preferences.getDouble(RT_WINDOW, 0.1d);
		msmsRtGroupingWindowField.setText(Double.toString(groupingWindow));
		
		useOnlyPrimaryIdButton.setSelected(
				preferences.getBoolean(PRIMARY_ID_ONLY, true));
		
		double scoreCutoff = preferences.getDouble(SCORE_CUTOFF, 0.8d);
		minScoreTextField.setText(Double.toString(scoreCutoff));
				
		useAssignedPrimaryIdsCheckBox.setSelected(
				preferences.getBoolean(USE_ASSIGNED_PRIMARY_IDS, true));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		Range rtRange = getRtRange();
		if(rtRange == null) {
			preferences.putDouble(RT_START, 0.0d);
			preferences.putDouble(RT_END, 0.0d);
		}
		else {
			preferences.putDouble(RT_START, rtRange.getMin());
			preferences.putDouble(RT_END, rtRange.getMax());
		}
		preferences.putDouble(RT_WINDOW, getMsmsRtGroupingWindow());	
		preferences.putBoolean(
				PRIMARY_ID_ONLY, useOnlyPrimaryIdButton.isSelected());
		preferences.putDouble(SCORE_CUTOFF, getEntropyScoreCutoff());		
		preferences.putBoolean(
				USE_ASSIGNED_PRIMARY_IDS, useAssignedPrimaryIdsCheckBox.isSelected());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(RESET_RT_RANGE)) {
			rtRangeStartField.setText(Double.toString(0.0d));
			rtRangeEndField.setText(Double.toString(0.0d));
		}		
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    
	    if(getClusterSetName().isEmpty())
	    	errors.add("Resulting data set name must be specified");
	    
	    if(getMsmsRtGroupingWindow() <= 0.0d)
	        errors.add("MSMS grouping window must be > 0");
	    
	    if(useIDwithScoreAboveButton.isSelected() && getEntropyScoreCutoff() <= 0.0d)
	    	errors.add("Match score cutoff must be > 0");
	    		
	    return errors;
	}
}
