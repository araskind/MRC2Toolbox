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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class MobilePhaseSynonymsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private MobilePhaseSynonymsTableModel model;
	private ValidationListener validationListener;
	
	public MobilePhaseSynonymsTable() {
		super();
		model = new MobilePhaseSynonymsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MobilePhaseSynonymsTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setSortable(0, false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(true);
		validationListener = new ValidationListener();
		model.addTableModelListener(validationListener);
		finalizeLayout();		
	}
	
	private class ValidationListener implements TableModelListener {
		@Override
		public void tableChanged(TableModelEvent tme) {
			validateSynonymList(tme);
		}
	}

	public void setTableModelFromSynonymList(Collection<String>synonyms) {

		model.removeTableModelListener(validationListener);
		model.setTableModelFromSynonymList(synonyms);
		tca.adjustColumns();
		model.addTableModelListener(validationListener);
	}

	public Set<String> getSynonymList() {

		Set<String>synonyms = new TreeSet<String>();
		int col = model.getColumnIndex(MobilePhaseSynonymsTableModel.SYNONYM_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			String synonym = (String)model.getValueAt(i, col);
			if(synonym != null && !synonym.trim().isEmpty())
				synonyms.add(synonym.trim());
		}		
		return synonyms;
	}
	
	public void addNewSynonym() {
		
		model.removeTableModelListener(validationListener);
		((MobilePhaseSynonymsTableModel)model).addNewSynonym();
		int col = getColumnIndex(MobilePhaseSynonymsTableModel.SYNONYM_COLUMN);
		int row = getRowCount() - 1;
		setRowSelectionInterval(row, row);
	    setColumnSelectionInterval(col, col);
	    if (editCellAt(row, col)) {
	        changeSelection(row, col, false, false);
	        getEditorComponent().requestFocus();
	    }	    
	    model.addTableModelListener(validationListener);
	}

	public void removeSelectedSynonym() {

		int row = getSelectedRow();
		if(row == -1)
			return;
		
		model.removeTableModelListener(validationListener);
		model.removeRow(convertRowIndexToModel(row));
		model.addTableModelListener(validationListener);
	}
	
	private void validateSynonymList(TableModelEvent tme) {
		
		Collection<String>synonyms = new TreeSet<String>();
		int col = model.getColumnIndex(MobilePhaseSynonymsTableModel.SYNONYM_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			String synonym = (String)model.getValueAt(i, col);
			if(synonyms.contains(synonym)) {
				
				model.removeTableModelListener(validationListener);
				MessageDialog.showErrorMsg("Duplicate synonym \"" + synonym + "\"", this);
				
				int row = tme.getFirstRow();				
				setValueAt(null, row, col);					
			    model.addTableModelListener(validationListener);
				return;
			}
			else
				synonyms.add(synonym);
		}
	}
}






