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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.math.NumberUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.BasicDialogWithPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.FormUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class FilterFeaturesByMzRtListDialog extends BasicDialogWithPreferences {

	private static final long serialVersionUID = 1L;

	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	public static final String RT_WINDOW = "RT_WINDOW";
	public static final String MASS_WINDOW = "MASS_WINDOW";
	public static final String MASS_ERROR_TYPE = "MASS_ERROR_TYPE";
	
	private static final String BROWSE_COMMAND = "Browse...";
	private File baseDirectory;
	private File inputFile;
	private String[][] inputDataArray;
	
	private JTextField inputFileTextField;
	private JComboBox<String> mzColumnComboBox;
	private JComboBox<String> rtColumnComboBox;
	private JComboBox<MassErrorType> massErrorTypecomboBox;
	private JFormattedTextField rtWindowField;
	private JFormattedTextField massWindowField;
	private JTextField dataSetNameField;
	
	
	public FilterFeaturesByMzRtListDialog( ActionListener actionListener) {
		super("Filter features usin MZ/RT list", 
				"filterClusterByMZRTList", 
				new Dimension(640,320), 
				actionListener);
		primaryActionButton.setText(
				MainActionCommands.FILTER_FEATURES_BY_MZ_RT_LIST_COMMAND.getName());
		primaryActionButton.setActionCommand(
				MainActionCommands.FILTER_FEATURES_BY_MZ_RT_LIST_COMMAND.getName());
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		mainPanel.setLayout(gridBagLayout);
		
		JLabel lblNewLabel_6 = new JLabel("Filtered set name");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 0;
		mainPanel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		dataSetNameField = new JTextField();
		dataSetNameField.setText("MZ/RT filtered feature set " + 
				MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(new Date()));
		GridBagConstraints gbc_dataSetNameField = new GridBagConstraints();
		gbc_dataSetNameField.gridwidth = 2;
		gbc_dataSetNameField.insets = new Insets(0, 0, 5, 5);
		gbc_dataSetNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataSetNameField.gridx = 1;
		gbc_dataSetNameField.gridy = 0;
		mainPanel.add(dataSetNameField, gbc_dataSetNameField);
		dataSetNameField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("MZ/RT list file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		mainPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_inputFileTextField = new GridBagConstraints();
		gbc_inputFileTextField.gridwidth = 2;
		gbc_inputFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_inputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileTextField.gridx = 0;
		gbc_inputFileTextField.gridy = 2;
		mainPanel.add(inputFileTextField, gbc_inputFileTextField);
		
		JButton btnNewButton = new JButton(BROWSE_COMMAND);
		btnNewButton.setActionCommand(BROWSE_COMMAND);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 2;
		mainPanel.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Select M/Z column");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		mainPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		mzColumnComboBox = new JComboBox<>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 3;
		mainPanel.add(mzColumnComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_2 = new JLabel("Select RT column");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 4;
		mainPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		rtColumnComboBox = new JComboBox<>();
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 4;
		mainPanel.add(rtColumnComboBox, gbc_comboBox_1);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(
				null, "Filtering settings", TitledBorder.LEADING, 
				TitledBorder.TOP, null, null), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		mainPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_3 = new JLabel("Mass window");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 0;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		massWindowField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massWindowField.setPreferredSize(new Dimension(80, 20));
		massWindowField.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_massWindowTextField = new GridBagConstraints();
		gbc_massWindowTextField.insets = new Insets(0, 0, 5, 5);
		gbc_massWindowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massWindowTextField.gridx = 1;
		gbc_massWindowTextField.gridy = 0;
		panel.add(massWindowField, gbc_massWindowTextField);
		
		massErrorTypecomboBox = 
				new JComboBox<>(new DefaultComboBoxModel<>(MassErrorType.values()));
		massErrorTypecomboBox.setPreferredSize(new Dimension(80, 22));
		massErrorTypecomboBox.setMinimumSize(new Dimension(80, 22));
		GridBagConstraints gbc_massErrorTypecomboBox = new GridBagConstraints();
		gbc_massErrorTypecomboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTypecomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypecomboBox.gridx = 2;
		gbc_massErrorTypecomboBox.gridy = 0;
		panel.add(massErrorTypecomboBox, gbc_massErrorTypecomboBox);
		
		JLabel lblNewLabel_4 = new JLabel("RT window");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 1;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		rtWindowField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowField.setMinimumSize(new Dimension(80, 20));
		rtWindowField.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 1;
		panel.add(rtWindowField, gbc_formattedTextField);
		
		JLabel lblNewLabel_5 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridx = 2;
		gbc_lblNewLabel_5.gridy = 1;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectMZRTListFile();
	}
	
	private void selectMZRTListFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setTitle("Select targeted data file (plain text)");
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files (TAB-separated)", "txt", "TXT", "tsv", "TSV");
		fc.addFilter("CSV files (Comma-separated)", "csv", "CSV");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			inputFileTextField.setText(inputFile.getAbsolutePath());
			populateColumnSelector();
		}
	}
	
	private void populateColumnSelector() {
		
		inputDataArray = 
				DelimitedTextParser.parseDataFileBasedOnExtension(inputFile);
		mzColumnComboBox.setModel(new DefaultComboBoxModel<>(inputDataArray[0]));
		mzColumnComboBox.setSelectedIndex(-1);
		
		Vector<String>rtColumns = new Vector<>();
		rtColumns.add(null);
		rtColumns.addAll(Arrays.asList(inputDataArray[0]));
		rtColumnComboBox.setModel(new DefaultComboBoxModel<>(rtColumns));
		rtColumnComboBox.setSelectedIndex(-1);
		for(int i=1; i<rtColumns.size(); i++) {
			
			if(rtColumns.get(i).toUpperCase().contains("RT") 
					|| rtColumns.get(i).toUpperCase().contains("RETEN")) {
				rtColumnComboBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public String getFilteredFeatureSetName() {
		return dataSetNameField.getText().trim();
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public String getMzColumnName() {
		return (String)mzColumnComboBox.getSelectedItem();
	}
	
	public int getMzColumnIndex() {
		return mzColumnComboBox.getSelectedIndex();
	}
	
	public String getRetentionColumnName() {
		return (String)rtColumnComboBox.getSelectedItem();
	}
	
	public int getRtColumnIndex() {
		return rtColumnComboBox.getSelectedIndex() - 1;
	}
	
	public double getMassWindow() {
		return FormUtils.getDoubleValueFromTextField(massWindowField);
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypecomboBox.getSelectedItem();
	}
	
	public double getRTWindow() {
		return FormUtils.getDoubleValueFromTextField(rtWindowField);
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    
	    String dataSetName = getFilteredFeatureSetName();
	    
	    if(dataSetName.isEmpty())
	    	errors.add("Filtered set name must be specified");
	    else {
	    	DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
	    	MsFeatureSet existing = 
	    			project.getMsFeatureSetsForDataPipeline(project.getActiveDataPipeline()).
	    			stream().filter(s -> s.getName().equals(dataSetName)).findFirst().orElse(null);
	    	if(existing != null)
	    		errors.add("MS feature set \"" + dataSetName +"\" already exists in the project");
	    }	    
	    if(getInputFile() == null)
	    	errors.add("MZ/RT list file not specified");
	    
	    if(inputDataArray == null || inputDataArray.length < 2)
	    	errors.add("No data in MZ/RT list file");
	    
	    if(getMzColumnName() == null)
	    	errors.add("MZ column not specified");
	    
	    if(getRetentionColumnName() == null)
	    	errors.add("RT column not specified");
	    
	    if(getMassWindow() <= 0.0d)
	        errors.add("Mass window must be > 0");
	    
	    if(getRTWindow() <= 0.0d)
	        errors.add("RT window must be > 0");
	    
	    if(errors.isEmpty())
	    	validateInputData(errors);
	    		
	    return errors;
	}
	
	private void validateInputData(Collection<String> errors) {
				
		int rtColumnIndex = getRtColumnIndex();		
		int mzColumnIndex = getMzColumnIndex();
		String value = null;
		
		for(int i=1; i<inputDataArray.length; i++) {
			
			value = inputDataArray[i][rtColumnIndex];
			if(value == null || value.isBlank() || !NumberUtils.isCreatable(value))
				errors.add("Invalid RT value on the line " + (i+1));
			
			value = inputDataArray[i][mzColumnIndex];
			if(value == null || value.isBlank() || !NumberUtils.isCreatable(value))
				errors.add("Invalid MZ value on the line " + (i+1));
		}	
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String baseDirPath = preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			baseDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double rtWindow = preferences.getDouble(RT_WINDOW, 0.1d);		
		rtWindowField.setText(Double.toString(rtWindow));
		
		double massWindow = preferences.getDouble(MASS_WINDOW, 15.0d);		
		massWindowField.setText(Double.toString(massWindow));
		
		String massErrorTypeString = preferences.get(MASS_ERROR_TYPE, MassErrorType.ppm.name());
		MassErrorType massErrorType = MassErrorType.getTypeByName(massErrorTypeString);
		if(massErrorType == null)
			massErrorType = MassErrorType.ppm;
		
		massErrorTypecomboBox.setSelectedItem(massErrorType);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
		preferences.putDouble(MASS_WINDOW, getMassWindow());
		preferences.put(MASS_ERROR_TYPE, getMassErrorType().name());
		preferences.putDouble(RT_WINDOW, getRTWindow());
	}

	public String[][] getInputDataArray() {
		return inputDataArray;
	}
}
