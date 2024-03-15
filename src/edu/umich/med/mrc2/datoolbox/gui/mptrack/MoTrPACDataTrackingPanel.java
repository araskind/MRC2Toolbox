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

package edu.umich.med.mrc2.datoolbox.gui.mptrack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.DockableExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.MotrpacReferenceDataDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.CreateUploadDirectoryStructureDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.DockableMoTrPACReportListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.MotrpacReportEmptyFileGeneratorDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.MotrpacReportUploadDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACStudyAssayListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACStudyManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACTissueCodeListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.upload.MoTrPACManifestForUploadDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.upload.MoTrPACRawDataCompressionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.AgilentDataCompressionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.CreateUploadManifestTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.MotrpacLimsDataPullTask;

public class MoTrPACDataTrackingPanel extends DockableMRC2ToolboxPanel implements TreeSelectionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("MoTrPAC", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon showMetadataIcon = GuiUtils.getIcon("metadata", 24);
	private static final Icon createFilesIcon = GuiUtils.getIcon("addMultifile", 24);
	private static final Icon uploadReportIcon = GuiUtils.getIcon("addSop", 24);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MoTrPACDataTrackingPanel.layout");

	private MotrpacReferenceDataDialog motrpacReferenceDataDialog;
//	private DockableMotrpacProjectTree projectTree;
	private DockableMoTrPACStudyManagerPanel studyManagerPanel;
	private DockableExperimentListingTable experimentListingTable;
	private DockableMoTrPACTissueCodeListingPanel tissueCodeListingPanel;
	private DockableMoTrPACStudyAssayListingPanel studyAssayListingPanel;
	private DockableMoTrPACReportListingPanel reportListingPanel;
	
	private MotrpacReportUploadDialog motrpacReportUploadDialog;
	private MotrpacReportEmptyFileGeneratorDialog motrpacReportEmptyFileGeneratorDialog;
	private MoTrPACRawDataCompressionSetupDialog motrpacRawDataCompressionSetupDialog;
	private boolean limsDataLoaded;
	private MoTrPACManifestForUploadDialog motrpacManifestForUploadDialog;

	public MoTrPACDataTrackingPanel() {
		
		super("MoTrPACDataTrackingPanel", PanelList.MOTRPAC_REPORT_TRACKER.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new MoTrPACPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);
		
//		projectTree  = new DockableMotrpacProjectTree(this);
		studyManagerPanel = new DockableMoTrPACStudyManagerPanel(this);	
		experimentListingTable = new DockableExperimentListingTable(this);
		tissueCodeListingPanel = new DockableMoTrPACTissueCodeListingPanel();
		tissueCodeListingPanel.getTable().getSelectionModel().addListSelectionListener(this);
		studyAssayListingPanel = new DockableMoTrPACStudyAssayListingPanel();
		studyAssayListingPanel.getTable().getSelectionModel().addListSelectionListener(this);
		reportListingPanel = new DockableMoTrPACReportListingPanel(this);
		
		grid.add(0, 0, 100, 100,
//				projectTree, 
				studyManagerPanel,
				experimentListingTable,
				tissueCodeListingPanel,
				studyAssayListingPanel,
				reportListingPanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		limsDataLoaded = false;
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(), 
				refreshDataIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName(),
				MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName(), 
				showMetadataIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName(),
				MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName(), 
				createFilesIcon, this));
		
		menuActions.addSeparator();
		
		SimpleButtonAction uploadAction = GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName(), 
				uploadReportIcon, this);
		uploadAction.setEnabled(false);
		menuActions.add(uploadAction);		
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

		if(command.equals(MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName()))
			refreshLimsData();

		if(command.equals(MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName()))
			showMetadataReference();

