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

package edu.umich.med.mrc2.datoolbox.gui.lims;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.lims.drcc.DockableDRCCDataPrepPanel;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.DockableExperimentDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims.LimsDataPullTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims.MetLimsToIDtrackerCrossDbUpdateTask;
import edu.umich.med.mrc2.datoolbox.utils.LIMSReportingUtils;

public class METLIMSPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("experimentDatabase", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);
	private static final Icon sendDesignToProjectIcon = GuiUtils.getIcon("sendDesignToProject", 24);
	private static final Icon syncDbIcon = GuiUtils.getIcon("synchronizeDb", 24);
	private static final Icon createExperimentDirIcon = GuiUtils.getIcon("newProject", 24);
	private static final Icon deleteExperimentIcon = GuiUtils.getIcon("deleteCollection", 24);
	
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "METLIMSPanel.layout");

	private DockableExperimentDataPanel experimentDataPanel;
	private DockableDRCCDataPrepPanel drccDataPrepPanel;

	public METLIMSPanel() {
		
		super("METLIMSPanel", PanelList.LIMS.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new METLIMSMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		experimentDataPanel = new DockableExperimentDataPanel();
		drccDataPrepPanel = new DockableDRCCDataPrepPanel();
		grid.add(0, 0, 100, 100, experimentDataPanel, drccDataPrepPanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
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
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND.getName(),
				MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND.getName(), 
				syncDbIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND.getName(),
				MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND.getName(), 
				deleteExperimentIcon, this));
		
		menuActions.addSeparator();	
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND.getName(),
				MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND.getName(), 
				sendDesignToProjectIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName(),
				MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName(), 
				createExperimentDirIcon, this));
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

		if(command.equals(MainActionCommands.SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND.getName()))
			syncMrc2limsAndMetlims();
			
		if(command.equals(MainActionCommands.DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND.getName()))
			deleteExperimentFromMrc2Lims();
		
		if(command.equals(MainActionCommands.RESYNCHRONIZE_MRC2LIMS_EXPERIMENT_TO_METLIMS_COMMAND.getName()))
			resyncMrc2ExperimentToMetlims();
		
		if(command.equals(MainActionCommands.SEND_DESIGN_TO_EXPERIMENT_COMMAND.getName()))
			sendDesignToExperiment();

		if(command.equals(MainActionCommands.CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND.getName()))
			createExperimentDirectory();
	}

	private void createExperimentDirectory() {

		LIMSExperiment experiment = experimentDataPanel.getSelectedExperiment();
		if(experiment == null)
			return;

		File defaultDataDir = new File(MRC2ToolBoxConfiguration.getDefaultDataDirectory());
		int response = MessageDialog.showChoiceMsg(
			"<HTML>Directory structure would be created or updated for experiment<br><b>" +
			experiment.getId() + "(" + experiment.getName() + ")</b><br>" +
			"in the <b>" + defaultDataDir.getPath() + "</b> directory.", this.getContentPane());

		if(response == JOptionPane.YES_OPTION) {

			try {
				Path experimentDir = LIMSReportingUtils.
						createExperimentDataDirectoryStructure(experiment.getId(), defaultDataDir);
				if(experimentDir.toFile().exists()) {

					if(MessageDialog.showChoiceMsg("Directory was created, do you want to open it?",
						this.getContentPane()) == JOptionPane.YES_OPTION)
					Desktop.getDesktop().open(experimentDir.toFile());
				}
				else {
					MessageDialog.showErrorMsg("Failed to create experiment directory", this.getContentPane());
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				MessageDialog.showErrorMsg("Failed to create experiment directory", this.getContentPane());
			}
		}
	}
	
	private void deleteExperimentFromMrc2Lims() {
		
		LIMSExperiment activeExperiment = experimentDataPanel.getSelectedExperiment();
		if(activeExperiment == null)
			return;
		
		// Authenticate as superuser
		if (!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		String yesNoQuestion =
				"Do you want to remove experiment \"" + activeExperiment.getName() + 
				"\" (" + activeExperiment.getId() + ") from the database?";
		int result = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion , this.getContentPane());
		if(result != JOptionPane.YES_OPTION)
			return;
		
		try {
			LIMSUtils.deleteMRC2LIMSExperiment(activeExperiment.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LIMSDataCache.getExperiments().remove(activeExperiment);
		clearPanel();		
		experimentDataPanel.loadExperimentList(LIMSDataCache.getExperiments());
	}

	private void syncMrc2limsAndMetlims() {

		//	MetLimsToMrc2limsDataTransferTask task = new MetLimsToMrc2limsDataTransferTask();
		MetLimsToIDtrackerCrossDbUpdateTask task = new MetLimsToIDtrackerCrossDbUpdateTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private void resyncMrc2ExperimentToMetlims() {
		
		LIMSExperiment activeExperiment = experimentDataPanel.getSelectedExperiment();
		if(activeExperiment == null)
			return;
	
	}
	
	private void sendDesignToExperiment() {
		
		LIMSExperiment activeExperiment = 
				experimentDataPanel.getSelectedExperiment();
		if(activeExperiment == null)
			return;
		
		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null 
				|| MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg("Please close currently active experiment first!");
			return;
		}
		MRC2ToolBoxCore.getMainWindow().
				showNewExperimentDialog(ProjectType.DATA_ANALYSIS, activeExperiment);
		//	DATA_ANALYSIS_NEW_FORMAT
	}

	public void refreshLimsData() {

		LimsDataPullTask lpt = new LimsDataPullTask();
		lpt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lpt);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(LimsDataPullTask.class)) {

				clearPanel();
				experimentDataPanel.loadExperimentList(LIMSDataCache.getExperiments());
			}
//			if(e.getSource().getClass().equals(MetLimsToMrc2limsDataTransferTask.class)) {
//				refreshLimsData();
//			}
			if(e.getSource().getClass().equals(MetLimsToIDtrackerCrossDbUpdateTask.class)) {
				refreshLimsData();
			}			
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
		if(!e.getValueIsAdjusting()) {

		}
	}

	@Override
	public synchronized void clearPanel() {
		// TODO Auto-generated method stub
		experimentDataPanel.clearPanel();
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
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

















