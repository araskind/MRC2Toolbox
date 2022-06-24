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

package edu.umich.med.mrc2.datoolbox.gui.qc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.threed.Dockable3DChartPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.qc.twod.Dockable2DQCPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.DataSetStatisticsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.PCATask;

public class QCPanel extends DockableMRC2ToolboxPanel {

	private DockableDataSetStatsTable dataSetStatsTable;
	private Dockable2DQCPanel twoDQCpanel;
	private Dockable3DChartPanel threeDpanel;

	private static final Icon componentIcon = GuiUtils.getIcon("qc", 16);
	static final Icon calcStatsIcon = GuiUtils.getIcon("stats", 24);
	static final Icon pcaIcon = GuiUtils.getIcon("scatterPlot3D", 24);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "QCPanel.layout");

	public QCPanel() {

		super("QCPanel", PanelList.QC.getName(), componentIcon);
		menuBar = new QCPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		dataSetStatsTable = new DockableDataSetStatsTable(
				"QCPanelDockableDataSetStatsTable", "Global data set statistics");
		twoDQCpanel = new Dockable2DQCPanel(
				"QCPanelDockable2DQCPanel", "2D QC plots");
		threeDpanel = new Dockable3DChartPanel(
				"QCPanelDockable3DChartPanel", "3D QC plots");

		grid.add(0, 0, 100, 50, dataSetStatsTable);
		grid.add(0, 50, 100, 50, twoDQCpanel, threeDpanel);
		grid.select(0, 50, 100, 50, twoDQCpanel);
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
				MainActionCommands.CALC_DATASET_STATS_COMMAND.getName(),
				MainActionCommands.CALC_DATASET_STATS_COMMAND.getName(), 
				calcStatsIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CALC_DATASET_PCA_COMMAND.getName(),
				MainActionCommands.CALC_DATASET_PCA_COMMAND.getName(), 
				pcaIcon, this));	
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
		
		if(currentProject == null || activeDataPipeline == null)
			return;

		String command = event.getActionCommand();
		if (command.equals(MainActionCommands.CALC_DATASET_STATS_COMMAND.getName()))
			calculateDataSetStats();

		if (command.equals(MainActionCommands.CALC_DATASET_PCA_COMMAND.getName()))
			runDataSetPca();
	}

	private void calculateDataSetStats() {

		DataSetStatisticsTask statTask =
				new DataSetStatisticsTask(currentProject, activeDataPipeline);
		statTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(statTask);
	}

	public synchronized void clearPanel() {

		twoDQCpanel.clearPanel();
		threeDpanel.clearPlotPanel();
		dataSetStatsTable.getTable().clearTable();
	}

	private void runDataSetPca() {

		PCATask pcaTask = new PCATask(
				currentProject,
				activeDataPipeline,
				currentProject.getExperimentDesign().getActiveDesignSubset(),
				currentProject.getActiveFeatureSetForDataPipeline(activeDataPipeline),
				3);
		pcaTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(pcaTask);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(DataSetStatisticsTask.class))
				showDataSetStatistics((DataSetStatisticsTask) e.getSource());

			if (e.getSource().getClass().equals(PCATask.class))
				showPCAResults((PCATask) e.getSource());
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}

	private void showDataSetStatistics(DataSetStatisticsTask dssTask) {

		dataSetStatsTable.loadDataSetStats(dssTask.getStatsList());
		twoDQCpanel.loadDataSetStats(dssTask.getStatsList());
	}

	private void showPCAResults(PCATask dssTask) {

		threeDpanel.showPca(
				dssTask.getProjection(), 
				dssTask.getAciveDataPipeline(), 
				dssTask.getActiveDesign());
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newDataPipeline);
		menuBar.updateMenuFromProject(currentProject, activeDataPipeline);
		twoDQCpanel.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		threeDpanel.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		if(currentProject != null && activeDataPipeline != null) {
			//TODO show data if available

		}
	}

	@Override
	public void closeProject() {

		super.closeProject();
		clearPanel();
		menuBar.updateMenuFromProject(null, null);
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
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}