//		if(command.equals(MainActionCommands.SEND_DESIGN_TO_PROJECT_COMMAND.getName()))
//			sendDesignToProject();

		if(command.equals(MainActionCommands.CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD.getName()))
			createMoTrPACExperimentReportDirectory();
		
		if(command.equals(MainActionCommands.CREATE_MOTRPAC_REPORT_FILES_COMMAND.getName()))
			createEmptyReportFiles();
		
		if(command.equals(MainActionCommands.SET_UP_AGILENT_DOTD_FILES_COMPRESSION_COMMAND.getName()))
			showCompressionSetupDialog();
		
		if(command.equals(MainActionCommands.COMPRESS_AGILENT_DOTD_FILES_FOR_UPLOAD_COMMAND.getName()))
			compressRawData();
		
		if(command.equals(MainActionCommands.SET_UP_UPLOAD_MANIFEST_GENERATION_COMMAND.getName()))
			showManifestSetupDialog();
		
		if(command.equals(MainActionCommands.CREATE_MANIFEST_FOR_BIC_UPLOAD_COMMAND.getName()))
			createManifestForBICUpload();
	}

	private void showManifestSetupDialog() {

		motrpacManifestForUploadDialog = new MoTrPACManifestForUploadDialog(this);
		motrpacManifestForUploadDialog.setLocationRelativeTo(this.getContentPane());
		motrpacManifestForUploadDialog.setVisible(true);
	}

	private void createManifestForBICUpload() {
		
		Collection<CreateUploadManifestTask>tasks = 
				motrpacManifestForUploadDialog.getManifestTasks();
		
		if(!tasks.isEmpty()) {
			
			for(CreateUploadManifestTask task : tasks)
				MRC2ToolBoxCore.getTaskController().addTask(task);
			
			motrpacManifestForUploadDialog.dispose();
		}
	}
	
	private void showCompressionSetupDialog() {

		motrpacRawDataCompressionSetupDialog = new MoTrPACRawDataCompressionSetupDialog(this);
		motrpacRawDataCompressionSetupDialog.setLocationRelativeTo(this.getContentPane());
		motrpacRawDataCompressionSetupDialog.setVisible(true);
	}
	
	private void compressRawData() {

		Collection<String> errors = motrpacRawDataCompressionSetupDialog.validateInput();
		if(!errors.isEmpty()) {
			
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					motrpacRawDataCompressionSetupDialog);
			return;
		}
		Collection<AgilentDataCompressionTask>tasks = 
				motrpacRawDataCompressionSetupDialog.getCompressionTasks();
		
		if(!tasks.isEmpty()) {
			
			for(AgilentDataCompressionTask task : tasks) {
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}			
			motrpacRawDataCompressionSetupDialog.dispose();
		}			
	}
	
	private void createEmptyReportFiles() {
		motrpacReportEmptyFileGeneratorDialog = new MotrpacReportEmptyFileGeneratorDialog();
		motrpacReportEmptyFileGeneratorDialog.setLocationRelativeTo(this.getContentPane());
		motrpacReportEmptyFileGeneratorDialog.setVisible(true);
	}
	
	private void showMetadataReference() {
		if(motrpacReferenceDataDialog == null) {
			motrpacReferenceDataDialog = new MotrpacReferenceDataDialog();
			motrpacReferenceDataDialog.setLocationRelativeTo(this.getContentPane());
		}
		motrpacReferenceDataDialog.setVisible(true);
	}

	private void createMoTrPACExperimentReportDirectory() {

		CreateUploadDirectoryStructureDialog dialog = 
				new CreateUploadDirectoryStructureDialog();
		dialog.setLocationRelativeTo(this.getContentPane());
		dialog.setVisible(true);
	}

//	private void syncMrc2limsAndMetlims() {
//
//		MetLimsToMrc2limsDataTransferTask task = new MetLimsToMrc2limsDataTransferTask();
//		task.addTaskListener(this);
//		CefAnalyzerCore.getTaskController().addTask(task);
//	}

