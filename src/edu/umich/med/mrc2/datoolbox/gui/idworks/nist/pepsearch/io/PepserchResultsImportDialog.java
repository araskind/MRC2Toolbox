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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.io;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsfdr.NISTPepSearchResultManipulator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMsPepSearchDataUploadTask;

public class PepserchResultsImportDialog extends JDialog 
		implements ActionListener, BackedByPreferences, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900043273522577847L;
	private static final Icon dialogIcon = GuiUtils.getIcon("NISTMS-pep-upload", 32);
	private Preferences preferences;
	private ImprovedFileChooser inputMsMsFileChooser;
	private File inputFileDirectory;
	private int skippedIdCount = 0;
	private NISTPepSearchParameterObject pepSearchParameterObject;
	
	//	Input source
	public static final String BASE_DIR = "BASE_DIR";
	private JTextField inputFileTextField;
	private File inputFile;
	private JSpinner maxHitsPerFeatureSpinner;
	private JTextArea textArea;
	
	private boolean resultsValid = false;
	
	private Map<String, ReferenceMsMsLibrary>refLibMap;
	private PepSearchSetupDialog pepSearchSetupDialog;
	private JCheckBox addMissingParamsCheckBox;
	
	
	public PepserchResultsImportDialog() {
		super();
		setTitle("Verify PepSearch results for upload");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(1000, 640));
		setPreferredSize(new Dimension(1000, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);		
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 59, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Select PepSearch output file:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 6;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(inputFileTextField, gbc_textField);
		inputFileTextField.setColumns(10);
		
		JLabel topHitLabel = new JLabel("Import only ");
		GridBagConstraints gbc_topHitLabel = new GridBagConstraints();
		gbc_topHitLabel.anchor = GridBagConstraints.EAST;
		gbc_topHitLabel.insets = new Insets(0, 0, 0, 5);
		gbc_topHitLabel.gridx = 0;
		gbc_topHitLabel.gridy = 2;
		panel.add(topHitLabel, gbc_topHitLabel);
		
		JButton btnNewButton = new JButton("Browse ...");
		btnNewButton.setActionCommand(MainActionCommands.SELECT_PEPSEARCH_OUTPUT_FILE_COMMAND.getName());
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 6;
		gbc_btnNewButton.gridy = 1;
		panel.add(btnNewButton, gbc_btnNewButton);
		
		maxHitsPerFeatureSpinner = new JSpinner();
		maxHitsPerFeatureSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		maxHitsPerFeatureSpinner.setSize(new Dimension(50, 20));
		maxHitsPerFeatureSpinner.setMinimumSize(new Dimension(50, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 2;
		panel.add(maxHitsPerFeatureSpinner, gbc_spinner);
		
		JLabel lblNewLabel_1 = new JLabel(" best hits per MSMS feature");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 2;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		textArea = new JTextArea();
		JScrollPane logScroll = new JScrollPane(textArea);
		logScroll.setBorder(new CompoundBorder(
				new EmptyBorder(10, 10, 10, 10), 
				new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Validation output", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		getContentPane().add(logScroll, BorderLayout.CENTER);
		
		pepSearchSetupDialog = new PepSearchSetupDialog();
		JPanel paramsPanel = new JPanel(new BorderLayout(0,0));
		JPanel pepSearchSettingsPanel = pepSearchSetupDialog.createSearchOptionsPanel(true);
		paramsPanel.add(pepSearchSettingsPanel, BorderLayout.CENTER);
		getContentPane().add(paramsPanel, BorderLayout.EAST);
		
		addMissingParamsCheckBox = new JCheckBox("Add missing parameters for existing hits when uploading results");
		paramsPanel.add(addMissingParamsCheckBox, BorderLayout.SOUTH);
		pepSearchSetupDialog.clearSearchOptionsPanel();
		pepSearchSetupDialog.lockSearchOptionsPanelForEditing();
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		JButton validateButton = new JButton(
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName());
		buttonPanel.add(validateButton);
		validateButton.setActionCommand(
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName());
		validateButton.addActionListener(this);
		JRootPane rootPane = SwingUtilities.getRootPane(validateButton);
		rootPane.setDefaultButton(validateButton);
		
		JButton uploadButton = new JButton(
				MainActionCommands.UPLOAD_PEPSEARCH_RESULTS_COMMAND.getName());
		uploadButton.setActionCommand(
				MainActionCommands.UPLOAD_PEPSEARCH_RESULTS_COMMAND.getName());
		uploadButton.addActionListener(this);		
		buttonPanel.add(uploadButton);
		
		loadPreferences();
		initFileChooser();
		inputFile = null;
		pack();
	}
	
	private void initFileChooser() {

		inputMsMsFileChooser = new ImprovedFileChooser();
		inputMsMsFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		inputMsMsFileChooser.addActionListener(this);
		inputMsMsFileChooser.setAcceptAllFileFilterUsed(false);
		inputMsMsFileChooser.setMultiSelectionEnabled(false);
		inputMsMsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		inputMsMsFileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		inputMsMsFileChooser.setCurrentDirectory(inputFileDirectory);

		FileNameExtensionFilter txtFilter =
			new FileNameExtensionFilter("Pepsearch output files", "txt", "TXT");
		inputMsMsFileChooser.addChoosableFileFilter(txtFilter);
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.SELECT_PEPSEARCH_OUTPUT_FILE_COMMAND.getName())) {

			if(inputMsMsFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				inputFileTextField.setText(inputMsMsFileChooser.getSelectedFile().getAbsolutePath());
				inputFileDirectory = inputMsMsFileChooser.getCurrentDirectory();
				inputFile = inputMsMsFileChooser.getSelectedFile();
			}
		}
		if(command.equals(MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName())) 
			startResultValidationAndUpload(true);
		
		if(command.equals(MainActionCommands.UPLOAD_PEPSEARCH_RESULTS_COMMAND.getName())) 
			startResultValidationAndUpload(false);
	}	

	private void startResultValidationAndUpload(boolean validateOnly) {
		
		textArea.setText("");
		String command = getPepSearchCommand();
		if(command == null || command.isEmpty()) {
			MessageDialog.showErrorMsg("Empty or missing search command line.", this);
			return;
		}		
		pepSearchParameterObject = 
				NISTPepSearchResultManipulator.parsePepSearchCommandLine(command);
		pepSearchSetupDialog.clearSearchOptionsPanel();
		if(pepSearchParameterObject != null)
			pepSearchSetupDialog.loadSearchParameters(pepSearchParameterObject);
		
		NISTMsPepSearchDataUploadTask task = new NISTMsPepSearchDataUploadTask(
				inputFile, 
				pepSearchParameterObject, 
				validateOnly, 
				addMissingParamsCheckBox.isSelected(),
				getMaxHitsPerMSMSFeature());
		task.setSearchCommand(command);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private String getPepSearchCommand() {
		
	      String line = "";
	      try (Stream<String> lines = Files.lines(Paths.get(inputFile.getAbsolutePath()))) {
	        line = lines.skip(1).findFirst().get();
	      }
	      catch(IOException e){
	        e.printStackTrace();
	      }
	      return line;
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		inputFileDirectory =
			new File(preferences.get(BASE_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
			getAbsoluteFile();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, inputFileDirectory.getAbsolutePath());
		
	}
	
	public boolean resultsValidated() {
		return resultsValid;
	}
	
	public int getMaxHitsPerMSMSFeature() {
		return (int)maxHitsPerFeatureSpinner.getValue();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(NISTMsPepSearchDataUploadTask.class)) {
				
				NISTMsPepSearchDataUploadTask task = (NISTMsPepSearchDataUploadTask)e.getSource();				
				File log = task.getLogFile();
				List<String>lines = new ArrayList<String>();
				try {
					lines = Files.readAllLines(Paths.get(log.getAbsolutePath()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for(String line : lines)
					textArea.append(line + "\n");				
			}
		}
	}
}
