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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class PeakQualityImportDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon addPeakQualityDataIcon = GuiUtils.getIcon("addStandardSample", 24);	
	public static final String CHOOSE_DATA_DIR_COMMAND = "Select directory containing data files";
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;
	private Preferences preferences;
	private static final String BASE_DIR = "BASE_DIR";
	private File baseDirectory;	
	private MinimalDataFileListingTable dataFileTable;
	private JTextField dataDirTextField;
	
	public PeakQualityImportDialog(
			DataAnalysisProject currentExperiment,
			DataPipeline activeDataPipeline,
			ActionListener listener) {
		super();
		
		setIconImage(((ImageIcon) addPeakQualityDataIcon).getImage());
		setPreferredSize(new Dimension(800, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		this.currentExperiment = currentExperiment;
		this.activeDataPipeline = activeDataPipeline;
		setTitle("Import peak quality data from CEF files for data pipeline " 
				+ activeDataPipeline.getName());

		JPanel filesPanel = new JPanel();
		filesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(filesPanel, BorderLayout.CENTER);
		filesPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		filesPanel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{485, 86, 0};
		gbl_panel_1.rowHeights = new int[]{20, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		dataDirTextField = new JTextField();
		dataDirTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.anchor = GridBagConstraints.NORTH;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panel_1.add(dataDirTextField, gbc_textField);
		dataDirTextField.setColumns(10);
		
		JButton btnNewButton = new JButton(CHOOSE_DATA_DIR_COMMAND);
		btnNewButton.addActionListener(this);
		btnNewButton.setActionCommand(CHOOSE_DATA_DIR_COMMAND);	
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(btnNewButton, gbc_btnNewButton);

		dataFileTable = new MinimalDataFileListingTable();
		filesPanel.add(new JScrollPane(dataFileTable), BorderLayout.CENTER);
			
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JButton startImportButton = new JButton(
				MainActionCommands.START_PEAK_QUALITY_DATA_IMPORT_COMMAND.getName());
		startImportButton.setActionCommand(
				MainActionCommands.START_PEAK_QUALITY_DATA_IMPORT_COMMAND.getName());
		startImportButton.addActionListener(listener);
		panel.add(startImportButton);
		JRootPane rootPane = SwingUtilities.getRootPane(startImportButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(startImportButton);
		
		loadPreferences();
		pack();
	}
	
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if (command.equals(CHOOSE_DATA_DIR_COMMAND))
			selectDataDirectory();
	}
	
	private void selectDataDirectory() {
		// TODO Auto-generated method stub
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select data directory:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Select");
		if (fc.showOpenDialog(this)) {
			
			baseDirectory = fc.getSelectedFile();
			dataDirTextField.setText(baseDirectory.getAbsolutePath());
			savePreferences();
			scanDirectoryForDataFiles();
		}
	}

	private void scanDirectoryForDataFiles() {
		
		List<Path>cefPaths = FIOUtils.findFilesByExtension(baseDirectory.toPath(), "cef");
		if(cefPaths.isEmpty()) {
			MessageDialog.showWarningMsg("No CEF files found in\n" + 
					baseDirectory.getAbsolutePath(), this);
			return;
		}
		Set<String>cefNames = cefPaths.stream().map(p -> FileNameUtils.getBaseName(p)).
				sorted().collect(Collectors.toSet());		
		Set<DataFile> dataFiles = 
				currentExperiment.getDataFilesWithDataForPipeline(activeDataPipeline);
		
		ArrayList<String>errors = new ArrayList<String>();
		Set<String>toImport = new TreeSet<String>();
		for(DataFile df : dataFiles) {
			
			if(!cefNames.contains(df.getName()))
				errors.add(df.getName() + " not found");
			else
				toImport.add(df.getName());
		}
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), this);
		    return;
		}
		List<File>cefFiles = cefPaths.stream().
				filter(p -> toImport.contains(FileNameUtils.getBaseName(p))).
				map(p -> p.toFile()).
				sorted().collect(Collectors.toList());
		
		dataFileTable.setModelFromDataFiles(cefFiles);
	}
	
	public Collection<File>getCefFiles(){
		return dataFileTable.getAllFiles();
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).getAbsoluteFile();		
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
	}
}
