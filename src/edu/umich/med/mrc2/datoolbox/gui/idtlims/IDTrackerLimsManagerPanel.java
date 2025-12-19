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

package edu.umich.med.mrc2.datoolbox.gui.idtlims;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.intern.CDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSClient;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.BinnerDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.column.DockableChromatographicColumnManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.DockableAcquisitionMethodManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph.DockableMobilePhaseManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport.IDTrackerMultiFileMSMSDataImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dataimport.IDtrackerAverageMsOneImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.design.DockableIDTrackerExperimentDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr.DockableDataExtractionMethodManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.experiment.IDTrackerExperimentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument.DockableInstrumentManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.prep.DockableSamplePrepManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.project.IDTrackerProjectDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.projecttree.DockableIdProjectTree;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.projecttree.IdProjectTree;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.results.DockableIDTrackerResultsOverviewPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.software.DockableSoftwareManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.sop.DockableSOPProtocolsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.DockableStockSampleManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.vendor.DockableVendorManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.IDTrackerDataLoadWizard;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist.DockableWorklistManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.CefMsMsPrescanSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerMetadataPullTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSSearchResultsBatchPrescanTask;

public class IDTrackerLimsManagerPanel extends DockableMRC2ToolboxPanel implements ItemListener, TreeSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -9147078549907652550L;
	
	private static final Icon componentIcon = GuiUtils.getIcon("idTrackerDatabase", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon newCdpIdProjectIcon = GuiUtils.getIcon("newIdProject", 24);
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 24);
	private static final Icon loadMultiFileIcon = GuiUtils.getIcon("importMultifile", 24);
	private static final Icon loadAvgMS1DataFileIcon = GuiUtils.getIcon("importTextfile", 24);
	private static final Icon importBinnerDataIcon = GuiUtils.getIcon("importBins", 24);
	private static final Icon scanCefIcon = GuiUtils.getIcon("scanCef", 24);
	private static final Icon wizardIcon = GuiUtils.getIcon("wizard", 24);
	
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "IDTrackerLimsManager.layout");

	private DockableIdProjectTree projectTreePanel;
	private DockableIDTrackerExperimentDesignEditorPanel designEditor;
	private DockableStockSampleManagerPanel stockSampleManagerPanel;
	private DockableInstrumentManagerPanel instrumentManagerPanel;
	private DockableAcquisitionMethodManagerPanel acquisitionMethodManagerPanel;	
	private DockableVendorManagerPanel vendorManagerPanel;
	private DockableSoftwareManagerPanel softwareManagerPanel;
	private DockableMobilePhaseManagerPanel mobilePhaseManagerPanel;
	private DockableChromatographicColumnManagerPanel cromatographicColumnManagerPanel;
	private DockableDataExtractionMethodManagerPanel dataExtractionMethodManagerPanel;
	private DockableSOPProtocolsManagerPanel protocolManagerPanel;
	private DockableSamplePrepManagerPanel samplePrepManagerPanel;
	private DockableWorklistManagerPanel instrumentSequenceManagerPanel;
	private DockableIDTrackerResultsOverviewPanel resultsOverviewPanel;
	private IDTrackerProjectDialog projectDialog;
	private IDTrackerExperimentDialog experimentDialog;
	private IDTrackerMultiFileMSMSDataImportDialog multiFileDataImportDialog;
	private IndeterminateProgressDialog idp;
	private IDtrackerAverageMsOneImportDialog idtrackerAverageMsOneImportDialog;

	private CefMsMsPrescanSetupDialog cefMsMsPrescanSetupDialog;
	private IDTrackerDataLoadWizard idTrackerDataLoadWizard;
	private BinnerDataImportDialog binnerDataImportDialog;

	public enum ImportType {
		MS1, IDDA;
	}

	public IDTrackerLimsManagerPanel() {

		super("IDTrackerLimsManagerPanel", PanelList.ID_TRACKER_LIMS.getName(), componentIcon);
		
		menuBar = new IDTrackerLimsManagerMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		projectTreePanel = new DockableIdProjectTree(this);
		projectTreePanel.getTree().addTreeSelectionListener(this);
		designEditor = new DockableIDTrackerExperimentDesignEditorPanel(this);
		stockSampleManagerPanel = new DockableStockSampleManagerPanel(this);
		instrumentManagerPanel = new DockableInstrumentManagerPanel(this);
		acquisitionMethodManagerPanel = new DockableAcquisitionMethodManagerPanel(this);
		vendorManagerPanel = new DockableVendorManagerPanel(this);
		softwareManagerPanel = new DockableSoftwareManagerPanel(this);
		mobilePhaseManagerPanel = new DockableMobilePhaseManagerPanel(this);
		cromatographicColumnManagerPanel = new DockableChromatographicColumnManagerPanel(this);
		dataExtractionMethodManagerPanel = new DockableDataExtractionMethodManagerPanel(this);
		protocolManagerPanel = new DockableSOPProtocolsManagerPanel(this);
		samplePrepManagerPanel = new DockableSamplePrepManagerPanel(this);
		instrumentSequenceManagerPanel = new DockableWorklistManagerPanel(this);
		resultsOverviewPanel = new DockableIDTrackerResultsOverviewPanel(this);

		grid.add(0, 0, 75, 100, designEditor, stockSampleManagerPanel, instrumentManagerPanel,
				acquisitionMethodManagerPanel, vendorManagerPanel, softwareManagerPanel, 
				mobilePhaseManagerPanel, cromatographicColumnManagerPanel,
				dataExtractionMethodManagerPanel, protocolManagerPanel, samplePrepManagerPanel,
				instrumentSequenceManagerPanel, resultsOverviewPanel);

		grid.add(-25, 0, 25, 100, projectTreePanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);

		control.getController().setFocusedDockable(designEditor.intern(), true);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}
	
	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(), 
				refreshDataIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND.getName(),
				MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND.getName(), 
				newCdpIdProjectIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName(), 
				newCdpIdExperimentIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND.getName(),
				MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND.getName(), 
				wizardIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND.getName(),
				MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND.getName(), 
				scanCefIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName(),
				MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName(), 
				loadMultiFileIcon, this));
		
		SimpleButtonAction avgMS1action = GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName(),
				MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName(), 
				loadAvgMS1DataFileIcon, this);
		avgMS1action.setEnabled(false);
		menuActions.add(avgMS1action);
		
		menuActions.addSeparator();
		
		SimpleButtonAction importBinnerDataAction = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(),
				MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName(), 
				importBinnerDataIcon, this);
		importBinnerDataAction.setEnabled(false);
		menuActions.add(importBinnerDataAction);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(e);

		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName()))
			refreshIdTrackerdata();
		
		if (command.equals(MainActionCommands.SHOW_DATA_UPLOAD_WIZARD_COMMAND.getName()))
			showDataUploadWizard();
	
		if (command.equals(MainActionCommands.NEW_IDTRACKER_PROJECT_DIALOG_COMMAND.getName()))
			showProjectEditDialog(null);

		if (command.equals(MainActionCommands.EDIT_IDTRACKER_PROJECT_DIALOG_COMMAND.getName())) {

			LIMSProject project = getSelectedProject();
			if (project == null)
				return;

			showProjectEditDialog(project);
		}
		if (command.equals(MainActionCommands.SAVE_IDTRACKER_PROJECT_COMMAND.getName())
				|| command.equals(MainActionCommands.NEW_IDTRACKER_PROJECT_COMMAND.getName()))
			saveProject();

		if (command.equals(MainActionCommands.DELETE_IDTRACKER_PROJECT_COMMAND.getName())) {
			
			LIMSProject project = getSelectedProject();
			if (project == null)
				return;
			
			reauthenticateAdminCommand(MainActionCommands.DELETE_IDTRACKER_PROJECT_COMMAND.getName());
		}

		if (command.equals(MainActionCommands.NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName()))
			showExperimentEditDialog(null);

		if (command.equals(MainActionCommands.EDIT_IDTRACKER_EXPERIMENT_DIALOG_COMMAND.getName()))
			showExperimentEditDialog(getSelectedExperiment());
		
		if (command.equals(MainActionCommands.SAVE_IDTRACKER_EXPERIMENT_COMMAND.getName())
				|| command.equals(MainActionCommands.NEW_IDTRACKER_EXPERIMENT_COMMAND.getName()))
			saveExperiment();

		if (command.equals(MainActionCommands.DELETE_IDTRACKER_EXPERIMENT_COMMAND.getName())) {
			
			LIMSExperiment experiment = getSelectedExperiment();
			if (experiment == null)
				return;
			
			reauthenticateAdminCommand(MainActionCommands.DELETE_IDTRACKER_EXPERIMENT_COMMAND.getName());//	deleteExperiment();
		}

		if (command.equals(MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName()))
			editSamplePrepDialog();
		
		if (command.equals(MainActionCommands.ADD_SAMPLE_PREP_DIALOG_COMMAND.getName()))
			newSamplePrepDialog();

		if (command.equals(MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName()))
			deleteSamplePrep();

		if (command.equals(MainActionCommands.LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND.getName()))
			openMultifileLoader();

		if (command.equals(MainActionCommands.LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND.getName()))
			openAverageMS1Loader();

		if (command.equals(MainActionCommands.CEF_MSMS_SCAN_SETUP_COMMAND.getName()))
			setupCefMsMsPrescan();

		if (command.equals(MainActionCommands.CEF_MSMS_SCAN_RUN_COMMAND.getName()))
			runCefMsMsPrescan();
		
		if (command.equals(MainActionCommands.ADD_DATA_PROCESSING_RESULTS_DIALOG_COMMAND.getName()))
			showAddDataProcessingResultsDialog();
		
		if (command.equals(MainActionCommands.ADD_DATA_PROCESSING_RESULTS_COMMAND.getName()))
			addDataProcessingResults();	
		
		if (command.equals(MainActionCommands.SHOW_BINNER_REPORT_IMPORT_DIALOG.getName()))
			showBinnerReportImportDialog();	
		
		if (command.equals(MainActionCommands.IMPORT_BINNER_DATA_COMMAND.getName()))
			importBinnerData();		
	}
	
	private void showBinnerReportImportDialog() {
		
		LIMSExperiment experiment = getSelectedExperiment();
		if (experiment == null) {
			MessageDialog.showWarningMsg("Please select experiment in the tree!", this.getContentPane());
			return;
		}
		if(!experimentHasMS1data(experiment)) {
			MessageDialog.showWarningMsg("Selected experiment \""
					+ experiment.getName() + "\" does not have MS1 data"
					+ " to associate with Binner report.", this.getContentPane());
			return;
		}
		binnerDataImportDialog = new BinnerDataImportDialog(this);
		binnerDataImportDialog.setLocationRelativeTo(this.getContentPane());
		binnerDataImportDialog.setVisible(true);
	}
	
	private void importBinnerData(){
		
		File binnerReportFile = binnerDataImportDialog.getBinnerReportFile();
		File postProcessorReportFile = binnerDataImportDialog.getPostProcessorReportFile();
		if(binnerReportFile == null) {
			MessageDialog.showErrorMsg(
					"Binner report file has to be specified.", binnerDataImportDialog);
			return;
		}
		
		binnerDataImportDialog.dispose();
	}
	
	private boolean experimentHasMS1data(LIMSExperiment experiment) {
		
		int countMS1features = 0;
		try {
			countMS1features = IDTUtils.getMS1featureCountForExperiment(experiment.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(countMS1features > 0)
			return true;
		else
			return false;
	}
	private void showAddDataProcessingResultsDialog() {
		
		MessageDialog.showInfoMsg("TODO", this.getContentPane());
	}
	
	private void addDataProcessingResults() {
		
		MessageDialog.showInfoMsg("TODO-2", this.getContentPane());
	}

	private void showDataUploadWizard() {

		idTrackerDataLoadWizard = new IDTrackerDataLoadWizard(this);
		idTrackerDataLoadWizard.setLocationRelativeTo(this.getContentPane());
		idTrackerDataLoadWizard.setVisible(true);
	}

	private void setupCefMsMsPrescan() {

		cefMsMsPrescanSetupDialog = new CefMsMsPrescanSetupDialog(this);
		cefMsMsPrescanSetupDialog.setLocationRelativeTo(this.getContentPane());
		cefMsMsPrescanSetupDialog.setVisible(true);
	}

	private void runCefMsMsPrescan() {

		File cefDir = cefMsMsPrescanSetupDialog.getCefDirectory();
		File cpdFile = cefMsMsPrescanSetupDialog.getCpdFile();
		if (cefDir == null || cpdFile == null) {
			MessageDialog.showErrorMsg("Directory with CEF files and/or compound data output file not specified.",
					cefMsMsPrescanSetupDialog);
			return;
		}
		List<Path> cefList = null;
		try {
			cefList = Files
					.find(Paths.get(cefDir.getAbsolutePath()), Integer.MAX_VALUE,
							(filePath,
									fileAttr) -> (filePath.toString().endsWith(".CEF")
											|| filePath.toString().endsWith("cef")) && fileAttr.isRegularFile())
					.sorted().collect(Collectors.toList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cefList == null) {
			MessageDialog.showErrorMsg("Unable to read CEF files from specified directory.",
					cefMsMsPrescanSetupDialog);
			return;
		}
		if (cefList.isEmpty()) {
			MessageDialog.showErrorMsg("No CEF files found in specified directory.", cefMsMsPrescanSetupDialog);
			return;
		}
		List<File> fileList = cefList.stream().map(p -> p.toFile()).collect(Collectors.toList());
		MSMSSearchResultsBatchPrescanTask task = new MSMSSearchResultsBatchPrescanTask(fileList, cpdFile);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		cefMsMsPrescanSetupDialog.dispose();
	}

	private void openAverageMS1Loader() {

		LIMSExperiment experiment = getSelectedExperiment();
		if (experiment == null) {
			MessageDialog.showWarningMsg("Please select experiment in the tree!", this.getContentPane());
			return;
		}
		idtrackerAverageMsOneImportDialog = new IDtrackerAverageMsOneImportDialog(experiment);
		idtrackerAverageMsOneImportDialog.setLocationRelativeTo(this.getContentPane());
		idtrackerAverageMsOneImportDialog.setVisible(true);
	}

	public void updateProjetTreeNodeForObject(Object o) {

		if (o == null)
			return;

		projectTreePanel.updateNodeForObject(o);
	}

	private void openMultifileLoader() {
		
		DefaultMutableTreeNode selectedNode = 
				(DefaultMutableTreeNode) projectTreePanel.getTree().getLastSelectedPathComponent();
		if(selectedNode == null) {
			MessageDialog.showWarningMsg(
					"Please select experiment in the tree!", 
					this.getContentPane());
			return;
		}			
		LIMSExperiment experiment = projectTreePanel.getExperimentForNode(selectedNode);
		//LIMSExperiment experiment = getSelectedExperiment();
		if (experiment == null) {
			MessageDialog.showWarningMsg(
					"Please select experiment in the tree!", 
					this.getContentPane());
			return;
		}
		multiFileDataImportDialog = new IDTrackerMultiFileMSMSDataImportDialog(experiment);
		multiFileDataImportDialog.setLocationRelativeTo(this.getContentPane());
		multiFileDataImportDialog.setVisible(true);
	}

	public void showProjectEditDialog(LIMSProject project) {

		projectDialog = new IDTrackerProjectDialog(project, this);
		projectDialog.setLocationRelativeTo(this.getContentPane());
		projectDialog.setVisible(true);
	}

	public void showExperimentEditDialog(LIMSExperiment experiment) {

//		if (experiment == null)
//			return;
		
		experimentDialog = new IDTrackerExperimentDialog(experiment, this);
		experimentDialog.setLocationRelativeTo(this.getContentPane());
		experimentDialog.setVisible(true);
	}

	private void deleteProject() {

		LIMSProject project = getSelectedProject();
		if (project == null)
			return;

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete project \"" + project.getName() + "\"?\n"
						+ "All associated experiments and data will be purged from the database!",
				this.getContentPane());

		if (result == JOptionPane.YES_OPTION) {

			DeleteTrackerProjectTask task = 
					new DeleteTrackerProjectTask(project);
			idp = new IndeterminateProgressDialog(
					"Deleting data for project "+ project.getName() + 
					" ...", this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
		}
	}

	private void deleteExperiment() {

		LIMSExperiment experiment = getSelectedExperiment();
		if (experiment == null)
			return;

		int result = MessageDialog.showChoiceWithWarningMsg("Do you really want to delete experiment \""
				+ experiment.getName() + "\"?\n" + "All associated data will be purged from the database!",
				this.getContentPane());

		if (result == JOptionPane.YES_OPTION) {

			DeleteTrackerExperimentTask task = 
					new DeleteTrackerExperimentTask(experiment);
			idp = new IndeterminateProgressDialog(
					"Deleting data for experiment "+ experiment.getName() + 
					" ...", this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
		}
	}
	
	class DeleteTrackerExperimentTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private LIMSExperiment experiment;

		public DeleteTrackerExperimentTask(LIMSExperiment experiment) {
			this.experiment = experiment;
		}

		@Override
		public Void doInBackground() {

			try {
				IDTUtils.deleteExperiment(experiment);
				refreshIdTrackerdata();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	class DeleteTrackerProjectTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private LIMSProject project;

		public DeleteTrackerProjectTask(LIMSProject project) {
			this.project = project;
		}
		
		@Override
		public Void doInBackground() {

			try {
				IDTUtils.deleteProject(project);
				refreshIdTrackerdata();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	private void editSamplePrepDialog() {

		LIMSSamplePreparation samplePrep = projectTreePanel.getSelectedSamplePrep();
		if (samplePrep == null)
			return;

		samplePrepManagerPanel.showEditSamplePrepDialog(samplePrep);
	}
	
	private void newSamplePrepDialog() {
		
		LIMSExperiment selectedExperiment = projectTreePanel.getSelectedExperiment();
		if (selectedExperiment == null)
			return;
		
		samplePrepManagerPanel.showCreateNewPrepDialog(selectedExperiment);		
	}

	private void deleteSamplePrep() {
		samplePrepManagerPanel.deleteSamplePrep(projectTreePanel.getSelectedSamplePrep());
	}

	public LIMSProject getSelectedProject() {
		return projectTreePanel.getSelectedProject();
	}

	public LIMSExperiment getSelectedExperiment() {
		return projectTreePanel.getSelectedExperiment();
	}

	public LIMSSamplePreparation getSelectedSamplePrep() {
		return projectTreePanel.getSelectedSamplePrep();
	}

	public DataAcquisitionMethod getSelectedAcquisitionMethod() {
		return projectTreePanel.getSelectedAcquisitionMethod();
	}

	public DataExtractionMethod getSelectedDataExtractionMethod() {
		return projectTreePanel.getSelectedDataExtractionMethod();
	}

	public void refreshIdTrackerdata() {
		
		//	clearPanel();
		IDTrackerMetadataPullTask task = new IDTrackerMetadataPullTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	public void selectExperiment(LIMSExperiment experiment) {
		projectTreePanel.selectNodeForObject(experiment);
	}

	public void selectProject(LIMSProject project) {
		projectTreePanel.selectNodeForObject(project);
	}
	
	public void refreshPanelData() {

		projectTreePanel.loadIdTrackerData();
		stockSampleManagerPanel.loadStockSamples();
		instrumentManagerPanel.loadInstruments();
		acquisitionMethodManagerPanel.loadAcquisitionMethods();
		mobilePhaseManagerPanel.loadMobilePhases();
		cromatographicColumnManagerPanel.loadColumnData();
		dataExtractionMethodManagerPanel.loadMethods();
		protocolManagerPanel.loadProtocolData();
		samplePrepManagerPanel.loadPrepData();	
		vendorManagerPanel.loadVendorList();
		softwareManagerPanel.loadSoftwareList();
	}
	
	public void refreshVendorList() {
		IDTDataCache.refreshManufacturers();
		vendorManagerPanel.loadVendorList();
	}

	public void reloadProjectTree() {
		projectTreePanel.loadIdTrackerData();
	}

	private void saveProject() {

		Collection<String> errors = validateProjectDefinition();
		if (!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), projectDialog);
			return;
		}
		// Create new project
		if (projectDialog.getProject() == null) {
			
			LIMSClient client = 
					LIMSDataCache.getLIMSClientForUser(MRC2ToolBoxCore.getIdTrackerUser());

			LIMSProject newProject = new LIMSProject(
					projectDialog.getProjectName(),
					projectDialog.getProjectDescription(), 
					projectDialog.getProjectNotes(),
					client);
			try {
				String projectId = IDTUtils.addNewProject(newProject);
				newProject.setId(projectId);
				IDTDataCache.getProjects().add(newProject);
				projectTreePanel.addObject(newProject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else { // Update existing project
			try {
				IDTUtils.updateProject(
						projectDialog.getProjectName(), 
						projectDialog.getProjectDescription(),
						projectDialog.getProjectNotes(), 
						projectDialog.getProject().getId());
				projectTreePanel.updateNodeForObject(projectDialog.getProject());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		projectDialog.dispose();
	}
	
	private Collection<String>validateProjectDefinition(){
		
		ArrayList<String> errors = new ArrayList<String>();
		if (projectDialog.getProjectName().isEmpty())
			errors.add("Project name can not be empty.");
		
		return errors;
	}

	private void saveExperiment() {

		Collection<String> errors = experimentDialog.validateExperimentDefinition();
		if (!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), experimentDialog);
			return;
		}
		// Add new experiment
		if (experimentDialog.getExperiment() == null) {

			LIMSExperiment newExperiment = new LIMSExperiment(
					experimentDialog.getExperimentName(),
					experimentDialog.getExperimentDescription(), 
					experimentDialog.getExperimentNotes(),
					experimentDialog.getExperimentProject());
			newExperiment.setCreator(MRC2ToolBoxCore.getIdTrackerUser());
			try {
				String experimentId = IDTUtils.addNewExperiment(newExperiment);				
				newExperiment.setId(experimentId);
				IDTUtils.setInstrumentForExperiment(
						newExperiment, experimentDialog.getInstrument());
						
				IDTDataCache.getExperiments().add(newExperiment);
				IDTDataCache.getExperimentInstrumentMap().put(
						newExperiment, experimentDialog.getInstrument());
				
				experimentDialog.getExperimentProject().getExperiments().add(newExperiment);
				projectTreePanel.addObject(newExperiment);	
				projectTreePanel.expandNodeForObject(newExperiment.getProject());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // Update existing project
			try {
				IDTUtils.updateExperiment(
						experimentDialog.getExperimentName(),
						experimentDialog.getExperimentDescription(), 
						experimentDialog.getExperimentNotes(),
						experimentDialog.getExperimentProject().getId(),
						experimentDialog.getExperiment().getId());
				
				IDTUtils.updateInstrumentForExperiment(
						experimentDialog.getExperiment(), experimentDialog.getInstrument());
				IDTDataCache.getExperimentInstrumentMap().put(
						experimentDialog.getExperiment(), experimentDialog.getInstrument());
				
				LIMSExperiment experiment = experimentDialog.getExperiment();
				if(!experiment.getProject().equals(experimentDialog.getExperimentProject())) {
					projectTreePanel.loadIdTrackerData();
				}
				else {
					experiment.setName(experimentDialog.getExperimentName());
					experiment.setDescription(experimentDialog.getExperimentDescription());
					experiment.setNotes(experimentDialog.getExperimentNotes());
					projectTreePanel.updateNodeForObject(experiment);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		experimentDialog.dispose();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		if (e.getSource() instanceof IdProjectTree) {

			IdProjectTree tree = (IdProjectTree) e.getSource();
			DefaultMutableTreeNode selectedNode = 
					(DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode == null)
				return;

			if (LIMSExperiment.class.isAssignableFrom(selectedNode.getUserObject().getClass())) 
				displayExperimentData((LIMSExperiment) selectedNode.getUserObject());
			
			if (LIMSSamplePreparation.class.isAssignableFrom(selectedNode.getUserObject().getClass())) 
				displaySamplePrepData((LIMSSamplePreparation) selectedNode.getUserObject());
			
			if(DataAcquisitionMethod.class.isAssignableFrom(selectedNode.getUserObject().getClass())) {
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
				if (LIMSSamplePreparation.class.isAssignableFrom(parent.getUserObject().getClass()))
					displaySamplePrepData((LIMSSamplePreparation) parent.getUserObject());
			}			
			if(DataExtractionMethod.class.isAssignableFrom(selectedNode.getUserObject().getClass())) {
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent().getParent();
				if (LIMSSamplePreparation.class.isAssignableFrom(parent.getUserObject().getClass()))
					displaySamplePrepData((LIMSSamplePreparation) parent.getUserObject());
			}
		}
	}
	
	public void displayExperimentData(LIMSExperiment experiment) {
		
		LIMSSamplePreparation selectedPrep = samplePrepManagerPanel.getSelectedPrep();
		
		if(selectedPrep == null 
				|| !IDTDataCache.getExperimentForSamplePrep(selectedPrep).equals(experiment)) {
			samplePrepManagerPanel.clearCurrentPrepData();
			instrumentSequenceManagerPanel.clearPanel();
		}
		designEditor.loadExperiment(experiment);
		LIMSSamplePreparation prep = null;
		if(!experiment.getSamplePreps().isEmpty())
			prep = experiment.getSamplePreps().iterator().next();
		
		if(prep != null) {
			samplePrepManagerPanel.selectSamplePrep(prep);
			instrumentSequenceManagerPanel.loadPrepWorklist(prep, experiment);
		}
	}
	
	public void displaySamplePrepData(LIMSSamplePreparation prep) {
		
		samplePrepManagerPanel.clearCurrentPrepData();
		instrumentSequenceManagerPanel.clearPanel();				
		samplePrepManagerPanel.selectSamplePrep(prep);		
		LIMSExperiment experiment = IDTDataCache.getExperimentForSamplePrep(prep);

		// Load parent experiment
		if (experiment != null) {

			if (designEditor.getExperiment() == null || !designEditor.getExperiment().equals(experiment))
				designEditor.loadExperiment(experiment);

			instrumentSequenceManagerPanel.loadPrepWorklist(prep, experiment);
		}
	}
	
	public void loadExperimentForPrep(LIMSSamplePreparation prep) {
		
		if(prep == null)
			return;
		
		LIMSExperiment experiment = IDTDataCache.getExperimentForSamplePrep(prep);
		if (experiment != null) {

			if (designEditor.getExperiment() == null || !designEditor.getExperiment().equals(experiment))
				designEditor.loadExperiment(experiment);

			instrumentSequenceManagerPanel.loadPrepWorklist(prep, experiment);
		}
	}

	public void loadExperiment(LIMSExperiment experiment) {
		designEditor.loadExperiment(experiment);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadLayout(File layoutFile) {

		if (control != null) {

			if (layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if (control != null) {

			for (int i = 0; i < control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if (uiObject instanceof PersistentLayout)
					((PersistentLayout) uiObject).saveLayout(((PersistentLayout) uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MSMSSearchResultsBatchPrescanTask.class))
				finalizeCefPrescan((MSMSSearchResultsBatchPrescanTask) e.getSource());
			
			if (e.getSource().getClass().equals(IDTrackerMetadataPullTask.class))
				finalizeIDTrackerMetadataPullTask((IDTrackerMetadataPullTask)e.getSource());
		}
	}
	
	private synchronized void finalizeIDTrackerMetadataPullTask(IDTrackerMetadataPullTask task) {
		
		refreshPanelData();
		RecentDataManager.readDataFromFile();
		MRC2ToolBoxCore.getMainWindow().updateGuiWithRecentData();
	}

	private synchronized void finalizeCefPrescan(MSMSSearchResultsBatchPrescanTask task) {

		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();

		// Go to report directory
		File reportFile = task.getMissingCompoundsListFile();
		if (reportFile.exists()) {
			MessageDialog.showInfoMsg(
					"Import log file \"" + reportFile.getName() + "\" was created, please review and fix\n"
					+ "the reported problems before uploading the data", 
					this.getContentPane());
			try {
				Desktop.getDesktop().open(reportFile.getParentFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			MessageDialog.showInfoMsg(
					"No information to report, data ready for upload", this.getContentPane());
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
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void clearPanel() {

		designEditor.clearPanel();
		stockSampleManagerPanel.clearPanel();
		instrumentManagerPanel.clearPanel();
		acquisitionMethodManagerPanel.clearPanel();
		mobilePhaseManagerPanel.clearPanel();
		cromatographicColumnManagerPanel.clearPanel();
		dataExtractionMethodManagerPanel.clearPanel();
		protocolManagerPanel.clearPanel();
		samplePrepManagerPanel.clearPanel();
		instrumentSequenceManagerPanel.clearPanel();
		resultsOverviewPanel.clearPanel();
		projectTreePanel.clearPanel();
	}
	
	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}
	
	public DockableIdProjectTree getProjectTreePanel() {
		return projectTreePanel;
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		if (command.equals(MainActionCommands.DELETE_IDTRACKER_EXPERIMENT_COMMAND.getName())) {
			deleteExperiment();
		}
		if (command.equals(MainActionCommands.DELETE_IDTRACKER_PROJECT_COMMAND.getName())) {
			deleteProject();
		}		
	}

	@Override
	public void updateGuiWithRecentData() {
		// TODO Auto-generated method stub
		
	}
}
