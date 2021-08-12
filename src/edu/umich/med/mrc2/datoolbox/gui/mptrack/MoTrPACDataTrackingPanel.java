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

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.DockableExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.MotrpacReferenceDataDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.DockableMoTrPACReportListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.MotrpacReportUploadDialog;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACStudyAssayListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACStudyManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.study.DockableMoTrPACTissueCodeListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.MotrpacLimsDataPullTask;

public class MoTrPACDataTrackingPanel extends DockableMRC2ToolboxPanel 
	implements TreeSelectionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("MoTrPAC", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MoTrPACDataTrackingPanel.layout");

	private MotrpacReferenceDataDialog motrpacReferenceDataDialog;
	private MoTrPACDataTrackingToolbar toolbar;
//	private DockableMotrpacProjectTree projectTree;
	private DockableMoTrPACStudyManagerPanel studyManagerPanel;
	private DockableExperimentListingTable experimentListingTable;
	private DockableMoTrPACTissueCodeListingPanel tissueCodeListingPanel;
	private DockableMoTrPACStudyAssayListingPanel studyAssayListingPanel;
	private DockableMoTrPACReportListingPanel reportListingPanel;
	
	private MotrpacReportUploadDialog motrpacReportUploadDialog;
	private boolean limsDataLoaded;

	public MoTrPACDataTrackingPanel() {
		
		super("MoTrPACDataTrackingPanel", PanelList.MOTRPAC_REPORT_TRACKER.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		toolbar = new MoTrPACDataTrackingToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
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

		loadLayout(layoutConfigFile);
		limsDataLoaded = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		//	TODO re-enable when coding completed
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
//			MessageDialogue.showErrorMsg("You are not logged in!", this.getContentPane());
//			return;
		}
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName()))
			refreshLimsData();

		if(command.equals(MainActionCommands.SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND.getName()))
			showMetadataReference();

//		if(command.equals(MainActionCommands.SEND_DESIGN_TO_PROJECT_COMMAND.getName()))
//			sendDesignToProject();

		if(command.equals(MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName()))
			createMoTrPACExperimentReportDirectory();
	}

	private void showMetadataReference() {
		if(motrpacReferenceDataDialog == null) {
			motrpacReferenceDataDialog = new MotrpacReferenceDataDialog();
			motrpacReferenceDataDialog.setLocationRelativeTo(this.getContentPane());
		}
		motrpacReferenceDataDialog.setVisible(true);
	}

	private void createMoTrPACExperimentReportDirectory() {

//		LIMSExperiment experiment = experimentDataPanel.getSelectedExperiment();
//		if(experiment == null)
//			return;
//
//		File defaultDataDir = new File(CaConfiguration.getDefaultDataDirectory());
//		int response = MessageDialogue.showChoiceMsg(
//			"<HTML>Directory structure would be created or updated for experiment<br><b>" +
//			experiment.getId() + "(" + experiment.getName() + ")</b><br>" +
//			"in the <b>" + defaultDataDir.getPath() + "</b> directory.", this.getContentPane());
//
//		if(response == JOptionPane.YES_OPTION) {
//
//			try {
//				Path experimentDir = LIMSReportingUtils.createExperimentDataDirectoryStructure(experiment.getId(), defaultDataDir);
//				if(experimentDir.toFile().exists()) {
//
//					if(MessageDialogue.showChoiceMsg("Directory was created, do you want to open it?",
//						this.getContentPane()) == JOptionPane.YES_OPTION)
//					Desktop.getDesktop().open(experimentDir.toFile());
//				}
//				else {
//					MessageDialogue.showErrorMsg("Failed to create experiment directory", this.getContentPane());
//				}
//			}
//			catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				MessageDialogue.showErrorMsg("Failed to create experiment directory", this.getContentPane());
//			}
//		}
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
				MoTrPACDatabaseCash.getFilteredMoTrPACReports(study, experiment, assay, tissueCode);
		reportListingPanel.loadReports(filteredReports);
	}
	
	private void clearStudyData() {
		
		experimentListingTable.clearPanel();
		tissueCodeListingPanel.clearPanel();
		studyAssayListingPanel.clearPanel();
		reportListingPanel.clearPanel();	
	}

	@Override
	public void clearPanel() {
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
}

















