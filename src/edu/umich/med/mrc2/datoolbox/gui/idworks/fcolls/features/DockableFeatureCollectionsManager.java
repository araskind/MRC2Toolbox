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
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.DataCollectionsManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;

public class DockableFeatureCollectionsManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private FeatureCollectionsManagerToolbar toolbar;
	private FeatureCollectionsTable featureCollectionsTable;
	private DataCollectionsManagerDialog parent;
	
	private FeatureCollectionEditorDialog msFeatureCollectionEditorDialog;
	private Collection<MSFeatureInfoBundle> featuresToAdd;
	
	public DockableFeatureCollectionsManager(DataCollectionsManagerDialog parent)  {

		super("DockableFeatureCollectionsManager", componentIcon, "Feature collections", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new FeatureCollectionsManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		featureCollectionsTable = new FeatureCollectionsTable();
		FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());
		
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
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			InsertOrUpdateDatabaseCollectionTask task = 
					new InsertOrUpdateDatabaseCollectionTask(edited, false);
			IndeterminateProgressDialog idp = 
					new IndeterminateProgressDialog(
							"Saving MSMS feature collection changes to database ...", 
							this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
			//	saveFeatureCollectionChangesToDatabase(edited);
		}
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
		if(edited.getFeatureIds().isEmpty()) {
			
			Set<String>dbIds = new TreeSet<String>();
			try {
				dbIds = FeatureCollectionUtils.
						getFeatureIdsForMsFeatureInfoBundleCollection(edited.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!dbIds.isEmpty())
				edited.getFeatureIds().addAll(dbIds);
		}
		Set<String> featureIdsToAdd = 
				msFeatureCollectionEditorDialog.getMsFeatureIdsToAdd().
				stream().filter(f -> !edited.getFeatureIds().contains(f)).collect(Collectors.toSet());
				
		if(!featureIdsToAdd.isEmpty()) {
		
			try {
				FeatureCollectionUtils.addFeaturesToCollection(edited.getId(), featureIdsToAdd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			edited.getFeatureIds().addAll(featureIdsToAdd);
			edited.setCollectionSize(edited.getFeatureIds().size());
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
	
	class InsertOrUpdateDatabaseCollectionTask extends LongUpdateTask {

		MsFeatureInfoBundleCollection msfCollection;
		boolean isNew;

		public InsertOrUpdateDatabaseCollectionTask(
				MsFeatureInfoBundleCollection msfCollection, 
				boolean isNew) {
			super();
			this.msfCollection = msfCollection;
			this.isNew = isNew;
		}

		@Override
		public Void doInBackground() {
			
			if(isNew)
				createNewFeatureCollectionInDatabase(msfCollection);
			else
				saveFeatureCollectionChangesToDatabase(msfCollection);

			return null;
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
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			InsertOrUpdateDatabaseCollectionTask task = 
					new InsertOrUpdateDatabaseCollectionTask(newCollection, true);
			IndeterminateProgressDialog idp = 
					new IndeterminateProgressDialog(
							"Saving new MSMS feature collection to database ...", 
							this.getContentPane(), task);
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
			//	createNewFeatureCollectionInDatabase(newCollection);
		}
		else
			createNewFeatureCollectionInProject(newCollection);
	}
	
	private void createNewFeatureCollectionInProject(MsFeatureInfoBundleCollection newCollection) {
		
		RawDataAnalysisProject project = 
				MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();		
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
			Set<String> featureIdsToAdd = 
					msFeatureCollectionEditorDialog.getMsFeatureIdsToAdd();
			
			if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
		
				try {
					FeatureCollectionUtils.addFeaturesToCollection(newId, featureIdsToAdd);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				newCollection.getFeatureIds().addAll(featureIdsToAdd);
				newCollection.setCollectionSize(newCollection.getFeatureIds().size());
			}
			FeatureCollectionManager.getfeatureCollectionsMSIDSet().add(newCollection);			
			
			if(msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench()) {
				loadCollectionIntoWorkBench(newCollection);
				msFeatureCollectionEditorDialog.dispose();
				parent.dispose();
			}
			else {
				msFeatureCollectionEditorDialog.dispose();
				featureCollectionsTable.setTableModelFromFeatureCollectionList(
						FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());	
				featuresToAdd = null;
			}
			RecentDataManager.addFeatureCollection(newCollection);
		}	
		featuresToAdd = null;
	}
	
	private void etitSelectedFeatureCollection() {

		MsFeatureInfoBundleCollection selected = 
				featureCollectionsTable.getSelectedCollection();
		if(selected == null)
			return;
		
		if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be edited.", this.getContentPane());
			return;
		}	
		showMsFeatureCollectionEditorDialog(selected);
	}

	private void deleteFeatureCollection() {
		
		MsFeatureInfoBundleCollection selected = 
				featureCollectionsTable.getSelectedCollection();
		if(selected == null)
			return;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			if(!selected.getOwner().equals(MRC2ToolBoxCore.getIdTrackerUser())
					&& !MRC2ToolBoxCore.getIdTrackerUser().isSuperUser()) {
				
				MessageDialog.showErrorMsg(
						"Data set \"" + selected.getName() + 
						"\" was created by a different user, you can not delete it.", 
						this.getContentPane());
				return;
			}
			deleteFeatureCollectionFromDatabase(selected);
			RecentDataManager.removeFeatureCollection(selected);
		}
		else {
			deleteFeatureCollectionFromProject(selected);
			RecentDataManager.removeFeatureCollection(selected);
		}
	}
	
	private void deleteFeatureCollectionFromProject(MsFeatureInfoBundleCollection selected) {

		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();	
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
			
			IDWorkbenchPanel panel = 
					(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
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
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		try {
			FeatureCollectionUtils.deleteMsFeatureInformationBundleCollection(selected);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FeatureCollectionManager.getMsFeatureInfoBundleCollections().remove(selected);
		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());
		
		IDWorkbenchPanel panel = 
				(IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
		if(panel.getActiveFeatureCollection() != null 
				&& panel.getActiveFeatureCollection().equals(selected))
			panel.clearMSMSFeatureData();		
	}

	public Collection<MSFeatureInfoBundle> getFeaturesToAdd() {
		return featuresToAdd;
	}

	public void setFeaturesToAdd(Collection<MSFeatureInfoBundle> featuresToAdd) {
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
				FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());
	}
	
	public void loadCollectionsForActiveProject() {
		
		RawDataAnalysisProject project = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();
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
