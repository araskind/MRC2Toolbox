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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTCefMSMSPrescanOrImportTask;

public class IDTrackerMultiFileMSMSDataImportDialog extends JDialog
	implements ActionListener, BackedByPreferences, TaskListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 6250617694491853835L;

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.IDTrackerMultiFileDataImportDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	public static final String ADD_DATA_FILES = "Add data files";
	public static final String REMOVE_DATA_FILES = "Remove data files";
	public static final String CLEAR_DATA = "Clear all input data";

	private static final Icon iddaIcon = GuiUtils.getIcon("importIDDAdata", 32);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 32);

	private LIMSExperiment experiment;
//	private LIMSSamplePreparation samplePrep;
	
	private JButton uploadDataButton;
	private IDtrackerDataFileSampleMatchTable dataFileSampleMatchTable;
	private IDTrackerMultiFileImportToolbar toolbar;
	private File baseDirectory;
	private int fileNumber, processedFiles;
	private Collection<String> importLog;

	private DAMethodAssignmentDialog daMethodAssignmentDialog;

	/**
	 * @wbp.parser.constructor
	 */
	public IDTrackerMultiFileMSMSDataImportDialog(
			Dialog owner,
			LIMSExperiment experiment) {

		super(owner);
		this.experiment = experiment;
		initGui();
	}

	public IDTrackerMultiFileMSMSDataImportDialog(LIMSExperiment experiment) {

		super();
		this.experiment = experiment;
		initGui();
	}

	private void initGui() {

		setTitle("Import MSMS data from multiple files");
		setIconImage(((ImageIcon) loadMultiFileIcon).getImage());
		setPreferredSize(new Dimension(800, 480));
		setSize(new Dimension(800, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		toolbar =  new IDTrackerMultiFileImportToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(null);
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		dataPanel.add(scrollPane, BorderLayout.CENTER);

		dataFileSampleMatchTable = new IDtrackerDataFileSampleMatchTable();
		scrollPane.setViewportView(dataFileSampleMatchTable);
		dataFileSampleMatchTable.addTablePopupMenu(new DataImportPopupMenu(this));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);
		uploadDataButton = new JButton(MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName());
		uploadDataButton.setActionCommand(MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName());
		uploadDataButton.addActionListener(this);
		panel_1.add(uploadDataButton);

		JRootPane rootPane = SwingUtilities.getRootPane(uploadDataButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(uploadDataButton);

		loadPreferences();
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(ADD_DATA_FILES))
			addDataFiles();

		if (e.getActionCommand().equals(REMOVE_DATA_FILES))
			removeDataFiles();

		if (e.getActionCommand().equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName()))
			showDAMethodAssignmentDialog();

		if (e.getActionCommand().equals(MainActionCommands.ASSIGN_DA_METHOD_TO_DATA_FILES_COMMAND.getName()))
			assignDaMethodToFiles();

		if (e.getActionCommand().equals(MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName()))
			importMSdata();
	}
	
	private void addDataFiles() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.addFilter("MGF files", "mgf", "MGF");
		fc.setTitle("Select MSMS files");
		fc.setMultiSelectionEnabled(true);
		fc.setOpenButtonText("Select files");
		
		if (fc.showOpenDialog(this)) {
			
			File[] dataFiles = fc.getSelectedFiles();
			if(dataFiles.length == 0)
				return;
			
			dataFileSampleMatchTable.setTableModelFromFiles(dataFiles, experiment);
			baseDirectory = dataFiles[0].getParentFile();
			savePreferences();
		}
	}

	private void assignDaMethodToFiles() {

		Collection<DataFile> files = daMethodAssignmentDialog.getDataFiles();
		DataExtractionMethod method = daMethodAssignmentDialog.getSelectedMethod();
		if(files.isEmpty() || method == null)
			return;

		dataFileSampleMatchTable.setDaMethodFoFiles(files, method);
		daMethodAssignmentDialog.dispose();
	}

	private void showDAMethodAssignmentDialog() {

		if(dataFileSampleMatchTable.getSelectedDataFiles().isEmpty())
			return;

		daMethodAssignmentDialog =
				new DAMethodAssignmentDialog(
						dataFileSampleMatchTable.getSelectedDataFiles(), this);
		daMethodAssignmentDialog.setLocationRelativeTo(this);
		daMethodAssignmentDialog.setVisible(true);
	}

	private void removeDataFiles() {

		if(dataFileSampleMatchTable.getSelectedDataFiles().isEmpty())
			return;

		String yesNoQuestion = "Do you want to remove selected data files from import queue?";
		if(MessageDialog.showChoiceMsg(yesNoQuestion , this) == JOptionPane.NO_OPTION)
			return;

		dataFileSampleMatchTable.removeSelectedDataFiles();
	}

	private void importMSdata() {

		if(!validateInput())
			return;		
		
		Map<DataFile,DataExtractionMethod>fileDaMethodMap = 
				dataFileSampleMatchTable.getFileDaMethodMap();

		fileNumber = fileDaMethodMap.size();
		processedFiles = 0;
		importLog = new TreeSet<String>();
		for (Entry<DataFile, DataExtractionMethod> entry : fileDaMethodMap.entrySet()) {
			
			IDTCefMSMSPrescanOrImportTask task = 
					new IDTCefMSMSPrescanOrImportTask(entry.getKey(), entry.getValue(), true);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	private boolean validateInput() {
		
		if(dataFileSampleMatchTable.hasMissingInjectionData() ||
				dataFileSampleMatchTable.hasMissingDataAnalysisMethod()) {
			MessageDialog.showErrorMsg("Some data files are not linked to injections\n"
					+ "and/or data analysis methods", this);
			return false;
		}
		Map<DataFile, DataExtractionMethod> existingData = null;
		try {
			existingData = getExistingAnalysisMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(existingData != null && !existingData.isEmpty()) {
			
			String message = "The following results are already present in the database:\n";
			for(Entry<DataFile,DataExtractionMethod>entry : existingData.entrySet())				
				message += entry.getKey().getName() + " processed using " + entry.getValue().getName() + " method\n";
			
			MessageDialog.showErrorMsg(message, this);
			return false;
		}	
		return true;
	}
	
	private Map<DataFile,DataExtractionMethod>getExistingAnalysisMap() throws Exception {
		
		Map<DataFile,DataExtractionMethod>existingAnalysisMap = new TreeMap<DataFile,DataExtractionMethod>();
		Map<DataFile,DataExtractionMethod>fileDaMethodMap = dataFileSampleMatchTable.getFileDaMethodMap();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT DATA_ANALYSIS_ID FROM DATA_ANALYSIS_MAP  " +
				"WHERE INJECTION_ID = ? AND EXTRACTION_METHOD_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		for(Entry<DataFile,DataExtractionMethod>entry : fileDaMethodMap.entrySet()) {
			
			ps.setString(1, entry.getKey().getInjectionId());
			ps.setString(2, entry.getValue().getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				existingAnalysisMap.put(entry.getKey(), entry.getValue());	
			
			rs.close();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);		
		return existingAnalysisMap;
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
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

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(IDTCefMSMSPrescanOrImportTask.class))
				finalizeIDTCefMSMSPrescanOrImportTask( (IDTCefMSMSPrescanOrImportTask)e.getSource());
		}
	}
	
	private synchronized void finalizeIDTCefMSMSPrescanOrImportTask(IDTCefMSMSPrescanOrImportTask task) {
		
		importLog.addAll(task.getImportLog());
		processedFiles++;
		
		if(processedFiles == fileNumber) {

			//	Write error log
			String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
			Path outputPath = Paths.get(baseDirectory.getAbsolutePath(),
							"MSMS_DATA_IMPORT_LOG_" + timestamp + ".TXT");
			
			if(!importLog.isEmpty()) {

			    try {
					Files.write(outputPath, 
							importLog, 
							StandardCharsets.UTF_8,
							StandardOpenOption.CREATE, 
							StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			MessageDialog.showInfoMsg("Data import completed.\n"
					+ "Error log saved to " + outputPath.toString(), this);
			dispose();
		}		
	}

//	public LIMSSamplePreparation getSamplePrep() {
//		return samplePrep;
//	}
//
//	public void setSamplePrep(LIMSSamplePreparation samplePrep) {
//		this.samplePrep = samplePrep;
//	}
}



























