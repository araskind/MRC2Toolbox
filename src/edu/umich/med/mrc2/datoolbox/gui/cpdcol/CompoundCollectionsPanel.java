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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.cpd.DockableCompoundCollectionListingTable;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex.DockableCompoundMultiplexListingTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableCompoundPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;

public class CompoundCollectionsPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("compoundCollection", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "CompoundCollectionsPanel.layout");
	
	private DockableCompoundCollectionListingTable compoundTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableCompoundPropertiesTable propertiesTable;
	private DockableCompoundMultiplexListingTable compoundMultiplexListingTable;
		
	public CompoundCollectionsPanel() {
		
		super("CompoundCollectionsPanel", PanelList.COMPOUND_COLLECTIONS.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new CompoundCollectionsPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);
		
		compoundTable = new DockableCompoundCollectionListingTable();
		compoundTable.getTable().addCompoundPopupListener(this);
		compoundTable.getTable().getSelectionModel().addListSelectionListener(this);

		molStructurePanel = new DockableMolStructurePanel(
				"CpdCollectionsPanelDockableMolStructurePanel");
		propertiesTable = new DockableCompoundPropertiesTable(
				"CpdCollectionCompoundPropertiesTable");
		compoundMultiplexListingTable =  
				new DockableCompoundMultiplexListingTable();
		compoundMultiplexListingTable.getTable().getSelectionModel().addListSelectionListener(this);

		grid.add(0, 0, 75, 40, compoundTable, compoundMultiplexListingTable);
		grid.add(75, 0, 25, 40, molStructurePanel);
		grid.add(0, 50, 100, 60, propertiesTable);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
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

		if(e.getValueIsAdjusting() || e.getSource() == null) 
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(compoundTable.getTable())) {
				
				clearDataPanels();
				if (compoundTable.getSelectedCompound() != null)
					loadCompoundData(compoundTable.getSelectedCompound());
				
				return;
			}
//			if(listener.equals(spectraTable.getTable())) {
//				
//				clearSpectrumDataPanels();
//				MsMsLibraryFeature feature = spectraTable.getSelectedFeature();
//				if(feature != null) {
//					msTwoPlot.showTandemMs(feature);
//					msTwoTable.setTableModelFromDataPoints(feature.getSpectrum(), feature.getParent());
//					msmsLibraryEntryPropertiesTable.showMsMsLibraryFeatureProperties(feature);
//				}
//				return;
//			}
		}	
	}
	
	public void loadCompoundData(CompoundIdentity cpd) {
		
	}
	
	private void clearDataPanels() {

//		narrativeDataPanel.clearPanel();
//		synonymsTable.clearTable();
//		dbLinksTable.clearTable();
//		propertiesTable.clearTable();
//		concentrationsTable.clearTable();
//		spectraTable.clearTable();
//		molStructurePanel.clearPanel();
//		clearSpectrumDataPanels();
//		clasyFireViewer.clearPanel();
	}

	@Override
	public void clearPanel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		// TODO Auto-generated method stub
		return null;
	}

}
