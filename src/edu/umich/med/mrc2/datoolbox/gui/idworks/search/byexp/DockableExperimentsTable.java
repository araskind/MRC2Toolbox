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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se.IDTrackerExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableExperimentsTable extends DefaultSingleCDockable implements ListSelectionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("idExperiment", 16);
	
	private IDTrackerExperimentListingTable experimentsTable;
	private DataPipelinesTable dataPipelinesTable;

	public DockableExperimentsTable()  {

		super("DockableExperimentsTable", componentIcon, "IDTracker Experiments", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		experimentsTable = new IDTrackerExperimentListingTable();
		experimentsTable.getSelectionModel().addListSelectionListener(this);
		add(new JScrollPane(experimentsTable), BorderLayout.CENTER);
		
		dataPipelinesTable = new DataPipelinesTable();
		add(new JScrollPane(dataPipelinesTable), BorderLayout.SOUTH);
	}
	
	public void setTableModelFromExperimentList(Collection<LIMSExperiment>experimentList) {		
		experimentsTable.setTableModelFromExperimentList(experimentList);
	}
	
	public Collection<LIMSExperiment>getSelectedExperiments(){
		 return experimentsTable.getSelectedExperiments();
	}
	
	public Collection<LIMSExperiment>getAllExperiments(){
		 return experimentsTable.getAllExperiments();
	}
	
	public void setSelectedExperiments(Collection<LIMSExperiment>experimentList) {
		experimentsTable.setSelectedExperiments(experimentList);
	}
	
	public IDTrackerExperimentListingTable getTable() {
		return experimentsTable;
	}
	
	public void allowMultipSeSelection(boolean allow) {
		
		if(allow) {
			experimentsTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);			
		}
		else {
			experimentsTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);	
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
