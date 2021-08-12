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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.tcode;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MotrpacTissueCodeRenderer;

public class TissueCodeTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1472881417476433802L;
	private TissueCodeTableModel model;

	public TissueCodeTable() {
		super();
		model = new TissueCodeTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<TissueCodeTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(TissueCodeTableModel.TISSUE_CODE_COLUMN)
			.setCellRenderer(new MotrpacTissueCodeRenderer(SortProperty.ID));
		
		finalizeLayout();
	}

	public void setTableModelFromTissueCodes(Collection<MoTrPACTissueCode>codes) {

		model.setTableModelFromTissueCodes(codes);
		tca.adjustColumns();
	}

	public MoTrPACTissueCode getSelectedCode(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MoTrPACTissueCode) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(TissueCodeTableModel.TISSUE_CODE_COLUMN));
	}
	
	public Collection<MoTrPACTissueCode> getSelectedCodes(){

		Collection<MoTrPACTissueCode>selectedCodes = new ArrayList<MoTrPACTissueCode>();
		int colIdx = model.getColumnIndex(TissueCodeTableModel.TISSUE_CODE_COLUMN);
		for(int i : getSelectedRows())
			selectedCodes.add((MoTrPACTissueCode)model.getValueAt(convertRowIndexToModel(i), colIdx));

		return selectedCodes;
	}
	
	public Collection<MoTrPACTissueCode> getAllCodes(){

		Collection<MoTrPACTissueCode>selectedCodes = new ArrayList<MoTrPACTissueCode>();
		int colIdx = model.getColumnIndex(TissueCodeTableModel.TISSUE_CODE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selectedCodes.add((MoTrPACTissueCode)model.getValueAt(i, colIdx));

		return selectedCodes;
	}

	public void selectCode(MoTrPACTissueCode code) {

		int colIdx = model.getColumnIndex(TissueCodeTableModel.TISSUE_CODE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {

			if(model.getValueAt(convertRowIndexToModel(i), colIdx).equals(code)){
				setRowSelectionInterval(i, i);
				this.scrollToSelected();
				return;
			}
		}
	}
}








