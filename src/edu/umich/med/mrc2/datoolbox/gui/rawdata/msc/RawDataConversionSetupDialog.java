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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RawDataConversionSetupDialog extends JDialog 
	implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672683664046129485L;
	private static final Icon msConvertIcon = GuiUtils.getIcon("msConvert", 32);
	private Preferences preferences;
	public static final String PREFS_NODE = RawDataConversionSetupDialog.class.getName();
	public static final String SOURCE_FILE_LOCATION = "SOURCE_FILE_LOCATION"; 
	public static final String OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY"; 
	
	private RawDataConversionSetupToolbar toolbar;
	private VendorRawDataFileTable fileTable;
	private JTextField outputDirectoryTextField;
	private File sourceFilesLocation;
	private File outputDirectory;
	private ImprovedFileChooser chooser;

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
		
		JPanel mainPanel = new JPanel(new BorderLayout(0,0));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Save converted files to");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		outputDirectoryTextField = new JTextField();
		outputDirectoryTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(outputDirectoryTextField, gbc_textField);
		outputDirectoryTextField.setColumns(10);
		
		fileTable = new VendorRawDataFileTable();
		JScrollPane tableScroll = new JScrollPane(fileTable);
		mainPanel.add(tableScroll, BorderLayout.CENTER);
				
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		JButton closeFilesButton = new JButton(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName());
		closeFilesButton.setActionCommand(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName());
		closeFilesButton.addActionListener(listener);
		panel_1.add(closeFilesButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(closeFilesButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(closeFilesButton);

		loadPreferences();
		initChooser();
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();		
		super.dispose();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.setAcceptAllFileFilterUsed(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			addDataFiles();
		
		if(command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeSelectedDataFiles();
		
		if(command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName()))
			fileTable.clearTable();
		
		if(command.equals(MainActionCommands.SELECT_OUPUT_DIRECTORY_COMMAND.getName())) 
			selectOutputDirectory();		
	}

	private void addDataFiles() {

		chooser.setCurrentDirectory(sourceFilesLocation);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		if(chooser.showDialog(this, "Select raw data files") == JFileChooser.APPROVE_OPTION) {
			
			File[] selectedFiles = chooser.getSelectedFiles();
			Collection<File> allFiles = fileTable.getAllFiles();
			TreeSet<File>validFiles = new TreeSet<File>();
			for(File dataFile : selectedFiles) {
				if(isValidVendorFile(dataFile) && !allFiles.contains(dataFile))
					validFiles.add(dataFile);
			}
			if(!validFiles.isEmpty()) {
				fileTable.addDataFiles(validFiles);
			}
			else {
				MessageDialog.showWarningMsg(
						"No valid source files selected or all selected files already added to queue", this);				
			}
			sourceFilesLocation = chooser.getCurrentDirectory();
			savePreferences();			
		}
//		PlatformImpl.startup(() -> {
//		    FileChooser d = new FileChooser();
//		    d.setInitialDirectory(outputDirectory);
//		    d.showSaveDialog(null);
//		});
	}
	
	private boolean isValidVendorFile(File dataFile) {

		//	Agilent files
		if(dataFile.isDirectory() && FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("D"))
			return true;
		
		// SCIEX files
		if(dataFile.isFile() && FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("WIFF"))
			return true;
		
		// Thermo files
		if(dataFile.isFile() && FilenameUtils.getExtension(dataFile.getName()).toUpperCase().equals("RAW"))
			return true;

		return false;
	}

	private void removeSelectedDataFiles() {
		
		Collection<File> selectedFiles = fileTable.getSelectedFiles();
		if(selectedFiles.isEmpty())
			return;
		
		Collection<File> allFiles = fileTable.getAllFiles();
		allFiles.removeAll(selectedFiles);
		fileTable.setModelFromDataFiles(allFiles);
	}

	private void selectOutputDirectory() {
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = "RAW_DATA_" + timestamp;
				
		chooser.setCurrentDirectory(outputDirectory);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		if(outputDirectory.getAbsolutePath().equals(MRC2ToolBoxConfiguration.getRawDataRepository()))
			chooser.setSelectedFile(new File(fileName));
		else
			chooser.setSelectedFile(outputDirectory);
			
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		if(chooser.showDialog(this, "Set output directory") == JFileChooser.APPROVE_OPTION) {
			outputDirectoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
			outputDirectory = chooser.getSelectedFile();
			savePreferences();
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		sourceFilesLocation =  
				new File(preferences.get(SOURCE_FILE_LOCATION, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		outputDirectory =  
				new File(preferences.get(OUTPUT_DIRECTORY, 
						MRC2ToolBoxConfiguration.getRawDataRepository()));
		
		if(!outputDirectory.exists())
			outputDirectory = new File(MRC2ToolBoxConfiguration.getRawDataRepository());
		
		if(!outputDirectory.getAbsolutePath().equals(MRC2ToolBoxConfiguration.getRawDataRepository()))
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
	
	public Collection<File> getFiles(){ 
		return fileTable.getAllFiles();	
	}
	
	public File getOutputFolder() {
		
		if(outputDirectoryTextField.getText().trim().isEmpty())
			return null;
		
		return new File(outputDirectoryTextField.getText());
	}
}




















