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

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureAndClusterCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;

public class DockableMSMSClusterDataSetsManager extends DefaultSingleCDockable implements ActionListener {

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
			showMsFeatureCollectionEditorDialog(null); 
		
		if(command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_COMMAND.getName())
				|| command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_WITH_CLUSTERS_COMMAND.getName()))
			createNewFeatureCollection();
		
		if(command.equals(MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName()))
			etitSelectedFeatureCollection();
		
		if(command.equals(MainActionCommands.EDIT_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			saveEditedMSMSClusterDataSet();
		
		if(command.equals(MainActionCommands.DELETE_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			 deleteFeatureCollection();
		
		if(command.equals(MainActionCommands.LOAD_MSMS_CLUSTER_DATASET_COMMAND.getName()))
			loadMSMSClusterDataSet() ;
	}
	
	public void showMsFeatureCollectionEditorDialog (MSMSClusterDataSet dataSet) {
		
		msmsClusterDataSetEditorDialog = 
				new MSMSClusterDataSetEditorDialog(dataSet, clustersToAdd, this);
		msmsClusterDataSetEditorDialog.setLocationRelativeTo(this.getContentPane());
		msmsClusterDataSetEditorDialog.setVisible(true);
	}

	private void saveEditedMSMSClusterDataSet() {
		
//		Collection<String>errors = msFeatureCollectionEditorDialog.validateCollectionData();
//		if(!errors.isEmpty()) {
//			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msFeatureCollectionEditorDialog);
//			return;
//		}
//		MsFeatureInfoBundleCollection edited = 
//				msFeatureCollectionEditorDialog.getFeatureCollection();
//		edited.setName(msFeatureCollectionEditorDialog.getFeatureCollectionName());
//		edited.setDescription(msFeatureCollectionEditorDialog.getFeatureCollectionDescription());
//		edited.setLastModified(new Date());
//		
//		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
//			saveFeatureCollectionChangesToDatabase(edited);
//		else
//			saveFeatureCollectionChangesToProject(edited);
	}
	
	private void saveFeatureCollectionChangesToProject(MsFeatureInfoBundleCollection edited) {
		
//		edited.addFeatures(msFeatureCollectionEditorDialog.getFeaturesToAdd());
//		boolean loadCollection = msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
//		msFeatureCollectionEditorDialog.dispose();	
//		if(loadCollection) {
//			loadCollectionIntoWorkBench(edited);
//			parent.dispose();
//		}
//		else {				
//			featureClusterCollectionsTable.updateCollectionData(edited);
//			featureClusterCollectionsTable.selectCollection(edited);
//		}
	}
	
	private void saveFeatureCollectionChangesToDatabase(MsFeatureInfoBundleCollection edited) { 
		
//		try {
//			FeatureCollectionUtils.updateMsFeatureInformationBundleCollectionMetadata(edited);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		Set<String>featureIds = new TreeSet<String>();
//		featureIds.addAll(FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().get(edited));
//		Set<String> featureIdsToAdd = msFeatureCollectionEditorDialog.getFeatureIdsToAdd();
//		if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
//			featureIds.addAll(featureIdsToAdd);				
//			try {
//				FeatureCollectionUtils.addFeaturesToCollection(edited.getId(), featureIdsToAdd);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		boolean loadCollection = msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
//		msFeatureCollectionEditorDialog.dispose();	
//		if(loadCollection) {
//			loadCollectionIntoWorkBench(edited);
//			parent.dispose();
//		}
//		else {
//			FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().put(edited, featureIds);					
//			featureClusterCollectionsTable.updateCollectionData(edited);
//			featureClusterCollectionsTable.selectCollection(edited);
//		}
	}

	private void createNewFeatureCollection() {

//		Collection<String>errors = msFeatureCollectionEditorDialog.validateCollectionData();
//		if(!errors.isEmpty()) {
//			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msFeatureCollectionEditorDialog);
//			return;
//		}
//		MsFeatureInfoBundleCollection newCollection = 
//				new MsFeatureInfoBundleCollection(
//						null, 
//						msFeatureCollectionEditorDialog.getFeatureCollectionName(),
//						msFeatureCollectionEditorDialog.getFeatureCollectionDescription(),
//						new Date(), 
//						new Date(),
//						MRC2ToolBoxCore.getIdTrackerUser());
//		
//		if(msFeatureCollectionEditorDialog.getFeaturesToAdd() != null)
//			newCollection.addFeatures(msFeatureCollectionEditorDialog.getFeaturesToAdd());
//		
//		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
//			createNewFeatureCollectionInDatabase(newCollection);
//		else
//			createNewFeatureCollectionInProject(newCollection);
	}
	
	private void createNewFeatureCollectionInProject(MsFeatureInfoBundleCollection newCollection) {
		
//		RawDataAnalysisProject project = 
//				MRC2ToolBoxCore.getActiveRawDataAnalysisProject();		
//		project.addMsFeatureInfoBundleCollection(newCollection);
//		boolean loadCollection = 
//				msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
//		msFeatureCollectionEditorDialog.dispose();	
//		if(loadCollection) {
//			loadCollectionIntoWorkBench(newCollection);
//			parent.dispose();
//		}
//		else {
//			featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//					project.getFeatureCollections());	
//			featuresToAdd = null;
//		}
	}
		
	private void createNewFeatureCollectionInDatabase(MsFeatureInfoBundleCollection newCollection) {

//		String newId = null;
//		try {
//			newId = FeatureCollectionUtils.addNewMsFeatureInformationBundleCollection(newCollection);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		boolean loadCollection = false;
//		if(newId != null) {
//			
//			newCollection.setId(newId);
//			Set<String>featureIds = new TreeSet<String>();
//			featureIds.addAll(newCollection.getMSMSFeatureIds());
//			Set<String> featureIdsToAdd = msFeatureCollectionEditorDialog.getFeatureIdsToAdd();
//			if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
//				featureIds.addAll(featureIdsToAdd);				
//				try {
//					FeatureCollectionUtils.addFeaturesToCollection(newId, featureIdsToAdd);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().put(newCollection, featureIds);	
//			loadCollection = msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
//			msFeatureCollectionEditorDialog.dispose();	
//			if(loadCollection) {
//				loadCollectionIntoWorkBench(newCollection);
//				parent.dispose();
//			}
//			else {
//				featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//						FeatureCollectionManager.getMsFeatureInfoBundleCollections());	
//				featuresToAdd = null;
//			}
//		}	
//		featuresToAdd = null;
	}
	
	private void etitSelectedFeatureCollection() {

//		MsFeatureInfoBundleCollection selected = featureClusterCollectionsTable.getSelectedDataSet();
//		if(selected == null)
//			return;
//		
//		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
//			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
//					"\" is locked and can not be edited.", this.getContentPane());
//			return;
//		}
////		if(selected.equals(FeatureCollectionManager.msmsSearchResults) 
////				|| selected.equals(FeatureCollectionManager.msOneSearchResults)) {
////			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
////					"\" is locked and can not be edited.", this);
////			return;
////		}		
//		showMsFeatureCollectionEditorDialog(selected);
	}


	private void deleteFeatureCollection() {
		
//		MsFeatureInfoBundleCollection selected = featureCollectionsTable.getSelectedCollection();
//		if(selected.equals(FeatureCollectionManager.msmsSearchResults) 
//				|| selected.equals(FeatureCollectionManager.msOneSearchResults)) {
//			MessageDialog.showErrorMsg("Collection \"" + selected.getName() + 
//					"\" represents database search results and can not be deleted.", this);
//			return;
//		}
		
//		MsFeatureInfoBundleCollection selected = 
//				featureClusterCollectionsTable.getSelectedDataSet();
//		if(selected == null)
//			return;
//		
//		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
//			deleteFeatureCollectionFromDatabase(selected);
//		else
//			deleteFeatureCollectionFromProject(selected);
	}
	
	private void deleteFeatureCollectionFromProject(MsFeatureInfoBundleCollection selected) {

//		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();	
//		if(!project.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
//			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
//					"\" is locked and can not be deleted.", this.getContentPane());
//			return;
//		}	
//		int res = MessageDialog.showChoiceWithWarningMsg(
//				"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
//				this.getContentPane());
//		if(res == JOptionPane.YES_OPTION) {
//			
//			project.removeMsFeatureInfoBundleCollection(selected);
//			featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//					project.getFeatureCollections());	
//			
//			IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
//			if(panel.getActiveFeatureCollection() != null 
//					&& panel.getActiveFeatureCollection().equals(selected))
//				panel.clearMSMSFeatureData();
//		}
	}

	private void deleteFeatureCollectionFromDatabase(MsFeatureInfoBundleCollection selected) {
		
//		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
//			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
//					"\" is locked and can not be deleted.", this.getContentPane());
//			return;
//		}		
//		if(MRC2ToolBoxCore.getIdTrackerUser().isSuperUser() || 
//				(selected.getOwner() != null && selected.getOwner().equals(MRC2ToolBoxCore.getIdTrackerUser()))) {
//			int res = MessageDialog.showChoiceWithWarningMsg(
//					"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
//					this.getContentPane());
//			if(res == JOptionPane.YES_OPTION) {
//				try {
//					FeatureCollectionUtils.deleteMsFeatureInformationBundleCollection(selected);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				FeatureCollectionManager.getMsFeatureInfoBundleCollections().remove(selected);
//				featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//						FeatureCollectionManager.getMsFeatureInfoBundleCollections());
//				
//				IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
//				if(panel.getActiveFeatureCollection() != null 
//						&& panel.getActiveFeatureCollection().equals(selected))
//					panel.clearMSMSFeatureData();
//			}
//		}
//		else {
//			MessageDialog.showErrorMsg(
//					"You are not authorized to delete this feature collection", 
//					this.getContentPane());
//			return;
//		}
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
	
	public void loadDatabaseStoredCollections() {
//		FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
//		featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//				FeatureCollectionManager.getMsFeatureInfoBundleCollections());
	}
	
	public void loadCollectionsForActiveProject() {
		
//		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();
//		if(project == null)
//			return;
//		
//		featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(
//				project.getFeatureCollections());
	}
	
	public void setTableModelFromFeatureCollectionList(
			Collection<MsFeatureInfoBundleCollection>featureCollections) {
//		featureClusterCollectionsTable.setTableModelFromFeatureCollectionList(featureCollections);
	}
	
	public MsFeatureInfoBundleCollection getSelectedCollection() {	
//		return featureClusterCollectionsTable.getSelectedDataSet();
		return null;
	}
	
	public void selectCollection(MsFeatureInfoBundleCollection toSelect) {		
		msmsClusterDataSetTable.selectCollection(toSelect);
	}
	
	public MSMSClusterDataSetsTable getTable() {
		return msmsClusterDataSetTable;
	}
}
