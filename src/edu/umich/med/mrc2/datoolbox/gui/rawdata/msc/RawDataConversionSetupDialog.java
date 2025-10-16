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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RawDataConversionSetupDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672683664046129485L;
	private static final Icon msConvertIcon = GuiUtils.getIcon("msConvert", 32);
	private Preferences preferences;
	public static final String PREFS_NODE = 
			RawDataConversionSetupDialog.class.getName();
	public static final String SOURCE_FILE_LOCATION = "SOURCE_FILE_LOCATION";
	public static final String OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";

	private RawDataConversionSetupToolbar toolbar;
	private VendorRawDataFileTable fileTable;
	private JTextField outputDirectoryTextField;
	private File sourceFilesLocation;
	private File outputDirectory;
	private JComboBox outputFormatComboBox;

	public RawDataConversionSetupDialog(ActionListener listener) {

		super();
		setTitle("Select vendor data files for conversion");
		setIconImage(((ImageIcon) msConvertIcon).getImage());
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(800, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout(0, 0));
		toolbar = new RawDataConversionSetupToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Save converted files to:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		outputDirectoryTextField = new JTextField();
		outputDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel.add(outputDirectoryTextField, gbc_textField);
		outputDirectoryTextField.setColumns(10);

		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setActionCommand(
				MainActionCommands.SELECT_OUPUT_DIRECTORY_COMMAND.getName());
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 1;
		panel.add(btnNewButton, gbc_btnNewButton);

		fileTable = new VendorRawDataFileTable();
		JScrollPane tableScroll = new JScrollPane(fileTable);
		mainPanel.add(tableScroll, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 10, 5, 10));
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 84, 114, 154, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 23, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0 };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblNewLabel_1 = new JLabel("Output format: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);

		outputFormatComboBox = new JComboBox<MsConvertOutputFormat>(
				new DefaultComboBoxModel<MsConvertOutputFormat>(MsConvertOutputFormat.values()));
		outputFormatComboBox.setSelectedItem(MsConvertOutputFormat.mzML);
		GridBagConstraints gbc_outputFormatComboBox = new GridBagConstraints();
		gbc_outputFormatComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_outputFormatComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFormatComboBox.gridx = 1;
		gbc_outputFormatComboBox.gridy = 0;
		panel_1.add(outputFormatComboBox, gbc_outputFormatComboBox);

		JLabel lblNewLabel_2 = new JLabel("  ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 0;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};

		JButton cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 3;
		gbc_cancelButton.gridy = 0;
		panel_1.add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(al);

		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		JButton closeFilesButton = new JButton(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName());
		closeFilesButton.setActionCommand(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName());
		closeFilesButton.addActionListener(listener);
		GridBagConstraints gbc_closeFilesButton = new GridBagConstraints();
		gbc_closeFilesButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_closeFilesButton.gridx = 4;
		gbc_closeFilesButton.gridy = 0;
		panel_1.add(closeFilesButton, gbc_closeFilesButton);
		JRootPane rootPane = SwingUtilities.getRootPane(closeFilesButton);
		rootPane.setDefaultButton(closeFilesButton);

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

		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			addDataFiles();

		if (command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeSelectedDataFiles();

		if (command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName()))
			fileTable.clearTable();

		if (command.equals(MainActionCommands.SELECT_OUPUT_DIRECTORY_COMMAND.getName()))
			selectOutputDirectory();
	}

	private void addDataFiles() {
		
		JnaFileChooser fc = new JnaFileChooser(sourceFilesLocation);		
		fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
		fc.setTitle("Select raw data files:");
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File[] selectedFiles = fc.getSelectedFiles();
			Collection<File> allFiles = fileTable.getAllFiles();
			TreeSet<File> validFiles = new TreeSet<File>();
			for (File dataFile : selectedFiles) {
				if (isValidVendorFile(dataFile) && !allFiles.contains(dataFile))
					validFiles.add(dataFile);
			}
			if (!validFiles.isEmpty()) {
				fileTable.addDataFiles(validFiles);
			} else {
				MessageDialog.showWarningMsg(
						"No valid source files selected or "
						+ "all selected files already added to queue", this);
			}
			sourceFilesLocation = fc.getCurrentDirectory();
			savePreferences();
		}
	}

	private boolean isValidVendorFile(File dataFile) {

		// Agilent files
		if (dataFile.isDirectory() 
				&& FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("D"))
			return true;

		// SCIEX files
		if (dataFile.isFile() 
				&& FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("WIFF"))
			return true;

		// Thermo files
		if (dataFile.isFile() 
				&& FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("RAW"))
			return true;

		return false;
	}

	private void removeSelectedDataFiles() {

		Collection<File> selectedFiles = fileTable.getSelectedFiles();
		if (selectedFiles.isEmpty())
			return;

		Collection<File> allFiles = fileTable.getAllFiles();
		allFiles.removeAll(selectedFiles);
		fileTable.setModelFromDataFiles(allFiles);
	}

	private void selectOutputDirectory() {

		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = "RAW_DATA_" + timestamp;
		File outDir = new File(MRC2ToolBoxConfiguration.getRawDataRepository());
		if(outputDirectory != null && outputDirectory.exists())
			outDir = outputDirectory;

		JnaFileChooser fc = new JnaFileChooser(outDir);		
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select data analysis method:");
		fc.setMultiSelectionEnabled(false);
		if (outDir.getAbsolutePath().equals(MRC2ToolBoxConfiguration.getRawDataRepository()))
			fc.setDefaultFileName(fileName);
		else
			fc.setDefaultFileName(outputDirectory.getName());
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			outputDirectoryTextField.setText(fc.getSelectedFile().getAbsolutePath());
			outputDirectory = fc.getSelectedFile();
			savePreferences();
		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		sourceFilesLocation = new File(
				preferences.get(SOURCE_FILE_LOCATION, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		outputDirectory = new File(preferences.get(OUTPUT_DIRECTORY, MRC2ToolBoxConfiguration.getRawDataRepository()));

		if (!outputDirectory.exists())
			outputDirectory = new File(MRC2ToolBoxConfiguration.getRawDataRepository());

		if (!outputDirectory.getAbsolutePath().equals(MRC2ToolBoxConfiguration.getRawDataRepository()))
			outputDirectoryTextField.setText(outputDirectory.getAbsolutePath());
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(SOURCE_FILE_LOCATION, sourceFilesLocation.getAbsolutePath());
		preferences.put(OUTPUT_DIRECTORY, outputDirectory.getAbsolutePath());
	}

	public Collection<File> getFiles() {
		return fileTable.getAllFiles();
	}

	public File getOutputFolder() {

		if (outputDirectoryTextField.getText().trim().isEmpty())
			return null;

		return new File(outputDirectoryTextField.getText());
	}
	
	public MsConvertOutputFormat getMsConvertOutputFormat(){
		return (MsConvertOutputFormat)outputFormatComboBox.getSelectedItem();
	}
}



