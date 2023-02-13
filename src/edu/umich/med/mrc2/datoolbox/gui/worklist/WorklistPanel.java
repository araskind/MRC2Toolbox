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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.TableClipboardKeyAdapter;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.WorklistExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.WorklistImportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.WorklistUtils;

public class WorklistPanel extends DockableMRC2ToolboxPanel implements BackedByPreferences{

	private Preferences preferences;
	public static final String PREFS_NODE = WorklistPanel.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private DockableWorklistTable worklistTable;
	private File baseDirectory;
	private IOFileFilter dotDfilter;
	private FileFilter txtFilter;
	private WorklistImportDialog worklistImportDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	private static final Icon loadWorklistFromFileIcon = GuiUtils.getIcon("loadWorklist", 24);
	private static final Icon addWorklistFromFileIcon = GuiUtils.getIcon("addWorklist", 24);
	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 24);
	private static final Icon addFromDirIcon = GuiUtils.getIcon("addFromFolder", 24);
	private static final Icon clearWorklistIcon = GuiUtils.getIcon("clearWorklist", 24);
	private static final Icon saveWorklistIcon = GuiUtils.getIcon("saveWorklist", 24);
	private static final Icon copyWorklistToClipboardIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);
	private static final Icon manifestIcon = GuiUtils.getIcon("manifest", 24);
	private static final Icon refreshIcon = GuiUtils.getIcon("rerun", 24);	
	private static final Icon extractWorklistIcon = GuiUtils.getIcon("extractList", 24);
	private static final Icon sampleWarningIcon = GuiUtils.getIcon("sampleWarning", 24);

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "WorklistPanel.layout");

	public WorklistPanel() {

		super("WorklistPanel", PanelList.WORKLIST.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new WorklistPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		worklistTable = new DockableWorklistTable();
		worklistTable.getTable().addKeyListener(new TableClipboardKeyAdapter(worklistTable.getTable()));

		grid.add(0, 0, 100, 100, worklistTable);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		loadPreferences();
		populatePanelsMenu();
		
		dotDfilter = FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT");
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName(), 
				scanDirIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(),
				MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName(), 
				addFromDirIcon, this));
		
		menuActions.addSeparator();
		
		SimpleButtonAction loadWorklistAction = GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(),
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(), 
				loadWorklistFromFileIcon, this);
		loadWorklistAction.setEnabled(false);
		menuActions.add(loadWorklistAction);
		
		SimpleButtonAction addWorklistAction = GuiUtils.setupButtonAction(
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(),
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(), 
				addWorklistFromFileIcon, this);
		addWorklistAction.setEnabled(false);
		menuActions.add(addWorklistAction);
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA.getName(),
				MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA.getName(), 
				sampleWarningIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(),
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(), 
				addFromDirIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(),
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(), 
				copyWorklistToClipboardIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName(),
				MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName(), 
				manifestIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(),
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(), 
				clearWorklistIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXTRACT_WORKLIST_COMMAND.getName(),
				MainActionCommands.EXTRACT_WORKLIST_COMMAND.getName(), 
				extractWorklistIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();
		
		if (command.equals(MainActionCommands.EXTRACT_WORKLIST_COMMAND.getName()))
			extractWorlkistFromRawDataFolderToFile();
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;

		if (command.equals(MainActionCommands.LOAD_WORKLIST_COMMAND.getName()))
			showWorklistLoadDialog(false);

		if (command.equals(MainActionCommands.ADD_WORKLIST_COMMAND.getName()))
			showWorklistLoadDialog(true);

		if (command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(false);

		if (command.equals(MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(true);
		
		if (command.equals(MainActionCommands.CHECK_WORKLIST_FOR_MISSING_DATA.getName()))
			checkWorklistForMissingData();

		if (command.equals(MainActionCommands.SAVE_WORKLIST_COMMAND.getName()))
			saveWorklistToFile();

		if (command.equals(MainActionCommands.COPY_WORKLIST_COMMAND.getName()))
			copyWorklistToClipboard();

		if (command.equals(MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName()))
			saveManifestToFile();

		if (command.equals(MainActionCommands.CLEAR_WORKLIST_COMMAND.getName()))
			clearWorklist();
	}

	private void checkWorklistForMissingData() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;
		
		Worklist worklist = currentExperiment.getWorklistForDataAcquisitionMethod(
				activeDataPipeline.getAcquisitionMethod());
		
		if(worklist == null)
			return;
				
		Set<DataFile> allDataFiles = currentExperiment.getDataFilesForPipeline(activeDataPipeline, false);
		Set<DataFile> worklistDataFiles = worklist.getWorklistItems().stream().
				map(i -> i.getDataFile()).collect(Collectors.toSet());
		
		List<DataFile> missingFiles = allDataFiles.stream().filter(f -> !worklistDataFiles.contains(f)).
			sorted().collect(Collectors.toList());
		
		if(!missingFiles.isEmpty()) {
			
			List<String> fileNames = missingFiles.stream().
					map(f -> f.getName()).sorted().collect(Collectors.toList());
			
			MessageDialog.showWarningMsg(
					"Worklist data missing for:\n" + StringUtils.join(fileNames, "\n"), 
					this.getContentPane());
		}
		else {
			MessageDialog.showInfoMsg(
					"No missing data.", 
					this.getContentPane());
		}
	}

	private void extractWorlkistFromRawDataFolderToFile() {
		
		File dirToScan = selectRawFilesDirectory();
		if (dirToScan != null && dirToScan.exists()) {

			Collection<File> dataDiles = FileUtils.listFilesAndDirs(
					dirToScan,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dataDiles.isEmpty()) {

				WorklistExtractionTask wlit = new WorklistExtractionTask(
						dirToScan,
						WorklistImportType.RAW_DATA_DIRECTORY_SCAN);
				wlit.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(wlit);
			}
		}
	}
	
	private void showWorklistLoadDialog(boolean appendWorklist) {
		
		if(!appendWorklist && currentExperiment.acquisitionMethodHasLinkedWorklist(
				activeDataPipeline.getAcquisitionMethod())) {

			int replaceList = MessageDialog.showChoiceWithWarningMsg(
					"The worklist for method " + activeDataPipeline.getAcquisitionMethod().getName() +
					" is already loaded, do you want to replace it?", this.getContentPane());
			
			if(replaceList == JOptionPane.NO_OPTION)
				return;
		}
		worklistImportDialog = new WorklistImportDialog(this, appendWorklist);
		worklistImportDialog.setLocationRelativeTo(this.getContentPane());
		worklistImportDialog.setVisible(true);
	}
	
	public void loadWorklistFromFile(File inputFile, boolean appendWorklist) {

		WorklistImportTask wit =
				new WorklistImportTask(
						inputFile, 
						activeDataPipeline.getAcquisitionMethod(), 
						appendWorklist,
						WorklistImportType.PLAIN_TEXT_FILE);
		wit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(wit);
		worklistImportDialog.dispose();
	}

	private void saveManifestToFile() {
		
		if(currentExperiment == null || activeDataPipeline == null)
			return;

		String manifestString = 
				WorklistUtils.createManifest(currentExperiment, activeDataPipeline);
		if(manifestString == null || manifestString.isEmpty())
			return;

		baseDirectory = 
				new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).getAbsoluteFile();
		if (currentExperiment != null)
			baseDirectory = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save manifest to file:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = currentExperiment.getName() + "_"
				+ currentExperiment.getActiveDataPipeline().getCode() + "_MANIFEST_"
				+ MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			baseDirectory = outputFile.getParentFile();
			outputFile = FIOUtils.changeExtension(outputFile, "txt") ;
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
		    try {
				Files.writeString(outputPath, 
						manifestString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void copyWorklistToClipboard() {

		String worklistString = worklistTable.getWorklistsAsString();
		if(worklistString == null || worklistString.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(worklistString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private void saveWorklistToFile() {

		String worklistString = worklistTable.getWorklistsAsString();
		if(worklistString == null || worklistString.isEmpty())
			return;

		baseDirectory = 
				new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).getAbsoluteFile();
		if (currentExperiment != null)
			baseDirectory = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save worklist to file:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = currentExperiment.getName() + "_"
				+ currentExperiment.getActiveDataPipeline().getCode() + "_WORKLIST_"
				+ MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
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

	private void loadWorklistFromDirectoryScan(boolean appendWorklist) {

		int selectedValue = JOptionPane.YES_OPTION;
		if (!appendWorklist && currentExperiment.acquisitionMethodHasLinkedWorklist(
				activeDataPipeline.getAcquisitionMethod())) {

			selectedValue = MessageDialog.showChoiceWithWarningMsg(
					"The worklist for method " + activeDataPipeline.getAcquisitionMethod().getName() +
					" is already loaded, do you want to replace it?", this.getContentPane());
		}
		if(selectedValue == JOptionPane.YES_OPTION ) {

			try {
				scanDirectoryForSampleInfo(appendWorklist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void clearPanel() {
		((WorklistTableModel) worklistTable.getTable().getModel()).clearModel();
	}

	private void clearWorklist() {

		int selectedValue = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to clear the worklist for method "
				+ activeDataPipeline.getAcquisitionMethod().getName() + "?", 
				this.getContentPane());

		if (selectedValue == JOptionPane.YES_OPTION) {

			currentExperiment.removeWorklistForMethod(activeDataPipeline.getAcquisitionMethod());
			MRC2ToolBoxCore.getMainWindow().
				switchPanelForDataPipeline(activeDataPipeline, PanelList.WORKLIST);
		}
	}

	private void scanDirectoryForSampleInfo(boolean appendWorklist) throws Exception {

		File dirToScan = selectRawFilesDirectory();

		if (dirToScan != null && dirToScan.exists()) {

			Collection<File> dataDiles = FileUtils.listFilesAndDirs(
					dirToScan,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dataDiles.isEmpty()) {

				WorklistImportTask wlit = new WorklistImportTask(
						dirToScan,
						activeDataPipeline.getAcquisitionMethod(),
						appendWorklist,
						WorklistImportType.RAW_DATA_DIRECTORY_SCAN);
				wlit.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(wlit);
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

	public void showWorklist(Worklist listToShow) {

		if(listToShow != null)
			worklistTable.setTableModelFromWorklist(listToShow);
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(experiment, newDataPipeline);
		menuBar.updateMenuFromExperiment(currentExperiment, activeDataPipeline);
		if(currentExperiment != null && activeDataPipeline != null)
			showWorklist(currentExperiment.getWorklistForDataAcquisitionMethod(
					activeDataPipeline.getAcquisitionMethod()));
	}

	@Override
	public void closeExperiment() {

		super.closeExperiment();
		clearPanel();
		menuBar.updateMenuFromExperiment(null, null);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(WorklistImportTask.class))
				finalizeWorklistLoad((WorklistImportTask) e.getSource());
			
			if (e.getSource().getClass().equals(WorklistExtractionTask.class))
				finalizeWorklistExtraction((WorklistExtractionTask) e.getSource());			
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}
	
	private void finalizeWorklistExtraction(WorklistExtractionTask task) {
		
		File worklist = task.getOutputFile();
		if(worklist == null || !worklist.exists()) {
			MessageDialog.showErrorMsg("Failed to extract worklist.", this.getContentPane());
			return;
		}
		String message = "Worklist file \"" + worklist.getName() + "\" was created, do you want to open it?";
		if(MessageDialog.showChoiceMsg(message, this.getContentPane()) == JOptionPane.YES_OPTION) {
			try {
				Desktop.getDesktop().open(worklist);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void finalizeWorklistLoad(WorklistImportTask eTask) {
		
		//	Clean worklist from files missing in the experiment 
		//	and check for files missing in worklist
		Worklist newWorklist = eTask.getWorklist();
		Set<DataFile> allDataFiles = 
				currentExperiment.getDataFilesForPipeline(activeDataPipeline, false);
		Set<DataFile> worklistDataFiles = newWorklist.getWorklistItems().stream().
				map(i -> i.getDataFile()).collect(Collectors.toSet());
		
		//	update timestamps
		for(DataFile df : allDataFiles) {
			
			for(DataFile wklFile : worklistDataFiles) {
				
				if(df.equals(wklFile)) {
					
					df.setInjectionTime(wklFile.getInjectionTime());
//					df.setInjectionVolume(wklFile.getInjectionVolume());
				}
			}
		}		
		List<DataFile> missingInWorklistFiles = 
				allDataFiles.stream().filter(f -> !worklistDataFiles.contains(f)).
				sorted().collect(Collectors.toList());
		
		List<DataFile> missingInExperimentFiles = 
				worklistDataFiles.stream().filter(f -> !allDataFiles.contains(f)).
				sorted().collect(Collectors.toList());
		if(!missingInExperimentFiles.isEmpty()) {
			
			List<WorklistItem> newItems = newWorklist.getWorklistItems().
					stream().filter(i -> allDataFiles.contains(i.getDataFile())).
					collect(Collectors.toList());
			newWorklist.getWorklistItems().clear();
			newWorklist.getWorklistItems().addAll(newItems);
		}
		if(!eTask.isAppendWorklist())
			currentExperiment.setWorklistForAcquisitionMethod(
					eTask.getDataAcquisitionMethod(), newWorklist);
		else
			currentExperiment.getWorklistForDataAcquisitionMethod(
					eTask.getDataAcquisitionMethod()).appendWorklist(newWorklist);
		
		switchDataPipeline(currentExperiment, activeDataPipeline);
		
		if(!missingInWorklistFiles.isEmpty()) {
			
			List<String> fileNames = missingInWorklistFiles.stream().
					map(f -> f.getName()).sorted().collect(Collectors.toList());
			
			MessageDialog.showWarningMsg(
					"Worklist data missing for:\n" + StringUtils.join(fileNames, "\n"), 
					this.getContentPane());
		}
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {

		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}




























