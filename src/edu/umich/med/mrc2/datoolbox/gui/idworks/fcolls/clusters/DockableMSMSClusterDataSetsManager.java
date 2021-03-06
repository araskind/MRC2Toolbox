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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureAndClusterCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSClusterDataSetUploadTask;

public class DockableMSMSClusterDataSetsManager extends DefaultSingleCDockable implements ActionListener, TaskListener {

	private static final Icon componentIcon = GuiUtils.getIcon("clusterFeatureTable", 16);
	
	private MSMSClusterDataSetsManagerToolbar toolbar;
	private MSMSClusterDataSetsTable msmsClusterDataSetTable;
	private FeatureAndClusterCollectionManagerDialog parent;
	
	private MSMSClusterDataSetEditorDialog msmsClusterDataSetEditorDialog;
	private Collection<MsFeatureInfoBundleCluster> clustersToAdd;
	
	public DockableMSMSClusterDataSetsManager(FeatureAndClusterCollectionManagerDialog parent)  {

		super("DockableFeatureClusterCollectionsManager", componentIcon, 
				"MSMS feature cluster data sets", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new MSMSClusterDataSetsManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		msmsClusterDataSetTable = new MSMSClusterDataSetsTable();	
		MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();		
		msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
				MSMSClusterDataSetManager.getMSMSClusterDataSets());
		
		msmsClusterDataSetTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {
							
							MSMSClusterDataSet selected = 
									msmsClusterDataSetTable.getSelectedDataSet();
							if(selected == null)
								return;
							
							loadMSMSClusterDataSet();
						}
					}
				});
		add(new JScrollPane(msmsClusterDataSetTable));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName()))
			showMsMSMSDataSetEditorDialog(null); 
		
		if(command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_COMMAND.getName())
				|| command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_WITH_CLUSTERS_COMMAND.getName()))
			createNewMSMSClusterDataSet();
		
		if(command.equals(MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName()))
			editSelectedMSMSClusterDataSet();
		
		if(command.equals(MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			saveEditedMSMSClusterDataSet();
		
		if(command.equals(MainActionCommands.DELETE_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			 deleteMSMSClusterDataSet();
		
		if(command.equals(MainActionCommands.LOAD_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			loadMSMSClusterDataSet() ;
	}
	
	private void editSelectedMSMSClusterDataSet() {
		
		MSMSClusterDataSet selected = 
				msmsClusterDataSetTable.getSelectedDataSet();
		if(selected == null)
			return;
		
		if(!MSMSClusterDataSetManager.getEditableMSMSClusterDataSets().contains(selected)) {
			MessageDialog.showWarningMsg("Data set \"" + selected.getName() + 
					"\" is locked and can not be edited.", this.getContentPane());
			return;
		}	
		showMsMSMSDataSetEditorDialog(selected);
	}
	
	public void showMsMSMSDataSetEditorDialog (MSMSClusterDataSet dataSet) {
		
		msmsClusterDataSetEditorDialog = 
				new MSMSClusterDataSetEditorDialog(dataSet, clustersToAdd, this);
		msmsClusterDataSetEditorDialog.setLocationRelativeTo(this.getContentPane());
		msmsClusterDataSetEditorDialog.setVisible(true);
	}

	private void saveEditedMSMSClusterDataSet() {
		
		Collection<String>errors = 
				msmsClusterDataSetEditorDialog.validateCollectionData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					msmsClusterDataSetEditorDialog);
			return;
		}
		MSMSClusterDataSet edited = 
				msmsClusterDataSetEditorDialog.getMSMSClusterDataSet();

		edited.setName(msmsClusterDataSetEditorDialog.getMSMSClusterDataSetName());
		edited.setDescription(msmsClusterDataSetEditorDialog.getMSMSClusterDataSetDescription());
		edited.setLastModified(new Date());
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			saveMSMSClusterDataSetChangesToProject(edited);
		else
			saveMSMSClusterDataSetChangesToDatabase(edited);
		
		clustersToAdd = null;
	}
	
	private void saveMSMSClusterDataSetChangesToProject(MSMSClusterDataSet edited) {
		
		if(msmsClusterDataSetEditorDialog.getClustersToAdd() != null)
			edited.getClusters().addAll(msmsClusterDataSetEditorDialog.getClustersToAdd());
		
		if(msmsClusterDataSetEditorDialog.loadMSMSClusterDataSetIntoWorkBench()) {
			loadMSMSClusterDataSetIntoWorkBench(edited);
			msmsClusterDataSetEditorDialog.dispose();
			parent.dispose();
		}
		else {		
			msmsClusterDataSetEditorDialog.dispose();
			msmsClusterDataSetTable.updateMSMSClusterDataSetData(edited);
			msmsClusterDataSetTable.selectDataSet(edited);
		}		
	}
	
	private void saveMSMSClusterDataSetChangesToDatabase(MSMSClusterDataSet edited) { 
		
		try {
			MSMSClusteringDBUtils.updateMSMSClusterDataSetMetadata(edited);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if(clustersToAdd != null && !clustersToAdd.isEmpty()) {			
			try {
				//	Assign DB cluster ids first
				MSMSClusteringDBUtils.addClustersToDataSet(edited, clustersToAdd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		Set<String>cids = clustersToAdd.stream().map(c -> c.getId()).collect(Collectors.toSet());
		MSMSClusterDataSetManager.clusterDataSetsToClusterIdsMap.get(edited).addAll(cids);
		
		if(msmsClusterDataSetEditorDialog.loadMSMSClusterDataSetIntoWorkBench()) {
			loadMSMSClusterDataSetIntoWorkBench(edited);
			msmsClusterDataSetEditorDialog.dispose();
			parent.dispose();
		}
		else {		
			msmsClusterDataSetEditorDialog.dispose();
			msmsClusterDataSetTable.updateMSMSClusterDataSetData(edited);
			msmsClusterDataSetTable.selectDataSet(edited);
		}
	}

	private void createNewMSMSClusterDataSet() {

		Collection<String>errors = 
				msmsClusterDataSetEditorDialog.validateCollectionData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					msmsClusterDataSetEditorDialog);
			return;
		}
		MSMSClusterDataSet newDataSet = 
				new MSMSClusterDataSet(
						msmsClusterDataSetEditorDialog.getMSMSClusterDataSetName(),
						msmsClusterDataSetEditorDialog.getMSMSClusterDataSetDescription(),
						MRC2ToolBoxCore.getIdTrackerUser());
		
		if(clustersToAdd != null && !clustersToAdd.isEmpty())
			newDataSet.getClusters().addAll(clustersToAdd);
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			createNewMSMSClusterDataSetInProject(newDataSet);
		else
			createNewMSMSClusterDataSetInDatabase(newDataSet);
		
		clustersToAdd = null;
	}
	
	private void createNewMSMSClusterDataSetInProject(MSMSClusterDataSet newDataSet) {
		
		RawDataAnalysisProject project = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisProject();		
		project.getMsmsClusterDataSets().add(newDataSet);
		if(msmsClusterDataSetEditorDialog.loadMSMSClusterDataSetIntoWorkBench()) {
			loadMSMSClusterDataSetIntoWorkBench(newDataSet);
			msmsClusterDataSetEditorDialog.dispose();
			parent.dispose();
		}
		else {
			msmsClusterDataSetEditorDialog.dispose();	
			msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(project.getMsmsClusterDataSets());
			msmsClusterDataSetTable.selectDataSet(newDataSet);		
		}
	}
		
	private void createNewMSMSClusterDataSetInDatabase(MSMSClusterDataSet newDataSet) {
		
		MSMSClusterDataSetUploadTask task = 
				new MSMSClusterDataSetUploadTask(newDataSet);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
	}

	private void deleteMSMSClusterDataSet() {
		
		MSMSClusterDataSet selected = msmsClusterDataSetTable.getSelectedDataSet();
		if(selected == null)
			return;
		
		if(!selected.getCreatedBy().equals(MRC2ToolBoxCore.getIdTrackerUser())
				&& !MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
			
			MessageDialog.showErrorMsg(
					"Data set \"" + selected.getName() + 
					"\" was created by a different user, you can not delete it.", 
					this.getContentPane());
			return;
		}
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			deleteMSMSClusterDataSetFromDatabase(selected);
		else
			deleteMSMSClusterDataSetFromProject(selected);
	}
	
	private void deleteMSMSClusterDataSetFromProject(MSMSClusterDataSet selected) {

		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();	
		if(!project.getEditableMsmsClusterDataSets().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be deleted.", this.getContentPane());
			return;
		}	
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete data set \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			project.getMsmsClusterDataSets().remove(selected);
			msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
					project.getMsmsClusterDataSets());
			
			IDWorkbenchPanel panel = 
					(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
			if(panel.getActiveMSMSClusterDataSet() != null 
					&& panel.getActiveMSMSClusterDataSet().equals(selected)) {
				panel.clearMSMSFeatureData();
				panel.clearMSMSClusterData();
			}
		}
	}

	private void deleteMSMSClusterDataSetFromDatabase(MSMSClusterDataSet selected) {
		
		if(!MSMSClusterDataSetManager.getEditableMSMSClusterDataSets().contains(selected)) {
			
			MessageDialog.showErrorMsg(
					"Data set \"" + selected.getName() + 
					"\" is locked and can not be deleted.", 
					this.getContentPane());
			return;
		}				
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
			
		try {
			MSMSClusteringDBUtils.deleteMSMSClusterDataSet(selected);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MSMSClusterDataSetManager.getMSMSClusterDataSets().remove(selected);
		msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
				MSMSClusterDataSetManager.getMSMSClusterDataSets());
		
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		if(panel.getActiveMSMSClusterDataSet() != null 
				&& panel.getActiveMSMSClusterDataSet().equals(selected)) {
			panel.clearMSMSFeatureData();
			panel.clearMSMSClusterData();
		}
	}

	public Collection<MsFeatureInfoBundleCluster> getClustersToAdd() {
		return clustersToAdd;
	}
	 
	public void setClustersToAdd(Collection<MsFeatureInfoBundleCluster> clustersToAdd) {
		this.clustersToAdd = clustersToAdd;
	}
	
	private void loadMSMSClusterDataSet() {
		
		MSMSClusterDataSet selectedDataSet = 
				msmsClusterDataSetTable.getSelectedDataSet();
		if(selectedDataSet == null)
			return;	
		
		loadMSMSClusterDataSetIntoWorkBench(selectedDataSet);		
	}
	
	public void loadMSMSClusterDataSetIntoWorkBench(MSMSClusterDataSet selectedDataSet) {
		
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);		
		panel.loadMSMSClusterDataSet(selectedDataSet);
		parent.dispose();
	}
	
	public void loadDatabaseStoredMSMSClusterDataSets() {
		msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
				MSMSClusterDataSetManager.getMSMSClusterDataSets());
	}
	
	public void loadMSMSClusterDataSetsForActiveProject() {
		
		RawDataAnalysisProject project = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisProject();
		if(project == null)
			return;
		
		msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
				project.getMsmsClusterDataSets());
	}
	
	public void setTableModelFromMSMSClusterDataSetList(
			Collection<MSMSClusterDataSet> dataSetList) {
		msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(dataSetList);
	}
	
	public MSMSClusterDataSet getSelectedMSMSClusterDataSet() {	
		return msmsClusterDataSetTable.getSelectedDataSet();
	}
	
	public void selectDataSet(MSMSClusterDataSet toSelect) {		
		msmsClusterDataSetTable.selectDataSet(toSelect);
	}
	
	public MSMSClusterDataSetsTable getTable() {
		return msmsClusterDataSetTable;
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MSMSClusterDataSetUploadTask.class)) {
				finalizeMSMSClusterDataSetUploadTask((MSMSClusterDataSetUploadTask)e.getSource());
			}
		}
	}

	private void finalizeMSMSClusterDataSetUploadTask(MSMSClusterDataSetUploadTask task) {

		MSMSClusterDataSet newDataSet = task.getDataSet();
		if(msmsClusterDataSetEditorDialog.loadMSMSClusterDataSetIntoWorkBench()) {
			loadMSMSClusterDataSetIntoWorkBench(newDataSet);
			msmsClusterDataSetEditorDialog.dispose();
			parent.dispose();
		}
		else {
			msmsClusterDataSetEditorDialog.dispose();	
			msmsClusterDataSetTable.setTableModelFromMSMSClusterDataSetList(
					MSMSClusterDataSetManager.getMSMSClusterDataSets());
			msmsClusterDataSetTable.selectDataSet(newDataSet);		
		}
	}
}



















