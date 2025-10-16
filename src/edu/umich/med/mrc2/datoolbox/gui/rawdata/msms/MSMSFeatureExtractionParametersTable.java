/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class MSMSFeatureExtractionParametersTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2087167895707677516L;

	public MSMSFeatureExtractionParametersTable() {

		super();

		model = new MSMSFeatureExtractionParametersTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		rowSorter = new TableRowSorter<MSMSFeatureExtractionParametersTableModel>(
				(MSMSFeatureExtractionParametersTableModel)model);
		setRowSorter(rowSorter);

		columnModel.getColumnById(MSMSFeatureExtractionParametersTableModel.METHOD_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());		
		setExactColumnWidth(MSMSFeatureExtractionParametersTableModel.METHOD_ID_COLUMN, 80);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);		
		finalizeLayout();
	}

	public void setModelFromParametersList(Collection<MSMSExtractionParameterSet>parameterList) {

		thf.setTable(null);
		((MSMSFeatureExtractionParametersTableModel)model).setModelFromParametersList(parameterList);
		thf.setTable(this);
		adjustColumns();
	}
	
	public MSMSExtractionParameterSet getSelectedMSMSExtractionParameterSet(){

		if(getSelectedRow() == -1)
			return null;
				
		return (MSMSExtractionParameterSet)model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(MSMSFeatureExtractionParametersTableModel.METHOD_COLUMN));
	}
}

























