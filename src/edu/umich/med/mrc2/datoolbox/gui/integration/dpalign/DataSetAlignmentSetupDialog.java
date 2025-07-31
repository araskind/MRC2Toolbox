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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataSetAlignmentSetupDialog extends JDialog implements ActionListener, BackedByPreferences, ListSelectionListener {

	private static final long serialVersionUID = -8085095852693381565L;
	private static final Icon dataSetAlignmentIcon = GuiUtils.getIcon("alignment", 32);
	private Preferences preferences;
	private JLabel acqMethodNameLabel;
	private JLabel daMethodNameLabel;
	private JLabel daSoftwareNameLabel;
	private DataPipelineSelectionTable dataPipelineSelectionTable;

	private Collection<DataPipeline>pipelines;
	
	public static final String MASS_WINDOW = "MASS_WINDOW";
	public static final String MASS_ERROR_TYPE = "MASS_ERROR_TYPE";
	public static final String RT_WINDOW_MIN = "RT_WINDOW_MIN";
	public static final String EXCLUDE_UNDETECTED = "EXCLUDE_UNDETECTED";
	
	private JComboBox<MassErrorType> massErrorTypeComboBox;
	private JFormattedTextField massErrorField;
	private JFormattedTextField rtErrorField;
	private JCheckBox excludeUndetectedCheckBox;
	
	public DataSetAlignmentSetupDialog(
			Collection<DataPipeline>pipelines, 
			ActionListener actionListener) {
		super();
		this.pipelines = pipelines;
		
		setTitle("Data set alignment setup");		
		setIconImage(((ImageIcon) dataSetAlignmentIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		dataPipelineSelectionTable = new DataPipelineSelectionTable();
		dataPipelineSelectionTable.setTableModelFromDataPipelineCollection(pipelines);
		dataPipelineSelectionTable.getSelectionModel().addListSelectionListener(this);
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.insets = new Insets(0, 0, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 0;
		dataPanel.add(new JScrollPane(dataPipelineSelectionTable), gbc_table);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Method details", TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 0)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		dataPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Acquisition method ");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		acqMethodNameLabel = new JLabel("");
		GridBagConstraints gbc_acqMethodNameLabel = new GridBagConstraints();
		gbc_acqMethodNameLabel.anchor = GridBagConstraints.WEST;
		gbc_acqMethodNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_acqMethodNameLabel.gridx = 1;
		gbc_acqMethodNameLabel.gridy = 0;
		panel_1.add(acqMethodNameLabel, gbc_acqMethodNameLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Data analysis method ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		daMethodNameLabel = new JLabel("");
		GridBagConstraints gbc_daMethodNameLabel = new GridBagConstraints();
		gbc_daMethodNameLabel.anchor = GridBagConstraints.WEST;
		gbc_daMethodNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_daMethodNameLabel.gridx = 1;
		gbc_daMethodNameLabel.gridy = 1;
		panel_1.add(daMethodNameLabel, gbc_daMethodNameLabel);
		
		JLabel lblNewLabel_2 = new JLabel("Data analysis software ");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		daSoftwareNameLabel = new JLabel("");
		GridBagConstraints gbc_daSoftwareNameLabel = new GridBagConstraints();
		gbc_daSoftwareNameLabel.anchor = GridBagConstraints.WEST;
		gbc_daSoftwareNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_daSoftwareNameLabel.gridx = 1;
		gbc_daSoftwareNameLabel.gridy = 2;
		panel_1.add(daSoftwareNameLabel, gbc_daSoftwareNameLabel);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Alignment parameters", TitledBorder.LEADING, TitledBorder.TOP, null, 
				new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		dataPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_3 = new JLabel("Mass eror");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 0;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		massErrorField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorField.setMinimumSize(new Dimension(80, 20));
		massErrorField.setPreferredSize(new Dimension(80, 20));
		massErrorField.setColumns(10);
		GridBagConstraints gbc_massErrorField = new GridBagConstraints();
		gbc_massErrorField.insets = new Insets(0, 0, 5, 5);
		gbc_massErrorField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorField.gridx = 1;
		gbc_massErrorField.gridy = 0;
		panel.add(massErrorField, gbc_massErrorField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setPreferredSize(new Dimension(80, 22));
		massErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 0;
		panel.add(massErrorTypeComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_4 = new JLabel("RT error");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.gridx = 3;
		gbc_lblNewLabel_4.gridy = 0;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		rtErrorField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtErrorField.setColumns(10);
		rtErrorField.setMinimumSize(new Dimension(80, 20));
		rtErrorField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_rtErrorField = new GridBagConstraints();
		gbc_rtErrorField.insets = new Insets(0, 0, 5, 5);
		gbc_rtErrorField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtErrorField.gridx = 4;
		gbc_rtErrorField.gridy = 0;
		panel.add(rtErrorField, gbc_rtErrorField);
		
		JLabel lblNewLabel_5 = new JLabel("min");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridx = 5;
		gbc_lblNewLabel_5.gridy = 0;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		excludeUndetectedCheckBox = 
				new JCheckBox("Exclude features not detected in samples and pools");
		GridBagConstraints gbc_excludeUndetectedCheckBox = new GridBagConstraints();
		gbc_excludeUndetectedCheckBox.anchor = GridBagConstraints.WEST;
		gbc_excludeUndetectedCheckBox.gridwidth = 6;
		gbc_excludeUndetectedCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_excludeUndetectedCheckBox.gridx = 0;
		gbc_excludeUndetectedCheckBox.gridy = 1;
		panel.add(excludeUndetectedCheckBox, gbc_excludeUndetectedCheckBox);

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
				MainActionCommands.DATA_SET_ALIGNMENT_RUN_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.DATA_SET_ALIGNMENT_RUN_COMMAND.getName());
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

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	public List<DataPipeline>getSelectedDataPipelines(){
		return dataPipelineSelectionTable.getCheckedDataPipelines();
	}
	
	public double getMassWindow() {
		return Double.parseDouble(massErrorField.getText());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}

	public double getRetentionWindow() {
		return Double.parseDouble(rtErrorField.getText());
	}
	
	public boolean excludeUndetected() {
		return excludeUndetectedCheckBox.isSelected();
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<String>();
	    Collection<DataPipeline> selectedDataPipelines = getSelectedDataPipelines();
	    
	    if(selectedDataPipelines.size() != 2)
	    	errors.add("Please select (check) TWO data pipelines to align data sets");
	    
	    DataAnalysisProject currentExperiment = 
	    		MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		for(DataPipeline dp : selectedDataPipelines) {
			
			if(currentExperiment.getAveragedFeatureLibraryForDataPipeline(dp) == null) 
				 errors.add("Averaged feature library must be generated "
				 		+ "for data pipeline \"" + dp.getName() + "\"");			
		}    
	    if(getMassWindow() <= 0)
	        errors.add("Mass error must be > 0");
	    
	    if(getMassErrorType() == null)
	        errors.add("Mass error type must be specified");
	    
	    if(getRetentionWindow() <= 0)	        
	    	errors.add("RT error must be > 0");
	    		
	    return errors;
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;	
		double massError = preferences.getDouble(MASS_WINDOW, 20.0d);
		massErrorField.setText(Double.toString(massError));
		
		MassErrorType massErrorType = MassErrorType.getTypeByName(
				preferences.get(MASS_ERROR_TYPE, MassErrorType.ppm.name()));
		massErrorTypeComboBox.setSelectedItem(massErrorType);
		double rtWindow = preferences.getDouble(RT_WINDOW_MIN, 0.05d);
		rtErrorField.setText(Double.toString(rtWindow));
		
		excludeUndetectedCheckBox.setSelected(
				preferences.getBoolean(EXCLUDE_UNDETECTED, true));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putDouble(MASS_WINDOW, getMassWindow());		
		preferences.put(MASS_ERROR_TYPE, getMassErrorType().name());
		preferences.putDouble(RT_WINDOW_MIN, getRetentionWindow());
		preferences.putBoolean(EXCLUDE_UNDETECTED, excludeUndetected());
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting())
			showDataForSelectedPipeline();		
	}
	
	private void showDataForSelectedPipeline() {
		
		DataPipeline pl = dataPipelineSelectionTable.getSelectedDataPipeline();
		if(pl == null)
			return;
		
		if(pl.getAcquisitionMethod().getSoftware() == null) {
			DataAcquisitionMethod dbMethod = 
					IDTDataCache.getAcquisitionMethodById(pl.getAcquisitionMethod().getId());
			if(dbMethod != null && dbMethod.getSoftware() != null)
				pl.getAcquisitionMethod().setSoftware(dbMethod.getSoftware());
		}
		if(pl.getDataExtractionMethod().getSoftware() == null) {
			DataExtractionMethod dbMethod = 
					IDTDataCache.getDataExtractionMethodById(pl.getDataExtractionMethod().getId());
			if(dbMethod != null && dbMethod.getSoftware() != null)
				pl.getDataExtractionMethod().setSoftware(dbMethod.getSoftware());
		}
		acqMethodNameLabel.setText(pl.getAcquisitionMethod().getName() + 
				" (" + pl.getAcquisitionMethod().getId() + ")");			
		daMethodNameLabel.setText(pl.getDataExtractionMethod().getName() + 
				" (" + pl.getDataExtractionMethod().getId() + ")");			
		daSoftwareNameLabel.setText(
				pl.getDataExtractionMethod().getSoftware().getName());
	}
}












