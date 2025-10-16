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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

import java.util.Collection;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.NISTPepSearchParameterObjectRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class PepSearchParameterSetTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3916578878036401634L;

	public PepSearchParameterSetTable() {
		super();
		model = new PepSearchParameterSetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<PepSearchParameterSetTableModel>(
				(PepSearchParameterSetTableModel)model);
		setRowSorter(rowSorter);
		
		columnModel.getColumnById(PepSearchParameterSetTableModel.PS_OBJECT_COLUMN).
			setCellRenderer(new NISTPepSearchParameterObjectRenderer());
		
		columnModel.getColumnById(PepSearchParameterSetTableModel.HR_SEARCH_OPTION_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(PepSearchParameterSetTableModel.HR_SEARCH_TYPE_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
				
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public NISTPepSearchParameterObject getSelectedNISTPepSearchParameterObject(){

		if(getSelectedRow() == -1)
			return null;

		return (NISTPepSearchParameterObject)model.getValueAt(
				convertRowIndexToModel(getSelectedRow()), 
				model.getColumnIndex(PepSearchParameterSetTableModel.PS_OBJECT_COLUMN));
	}

	public void setModelFromObjectCollection(Collection<NISTPepSearchParameterObject> pspObjects) {
		((PepSearchParameterSetTableModel)model).setModelFromObjectCollection(pspObjects);
		adjustColumns();
	}
	
	public void setModelFromHitCountMap(Map<NISTPepSearchParameterObject, Long>paramCounts) {
		((PepSearchParameterSetTableModel)model).setModelFromHitCountMap(paramCounts);
		adjustColumns();
	}
}









