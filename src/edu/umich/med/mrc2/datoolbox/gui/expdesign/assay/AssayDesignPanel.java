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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class AssayDesignPanel extends DockableMRC2ToolboxPanel{

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "AssayDesignPanel.layout");
	
	private static final Icon componentIcon = GuiUtils.getIcon("link", 16);
	private static final Icon editDesignIcon = GuiUtils.getIcon("editDesignSubset", 24);
	private static final Icon linkFilesIcon = GuiUtils.getIcon("link", 24);
	private static final Icon enableSelectedIcon = GuiUtils.getIcon("checkboxFull", 24);
	private static final Icon disableSelectedIcon = GuiUtils.getIcon("checkboxEmpty", 24);
	private static final Icon enableAllIcon = GuiUtils.getIcon("enableAll", 24);
	private static final Icon disableAllIcon = GuiUtils.getIcon("disableAll", 24);
	private static final Icon invertEnabledIcon = GuiUtils.getIcon("invertSelection", 24);
	private static final Icon resetFilterIcon = GuiUtils.getIcon("resetFilter", 24);
	private static final Icon deleteFilesIcon = GuiUtils.getIcon("deleteDataFile", 24);

	private AssayDesignTable assayDesignTable;

	public AssayDesignPanel() {

		super("AssayDesignPanel", "Active data pieline design", componentIcon);
		setLayout(new BorderLayout(0, 0));
		menuBar = new AssayDesignEditorMenuBar(this); 
		add(menuBar, BorderLayout.NORTH);

		assayDesignTable = new AssayDesignTable();
		assayDesignTable.addTablePopupMenu(new AssayDesignPopupMenu(this));
		assayDesignTable.setTablePopupEnabled(false);
		JScrollPane designScrollPane = new JScrollPane(assayDesignTable);
		add(designScrollPane, BorderLayout.CENTER);
		initActions();
		
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}

	@Override
	protected void initActions() {
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName(), 
				enableSelectedIcon, this));
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName(), 
				disableSelectedIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName(), 
				enableAllIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(),
				MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName(), 
				disableAllIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(),
				MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName(), 
				invertEnabledIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND.getName(),
				MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND.getName(), 
				resetFilterIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(),
				MainActionCommands.DELETE_DATA_FILES_COMMAND.getName(), 
				deleteFilesIcon, this));
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

		if (command.equals(MainActionCommands.DELETE_DATA_FILES_COMMAND.getName()))
			deleteSelectedFiles();

		if (command.equals(MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName()))
			setSamplesEnabledStatus(true, true);

		if (command.equals(MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName()))
			setSamplesEnabledStatus(false, true);

		if (command.equals(MainActionCommands.ENABLE_ALL_SAMPLES_COMMAND.getName()))
			setSamplesEnabledStatus(true, false);

		if (command.equals(MainActionCommands.DISABLE_ALL_SAMPLES_COMMAND.getName()))
			setSamplesEnabledStatus(false, false);

		if (command.equals(MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName()))
			invertSamplesEnabledStatus();

		if (command.equals(MainActionCommands.CLEAR_SAMPLES_FILTER_COMMAND.getName()))
			clearSampleFilter();

		if (command.equals(MainActionCommands.ASSIGN_BATCH_FOR_SELECTED_DATA_FILES_COMMAND.getName()))
			assignBatchToSelectedFiles();
	}

	private void assignBatchToSelectedFiles() {

		Collection<DataFile> selectedFiles = assayDesignTable.getDataFiles(true);
		if(selectedFiles.isEmpty())
			return;

		BatchAssignmentDialog bad = new BatchAssignmentDialog(selectedFiles);
		bad.setLocationRelativeTo(this.getContentPane());
		bad.setVisible(true);
	}

	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);		
	}

	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {

		super.switchDataPipeline(project, newDataPipeline);
		clearPanel();	
		if(currentProject != null)
			assayDesignTable.setTableModelFromExperimentDesign(currentProject, activeDataPipeline);			
	}

	public synchronized void clearPanel() {
		assayDesignTable.clearTable();
	}

	private void deleteSelectedFiles() {

		if(assayDesignTable.getSelectedRowCount() > 0){

			int approve = MessageDialog.showChoiceWithWarningMsg(
				"Remove selected data file(s) from the project?\n"
				+ "(NO UNDO!)");

			if (approve == JOptionPane.YES_OPTION) {

				currentProject.getExperimentDesign().setSuppressEvents(true);

				HashSet<DataFile>filesToRemove = new HashSet<DataFile>();
				int fileCol = assayDesignTable.getColumnIndex(AssayDesignTableModel.DATA_FILE_COLUMN);

				for(int i : assayDesignTable.getSelectedRows()){

					DataFile df = (DataFile) assayDesignTable.getValueAt(i, fileCol);
					filesToRemove.add(df);
				}
				Matrix dataMatrix = 
						currentProject.getDataMatrixForDataPipeline(activeDataPipeline);
				Matrix fileMatrix = dataMatrix.getMetaDataDimensionMatrix(1);
				ArrayList<Long> rem = new ArrayList<Long>();

				for (DataFile df : filesToRemove)
					rem.add(dataMatrix.getRowForLabel(df));

				currentProject.deleteDataFiles(filesToRemove);

				Matrix newDataMatrix = dataMatrix.deleteRows(Ret.NEW, rem);
				Matrix newFileMatrix = fileMatrix.deleteRows(Ret.NEW, rem);
				newDataMatrix.setMetaDataDimensionMatrix(1, newFileMatrix);
				newDataMatrix.setMetaDataDimensionMatrix(0, dataMatrix.getMetaDataDimensionMatrix(0));
				currentProject.setDataMatrixForDataPipeline(activeDataPipeline, newDataMatrix);
				currentProject.getExperimentDesign().setSuppressEvents(false);
				currentProject.getExperimentDesign().fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
				MessageDialog.showWarningMsg("You will have to re-calculate statistical data now!");
			}
		}
	}

	private void clearSampleFilter() {
		assayDesignTable.resetFilter();
	}

	private void invertSamplesEnabledStatus() {

		for(DataFile df : assayDesignTable.getDataFiles(false))
			df.setEnabled(!df.isEnabled());

		assayDesignTable.setTableModelFromExperimentDesign(currentProject, activeDataPipeline);
	}

	private void setSamplesEnabledStatus(boolean enable, boolean selectedOnly) {
		assayDesignTable.setDataFileStatus(enable, selectedOnly);
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
	public void valueChanged(ListSelectionEvent e) {
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
