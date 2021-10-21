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

package edu.umich.med.mrc2.datoolbox.gui.io.msms;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.lib.MSMSLibraryListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ReferenceMSMSLibraryExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms.DecoyLibraryGenerationTask;

public class ReferenceMSMSLibraryExportDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4915205827460068131L;

	private static final Icon libraryExportIcon = GuiUtils.getIcon("exportLibrary", 32);
	
	private TaskListener taskListener;
	private MSMSLibraryListingTable libraryListingTable;
	private JTextField outputFolderTextField;

	private JComboBox outputFormatComboBox;
	private JComboBox polarityComboBox;
	
	public static final String BROWSE = "BROWSE";
	public static final String BROWSE_FOR_LOG = "BROWSE_FOR_LOG";
	public static final String BROWSE_FOR_MSP = "BROWSE_FOR_MSP";
	private File baseDirectory;
	
	private Preferences preferences;

	private JFormattedTextField entriesPerFileTextField;
	public static final String BASE_DIR = "BASE_DIR";
	public static final String OUTPUT_FORMAT = "OUTPUT_FORMAT";
	public static final String POLARITY = "POLARITY";
	public static final String ENTRIES_PER_FILE = "ENTRIES_PER_FILE";
	public static final String MAX_TIME_PER_COMPOUND = "MAX_TIME_PER_COMPOUND";
	public static final String HIGH_RES_ONLY = "HIGH_RES_ONLY";
	public static final long MAX_TIME_PER_COMPOUND_DEFAULT = 10L;
	
	private JTextField oldLogFileTextField;
	private JTextField lastProcessedIdTextField;
	private JTextField mspTextField;
	private JFormattedTextField maxTimeTextField;
	private JCheckBox highResOnlyCheckBox;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReferenceMSMSLibraryExportDialog(TaskListener taskListener) {
		super();
		this.taskListener = taskListener;
		setPreferredSize(new Dimension(800, 480));
		setSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Export reference MSMS library");
		setIconImage(((ImageIcon) libraryExportIcon).getImage());
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(0, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		libraryListingTable = new MSMSLibraryListingTable();
		libraryListingTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		libraryListingTable.setTableModelFromReferenceMsMsLibraryList(
				IDTDataCash.getReferenceMsMsLibraryList());
		JScrollPane scrollPane = new JScrollPane(libraryListingTable);
		
//		JScrollPane scrollPane = new JScrollPane();
		
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_1.add(scrollPane, gbc_scrollPane);
		
		JLabel lblNewLabel = new JLabel("Output format ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		outputFormatComboBox = new JComboBox(
				new DefaultComboBoxModel<MsLibraryFormat>(
						new MsLibraryFormat[] {
								MsLibraryFormat.MSP, 
								MsLibraryFormat.SIRIUS_MS, 
								MsLibraryFormat.MGF,
								MsLibraryFormat.XY_META_MGF}));
		GridBagConstraints gbc_outputFormatComboBox = new GridBagConstraints();
		gbc_outputFormatComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_outputFormatComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFormatComboBox.gridx = 1;
		gbc_outputFormatComboBox.gridy = 1;
		panel_1.add(outputFormatComboBox, gbc_outputFormatComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		polarityComboBox = new JComboBox(new DefaultComboBoxModel<Polarity>(
				new Polarity[] {
						Polarity.Positive, 
						Polarity.Negative}));
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 3;
		gbc_polarityComboBox.gridy = 1;
		panel_1.add(polarityComboBox, gbc_polarityComboBox);
		
		JLabel lblNewLabel_2 = new JLabel("Entries per file (0 => all in one file)");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 5;
		gbc_lblNewLabel_2.gridy = 1;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		entriesPerFileTextField = new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_entriesPerFileTextField = new GridBagConstraints();
		gbc_entriesPerFileTextField.insets = new Insets(0, 0, 5, 0);
		gbc_entriesPerFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_entriesPerFileTextField.gridx = 6;
		gbc_entriesPerFileTextField.gridy = 1;
		panel_1.add(entriesPerFileTextField, gbc_entriesPerFileTextField);
		
		highResOnlyCheckBox = new JCheckBox("High resolution entries only");
		GridBagConstraints gbc_highResOnlyCheckBox = new GridBagConstraints();
		gbc_highResOnlyCheckBox.anchor = GridBagConstraints.WEST;
		gbc_highResOnlyCheckBox.gridwidth = 3;
		gbc_highResOnlyCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_highResOnlyCheckBox.gridx = 0;
		gbc_highResOnlyCheckBox.gridy = 2;
		panel_1.add(highResOnlyCheckBox, gbc_highResOnlyCheckBox);
		
		outputFolderTextField = new JTextField();
		outputFolderTextField.setEditable(false);
		GridBagConstraints gbc_outputFolderTextField = new GridBagConstraints();
		gbc_outputFolderTextField.gridwidth = 6;
		gbc_outputFolderTextField.insets = new Insets(0, 0, 5, 5);
		gbc_outputFolderTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputFolderTextField.gridx = 0;
		gbc_outputFolderTextField.gridy = 3;
		panel_1.add(outputFolderTextField, gbc_outputFolderTextField);
		outputFolderTextField.setColumns(10);
		
		JButton outputDirBrowseButton = new JButton("Select output folder");
		outputDirBrowseButton.setActionCommand(BROWSE);
		outputDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_outputDirBrowseButton = new GridBagConstraints();
		gbc_outputDirBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_outputDirBrowseButton.gridx = 6;
		gbc_outputDirBrowseButton.gridy = 3;
		panel_1.add(outputDirBrowseButton, gbc_outputDirBrowseButton);
		
		oldLogFileTextField = new JTextField();
		oldLogFileTextField.setEditable(false);
		GridBagConstraints gbc_oldLogFileTextField = new GridBagConstraints();
		gbc_oldLogFileTextField.gridwidth = 6;
		gbc_oldLogFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_oldLogFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_oldLogFileTextField.gridx = 0;
		gbc_oldLogFileTextField.gridy = 4;
		panel_1.add(oldLogFileTextField, gbc_oldLogFileTextField);
		oldLogFileTextField.setColumns(10);
		
		JButton oldLogBrowseButton = new JButton("Select old log file");
		oldLogBrowseButton.setActionCommand(BROWSE_FOR_LOG);
		oldLogBrowseButton.addActionListener(this);
		GridBagConstraints gbc_oldLogBrowseButton = new GridBagConstraints();
		gbc_oldLogBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_oldLogBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_oldLogBrowseButton.gridx = 6;
		gbc_oldLogBrowseButton.gridy = 4;
		panel_1.add(oldLogBrowseButton, gbc_oldLogBrowseButton);
		
		mspTextField = new JTextField();
		mspTextField.setEditable(false);
		GridBagConstraints gbc_mspTextField = new GridBagConstraints();
		gbc_mspTextField.gridwidth = 6;
		gbc_mspTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mspTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mspTextField.gridx = 0;
		gbc_mspTextField.gridy = 5;
		panel_1.add(mspTextField, gbc_mspTextField);
		mspTextField.setColumns(10);
		
		JButton selectMspButton = new JButton("Select previous MSP output ");
		selectMspButton.setActionCommand(BROWSE_FOR_MSP);
		selectMspButton.addActionListener(this);
		GridBagConstraints gbc_selectMspButton = new GridBagConstraints();
		gbc_selectMspButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectMspButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectMspButton.gridx = 6;
		gbc_selectMspButton.gridy = 5;
		panel_1.add(selectMspButton, gbc_selectMspButton);
		
		JLabel lblNewLabel_3 = new JLabel("Last processed ID");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 6;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		lastProcessedIdTextField = new JTextField();
		GridBagConstraints gbc_lastProcessedIdTextField = new GridBagConstraints();
		gbc_lastProcessedIdTextField.insets = new Insets(0, 0, 0, 5);
		gbc_lastProcessedIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lastProcessedIdTextField.gridx = 1;
		gbc_lastProcessedIdTextField.gridy = 6;
		panel_1.add(lastProcessedIdTextField, gbc_lastProcessedIdTextField);
		lastProcessedIdTextField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Max time per entry");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 2;
		gbc_lblNewLabel_4.gridy = 6;
		panel_1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		maxTimeTextField = new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_maxTimeTextField = new GridBagConstraints();
		gbc_maxTimeTextField.insets = new Insets(0, 0, 0, 5);
		gbc_maxTimeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxTimeTextField.gridx = 3;
		gbc_maxTimeTextField.gridy = 6;
		panel_1.add(maxTimeTextField, gbc_maxTimeTextField);
		
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

		JButton btnSave = new JButton(
				MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		btnSave.addActionListener(this);
		
		JButton createDecoyButton = new JButton(
				MainActionCommands.CREATE_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		createDecoyButton.setActionCommand(
				MainActionCommands.CREATE_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName());
		createDecoyButton.addActionListener(this);

		panel.add(createDecoyButton);
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
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))
			exportSelectedReferenceLibrary();
		
		if(e.getActionCommand().equals(MainActionCommands.CREATE_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))
			createDecoyFromSelectedReferenceLibrary();
		
		if(e.getActionCommand().equals(BROWSE))
			selectOutputFolder();
		
		if(e.getActionCommand().equals(BROWSE_FOR_LOG))
			selectOldLogFile();
		
		if(e.getActionCommand().equals(BROWSE_FOR_MSP))
			selectPreviousMspOutputFile();
	}
	
	private int getEntriesPerFile() {
		
		String epf = entriesPerFileTextField.getText().trim();
		if(epf != null && !epf.isEmpty())
			return Integer.parseInt(epf);
		else
			return -1;
	}
	
	private String getLastProcessedId() {
		return lastProcessedIdTextField.getText().trim();
	}
	
	private File getOldLogFile() {
		
		String oldLogPath  = oldLogFileTextField.getText().trim();
		if(oldLogPath.isEmpty())
			return null;
		
		File logFile = Paths.get(oldLogPath).toFile();
		if(!logFile.exists())
			return null;
		else
			return logFile;
	}
	
	private File getPreviousMspFile() {
		
		String mspPath  = mspTextField.getText().trim();
		if(mspPath.isEmpty())
			return null;
		
		File mspFile = Paths.get(mspPath).toFile();
		if(!mspFile.exists())
			return null;
		else
			return mspFile;
	}
	
	private void createDecoyFromSelectedReferenceLibrary() {
		
		ArrayList<String>errors = new ArrayList<String>();		
		ReferenceMsMsLibrary library = libraryListingTable.getSelectedLibrary();
		if(library == null)
			errors.add("No library selected.");
		
		Polarity polarity = (Polarity) polarityComboBox.getSelectedItem();
		File outputDirectory = Paths.get(baseDirectory.getAbsolutePath()).toFile();	
		String lastProcessedId = getLastProcessedId();
		String idMask = DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}";
		Pattern idPattern = Pattern.compile(idMask);
		if(!lastProcessedId.isEmpty() && !idPattern.matcher(lastProcessedId).find()) 
			errors.add("Invalid library ID. Correct format is \"" + 
					DataPrefix.MSMS_LIBRARY_ENTRY.name() + "\" followed by 9 digits.");
		
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		long maxTime = MAX_TIME_PER_COMPOUND_DEFAULT;
		if(!maxTimeTextField.getText().trim().isEmpty())				
			maxTime = Long.parseLong(maxTimeTextField.getText().trim());
		
		DecoyLibraryGenerationTask task = 
				new DecoyLibraryGenerationTask(
						library, polarity, 
						outputDirectory,
						maxTime);
		
		if(!lastProcessedId.isEmpty())
			task.setLastProcessedId(lastProcessedId);		
		
		File oldLog =  getOldLogFile();
		if(oldLog != null)
			task.setReferenceLogFile(oldLog);	
		
		File prevMsp = getPreviousMspFile();
		if(prevMsp != null)
			task.setPreviousMspFile(prevMsp);
		
		task.addTaskListener(taskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		savePreferences();
		dispose();
	}
	
	private void exportSelectedReferenceLibrary() {

		ReferenceMsMsLibrary library = libraryListingTable.getSelectedLibrary();
		MsLibraryFormat exportFormat = (MsLibraryFormat) outputFormatComboBox.getSelectedItem();
		Polarity polarity = (Polarity) polarityComboBox.getSelectedItem();
		File outputDirectory = Paths.get(baseDirectory.getAbsolutePath()).toFile();
		
		ReferenceMSMSLibraryExportTask task = 
				new ReferenceMSMSLibraryExportTask(
						library, 
						exportFormat, 
						polarity, 
						outputDirectory, 
						getEntriesPerFile(),
						highResOnlyCheckBox.isSelected());
		task.addTaskListener(taskListener);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		savePreferences();
		dispose();
	}
	
	private void selectOutputFolder() {
		
		ImprovedFileChooser outputFileChooser = new ImprovedFileChooser();
		outputFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		outputFileChooser.addActionListener(this);
		outputFileChooser.setAcceptAllFileFilterUsed(true);
		outputFileChooser.setMultiSelectionEnabled(false);
		outputFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		outputFileChooser.setCurrentDirectory(baseDirectory);
		outputFileChooser.setApproveButtonText("Set output folder");		
		if(outputFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			
			outputFolderTextField.setText(outputFileChooser.getSelectedFile().getAbsolutePath());
			baseDirectory = outputFileChooser.getSelectedFile();
			savePreferences();
		}
	}
	
	private void selectOldLogFile() {
		
		ImprovedFileChooser oldLogFileChooser = new ImprovedFileChooser();
		oldLogFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		oldLogFileChooser.addActionListener(this);
		oldLogFileChooser.setAcceptAllFileFilterUsed(false);
		oldLogFileChooser.setMultiSelectionEnabled(false);
		oldLogFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		oldLogFileChooser.setCurrentDirectory(baseDirectory);
		oldLogFileChooser.setApproveButtonText("Select old log file");	
		oldLogFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Log files", "log", "LOG"));
		if(oldLogFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)			
			oldLogFileTextField.setText(oldLogFileChooser.getSelectedFile().getAbsolutePath());
	}
	
	private void selectPreviousMspOutputFile() {
		
		ImprovedFileChooser mspFileChooser = new ImprovedFileChooser();
		mspFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		mspFileChooser.addActionListener(this);
		mspFileChooser.setAcceptAllFileFilterUsed(false);
		mspFileChooser.setMultiSelectionEnabled(false);
		mspFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		mspFileChooser.setCurrentDirectory(baseDirectory);
		mspFileChooser.setApproveButtonText("Select previous MSP output file");	
		mspFileChooser.addChoosableFileFilter(
				new FileNameExtensionFilter(MsLibraryFormat.MSP.getName(), MsLibraryFormat.MSP.getFileExtension()));
		if(mspFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)			
			mspTextField.setText(mspFileChooser.getSelectedFile().getAbsolutePath());
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, 
						Paths.get(System.getProperty("user.dir"), "data", "mssearch").toString()));
		outputFolderTextField.setText(baseDirectory.getAbsolutePath());

		MsLibraryFormat depth = MsLibraryFormat.getFormatByName(
				preferences.get(OUTPUT_FORMAT, MsLibraryFormat.MSP.name()));
		outputFormatComboBox.setSelectedItem(depth);
		
		Polarity pol = Polarity.getPolarityByCode(preferences.get(POLARITY, Polarity.Positive.getCode()));
		polarityComboBox.setSelectedItem(pol);			
		entriesPerFileTextField.setText(Integer.toString(preferences.getInt(ENTRIES_PER_FILE, 0)));		
		maxTimeTextField.setText(Long.toString(preferences.getLong(MAX_TIME_PER_COMPOUND, MAX_TIME_PER_COMPOUND_DEFAULT)));		
		highResOnlyCheckBox.setSelected(preferences.getBoolean(HIGH_RES_ONLY, true));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
		
		MsLibraryFormat format = (MsLibraryFormat) outputFormatComboBox.getSelectedItem();
		preferences.put(OUTPUT_FORMAT, format.name());
		
		Polarity pol = (Polarity) polarityComboBox.getSelectedItem();
		preferences.put(POLARITY, pol.getCode());		
		preferences.putInt(ENTRIES_PER_FILE, getEntriesPerFile());		
		preferences.putLong(MAX_TIME_PER_COMPOUND, Long.parseLong(maxTimeTextField.getText().trim()));
		preferences.putBoolean(HIGH_RES_ONLY, highResOnlyCheckBox.isSelected());
	}

}
