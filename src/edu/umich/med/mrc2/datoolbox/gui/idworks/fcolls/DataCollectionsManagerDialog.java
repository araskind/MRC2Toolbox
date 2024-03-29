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
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.binner.DockableBinnerAnnotationDataSetManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters.DockableMSMSClusterDataSetsManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.DockableFeatureCollectionsManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup.DockableFeatureLookupListManager;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.BinnerAnnotationDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;

public class DataCollectionsManagerDialog extends JDialog 
		implements ActionListener, PersistentLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6227984367485372521L;
	private static final Icon stIcon = GuiUtils.getIcon("editSop", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "FeatureAndClusterCollectionManagerDialog.layout");
	
	private CControl control;
	private CGrid grid;
	
	private CollectionManagerToolbar toolbar;
	private DockableFeatureCollectionsManager featureCollectionsManager;
	private DockableMSMSClusterDataSetsManager featureClusterCollectionsManager;
	private DockableFeatureLookupListManager featureLookupDataSetManager;
	private DockableBinnerAnnotationDataSetManager binnerAnnotationDataSetManager;
	private IndeterminateProgressDialog idp;
	
	public DataCollectionsManagerDialog() {
		super();
		setTitle("Data collections managers");
		setIconImage(((ImageIcon) stIcon).getImage());
		setPreferredSize(new Dimension(1000, 500));
		setSize(new Dimension(1000, 500));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));	
		
		toolbar = new CollectionManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);	
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		add(control.getContentArea(), BorderLayout.CENTER);
		grid = new CGrid(control);
			
		featureCollectionsManager = 
				new DockableFeatureCollectionsManager(this);
		featureClusterCollectionsManager = 
				new DockableMSMSClusterDataSetsManager(this);
		featureLookupDataSetManager = 
				new DockableFeatureLookupListManager(this);
		binnerAnnotationDataSetManager = 
				new DockableBinnerAnnotationDataSetManager(this);

		grid.add(0, 0, 1, 1, 
				featureCollectionsManager, 
				featureClusterCollectionsManager,				
				featureLookupDataSetManager,
				binnerAnnotationDataSetManager);
		control.getContentArea().deploy(grid);
				
		loadLayout(layoutConfigFile);
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			featureCollectionsManager.loadDatabaseStoredCollections();
			featureLookupDataSetManager.loadDatabaseStoredFeatureLookupDataSets();
			featureClusterCollectionsManager.loadDatabaseStoredMSMSClusterDataSets();
			binnerAnnotationDataSetManager.loadDatabaseStoredBinnerAnnotationLookupDataSets();
		}
		else {
			featureCollectionsManager.loadCollectionsForActiveProject();
			featureLookupDataSetManager.loadFeatureLookupDataSetsForActiveProject();
			featureClusterCollectionsManager.loadMSMSClusterDataSetsForActiveProject();	
			binnerAnnotationDataSetManager.loadBinnerAnnotationLookupDataSetsForActiveProject();
		}
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.REFRESH_FEATURE_AND_CLUSTER_COLLECTIONS_COMMAND.getName()))
			refreshFeatureCollections();
	}
	
	private void refreshFeatureCollections() { 

		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			featureCollectionsManager.loadCollectionsForActiveProject();
			featureLookupDataSetManager.loadFeatureLookupDataSetsForActiveProject();
			featureClusterCollectionsManager.loadMSMSClusterDataSetsForActiveProject();	
			binnerAnnotationDataSetManager.loadBinnerAnnotationLookupDataSetsForActiveProject();
		}
		RefreshCollectionsTask task = 
			new RefreshCollectionsTask();
		idp = new IndeterminateProgressDialog(
				"Refreshing data for feature and cluster collections  ...", 
				this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class RefreshCollectionsTask extends LongUpdateTask {

		public RefreshCollectionsTask() {

		}

		@Override
		public Void doInBackground() {
			
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();
			BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();
			return null;
		}
		
	    @Override
	    public void done() {
	    	
			featureCollectionsManager.loadDatabaseStoredCollections();
			featureLookupDataSetManager.loadDatabaseStoredFeatureLookupDataSets();
			featureClusterCollectionsManager.loadDatabaseStoredMSMSClusterDataSets();
			binnerAnnotationDataSetManager.loadDatabaseStoredBinnerAnnotationLookupDataSets();
	    	super.done();
	    }
	}
	
	@Override
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		super.dispose();
	}
	
	public void showMsFeatureCollectionEditorDialog (MsFeatureInfoBundleCollection collection) {		
		featureCollectionsManager.showMsFeatureCollectionEditorDialog(collection);
	}

	public void setFeaturesToAdd(Collection<MSFeatureInfoBundle> featuresToAdd) {
		//	this.featuresToAdd = featuresToAdd;
		featureCollectionsManager.setFeaturesToAdd(featuresToAdd);
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











