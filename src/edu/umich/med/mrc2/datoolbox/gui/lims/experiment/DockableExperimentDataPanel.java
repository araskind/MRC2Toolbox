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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCash;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.design.DockableLIMSDesignEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims.ExperimentDesignPullTask;


public class DockableExperimentDataPanel  extends DefaultSingleCDockable
	implements ActionListener, TaskListener, PersistentLayout, ListSelectionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("experimentDatabase", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "ExperimentDataPanel.layout");

	protected CControl control;
	protected CGrid grid;
	private DockableExperimentListingTable experimentListingTable;
	private DockableExperimentSummaryPanel experimentSummary;
	private DockableLIMSDesignEditorPanel designEditorPanel;
	private IndeterminateProgressDialog idp;

	public DockableExperimentDataPanel() {

		super("DockableExperimentDataPanel", componentIcon, "Experiment data", null, Permissions.MIN_MAX_EXT_STACK);
		setLayout(new BorderLayout(0, 0));
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		experimentListingTable = new DockableExperimentListingTable(this);
		experimentSummary = new DockableExperimentSummaryPanel(this);
		designEditorPanel = new DockableLIMSDesignEditorPanel();

		grid.add(0, 0, 50, 50, experimentListingTable);
		grid.add(0, 50, 50, 50, designEditorPanel);
		grid.add(50, 0, 50, 100, experimentSummary);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);

		//	experimentListingTable.setModelFromExperimentCollection(LIMSDataCash.getExperiments());
	}

	public void loadExperimentList(Collection<LIMSExperiment>experimentList) {

		TableUpdateTask task = new TableUpdateTask(experimentList);
		idp = new IndeterminateProgressDialog("Uptating table data ...", experimentListingTable.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);

//		clearPanel();
//		experimentListingTable.setModelFromExperimentCollection(experimentList);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.SAVE_EXPERIMENT_SUMMARY_COMMAND.getName()))
			saveExperimentSummary();
	}

	private void saveExperimentSummary() {
		// TODO Auto-generated method stub

	}

	public synchronized void clearPanel() {

		experimentListingTable.clearPanel();
		experimentSummary.clearPanel();
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
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

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			LIMSExperiment selected = experimentListingTable.getSelectedExperiment();
			if (selected != null)
				loadExperimentData(selected);
			else {
				experimentSummary.clearPanel();
				designEditorPanel.clearPanel();
			}
		}
	}

	public LIMSExperiment getSelectedExperiment() {
		return experimentListingTable.getSelectedExperiment();
	}

	private void loadExperimentData(LIMSExperiment selected) {

		experimentSummary.showExperimentSummary(selected);
		if(selected.getExperimentDesign() == null) {

			ExperimentDesignPullTask edt = new ExperimentDesignPullTask(selected);
			edt.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(edt);
		}
		else
			designEditorPanel.showExperimentDesign(selected.getExperimentDesign());
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(ExperimentDesignPullTask.class)) {

				ExperimentDesign design = ((ExperimentDesignPullTask)e.getSource()).getDesign();
				if(design != null)
					designEditorPanel.showExperimentDesign(design);

				MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
				MainWindow.hideProgressDialog();
			}
		}
	}

	class TableUpdateTask extends LongUpdateTask {

		public TableUpdateTask(Collection<LIMSExperiment> experimentList) {
			super();
			this.experimentList = experimentList;
		}

		/*
		 * Main task. Executed in background thread.
		 */
		private Collection<LIMSExperiment>experimentList;

		@Override
		public Void doInBackground() {

			if(experimentList == null) {
				clearPanel();
				experimentListingTable.setModelFromExperimentCollection(experimentList);
			}
			else {
				experimentListingTable.setModelFromExperimentCollection(LIMSDataCash.getExperiments());
			}
			return null;
		}
	}
}



























