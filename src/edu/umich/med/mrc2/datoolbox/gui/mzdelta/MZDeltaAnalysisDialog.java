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

package edu.umich.med.mrc2.datoolbox.gui.mzdelta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.tables.DoubleValueTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MZDeltaAnalysisDialog extends JDialog implements ActionListener, BackedByPreferences{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon dialogIcon = GuiUtils.getIcon("extractDeltas", 24);
	private DoubleValueTable anchorMzList;
	private DoubleValueTable rtSeriesMzList;
	private JFormattedTextField anchorMassErrorTextField;
	private JComboBox<MassErrorType> anchorMassErrorTypeComboBox;
	private JFormattedTextField anchorRtErrorTextField;
	private JFormattedTextField rtSeriesMassErrorTextField;
	private JComboBox<MassErrorType> rtSeriesMassErrorTypeComboBox;
	private JFormattedTextField rtSeriesMinStepTextField;
	
	private Preferences preferences;
	private static final String ANCHOR_MASS_LIST = "ANCHOR_MASS_LIST";
	private static final String ANCHOR_MASS_ERROR = "ANCHOR_MASS_ERROR";
	private static final String ANCHOR_MASS_ERROR_TYPE = "ANCHOR_MASS_ERROR_TYPE";
	private static final String ANCHOR_RT_ERROR = "ANCHOR_RT_ERROR";
	private static final String RT_SERIES_MASS_LIST = "RT_SERIES_MASS_LIST";
	private static final String RT_SERIES_MASS_ERROR = "RT_SERIES_MASS_ERROR";
	private static final String RT_SERIES_MASS_ERROR_TYPE = "RT_SERIES_MASS_ERROR_TYPE";
	private static final String RT_SERIES_MIN_STEP = "RT_SERIES_MIN_STEP";
	
	private static final String ADD_ANCHOR_MASS = "ADD_ANCHOR_MASS";
	private static final String REMOVE_ANCHOR_MASS = "REMOVE_ANCHOR_MASS";
	private static final String ADD_RT_SERIES_MASS = "ADD_RT_SERIES_MASS";
	private static final String REMOVE_RT_SERIES_MASS = "REMOVE_RT_SERIES_MASS";
	
	public MZDeltaAnalysisDialog(ActionListener actionListener) {
		super();
		setTitle("Analyze mass difference patterns");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JPanel coelutePanel = new JPanel();
		coelutePanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Anchor mass series", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		dataPanel.add(coelutePanel, gbc_panel);
		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE, 0.0};
		coelutePanel.setLayout(gbl_panel);
		
		anchorMzList = new DoubleValueTable();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		coelutePanel.add(new JScrollPane(anchorMzList), gbc_scrollPane);
		
		JButton addAnchorMassButton = new JButton("Add value");
		addAnchorMassButton.setActionCommand(ADD_ANCHOR_MASS);
		addAnchorMassButton.addActionListener(this);
		GridBagConstraints gbc_addCoElutingMassButton = new GridBagConstraints();
		gbc_addCoElutingMassButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_addCoElutingMassButton.insets = new Insets(0, 0, 5, 5);
		gbc_addCoElutingMassButton.gridx = 0;
		gbc_addCoElutingMassButton.gridy = 1;
		coelutePanel.add(addAnchorMassButton, gbc_addCoElutingMassButton);
		
		JButton removeAnchorMassButton = new JButton("Remove selected");
		removeAnchorMassButton.setActionCommand(REMOVE_ANCHOR_MASS);
		removeAnchorMassButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		coelutePanel.add(removeAnchorMassButton, gbc_btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Mass error");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		coelutePanel.add(lblNewLabel, gbc_lblNewLabel);
		
		anchorMassErrorTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		anchorMassErrorTextField.setMinimumSize(new Dimension(60, 20));
		anchorMassErrorTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_coeluteMassErrorTextField = new GridBagConstraints();
		gbc_coeluteMassErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coeluteMassErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coeluteMassErrorTextField.gridx = 1;
		gbc_coeluteMassErrorTextField.gridy = 2;
		coelutePanel.add(anchorMassErrorTextField, gbc_coeluteMassErrorTextField);
		
		anchorMassErrorTypeComboBox = 
				new JComboBox<>(new DefaultComboBoxModel<>(MassErrorType.values()));
		GridBagConstraints gbc_coeluteMassErrorTypeComboBox = new GridBagConstraints();
		gbc_coeluteMassErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_coeluteMassErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_coeluteMassErrorTypeComboBox.gridx = 2;
		gbc_coeluteMassErrorTypeComboBox.gridy = 2;
		coelutePanel.add(anchorMassErrorTypeComboBox, gbc_coeluteMassErrorTypeComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("RT window");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		coelutePanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		anchorRtErrorTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		anchorRtErrorTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_coeluteRtErrorTextField = new GridBagConstraints();
		gbc_coeluteRtErrorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_coeluteRtErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coeluteRtErrorTextField.gridx = 1;
		gbc_coeluteRtErrorTextField.gridy = 3;
		coelutePanel.add(anchorRtErrorTextField, gbc_coeluteRtErrorTextField);
		
		JLabel lblNewLabel_2 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 3;
		coelutePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JPanel rtSeriesPanel = new JPanel();
		rtSeriesPanel.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "RT-series mass differences", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 4;
		gbc_panel_1.gridy = 0;
		dataPanel.add(rtSeriesPanel, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		rtSeriesPanel.setLayout(gbl_panel_1);
		
		rtSeriesMzList = new DoubleValueTable();
		
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		rtSeriesPanel.add(new JScrollPane(rtSeriesMzList), gbc_scrollPane_1);
		
		JButton addRtSeriesMassButton = new JButton("Add value");
		addRtSeriesMassButton.setActionCommand(ADD_RT_SERIES_MASS);
		addRtSeriesMassButton.addActionListener(this);
		GridBagConstraints gbc_addRtSeriesMassButton = new GridBagConstraints();
		gbc_addRtSeriesMassButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_addRtSeriesMassButton.insets = new Insets(0, 0, 5, 5);
		gbc_addRtSeriesMassButton.gridx = 0;
		gbc_addRtSeriesMassButton.gridy = 1;
		rtSeriesPanel.add(addRtSeriesMassButton, gbc_addRtSeriesMassButton);
		
		JButton removeRtSeriesMassButton = new JButton("Remove selected");
		removeRtSeriesMassButton.setActionCommand(REMOVE_RT_SERIES_MASS);
		removeRtSeriesMassButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.gridwidth = 2;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 1;
		rtSeriesPanel.add(removeRtSeriesMassButton, gbc_btnNewButton_1);
		
		JLabel lblNewLabel_4 = new JLabel("Mass error");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 2;
		rtSeriesPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		rtSeriesMassErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		rtSeriesMassErrorTextField.setMinimumSize(new Dimension(60, 20));
		rtSeriesMassErrorTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_rtSeriesMassErrorTextField = new GridBagConstraints();
		gbc_rtSeriesMassErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtSeriesMassErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtSeriesMassErrorTextField.gridx = 1;
		gbc_rtSeriesMassErrorTextField.gridy = 2;
		rtSeriesPanel.add(rtSeriesMassErrorTextField, gbc_rtSeriesMassErrorTextField);
		
		rtSeriesMassErrorTypeComboBox = 
				new JComboBox<>(new DefaultComboBoxModel<>(MassErrorType.values()));
		GridBagConstraints gbc_rtSeriesMassErrorTypeComboBox = new GridBagConstraints();
		gbc_rtSeriesMassErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_rtSeriesMassErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtSeriesMassErrorTypeComboBox.gridx = 2;
		gbc_rtSeriesMassErrorTypeComboBox.gridy = 2;
		rtSeriesPanel.add(rtSeriesMassErrorTypeComboBox, gbc_rtSeriesMassErrorTypeComboBox);
		
		JLabel lblNewLabel_5 = new JLabel("Minimal RT difference");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		rtSeriesPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		rtSeriesMinStepTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtSeriesMinStepTextField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_rtSeriesMinStepTextField = new GridBagConstraints();
		gbc_rtSeriesMinStepTextField.insets = new Insets(0, 0, 0, 5);
		gbc_rtSeriesMinStepTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtSeriesMinStepTextField.gridx = 1;
		gbc_rtSeriesMinStepTextField.gridy = 3;
		rtSeriesPanel.add(rtSeriesMinStepTextField, gbc_rtSeriesMinStepTextField);
		
		JLabel lblNewLabel_6 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_6.gridx = 2;
		gbc_lblNewLabel_6.gridy = 3;
		rtSeriesPanel.add(lblNewLabel_6, gbc_lblNewLabel_6);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.RUN_MZ_DIFFERENCE_ANALYSIS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.RUN_MZ_DIFFERENCE_ANALYSIS_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public Set<Double>getAnchorMassSet(){
		
		Set<Double>anchorMassSet = new TreeSet<>();
		anchorMassSet.addAll(anchorMzList.getValues());
		return anchorMassSet;
	}
	
	public Set<Double>getRTSeriesMassSet(){
		
		Set<Double>rtSeriesMassSet = new TreeSet<>();
		rtSeriesMassSet.addAll(rtSeriesMzList.getValues());
		return rtSeriesMassSet;
	}
	
	public double getAnchorMassError() {
		return Double.parseDouble(anchorMassErrorTextField.getText());
	}
	
	public MassErrorType getAnchorMassErrorType() {
		return (MassErrorType)anchorMassErrorTypeComboBox.getSelectedItem();
	}

	public double getAnchorRTError() {
		return Double.parseDouble(anchorRtErrorTextField.getText());
	}
	
	public double getRTSeriesMassError() {
		return Double.parseDouble(rtSeriesMassErrorTextField.getText());
	}
	
	public MassErrorType getRTSeriesMassErrorType() {
		return (MassErrorType)rtSeriesMassErrorTypeComboBox.getSelectedItem();
	}

	public double getRTSeriesMinStep() {
		return Double.parseDouble(rtSeriesMinStepTextField.getText());
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    if(getAnchorMassSet().isEmpty())
	    	errors.add("No values in the anchor mass list.");
	    
	    if(getRTSeriesMassSet().isEmpty())
	    	errors.add("No values in the RT series mass difference list.");
	    
	    if(getAnchorMassError() <= 0.0d)
	    	errors.add("M/Z error for anchor mass points must be > 0");
	
	    if(getAnchorRTError() <= 0.0d)
	    	errors.add("RT error for anchor mass points must be > 0");
	    
	    if(getRTSeriesMassError() <= 0.0d)
	    	errors.add("M/Z error for RT series mass points must be > 0");
	    
	    if(getRTSeriesMinStep() <= 0.0d)
	    	errors.add("RT series minimal step must be > 0");
	    
	    return errors;
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		
		String anchorMassListStrig = preferences.get(ANCHOR_MASS_LIST, "");
		String[] anchorMassList = anchorMassListStrig.split("@");
		if(anchorMassList.length > 0) {
			
			Collection<Double> anchorValues = new TreeSet<>();
			for(String am : anchorMassList) {
				if(NumberUtils.isCreatable(am))
					anchorValues.add(Double.parseDouble(am));
			}			
			anchorMzList.setTableModelFromValues(anchorValues);
		}		
		String rtSeriesMassListStrig = preferences.get(RT_SERIES_MASS_LIST, "");
		String[] rtSeriesMassList = rtSeriesMassListStrig.split("@");
		if(rtSeriesMassList.length > 0) {
			
			Collection<Double> rtSeriesValues = new TreeSet<>();
			for(String am : rtSeriesMassList) {
				if(NumberUtils.isCreatable(am))
					rtSeriesValues.add(Double.parseDouble(am));
			}
			rtSeriesMzList.setTableModelFromValues(rtSeriesValues);
		}
		double anchorMassError = preferences.getDouble(ANCHOR_MASS_ERROR, 15.0d);
		anchorMassErrorTextField.setText(Double.toString(anchorMassError));
		
		MassErrorType anchorMassErrorType = MassErrorType.getTypeByName(
				preferences.get(ANCHOR_MASS_ERROR_TYPE, MassErrorType.ppm.name()));
		anchorMassErrorTypeComboBox.setSelectedItem(anchorMassErrorType);
		
		double anchorRTError = preferences.getDouble(ANCHOR_RT_ERROR, 0.02d);
		anchorRtErrorTextField.setText(Double.toString(anchorRTError));
		
		double rtSeriesMassError = preferences.getDouble(RT_SERIES_MASS_ERROR, 20.0d);
		rtSeriesMassErrorTextField.setText(Double.toString(rtSeriesMassError));
		
		MassErrorType rtSeriesMassErrorType = MassErrorType.getTypeByName(
				preferences.get(RT_SERIES_MASS_ERROR_TYPE, MassErrorType.ppm.name()));
		rtSeriesMassErrorTypeComboBox.setSelectedItem(rtSeriesMassErrorType);
		
		double rtSeriesMinStep = preferences.getDouble(RT_SERIES_MIN_STEP, 0.06);
		rtSeriesMinStepTextField.setText(Double.toString(rtSeriesMinStep));
	}

	@Override
	public void loadPreferences() {		
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences.put(ANCHOR_MASS_LIST, StringUtils.join(getAnchorMassSet(), "@"));
		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putDouble(ANCHOR_MASS_ERROR, getAnchorMassError());
		preferences.put(ANCHOR_MASS_ERROR_TYPE, getAnchorMassErrorType().name());
		preferences.putDouble(ANCHOR_RT_ERROR, getAnchorRTError());
		preferences.put(RT_SERIES_MASS_LIST, StringUtils.join(getRTSeriesMassSet(), "@"));
		preferences.putDouble(RT_SERIES_MASS_ERROR, getRTSeriesMassError());
		preferences.put(RT_SERIES_MASS_ERROR_TYPE, getRTSeriesMassErrorType().name());
		preferences.putDouble(RT_SERIES_MIN_STEP, getRTSeriesMinStep());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(ADD_ANCHOR_MASS)) 
			anchorMzList.addRowAndStartEditing();
		
		if(command.equals(REMOVE_ANCHOR_MASS))
			anchorMzList.removeSelectedRows();
		
		if(command.equals(ADD_RT_SERIES_MASS))
			rtSeriesMzList.addRowAndStartEditing();
			
		if(command.equals(REMOVE_RT_SERIES_MASS))
			rtSeriesMzList.removeSelectedRows();
	}
}
