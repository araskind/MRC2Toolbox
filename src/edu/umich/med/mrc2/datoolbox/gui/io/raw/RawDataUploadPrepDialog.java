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

package edu.umich.med.mrc2.datoolbox.gui.io.raw;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.RawDataUploadPrepTask;

public class RawDataUploadPrepDialog extends JDialog
	implements BackedByPreferences, ActionListener, TaskListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -1153207989372564653L;

	private static final Icon cleanAndZipIcon = GuiUtils.getIcon("cleanAndZip", 32);

	private Preferences preferences;
	public static final String PREFS_NODE = RawDataUploadPrepDialog.class.getName();
	public static final String RAW_DATA_BASE_DIRECTORY = "RAW_DATA_BASE_DIRECTORY";
	public static final String ZIP_DATA_BASE_DIRECTORY = "ZIP_DATA_BASE_DIRECTORY";
	public static final String RECURSIVE_SCAN = "RECURSIVE_SCAN";

	private JTextField rawDataDirTextField;
	private JTextField zipDirTextField;
	private JFileChooser chooser;
	private File baseDirectory;
	private File zipBaseDirectory;
	private JButton cleanAndZipButton;
	private JButton zipDirBrowseButton;
	private JButton rawDataBrowseButton;
	private String fileSelectType;
	private JCheckBox recursiveScanCheckBox;
	private JCheckBox createZipsCheckBox;

	public RawDataUploadPrepDialog() {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800, 200));
		setTitle("MOTRPAC raw data upload preparation");
		setIconImage(((ImageIcon) cleanAndZipIcon).getImage());
		setSize(new Dimension(800, 200));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 86, 86, 86, 89, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblSmiles = new JLabel("Data directory");
		GridBagConstraints gbc_lblSmiles = new GridBagConstraints();
		gbc_lblSmiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblSmiles.anchor = GridBagConstraints.EAST;
		gbc_lblSmiles.gridx = 0;
		gbc_lblSmiles.gridy = 0;
		panel.add(lblSmiles, gbc_lblSmiles);

		rawDataDirTextField = new JTextField();
		//rawDataDirTextField.setEditable(false);
		GridBagConstraints gbc_rawDataTextField = new GridBagConstraints();
		gbc_rawDataTextField.gridwidth = 3;
		gbc_rawDataTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rawDataTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rawDataTextField.gridx = 1;
		gbc_rawDataTextField.gridy = 0;
		panel.add(rawDataDirTextField, gbc_rawDataTextField);
		rawDataDirTextField.setColumns(10);

		rawDataBrowseButton = new JButton("Browse ...");
		rawDataBrowseButton.setActionCommand(MainActionCommands.BROWSE_FOR_RAW_DATA_DIR.getName());
		rawDataBrowseButton.addActionListener(this);
		GridBagConstraints gbc_rawDataBrowseButton = new GridBagConstraints();
		gbc_rawDataBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_rawDataBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_rawDataBrowseButton.anchor = GridBagConstraints.NORTH;
		gbc_rawDataBrowseButton.gridx = 4;
		gbc_rawDataBrowseButton.gridy = 0;
		panel.add(rawDataBrowseButton, gbc_rawDataBrowseButton);
		
		recursiveScanCheckBox = new JCheckBox("Recursively scan for data files");
		GridBagConstraints gbc_recursiveScanCheckBox = new GridBagConstraints();
		gbc_recursiveScanCheckBox.anchor = GridBagConstraints.WEST;
		gbc_recursiveScanCheckBox.gridwidth = 2;
		gbc_recursiveScanCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_recursiveScanCheckBox.gridx = 0;
		gbc_recursiveScanCheckBox.gridy = 1;
		panel.add(recursiveScanCheckBox, gbc_recursiveScanCheckBox);
		
		createZipsCheckBox = new JCheckBox("Clean \"Results\" only (do not create compressed files)");
		GridBagConstraints gbc_createZipsCheckBox = new GridBagConstraints();
		gbc_createZipsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_createZipsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_createZipsCheckBox.gridx = 2;
		gbc_createZipsCheckBox.gridy = 1;
		panel.add(createZipsCheckBox, gbc_createZipsCheckBox);

		JLabel lblPeptide = new JLabel("ZIP directory");
		GridBagConstraints gbc_lblPeptide = new GridBagConstraints();
		gbc_lblPeptide.anchor = GridBagConstraints.EAST;
		gbc_lblPeptide.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeptide.gridx = 0;
		gbc_lblPeptide.gridy = 2;
		panel.add(lblPeptide, gbc_lblPeptide);

		zipDirTextField = new JTextField();
		//zipDirTextField.setEditable(false);
		GridBagConstraints gbc_zipDirTextField = new GridBagConstraints();
		gbc_zipDirTextField.gridwidth = 3;
		gbc_zipDirTextField.insets = new Insets(0, 0, 5, 5);
		gbc_zipDirTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_zipDirTextField.gridx = 1;
		gbc_zipDirTextField.gridy = 2;
		panel.add(zipDirTextField, gbc_zipDirTextField);
		zipDirTextField.setColumns(10);

		zipDirBrowseButton = new JButton("Browse ...");
		zipDirBrowseButton.setActionCommand(MainActionCommands.BROWSE_FOR_ZIP_DIR.getName());
		zipDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_zipDirBrowseButton = new GridBagConstraints();
		gbc_zipDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_zipDirBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_zipDirBrowseButton.gridx = 4;
		gbc_zipDirBrowseButton.gridy = 2;
		panel.add(zipDirBrowseButton, gbc_zipDirBrowseButton);

		cleanAndZipButton = new JButton(MainActionCommands.CLEAN_AND_ZIP_COMMAND.getName());
		cleanAndZipButton.setActionCommand(MainActionCommands.CLEAN_AND_ZIP_COMMAND.getName());
		cleanAndZipButton.addActionListener(this);
		GridBagConstraints gbc_cleanAndZipButton = new GridBagConstraints();
		gbc_cleanAndZipButton.gridwidth = 2;
		gbc_cleanAndZipButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_cleanAndZipButton.gridx = 3;
		gbc_cleanAndZipButton.gridy = 4;
		panel.add(cleanAndZipButton, gbc_cleanAndZipButton);

		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if (command.equals(MainActionCommands.BROWSE_FOR_RAW_DATA_DIR.getName()))
			selectRawDataDirectory();
		
		if (command.equals(MainActionCommands.BROWSE_FOR_ZIP_DIR.getName()))
			selectZIPDirectory();
		
		if(command.equals(MainActionCommands.CLEAN_AND_ZIP_COMMAND.getName()))
			cleanAndZip();
	}

	private void selectRawDataDirectory() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showOpenDialog(this)) {	
			File inputFile = fc.getSelectedFile();
			rawDataDirTextField.setText(inputFile.getAbsolutePath());
			baseDirectory = inputFile.getParentFile();
			savePreferences();
		}
	}

	private void selectZIPDirectory() {

		JnaFileChooser fc = new JnaFileChooser(zipBaseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory to save compressed data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showOpenDialog(this)) {	
			File inputFile = fc.getSelectedFile();
			zipDirTextField.setText(inputFile.getAbsolutePath());
			zipBaseDirectory = inputFile.getParentFile();
			savePreferences();
		}
	}
	
	private void cleanAndZip() {

		if(rawDataDirTextField.getText().isEmpty()) {
			MessageDialog.showErrorMsg("Raw data directory not specified.", this);
			return;
		}
		if(zipDirTextField.getText().isEmpty()) {
			MessageDialog.showErrorMsg("ZIP output directory not specified.", this);
			return;
		}
		RawDataUploadPrepTask task = new RawDataUploadPrepTask(
				rawDataDirTextField.getText(),
				zipDirTextField.getText(),
				recursiveScanCheckBox.isSelected(),
				!createZipsCheckBox.isSelected());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(RAW_DATA_BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		zipBaseDirectory =
			new File(preferences.get(ZIP_DATA_BASE_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));

		rawDataDirTextField.setText(baseDirectory.getAbsolutePath());
		zipDirTextField.setText(zipBaseDirectory.getAbsolutePath());
		
		recursiveScanCheckBox.setSelected(preferences.getBoolean(RECURSIVE_SCAN, Boolean.FALSE));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);

		baseDirectory = new File(rawDataDirTextField.getText());
		zipBaseDirectory = new File(zipDirTextField.getText());
		preferences.put(RAW_DATA_BASE_DIRECTORY, baseDirectory.getAbsolutePath());
		preferences.put(ZIP_DATA_BASE_DIRECTORY, zipBaseDirectory.getAbsolutePath());
		preferences.putBoolean(RECURSIVE_SCAN, recursiveScanCheckBox.isSelected());
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(RawDataUploadPrepTask.class))
				finalizeRawDataUploadPrepTask((RawDataUploadPrepTask)e.getSource());
		}
	}
	
	private void finalizeRawDataUploadPrepTask(RawDataUploadPrepTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();
		MessageDialog.showInfoMsg("Data processing completed.", this);
		savePreferences();
		dispose();
	}
}









