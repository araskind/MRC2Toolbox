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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureAndClusterCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableFeatureLookupDataSetManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private FeatureLookupDataSetManagerToolbar toolbar;
	private FeatureLookupDataSetListTable featureCollectionsTable;
	private FeatureAndClusterCollectionManagerDialog parent;	
	private FeatureLookupDataSetEditorDialog featureLookupDataSetEditorDialog;

	public DockableFeatureLookupDataSetManager(FeatureAndClusterCollectionManagerDialog parent)  {

		super("DockableFeatureLookupDataSetManager", componentIcon, "Lookup feature data sets", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new FeatureLookupDataSetManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		featureCollectionsTable = new FeatureLookupDataSetListTable();
		featureCollectionsTable.setTableModelFromFeatureLookupDataSetList(
				FeatureLookupDataSetManager.getFeatureLookupDataSetList());		
		featureCollectionsTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							FeatureLookupDataSet selected = 
									featureCollectionsTable.getSelectedDataSet();
							if(selected == null)
								return;
							
//							loadFeatureCollection();
						}
					}
				});
		add(new JScrollPane(featureCollectionsTable));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_FEATURE_LOOKUP_DATA_SET_DIALOG_COMMAND.getName()))
			showFeatureLookupDataSetEditorDialog(null); 
		
		if(command.equals(MainActionCommands.ADD_FEATURE_LOOKUP_DATA_SET_COMMAND.getName()))
			createNewFeatureLookupDataSet();
		
		if(command.equals(MainActionCommands.EDIT_FEATURE_LOOKUP_DATA_SET_DIALOG_COMMAND.getName()))
			etitSelectedFeatureLookupDataSet();
		
		if(command.equals(MainActionCommands.EDIT_FEATURE_LOOKUP_DATA_SET_COMMAND.getName()))
			saveEditedFeatureLookupDataSet();
		
		if(command.equals(MainActionCommands.DELETE_FEATURE_LOOKUP_DATA_SET_COMMAND.getName()))
			 deleteFeatureLookupDataSet();
	}
	
	public void showFeatureLookupDataSetEditorDialog (FeatureLookupDataSet datSet) {
		
		featureLookupDataSetEditorDialog = new FeatureLookupDataSetEditorDialog(datSet, this);
		featureLookupDataSetEditorDialog.setLocationRelativeTo(this.getContentPane());
		featureLookupDataSetEditorDialog.setVisible(true);
	}

	private void saveEditedFeatureLookupDataSet() {
		
		Collection<String>errors = featureLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					featureLookupDataSetEditorDialog);
			return;
		}	
		FeatureLookupDataSet edited = featureLookupDataSetEditorDialog.getFeatureLookupDataSet();

		edited.setName(featureLookupDataSetEditorDialog.getDataSetName());
		edited.setDescription(featureLookupDataSetEditorDialog.getDataSetDescription());
		edited.setLastModified(new Date());
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			saveFeatureCollectionChangesToDatabase(edited);
		else
			saveFeatureCollectionChangesToProject(edited);
	}
	
	private void saveFeatureCollectionChangesToProject(FeatureLookupDataSet edited) {
		
//		edited.addFeatures(featureLookupDataSetEditorDialog.getFeaturesToAdd());
//		if(featureLookupDataSetEditorDialog.loadCollectionIntoWorkBench()) {
//			loadCollectionIntoWorkBench(edited);
//			featureLookupDataSetEditorDialog.dispose();	
//			parent.dispose();
//		}
//		else {	
//			featureLookupDataSetEditorDialog.dispose();	
//			featureCollectionsTable.updateCollectionData(edited);
//			featureCollectionsTable.selectCollection(edited);
//		}
	}
	
	private void saveFeatureCollectionChangesToDatabase(FeatureLookupDataSet edited) { 
		
//		try {
//			FeatureCollectionUtils.updateMsFeatureInformationBundleCollectionMetadata(edited);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		Set<String>featureIds = new TreeSet<String>();
//		featureIds.addAll(FeatureCollectionManager.getFeatureCollectionsMsIdMap().get(edited));
//		Set<String> featureIdsToAdd = 
//				featureLookupDataSetEditorDialog.getMsFeatureIdsToAdd();
//		if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
//			featureIds.addAll(featureIdsToAdd);				
//			try {
//				FeatureCollectionUtils.addFeaturesToCollection(edited.getId(), featureIdsToAdd);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			FeatureCollectionManager.getFeatureCollectionsMsIdMap().get(edited).addAll(featureIdsToAdd);
//		}			
//		if(featureLookupDataSetEditorDialog.loadCollectionIntoWorkBench()) {
//			loadCollectionIntoWorkBench(edited);
//			featureLookupDataSetEditorDialog.dispose();
//			parent.dispose();
//		}
//		else {	
//			featureLookupDataSetEditorDialog.dispose();
//			featureCollectionsTable.updateCollectionData(edited);
//			featureCollectionsTable.selectCollection(edited);
//		}		
	}

	private void createNewFeatureLookupDataSet() {

		Collection<String>errors = 
				featureLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					featureLookupDataSetEditorDialog);
			return;
		}
