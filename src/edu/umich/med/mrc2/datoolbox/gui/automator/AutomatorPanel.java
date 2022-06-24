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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;

import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskPriority;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.impl.WrappedTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.QualAutomation.QualAutomationDataProcessingTask;

public class AutomatorPanel extends DockableMRC2ToolboxPanel implements TaskControlListener {

	private static final int PAUSE_BETWEEN_TASKS = 50;
	
	private AutomatorToolbar toolbar;
	private DockableProcessingMethodsPanel parametersPanel;
	private DockableInputFilesPanel dataFilesPanel;
	private DockableAutomatorTaskTable runningTaskTable;
	private DockableFailedTasksTable failedTasksTable;
	private DockableAutomatorConsole console;
	private PrintStream ps;
	private TextAreaOutputStream taos;
	private FileFilter dotDfilter;
	private ArrayList<Task> tasksToRerurn;
	private int processNumber;
	private File[] posDataFileList;
	private File[] negDataFileList;
	private File positiveModeDataFilesFolder;
	private File negativeModeDataFilesFolder;
	private File positiveModeMethod;
	private File negativeModeMethod;
	private File qualAutomationBinary;

	private static final Icon componentIcon = GuiUtils.getIcon("script", 16);
	private static final Icon runIcon = GuiUtils.getIcon("run", 24);
	private static final Icon reRunIcon = GuiUtils.getIcon("rerun", 24);
	private static final Icon stopIcon = GuiUtils.getIcon("stop", 24);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "AutomatorPanel.layout");
	
	//	private LinkedHashMap<SingleCDockable, Boolean> panelShowing;

	public AutomatorPanel() {

		super("AutomatorPanel", PanelList.AUTOMATOR.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		toolbar = new AutomatorToolbar(this);
		toolbar.disableRerunButton();
		add(toolbar, BorderLayout.NORTH);

		parametersPanel = new DockableProcessingMethodsPanel();
		dataFilesPanel = new DockableInputFilesPanel();
		runningTaskTable = new DockableAutomatorTaskTable(this);
		failedTasksTable = new DockableFailedTasksTable();
		console = new DockableAutomatorConsole();
		
		control.addDockable(parametersPanel);
		grid.add( 0, 0, 25, 25, parametersPanel);
		grid.add( 0, 25, 25, 25, dataFilesPanel);
		grid.add( 25, 0, 75, 60, runningTaskTable);
		grid.add( 25, 60, 75, 40, failedTasksTable, console);

		control.getContentArea().deploy( grid );
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		dotDfilter = new RegexFileFilter(".+\\.[dD]$");
		tasksToRerurn = new ArrayList<Task>();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RUN_AUTOMATOR_COMMAND.getName(),
				MainActionCommands.RUN_AUTOMATOR_COMMAND.getName(), 
				runIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.RERUN_FAILED_ASSAY_COMMAND.getName(),
				MainActionCommands.RERUN_FAILED_ASSAY_COMMAND.getName(), 
				reRunIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.STOP_AUTOMATOR_COMMAND.getName(),
				MainActionCommands.STOP_AUTOMATOR_COMMAND.getName(), 
				stopIcon, this));
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
		if (command.equals(MainActionCommands.RUN_AUTOMATOR_COMMAND.getName())) {
			runAnalysis();
			return;
		}
		else if (command.equals(MainActionCommands.RERUN_FAILED_ASSAY_COMMAND.getName())) {
			rerunFailedAnalysis();
			return;
		}
		else if (command.equals(MainActionCommands.STOP_AUTOMATOR_COMMAND.getName())) {
			stopAnalysis();
			return;
		}
		else if (command.equals(MainActionCommands.CANCEL_ALL_TASKS_COMMAND.getName())) {

			MRC2ToolBoxCore.getTaskController().cancelAllTasks();
			MainWindow.hideProgressDialog();
			return;
		}
		else {
			Task[] selectedTasks = runningTaskTable.getSelectedTasks();

			for (Task t : selectedTasks) {

				if (t != null) {

					if (command.equals(MainActionCommands.CANCEL_SELECTED_TASK_COMMAND.getName())){

						if ((t.getStatus() == TaskStatus.WAITING) || (t.getStatus() == TaskStatus.PROCESSING))
							t.cancel();
					}
					if (command.equals(MainActionCommands.SET_HIGH_PRIORITY_COMMAND.getName()))
						MRC2ToolBoxCore.getTaskController().setTaskPriority(t, TaskPriority.HIGH);

					if (command.equals(MainActionCommands.SET_NORMAL_PRIORITY_COMMAND.getName()))
						MRC2ToolBoxCore.getTaskController().setTaskPriority(t, TaskPriority.NORMAL);

					if (command.equals(MainActionCommands.RESTART_SELECTED_TASK_COMMAND.getName())) {

						Task clonedTask = t.cloneTask();
						t.setStatus(TaskStatus.REPROCESSING);
						MRC2ToolBoxCore.getTaskController().addTask(clonedTask, TaskPriority.NORMAL);
					}
				}
			}
		}
	}

	public void addTaskToRerunQueue(Task failedTask) {

		tasksToRerurn.add(failedTask);
		failedTasksTable.addTask(failedTask);
		toolbar.enableRerunButton();
	}

	@Override
	public void allTasksFinished(boolean atf) {

		ps.close();
		try {
			taos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.setOut(System.out);
		System.setErr(System.err);
		console.setText("");
	}

	private boolean checkInputs() {

		boolean inputValid = true;

		processNumber = MRC2ToolBoxConfiguration.getMaxThreadNumber();

		if (processNumber == 0)
			processNumber = 12;

		// Executable
		qualAutomationBinary = new File(MRC2ToolBoxConfiguration.getQualAutomationExecutableFile());

		if (qualAutomationBinary == null || !qualAutomationBinary.exists()
				|| !qualAutomationBinary.getName().equals("QualAutomation.exe")) {

			MessageDialog.showErrorMsg("AgtQual binary not found!");
			return false;
		}

		// Input data and methods
		positiveModeDataFilesFolder = dataFilesPanel.getPositiveDataFolder();

		if (positiveModeDataFilesFolder.exists())
			posDataFileList = positiveModeDataFilesFolder.listFiles(dotDfilter);

		negativeModeDataFilesFolder = dataFilesPanel.getNegativeDataFolder();

		if (negativeModeDataFilesFolder.exists())
			negDataFileList = negativeModeDataFilesFolder.listFiles(dotDfilter);

		positiveModeMethod = parametersPanel.getPositiveMethodFile();

		if (positiveModeDataFilesFolder.exists()) {

			if (posDataFileList.length > 0) {

				if (!positiveModeMethod.exists() || !Pattern.matches(".+[mM]$", positiveModeMethod.getName())) {

					MessageDialog.showErrorMsg("Positive mode method file missing or wrong type!");
					return false;
				} else
					inputValid = true;
			}
		}
		negativeModeMethod = parametersPanel.getNegativeMethodFile();

		if (negativeModeDataFilesFolder.exists()) {

			if (negDataFileList.length > 0) {

				if (!negativeModeMethod.exists() || !Pattern.matches(".+[mM]$", negativeModeMethod.getName())) {

					MessageDialog.showErrorMsg("Negative mode method file missing or wrong type!");
					return false;
				}
			} else
				inputValid = true;
		}
		return inputValid;
	}

	@Override
	public synchronized void clearPanel() {
		//	TODO - properly clean the UI when analysis completed or stopped
	}

	@Override
	public void numberOfWaitingTasksChanged(int numOfTasks) {

	}

	private void rerunFailedAnalysis() {

		try {
			taos = new TextAreaOutputStream(console.getConsoleTextArea());

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (taos != null) {

			ps = new PrintStream(taos);
			// System.setOut( ps );
			System.setErr(ps);
		}

		for (Task newTask : tasksToRerurn)
			MRC2ToolBoxCore.getTaskController().addTask(newTask, TaskPriority.HIGH);

		tasksToRerurn.clear();
		failedTasksTable.clearTable();
		toolbar.disableRerunButton();
	}

	private void runAnalysis() {

		posDataFileList = new File[0];
		negDataFileList = new File[0];

		if (checkInputs()) {

			try {
				taos = new TextAreaOutputStream(console.getConsoleTextArea());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (taos != null) {

				ps = new PrintStream(taos);
				// System.setOut( ps );
				System.setErr(ps);
			}
			MRC2ToolBoxCore.getTaskController().setMaxRunningThreads(processNumber);

			// displayErrorMessage("File count", "To process\n" + "POS " +
			// posDataFileList.length + "\nNEG " + negDataFileList.length);

			if (posDataFileList.length > 0) {

				for (File df : posDataFileList) {

					try {
						Thread.sleep(PAUSE_BETWEEN_TASKS);
					} catch (InterruptedException e) {

					}
					Task newTask = new QualAutomationDataProcessingTask(df, positiveModeMethod, qualAutomationBinary);
					MRC2ToolBoxCore.getTaskController().addTask(newTask, TaskPriority.HIGH);
				}
				try {
					Thread.sleep(PAUSE_BETWEEN_TASKS);
				} catch (InterruptedException e) {

				}
			}
			if (negDataFileList.length > 0) {

				for (File df : negDataFileList) {

					try {
						Thread.sleep(PAUSE_BETWEEN_TASKS);
					} catch (InterruptedException e) {

					}
					Task newTask = new QualAutomationDataProcessingTask(df, negativeModeMethod, qualAutomationBinary);
					MRC2ToolBoxCore.getTaskController().addTask(newTask, TaskPriority.HIGH);
				}
			}
		}
	}

	private void stopAnalysis() {

		WrappedTask currentQueue[] = MRC2ToolBoxCore.getTaskController().getTaskQueue().getQueueSnapshot();

		for (WrappedTask wrappedTask : currentQueue) {

			Task task = wrappedTask.getActualTask();
			TaskStatus status = task.getStatus();

			if ((status == TaskStatus.WAITING) || (status == TaskStatus.PROCESSING))
				task.cancel();
		}
		tasksToRerurn.clear();
		failedTasksTable.clearTable();
		toolbar.disableRerunButton();
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		super.switchDataPipeline(project, newPipeline);
		toolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void closeProject() {
		super.closeProject();
		toolbar.updateGuiFromProjectAndDataPipeline(null, null);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

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
