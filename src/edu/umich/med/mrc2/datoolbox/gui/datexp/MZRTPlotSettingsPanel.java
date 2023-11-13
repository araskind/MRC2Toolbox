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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MZRTPlotSettingsPanel extends 
		JPanel implements ItemListener, ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon refreshDataIcon = GuiUtils.getIcon("rerun", 16);

	private ActionListener actListener;
	private ItemListener externalItemListener;
	
	private JComboBox featureSubsetComboBox;
	private JRadioButton useFeaturesFromTableRadioButton;
	private JRadioButton useCompleteSetRadioButton;
	private JFormattedTextField startRTTextField, endRTTextField; 
	private JFormattedTextField startMZTextField, endMZTextField; 
	
	private Preferences preferences;
	private static final String START_RT = "START_RT";
	private static final String END_RT = "END_RT";
	private static final String START_MZ = "START_MZ";
	private static final String END_MZ = "END_MZ";
	private static final String FEATURE_PLOT_COLOR_OPTION = "FEATURE_PLOT_COLOR_OPTION";
	
	private JButton resetLimitsButton;
	private JButton refreshPlotButton;
	private JComboBox plotColorOptionComboBox;
	
	public MZRTPlotSettingsPanel(
			ActionListener actListener, 
			ItemListener externalItemListener,
			boolean isMSMSdata) {
		
		super();
		this.actListener = actListener;
		this.externalItemListener = externalItemListener;
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 29, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		ButtonGroup bg = new ButtonGroup();
		useFeaturesFromTableRadioButton = 
				new JRadioButton("Use features from the table");
		bg.add(useFeaturesFromTableRadioButton);
		GridBagConstraints gbc_useFeaturesFromTableRadioButton = new GridBagConstraints();
		gbc_useFeaturesFromTableRadioButton.gridwidth = 4;
		gbc_useFeaturesFromTableRadioButton.anchor = GridBagConstraints.WEST;
		gbc_useFeaturesFromTableRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_useFeaturesFromTableRadioButton.gridx = 0;
		gbc_useFeaturesFromTableRadioButton.gridy = 0;
		add(useFeaturesFromTableRadioButton, gbc_useFeaturesFromTableRadioButton);
		useFeaturesFromTableRadioButton.addItemListener(this);
		
		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		GridBagConstraints gbc_featureSubsetComboBox = new GridBagConstraints();
		gbc_featureSubsetComboBox.gridwidth = 5;
		gbc_featureSubsetComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_featureSubsetComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSubsetComboBox.gridx = 0;
		gbc_featureSubsetComboBox.gridy = 1;
		add(featureSubsetComboBox, gbc_featureSubsetComboBox);
		featureSubsetComboBox.addItemListener(externalItemListener);
		
		useCompleteSetRadioButton = 
				new JRadioButton("Use complete active feature set");
		bg.add(useCompleteSetRadioButton);
		GridBagConstraints gbc_useCompleteSetRadioButton = new GridBagConstraints();
		gbc_useCompleteSetRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_useCompleteSetRadioButton.gridwidth = 4;
		gbc_useCompleteSetRadioButton.anchor = GridBagConstraints.WEST;
		gbc_useCompleteSetRadioButton.gridx = 0;
		gbc_useCompleteSetRadioButton.gridy = 2;
		add(useCompleteSetRadioButton, gbc_useCompleteSetRadioButton);
		useCompleteSetRadioButton.addItemListener(this);
		
		JLabel lblNewLabel = new JLabel("RT from ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 3;
		add(lblNewLabel, gbc_lblNewLabel);
		
		startRTTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		startRTTextField.setColumns(6);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 3;
		add(startRTTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_1 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 3;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		endRTTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		endRTTextField.setColumns(6);
		GridBagConstraints gbc_startRTTextField_1 = new GridBagConstraints();
		gbc_startRTTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_startRTTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_startRTTextField_1.gridx = 3;
		gbc_startRTTextField_1.gridy = 3;
		add(endRTTextField, gbc_startRTTextField_1);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 3;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("M/Z from");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 4;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		startMZTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		startMZTextField.setColumns(8);
		GridBagConstraints gbc_startMZTextField = new GridBagConstraints();
		gbc_startMZTextField.insets = new Insets(0, 0, 5, 5);
		gbc_startMZTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_startMZTextField.gridx = 1;
		gbc_startMZTextField.gridy = 4;
		add(startMZTextField, gbc_startMZTextField);
		
		JLabel lblNewLabel_4 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 4;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		endMZTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		endMZTextField.setColumns(8);
		GridBagConstraints gbc_endMZTextField = new GridBagConstraints();
		gbc_endMZTextField.insets = new Insets(0, 0, 5, 5);
		gbc_endMZTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_endMZTextField.gridx = 3;
		gbc_endMZTextField.gridy = 4;
		add(endMZTextField, gbc_endMZTextField);
		
		JLabel lblNewLabel_5 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridx = 4;
		gbc_lblNewLabel_5.gridy = 4;
		add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		if(isMSMSdata) {
			
			JLabel lblNewLabel_7 = new JLabel("Color by ");
			GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
			gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_7.gridx = 0;
			gbc_lblNewLabel_7.gridy = 5;
			add(lblNewLabel_7, gbc_lblNewLabel_7);
			
			plotColorOptionComboBox = new JComboBox<FeaturePlotColorOption>(
					new DefaultComboBoxModel<FeaturePlotColorOption>(
							FeaturePlotColorOption.values()));
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.gridwidth = 4;
			gbc_comboBox.insets = new Insets(0, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 5;
			add(plotColorOptionComboBox, gbc_comboBox);
			plotColorOptionComboBox.addItemListener(externalItemListener);
		}		
		JLabel lblNewLabel_6 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 6;
		add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		resetLimitsButton = new JButton(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.setActionCommand(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.addActionListener(this);
		GridBagConstraints gbc_resetLimitsButton = new GridBagConstraints();
		gbc_resetLimitsButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_resetLimitsButton.gridwidth = 2;
		gbc_resetLimitsButton.insets = new Insets(0, 0, 0, 5);
		gbc_resetLimitsButton.gridx = 0;
		gbc_resetLimitsButton.gridy = 7;
		add(resetLimitsButton, gbc_resetLimitsButton);
		
		refreshPlotButton = new JButton("Refresh plot", refreshDataIcon);
		refreshPlotButton.setActionCommand(
				MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName());
		refreshPlotButton.addActionListener(actListener);
		GridBagConstraints gbc_refreshPlotButton = new GridBagConstraints();
		gbc_refreshPlotButton.gridwidth = 2;
		gbc_refreshPlotButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_refreshPlotButton.gridx = 3;
		gbc_refreshPlotButton.gridy = 7;
		add(refreshPlotButton, gbc_refreshPlotButton);

		loadPreferences();
		useCompleteSetRadioButton.setSelected(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName()))
			resetMZRTlimits();		
	}
	
	private void resetMZRTlimits() {
		
		double limit = 0.0d;
		startRTTextField.setText(Double.toString(limit));
		endRTTextField.setText(Double.toString(limit));
		startMZTextField.setText(Double.toString(limit));
		endMZTextField.setText(Double.toString(limit));
		useCompleteSetRadioButton.setSelected(true);
	}

	public void setTableRowSubset(TableRowSubset newSubset) {
		featureSubsetComboBox.setSelectedItem(newSubset);
	}
	
	public TableRowSubset getTableRowSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}

	public void setRtRange(Range rtRange) {
		
		startRTTextField.setText(Double.toString(rtRange.getMin()));
		endRTTextField.setText(Double.toString(rtRange.getMax()));
	}
	
	public Range getRtRange() {
		
		double startRt = 0.0d;
		if(!startRTTextField.getText().trim().isEmpty())
			startRt = Double.parseDouble(startRTTextField.getText().trim());
		
		double endRt = 0.0d;
		if(!endRTTextField.getText().trim().isEmpty())
			endRt = Double.parseDouble(endRTTextField.getText().trim());
		
		if(startRt <= endRt)
			return new Range(startRt, endRt);
		
		return null;
	}	
	
	public void setMZRange(Range mzRange) {
		
		startMZTextField.setText(Double.toString(mzRange.getMin()));
		endMZTextField.setText(Double.toString(mzRange.getMax()));
	}
	
	public Range getMZRange() {
		
		double startMZ = 0.0d;
		if(!startMZTextField.getText().trim().isEmpty())
			startMZ = Double.parseDouble(startMZTextField.getText().trim());
		
		double endMZ = 0.0d;
		if(!endMZTextField.getText().trim().isEmpty())
			endMZ = Double.parseDouble(endMZTextField.getText().trim());
		
		if(startMZ <= endMZ)
			return new Range(startMZ, endMZ);
		
		return null;
	}
	
	public FeaturePlotColorOption getFeaturePlotColorOption() {
		return (FeaturePlotColorOption)plotColorOptionComboBox.getSelectedItem();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if(useFeaturesFromTableRadioButton.isSelected()) {
			featureSubsetComboBox.setEnabled(true);
		}
		if(useCompleteSetRadioButton.isSelected()) {
			
			featureSubsetComboBox.setSelectedIndex(-1);			
			featureSubsetComboBox.setEnabled(false);
		}		
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		double startRt = preferences.getDouble(START_RT, 0.0d);
		startRTTextField.setText(Double.toString(startRt));
		
		double endRt = preferences.getDouble(END_RT, 0.0d);
		endRTTextField.setText(Double.toString(endRt));
		
		double startMz = preferences.getDouble(START_MZ, 0.0d);
		startMZTextField.setText(Double.toString(startMz));
		
		double endMz = preferences.getDouble(END_MZ, 0.0d);
		endMZTextField.setText(Double.toString(endMz));
		
		FeaturePlotColorOption co = 
				FeaturePlotColorOption.getFeaturePlotColorOptionByName(
						preferences.get(FEATURE_PLOT_COLOR_OPTION, 
								FeaturePlotColorOption.COLOR_BY_ID_LEVEL.name()));
		if(co == null)
			co = FeaturePlotColorOption.COLOR_BY_ID_LEVEL;
		
		plotColorOptionComboBox.setSelectedItem(co);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userNodeForPackage(this.getClass());
		Range rtRange = getRtRange();
		if(rtRange != null) {
			
			preferences.putDouble(START_RT, rtRange.getMin());
			preferences.putDouble(END_RT, rtRange.getMax());
		}
		Range mzRange = getMZRange();
		if(mzRange != null) {
			
			preferences.putDouble(START_MZ, mzRange.getMin());
			preferences.putDouble(END_MZ, mzRange.getMax());
		}
		if(getFeaturePlotColorOption() != null)
			preferences.put(FEATURE_PLOT_COLOR_OPTION, 
					getFeaturePlotColorOption().name());		
	}
}











