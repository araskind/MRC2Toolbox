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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.ReferenceMS1ImportTask;

public class IDtrackerAverageMsOneImportDialog extends JDialog
		implements ActionListener, BackedByPreferences, TaskListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -953337134925770643L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.IDtrackerAverageMsOneImportDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private static final Icon loadAvgMS1DataFileIcon = GuiUtils.getIcon("importTextfile", 32);

	private LIMSExperiment experiment;
	private JButton uploadDataButton;
	private File baseDirectory;
//	private JFileChooser chooser;
//	private FileNameExtensionFilter xmlFilter;
	private Collection<String> importLog;
	private JTextField inputFileTextField;
	private JLabel experimentNameLabel;
	private JComboBox sampleComboBox;
	private JComboBox acquisitionMethodComboBox;
	private JButton selectFileButton;
	private JTextField daMethodTextField;
	private DataExtractionMethod dataExtractionMethod;
	private JButton daSelectButton;
	private DataAnalysisMethodSelectionDialog daMethodSelectionDialog;
	private File inputDataFile;

	@SuppressWarnings("unchecked")
	public IDtrackerAverageMsOneImportDialog(LIMSExperiment experiment) {

		super();
		this.experiment = experiment;

		setTitle("Import reference MS1 data for experiment");
		setIconImage(((ImageIcon) loadAvgMS1DataFileIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 311, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblExperiment = new JLabel("Experiment");
		lblExperiment.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExperiment = new GridBagConstraints();
		gbc_lblExperiment.anchor = GridBagConstraints.EAST;
		gbc_lblExperiment.insets = new Insets(0, 0, 5, 5);
		gbc_lblExperiment.gridx = 0;
		gbc_lblExperiment.gridy = 0;
		panel.add(lblExperiment, gbc_lblExperiment);

		experimentNameLabel = new JLabel(experiment.getName() + " (" + experiment.getId() + ")");
		GridBagConstraints gbc_experimentNameLabel = new GridBagConstraints();
		gbc_experimentNameLabel.gridwidth = 2;
		gbc_experimentNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_experimentNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_experimentNameLabel.gridx = 1;
		gbc_experimentNameLabel.gridy = 0;
		panel.add(experimentNameLabel, gbc_experimentNameLabel);

		JLabel lblSample = new JLabel("Sample");
		lblSample.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.insets = new Insets(0, 0, 5, 5);
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 1;
		panel.add(lblSample, gbc_lblSample);

		sampleComboBox = new JComboBox(
				new SortedComboBoxModel<ExperimentalSample>(
						experiment.getExperimentDesign().getSamples()));
		GridBagConstraints gbc_sampleComboBox = new GridBagConstraints();
		gbc_sampleComboBox.gridwidth = 2;
		gbc_sampleComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_sampleComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleComboBox.gridx = 1;
		gbc_sampleComboBox.gridy = 1;
		panel.add(sampleComboBox, gbc_sampleComboBox);

		JLabel lblAcquisitionMethod = new JLabel("Acquisition method");
		lblAcquisitionMethod.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblAcquisitionMethod = new GridBagConstraints();
		gbc_lblAcquisitionMethod.anchor = GridBagConstraints.EAST;
		gbc_lblAcquisitionMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblAcquisitionMethod.gridx = 0;
		gbc_lblAcquisitionMethod.gridy = 2;
		panel.add(lblAcquisitionMethod, gbc_lblAcquisitionMethod);

		acquisitionMethodComboBox = new JComboBox(
				new SortedComboBoxModel<DataAcquisitionMethod>(
						IDTDataCache.getAcquisitionMethodsForExperiment(experiment)));
		acquisitionMethodComboBox.setSelectedIndex(-1);

		GridBagConstraints gbc_acquisitionMethodComboBox = new GridBagConstraints();
		gbc_acquisitionMethodComboBox.gridwidth = 2;
		gbc_acquisitionMethodComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_acquisitionMethodComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_acquisitionMethodComboBox.gridx = 1;
		gbc_acquisitionMethodComboBox.gridy = 2;
		panel.add(acquisitionMethodComboBox, gbc_acquisitionMethodComboBox);

		JLabel lblDataAnalysisMethod = new JLabel("Data analysis method");
		lblDataAnalysisMethod.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDataAnalysisMethod = new GridBagConstraints();
		gbc_lblDataAnalysisMethod.anchor = GridBagConstraints.EAST;
		gbc_lblDataAnalysisMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblDataAnalysisMethod.gridx = 0;
		gbc_lblDataAnalysisMethod.gridy = 3;
		panel.add(lblDataAnalysisMethod, gbc_lblDataAnalysisMethod);

		daMethodTextField = new JTextField();
		daMethodTextField.setEditable(false);
		GridBagConstraints gbc_datextField = new GridBagConstraints();
		gbc_datextField.insets = new Insets(0, 0, 5, 5);
		gbc_datextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_datextField.gridx = 1;
		gbc_datextField.gridy = 3;
		panel.add(daMethodTextField, gbc_datextField);
		daMethodTextField.setColumns(10);

		daSelectButton = new JButton("Select DA method");
		daSelectButton.setActionCommand(MainActionCommands.SELECT_DA_METHOD_DIALOG_COMMAND.getName());
		daSelectButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 3;
		panel.add(daSelectButton, gbc_btnNewButton);

		JLabel lblDataFile = new JLabel("Data file");
		lblDataFile.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDataFile = new GridBagConstraints();
		gbc_lblDataFile.anchor = GridBagConstraints.EAST;
		gbc_lblDataFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblDataFile.gridx = 0;
		gbc_lblDataFile.gridy = 4;
		panel.add(lblDataFile, gbc_lblDataFile);

		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 4;
		panel.add(inputFileTextField, gbc_textField);
		inputFileTextField.setColumns(10);

		selectFileButton = new JButton(
				MainActionCommands.SELECT_REF_MS1_DATA_FILE_COMMAND.getName());
		selectFileButton.setActionCommand(
				MainActionCommands.SELECT_REF_MS1_DATA_FILE_COMMAND.getName());
		selectFileButton.addActionListener(this);
		GridBagConstraints gbc_selectFileButton = new GridBagConstraints();
		gbc_selectFileButton.gridwidth = 2;
		gbc_selectFileButton.anchor = GridBagConstraints.EAST;
		gbc_selectFileButton.gridx = 1;
		gbc_selectFileButton.gridy = 5;
		panel.add(selectFileButton, gbc_selectFileButton);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		uploadDataButton = new JButton(MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName());
		uploadDataButton.setActionCommand(MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName());
		uploadDataButton.addActionListener(this);
		panel_1.add(uploadDataButton);

		JRootPane rootPane = SwingUtilities.getRootPane(uploadDataButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(uploadDataButton);

		loadPreferences();

//		chooser = new ImprovedFileChooser();
//		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
//		chooser.addActionListener(this);
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setCurrentDirectory(baseDirectory);
//		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "cef", "CEF");
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileFilter(xmlFilter);

		importLog = new ArrayList<String>();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SELECT_REF_MS1_DATA_FILE_COMMAND.getName()))
			selectMS1DataFile();

		if(e.getActionCommand().equals(MainActionCommands.SELECT_DA_METHOD_DIALOG_COMMAND.getName()))
			showDaMethodSelector();

		if(e.getActionCommand().equals(MainActionCommands.SELECT_DA_METHOD_COMMAND.getName()))
			selectDaMethod();

		if(e.getActionCommand().equals(MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName()))
			uploadAverageMs1Data();
	}
	
	private void selectMS1DataFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select reference MS1 CEF file");
		fc.setMultiSelectionEnabled(false);
		fc.setOpenButtonText("Select");
		if (fc.showOpenDialog(this)) {
			
			inputDataFile = fc.getSelectedFile();
			inputFileTextField.setText(inputDataFile.getAbsolutePath());
			baseDirectory = inputDataFile.getParentFile();
			savePreferences();
		}
	}

	private void uploadAverageMs1Data() {

		ArrayList<String>errors = new ArrayList<String>();
		DataAcquisitionMethod acquisitionMethod = (DataAcquisitionMethod) acquisitionMethodComboBox.getSelectedItem();
		ExperimentalSample selectedSample = (ExperimentalSample) sampleComboBox.getSelectedItem();
		if(acquisitionMethod == null)
			errors.add("Data acquisition method has to be specified.");

		if(dataExtractionMethod == null)
			errors.add("Data analysis method has to be specified.");

		if(inputDataFile == null)
			errors.add("Input data file has to be specified.");

		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		DataFile inputFile = new DataFile(inputDataFile.getName());
		inputFile.setFullPath(inputDataFile.getAbsolutePath());
		ReferenceMS1ImportTask task = new ReferenceMS1ImportTask(
				inputFile,
				acquisitionMethod,
				dataExtractionMethod,
				experiment,
				selectedSample);

		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void showDaMethodSelector() {
		daMethodSelectionDialog = new DataAnalysisMethodSelectionDialog(this);
		daMethodSelectionDialog.setLocationRelativeTo(this);
		daMethodSelectionDialog.setVisible(true);
	}

	private void selectDaMethod() {

		dataExtractionMethod = daMethodSelectionDialog.getSelectedMethod();
		if(dataExtractionMethod == null)
			return;

		daMethodTextField.setText(dataExtractionMethod.getName());
		daMethodSelectionDialog.dispose();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(ReferenceMS1ImportTask.class)) 
				finalizeReferenceMS1ImportTask((ReferenceMS1ImportTask)e.getSource());
		}
	}
	
	private synchronized void finalizeReferenceMS1ImportTask(ReferenceMS1ImportTask task) {
		
		//	Write error log
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		Path outputPath = Paths.get(baseDirectory.getAbsolutePath(),
						"REFERENCE_MS1DATA_IMPORT_LOG_" + timestamp + ".TXT");
		
		if(!importLog.isEmpty()) {

		    try {
				Files.write(outputPath, 
						importLog, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		MessageDialog.showInfoMsg("Data import completed.\n"
				+ "Error log saved to " + outputPath.toString(), this);
		dispose();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
