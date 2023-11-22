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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.binner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureAndClusterCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.BinnerAnnotationDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableBinnerAnnotationDataSetManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private BinnerAnnotationDataSetManagerToolbar toolbar;
	private BinnerAnnotationLookupDataSetListTable binnerAnnotationLookupDataSetListTable;
	private FeatureAndClusterCollectionManagerDialog parent;	
	private BinnerAnnotationsLookupDataSetEditorDialog binnerAnnotationLookupDataSetEditorDialog;

	private IndeterminateProgressDialog idp;

	public DockableBinnerAnnotationDataSetManager(FeatureAndClusterCollectionManagerDialog parent)  {

		super("DockableBinnerAnnotationDataSetManager", componentIcon, 
				"Binner annotation data sets", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parent = parent;
		
		toolbar = new BinnerAnnotationDataSetManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		binnerAnnotationLookupDataSetListTable = new BinnerAnnotationLookupDataSetListTable();
		binnerAnnotationLookupDataSetListTable.setTableModelFromBinnerAnnotationLookupDataSetList(
				BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList());		
		binnerAnnotationLookupDataSetListTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							BinnerAnnotationLookupDataSet selected = 
									binnerAnnotationLookupDataSetListTable.getSelectedDataSet();
							if(selected == null)
								return;
							
							showBinnerAnnotationDataSetEditorDialog(selected); 
						}
					}
				});
		add(new JScrollPane(binnerAnnotationLookupDataSetListTable));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_BINNER_ANNOTATIONS_DATA_SET_DIALOG_COMMAND.getName()))
			showBinnerAnnotationDataSetEditorDialog(null); 
		
		if(command.equals(MainActionCommands.ADD_BINNER_ANNOTATIONS_DATA_SET_COMMAND.getName()))
			createNewBinnerAnnotationLookupDataSet();
		
		if(command.equals(MainActionCommands.EDIT_BINNER_ANNOTATIONS_DATA_SET_DIALOG_COMMAND.getName()))
			etitSelectedBinnerAnnotationLookupDataSet();
		
		if(command.equals(MainActionCommands.EDIT_BINNER_ANNOTATIONS_DATA_SET_COMMAND.getName()))
			saveEditedBinnerAnnotationLookupDataSet();
		
		if(command.equals(MainActionCommands.DELETE_BINNER_ANNOTATIONS_DATA_SET_COMMAND.getName()))
			 deleteBinnerAnnotationLookupDataSet();
	}
	
	public void showBinnerAnnotationDataSetEditorDialog (BinnerAnnotationLookupDataSet datSet) {
		
		binnerAnnotationLookupDataSetEditorDialog = new BinnerAnnotationsLookupDataSetEditorDialog(datSet, this);
		binnerAnnotationLookupDataSetEditorDialog.setLocationRelativeTo(this.getContentPane());
		binnerAnnotationLookupDataSetEditorDialog.setVisible(true);
	}

	private void createNewBinnerAnnotationLookupDataSet() {

		Collection<String>errors = 
				binnerAnnotationLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					binnerAnnotationLookupDataSetEditorDialog);
			return;
		}
		BinnerAnnotationLookupDataSet dataSet = new BinnerAnnotationLookupDataSet(
				binnerAnnotationLookupDataSetEditorDialog.getDataSetName(), 
				binnerAnnotationLookupDataSetEditorDialog.getDataSetDescription(), 
				MRC2ToolBoxCore.getIdTrackerUser());
		dataSet.getBinnerAnnotationClusters().addAll(
				binnerAnnotationLookupDataSetEditorDialog.getAllClusters());
			
		binnerAnnotationLookupDataSetEditorDialog.dispose();
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			createNewBinnerAnnotationLookupDataSetInDatabase(dataSet);
		else
			createNewBinnerAnnotationLookupDataSetInProject(dataSet);
	}
		
	private void createNewBinnerAnnotationLookupDataSetInDatabase(BinnerAnnotationLookupDataSet newDataSet) {

		UploadNewBinnerAnnotationLookupDataSetTask task = 
				new UploadNewBinnerAnnotationLookupDataSetTask(newDataSet);
		idp = new IndeterminateProgressDialog(
				"Uploading feature lookup data set ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class UploadNewBinnerAnnotationLookupDataSetTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private BinnerAnnotationLookupDataSet newDataSet;

		public UploadNewBinnerAnnotationLookupDataSetTask(BinnerAnnotationLookupDataSet newDataSet) {
			this.newDataSet = newDataSet;
		}

		@Override
		public Void doInBackground() {

			try {
				BinnerUtils.addBinnerAnnotationLookupDataSet(newDataSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();
			binnerAnnotationLookupDataSetListTable.setTableModelFromBinnerAnnotationLookupDataSetList(
					BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList());
			binnerAnnotationLookupDataSetListTable.selectDataSet(newDataSet);
			return null;
		}
	}
		
	private void createNewBinnerAnnotationLookupDataSetInProject(BinnerAnnotationLookupDataSet newDataSet) {
		
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

	private void etitSelectedBinnerAnnotationLookupDataSet() {
		
		BinnerAnnotationLookupDataSet selected = 
				binnerAnnotationLookupDataSetListTable.getSelectedDataSet();
		if(selected == null)
			return;
		
		showBinnerAnnotationDataSetEditorDialog(selected);
	}
	
	private void saveEditedBinnerAnnotationLookupDataSet() {
		
		Collection<String>errors = 
				binnerAnnotationLookupDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					binnerAnnotationLookupDataSetEditorDialog);
			return;
		}	
		BinnerAnnotationLookupDataSet edited = 
				binnerAnnotationLookupDataSetEditorDialog.getBinnerAnnotationLookupDataSet();
		edited.setName(binnerAnnotationLookupDataSetEditorDialog.getDataSetName());
		edited.setDescription(binnerAnnotationLookupDataSetEditorDialog.getDataSetDescription());
		edited.setLastModified(new Date());
		
		binnerAnnotationLookupDataSetEditorDialog.dispose();
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			saveBinnerAnnotationLookupDataSetChangesToDatabase(edited);
		else
			saveBinnerAnnotationLookupDataSetChangesToProject(edited);
	}
	
	private void saveBinnerAnnotationLookupDataSetChangesToDatabase(BinnerAnnotationLookupDataSet edited) { 
		
		try {
			BinnerUtils.editBinnerAnnotationLookupDataSetMetadata(edited);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();;
		binnerAnnotationLookupDataSetListTable.setTableModelFromBinnerAnnotationLookupDataSetList(
				BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList());
		binnerAnnotationLookupDataSetListTable.selectDataSet(edited);
	}
	
	private void saveBinnerAnnotationLookupDataSetChangesToProject(BinnerAnnotationLookupDataSet edited) {
		
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

	private void deleteBinnerAnnotationLookupDataSet() {
		
		BinnerAnnotationLookupDataSet selected = 
				binnerAnnotationLookupDataSetListTable.getSelectedDataSet();
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
			deleteBinnerAnnotationLookupDataSetFromDatabase(selected);
		else
			deleteBinnerAnnotationLookupDataSetFromProject(selected);
	}
	
	private void deleteBinnerAnnotationLookupDataSetFromProject(BinnerAnnotationLookupDataSet selected) {

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

	private void deleteBinnerAnnotationLookupDataSetFromDatabase(BinnerAnnotationLookupDataSet selected) {
			
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete feature lookup data set \"" + selected.getName() + "\"?", 
				this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		try {
			BinnerUtils.deleteBinnerAnnotationLookupDataSet(selected);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList().remove(selected);
		binnerAnnotationLookupDataSetListTable.setTableModelFromBinnerAnnotationLookupDataSetList(
				BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList());
	}
	
	public void loadDatabaseStoredBinnerAnnotationLookupDataSets() {
		
		binnerAnnotationLookupDataSetListTable.clearTable();
		binnerAnnotationLookupDataSetListTable.setTableModelFromBinnerAnnotationLookupDataSetList(
				BinnerAnnotationDataSetManager.getBinnerAnnotationLookupDataSetList());
	}
	
	public void loadBinnerAnnotationLookupDataSetsForActiveProject() {
		
//		MessageDialog.showWarningMsg(
//				"Feature under development", this.getContentPane());
		
//		featureLookupDataSetListTable.clearTable();		
//		RawDataAnalysisExperiment project = 
//				MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment();
//		if(project == null)
//			return;
//		
//		List<FeatureLookupDataSet> flListCollection = 
//				project.getMsmsClusterDataSets().stream().
//					filter(ds -> ds.getFeatureLookupDataSet() != null).
//					map(ds -> ds.getFeatureLookupDataSet()).distinct().
//					collect(Collectors.toList());
//
//		featureLookupDataSetListTable.setTableModelFromFeatureLookupDataSetList(flListCollection);
	}

	public BinnerAnnotationLookupDataSetListTable getTable() {
		return binnerAnnotationLookupDataSetListTable;
	}
}
