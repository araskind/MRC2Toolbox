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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableFeatureListPanel extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	private Preferences preferences;
	private File baseDirectory;
	
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.DockableFeatureListPanel";
	private MinimalMSOneFeatureTable featureTable;
	private ImprovedFileChooser chooser;
	
	public DockableFeatureListPanel() {
		
		super("DockableFeatureListPanel", componentIcon, "Feature list manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureTable = new MinimalMSOneFeatureTable();
		add(new JScrollPane(featureTable), BorderLayout.CENTER);
		JPanel fileImportPanel = createFileImportPanel();
		add(fileImportPanel, BorderLayout.SOUTH);
		
		loadPreferences();
		initChooser();
	}

	private JPanel createFileImportPanel() {

		JPanel fileImportPanel = new JPanel();
		fileImportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		fileImportPanel.setLayout(gridBagLayout);
		
		JButton btnNewButton = new JButton(
				MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName());
		btnNewButton.setActionCommand(MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName());
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		fileImportPanel.add(btnNewButton, gbc_btnNewButton);
		
		return fileImportPanel;
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
		//	chooser.setApproveButtonText("Attach document");
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("Text files (TAB-separated)", "txt", "TXT", "tsv", "TSV"));
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("Comma-separated text files", "csv", "CSV"));
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("CEF files", "cef", "CEF"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName())) 			
			chooser.showOpenDialog(this.getContentPane());

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			File inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			savePreferences();
			readFeaturesFromInputFile(inputFile);
		}
	}
	
	private void readFeaturesFromInputFile(File inputFile) {
		// TODO Auto-generated method stub
		MessageDialog.showInfoMsg(
				"Reading features from " + inputFile.getName(), 
				this.getContentPane());
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
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
