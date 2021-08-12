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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.MoTrPACAssayComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacAssayRenderer;

public class MotrpacMinimalAssayTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -381502820936496986L;
	private MotrpacMinimalAssayTableModel model;

	public MotrpacMinimalAssayTable() {
		super();
		model = new MotrpacMinimalAssayTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MotrpacMinimalAssayTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MotrpacMinimalAssayTableModel.ASSAY_COLUMN),
				new MoTrPACAssayComparator(SortProperty.Description));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(MotrpacMinimalAssayTableModel.ASSAY_COLUMN)
			.setCellRenderer(new MotrpacAssayRenderer(SortProperty.Description));

		finalizeLayout();
	}

	public void setTableModelFromAssays(Collection<MoTrPACAssay> assays)  {
		model.setTableModelFromAssays(assays);
		tca.adjustColumns();
	}

	public MoTrPACAssay getSelectedAssay(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MoTrPACAssay) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MotrpacMinimalAssayTableModel.ASSAY_COLUMN));
	}
	
	public Collection<MoTrPACAssay> getSelectedAssays(){

		Collection<MoTrPACAssay>selectedAssays = new ArrayList<MoTrPACAssay>();
		int colIdx = model.getColumnIndex(MotrpacMinimalAssayTableModel.ASSAY_COLUMN);
		for(int i : getSelectedRows())
			selectedAssays.add((MoTrPACAssay) model.getValueAt(convertRowIndexToModel(i), colIdx));			
		
		return selectedAssays;
	}

	public Collection<MoTrPACAssay> getAllAssays(){

		Collection<MoTrPACAssay>selectedAssays = new ArrayList<MoTrPACAssay>();
		int colIdx = model.getColumnIndex(MotrpacMinimalAssayTableModel.ASSAY_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selectedAssays.add((MoTrPACAssay) model.getValueAt(i, colIdx));

		return selectedAssays;
	}
	
	public void selectAssay(MoTrPACAssay assay) {

		int colIdx = model.getColumnIndex(MotrpacMinimalAssayTableModel.ASSAY_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), colIdx).equals(assay)){
				setRowSelectionInterval(i, i);
				this.scrollToSelected();
				return;
			}
		}
	}
}
