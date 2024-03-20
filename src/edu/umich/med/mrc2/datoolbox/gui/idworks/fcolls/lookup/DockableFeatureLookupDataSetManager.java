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
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupDataSetUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.DataCollectionsManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;

public class DockableFeatureLookupDataSetManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private FeatureLookupDataSetManagerToolbar toolbar;
	private FeatureLookupDataSetListTable featureLookupDataSetListTable;
	private DataCollectionsManagerDialog parent;	
	private FeatureLookupDataSetEditorDialog featureLookupDataSetEditorDialog;

	private IndeterminateProgressDialog idp;

	public DockableFeatureLookupDataSetManager(DataCollectionsManagerDialog parent)  {

		super("DockableFeatureLookupDataSetManager", componentIcon, "Lookup feature data sets", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new FeatureLookupDataSetManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		featureLookupDataSetListTable = new FeatureLookupDataSetListTable();
		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(
				FeatureLookupDataSetManager.getFeatureLookupDataSetList());		
		featureLookupDataSetListTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							FeatureLookupDataSet selected = 
									featureLookupDataSetListTable.getSelectedDataSet();
							if(selected == null)
								return;
							
							showFeatureLookupDataSetEditorDialog(selected); 
						}
					}
				});
		add(new JScrollPane(featureLookupDataSetListTable));
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

	private void createNewFeatureLookupDataSet() {

		Collection<String>errors = 
				featureLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					featureLookupDataSetEditorDialog);
			return;
		}
		FeatureLookupDataSet dataSet = new FeatureLookupDataSet(
				featureLookupDataSetEditorDialog.getDataSetName(), 
				featureLookupDataSetEditorDialog.getDataSetDescription(), 
				MRC2ToolBoxCore.getIdTrackerUser());
		dataSet.getFeatures().addAll(
				featureLookupDataSetEditorDialog.getAllFeatures());
			
		featureLookupDataSetEditorDialog.dispose();
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			createNewFeatureLookupDataSetInDatabase(dataSet);
		else
			createNewFeatureLookupDataSetInProject(dataSet);
	}
		
	private void createNewFeatureLookupDataSetInDatabase(FeatureLookupDataSet newDataSet) {

		UploadNewFeatureLookupDataSetTask task = 
				new UploadNewFeatureLookupDataSetTask(newDataSet);
		idp = new IndeterminateProgressDialog(
				"Uploading feature lookup data set ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class UploadNewFeatureLookupDataSetTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private FeatureLookupDataSet newDataSet;

		public UploadNewFeatureLookupDataSetTask(FeatureLookupDataSet newDataSet) {
			this.newDataSet = newDataSet;
		}

		@Override
		public Void doInBackground() {

			try {
				FeatureLookupDataSetUtils.addFeatureLookupDataSet(newDataSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FeatureLookupDataSetManager.refreshFeatureLookupDataSetList();
			featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(
					FeatureLookupDataSetManager.getFeatureLookupDataSetList());
			featureLookupDataSetListTable.selectDataSet(newDataSet);
			return null;
		}
	}
		
	private void createNewFeatureLookupDataSetInProject(FeatureLookupDataSet newDataSet) {
		
		MessageDialog.showWarningMsg(
				"Feature under development", this.getContentPane());
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

	private void etitSelectedFeatureLookupDataSet() {
		
		FeatureLookupDataSet selected = 
				featureLookupDataSetListTable.getSelectedDataSet();
		if(selected == null)
			return;
		
		showFeatureLookupDataSetEditorDialog(selected);
	}
	
	private void saveEditedFeatureLookupDataSet() {
		
		Collection<String>errors = 
				featureLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					featureLookupDataSetEditorDialog);
			return;
		}	
		FeatureLookupDataSet edited = 
				featureLookupDataSetEditorDialog.getFeatureLookupDataSet();
		edited.setName(featureLookupDataSetEditorDialog.getDataSetName());
		edited.setDescription(featureLookupDataSetEditorDialog.getDataSetDescription());
		edited.setLastModified(new Date());
		
		featureLookupDataSetEditorDialog.dispose();
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			saveFeatureLookupDataSetChangesToDatabase(edited);
		else
			saveFeatureLookupDataSetChangesToProject(edited);
	}
	
	private void saveFeatureLookupDataSetChangesToDatabase(FeatureLookupDataSet edited) { 
		
		try {
			FeatureLookupDataSetUtils.editFeatureLookupDataSetMetadata(edited);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FeatureLookupDataSetManager.refreshFeatureLookupDataSetList();
		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(
				FeatureLookupDataSetManager.getFeatureLookupDataSetList());		
		featureLookupDataSetListTable.selectDataSet(edited);
	}
	
	private void saveFeatureLookupDataSetChangesToProject(FeatureLookupDataSet edited) {
		
		MessageDialog.showWarningMsg(
				"Feature under development", this.getContentPane());
		
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

	private void deleteFeatureLookupDataSet() {
		
		FeatureLookupDataSet selected = 
				featureLookupDataSetListTable.getSelectedDataSet();
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
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			deleteFeatureLookupDataSetFromDatabase(selected);
		else
			deleteFeatureLookupDataSetFromProject(selected);
	}
	
	private void deleteFeatureLookupDataSetFromProject(FeatureLookupDataSet selected) {

		MessageDialog.showWarningMsg(
				"Feature under development", this.getContentPane());
		
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

	private void deleteFeatureLookupDataSetFromDatabase(FeatureLookupDataSet selected) {
			
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature lookup data set \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		try {
			FeatureLookupDataSetUtils.deleteFeatureLookupDataSet(selected);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		FeatureLookupDataSetManager.getFeatureLookupDataSetList().remove(selected);
		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(
				FeatureLookupDataSetManager.getFeatureLookupDataSetList());
	}
	
	public void loadDatabaseStoredFeatureLookupDataSets() {
		
		featureLookupDataSetListTable.clearTable();
		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(
				FeatureLookupDataSetManager.getFeatureLookupDataSetList());
	}
	
	public void loadFeatureLookupDataSetsForActiveProject() {
		
		featureLookupDataSetListTable.clearTable();		
		RawDataAnalysisExperiment project = 
				MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();
		if(project == null)
			return;
		
		List<FeatureLookupDataSet> flListCollection = 
				project.getMsmsClusterDataSets().stream().
					filter(ds -> ds.getFeatureLookupDataSet() != null).
					map(ds -> ds.getFeatureLookupDataSet()).distinct().
					collect(Collectors.toList());

		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(flListCollection);
	}

	public FeatureLookupDataSetListTable getTable() {
		return featureLookupDataSetListTable;
	}
}
