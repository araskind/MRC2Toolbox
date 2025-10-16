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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.compare.IdStatusComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;

public class MinimalIdLevelTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public MinimalIdLevelTable() {
		super();
		model =  new MinimalIdLevelTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MinimalIdLevelTableModel>((MinimalIdLevelTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MinimalIdLevelTableModel.LEVEL_COLUMN),
				new IdStatusComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MinimalIdLevelTableModel.LEVEL_COLOR_CODE_COLUMN),
				new IdStatusComparator(SortProperty.Name));	
		
		columnModel.getColumnById(MinimalIdLevelTableModel.LEVEL_COLOR_CODE_COLUMN)
				.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
				
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setExactColumnWidth(MinimalIdLevelTableModel.LEVEL_COLOR_CODE_COLUMN, 50);
		finalizeLayout();
	}

	public void setTableModelFromLevelList(Collection<MSFeatureIdentificationLevel>statusList) {
		((MinimalIdLevelTableModel)model).setTableModelFromLevelList(statusList);
		adjustColumns();
	}

	public Collection<MSFeatureIdentificationLevel> getSelectedLevels() {
		
		Collection<MSFeatureIdentificationLevel>selected = 
				new ArrayList<MSFeatureIdentificationLevel>();
		int[] rows = getSelectedRows();
		if(rows.length == 0)
			return selected;
		
		int col = model.getColumnIndex(MinimalIdLevelTableModel.LEVEL_COLUMN);
		for(int row : rows) {
			
			MSFeatureIdentificationLevel lvl = 
					(MSFeatureIdentificationLevel) model.getValueAt(convertRowIndexToModel(row),col);
			selected.add(lvl);
		}
		return selected;
	}
	
	public void selectLevels(
			Collection<MSFeatureIdentificationLevel>levels) {
		
		int col = model.getColumnIndex(MinimalIdLevelTableModel.LEVEL_COLUMN);		
		for(int i=0; i<getRowCount(); i++) {
			
			if(levels.contains(model.getValueAt(convertRowIndexToModel(i),col)))
				addRowSelectionInterval(i, i);
		}
	}
}
















