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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;

public class FeatureCollectionManagerDialog extends JDialog 
		implements ActionListener, ListSelectionListener, PersistentLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6227984367485372521L;
	private static final Icon stIcon = GuiUtils.getIcon("editSop", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "FeatureCollectionManagerDialog.layout");
	
	protected CControl control;
	protected CGrid grid;
	
	private FeatureCollectionsManagerToolbar toolbar;
	private DockableFeatureCollectionsTable featureCollectionsTable;
	private DockableCollectionFeatureTable featuresTable;
	private MsFeatureCollectionEditorDialog msFeatureCollectionEditorDialog;
	private Collection<MsFeatureInfoBundle> featuresToAdd;
	
	public FeatureCollectionManagerDialog() {
		super();
		setTitle("Feature collection manager");
		setIconImage(((ImageIcon) stIcon).getImage());
		setPreferredSize(new Dimension(1000, 500));
		setSize(new Dimension(1000, 500));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));	
		
		toolbar = new FeatureCollectionsManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		add(control.getContentArea(), BorderLayout.CENTER);
		grid = new CGrid(control);
			
		featureCollectionsTable = new DockableFeatureCollectionsTable(this);
		if(MRC2ToolBoxCore.getActiveRawDataAnalysisProject() == null)
			featureCollectionsTable.loadDatabaseStoredCollections();
		else
			featureCollectionsTable.loadCollectionsForActiveProject();

		featureCollectionsTable.getTable().addMouseListener(

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
		//featuresTable = new DockableCollectionFeatureTable();

		grid.add(0, 0, 1, 1, featureCollectionsTable
				//, featuresTable
				);
		control.getContentArea().deploy(grid);
				
		loadLayout(layoutConfigFile);
		pack();
	}
	
	@Override
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		super.dispose();
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
		
		msFeatureCollectionEditorDialog = new MsFeatureCollectionEditorDialog(collection, featuresToAdd, this);
		msFeatureCollectionEditorDialog.setLocationRelativeTo(this);
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
		try {
			FeatureCollectionUtils.updateMsFeatureInformationBundleCollectionMetadata(edited);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		Set<String>featureIds = new TreeSet<String>();
		featureIds.addAll(FeatureCollectionManager.getFeaturecollectionsmsmsidmap().get(edited));
		Set<String> featureIdsToAdd = msFeatureCollectionEditorDialog.getFeatureIdsToAdd();
		if(featureIdsToAdd != null && !featureIdsToAdd.isEmpty()) {
			featureIds.addAll(featureIdsToAdd);				
			try {
				FeatureCollectionUtils.addFeaturesToCollection(edited.getId(), featureIdsToAdd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean loadCollection = msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
		msFeatureCollectionEditorDialog.dispose();	
		if(loadCollection) {
			loadCollectionIntoWorkBench(edited);
			dispose();
		}
		else {
			FeatureCollectionManager.getFeaturecollectionsmsmsidmap().put(edited, featureIds);					
			featureCollectionsTable.getTable().updateCollectionData(edited);
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
		boolean loadCollection = 
				msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
		msFeatureCollectionEditorDialog.dispose();	
		if(loadCollection) {
			loadCollectionIntoWorkBench(newCollection);
			dispose();
		}
		else {
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
		boolean loadCollection = false;
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
			FeatureCollectionManager.getFeaturecollectionsmsmsidmap().put(newCollection, featureIds);	
			loadCollection = msFeatureCollectionEditorDialog.loadCollectionIntoWorkBench();
			msFeatureCollectionEditorDialog.dispose();	
			if(loadCollection) {
				loadCollectionIntoWorkBench(newCollection);
				dispose();
			}
			else {
				featureCollectionsTable.setTableModelFromFeatureCollectionList(
						FeatureCollectionManager.getMsFeatureInformationBundleCollectionList());	
				featuresToAdd = null;
			}
		}	
		featuresToAdd = null;
	}
	
	private void etitSelectedFeatureCollection() {

		MsFeatureInfoBundleCollection selected = featureCollectionsTable.getSelectedCollection();
		if(selected == null)
			return;
		
		if(!FeatureCollectionManager.getEditableMsFeatureInformationBundleCollectionList().contains(selected)) {
			MessageDialog.showWarningMsg("Collection \"" + selected.getName() + 
					"\" is locked and can not be edited.", this);
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
		
		MsFeatureInfoBundleCollection selected = featureCollectionsTable.getSelectedCollection();
		if(selected.equals(FeatureCollectionManager.msmsSearchResults) 
				|| selected.equals(FeatureCollectionManager.msOneSearchResults)) {
			MessageDialog.showErrorMsg("Collection \"" + selected.getName() + 
					"\" represents database search results and can not be deleted.", this);
			return;
		}
		if(selected != null) {
			
			if(MRC2ToolBoxCore.getIdTrackerUser().isSuperUser() || 
					(selected.getOwner() != null && selected.getOwner().equals(MRC2ToolBoxCore.getIdTrackerUser()))) {
				int res = MessageDialog.showChoiceWithWarningMsg(
						"Are you sure you want to delete feature collection \"" + selected.getName() + "\"?", this);
				if(res == JOptionPane.YES_OPTION) {
					try {
						FeatureCollectionUtils.deleteMsFeatureInformationBundleCollection(selected);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					FeatureCollectionManager.getMsFeatureInformationBundleCollectionList().remove(selected);
					featureCollectionsTable.setTableModelFromFeatureCollectionList(
							FeatureCollectionManager.getMsFeatureInformationBundleCollectionList());
					
					IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);
					if(panel.getActiveFeatureCollection() != null 
							&& panel.getActiveFeatureCollection().equals(selected))
						panel.clearMSMSFeatureData();
				}
			}
			else {
				MessageDialog.showErrorMsg("You are not authorized to delete this feature collection", this);
				return;
			}
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
		
		IDWorkbenchPanel panel = (IDWorkbenchPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.ID_WORKBENCH);		
		panel.loadMSMSFeatureInformationBundleCollection(selectedCollection);	
		dispose();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
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

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}











