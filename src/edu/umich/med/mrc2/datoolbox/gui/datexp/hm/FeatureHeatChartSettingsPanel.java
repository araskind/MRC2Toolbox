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
import java.util.Collection;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.Project;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class FeatureHeatChartSettingsPanel extends 
		JPanel implements ItemListener, ActionListener, BackedByPreferences, TableModelListener {

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
	private static final String FILE_SORTING_ORDER = "FILE_SORTING_ORDER";
	private static final String FEATURE_SORTING_ORDER = "FEATURE_SORTING_ORDER";
	
	private ActionListener actListener;
	private ItemListener externalItemListener;	
	private JFormattedTextField startRTTextField, endRTTextField; 
	private JFormattedTextField startMZTextField, endMZTextField; 
	private JComboBox colorSchemeComboBox;
	private JComboBox colorScaleComboBox;
//	private JComboBox dataRangeComboBox;
	private JComboBox<DataScale> dataScaleComboBox;
	private JComboBox<FileSortingOrder> fileSortingOrderComboBox;
	private JComboBox<SortProperty> featureSortingOrderComboBox;
	private SampleGroupTable sampleGroupTable;
	private JButton refreshPlotButton;
	
	private Project experiment;
	
	@SuppressWarnings("unchecked")
	public FeatureHeatChartSettingsPanel(
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
		startRTTextField.setPreferredSize(new Dimension(80, 20));
		startRTTextField.setMinimumSize(new Dimension(60, 20));
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
		endRTTextField.setPreferredSize(new Dimension(80, 20));
		endRTTextField.setMinimumSize(new Dimension(60, 20));
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
		startMZTextField.setPreferredSize(new Dimension(80, 20));
		startMZTextField.setMinimumSize(new Dimension(60, 20));
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
		endMZTextField.setPreferredSize(new Dimension(80, 20));
		endMZTextField.setMinimumSize(new Dimension(60, 20));
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
		
		JButton resetLimitsButton = new JButton(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.setActionCommand(
				MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName());
		resetLimitsButton.addActionListener(this);
		GridBagConstraints gbc_resetLimitsButton = new GridBagConstraints();
		gbc_resetLimitsButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_resetLimitsButton.gridwidth = 3;
		gbc_resetLimitsButton.insets = new Insets(0, 0, 5, 0);
		gbc_resetLimitsButton.gridx = 2;
		gbc_resetLimitsButton.gridy = rowCount;
		add(resetLimitsButton, gbc_resetLimitsButton);
		
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
		
		JLabel bubbleScaleLabel = new JLabel("Peak area scaling ");
		GridBagConstraints gbc_bubbleScaleLabel = new GridBagConstraints();
		gbc_bubbleScaleLabel.anchor = GridBagConstraints.EAST;
		gbc_bubbleScaleLabel.insets = new Insets(0, 0, 5, 5);
		gbc_bubbleScaleLabel.gridx = 0;
		gbc_bubbleScaleLabel.gridy = rowCount;
		add(bubbleScaleLabel, gbc_bubbleScaleLabel);
		
		dataScaleComboBox = new JComboBox<DataScale>();
//		dataScaleComboBox.setModel(new DefaultComboBoxModel<DataScale>(
//				new DataScale[] {DataScale.LN, DataScale.LOG10, DataScale.SQRT}));
		dataScaleComboBox.setModel(new DefaultComboBoxModel<DataScale>(DataScale.values()));
		dataScaleComboBox.setSelectedItem(DataScale.LN);
		dataScaleComboBox.setMaximumSize(new Dimension(120, 26));
		GridBagConstraints gbc_dataScaleComboBox = new GridBagConstraints();
		gbc_dataScaleComboBox.gridwidth = 2;
		gbc_dataScaleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataScaleComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_dataScaleComboBox.gridx = 1;
		gbc_dataScaleComboBox.gridy = rowCount;
		add(dataScaleComboBox, gbc_dataScaleComboBox);
		
		rowCount++;
		
		gridBagLayout.rowHeights = new int[rowCount + 2];
		Arrays.fill(gridBagLayout.rowHeights, 0);
		
		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		
		JLabel lblNewLabel_7 = new JLabel("File sorting order");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = rowCount;
		add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		fileSortingOrderComboBox = new JComboBox<FileSortingOrder>(
				new DefaultComboBoxModel<FileSortingOrder>(FileSortingOrder.values()));
		GridBagConstraints gbc_fileSortingOrderComboBox = new GridBagConstraints();
		gbc_fileSortingOrderComboBox.gridwidth = 2;
		gbc_fileSortingOrderComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_fileSortingOrderComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileSortingOrderComboBox.gridx = 1;
		gbc_fileSortingOrderComboBox.gridy = rowCount;
		add(fileSortingOrderComboBox, gbc_fileSortingOrderComboBox);
		
		rowCount++;
		
		JLabel lblNewLabel_10 = new JLabel("Feature sorting order");
		GridBagConstraints gbc_lblNewLabel_10 = new GridBagConstraints();
		gbc_lblNewLabel_10.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_10.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_10.gridx = 0;
		gbc_lblNewLabel_10.gridy = rowCount;
		add(lblNewLabel_10, gbc_lblNewLabel_10);
		
		featureSortingOrderComboBox = new JComboBox<SortProperty>(
				new DefaultComboBoxModel<SortProperty>(
						new SortProperty[] {SortProperty.RT, SortProperty.MZ}));
		GridBagConstraints gbc_featureSortingOrderComboBox = new GridBagConstraints();
		gbc_featureSortingOrderComboBox.gridwidth = 2;
		gbc_featureSortingOrderComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_featureSortingOrderComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSortingOrderComboBox.gridx = 1;
		gbc_featureSortingOrderComboBox.gridy = rowCount;
		add(featureSortingOrderComboBox, gbc_featureSortingOrderComboBox);
		
		rowCount++;
		
		JLabel lblNewLabel_8 = new JLabel("Include sample groups:");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_8.gridwidth = 2;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.gridx = 0;
		gbc_lblNewLabel_8.gridy = rowCount;
		add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		rowCount++;
		
		sampleGroupTable = new SampleGroupTable();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridheight = 2;
		gbc_comboBox.gridwidth = 5;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = rowCount;
		add(new JScrollPane(sampleGroupTable), gbc_comboBox);
		
		rowCount++;
		
		JLabel lblNewLabel_6 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = rowCount;
		add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		rowCount++;
		
		refreshPlotButton = new JButton("Refresh plot", refreshDataIcon);
		refreshPlotButton.setActionCommand(
				MainActionCommands.REDRAW_HEAT_MAP_COMMAND.getName());
		refreshPlotButton.addActionListener(actListener);
		GridBagConstraints gbc_refreshPlotButton = new GridBagConstraints();
		gbc_refreshPlotButton.gridwidth = 2;
		gbc_refreshPlotButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_refreshPlotButton.gridx = 3;
		gbc_refreshPlotButton.gridy = rowCount;
		add(refreshPlotButton, gbc_refreshPlotButton);
		
		gridBagLayout.rowHeights = new int[rowCount + 2];
		Arrays.fill(gridBagLayout.rowHeights, 0);
		gridBagLayout.rowHeights[rowCount + 1] = Integer.MIN_VALUE;
		    
		gridBagLayout.rowWeights = new double[rowCount + 2];
		Arrays.fill(gridBagLayout.rowWeights, 0.0d);
		gridBagLayout.rowWeights[rowCount + 1] = Double.MIN_VALUE;
				
		loadPreferences();

		colorSchemeComboBox.addItemListener(externalItemListener);
		colorScaleComboBox.addItemListener(externalItemListener);
		colorSchemeComboBox.addItemListener(externalItemListener);
		dataScaleComboBox.addItemListener(externalItemListener);
		fileSortingOrderComboBox.addItemListener(externalItemListener);
		featureSortingOrderComboBox.addItemListener(externalItemListener);
		sampleGroupTable.getModel().addTableModelListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.RESET_MZ_RT_LIMITS_COMMAND.getName()))
			resetMZRTlimits();		
	}
	
	public void loadSampleTypes(Project experiment) {
		
		this.experiment = experiment;
		sampleGroupTable.getModel().removeTableModelListener(this);
		sampleGroupTable.loadSampleTypes(experiment);
		sampleGroupTable.getModel().addTableModelListener(this);
	}
	
	private void resetMZRTlimits() {
		
		double limit = 0.0d;
		startRTTextField.setText(Double.toString(limit));
		endRTTextField.setText(Double.toString(limit));
		startMZTextField.setText(Double.toString(limit));
		endMZTextField.setText(Double.toString(limit));
		refreshPlotButton.doClick();
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
		
		if(startRt < endRt)
			return new Range(startRt, endRt);
		else
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
		
		if(startMZ < endMZ)
			return new Range(startMZ, endMZ);
		else
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
	
	public FileSortingOrder getFileSortingOrder() {
		return (FileSortingOrder)fileSortingOrderComboBox.getSelectedItem();
	}
	
	public SortProperty getFeatureSortingOrder() {
		return (SortProperty)featureSortingOrderComboBox.getSelectedItem();
	}
	
	public Collection<ExperimentalSample> getSelectedSamples() {
		return sampleGroupTable.getSelectedSamples();
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

		ColorGradient cg = ColorGradient.getOptionByName(
						preferences.get(COLOR_SCHEME, ColorGradient.GREEN_RED.name()));			
		colorSchemeComboBox.setSelectedItem(cg);
		
		ColorScale cs = ColorScale.getOptionByName(
						preferences.get(COLOR_SCALE, ColorScale.LINEAR.name()));			
		colorScaleComboBox.setSelectedItem(cs);		
		
		SortProperty fileSortingOrder = SortProperty.getOptionByName(
				preferences.get(FILE_SORTING_ORDER, SortProperty.injectionTime.name()));
		fileSortingOrderComboBox.setSelectedItem(fileSortingOrder);
		
		SortProperty featureSortingOrder = SortProperty.getOptionByName(
				preferences.get(FEATURE_SORTING_ORDER, SortProperty.RT.name()));
		featureSortingOrderComboBox.setSelectedItem(featureSortingOrder);
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
		
		if(getFileSortingOrder() != null)
			preferences.put(FILE_SORTING_ORDER, getFileSortingOrder().name());
		
		if(getFeatureSortingOrder() != null)
			preferences.put(FEATURE_SORTING_ORDER, getFeatureSortingOrder().name());	
	}
	
	public MZRTPlotParameterObject getPlotParameters() {
		
		Set<ExperimentalSample>selectedSamples = 
				experiment.getExperimentDesign().getExperimentalSamplesBySampleTypes(
						sampleGroupTable.getSelectedSamples(), true);
		
		MZRTPlotParameterObject params = new MZRTPlotParameterObject();
			params.setMzRange(getMZRange());
			params.setRtRange(getRtRange());
			params.setColorGradient(getColorGradient());
			params.setColorScale(getColorScale());
			params.setDataScale((DataScale) dataScaleComboBox.getSelectedItem());
			params.setFileSortingOrder(getFileSortingOrder());
			params.setFeatureSortingOrder(getFeatureSortingOrder());
			params.setActiveSamples(selectedSamples);
		
		return params;
	}

	@Override
	public void tableChanged(TableModelEvent e) {

		SampleGroupTableModel tModel = (SampleGroupTableModel)e.getSource();
		if(e.getColumn() == tModel.getColumnIndex(SampleGroupTableModel.INCLUDE_COLUMN)
				&& e.getType() == TableModelEvent.UPDATE) {
			refreshPlotButton.doClick();
		}
	}
	
	public void clearSampleGroups() {
		
		sampleGroupTable.getModel().removeTableModelListener(this);
		sampleGroupTable.clearTable();
		sampleGroupTable.getModel().addTableModelListener(this);	
		experiment = null;
	}
}











