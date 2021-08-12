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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableProcessingMethodsPanel extends DefaultSingleCDockable 
		implements ActionListener, BackedByPreferences {

	private JTextField posMethodTextField;
	private JTextField negMethodTextField;
	private File methodsBaseDirectory;
	private Collection<File>recentFiles;
	private RecentFilesDialog recentFilesDialog;

	public static final String METHODS_DIR = "defaultProcessingMethodsDirectory";
	public static final String METHODS_DIR_DEFAULT = 
			"." + File.separator + "data" + File.separator + "qualmethods";
	public static final String POSITIVE_MODE_METHOD = "defaultProcessingMethodPositiveMode";
	public static final String NEGATIVE_MODE_METHOD = "defaultProcessingMethodNegativeMode";
	public static final String RECENT_METHODS = "recentMethods";

	private static final Icon componentIcon = GuiUtils.getIcon("processingMethod", 16);

	public DockableProcessingMethodsPanel() {

		super("DockableProcessingMethodsPanel", componentIcon, "Data processing methods", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblPosModeMethod = new JLabel("POS mode");
		GridBagConstraints gbc_lblPosModeMethod = new GridBagConstraints();
		gbc_lblPosModeMethod.anchor = GridBagConstraints.EAST;
		gbc_lblPosModeMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblPosModeMethod.gridx = 0;
		gbc_lblPosModeMethod.gridy = 1;
		add(lblPosModeMethod, gbc_lblPosModeMethod);

		posMethodTextField = new JTextField();
		GridBagConstraints gbc_posMethodTextField = new GridBagConstraints();
		gbc_posMethodTextField.insets = new Insets(0, 0, 5, 5);
		gbc_posMethodTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_posMethodTextField.gridx = 1;
		gbc_posMethodTextField.gridy = 1;
		add(posMethodTextField, gbc_posMethodTextField);
		posMethodTextField.setColumns(10);
		
		JButton selectRecentPosMethodButton = new JButton("Recent ...");
		selectRecentPosMethodButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_POSITIVE_MODE_METHOD.getName());
		selectRecentPosMethodButton.addActionListener(this);
		GridBagConstraints gbc_selectRecentPosMethodButton = new GridBagConstraints();
		gbc_selectRecentPosMethodButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectRecentPosMethodButton.gridx = 2;
		gbc_selectRecentPosMethodButton.gridy = 1;
		add(selectRecentPosMethodButton, gbc_selectRecentPosMethodButton);	

		JButton browseForPosMethodButton = new JButton("Browse...");
		browseForPosMethodButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_POSITIVE_MODE_METHOD.getName());
		browseForPosMethodButton.addActionListener(this);
		GridBagConstraints gbc_posMethodBrowseButton = new GridBagConstraints();
		gbc_posMethodBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_posMethodBrowseButton.gridx = 3;
		gbc_posMethodBrowseButton.gridy = 1;
		add(browseForPosMethodButton, gbc_posMethodBrowseButton);

		JLabel lblNegModeMethod = new JLabel("NEG mode");
		GridBagConstraints gbc_lblNegModeMethod = new GridBagConstraints();
		gbc_lblNegModeMethod.anchor = GridBagConstraints.EAST;
		gbc_lblNegModeMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblNegModeMethod.gridx = 0;
		gbc_lblNegModeMethod.gridy = 2;
		add(lblNegModeMethod, gbc_lblNegModeMethod);

		negMethodTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 2;
		add(negMethodTextField, gbc_textField_1);
		negMethodTextField.setColumns(10);
		
		JButton selectRecentNegMethodButton = new JButton("Recent ...");
		selectRecentNegMethodButton.setActionCommand(
				MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_METHOD.getName());
		selectRecentNegMethodButton.addActionListener(this);
		GridBagConstraints gbc_selectRecentNegMethodButton = new GridBagConstraints();
		gbc_selectRecentNegMethodButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectRecentNegMethodButton.gridx = 2;
		gbc_selectRecentNegMethodButton.gridy = 2;
		add(selectRecentNegMethodButton, gbc_selectRecentNegMethodButton);
		
		JButton browseForNegMethodButton = new JButton("Browse...");
		browseForNegMethodButton.setActionCommand(
				MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_METHOD.getName());
		browseForNegMethodButton.addActionListener(this);
		GridBagConstraints gbc_negMethodBrowseButton = new GridBagConstraints();
		gbc_negMethodBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_negMethodBrowseButton.gridx = 3;
		gbc_negMethodBrowseButton.gridy = 2;
		add(browseForNegMethodButton, gbc_negMethodBrowseButton);

		recentFiles = new TreeSet<File>();
		loadPreferences();		
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_METHOD.getName()) ||
				command.equals(MainActionCommands.BROWSE_FOR_POSITIVE_MODE_METHOD.getName()))
			selectMethodFile(command);
		
		if(command.equals(MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_METHOD.getName()) ||
				command.equals(MainActionCommands.SELECT_RECENT_POSITIVE_MODE_METHOD.getName()))
			selectRecentMethodFile(command);
		
		if(command.equals(MainActionCommands.SET_NEGATIVE_MODE_METHOD.getName()) || 				
				command.equals(MainActionCommands.SET_POSITIVE_MODE_METHOD.getName())) 
			setRecentMethodFile(command);
	}
	
	private void setRecentMethodFile(String command) {
		
		File methodFile = recentFilesDialog.getSelectedFile();
		if(methodFile == null)
			return;
		methodsBaseDirectory = methodFile.getParentFile();
		
		if(command.equals(MainActionCommands.SET_NEGATIVE_MODE_METHOD.getName()))
			negMethodTextField.setText(methodFile.getAbsolutePath());
		
		if(command.equals(MainActionCommands.SET_POSITIVE_MODE_METHOD.getName()))				 
			posMethodTextField.setText(methodFile.getAbsolutePath());
		
		recentFiles.add(methodFile);		
		savePreferences();
		recentFilesDialog.dispose();
	}
	
	private void selectRecentMethodFile(String command) {
		
		String selectCommand = null;
		if(command.equals(MainActionCommands.SELECT_RECENT_NEGATIVE_MODE_METHOD.getName()))
				selectCommand = MainActionCommands.SET_NEGATIVE_MODE_METHOD.getName();
		
		if(command.equals(MainActionCommands.SELECT_RECENT_POSITIVE_MODE_METHOD.getName()))
				selectCommand = MainActionCommands.SET_POSITIVE_MODE_METHOD.getName();
		
		recentFilesDialog = new RecentFilesDialog(command, recentFiles, this, selectCommand);
		recentFilesDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		recentFilesDialog.setVisible(true);
	}
	
	private void selectMethodFile(String command) {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select data analysis method:");	
		chooser.setCurrentDirectory(methodsBaseDirectory);
		if (chooser.showOpenDialog(MRC2ToolBoxCore.getMainWindow()) == JFileChooser.APPROVE_OPTION) {

			inputFile = chooser.getSelectedFile();
			if(!inputFile.isDirectory() || !inputFile.getName().endsWith(".m")) {
				MessageDialog.showErrorMsg(inputFile.getName() + " is not a valid Agilent method!", chooser);
				return;
			}
			methodsBaseDirectory = inputFile.getParentFile();
			if (command.equals(MainActionCommands.BROWSE_FOR_POSITIVE_MODE_METHOD.getName())) 
				posMethodTextField.setText(inputFile.getAbsolutePath());				
			
			if (command.equals(MainActionCommands.BROWSE_FOR_NEGATIVE_MODE_METHOD.getName())) 
				negMethodTextField.setText(inputFile.getAbsolutePath());					
			
			recentFiles.add(inputFile);
			savePreferences();			
		}
	}
	
	public File getNegativeMethodFile() {
		return new File(negMethodTextField.getText());
	}

	public File getPositiveMethodFile() {
		return new File(posMethodTextField.getText());
	}

	public void setNegativeMethodFile(String pathToNegativeMethodFile) {
		negMethodTextField.setText(pathToNegativeMethodFile);
	}

	public void setPositiveMethodFile(String pathToPositiveMethodFile) {
		posMethodTextField.setText(pathToPositiveMethodFile);
	}

	@Override
	public void loadPreferences(Preferences preferences) {		
		
		methodsBaseDirectory = new File(preferences.get(METHODS_DIR, METHODS_DIR_DEFAULT));
		if(!methodsBaseDirectory.exists())
			methodsBaseDirectory = new File(METHODS_DIR_DEFAULT);
		
		String methodsString = preferences.get(RECENT_METHODS, "");
		if(!methodsString.isEmpty()) {
			
			String[] methodFilesLocations = methodsString.split("\\|");
			for(String location : methodFilesLocations) {
				
				File methodFile = FIOUtils.getFileForLocation(location);			
				if(methodFile != null && methodFile.isDirectory() && methodFile.getName().endsWith(".m"))
					recentFiles.add(methodFile);
			}
		}
		File negativeMethodFile = FIOUtils.getFileForLocation(preferences.get(NEGATIVE_MODE_METHOD, ""));
		if(negativeMethodFile != null) {
			negMethodTextField.setText(negativeMethodFile.getAbsolutePath());
			recentFiles.add(negativeMethodFile);
		}		
		File positiveMethodFile = FIOUtils.getFileForLocation(preferences.get(POSITIVE_MODE_METHOD, ""));
		if(positiveMethodFile != null) {
			posMethodTextField.setText(positiveMethodFile.getAbsolutePath());
			recentFiles.add(positiveMethodFile);
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
		prefs.put(NEGATIVE_MODE_METHOD, negMethodTextField.getText().trim());
		prefs.put(POSITIVE_MODE_METHOD, posMethodTextField.getText().trim());
		prefs.put(METHODS_DIR, methodsBaseDirectory.getAbsolutePath());
		
		List<String>recentMethodPaths = 
				recentFiles.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList());
		prefs.put(RECENT_METHODS, StringUtils.join(recentMethodPaths, "|"));
	}
}









