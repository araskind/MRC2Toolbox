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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.compare.IdStatusComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.IdLevelUtils;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;

public class IdLevelTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;
	private IdLevelTableModel model;

	public IdLevelTable() {
		super();
		model =  new IdLevelTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<IdLevelTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(IdLevelTableModel.LEVEL_COLUMN),
				new IdStatusComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(IdLevelTableModel.LEVEL_COLOR_CODE_COLUMN),
				new IdStatusComparator(SortProperty.Name));	
		
		IdLevelTableModelListener modelListener = new IdLevelTableModelListener();
		model.addTableModelListener(modelListener);
		
		columnModel.getColumnById(IdLevelTableModel.LEVEL_COLOR_CODE_COLUMN)
				.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		finalizeLayout();
	}

	public void setTableModelFromLevelList(Collection<MSFeatureIdentificationLevel>statusList) {
		model.setTableModelFromLevelList(statusList);
		tca.adjustColumns();
	}

	public MSFeatureIdentificationLevel getSelectedLevel() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MSFeatureIdentificationLevel) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(IdLevelTableModel.LEVEL_COLUMN));
	}
	
	private class IdLevelTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = e.getFirstRow();
			int col = e.getColumn();
			if (col == model.getColumnIndex(IdLevelTableModel.ALLOW_UPDATE_DEFAULT_ID_COLUMN)) {

				MSFeatureIdentificationLevel selectedIdLevel = 
						(MSFeatureIdentificationLevel) model.getValueAt(row,
								model.getColumnIndex(IdLevelTableModel.LEVEL_COLUMN));
				
				boolean allowToReplaceAsDefault = (Boolean)model.getValueAt(row, col);
				selectedIdLevel.setAllowToReplaceAsDefault(allowToReplaceAsDefault);
				try {
					IdLevelUtils.editMSFeatureIdentificationLevel(selectedIdLevel);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
















