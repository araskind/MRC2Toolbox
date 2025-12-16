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

package edu.umich.med.mrc2.datoolbox.gui.filetools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.gui.automator.TextAreaOutputStream;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.TableClipboardKeyAdapter;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.gui.worklist.WorklistTable;
import edu.umich.med.mrc2.datoolbox.gui.worklist.WorklistTableModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.RawDataUploadPrepTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.WorklistExtractionTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class FileToolsDialog extends JDialog 
	implements ActionListener, BackedByPreferences, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8176806884989838988L;
	
	private static final Icon dataFileToolsIcon = GuiUtils.getIcon("dataFileTools", 32);

	private Preferences preferences;
	public static final String CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
	public static final String ZIP_DATA_BASE_DIRECTORY = "ZIP_DATA_BASE_DIRECTORY";
	public static final String RECURSIVE_SCAN = "RECURSIVE_SCAN";
	
	private File baseDirectory;
	private JTextField rawDataFolderForCleanupTextField;
	private JTextArea consoleTextArea;
	private TextAreaOutputStream taos;
	private PrintStream ps;
	private JCheckBox removeProfileMsCheckBox;
	private IOFileFilter dotDfilter;
	private FileFilter txtFilter;
	private Worklist worklist;
	private WorklistTable worklistTable;	
	private JTextField rawDataDirTextField;
	private JTextField zipDirTextField;
	private File zipBaseDirectory;
	private JButton cleanAndZipButton;
	private JButton zipDirBrowseButton;
	private JButton rawDataBrowseButton;
	private JCheckBox recursiveScanCheckBox;
	private JCheckBox createZipsCheckBox;

	public FileToolsDialog() {

		super();
		setTitle("Raw data file tools");
		setIconImage(((ImageIcon) dataFileToolsIcon).getImage());

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel worklistPanel = createWorklistPanel();
		tabbedPane.addTab("Extract worklist", worklistPanel);
		 
		JPanel cleanupPanel = createUntargetedResultsCleanupPanel();
		tabbedPane.addTab("Cleanup raw data", cleanupPanel);
		
		JPanel cleanAndZipPanel = createMoTrPACCleanAndZIPPanel();
		tabbedPane.addTab("Metabolomics Workbench raw data upload preparation", cleanAndZipPanel);
		
		dotDfilter = FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT");
		loadPreferences();
	
		pack();
	}
	
	private JPanel createMoTrPACCleanAndZIPPanel() {

		JPanel panel = new JPanel();		
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 86, 86, 86, 89, 0};
		gbl_panel.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
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
		
		return panel;
	}
	
	private JPanel createUntargetedResultsCleanupPanel() {
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{71, 46, 46, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{14, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblNewLabel = new JLabel("Raw data folder");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		rawDataFolderForCleanupTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(rawDataFolderForCleanupTextField, gbc_textField);
		rawDataFolderForCleanupTextField.setColumns(10);

		JButton fileBrowseButton = new JButton("Browse ...");
		fileBrowseButton.setActionCommand(MainActionCommands.SELECT_RAW_DATA_FOLDER_FOR_CLEANUP.getName());
		fileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_fileBrowseButton = new GridBagConstraints();
		gbc_fileBrowseButton.insets = new Insets(0, 0, 5, 5);
		gbc_fileBrowseButton.gridx = 4;
		gbc_fileBrowseButton.gridy = 0;
		panel.add(fileBrowseButton, gbc_fileBrowseButton);

		JButton runButton = new JButton(MainActionCommands.CLEANUP_RAW_DATA.getName());
		runButton.setActionCommand(MainActionCommands.CLEANUP_RAW_DATA.getName());
		runButton.addActionListener(this);
		
		removeProfileMsCheckBox = new JCheckBox("Remove MS profile data");
		GridBagConstraints gbc_removeProfileMsCheckBox = new GridBagConstraints();
		gbc_removeProfileMsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_removeProfileMsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_removeProfileMsCheckBox.gridx = 1;
		gbc_removeProfileMsCheckBox.gridy = 1;
		panel.add(removeProfileMsCheckBox, gbc_removeProfileMsCheckBox);
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.gridwidth = 2;
		gbc_runButton.insets = new Insets(0, 0, 5, 5);
		gbc_runButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_runButton.gridx = 3;
		gbc_runButton.gridy = 1;
		panel.add(runButton, gbc_runButton);

		consoleTextArea = new JTextArea();
		JScrollPane areaScrollPane = new JScrollPane(consoleTextArea);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(600, 250));
		GridBagConstraints gbc_console = new GridBagConstraints();
		gbc_console.insets = new Insets(0, 0, 0, 5);
		gbc_console.gridwidth = 5;
		gbc_console.fill = GridBagConstraints.BOTH;
		gbc_console.gridx = 0;
		gbc_console.gridy = 2;
		panel.add(areaScrollPane, gbc_console);	
		try {
			taos = new TextAreaOutputStream(consoleTextArea);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (taos != null) {
			ps = new PrintStream(taos);
			System.setOut(ps);
			//	System.setErr(ps);
		}
		return panel;
	}
	
	private JPanel createWorklistPanel() {
		
		JPanel panel = new JPanel(new BorderLayout(0,0));
		WorklistToolsToolbar toolbar = new WorklistToolsToolbar(this);
		panel.add(toolbar, BorderLayout.NORTH);
		
		worklistTable = new WorklistTable();
		panel.add(new JScrollPane(worklistTable), BorderLayout.CENTER);
		worklistTable.addKeyListener(new TableClipboardKeyAdapter(worklistTable));
		
		return panel;
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		//	Worklist
		if (command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName())) {
			try {
				scanDirectoryForSampleInfo(false);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (command.equals(MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName())) {
			try {
				scanDirectoryForSampleInfo(true);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (command.equals(MainActionCommands.SAVE_WORKLIST_COMMAND.getName()))
			saveWorklistToFile();

		if (command.equals(MainActionCommands.COPY_WORKLIST_COMMAND.getName()))
			copyWorklistToClipboard();

		if (command.equals(MainActionCommands.CLEAR_WORKLIST_COMMAND.getName()))
			clearWorklist();

		//	Data cleanup
		if (command.equals(MainActionCommands.SELECT_RAW_DATA_FOLDER_FOR_CLEANUP.getName()))
			selectRawDataFolderForCleanup();
		
		if (command.equals(MainActionCommands.CLEANUP_RAW_DATA.getName())) 
			removeResultsFolders();
		
		if (command.equals(MainActionCommands.BROWSE_FOR_RAW_DATA_DIR.getName()))
			selectRawDataDirectory();

		if(command.equals(MainActionCommands.BROWSE_FOR_ZIP_DIR.getName()))
			selectZIPDestinationDirectory();
		
		if(command.equals(MainActionCommands.CLEAN_AND_ZIP_COMMAND.getName()))
			cleanAndZip();
	}
	
	private void selectRawDataDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files to compress:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			rawDataDirTextField.setText(inputFile.getAbsolutePath());
			savePreferences();
		}
	}
	
	private void selectZIPDestinationDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(zipBaseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select detination directory for compressed raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputFile = fc.getSelectedFile();
			zipBaseDirectory = inputFile.getParentFile();
			zipDirTextField.setText(inputFile.getAbsolutePath());
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

	private void scanDirectoryForSampleInfo(boolean appendWorklist) throws Exception {

		File dirToScan = selectRawFilesDirectory();
		if (dirToScan != null && dirToScan.exists()) {

			Collection<File> dataDiles = FileUtils.listFilesAndDirs(
					dirToScan,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dataDiles.isEmpty()) {

				WorklistExtractionTask wlit = new WorklistExtractionTask(
						dirToScan,
						WorklistImportType.RAW_DATA_DIRECTORY_SCAN,
						false,
						appendWorklist);
				wlit.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(wlit);
			}
		}
	}
	
	private void saveWorklistToFile() {

		String worklistString = worklistTable.getWorklistsAsString();
		if(worklistString == null || worklistString.isEmpty())
			return;

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save assay worklist to file:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "EXTRACTED_WORKLIST_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			baseDirectory = outputFile.getParentFile();
			outputFile = FIOUtils.changeExtension(outputFile, "txt") ;
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
		    try {
				Files.writeString(outputPath, 
						worklistString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private File selectRawFilesDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))	{	
			baseDirectory = fc.getSelectedFile();
			savePreferences();
			return baseDirectory;
		}
		else
			return null;
	}
	
	private void copyWorklistToClipboard() {

		String worklistString = worklistTable.getWorklistsAsString();
		if(worklistString == null || worklistString.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(worklistString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	
	private void clearWorklist() {

		int selectedValue = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to clear the current worklist?", this);

		if (selectedValue == JOptionPane.YES_OPTION)
			((WorklistTableModel) worklistTable.getModel()).clearModel();
	}
	
	private void selectRawDataFolderForCleanup() {
	
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			rawDataFolderForCleanupTextField.setText(inputFile.getAbsolutePath());
			savePreferences();
		}
	}

	private void removeResultsFolders() {
		consoleTextArea.setText("");
		List<Path> dDirs = null;
		try {
			dDirs = Files.find(Paths.get(rawDataFolderForCleanupTextField.getText().trim()), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d")) && 
					fileAttr.isDirectory()).collect(Collectors.toList());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dDirs != null) {
			for(Path dir : dDirs) {

				File resultsDir = Paths.get(dir.toString(), "Results").toFile();
				if(resultsDir.exists()) {
					try {
						FileUtils.deleteDirectory(resultsDir);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Cleaning " + dir.getFileName());
				}
				if(removeProfileMsCheckBox.isSelected()) {
					
					File profileMs = Paths.get(dir.toString(), "AcqData", "MSProfile.bin").toFile();
					if(profileMs.exists()) 
						FileUtils.deleteQuietly(profileMs);
				}
			}
		}
		MessageDialog.showInfoMsg("Data cleanup finished", this);
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		baseDirectory = Paths.get(
				preferences.get(CURRENT_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).toFile();
		zipBaseDirectory =
				new File(preferences.get(ZIP_DATA_BASE_DIRECTORY,
					MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		rawDataDirTextField.setText(baseDirectory.getAbsolutePath());
		zipDirTextField.setText(zipBaseDirectory.getAbsolutePath());		
		recursiveScanCheckBox.setSelected(preferences.getBoolean(RECURSIVE_SCAN, Boolean.FALSE));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(CURRENT_DIRECTORY, baseDirectory.getAbsolutePath());
		zipBaseDirectory = new File(zipDirTextField.getText());
		preferences.put(ZIP_DATA_BASE_DIRECTORY, zipBaseDirectory.getAbsolutePath());
		preferences.putBoolean(RECURSIVE_SCAN, recursiveScanCheckBox.isSelected());
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(WorklistExtractionTask.class))
				finalizeWorklistExtractionTask((WorklistExtractionTask) e.getSource());					
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();		
	}
	
	private synchronized void finalizeWorklistExtractionTask(WorklistExtractionTask eTask) {
		
		Worklist newWorklist = eTask.getWorklist();
		if(worklist == null) {
			worklist = newWorklist;
		}
		else {
			if(eTask.isAppendWorklist())
				worklist.appendWorklist(newWorklist);			
			else
				worklist = newWorklist;
		}
		worklistTable.setTableModelFromWorklist(worklist);
	}
}
