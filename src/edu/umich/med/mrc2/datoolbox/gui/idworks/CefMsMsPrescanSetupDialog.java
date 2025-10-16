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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CefMsMsPrescanSetupDialog extends JDialog
	implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -6570332210094151769L;
	private static final Icon scanCefIcon = GuiUtils.getIcon("scanCef", 32);
	private JTextField cefFolderTextField;
	private JTextField missingCompoundsFileTextField;
	private JButton btnBrowseCefFolder;
	private JButton btnBrowseMissingCompsFile;
	private JButton btnScanCefFiles;

	private static final String BROWSE_FOR_CEFS = "BROWSE_FOR_CEFS";
	private static final String BROWSE_FOR_CPD_FILE = "BROWSE_FOR_CPD_FILE";

	private Preferences preferences;
	public static final String PREFS_NODE = CefMsMsPrescanSetupDialog.class.getName();
	public static final String CEF_DIR_PARENT = "CEF_DIR_PARENT";
	public static final String CPD_FILE_PARENT = "CPD_FILE_PARENT";

	private File cefParentDir;
	private File cpdFileParentDir;
	private JButton btnCancel;

	public CefMsMsPrescanSetupDialog(ActionListener listener) {
		super();
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Set up CEF MSMS pre-scan");
		setIconImage(((ImageIcon) scanCefIcon).getImage());

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblSelectDirectoryContaining = new JLabel("Select directory containing CEF files to scan:");
		GridBagConstraints gbc_lblSelectDirectoryContaining = new GridBagConstraints();
		gbc_lblSelectDirectoryContaining.gridwidth = 2;
		gbc_lblSelectDirectoryContaining.anchor = GridBagConstraints.WEST;
		gbc_lblSelectDirectoryContaining.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectDirectoryContaining.gridx = 0;
		gbc_lblSelectDirectoryContaining.gridy = 0;
		panel.add(lblSelectDirectoryContaining, gbc_lblSelectDirectoryContaining);

		cefFolderTextField = new JTextField();
		cefFolderTextField.setEditable(false);
		GridBagConstraints gbc_cefFolderTextField = new GridBagConstraints();
		gbc_cefFolderTextField.gridwidth = 2;
		gbc_cefFolderTextField.insets = new Insets(0, 0, 5, 5);
		gbc_cefFolderTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_cefFolderTextField.gridx = 0;
		gbc_cefFolderTextField.gridy = 1;
		panel.add(cefFolderTextField, gbc_cefFolderTextField);
		cefFolderTextField.setColumns(10);

		btnBrowseCefFolder = new JButton("Browse ...");
		btnBrowseCefFolder.setActionCommand(BROWSE_FOR_CEFS);
		btnBrowseCefFolder.addActionListener(this);
		GridBagConstraints gbc_btnBrowseCefFolder = new GridBagConstraints();
		gbc_btnBrowseCefFolder.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseCefFolder.gridx = 2;
		gbc_btnBrowseCefFolder.gridy = 1;
		panel.add(btnBrowseCefFolder, gbc_btnBrowseCefFolder);

		JLabel lblSelectMissingCompounds = new JLabel("Select missing compounds output file:");
		GridBagConstraints gbc_lblSelectMissingCompounds = new GridBagConstraints();
		gbc_lblSelectMissingCompounds.gridwidth = 2;
		gbc_lblSelectMissingCompounds.anchor = GridBagConstraints.WEST;
		gbc_lblSelectMissingCompounds.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectMissingCompounds.gridx = 0;
		gbc_lblSelectMissingCompounds.gridy = 2;
		panel.add(lblSelectMissingCompounds, gbc_lblSelectMissingCompounds);

		missingCompoundsFileTextField = new JTextField();
		GridBagConstraints gbc_missingCompoundsFileTextField = new GridBagConstraints();
		gbc_missingCompoundsFileTextField.gridwidth = 2;
		gbc_missingCompoundsFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_missingCompoundsFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_missingCompoundsFileTextField.gridx = 0;
		gbc_missingCompoundsFileTextField.gridy = 3;
		panel.add(missingCompoundsFileTextField, gbc_missingCompoundsFileTextField);
		missingCompoundsFileTextField.setColumns(10);

		btnBrowseMissingCompsFile = new JButton("Browse ...");
		btnBrowseMissingCompsFile.setActionCommand(BROWSE_FOR_CPD_FILE);
		btnBrowseMissingCompsFile.addActionListener(this);
		GridBagConstraints gbc_btnBrowseMissingCompsFile = new GridBagConstraints();
		gbc_btnBrowseMissingCompsFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseMissingCompsFile.gridx = 2;
		gbc_btnBrowseMissingCompsFile.gridy = 3;
		panel.add(btnBrowseMissingCompsFile, gbc_btnBrowseMissingCompsFile);

		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 4;
		panel.add(btnCancel, gbc_btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnScanCefFiles = new JButton("Scan CEF files");
		btnScanCefFiles.setActionCommand(MainActionCommands.CEF_MSMS_SCAN_RUN_COMMAND.getName());
		btnScanCefFiles.addActionListener(listener);
		GridBagConstraints gbc_btnScanCefFiles = new GridBagConstraints();
		gbc_btnScanCefFiles.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnScanCefFiles.gridwidth = 2;
		gbc_btnScanCefFiles.gridx = 1;
		gbc_btnScanCefFiles.gridy = 4;
		panel.add(btnScanCefFiles, gbc_btnScanCefFiles);
		JRootPane rootPane = SwingUtilities.getRootPane(btnScanCefFiles);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnScanCefFiles);

		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().endsWith(BROWSE_FOR_CEFS))
			selectCEFDir();

		if(e.getActionCommand().endsWith(BROWSE_FOR_CPD_FILE))
			selectCPDoutFile();
	}

	private void selectCPDoutFile() {

		JnaFileChooser fc = new JnaFileChooser(cpdFileParentDir);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Set log file:");
		fc.setSaveButtonText("Set log file");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "missingCompounds_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {			
			missingCompoundsFileTextField.setText(fc.getSelectedFile().getAbsolutePath());
			savePreferences();
		}
	}

	private void selectCEFDir() {
		
		JnaFileChooser fc = new JnaFileChooser(cefParentDir);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select folder containing CEF files:");
		fc.setMultiSelectionEnabled(false);
		fc.setAllowOverwrite(true);
		fc.setOpenButtonText("Select folder");
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			
			cefFolderTextField.setText(fc.getSelectedFile().getAbsolutePath());
			missingCompoundsFileTextField.setText(cefFolderTextField.getText() + File.separator + "missingCompounds.txt");
			savePreferences();
		}
	}

	public File getCefDirectory() {

		String cefPath = cefFolderTextField.getText().trim();
		if(cefPath.isEmpty())
			return null;

		return Paths.get(cefPath).toFile();
	}

	public File getCpdFile() {

		String cpdPath = missingCompoundsFileTextField.getText().trim();
		if(cpdPath.isEmpty())
			return null;

		return Paths.get(cpdPath).toFile();
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		cefParentDir = 
				Paths.get(preferences.get(CEF_DIR_PARENT, MRC2ToolBoxCore.dataDir)).toFile();
		cpdFileParentDir = 
				Paths.get(preferences.get(CPD_FILE_PARENT, MRC2ToolBoxCore.dataDir)).toFile();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);
		File cefDir = getCefDirectory();
		if(cefDir != null)
			preferences.put(CEF_DIR_PARENT, cefDir.getParentFile().getAbsolutePath());

		File cpdFIle = getCpdFile();
		if(cpdFIle != null)
			preferences.put(CPD_FILE_PARENT, cpdFIle.getParentFile().getAbsolutePath());

	}
}

























