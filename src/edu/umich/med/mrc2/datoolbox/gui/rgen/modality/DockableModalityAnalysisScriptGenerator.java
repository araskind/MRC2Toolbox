/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.rgen.modality;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rqc.ModalityAnalysisScriptGenerator;
import edu.umich.med.mrc2.datoolbox.rqc.RAnalysisUtils;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class DockableModalityAnalysisScriptGenerator extends DefaultSingleCDockable
		implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogram", 32);
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.DockableModalityAnalysisScriptGenerator";
	public static final String WORK_DIRECTORY = "WORK_DIRECTORY";
	public static final String MAX_PERCENT_MISSING = "MAX_PERCENT_MISSING";
	public static final String P_VALUE_CUTOFF = "P_VALUE_CUTOFF";
	
	public static final String BROWSE_COMMAND = "Browse";
	
	private File workDirectory;	
	private ModalityAnalysisInputFileListingTable inputFileListingTable;
	private ModalityAnalysisScriptDialogToolbar toolbar;
	private JTextField workDirectoryTextField;
	private JSpinner maxPercentMissingSpinner;
	private JFormattedTextField pValueTextField;
	
	public DockableModalityAnalysisScriptGenerator() {
		
		super("DockableModalityAnalysisScriptGenerator", componentIcon, 
				"Find features with multimodal distribution of parameters", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		toolbar = new ModalityAnalysisScriptDialogToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(dataPanel, BorderLayout.CENTER);
		
		add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{111, 0, 0, 0, 0, 105, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Data files directory:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		workDirectoryTextField = new JTextField();	
		workDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_workDirectoryTextField = new GridBagConstraints();
		gbc_workDirectoryTextField.gridwidth = 5;
		gbc_workDirectoryTextField.insets = new Insets(0, 0, 5, 5);
		gbc_workDirectoryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_workDirectoryTextField.gridx = 1;
		gbc_workDirectoryTextField.gridy = 0;
		dataPanel.add(workDirectoryTextField, gbc_workDirectoryTextField);
		workDirectoryTextField.setColumns(10);
		
		JButton selectWorkingDirButton = new JButton(BROWSE_COMMAND);
		selectWorkingDirButton.setActionCommand(BROWSE_COMMAND);
		selectWorkingDirButton.addActionListener(this);
		GridBagConstraints gbc_selectWorkingDirButton = new GridBagConstraints();
		gbc_selectWorkingDirButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectWorkingDirButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectWorkingDirButton.gridx = 6;
		gbc_selectWorkingDirButton.gridy = 0;
		dataPanel.add(selectWorkingDirButton, gbc_selectWorkingDirButton);
		
		inputFileListingTable = new ModalityAnalysisInputFileListingTable();
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridwidth = 7;
		gbc_table.insets = new Insets(0, 0, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 1;
		JScrollPane scrollPane = new JScrollPane(inputFileListingTable);
		scrollPane.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "MetabCombiner input files for alignment", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(scrollPane, gbc_table);
		
		JButton selectMCFilesButton = new JButton(
				MainActionCommands.SELECT_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.setActionCommand(
				MainActionCommands.SELECT_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName());
		selectMCFilesButton.addActionListener(this);
		
		GridBagConstraints gbc_selectMCFilesButton = new GridBagConstraints();
		gbc_selectMCFilesButton.gridwidth = 2;
		gbc_selectMCFilesButton.anchor = GridBagConstraints.EAST;
		gbc_selectMCFilesButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectMCFilesButton.gridx = 5;
		gbc_selectMCFilesButton.gridy = 2;
		dataPanel.add(selectMCFilesButton, gbc_selectMCFilesButton);
		
		JButton clearMCFilesButton = new JButton(
				MainActionCommands.CLEAR_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.setActionCommand(
				MainActionCommands.CLEAR_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName());
		clearMCFilesButton.addActionListener(this);
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 2;
		dataPanel.add(clearMCFilesButton, gbc_btnNewButton_1);
		
		JButton importFromFileButton = new JButton(
				MainActionCommands.IMPORT_MODALITY_ANALYSIS_INPUTS_FROM_FILE_COMMAND.getName());
		importFromFileButton.setActionCommand(
				MainActionCommands.IMPORT_MODALITY_ANALYSIS_INPUTS_FROM_FILE_COMMAND.getName());
		importFromFileButton.addActionListener(this);
		GridBagConstraints gbc_importFromFileButton = new GridBagConstraints();
		gbc_importFromFileButton.insets = new Insets(0, 0, 5, 5);
		gbc_importFromFileButton.gridx = 4;
		gbc_importFromFileButton.gridy = 2;
		dataPanel.add(importFromFileButton, gbc_importFromFileButton);
		
		JLabel lblNewLabel_1 = new JLabel("Max. % missing data");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		maxPercentMissingSpinner = new JSpinner();
		maxPercentMissingSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		GridBagConstraints gbc_maxPercentMissingSpinner = new GridBagConstraints();
		gbc_maxPercentMissingSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxPercentMissingSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_maxPercentMissingSpinner.gridx = 1;
		gbc_maxPercentMissingSpinner.gridy = 3;
		dataPanel.add(maxPercentMissingSpinner, gbc_maxPercentMissingSpinner);
		
		JLabel lblNewLabel_2 = new JLabel("P-value upper cutoff for result filtering");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 4;
		gbc_lblNewLabel_2.gridy = 3;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
        NumberFormatter formatter = new NumberFormatter(new DecimalFormat("0.0000"));
        formatter.setMinimum(0.0d);
        formatter.setMaximum(1.0d);
 		pValueTextField = new JFormattedTextField(formatter);
		GridBagConstraints gbc_pValueTextField = new GridBagConstraints();
		gbc_pValueTextField.insets = new Insets(0, 0, 0, 5);
		gbc_pValueTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_pValueTextField.gridx = 5;
		gbc_pValueTextField.gridy = 3;
		dataPanel.add(pValueTextField, gbc_pValueTextField);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(buttonPanel, BorderLayout.SOUTH);

		JButton btnSave = new JButton(
				MainActionCommands.GENERATE_MODALITY_ANALYSIS_SCRIPT_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.GENERATE_MODALITY_ANALYSIS_SCRIPT_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
		
		loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();		
		if (command.equals(BROWSE_COMMAND))
			selectWorkingDirectory();
		
		if (command.equals(MainActionCommands.SELECT_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName())) {
			
		}
		if (command.equals(MainActionCommands.IMPORT_MODALITY_ANALYSIS_INPUTS_FROM_FILE_COMMAND.getName()))
			importModalityAnalysisInputsFromFile();
		
		if (command.equals(MainActionCommands.CLEAR_MODALITY_ANALYSIS_INPUT_FILES_COMMAND.getName()))
			cleartInputFiles();
		
		if (command.equals(MainActionCommands.GENERATE_MODALITY_ANALYSIS_SCRIPT_COMMAND.getName()))
			generateModalityAnalysisScript();			
	}	
	
	private void generateModalityAnalysisScript() {

		Collection<String>errors = validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
		    		SwingUtilities.getWindowAncestor(this.getContentPane()));
		    return;
		}
		ModalityAnalysisParametersObject parameters = new ModalityAnalysisParametersObject(
				workDirectory, 
				null,
				inputFileListingTable.getMetabCombinerFileInputObjects(), 
				getMaxPercentMissing(), 
				getPvalueCutoff(),
				null);
		ModalityAnalysisScriptGenerator scriptGen = 
				new ModalityAnalysisScriptGenerator(parameters);
		
		scriptGen.createMultiBatchMZRTDistributionModalityAnalysisScript();
		if (Desktop.isDesktopSupported()) {
		    try {
		        Desktop.getDesktop().open(parameters.getProjectDirectory());
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}
	}
	
	private int getMaxPercentMissing() {
		return (int) maxPercentMissingSpinner.getValue();
	}
	
	public double getPvalueCutoff(){
		
		if(pValueTextField.getText().isBlank())
			return 0.0d;
		else
			return Double.parseDouble(pValueTextField.getText());
	}
	
	private Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    if(workDirectory == null || !workDirectory.exists())
	    	errors.add("Work directory not specified or not a valid directory");
	    
	    Collection<RMultibatchAnalysisInputObject> ioList = 
	    		inputFileListingTable.getMetabCombinerFileInputObjects();
	    if(ioList.isEmpty())
	    	errors.add("No input data files for alignment specified");
	    
	    return errors;
	}

	private void cleartInputFiles() {

		if(!inputFileListingTable.getAllFiles(SummaryInputColumns.PEAK_AREAS).isEmpty()) {
			
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to clear input data table?", 
					SwingUtilities.getWindowAncestor(this.getContentPane()));
			if(res == JOptionPane.YES_OPTION)
				inputFileListingTable.clearTable();
		}
	}
	
	private void selectWorkingDirectory() {

		if(workDirectory == null || !workDirectory.exists())
			workDirectory = 
				Paths.get(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).toFile();
		
		JnaFileChooser fc = new JnaFileChooser(workDirectory.getParentFile());
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			workDirectory = fc.getSelectedFile();
			workDirectoryTextField.setText(workDirectory.getPath());
			savePreferences();	
		}
	}
	
	private void importModalityAnalysisInputsFromFile() {
		
		if(workDirectory == null || !workDirectory.exists())
			workDirectory = 
				Paths.get(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).toFile();

		JnaFileChooser fc = new JnaFileChooser(workDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputListFile = fc.getSelectedFile();
			parseInputList(inputListFile);
		}
	}
	
	private void parseInputList(File inputListFile) {

		if(workDirectory == null || !workDirectory.exists()) {
			
			MessageDialog.showErrorMsg(
					"Work directory containing the input files for alignment must be specified first", 
					SwingUtilities.getWindowAncestor(this.getContentPane()));
			return;
		}
		String[][] mcInputData = DelimitedTextParser.parseTextFile(
				inputListFile, MRC2ToolBoxConfiguration.getTabDelimiter());

		List<String>errorList = new ArrayList<>();
		Set<RMultibatchAnalysisInputObject>mcioSet = RAnalysisUtils.createRmultibatchInputSet(
				mcInputData, 
				ModalityAnalysisScriptGenerator.requiredProperties,
				ModalityAnalysisScriptGenerator.selectableProperties,
				workDirectory,
				errorList);		
		
		if(!errorList.isEmpty()) {
		    MessageDialog.showErrorMsg(StringUtils.join(errorList, "\n"), 
		    		SwingUtilities.getWindowAncestor(this.getContentPane()));	
		}
		else {
			inputFileListingTable.setModelFromInputObjects(mcioSet);
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
	    preferences = prefs;
		String baseDirPath = preferences.get(WORK_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			workDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		workDirectoryTextField.setText(workDirectory.getPath());
		maxPercentMissingSpinner.setValue(preferences.getInt(MAX_PERCENT_MISSING, 50));
		pValueTextField.setText(Double.toString(preferences.getDouble(P_VALUE_CUTOFF, 0.001d)));
	}

	@Override
	public void loadPreferences() {
	    loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
	    preferences = Preferences.userRoot().node(PREFS_NODE);
	    preferences.put(WORK_DIRECTORY, workDirectory.getAbsolutePath());
	    preferences.putInt(MAX_PERCENT_MISSING, getMaxPercentMissing());
	    preferences.putDouble(P_VALUE_CUTOFF, getPvalueCutoff());
	}
}