//	private void sendDesignToProject() {
//
//		LIMSExperiment activeExperiment = experimentDataPanel.getSelectedExperiment();
//		if(activeExperiment == null)
//			return;
//
//		if(CefAnalyzerCore.getCurrentProject() != null) {
//
//			String yesNoQuestion =
//					"Do you want to replace the design for the current project\n"
//					+ "with the data for selected LIMS experiment?";
//			int result = MessageDialogue.showChoiceWithWarningMsg(yesNoQuestion , this.getContentPane());
//			if(result == JOptionPane.NO_OPTION)
//				return;
//
//			CefAnalyzerCore.getCurrentProject().setLimsExperiment(activeExperiment);
//			CefAnalyzerCore.getCurrentProject().setLimsProject(activeExperiment.getProject());
//			ExperimentDesign design = CefAnalyzerCore.getCurrentProject().getExperimentDesign();
//
//			if(design.getSamples().isEmpty() && activeExperiment.getExperimentDesign() != null)
//				design.replaceDesign(activeExperiment.getExperimentDesign());
//		}
//		else {
//			CefAnalyzerCore.getMainWindow().
//				showNewProjectFromLimsExperimentDialogue(ProjectType.DATA_ANALYSIS, activeExperiment);
//		}
//	}

	private void refreshLimsData() {
		
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", this.getContentPane());
			return;
		}
		MotrpacLimsDataPullTask lpt = new MotrpacLimsDataPullTask();
		lpt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lpt);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			if(e.getSource().getClass().equals(MotrpacLimsDataPullTask.class)) {
				refreshPanels();
				limsDataLoaded = true;
				studyManagerPanel.setLimsDataLoaded(true);
			}
		}
	}

	private void refreshPanels() {
		// TODO Auto-generated method stub
		studyManagerPanel.loadStudies();

		
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
		
		if(!e.getValueIsAdjusting()) {

			if(e.getSource().equals(studyManagerPanel.getTable().getSelectionModel())) {
				
				MoTrPACStudy study = studyManagerPanel.getTable().getSelectedStudy();
				if(study != null) {
					experimentListingTable.setModelFromExperimentCollection(study.getExperiments());
					studyAssayListingPanel.loadAssays(study.getAssays());
					showReportListing();
				}
				else
					clearStudyData();
			}
			if(e.getSource().equals(experimentListingTable.getTable().getSelectionModel())) {
				
				LIMSExperiment experiment = experimentListingTable.getSelectedExperiment();
				MoTrPACStudy study = studyManagerPanel.getTable().getSelectedStudy();
				if (experiment == null) {
					tissueCodeListingPanel.clearPanel();
					reportListingPanel.clearPanel();
				}
				else {
					Collection<MoTrPACTissueCode> experimentTissueCodes = 
							study.getTissueCodesForExperiment(experiment);
					if(experimentTissueCodes != null)
						tissueCodeListingPanel.loadTissueCodes(experimentTissueCodes);						
					else
						reportListingPanel.clearPanel();
					
					showReportListing();
				}
			}
			if(e.getSource().equals(studyAssayListingPanel.getTable().getSelectionModel())) {
				showReportListing();
			}
			if(e.getSource().equals(tissueCodeListingPanel.getTable().getSelectionModel())) {
				showReportListing();
			}
		}
	}
	
	public void showReportListing() {
		
		MoTrPACStudy study = studyManagerPanel.getTable().getSelectedStudy();
		LIMSExperiment experiment = experimentListingTable.getSelectedExperiment();
		MoTrPACAssay assay = studyAssayListingPanel.getSelectedAssay();
		MoTrPACTissueCode tissueCode = tissueCodeListingPanel.getSelectedCode();
		if (study == null && experiment == null) {
			clearStudyData();
			return;
		}
		Collection<MoTrPACReport>filteredReports = 
				MoTrPACDatabaseCache.getFilteredMoTrPACReports(study, experiment, assay, tissueCode);
		reportListingPanel.loadReports(filteredReports);
	}
	
	private void clearStudyData() {
		
		experimentListingTable.clearPanel();
		tissueCodeListingPanel.clearPanel();
		studyAssayListingPanel.clearPanel();
		reportListingPanel.clearPanel();	
	}

	@Override
	public synchronized void clearPanel() {
		studyManagerPanel.clearPanel();
		clearStudyData();
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLimsDataLoaded() {
		return limsDataLoaded;
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

	@Override
	public void updateGuiWithRecentData() {
		// TODO Auto-generated method stub
		
	}
}

















