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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class IDTrackerExperimentListingTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -363757863489122914L;

	public IDTrackerExperimentListingTable() {
		super();
		model = new IDTrackerExperimentListingTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IDTrackerExperimentListingTableModel>(
				(IDTrackerExperimentListingTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		
		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		columnModel.getColumnById(IDTrackerExperimentListingTableModel.EXPERIMENT_NAME_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(
				IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN).setMaxWidth(100);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));
		finalizeLayout();
	}
	
	public void setTableModelFromExperimentList(Collection<LIMSExperiment>experimentList) {
		
		thf.setTable(null);
		((IDTrackerExperimentListingTableModel)model).setTableModelFromExperimentList(experimentList);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<LIMSExperiment>getSelectedExperiments(){
		
		 Collection<LIMSExperiment>selectedExperiments = new  ArrayList<LIMSExperiment>();
		 int expColumn = model.getColumnIndex(IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN);
		 for(int i : getSelectedRows())
			 selectedExperiments.add((LIMSExperiment)model.getValueAt(convertRowIndexToModel(i),expColumn));
		 	 
		 return selectedExperiments;
	}
	
	public LIMSExperiment getSelectedExperiment(){

		int selectedRow = getSelectedRow();
		if(selectedRow == -1)
			return null;
		 
		return  (LIMSExperiment)model.getValueAt(convertRowIndexToModel(selectedRow),
				 model.getColumnIndex(IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN));
	}
	
	public Collection<LIMSExperiment>getAllExperiments(){
		
		 Collection<LIMSExperiment>selectedExperiments = new  ArrayList<LIMSExperiment>();
		 int expColumn = model.getColumnIndex(IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN);
		 for(int i=0; i<model.getRowCount(); i++)
			 selectedExperiments.add((LIMSExperiment)model.getValueAt(i,expColumn));
		 	 
		 return selectedExperiments;
	}
	
	public void setSelectedExperiments(Collection<LIMSExperiment>experimentList) {
		
		int expColumn = model.getColumnIndex(IDTrackerExperimentListingTableModel.EXPERIMENT_ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(experimentList.contains((LIMSExperiment)model.getValueAt(convertRowIndexToModel(i),expColumn)))
				addRowSelectionInterval(i, i);
		}		
	}
}
