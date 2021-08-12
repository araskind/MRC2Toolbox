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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.ConvertCefLibraryForRecursionTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class ConvertLibraryForRecursionDialog  extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 1882854332229563842L;
	private static final Icon mergeLibrariesIcon = GuiUtils.getIcon("mergeLibraries", 32);
	
	private Preferences preferences;
	public static final String PREFS_NODE = ConvertLibraryForRecursionDialog.class.getName();
	public static final String BASE_LIBRARY_DIRECTORY = "BASE_DIRECTORY";
	private File baseLibraryDirectory;
	private JTextField sourceLibraryTextField;
	private JTextField outputFolderTextField;
	private JCheckBox combineAdductsCheckBox;

	private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public ConvertLibraryForRecursionDialog() {

		super();
		setTitle("Confert CEF library for recursive search");
		setIconImage(((ImageIcon) mergeLibrariesIcon).getImage());
		setPreferredSize(new Dimension(640, 230));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 230));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panelOne = new JPanel();
		panelOne.setLayout(new BorderLayout(0, 0));
		panelOne.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panelOne, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panelOne.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 404, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblLibraryFile = new JLabel("Input CEF file");
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

		JButton browseButton = new JButton("Browse ...");
		browseButton.setActionCommand(MainActionCommands.SELECT_INPUT_LIBRARY_FOR_CONVERSION_COMMAND.getName());
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_browseButton.insets = new Insets(0, 0, 5, 0);
		gbc_browseButton.gridx = 2;
		gbc_browseButton.gridy = 0;
		panel_1.add(browseButton, gbc_browseButton);
		
		JLabel lblNewLabel = new JLabel("Output folder");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		outputFolderTextField = new JTextField();
		outputFolderTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		panel_1.add(outputFolderTextField, gbc_textField);
		outputFolderTextField.setColumns(10);
		
		JButton selectOutputFolderButton = new JButton("Browse ...");
		selectOutputFolderButton.setActionCommand(
				MainActionCommands.SELECT_OUTPUT_FOLDER_FOR_CONVERTED_LIBRARY_COMMAND.getName());
		selectOutputFolderButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 1;
		panel_1.add(selectOutputFolderButton, gbc_btnNewButton);

		combineAdductsCheckBox = new JCheckBox("Combine all adducts in a single library entry");		
		GridBagConstraints gbc_combineAdductsCheckBox = new GridBagConstraints();
		gbc_combineAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_combineAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_combineAdductsCheckBox.gridx = 1;
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

		JButton btnSave = new JButton("Convert");
		btnSave.setActionCommand(MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_COMMAND.getName());
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

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

		String command = event.getActionCommand();

		if(command.equals(MainActionCommands.SELECT_INPUT_LIBRARY_FOR_CONVERSION_COMMAND.getName()))
			selectInputLibraryFile();
		
		if(command.equals(MainActionCommands.SELECT_OUTPUT_FOLDER_FOR_CONVERTED_LIBRARY_COMMAND.getName()))
			selectOutputFolder();
		
		if(command.equals(MainActionCommands.CONVERT_LIBRARY_FOR_RECURSION_COMMAND.getName()))
			convertLibraryForRecursion();
	}
	
	private File getInputFile() {
		
		String filePath = sourceLibraryTextField.getText().trim();
		if(filePath.isEmpty())
			return null;
		
		File inputCef = new File(filePath);
		if(!inputCef.exists() || !inputCef.canRead())	
			return null;
		else
			return inputCef;
	}
	
	private File getOutputFolder() {
		
		String filePath = outputFolderTextField.getText().trim();
		if(filePath.isEmpty())
			return null;
		
		File outputFolder = new File(filePath);
		if(!outputFolder.exists() || !outputFolder.canWrite())
			return null;
		else
			return outputFolder;
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
		libFileChooser.setCurrentDirectory(baseLibraryDirectory);

		if (libFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			inputFile = libFileChooser.getSelectedFile();
			baseLibraryDirectory = libFileChooser.getCurrentDirectory();
			if (inputFile.exists() && inputFile.canRead()) {

				sourceLibraryTextField.setText(inputFile.getAbsolutePath());
				savePreferences();
				
				if(getOutputFolder() == null)
					outputFolderTextField.setText(inputFile.getParentFile().getAbsolutePath());
			}
		}
	}
	
	private void selectOutputFolder() {
		
		JFileChooser outputFolderChooser = new ImprovedFileChooser();
		File outputFolder = null;

		outputFolderChooser.setMultiSelectionEnabled(false);
		outputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		outputFolderChooser.setDialogTitle("Select output folder:");
		File inputCef =  getInputFile();
		if(inputCef != null)
			outputFolderChooser.setCurrentDirectory(inputCef.getParentFile());
		else
			outputFolderChooser.setCurrentDirectory(baseLibraryDirectory);

		if (outputFolderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			outputFolder = outputFolderChooser.getSelectedFile();
			baseLibraryDirectory = outputFolderChooser.getCurrentDirectory();

			if (outputFolder.exists() && outputFolder.canWrite())
				outputFolderTextField.setText(outputFolder.getAbsolutePath());
		}
	}

	private void convertLibraryForRecursion() {
		
		File inputCef = getInputFile();
		File outputDir = getOutputFolder();
		if(inputCef == null) {
			MessageDialog.showErrorMsg("Input file not specified.", this);
			return;
		}
		if(outputDir == null) {
			MessageDialog.showErrorMsg("Output folder not specified.", this);
			return;
		}		
		MsLibraryFormat libraryFormat = MsLibraryFormat.CEF;	
		File outputFile  = Paths.get(outputDir.getAbsolutePath(), 
				FilenameUtils.getBaseName(inputCef.getName()) + "_" + FIOUtils.getTimestamp() 
				+ "." + libraryFormat.getFileExtension()).toFile();
		
		ConvertCefLibraryForRecursionTask task = new ConvertCefLibraryForRecursionTask(
				inputCef, outputFile, combineAdductsCheckBox.isSelected());
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		dispose();
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		baseLibraryDirectory =  new File(preferences.get(BASE_LIBRARY_DIRECTORY, MRC2ToolBoxCore.libraryDir));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_LIBRARY_DIRECTORY, baseLibraryDirectory.getAbsolutePath());
	}
}



































