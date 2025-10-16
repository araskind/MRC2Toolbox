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

import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicSeparationTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChromatographicSeparationTypeRenderer;

public class ChromatographicSeparationTypeTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public ChromatographicSeparationTypeTable() {
		super();
		model =  new ChromatographicSeparationTypeTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ChromatographicSeparationTypeTableModel>(
				(ChromatographicSeparationTypeTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ChromatographicSeparationTypeTableModel.ID_COLUMN),
				new ChromatographicSeparationTypeComparator(SortProperty.ID));
		columnModel.getColumnById(ChromatographicSeparationTypeTableModel.ID_COLUMN)
				.setCellRenderer(new ChromatographicSeparationTypeRenderer(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromChromatographicSeparationTypeList(
			Collection<ChromatographicSeparationType>typeList) {
		((ChromatographicSeparationTypeTableModel)model).
				setTableModelFromChromatographicSeparationTypeList(typeList);
		adjustColumns();
	}
	
	public void selectTypeList(Collection<ChromatographicSeparationType>typeList) {

		int col = model.getColumnIndex(ChromatographicSeparationTypeTableModel.ID_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(typeList.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}
	}

	public ChromatographicSeparationType getSelectedType() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (ChromatographicSeparationType) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(ChromatographicSeparationTypeTableModel.ID_COLUMN));
	}
	
	public Collection<ChromatographicSeparationType>getSelectedTypes() {

		Collection<ChromatographicSeparationType>selected = 
				new ArrayList<ChromatographicSeparationType>();
		int col = model.getColumnIndex(ChromatographicSeparationTypeTableModel.ID_COLUMN);
		for(int i : getSelectedRows())
			selected.add((ChromatographicSeparationType) model.getValueAt(convertRowIndexToModel(i), col));

		return selected;
	}
}
















