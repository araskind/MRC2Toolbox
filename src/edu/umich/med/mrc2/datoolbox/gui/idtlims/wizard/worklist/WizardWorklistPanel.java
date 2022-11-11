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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.worklist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.IDTrackerDataLoadWizard;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.IDTrackerDataLoadWizardPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist.BatchSampleAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist.InstrumentSequenceTable;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist.WorklistImportPopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.LIMSWorklistImportTask;

public class WizardWorklistPanel extends IDTrackerDataLoadWizardPanel 
		implements ActionListener, BackedByPreferences, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;

	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;

	private LIMSSamplePreparation samplePrep;
	private LIMSExperiment experiment;
	private Worklist worklist;
	private boolean appendWorklist;

	private InstrumentSequenceTable instrumentSequenceTable;
	private WorklistImportToolbar toolbar;
	private BatchSampleAssignmentDialog batchSampleAssignmentDialog;
	
	public WizardWorklistPanel(IDTrackerDataLoadWizard wizard) {
		
		super(wizard);
			
		JPanel panel  = initPanel();
		add(panel, gbc_panel);
		appendWorklist = false;
		
		completeStageButton.setText(
				MainActionCommands.COMPLETE_ANALYSIS_WORKLIST_VERIFICATION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_ANALYSIS_WORKLIST_VERIFICATION_COMMAND.getName());
		
		loadPreferences();
	}

	private JPanel initPanel() {
		
		JPanel panel = new JPanel(new BorderLayout(0,0));
		instrumentSequenceTable = new InstrumentSequenceTable();
		instrumentSequenceTable.addTablePopupMenu(new WorklistImportPopupMenu(this));
		JScrollPane scrollPane = new JScrollPane(instrumentSequenceTable);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		toolbar = new WorklistImportToolbar(this);
		panel.add(toolbar, BorderLayout.NORTH);
		return panel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName())) {
			
			if (instrumentSequenceTable.getRowCount() > 0) {

				int res = MessageDialog.showChoiceWithWarningMsg(
						"Data for some files already loaded, do you want to replace it?", this);
				if(res == JOptionPane.NO_OPTION)
					return;
			}
			loadWorklistFromDirectoryScan(false);
		}
		if (command.equals(MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(true);

		if (command.equals(MainActionCommands.CLEAR_WORKLIST_COMMAND.getName()))
			clearWorklist();
		
		if (command.equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName()))
			showSampleDesignEditor();
		
		if (command.equals(MainActionCommands.ASSIGN_SAMPLE_TO_DATA_FILES_COMMAND.getName()))
			assignSamplesToDataFiles();
				
		if (command.equals(MainActionCommands.DELETE_DATA_FILES_COMMAND.getName()))
			deleteSelectedFiles();		
	}
	
	private void deleteSelectedFiles() {
		
		Collection<DataFile> selectedFiles = instrumentSequenceTable.getSelectedDataFiles();
		if(selectedFiles.isEmpty())
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete selected sata files from the worklist?", this);
		
		if(res == JOptionPane.YES_OPTION) {
			selectedFiles.stream().forEach(f -> worklist.removeDataFile(f));
			instrumentSequenceTable.populateTableFromWorklistExperimentAndSamplePrep(worklist, experiment, samplePrep);
		}
	}
	
	private void showSampleDesignEditor() {
		
		Collection<DataFile> selectedDataFiles = instrumentSequenceTable.getSelectedDataFiles();
		if(selectedDataFiles.isEmpty())
			return;

		batchSampleAssignmentDialog = new BatchSampleAssignmentDialog(
				selectedDataFiles, experiment, samplePrep, this);
		batchSampleAssignmentDialog.setLocationRelativeTo(this);
		batchSampleAssignmentDialog.setVisible(true);
	}
	
	private void assignSamplesToDataFiles() {
		
		Collection<DataFile> selectedFiles = batchSampleAssignmentDialog.getDataFiles();
		ExperimentalSample selectedSample = batchSampleAssignmentDialog.getSelectedSample();
		String selectedPrep = batchSampleAssignmentDialog.getSelectedPrepItem();
		if(selectedSample == null && selectedPrep == null)
			return;

		if(selectedSample != null) {
			worklist.getTimeSortedWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).filter(i -> selectedFiles.contains(i.getDataFile())).
				forEach(i -> i.setSample(selectedSample));
		}
		if(selectedPrep != null) {
			worklist.getTimeSortedWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).filter(i -> selectedFiles.contains(i.getDataFile())).
				forEach(i -> i.setPrepItemId(selectedPrep));
		}
		instrumentSequenceTable.populateTableFromWorklistExperimentAndSamplePrep(worklist, experiment, samplePrep);
		batchSampleAssignmentDialog.dispose();
	}
	
	private void loadWorklistFromDirectoryScan(boolean append) {
		
		appendWorklist = append;
		experiment = wizard.getExperiment();
		samplePrep = wizard.getSamplePrep();
		if(experiment == null || samplePrep == null) {
			MessageDialog.showErrorMsg("Experiment and/or sample preparation definitions are not completed.", this);
			return;
		}
		
//		int selectedValue = JOptionPane.YES_OPTION;
//		if (!append && instrumentSequenceTable.getRowCount() > 0) {
//
//			selectedValue = MessageDialog.showChoiceWithWarningMsg(
//					"Data for some files already loaded, do you want to replace it?", this);
//		}
//		if(selectedValue == JOptionPane.YES_OPTION ) {

			try {
				scanDirectoryForSampleInfo();
			} catch (Exception e) {
				e.printStackTrace();
			}
//		}
	}
	
	private boolean hasInjectionData(LIMSSamplePreparation activeSamplePrep2) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void clearWorklist() {

		if(instrumentSequenceTable.getRowCount() == 0)
			return;
		
		int selectedValue = MessageDialog.showChoiceWithWarningMsg(
			"Do you want to clear current worklist?",  this);

		if (selectedValue == JOptionPane.YES_OPTION) {
			instrumentSequenceTable.clearTable();	
			if(worklist != null)
				worklist.getWorklistItems().clear();
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
					samplePrep);
		wlit.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(wlit);
	}
	
	private File selectRawFilesDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select folder containing raw data files:");
		fc.setMultiSelectionEnabled(false);
		fc.setAllowOverwrite(true);
		fc.setOpenButtonText("Select folder");
		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			
			File rawDataDir = fc.getSelectedFile();
			if (rawDataDir.exists()) {
				baseDirectory = rawDataDir.getParentFile();
				savePreferences();
				return rawDataDir;
			}
		}
		return null;
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

		if(task.getWorklist() == null) {
			MessageDialog.showErrorMsg("Error creating worklist,", this);
			return;
		}
		if(!task.getMissingMethods().isEmpty()) {
			
			String message = 
					"Some acquisition methods used for this experiment "
					+ "are not yet in the database:\n" +
					StringUtils.join(task.getMissingMethods(), "\n") +
					"\nDo you want to import the worklist excluding data files "
					+ "with missing data acquisition methods?";

			if(MessageDialog.showChoiceWithWarningMsg(message, this) == JOptionPane.NO_OPTION)
				return;
		}
		if(worklist == null || !appendWorklist) 
			worklist = new Worklist();
		
		Collection<LIMSWorklistItem>completeItems = 
				task.getWorklist().getWorklistItems().stream().
			filter(LIMSWorklistItem.class::isInstance).
			map(LIMSWorklistItem.class::cast).
			filter(i -> Objects.nonNull(i.getAcquisitionMethod())).
			collect(Collectors.toList());
		
		if(!completeItems.isEmpty())
			worklist.getWorklistItems().addAll(completeItems);
		
		instrumentSequenceTable.populateTableFromWorklistExperimentAndSamplePrep(
				worklist, experiment, samplePrep);
		appendWorklist = false;
	}
	
	public void setSamplePrep(LIMSSamplePreparation samplePrep) {
		this.samplePrep = samplePrep;
	}

	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}

	@Override
	public void loadPreferences(Preferences prefs) {		
		preferences = prefs;		
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));		
	}

	@Override
	public void loadPreferences() {
		loadPreferences(
				Preferences.userRoot().node(WizardWorklistPanel.class.getName()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(WizardWorklistPanel.class.getName());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public Worklist getWorklist() {
		return worklist;
	}

	public Collection<String> validateWorklistData() {

		Collection<String>errors = new ArrayList<String>();
		if(worklist == null || worklist.getTimeSortedWorklistItems().isEmpty()) {
			errors.add("No data files added to instrument sequence.");
			return errors;
		}
		Collection<LIMSWorklistItem>items =
				worklist.getTimeSortedWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).
				collect(Collectors.toList());
		//	Verify if injections are already recorded
		Collection<LIMSWorklistItem>uploadedInjections = new ArrayList<LIMSWorklistItem>();
		try {
			uploadedInjections = IDTUtils.checkForUploadedInjections(items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!uploadedInjections.isEmpty()) {
			errors.add("Injection data for the following files are already in database:");
			for(LIMSWorklistItem item :uploadedInjections) {
				errors.add(item.getDataFile().getName() + "; Injection timestamp " + 
						MRC2ToolBoxConfiguration.getDateTimeFormat().format(item.getTimeStamp()));
			}
			errors.add("Please use IDTracker LIMS panel tools "
					+ "to add MS/MSMS data for existing experiments.");
			return errors;
		}	
		//	Verify sample assignment
		if(items.stream().filter(i -> Objects.isNull(i.getSample())).count() > 0)
			errors.add("Some data files not linked to samples.");

		if(items.stream().filter(i -> Objects.isNull(i.getPrepItemId())).count() > 0)
			errors.add("Some data files not linked to sample prep items.");
	
		return errors;
	}
}
