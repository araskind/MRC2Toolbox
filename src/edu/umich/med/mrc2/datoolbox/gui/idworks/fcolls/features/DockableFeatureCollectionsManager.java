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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureAndClusterCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;

public class DockableFeatureCollectionsManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private FeatureCollectionsManagerToolbar toolbar;
	private FeatureCollectionsTable featureCollectionsTable;
	private FeatureAndClusterCollectionManagerDialog parent;
	
	private FeatureCollectionEditorDialog msFeatureCollectionEditorDialog;
	private Collection<MsFeatureInfoBundle> featuresToAdd;
	
	public DockableFeatureCollectionsManager(FeatureAndClusterCollectionManagerDialog parent)  {

		super("DockableFeatureCollectionsManager", componentIcon, "Feature collections", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new FeatureCollectionsManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		featureCollectionsTable = new FeatureCollectionsTable();
		FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				FeatureCollectionManager.getMsFeatureInfoBundleCollections());
		
		featureCollectionsTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							MsFeatureInfoBundleCollection selected = 
									featureCollectionsTable.getSelectedCollection();
							if(selected == null)
								return;
							
							loadFeatureCollection();
						}
					}
				});
		add(new JScrollPane(featureCollectionsTable));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_FEATURE_COLLECTION_DIALOG_COMMAND.getName()))
			showMsFeatureCollectionEditorDialog(null); 
		
		if(command.equals(MainActionCommands.ADD_FEATURE_COLLECTION_COMMAND.getName())
				|| command.equals(MainActionCommands.ADD_FEATURE_COLLECTION_WITH_FEATURES_COMMAND.getName()))
			createNewFeatureCollection();
		
		if(command.equals(MainActionCommands.EDIT_FEATURE_COLLECTION_DIALOG_COMMAND.getName()))
			etitSelectedFeatureCollection();
		
		if(command.equals(MainActionCommands.EDIT_FEATURE_COLLECTION_COMMAND.getName()))
			saveEditedFeatureCollectionData();
		
		if(command.equals(MainActionCommands.DELETE_FEATURE_COLLECTION_COMMAND.getName()))
			 deleteFeatureCollection();
		
		if(command.equals(MainActionCommands.LOAD_FEATURE_COLLECTION_COMMAND.getName()))
			 loadFeatureCollection() ;
	}
	
	public void showMsFeatureCollectionEditorDialog (MsFeatureInfoBundleCollection collection) {
		
		msFeatureCollectionEditorDialog = new FeatureCollectionEditorDialog(collection, featuresToAdd, this);
		msFeatureCollectionEditorDialog.setLocationRelativeTo(this.getContentPane());
		msFeatureCollectionEditorDialog.setVisible(true);
	}

	private void saveEditedFeatureCollectionData() {
		
		Collection<String>errors = msFeatureCollectionEditorDialog.validateCollectionData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msFeatureCollectionEditorDialog);
			return;
		}
		MsFeatureInfoBundleCollection edited = 
				msFeatureCollectionEditorDialog.getFeatureCollection();
		edited.setName(msFeatureCollectionEditorDialog.getFeatureCollectionName());
		edited.setDescription(msFeatureCollectionEditorDialog.getFeatureCollectionDescription());
		edited.setLastModified(new Date());
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			saveFeatureCollectionChangesToDatabase(edited);
		else
			saveFeatureCollectionChangesToProject(edited);
	}
	
	private void saveFeatureCollectionChangesToProject(MsFeatureInfoBundleCollection edited) {
		
		edited.addFeatures(msFeatureCollectionEditorDialog.getFeaturesToAdd());
		if(msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench()) {
			loadCollectionIntoWorkBench(edited);
			msFeatureCollectionEditorDialog.dispose();	
			parent.dispose();
		}
		else {	
			msFeatureCollectionEditorDialog.dispose();	
			featureCollectionsTable.updateCollectionData(edited);
			featureCollectionsTable.selectCollection(edited);
		}
	}
	
	private void saveFeatureCollectionChangesToDatabase(MsFeatureInfoBundleCollection edited) { 
		
		try {
			FeatureCollectionUtils.updateMsFeatureInformationBundleCollectionMetadata(edited);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		Set<String>featureIds = new TreeSet<String>();
		featureIds.addAll(FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().get(edited));
		Set<String> featureIdsToAdd = msFeatureCollectionEditorDialog.getFeatureIdsToAdd();
		if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
			featureIds.addAll(featureIdsToAdd);				
			try {
				FeatureCollectionUtils.addFeaturesToCollection(edited.getId(), featureIdsToAdd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().get(edited).addAll(featureIdsToAdd);
		}			
		if(msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench()) {
			loadCollectionIntoWorkBench(edited);
			msFeatureCollectionEditorDialog.dispose();
			parent.dispose();
		}
		else {	
			msFeatureCollectionEditorDialog.dispose();
			featureCollectionsTable.updateCollectionData(edited);
			featureCollectionsTable.selectCollection(edited);
		}		
	}

	private void createNewFeatureCollection() {

		Collection<String>errors = msFeatureCollectionEditorDialog.validateCollectionData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msFeatureCollectionEditorDialog);
			return;
		}
		MsFeatureInfoBundleCollection newCollection = 
				new MsFeatureInfoBundleCollection(
						null, 
						msFeatureCollectionEditorDialog.getFeatureCollectionName(),
						msFeatureCollectionEditorDialog.getFeatureCollectionDescription(),
						new Date(), 
						new Date(),
						MRC2ToolBoxCore.getIdTrackerUser());
		
		if(msFeatureCollectionEditorDialog.getFeaturesToAdd() != null)
			newCollection.addFeatures(msFeatureCollectionEditorDialog.getFeaturesToAdd());
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			createNewFeatureCollectionInDatabase(newCollection);
		else
			createNewFeatureCollectionInProject(newCollection);
	}
	
	private void createNewFeatureCollectionInProject(MsFeatureInfoBundleCollection newCollection) {
		
		RawDataAnalysisProject project = 
				MRC2ToolBoxCore.getActiveRawDataAnalysisProject();		
		project.addMsFeatureInfoBundleCollection(newCollection);
		
		if(msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench()) {
			loadCollectionIntoWorkBench(newCollection);
			msFeatureCollectionEditorDialog.dispose();	
			parent.dispose();
		}
		else {
			msFeatureCollectionEditorDialog.dispose();	
			featureCollectionsTable.setTableModelFromFeatureCollectionList(
					project.getFeatureCollections());	
			featuresToAdd = null;
		}
	}
		
	private void createNewFeatureCollectionInDatabase(MsFeatureInfoBundleCollection newCollection) {

		String newId = null;
		try {
			newId = FeatureCollectionUtils.addNewMsFeatureInformationBundleCollection(newCollection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newId != null) {
			
			newCollection.setId(newId);
			Set<String>featureIds = new TreeSet<String>();
			featureIds.addAll(newCollection.getMSMSFeatureIds());
			Set<String> featureIdsToAdd = msFeatureCollectionEditorDialog.getFeatureIdsToAdd();
			if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
				featureIds.addAll(featureIdsToAdd);				
				try {
					FeatureCollectionUtils.addFeaturesToCollection(newId, featureIdsToAdd);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			FeatureCollectionManager.getFeatureCollectionsMsmsIdMap().put(newCollection, featureIds);					
			if(msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench()) {
				loadCollectionIntoWorkBench(newCollection);
				msFeatureCollectionEditorDialog.dispose();
				parent.dispose();
			}
			else {
				msFeatureCollectionEditorDialog.dispose();
				featureCollectionsTable.setTableModelFromFeatureCollectionList(
						FeatureCollectionManager.getMsFeatureInfoBundleCollections());	
				featuresToAdd = null;
			}
		}	
		featuresToAdd = null;
	}
	
	private void etitSelectedFeatureCollection() {

		MsFeatureInfoBundleCollection selected = featureCollectionsTable.getSelectedCollection();
		if(selected == null)
			return;
		
		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be edited.", this.getContentPane());
			return;
		}
//		if(selected.equals(FeatureCollectionManager.msmsSearchResults) 
//				|| selected.equals(FeatureCollectionManager.msOneSearchResults)) {
//			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
//					"\" is locked and can not be edited.", this);
//			return;
//		}		
		showMsFeatureCollectionEditorDialog(selected);
	}


	private void deleteFeatureCollection() {
		
//		MsFeatureInfoBundleCollection selected = featureCollectionsTable.getSelectedCollection();
//		if(selected.equals(FeatureCollectionManager.msmsSearchResults) 
//				|| selected.equals(FeatureCollectionManager.msOneSearchResults)) {
//			MessageDialog.showErrorMsg("Collection \"" + selected.getName() + 
//					"\" represents database search results and can not be deleted.", this);
//			return;
//		}
		MsFeatureInfoBundleCollection selected = 
				featureCollectionsTable.getSelectedCollection();
		if(selected == null)
			return;
		
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			deleteFeatureCollectionFromDatabase(selected);
		else
			deleteFeatureCollectionFromProject(selected);
	}
	
	private void deleteFeatureCollectionFromProject(MsFeatureInfoBundleCollection selected) {

		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();	
		if(!project.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be deleted.", this.getContentPane());
			return;
		}	
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			project.removeMsFeatureInfoBundleCollection(selected);
			featureCollectionsTable.setTableModelFromFeatureCollectionList(
					project.getFeatureCollections());	
			
			IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
			if(panel.getActiveFeatureCollection() != null 
					&& panel.getActiveFeatureCollection().equals(selected))
				panel.clearMSMSFeatureData();
		}
	}

	private void deleteFeatureCollectionFromDatabase(MsFeatureInfoBundleCollection selected) {
		
		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be deleted.", this.getContentPane());
			return;
		}		
		if(MRC2ToolBoxCore.getIdTrackerUser().isSuperUser() || 
				(selected.getOwner() != null && selected.getOwner().equals(MRC2ToolBoxCore.getIdTrackerUser()))) {
			int res = MessageDialog.showChoiceWithWarningMsg(
					"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
					this.getContentPane());
			if(res == JOptionPane.YES_OPTION) {
				try {
					FeatureCollectionUtils.deleteMsFeatureInformationBundleCollection(selected);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				FeatureCollectionManager.getMsFeatureInfoBundleCollections().remove(selected);
				featureCollectionsTable.setTableModelFromFeatureCollectionList(
						FeatureCollectionManager.getMsFeatureInfoBundleCollections());
				
				IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
				if(panel.getActiveFeatureCollection() != null 
						&& panel.getActiveFeatureCollection().equals(selected))
					panel.clearMSMSFeatureData();
			}
		}
		else {
			MessageDialog.showErrorMsg(
					"You are not authorized to delete this feature collection", 
					this.getContentPane());
			return;
		}
	}

	public Collection<MsFeatureInfoBundle> getFeaturesToAdd() {
		return featuresToAdd;
	}

	public void setFeaturesToAdd(Collection<MsFeatureInfoBundle> featuresToAdd) {
		this.featuresToAdd = featuresToAdd;
	}
	
	private void loadFeatureCollection() {
		
		MsFeatureInfoBundleCollection selectedCollection = 
				featureCollectionsTable.getSelectedCollection();
		if(selectedCollection == null)
			return;
		
		loadCollectionIntoWorkBench(selectedCollection);		
	}
	
	public void loadCollectionIntoWorkBench(MsFeatureInfoBundleCollection selectedCollection) {
		
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);		
		panel.loadMSMSFeatureInformationBundleCollection(selectedCollection);
		parent.dispose();
	}
	
	public void loadDatabaseStoredCollections() {

		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				FeatureCollectionManager.getMsFeatureInfoBundleCollections());
	}
	
	public void loadCollectionsForActiveProject() {
		
		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveRawDataAnalysisProject();
		if(project == null)
			return;
		
		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				project.getFeatureCollections());
	}
	
	public void setTableModelFromFeatureCollectionList(
			Collection<MsFeatureInfoBundleCollection>featureCollections) {
		featureCollectionsTable.setTableModelFromFeatureCollectionList(featureCollections);
	}
	
	public MsFeatureInfoBundleCollection getSelectedCollection() {	
		return featureCollectionsTable.getSelectedCollection();
	}
	
	public void selectCollection(MsFeatureInfoBundleCollection toSelect) {		
		featureCollectionsTable.selectCollection(toSelect);
	}
	
	public FeatureCollectionsTable getTable() {
		return featureCollectionsTable;
	}
}
