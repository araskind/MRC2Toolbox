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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.mgf.mgftree.DockableMsMsTree;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MgfImportTask;

public class MgfPanel extends DockableMRC2ToolboxPanel implements TreeSelectionListener {

	private DockableMsMsTree msMsTree;
	private DockableSpectumPlot msPlot;
	private DockableMsMsTable msMsTable;
	private DockableMsMsClusterTable msMsClusterTable;
	private File baseDirectory;

	private static final Icon componentIcon = GuiUtils.getIcon("filterMsMs", 16);
	private static final Icon importMgfIcon = GuiUtils.getIcon("loadMgf", 24);
	private static final Icon exportMsMsIcon = GuiUtils.getIcon("exportMsMs", 24);

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MgfPanel.layout");

	public MgfPanel() {

		super("MgfPanel", PanelList.MGF.getName(), componentIcon);

		menuBar = new MGFPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		msMsTree = new DockableMsMsTree("MgfPanelDockableMsMsTree", "MGF spectra");
		msMsTree.getTree().addTreeSelectionListener(this);

		msMsClusterTable = new DockableMsMsClusterTable(this);
		msMsTable = new DockableMsMsTable();
		msPlot = new DockableSpectumPlot("MgfPanelDockableSpectumPlot", "MGF MS/MS plot");

		grid.add(0, 0, 100, 50, msMsClusterTable);
		grid.add(50, 50, 50, 50, msMsTable);
		grid.add(0, 50, 50, 50, msPlot);

		grid.add(-25, 0, 25, 100, msMsTree);
		grid.select(-25, 0, 25, 100, msMsTree);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.IMPORT_MGF_COMMAND.getName(),
				MainActionCommands.IMPORT_MGF_COMMAND.getName(), 
				importMgfIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_MSMS_COMMAND.getName(),
				MainActionCommands.EXPORT_MSMS_COMMAND.getName(), 
				exportMsMsIcon, this));		
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

		if (command.equals(MainActionCommands.IMPORT_MGF_COMMAND.getName()))
			importMgf();

		if (command.equals(MainActionCommands.EXPORT_MSMS_COMMAND.getName()))
			exportMsMs();
	}

	public synchronized void clearPanel() {

		msMsTree.resetTree();
		msPlot.removeAllDataSets();
		msMsClusterTable.clearTable();;
		msMsTable.clearTable();;
	}

	private void exportMsMs() {
		// TODO Auto-generated method stub

	}

	private void importMgf() {

		String yesNoQuestion = "You are going to discard existing results, do you want to proceed?";
		if (MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {

			File mgfFile = selectMgfFile();

			if (mgfFile != null) {

				if (mgfFile.exists()) {

					MgfImportTask mip = new MgfImportTask(mgfFile);
					mip.addTaskListener(this);
					MRC2ToolBoxCore.getTaskController().addTask(mip);
				}
			}
		}
		else
			return;
	}

	private File selectMgfFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select library CEF file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			return inputFile;
		}
		return null;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			// MgfImportTask
			if (e.getSource().getClass().equals(MgfImportTask.class)) {

				clearPanel();

				MgfImportTask eTask = (MgfImportTask) e.getSource();

				for (MsMsCluster cluster : eTask.getFeatureClusterss())
					msMsTree.addFeatureClusterToTree(cluster);

				msMsTree.expandClusterBranch();
			}
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			if (e.getSource() instanceof DefaultListSelectionModel) {

				ListSelectionListener[] source = ((DefaultListSelectionModel) e.getSource()).getListSelectionListeners();

				if (Arrays.asList(source).contains(msMsClusterTable)) {

					SimpleMsMs selectedMsMs = msMsClusterTable.getSelectedMsMs();
					if(selectedMsMs != null) {
						msPlot.showSimpleMsMs(Arrays.asList(selectedMsMs));
						msMsTable.setTableModelFromSimpleMsMs(selectedMsMs);
					}
					else {
						msPlot.removeAllDataSets();
						msMsTable.clearTable();
					}
				}
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {

		if (event.getSource().equals(msMsTree)) {

			if (msMsTree.getClickedObject() instanceof MsMsCluster) {

				MsMsCluster fc = (MsMsCluster) msMsTree.getClickedObject();
				msMsTable.setTableModelFromMsMsCluster(fc);
				msMsClusterTable.setTableModelFromMsMsCluster(fc);
				msPlot.showMsForCluster(fc);
			}
			if (msMsTree.getClickedObject() instanceof SimpleMsMs) {

				SimpleMsMs fc = (SimpleMsMs) msMsTree.getClickedObject();
				msMsTable.setTableModelFromSimpleMsMs(fc);
				msPlot.showSimpleMsMs(Arrays.asList(fc));
			}
		}
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newPipeline);
		menuBar.updateMenuFromExperiment(currentProject, activeDataPipeline);
	}

	@Override
	public void closeExperiment() {
		// TODO Auto-generated method stub
		super.closeExperiment();
		clearPanel();
		menuBar.updateMenuFromExperiment(null, null);
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
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}
