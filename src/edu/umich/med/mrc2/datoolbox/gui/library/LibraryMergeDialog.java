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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.prefs.Preferences;

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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibraryExportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class LibraryMergeDialog  extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 1882854332229563842L;

	private Preferences preferences;
	public static final String PREFS_NODE = LibraryMergeDialog.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private static final String SELECT_INPUT_LIBRARY = "Select input library";
	private JFileChooser chooser;
	private File baseDirectory;
	private JPanel panel;
	private CompoundLibrary currentLibrary;
	private Set<LibraryMsFeature>targetSubset;
	private Set<MsFeature>featureSubset;
	private JCheckBox combineAdductsCheckBox;
	private JLabel lblLibraryFile;
	private JTextField sourceLibraryTextField;
	private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private JButton browseButton;
	private JCheckBox combineLibCheckBox;

	private static final Icon mergeLibrariesIcon = GuiUtils.getIcon("mergeLibraries", 32);

	public LibraryMergeDialog() {

		super();
		setTitle("Export / reformat / merge libraries");
		setIconImage(((ImageIcon) mergeLibrariesIcon).getImage());
		setPreferredSize(new Dimension(640, 480));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setBorder(new TitledBorder(
			UIManager.getBorder("TitledBorder.border"), "Output",
			TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setApproveButtonText("Export library");
		chooser.addActionListener(this);
		for(MsLibraryFormat f : MsLibraryFormat.values()){

			FileNameExtensionFilter txtFilter =
					new FileNameExtensionFilter(f.getName(), f.getFileExtension());
			chooser.addChoosableFileFilter(txtFilter);
		}
		loadPreferences();
		chooser.setCurrentDirectory(baseDirectory);
		panel.add(chooser);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(
			UIManager.getBorder("TitledBorder.border"), "File library options",
			TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 404, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblLibraryFile = new JLabel("Data file");
		GridBagConstraints gbc_lblLibraryFile = new GridBagConstraints();
		gbc_lblLibraryFile.anchor = GridBagConstraints.EAST;
		gbc_lblLibraryFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblLibraryFile.gridx = 0;
		gbc_lblLibraryFile.gridy = 0;
		panel_1.add(lblLibraryFile, gbc_lblLibraryFile);

		sourceLibraryTextField = new JTextField();
		sourceLibraryTextField.setEditable(false);
		GridBagConstraints gbc_sourceLibraryTextField = new GridBagConstraints();
		gbc_sourceLibraryTextField.insets = new Insets(0, 0, 5, 5);
		gbc_sourceLibraryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sourceLibraryTextField.gridx = 1;
		gbc_sourceLibraryTextField.gridy = 0;
		panel_1.add(sourceLibraryTextField, gbc_sourceLibraryTextField);
		sourceLibraryTextField.setColumns(10);

		browseButton = new JButton("Browse ...");
		browseButton.setActionCommand(SELECT_INPUT_LIBRARY);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_browseButton.insets = new Insets(0, 0, 5, 0);
		gbc_browseButton.gridx = 2;
		gbc_browseButton.gridy = 0;
		panel_1.add(browseButton, gbc_browseButton);

		combineLibCheckBox = new JCheckBox("Combine with active database library");
		GridBagConstraints gbc_combineLibCheckBox = new GridBagConstraints();
		gbc_combineLibCheckBox.anchor = GridBagConstraints.WEST;
		gbc_combineLibCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_combineLibCheckBox.gridx = 1;
		gbc_combineLibCheckBox.gridy = 1;
		panel_1.add(combineLibCheckBox, gbc_combineLibCheckBox);

		combineAdductsCheckBox = new JCheckBox("Combine all adducts in a single library entry");
		GridBagConstraints gbc_combineAdductsCheckBox = new GridBagConstraints();
		gbc_combineAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_combineAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_combineAdductsCheckBox.gridx = 1;
		gbc_combineAdductsCheckBox.gridy = 2;
		panel_1.add(combineAdductsCheckBox, gbc_combineAdductsCheckBox);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				savePreferences();
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(chooser);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);

		targetSubset = null;
		currentLibrary = null;
		featureSubset = null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if(command.equals(JFileChooser.CANCEL_SELECTION)) {
			savePreferences();
			this.dispose();
		}

		if(command.equals(JFileChooser.APPROVE_SELECTION)){

			File selectedFile =
					FIOUtils.changeExtension(chooser.getSelectedFile(), getSelectedFormat().getFileExtension());
			baseDirectory = selectedFile.getParentFile();
			savePreferences();
	        createLibraryExportTask(selectedFile);
		}
		if(command.equals(SELECT_INPUT_LIBRARY))
			selectInputLibraryFile();
	}

	private MsLibraryFormat getSelectedFormat() {

		String libraryFormatString = chooser.getFileFilter().getDescription();

		for(MsLibraryFormat f : MsLibraryFormat.values()){

			if(f.getName().equals(libraryFormatString))
				return f;
		}
		return null;
	}

	private void createLibraryExportTask(File selectedFile) {
		
		File libraryInputFile = null;
		if(!sourceLibraryTextField.getText().isEmpty())
			libraryInputFile = new File(sourceLibraryTextField.getText());

		MsLibraryFormat libraryFormat = getSelectedFormat();

		if(libraryInputFile == null && currentLibrary == null) {
			MessageDialog.showErrorMsg("No database and/or file library to process!", this);
			return;
		}
		LibraryExportTask let = new LibraryExportTask(
				libraryInputFile,
				selectedFile,
				combineAdductsCheckBox.isSelected(),
				currentLibrary,
				null,
				null,
				libraryFormat);
		MRC2ToolBoxCore.getTaskController().addTask(let);
		this.dispose();
	}

	private void selectInputLibraryFile() {

		JFileChooser libFileChooser = new ImprovedFileChooser();
		File inputFile = null;

		libFileChooser.setAcceptAllFileFilterUsed(false);
		libFileChooser.setMultiSelectionEnabled(false);
		libFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		libFileChooser.setDialogTitle("Select library file:");
		FileNameExtensionFilter cefFilter = 
				new FileNameExtensionFilter(MsLibraryFormat.CEF.getName(), 
						MsLibraryFormat.CEF.getFileExtension());
		libFileChooser.setFileFilter(cefFilter);
		libFileChooser.setCurrentDirectory(baseDirectory);

		if (libFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			inputFile = libFileChooser.getSelectedFile();
			baseDirectory = libFileChooser.getCurrentDirectory();

			if (inputFile.exists() && inputFile.canRead()) {

				sourceLibraryTextField.setText(inputFile.getAbsolutePath());
				chooser.setCurrentDirectory(inputFile.getParentFile());

				String libName = FilenameUtils.getBaseName(inputFile.getPath());
				String timestamp = dateTimeFormat.format(new Date());
				String outFile = libName + "-" + timestamp;

				if(currentLibrary != null && combineLibCheckBox.isSelected())
					outFile = currentLibrary.getLibraryName() + " AND " + libName + "-" + timestamp;

				MsLibraryFormat libraryFormat = getSelectedFormat();
				File libFile = new File(outFile + "." + libraryFormat.getFileExtension());
				chooser.setSelectedFile(libFile);

				savePreferences();
			}
			else {
				MessageDialog.showErrorMsg("Invalid file selection!", this);
			}
		}
	}

	public void setCurrentLibrary(CompoundLibrary newLibrary) {

		currentLibrary = newLibrary;
		if(currentLibrary != null) {

			String timestamp = dateTimeFormat.format(new Date());
			File libFile  = new File(currentLibrary.getLibraryName() + "-" + timestamp);
			//File libFile  = new File(baseDirectory.getPath() + File.separator + currentLibrary.getLibraryName() + "-" + timestamp);

			String extension = FilenameUtils.getExtension(libFile.getAbsolutePath());
			MsLibraryFormat libraryFormat = getSelectedFormat();
			if(!extension.equalsIgnoreCase(libraryFormat.getFileExtension()))
				libFile = new File(FilenameUtils.removeExtension(libFile.getName()) + "." + libraryFormat.getFileExtension());

			if(combineLibCheckBox.isSelected())
				chooser.setSelectedFile(libFile);
		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxCore.libraryDir));
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



