//		MsFeatureInfoBundleCollection newCollection = 
//				new MsFeatureInfoBundleCollection(
//						null, 
//						featureLookupDataSetEditorDialog.getFeatureCollectionName(),
//						featureLookupDataSetEditorDialog.getFeatureCollectionDescription(),
//						new Date(), 
//						new Date(),
//						MRC2ToolBoxCore.getIdTrackerUser());
//		
//		if(featureLookupDataSetEditorDialog.getFeaturesToAdd() != null)
//			newCollection.addFeatures(featureLookupDataSetEditorDialog.getFeaturesToAdd());
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
//		
//		if(featureLookupDataSetEditorDialog.loadCollectionIntoWorkBench()) {
//			loadCollectionIntoWorkBench(newCollection);
//			featureLookupDataSetEditorDialog.dispose();	
//			parent.dispose();
//		}
//		else {
//			featureLookupDataSetEditorDialog.dispose();	
//			featureCollectionsTable.setTableModelFromFeatureCollectionList(
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
//		if(newId != null) {
//			
//			newCollection.setId(newId);
//			Set<String>featureIds = new TreeSet<String>();
//			featureIds.addAll(newCollection.getMsFeatureIds());
//			Set<String> featureIdsToAdd = 
//					featureLookupDataSetEditorDialog.getMsFeatureIdsToAdd();
//			if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
//				featureIds.addAll(featureIdsToAdd);				
//				try {
//					FeatureCollectionUtils.addFeaturesToCollection(newId, featureIdsToAdd);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			FeatureCollectionManager.getFeatureCollectionsMsIdMap().put(newCollection, featureIds);					
//			if(featureLookupDataSetEditorDialog.loadCollectionIntoWorkBench()) {
//				loadCollectionIntoWorkBench(newCollection);
//				featureLookupDataSetEditorDialog.dispose();
//				parent.dispose();
//			}
//			else {
//				featureLookupDataSetEditorDialog.dispose();
//				featureCollectionsTable.setTableModelFromFeatureCollectionList(
//						FeatureCollectionManager.getMsFeatureInfoBundleCollections());	
//				featuresToAdd = null;
//			}
//		}	
//		featuresToAdd = null;
	}
	
	private void etitSelectedFeatureLookupDataSet() {

//		MsFeatureInfoBundleCollection selected = 
//				featureCollectionsTable.getSelectedCollection();
//		if(selected == null)
//			return;
//		
//		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
//			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
//					"\" is locked and can not be edited.", this.getContentPane());
//			return;
//		}	
//		showMsFeatureCollectionEditorDialog(selected);
	}

	private void deleteFeatureLookupDataSet() {
		
//		MsFeatureInfoBundleCollection selected = 
//				featureCollectionsTable.getSelectedCollection();
//		if(selected == null)
//			return;
//		
//		if(!selected.getOwner().equals(MRC2ToolBoxCore.getIdTrackerUser())
//				&& !MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
//			
//			MessageDialog.showErrorMsg(
//					"Data set \"" + selected.getName() + 
//					"\" was created by a different user, you can not delete it.", 
//					this.getContentPane());
//			return;
//		}
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
//			featureCollectionsTable.setTableModelFromFeatureCollectionList(
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
//		int res = MessageDialog.showChoiceWithWarningMsg(
//				"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
//				this.getContentPane());
//		if(res != JOptionPane.YES_OPTION)
//			return;
//		
//		try {
//			FeatureCollectionUtils.deleteMsFeatureInformationBundleCollection(selected);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		FeatureCollectionManager.getMsFeatureInfoBundleCollections().remove(selected);
//		featureCollectionsTable.setTableModelFromFeatureCollectionList(
//				FeatureCollectionManager.getMsFeatureInfoBundleCollections());
//		
//		IDWorkbenchPanel panel = 
//				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
//		if(panel.getActiveFeatureCollection() != null 
//				&& panel.getActiveFeatureCollection().equals(selected))
//			panel.clearMSMSFeatureData();		
	}
	
	public void loadDatabaseStoredCollections() {
//
//		featureCollectionsTable.setTableModelFromFeatureCollectionList(
//				FeatureCollectionManager.getMsFeatureInfoBundleCollections());
	}
	
	public void loadCollectionsForActiveProject() {
		
//		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();
//		if(project == null)
//			return;
//		
//		featureCollectionsTable.setTableModelFromFeatureCollectionList(
//				project.getFeatureCollections());
	}

	public FeatureLookupDataSetListTable getTable() {
		return featureCollectionsTable;
	}
}
