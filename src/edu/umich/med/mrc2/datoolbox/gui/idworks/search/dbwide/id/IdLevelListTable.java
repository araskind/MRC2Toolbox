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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.id;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.compare.IdStatusComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;

public class IdLevelListTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private IdLevelListTableModel model;

	public IdLevelListTable() {
		super();
		model =  new IdLevelListTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IdLevelListTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IdLevelListTableModel.LEVEL_COLUMN),
				new IdStatusComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(IdLevelListTableModel.LEVEL_COLOR_CODE_COLUMN),
				new IdStatusComparator(SortProperty.Name));
		
		columnModel.getColumnById(IdLevelListTableModel.LEVEL_COLOR_CODE_COLUMN)
				.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromLevelList(Collection<MSFeatureIdentificationLevel>levelsList) {
		model.setTableModelFromLevelList(levelsList);
		tca.adjustColumns();
	}
	
	public void selectLevelList(Collection<MSFeatureIdentificationLevel>levelsList) {

		int col = model.getColumnIndex(IdLevelListTableModel.LEVEL_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(levelsList.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}
	}

	public MSFeatureIdentificationLevel getSelectedLevel() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MSFeatureIdentificationLevel) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(IdLevelListTableModel.LEVEL_COLUMN));
	}
	
	public Collection<MSFeatureIdentificationLevel>getSelectedLevels() {

		Collection<MSFeatureIdentificationLevel>selected = 
				new ArrayList<MSFeatureIdentificationLevel>();
		int col = model.getColumnIndex(IdLevelListTableModel.LEVEL_COLUMN);
		for(int i : getSelectedRows())
			selected.add((MSFeatureIdentificationLevel) model.getValueAt(convertRowIndexToModel(i), col));

		return selected;
	}
}
















