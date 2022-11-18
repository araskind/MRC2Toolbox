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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableInputFilesPanel extends DefaultSingleCDockable 
		implements ActionListener, BackedByPreferences {

	private JTextField positiveModeFolderTextField;
	private JTextField negativeModeFolderTextField;
	private File positiveModeBaseDirectory;
	private File negativeModeBaseDirectory;
	private Collection<File>recentFiles;
	private RecentFilesDialog recentFilesDialog;
	
	public static final String POSITIVE_MODE_BASE_DIR = "positiveModeBaseDirectory";
	public static final String NEGATIVE_MODE_BASE_DIR = "negativeModeBaseDirectory";
	public static final String POSITIVE_MODE_SOURCE_DIR = "positiveModeInputFilesDirectory";
	public static final String NEGATIVE_MODE_SOURCE_DIR = "negativeModeInputFilesDirectory";
	public static final String RECENT_DATA_FOLDERS = "recentDataFolders";
	
	private static final Icon componentIcon = GuiUtils.getIcon("database", 16);

	public DockableInputFilesPanel() {

		super("DockableInputFilesPanel", componentIcon, "Input files", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("POS mode");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);

		positiveModeFolderTextField = new JTextField();
		GridBagConstraints gbc_txtSelectDataFile = new GridBagConstraints();
		gbc_txtSelectDataFile.insets = new Insets(0, 0, 5, 0);
		gbc_txtSelectDataFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSelectDataFile.gridx = 1;
		gbc_txtSelectDataFile.gridy = 1;
		add(positiveModeFolderTextField, gbc_txtSelectDataFile);
		positiveModeFolderTextField.setColumns(10);

		JButton recentPosFoldersButton = new JButton("Recent ...");
		recentPosFoldersButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_POSITIVE_MODE_RAW_DATA_FOLDER.getName());
		recentPosFoldersButton.addActionListener(this);
		GridBagConstraints gbc_recentPosFoldersButton = new GridBagConstraints();
		gbc_recentPosFoldersButton.insets = new Insets(0, 0, 5, 0);
		gbc_recentPosFoldersButton.gridx = 2;
		gbc_recentPosFoldersButton.gridy = 1;
		add(recentPosFoldersButton, gbc_recentPosFoldersButton);
		
		JButton browseForPosFolderButton = new JButton("Browse...");
		browseForPosFolderButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER.getName());
		browseForPosFolderButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 1;
		add(browseForPosFolderButton, gbc_btnNewButton);

		JLabel lblExperimentDesign = new JLabel("NEG mode");
		GridBagConstraints gbc_lblExperimentDesign = new GridBagConstraints();
		gbc_lblExperimentDesign.anchor = GridBagConstraints.WEST;
		gbc_lblExperimentDesign.insets = new Insets(0, 0, 5, 0);
		gbc_lblExperimentDesign.gridx = 0;
		gbc_lblExperimentDesign.gridy = 2;
		add(lblExperimentDesign, gbc_lblExperimentDesign);

		negativeModeFolderTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 2;
		add(negativeModeFolderTextField, gbc_textField);
		negativeModeFolderTextField.setColumns(10);

		JButton recentNegFoldersButton = new JButton("Recent ...");
		recentNegFoldersButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_RAW_DATA_FOLDER.getName());
		recentNegFoldersButton.addActionListener(this);
		GridBagConstraints gbc_recentNegFoldersButton = new GridBagConstraints();
		gbc_recentNegFoldersButton.insets = new Insets(0, 0, 5, 0);
		gbc_recentNegFoldersButton.gridx = 2;
		gbc_recentNegFoldersButton.gridy = 2;
		add(recentNegFoldersButton, gbc_recentNegFoldersButton);
		
		JButton browseForNegFolderButton = new JButton("Browse...");
		browseForNegFolderButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER.getName());
		browseForNegFolderButton.addActionListener(this);
		GridBagConstraints gbc_negModeButton = new GridBagConstraints();
		gbc_negModeButton.insets = new Insets(0, 0, 5, 0);
		gbc_negModeButton.gridx = 3;
		gbc_negModeButton.gridy = 2;
		add(browseForNegFolderButton, gbc_negModeButton);

		recentFiles = new TreeSet<File>();
		loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER.getName()) ||
				command.equals(MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER.getName()))
			selectRawDataFolder(command);
		
		if(command.equals(MainActionCommands.SELECT_RECENT_POSITIVE_MODE_RAW_DATA_FOLDER.getName()) ||
				command.equals(MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_RAW_DATA_FOLDER.getName()))
			selectRecentRawDataFolder(command);
		
		if(command.equals(MainActionCommands.SET_NEGATIVE_MODE_RAW_DATA_FOLDER.getName()) || 				
				command.equals(MainActionCommands.SET_POSITIVE_MODE_RAW_DATA_FOLDER.getName())) 
			setRecentDataFolder(command);
	}
	
	private void setRecentDataFolder(String command) {
		
		File dataFolder = recentFilesDialog.getSelectedFile();
		if(dataFolder == null)
			return;
		
		if (command.equals(MainActionCommands.SET_POSITIVE_MODE_RAW_DATA_FOLDER.getName())) {
			positiveModeFolderTextField.setText(dataFolder.getAbsolutePath());
			positiveModeBaseDirectory = dataFolder.getParentFile();
		}
		if (command.equals(MainActionCommands.SET_NEGATIVE_MODE_RAW_DATA_FOLDER.getName())) {
			negativeModeFolderTextField.setText(dataFolder.getAbsolutePath());
			negativeModeBaseDirectory = dataFolder.getParentFile();
		}	
		recentFiles.add(dataFolder);		
		savePreferences();
		recentFilesDialog.dispose();
	}

	private void selectRecentRawDataFolder(String command) {
		
		String selectCommand = null;
		if(command.equals(MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_RAW_DATA_FOLDER.getName()))
				selectCommand = MainActionCommands.SET_NEGATIVE_MODE_RAW_DATA_FOLDER.getName();
		
		if(command.equals(MainActionCommands.SELECT_RECENT_POSITIVE_MODE_RAW_DATA_FOLDER.getName()))
				selectCommand = MainActionCommands.SET_POSITIVE_MODE_RAW_DATA_FOLDER.getName();
		
		recentFilesDialog = new RecentFilesDialog(command, recentFiles, this, selectCommand);
		recentFilesDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		recentFilesDialog.setVisible(true);
	}
	
	private void selectRawDataFolder(String command) {
		
		JnaFileChooser fc = new JnaFileChooser();
		if (command.equals(MainActionCommands.BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER.getName()))
			fc.setCurrentDirectory(positiveModeBaseDirectory.getAbsolutePath());
		
		if (command.equals(MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER.getName()))
			fc.setCurrentDirectory(negativeModeBaseDirectory.getAbsolutePath());
		
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select folder containing data files:");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File dataFolder = fc.getSelectedFile();
			if (command.equals(MainActionCommands.BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER.getName())) {
				positiveModeFolderTextField.setText(dataFolder.getAbsolutePath());
				positiveModeBaseDirectory = dataFolder.getParentFile();
			}
			if (command.equals(MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER.getName())) {
				negativeModeFolderTextField.setText(dataFolder.getAbsolutePath());
				negativeModeBaseDirectory = dataFolder.getParentFile();
			}
			recentFiles.add(dataFolder);
			savePreferences();
		}
	}

	public File getNegativeDataFolder() {
		return new File(negativeModeFolderTextField.getText());
	}

	public File getPositiveDataFolder() {
		return new File(positiveModeFolderTextField.getText());
	}
	
	public void setNegativeDataFolder(String pathToNegativeDataFolder) {
		negativeModeFolderTextField.setText(pathToNegativeDataFolder);
	}

	public void setPositiveDataFolder(String pathToPositiveDataFolder) {
		positiveModeFolderTextField.setText(pathToPositiveDataFolder);
	}

	@Override
	public void loadPreferences(Preferences preferences) {
				
		positiveModeBaseDirectory = new File(preferences.get(POSITIVE_MODE_BASE_DIR, "."));
		if(!positiveModeBaseDirectory.exists())
			positiveModeBaseDirectory = new File(".");
		
		negativeModeBaseDirectory = new File(preferences.get(NEGATIVE_MODE_BASE_DIR, "."));
		if(!negativeModeBaseDirectory.exists())
			negativeModeBaseDirectory = new File(".");
		
		String recentDataFolderString = preferences.get(RECENT_DATA_FOLDERS, "");
		if(!recentDataFolderString.isEmpty()) {
			
			String[] dataFoldersLocations = recentDataFolderString.split("\\|");
			for(String location : dataFoldersLocations) {
				
				File methodFile = FIOUtils.getFileForLocation(location);			
				if(methodFile != null && methodFile.isDirectory() && methodFile.getName().endsWith(".m"))
					recentFiles.add(methodFile);
			}
		}		
		File negativeMethodDataFolder = FIOUtils.getFileForLocation(preferences.get(NEGATIVE_MODE_SOURCE_DIR, ""));
		if(negativeMethodDataFolder != null) {
			negativeModeFolderTextField.setText(negativeMethodDataFolder.getAbsolutePath());
			recentFiles.add(negativeMethodDataFolder);
		}		
		File positiveMethodDataFolder = FIOUtils.getFileForLocation(preferences.get(POSITIVE_MODE_SOURCE_DIR, ""));
		if(positiveMethodDataFolder != null) {
			positiveModeFolderTextField.setText(positiveMethodDataFolder.getAbsolutePath());
			recentFiles.add(positiveMethodDataFolder);
		}
	}

	@Override
	public void loadPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		loadPreferences(prefs);
	}

	@Override
	public void savePreferences() {

		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(POSITIVE_MODE_SOURCE_DIR, positiveModeFolderTextField.getText().trim());
		prefs.put(NEGATIVE_MODE_SOURCE_DIR, negativeModeFolderTextField.getText().trim());
		
		String positiveModeBaseDirectoryPath = "";
		if (positiveModeBaseDirectory != null && positiveModeBaseDirectory.exists()) 			
			positiveModeBaseDirectoryPath = positiveModeBaseDirectory.getAbsolutePath();
		
		prefs.put(POSITIVE_MODE_BASE_DIR, positiveModeBaseDirectoryPath);
		
		String negativeModeBaseDirectoryPath = "";
		if (negativeModeBaseDirectory != null && negativeModeBaseDirectory.exists()) 			
			negativeModeBaseDirectoryPath = negativeModeBaseDirectory.getAbsolutePath();
		
		prefs.put(NEGATIVE_MODE_BASE_DIR, negativeModeBaseDirectoryPath);
		
		List<String>recentMethodPaths = 
				recentFiles.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList());
		prefs.put(RECENT_DATA_FOLDERS, StringUtils.join(recentMethodPaths, "|"));
	}
}
