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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.LIMSWorklistImportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableWorklistManagerPanel extends AbstractIDTrackerLimsPanel implements TaskListener {

	private Preferences preferences;
	public static final String PREFS_NODE =
			"edu.umich.med.mrc2.cefanalyzer.gui.idtracker.InstrumentSequenceManagerPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private static final Icon componentIcon = GuiUtils.getIcon("table", 16);
	private static final Icon loadWorklistFromFileIcon = GuiUtils.getIcon("loadWorklist", 24);
	private static final Icon addWorklistFromFileIcon = GuiUtils.getIcon("addWorklist", 24);
	private static final Icon scanDirIcon = GuiUtils.getIcon("scanFolder", 24);
	private static final Icon addFromDirIcon = GuiUtils.getIcon("addFromFolder", 24);
	private static final Icon clearWorklistIcon = GuiUtils.getIcon("clearWorklist", 24);
	private static final Icon saveWorklistIcon = GuiUtils.getIcon("saveWorklist", 24);
	private static final Icon copyWorklistToClipboardIcon = GuiUtils.getIcon("copyWorklistToClipboard", 24);

	//	private InstrumentSequenceManagerToolbar toolbar;
	private IDTrackerWorklistPanelMenuBar menuBar;
	private InstrumentSequenceTable worklistTable;
	private File baseDirectory;
	private boolean replaceExistingWorklist;
	private LIMSSamplePreparation activeSamplePrep;
	private LIMSExperiment experiment;
	private IndeterminateProgressDialog idp;
	private InstrumentSequenceImportDialog instrumentSequenceImportDialog;
	private Worklist wkl;

	public DockableWorklistManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, 
				"DockableInstrumentSequenceManagerPanel", 
				componentIcon, "Instrument sequences", null, 
				Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		menuBar = new IDTrackerWorklistPanelMenuBar(this);
		//	toolbar = new InstrumentSequenceManagerToolbar(this);
		getContentPane().add(menuBar, BorderLayout.NORTH);

		worklistTable = new InstrumentSequenceTable();
		JScrollPane designScrollPane = new JScrollPane(worklistTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);

		replaceExistingWorklist = false;
		initActions();
		loadPreferences();
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
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(),
				MainActionCommands.LOAD_WORKLIST_COMMAND.getName(), 
				loadWorklistFromFileIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(),
				MainActionCommands.ADD_WORKLIST_COMMAND.getName(), 
				addWorklistFromFileIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(),
				MainActionCommands.CLEAR_WORKLIST_COMMAND.getName(), 
				clearWorklistIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(),
				MainActionCommands.SAVE_WORKLIST_COMMAND.getName(), 
				saveWorklistIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(),
				MainActionCommands.COPY_WORKLIST_COMMAND.getName(), 
				copyWorklistToClipboardIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		if(experiment == null || activeSamplePrep == null)
			return;

		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.LOAD_WORKLIST_COMMAND.getName()))
			loadWorklistFromFile(true);

		if (command.equals(MainActionCommands.ADD_WORKLIST_COMMAND.getName()))
			loadWorklistFromFile(false);

		if (command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(true);

		if (command.equals(MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(false);

		if (command.equals(MainActionCommands.SEND_WORKLIST_TO_DATABASE.getName()))
			sendWorklistToDatabase();

		if (command.equals(MainActionCommands.SAVE_WORKLIST_COMMAND.getName()))
			saveWorklistToFile();

		if (command.equals(MainActionCommands.COPY_WORKLIST_COMMAND.getName()))
			copyWorklistToClipboard();

		if (command.equals(MainActionCommands.CLEAR_WORKLIST_COMMAND.getName()))
			clearWorklist();
	}

	private void sendWorklistToDatabase() {

		Worklist newWorklist = instrumentSequenceImportDialog.getWorklist();
		Collection<LIMSWorklistItem>items =
				newWorklist.getTimeSortedWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).
				collect(Collectors.toList());

		//	Verify worklist
		ArrayList<String>errors = new ArrayList<String>();
		if(items.stream().filter(i -> i.getSample() == null).count() > 0)
			errors.add("Some data files not linked to samples.");

		if(items.stream().filter(i -> i.getPrepItemId() == null).count() > 0)
			errors.add("Some data files not linked to sample prep items.");

		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), instrumentSequenceImportDialog);
			return;
		}
		//	Upload data
		try {
			IDTUtils.uploadInjectionData(newWorklist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCash.refreshSamplePrepDataPipelineMap();
		idTrackerLimsManager.reloadProjectTree();
		instrumentSequenceImportDialog.dispose();
		loadPrepWorklist(activeSamplePrep, experiment);
	}

	public void loadLimsWorklist(Worklist wkl) {

		this.wkl = wkl;
		worklistTable.populateTableFromWorklistExperimentAndSamplePrep(wkl, experiment, activeSamplePrep);
	}

	private void copyWorklistToClipboard() {

		String worklistString = worklistTable.getWorklistsAsString();

		if(worklistString == null)
			return;

		if(worklistString.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(worklistString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private void saveWorklistToFile() {

		String worklistString = worklistTable.getWorklistsAsString();
		if(worklistString == null)
			return;

		if(worklistString.isEmpty())
			return;

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Save experiment design to file:");
		chooser.setApproveButtonText("Save design");
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "TXT"));
		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			File outputFile = FIOUtils.changeExtension(chooser.getSelectedFile(), "txt") ;
			try {
				FileUtils.writeStringToFile(outputFile, worklistString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadWorklistFromDirectoryScan(boolean replace) {

		if(activeSamplePrep == null)
			return;

		replaceExistingWorklist = replace;
		int selectedValue = JOptionPane.YES_OPTION;
		if (replace && hasInjectionData(activeSamplePrep)) {

			selectedValue = MessageDialog.showChoiceWithWarningMsg(
					"Sample injection data for sample prep  " + activeSamplePrep.getName() +
					" is already loaded, do you want to replace it?", this.getContentPane());
		}
		if(selectedValue == JOptionPane.YES_OPTION ) {

			try {
				scanDirectoryForSampleInfo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//	TODO
	private void loadWorklistFromFile(boolean replace) {

		if(activeSamplePrep == null)
			return;

		replaceExistingWorklist = replace;
		int selectedValue = JOptionPane.YES_OPTION;
		if (replace && hasInjectionData(activeSamplePrep)) {

			selectedValue = MessageDialog.showChoiceWithWarningMsg(
					"Sample injection data for sample prep  " + activeSamplePrep.getName() +
					" is already loaded, do you want to replace it?", this.getContentPane());
		}
		if(selectedValue == JOptionPane.YES_OPTION ) {

			//	TODO
		}
	}

	private boolean hasInjectionData(LIMSSamplePreparation activeSamplePrep2) {
		// TODO Auto-generated method stub
		return false;
	}

	public synchronized void clearPanel() {
		worklistTable.clearTable();
		activeSamplePrep = null;
	}

	private void clearWorklist() {

		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		int selectedValue = MessageDialog.showChoiceWithWarningMsg(
			"Do you want to clear current worklist?\n" +
			"All downstream data and analysis results will be deleted!",  this.getContentPane());

		if (selectedValue == JOptionPane.YES_OPTION) {

			//	TODO
		}
	}

	private void scanDirectoryForSampleInfo() throws Exception {

		File dirToScan = selectRawFilesDirectory();
		if(dirToScan == null)
			return;

		LIMSWorklistImportTask wlit =
			new LIMSWorklistImportTask(
					dirToScan,
					WorklistImportType.RAW_DATA_DIRECTORY_SCAN,
					experiment,
					activeSamplePrep);
		wlit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(wlit);
	}

	private File selectRawFilesDirectory() {

		JFileChooser chooser = new ImprovedFileChooser();
		File inputFile = null;

		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select folder containing data files:");
		chooser.setCurrentDirectory(baseDirectory);

		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			inputFile = chooser.getSelectedFile();

			if (inputFile.exists()) {
				baseDirectory = inputFile.getParentFile();
				savePreferences();
			}
		}
		return inputFile;
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

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(LIMSWorklistImportTask.class))
				loadImportedWorklistForReview((LIMSWorklistImportTask)e.getSource());
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}

	private void loadImportedWorklistForReview(LIMSWorklistImportTask task) {

		if(!task.getMissingMethods().isEmpty()) {

			InformationDialog id = new InformationDialog(
					"Missing acquisition methods",
					"Some acquisition methods used for this experiment are not yet in the database.",
					StringUtils.join(task.getMissingMethods(), "\n"),
					this.getContentPane());
			return;
		}
		if(task.getWorklist() == null) {
			MessageDialog.showErrorMsg("Error creating worklist,", this.getContentPane());
			return;
		}
		instrumentSequenceImportDialog = new InstrumentSequenceImportDialog(this);
		instrumentSequenceImportDialog.loadWorklist(task.getWorklist(), experiment, activeSamplePrep);
		instrumentSequenceImportDialog.setLocationRelativeTo(this.getContentPane());
		instrumentSequenceImportDialog.setVisible(true);
	}

	/**
	 * @return the activeSamplePrep
	 */
	public LIMSSamplePreparation getActiveSamplePrep() {
		return activeSamplePrep;
	}

	/**
	 * @param activeSamplePrep the activeSamplePrep to set
	 */
	public void setActiveSamplePrep(LIMSSamplePreparation activeSamplePrep) {
		this.activeSamplePrep = activeSamplePrep;
	}

	public void loadPrepWorklist(LIMSSamplePreparation prep, LIMSExperiment exp) {
		// TODO Auto-generated method stub
		this.experiment = exp;
		this.activeSamplePrep = prep;

		LoadExperimentWorklistTask task = new LoadExperimentWorklistTask();
		idp = new IndeterminateProgressDialog("Loading worklist ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class LoadExperimentWorklistTask extends LongUpdateTask {

		@Override
		public Void doInBackground() {

			Worklist wkl = null;
			try {
				wkl = IDTUtils.getLimsWorlkistForPrep(activeSamplePrep, experiment);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(wkl != null)
				loadLimsWorklist(wkl);

			return null;
		}
	}
}































