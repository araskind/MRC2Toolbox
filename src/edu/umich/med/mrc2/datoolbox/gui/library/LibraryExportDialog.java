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

package edu.umich.med.mrc2.datoolbox.gui.library;

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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibraryExportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class LibraryExportDialog extends JDialog implements ActionListener, BackedByPreferences, TaskListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -5710215500366663782L;	
	private static final Icon exportLibraryIcon = GuiUtils.getIcon("exportLibrary", 32);
	
	private File baseDirectory;
	private File destinationDirectory;
	private File exportFile;
	private JPanel panel;
	private CompoundLibrary currentLibrary;
	private Collection<LibraryMsFeature>targetSubset;
	private Collection<MsFeature>featureSubset;
	private JCheckBox combineAdductsCheckBox;
	
	private JTextField exportFileTextField;
	private String exportCommand;
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";	
	private static final String BROWSE_COMMAND = "BROWSE";

	public LibraryExportDialog(String exportCommand, CompoundLibrary currentLibrary) {

		super(MRC2ToolBoxCore.getMainWindow(), "Export library");
		setIconImage(((ImageIcon) exportLibraryIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(640, 250));
//		setSize(new Dimension(640, 250));
		
		this.exportCommand = exportCommand;
		this.currentLibrary = currentLibrary;

		String title = "";
		if (exportCommand.equals(MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName()))
			title = "Export complete library to file";

		if(exportCommand.equals(MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName()))
			title = "Export filtered library to file";

		if(exportCommand.equals(MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName()))
			title = "Export feature subset as library file";

		setTitle(title);

		panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		baseDirectory = new File(MRC2ToolBoxCore.libraryDir);
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null)
			baseDirectory = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 182, 220, -167, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Export file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 3;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		exportFileTextField = new JTextField();
		exportFileTextField.setEditable(false);
		GridBagConstraints gbc_exportFileTextField = new GridBagConstraints();
		gbc_exportFileTextField.gridwidth = 3;
		gbc_exportFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_exportFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_exportFileTextField.gridx = 0;
		gbc_exportFileTextField.gridy = 1;
		panel_1.add(exportFileTextField, gbc_exportFileTextField);
		exportFileTextField.setColumns(10);
		
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(BROWSE_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 1;
		panel_1.add(browseButton, gbc_btnNewButton);

		combineAdductsCheckBox = new JCheckBox("Combine all adducts in a single library entry");
		GridBagConstraints gbc_combineAdductsCheckBox = new GridBagConstraints();
		gbc_combineAdductsCheckBox.gridwidth = 3;
		gbc_combineAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_combineAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_combineAdductsCheckBox.gridx = 0;
		gbc_combineAdductsCheckBox.gridy = 2;
		panel_1.add(combineAdductsCheckBox, gbc_combineAdductsCheckBox);

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

		JButton btnSave = new JButton(exportCommand);
		btnSave.setActionCommand(exportCommand);
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		destinationDirectory = null;
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getActionCommand().equals(BROWSE_COMMAND))
			setExportFile();
		
		if (event.getActionCommand().equals(exportCommand))
			exportLibrary();
	}

	private void setExportFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		if(destinationDirectory != null)
			fc.setCurrentDirectory(destinationDirectory.getAbsolutePath());		
		
		fc.setMode(JnaFileChooser.Mode.Files);
		for(MsLibraryFormat f : MsLibraryFormat.values())
			fc.addFilter(f.getName(), f.getFileExtension());
			
		fc.setTitle("Export library");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Select");		
		String defaultFileName = currentLibrary.getLibraryName();
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(this)) {
						
			exportFile  = fc.getSelectedFile();
			String[] fileFilter  = fc.getSelectedFilter();
			if(fileFilter != null) {
				MsLibraryFormat libraryFormat = MsLibraryFormat.getFormatByDescription(fileFilter[0]);
				exportFile = FIOUtils.changeExtension(exportFile, libraryFormat.getFileExtension());
				exportFileTextField.setText(exportFile.getAbsolutePath());
			}
		}		
	}

	private void exportLibrary() {

		if(exportFile == null)
			return;
		
		String extension = 
				FilenameUtils.getExtension(exportFile.getName());
		MsLibraryFormat libraryFormat = 
				MsLibraryFormat.getFormatByExtension(extension);

		if(libraryFormat == null) {
			MessageDialog.showErrorMsg("Unrecognized export format: " + extension, this);
			return;
		}
		LibraryExportTask let = new LibraryExportTask(
					null,
					exportFile,				
					combineAdductsCheckBox.isSelected(),
					currentLibrary,
					targetSubset,
					featureSubset,
					libraryFormat);
		let.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(let);	
	}

	public void setFeatureSubset(Collection<MsFeature> features) {
		featureSubset = features;
	}

	public void setTargetSubset(Collection<LibraryMsFeature> targetSubset) {
		this.targetSubset = targetSubset;
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(LibraryExportTask.class)) {

				MessageDialog.showInfoMsg("Library export completed.", this);
				dispose();
			}
		}
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String baseDirPath = preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			baseDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public File getExportFile() {
		return exportFile;
	}

	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}
}





















