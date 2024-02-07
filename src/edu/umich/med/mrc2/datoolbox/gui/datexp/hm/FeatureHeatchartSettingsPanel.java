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

package edu.umich.med.mrc2.datoolbox.gui.datexp.hm;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.HeatMapDataRange;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureHeatchartSettingsPanel extends 
		JPanel implements ItemListener, ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon refreshDataIcon = GuiUtils.getIcon("rerun", 16);
	
	private Preferences preferences;
	private static final String START_RT = "START_RT";
	private static final String END_RT = "END_RT";
	private static final String START_MZ = "START_MZ";
	private static final String END_MZ = "END_MZ";
	private static final String COLOR_SCHEME = "COLOR_SCHEME";
	private static final String COLOR_SCALE = "COLOR_SCALE";
	
	private ActionListener actListener;
	private ItemListener externalItemListener;	
	private JFormattedTextField startRTTextField, endRTTextField; 
	private JFormattedTextField startMZTextField, endMZTextField; 
	private JButton resetLimitsButton;
	private JButton refreshPlotButton;
	private JComboBox colorSchemeComboBox;
	private JComboBox colorScaleComboBox;
	private JComboBox dataRangeComboBox;
	
	@SuppressWarnings("unchecked")
	public FeatureHeatchartSettingsPanel(
			ActionListener actListener, 
			ItemListener externalItemListener) {
		
		super();
		this.actListener = actListener;
		this.externalItemListener = externalItemListener;
		
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 29, 69, 0, 0};		
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		int rowCount = 0;
		
		JLabel lblNewLabel = new JLabel("RT from ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = rowCount;
		add(lblNewLabel, gbc_lblNewLabel);
		
		startRTTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		startRTTextField.setColumns(6);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = rowCount;
		add(startRTTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_1 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = rowCount;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		endRTTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		endRTTextField.setColumns(6);
		GridBagConstraints gbc_startRTTextField_1 = new GridBagConstraints();
		gbc_startRTTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_startRTTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_startRTTextField_1.gridx = 3;
		gbc_startRTTextField_1.gridy = rowCount;
		add(endRTTextField, gbc_startRTTextField_1);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = rowCount;
		add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		rowCount++;
		
		JLabel lblNewLabel_3 = new JLabel("M/Z from");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = rowCount;
		add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		startMZTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		startMZTextField.setColumns(8);
		GridBagConstraints gbc_startMZTextField = new GridBagConstraints();
		gbc_startMZTextField.insets = new Insets(0, 0, 5, 5);
		gbc_startMZTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_startMZTextField.gridx = 1;
		gbc_startMZTextField.gridy = rowCount;
		add(startMZTextField, gbc_startMZTextField);
		
		JLabel lblNewLabel_4 = new JLabel("to");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = rowCount;
		add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		endMZTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getMzFormat());
		endMZTextField.setColumns(8);
		GridBagConstraints gbc_endMZTextField = new GridBagConstraints();
		gbc_endMZTextField.insets = new Insets(0, 0, 5, 5);
		gbc_endMZTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_endMZTextField.gridx = 3;
		gbc_endMZTextField.gridy = rowCount;
		add(endMZTextField, gbc_endMZTextField);
		
		JLabel lblNewLabel_5 = new JLabel("Da");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridx = 4;
		gbc_lblNewLabel_5.gridy = rowCount;
		add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		rowCount++;
		
		JLabel dataRangeLabel = new JLabel("Data range ");
		GridBagConstraints gbc_dataRangeLabel = new GridBagConstraints();
		gbc_dataRangeLabel.anchor = GridBagConstraints.EAST;
		gbc_dataRangeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dataRangeLabel.gridx = 0;
		gbc_dataRangeLabel.gridy = rowCount;
		add(dataRangeLabel, gbc_dataRangeLabel);

		dataRangeComboBox = new JComboBox<HeatMapDataRange>(
				new DefaultComboBoxModel<HeatMapDataRange>(HeatMapDataRange.values()));
		dataRangeComboBox.setSelectedItem(HeatMapDataRange.CORRELATION);
		dataRangeComboBox.setSelectedItem(ColorGradient.GREEN_RED);
		dataRangeComboBox.setPreferredSize(new Dimension(100, 25));
		dataRangeComboBox.setSize(new Dimension(100, 25));			
		GridBagConstraints gbc_dataRangeComboBox = new GridBagConstraints();
		gbc_dataRangeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataRangeComboBox.gridwidth = 4;
		gbc_dataRangeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_dataRangeComboBox.gridx = 1;
		gbc_dataRangeComboBox.gridy = rowCount;
		add(dataRangeComboBox, gbc_dataRangeComboBox);
		
		rowCount++;
			
		JLabel colorSchemeLabel = new JLabel("Palette ");
		GridBagConstraints gbc_colorSchemeLabel = new GridBagConstraints();
		gbc_colorSchemeLabel.anchor = GridBagConstraints.EAST;
		gbc_colorSchemeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_colorSchemeLabel.gridx = 0;
		gbc_colorSchemeLabel.gridy = rowCount;
		add(colorSchemeLabel, gbc_colorSchemeLabel);
		
		colorSchemeComboBox = new JComboBox(
				new SortedComboBoxModel<ColorGradient>(ColorGradient.values()));
		colorSchemeComboBox.setSelectedItem(ColorGradient.GREEN_RED);
		colorSchemeComboBox.setPreferredSize(new Dimension(100, 25));
		colorSchemeComboBox.setSize(new Dimension(100, 25));			
		GridBagConstraints gbc_colorSchemeComboBox = new GridBagConstraints();
		gbc_colorSchemeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_colorSchemeComboBox.gridwidth = 4;
		gbc_colorSchemeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_colorSchemeComboBox.gridx = 1;
		gbc_colorSchemeComboBox.gridy = rowCount;
		add(colorSchemeComboBox, gbc_colorSchemeComboBox);

		rowCount++;
		
		JLabel colorScaleLabel = new JLabel("Color scale ");
		GridBagConstraints gbc_colorScaleLabel = new GridBagConstraints();
		gbc_colorScaleLabel.anchor = GridBagConstraints.EAST;
		gbc_colorScaleLabel.insets = new Insets(0, 0, 5, 5);
		gbc_colorScaleLabel.gridx = 0;
		gbc_colorScaleLabel.gridy = rowCount;
		add(colorScaleLabel, gbc_colorScaleLabel);

		colorScaleComboBox = new JComboBox(
				new SortedComboBoxModel<ColorScale>(ColorScale.values()));
		colorScaleComboBox.setSelectedItem(ColorScale.LINEAR);
		colorScaleComboBox.setPreferredSize(new Dimension(100, 25));
		colorScaleComboBox.setSize(new Dimension(100, 25));
		GridBagConstraints gbc_colorScaleComboBox = new GridBagConstraints();
		gbc_colorScaleComboBox.gridwidth = 4;
		gbc_colorScaleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_colorScaleComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_colorScaleComboBox.gridx = 1;
		gbc_colorScaleComboBox.gridy = rowCount;
		add(colorScaleComboBox, gbc_colorScaleComboBox);
		
		rowCount++;
			
		JLabel lblNewLabel_6 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = rowCount;
		add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		rowCount++;
		
		resetLimitsButton = new JButton(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.setActionCommand(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.addActionListener(this);
		GridBagConstraints gbc_resetLimitsButton = new GridBagConstraints();
		gbc_resetLimitsButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_resetLimitsButton.gridwidth = 2;
		gbc_resetLimitsButton.insets = new Insets(0, 0, 5, 5);
		gbc_resetLimitsButton.gridx = 0;
		gbc_resetLimitsButton.gridy = rowCount;
		add(resetLimitsButton, gbc_resetLimitsButton);
		
		refreshPlotButton = new JButton("Refresh plot", refreshDataIcon);
		refreshPlotButton.setActionCommand(
				MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName());
		refreshPlotButton.addActionListener(actListener);
		GridBagConstraints gbc_refreshPlotButton = new GridBagConstraints();
		gbc_refreshPlotButton.insets = new Insets(0, 0, 5, 0);
		gbc_refreshPlotButton.gridwidth = 2;
		gbc_refreshPlotButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_refreshPlotButton.gridx = 3;
		gbc_refreshPlotButton.gridy = rowCount;
		add(refreshPlotButton, gbc_refreshPlotButton);
        
		gridBagLayout.rowHeights = new int[rowCount + 2];
		Arrays.fill(gridBagLayout.rowHeights, 0);
		
		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		gridBagLayout.rowWeights[rowCount + 1] = Double.MIN_VALUE;
				
		loadPreferences();

		colorSchemeComboBox.addItemListener(externalItemListener);
		colorScaleComboBox.addItemListener(externalItemListener);
		colorSchemeComboBox.addItemListener(externalItemListener);
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
	
	public ColorGradient getColorGradient() {
		
		if(colorSchemeComboBox == null)
			return null;
		else
			return (ColorGradient)colorSchemeComboBox.getSelectedItem();
	}
	
	public ColorScale getColorScale() {
		
		if(colorScaleComboBox == null)
			return null;
		else
			return (ColorScale)colorScaleComboBox.getSelectedItem();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
			
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

		if(colorSchemeComboBox != null) {
			
			ColorGradient ds = ColorGradient.getOptionByName(
							preferences.get(COLOR_SCHEME, ColorGradient.GREEN_RED.name()));			
			colorSchemeComboBox.setSelectedItem(ds);
		}
		if(colorScaleComboBox != null) {
			
			ColorScale ds = ColorScale.getOptionByName(
							preferences.get(COLOR_SCALE, ColorScale.LINEAR.name()));			
			colorScaleComboBox.setSelectedItem(ds);
		}	
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
		
		if(getColorGradient() != null)
			preferences.put(COLOR_SCHEME, getColorGradient().name());	
		
		if(getColorScale() != null)
			preferences.put(COLOR_SCALE, getColorScale().name());	
	}
	
	public MZRTPlotParameterObject getPlotParameters() {
		
		MZRTPlotParameterObject params = new MZRTPlotParameterObject();
		params.setMzRange(getMZRange());
		params.setRtRange(getRtRange());
		params.setColorGradient(getColorGradient());
		params.setColorScale(getColorScale());
		return params;
	}
}











