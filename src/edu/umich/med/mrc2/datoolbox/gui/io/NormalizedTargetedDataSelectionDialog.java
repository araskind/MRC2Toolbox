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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.library.manager.LibrarySelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.CompoundNameMatchingTask;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class NormalizedTargetedDataSelectionDialog extends JDialog 
		implements ActionListener, ItemListener, BackedByPreferences, TaskListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Icon dialogIcon = GuiUtils.getIcon("loess", 32);
	
	private Preferences preferences;
	public static final String FILE_NAME_MASK = "FILE_NAME_MASK";
	private static final String LINES_TO_SKIP = "LINES_TO_SKIP";
	private static final String USE_DATABASE_FOR_MATCHING = "USE_DATABASE_FOR_MATCHING";

	private JTextField inputFileTextField;
	private JTextField fileNameMaskField;
	private JSpinner lineSkipSpinner;
	private JComboBox<String> featureColumnComboBox;
	private JLabel refLibraryNameLabel;
	private LibrarySelectorDialog librarySelectorDialog;
	private JButton btnSave;
	private JRadioButton refLibRadioButton;
	private JRadioButton cpdDatabaseRadioButton;
	private JButton refLibrarySelectButton;
	
	private File inputFile;
	private File baseLibraryDirectory;
	private CompoundLibrary referenceLibrary;

	private Map<String, LibraryMsFeature> nameFeatureMap;
	
	private static final String BROWSE = "BROWSE";

	public NormalizedTargetedDataSelectionDialog(
			ActionListener parserListener, 
			File baseLibraryDirectory, 
			boolean useToverifyCompoundData) {
		super();
		this.baseLibraryDirectory = baseLibraryDirectory;
		setTitle("Select normalized targeted data file");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(700, 300));
		setSize(new Dimension(700, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);
		
		JLabel lblNewLabel = new JLabel("Input file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		dataPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_inputFileTextField = new GridBagConstraints();
		gbc_inputFileTextField.gridwidth = 3;
		gbc_inputFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_inputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileTextField.gridx = 0;
		gbc_inputFileTextField.gridy = 1;
		dataPanel.add(inputFileTextField, gbc_inputFileTextField);
		inputFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setActionCommand(BROWSE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 1;
		dataPanel.add(btnNewButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Ignore");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		dataPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		lineSkipSpinner = new JSpinner();
		lineSkipSpinner.setModel(new SpinnerNumberModel(0, 0, 10, 1));
		GridBagConstraints gbc_lineSkipSpinner = new GridBagConstraints();
		gbc_lineSkipSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_lineSkipSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_lineSkipSpinner.gridx = 1;
		gbc_lineSkipSpinner.gridy = 2;
		dataPanel.add(lineSkipSpinner, gbc_lineSkipSpinner);
		
		JLabel lblNewLabel_2 = new JLabel("lines after header");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 2;
		dataPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("File name mask");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.gridwidth = 2;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 3;
		dataPanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		fileNameMaskField = new JTextField();
		GridBagConstraints gbc_fileNameMaskField = new GridBagConstraints();
		gbc_fileNameMaskField.insets = new Insets(0, 0, 5, 5);
		gbc_fileNameMaskField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileNameMaskField.gridx = 2;
		gbc_fileNameMaskField.gridy = 3;
		dataPanel.add(fileNameMaskField, gbc_fileNameMaskField);
		fileNameMaskField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Feature column");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridwidth = 2;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 4;
		dataPanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		featureColumnComboBox = new JComboBox<>();
		GridBagConstraints gbc_featureColumnComboBox = new GridBagConstraints();
		gbc_featureColumnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_featureColumnComboBox.gridwidth = 2;
		gbc_featureColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureColumnComboBox.gridx = 2;
		gbc_featureColumnComboBox.gridy = 4;
		dataPanel.add(featureColumnComboBox, gbc_featureColumnComboBox);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(
				new TitledBorder(null, "Compound matching settings", 
						TitledBorder.LEADING, TitledBorder.TOP, null, null), 
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		dataPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		ButtonGroup radioGroup = new ButtonGroup();

		refLibRadioButton = new JRadioButton("Use reference library");
		radioGroup.add(refLibRadioButton);
		refLibRadioButton.addItemListener(this);
		GridBagConstraints gbc_refLibRadioButton = new GridBagConstraints();
		gbc_refLibRadioButton.anchor = GridBagConstraints.WEST;
		gbc_refLibRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_refLibRadioButton.gridx = 0;
		gbc_refLibRadioButton.gridy = 0;
		panel.add(refLibRadioButton, gbc_refLibRadioButton);
		
		refLibraryNameLabel = new JLabel("");
		GridBagConstraints gbc_refLibraryNameLabel = new GridBagConstraints();
		gbc_refLibraryNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_refLibraryNameLabel.gridx = 1;
		gbc_refLibraryNameLabel.gridy = 0;
		panel.add(refLibraryNameLabel, gbc_refLibraryNameLabel);
		refLibraryNameLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		refLibrarySelectButton = new JButton(
				MainActionCommands.SELECT_REFERENCE_TARGETED_LIBRARY_COMMAND.getName());
		GridBagConstraints gbc_refLibrarySelectButton = new GridBagConstraints();
		gbc_refLibrarySelectButton.insets = new Insets(0, 0, 5, 0);
		gbc_refLibrarySelectButton.gridx = 2;
		gbc_refLibrarySelectButton.gridy = 0;
		panel.add(refLibrarySelectButton, gbc_refLibrarySelectButton);
		refLibrarySelectButton.setActionCommand(
				MainActionCommands.SELECT_REFERENCE_TARGETED_LIBRARY_COMMAND.getName());
		
		cpdDatabaseRadioButton = new JRadioButton("Use compound database");
		radioGroup.add(cpdDatabaseRadioButton);
		cpdDatabaseRadioButton.addItemListener(this);
		GridBagConstraints gbc_cpdDatabaseRadioButton = new GridBagConstraints();
		gbc_cpdDatabaseRadioButton.gridwidth = 2;
		gbc_cpdDatabaseRadioButton.anchor = GridBagConstraints.WEST;
		gbc_cpdDatabaseRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_cpdDatabaseRadioButton.gridx = 0;
		gbc_cpdDatabaseRadioButton.gridy = 1;
		panel.add(cpdDatabaseRadioButton, gbc_cpdDatabaseRadioButton);
		refLibrarySelectButton.addActionListener(this);

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

		if(useToverifyCompoundData) {
			
			btnSave = new JButton(
					MainActionCommands.VERIFY_COMPOUNDS_IN_TARGETED_DATA_COMMAND.getName());
			btnSave.setActionCommand(
					MainActionCommands.VERIFY_COMPOUNDS_IN_TARGETED_DATA_COMMAND.getName());
			btnSave.addActionListener(this);
		}
		else {
			btnSave = new JButton(
					MainActionCommands.PARSE_NORMALIZED_TARGETED_DATA_COMMAND.getName());
			btnSave.setActionCommand(
					MainActionCommands.PARSE_NORMALIZED_TARGETED_DATA_COMMAND.getName());
			btnSave.addActionListener(parserListener);
		}
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

		if(e.getActionCommand().equals(BROWSE))
			selectNormalizedTargetedDataFile();
		
		if(e.getActionCommand().equals(MainActionCommands.SELECT_REFERENCE_TARGETED_LIBRARY_COMMAND.getName()))
			selectReferenceLibrary();
		
		if(e.getActionCommand().equals(MainActionCommands.SET_REFERENCE_TARGETED_LIBRARY_COMMAND.getName()))
			setReferenceLibrary();
		
		if(e.getActionCommand().equals(MainActionCommands.VERIFY_COMPOUNDS_IN_TARGETED_DATA_COMMAND.getName()))
			verifyCompoundData();
	}
	
	private void verifyCompoundData() {
		
		String message = "";
		if(inputFile == null)
			message = "Input data file must be defined\n";

		if((refLibRadioButton.isSelected() && referenceLibrary == null))
			message += "Reference library must be defined\n";
		
		if(!message.isEmpty()) {
			MessageDialog.showErrorMsg(message, this);
			btnSave.setEnabled(false);
		}
		else {
			btnSave.setEnabled(true);
			String[][] inputDataArray = 
					DelimitedTextParser.parseDataFileBasedOnExtension(inputFile);			
			String featureColumn = getFeatureColumnName();
			if(featureColumn == null || featureColumn.isBlank()) {
				MessageDialog.showErrorMsg("Feature column not specified", this);
				btnSave.setEnabled(false);
			}
			else {
				btnSave.setEnabled(true);
				String[] compoundNames = DataImportUtils.extractNamedColumn(
						inputDataArray, featureColumn, getNumberOfLinesToSkipAfterHeader());
				long badNameCount = Arrays.asList(compoundNames).stream().
						filter(n -> (Objects.isNull(n) || n.isBlank())).count();
				if(badNameCount > 0) {
					MessageDialog.showErrorMsg("Missing names in \"" + featureColumn + "\" column");
					btnSave.setEnabled(false);
				}
				else {
					btnSave.setEnabled(true);
					CompoundNameMatchingTask task = new CompoundNameMatchingTask(
							compoundNames, 
							referenceLibrary,
							false,
							null);
					task.addTaskListener(this);
					MRC2ToolBoxCore.getTaskController().addTask(task);
				}
			}			
		}		
	}

	private void selectReferenceLibrary() {

		librarySelectorDialog = new LibrarySelectorDialog(this);
		librarySelectorDialog.setLocationRelativeTo(this);
		librarySelectorDialog.setVisible(true);
	}
	
	private void setReferenceLibrary() {
		
		referenceLibrary = librarySelectorDialog.getSelectedLibrary();
		refLibraryNameLabel.setText(referenceLibrary.getLibraryName());
		librarySelectorDialog.dispose();
	}

	private void selectNormalizedTargetedDataFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseLibraryDirectory);
		fc.setTitle("Select normalized targeted data file (TAB-separated)");
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files (TAB-separated)", "txt", "TXT", "tsv", "TSV");
		fc.addFilter("CSV files (Comma-separated)", "csv", "CSV");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			inputFile = fc.getSelectedFile();
			inputFileTextField.setText(inputFile.getAbsolutePath());
			populateColumnSelector();
			verifyCompoundData();
		}
	}

	private void populateColumnSelector() {
		
		String[][] inputDataArray = 
				DelimitedTextParser.parseDataFileBasedOnExtension(inputFile);
		featureColumnComboBox.setModel(new DefaultComboBoxModel<>(inputDataArray[0]));
		featureColumnComboBox.setSelectedIndex(0);
	}

	public File getInputFile() {
		return inputFile;
	}
	
	public String getFileNameMask() {
		return fileNameMaskField.getText().trim();
	}
	
	public int getNumberOfLinesToSkipAfterHeader() {
		return (int)lineSkipSpinner.getValue();
	}
	
	public String getFeatureColumnName() {
		return (String)featureColumnComboBox.getSelectedItem();
	}
	
	public CompoundLibrary getReferenceLibrary() {
		return referenceLibrary;
	}
	
	public Collection<String>validateFormData(){
	    
	    Collection<String>errors = new ArrayList<>();
	    if(inputFile == null || !inputFile.exists())
	    	errors.add("Input file not specified or invalid");
	    
	    if(getFileNameMask().isEmpty())
	    	errors.add("File name mask not specified");
	    else {
	    	if(!TextUtils.isValidRegex(getFileNameMask()))
	    		errors.add("File name mask is not a valid regular expression");
	    }	
	    if(getFeatureColumnName() == null || getFeatureColumnName().isBlank())
	    	errors.add("Feature column not specified");
	    
	    if(refLibRadioButton.isSelected() && referenceLibrary == null)
	    	errors.add("Reference library not specified");
	    
	    return errors;
	} 

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String fileNameMask = preferences.get(FILE_NAME_MASK, 
						MRC2ToolBoxConfiguration.CORE_DATA_FILE_MASK_DEFAULT);
		if(fileNameMask == null || fileNameMask.isBlank())
			fileNameMask = MRC2ToolBoxConfiguration.CORE_DATA_FILE_MASK_DEFAULT;
			
		fileNameMaskField.setText(fileNameMask);
		lineSkipSpinner.setValue(preferences.getInt(LINES_TO_SKIP, 1));
		
		boolean useDbForMatching = preferences.getBoolean(USE_DATABASE_FOR_MATCHING, Boolean.TRUE);
		cpdDatabaseRadioButton.setSelected(useDbForMatching);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(FILE_NAME_MASK, getFileNameMask());
		preferences.putInt(LINES_TO_SKIP, getNumberOfLinesToSkipAfterHeader());
		preferences.putBoolean(USE_DATABASE_FOR_MATCHING, cpdDatabaseRadioButton.isSelected());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getSource().equals(refLibRadioButton) && e.getStateChange() == ItemEvent.SELECTED)
			refLibrarySelectButton.setEnabled(true);
		
		if(e.getSource().equals(cpdDatabaseRadioButton) && e.getStateChange() == ItemEvent.SELECTED)
			refLibrarySelectButton.setEnabled(false);		
	}

	@Override
	public void statusChanged(TaskEvent e) {

	    if (e.getStatus() == TaskStatus.FINISHED) {

	        ((AbstractTask)e.getSource()).removeTaskListener(this);

	        if (e.getSource().getClass().equals(CompoundNameMatchingTask.class))
	        	finalizeCompoundNameMatchingTask((CompoundNameMatchingTask)e.getSource());
	    }		
	}

	private void finalizeCompoundNameMatchingTask(CompoundNameMatchingTask task) {

		List<String> compoundErrors = task.getErrors();
		if(!compoundErrors.isEmpty()) {
			
			btnSave.setEnabled(false);
			String message = compoundErrors.remove(0);
			String details = StringUtils.join(compoundErrors, "\n");
			InformationDialog infoDialog = new InformationDialog(
					"Unmatched compounds", 
					message, 
					details);
			infoDialog.setLocationRelativeTo(this);
			infoDialog.setVisible(true);
		}
		else {
			MessageDialog.showInfoMsg("Data successfully verified", this);
			nameFeatureMap = task.getNameFeatureMap();
			btnSave.setEnabled(true);
		}
	}

	public Map<String, LibraryMsFeature> getNameFeatureMap() {
		return nameFeatureMap;
	}
}
