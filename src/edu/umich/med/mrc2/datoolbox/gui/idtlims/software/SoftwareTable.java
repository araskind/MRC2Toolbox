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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class SoftwareTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2069272556941448636L;
	private SoftwareTableModel model;

	public SoftwareTable() {
		super();
		model = new SoftwareTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SoftwareTableModel>(model);
		setRowSorter(rowSorter);		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(SoftwareTableModel.SOFTWARE_DESCRIPTION_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromSoftwareList(Collection<DataProcessingSoftware>softwareItems) {

		thf.setTable(null);
		model.setTableModelFromSoftwareList(softwareItems);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public DataProcessingSoftware getSelectedSoftware(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (DataProcessingSoftware) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(SoftwareTableModel.SOFTWARE_COLUMN));
	}

	public void selectSoftware(DataProcessingSoftware item) {

		int colIdx = model.getColumnIndex(SoftwareTableModel.SOFTWARE_COLUMN);
		if(colIdx == -1)
			return;
		
		for(int i=0; i<model.getRowCount(); i++) {

			if(model.getValueAt(i,colIdx).equals(item)){
				
				int row = convertRowIndexToView(i);			
				setRowSelectionInterval(row, row);
				this.scrollToSelected();
				return;
			}
		}
	}
}
