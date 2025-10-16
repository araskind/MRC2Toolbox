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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.acq;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.compare.MsTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsTypeRenderer;

public class MsTypeTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public MsTypeTable() {
		super();
		model =  new MsTypeTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsTypeTableModel>((MsTypeTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MsTypeTableModel.ID_COLUMN),
				new MsTypeComparator(SortProperty.ID));
		
		columnModel.getColumnById(MsTypeTableModel.ID_COLUMN)
				.setCellRenderer(new MsTypeRenderer(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromMsTypeList(Collection<MsType>typeList) {
		((MsTypeTableModel)model).setTableModelFromMsTypeList(typeList);
		adjustColumns();
	}
	
	public void selectTypeList(Collection<MsType>typeList) {

		int col = model.getColumnIndex(MsTypeTableModel.ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(typeList.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}
	}

	public MsType getSelectedType() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsType) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MsTypeTableModel.ID_COLUMN));
	}
	
	public Collection<MsType>getSelectedTypes() {

		Collection<MsType>selected = new ArrayList<MsType>();
		int col = model.getColumnIndex(MsTypeTableModel.ID_COLUMN);
		for(int i : getSelectedRows())
			selected.add((MsType) model.getValueAt(convertRowIndexToModel(i), col));

		return selected;
	}
}
















