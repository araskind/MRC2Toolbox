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

package edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod;

import java.util.Collection;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.database.idt.ChemicalModificationDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalModificationRenderer;

public class ChemModificationTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private ChemModificationTableModel model;
	private ChmodTableModelListener modelListener;

	public ChemModificationTable() {

		super();
		model = new ChemModificationTableModel();
		setModel(model);
		modelListener = new ChmodTableModelListener();
		model.addTableModelListener(modelListener);
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		rowSorter = new TableRowSorter<ChemModificationTableModel>(model);
		setRowSorter(rowSorter);

		chmodRenderer = new ChemicalModificationRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);

		columnModel.getColumnById(ChemModificationTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer); // Neutral mass
		columnModel.getColumnById(ChemModificationTableModel.MASS_CORRECTION_ABS_COLUMN)
				.setCellRenderer(mzRenderer); // Neutral mass

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public Adduct getSelectedModification() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (Adduct)getValueAt(row, getColumnIndex(ChemModificationTableModel.CHEM_MOD_COLUMN));		
	}

	private class ChmodTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = convertRowIndexToView(e.getFirstRow());
			int col = convertColumnIndexToView(e.getColumn());

			if (col == getColumnIndex(ChemModificationTableModel.ENABLED_COLUMN)) {

				Adduct selectedModification =
						(Adduct) getValueAt(row, getColumnIndex(ChemModificationTableModel.CHEM_MOD_COLUMN));

				selectedModification.setEnabled((boolean) getValueAt(row, col));
				try {
					ChemicalModificationDatabaseUtils.updateChemicalModification(selectedModification.getName(),
							selectedModification);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	@Override
	public void clearTable() {

		model.removeTableModelListener(modelListener);
		super.clearTable();
		model.addTableModelListener(modelListener);
	}

	public void setTableModelFromAdductList(Collection<Adduct> collection) {

		model.removeTableModelListener(modelListener);
		model.setTableModelFromAdductList(collection);
		tca.adjustColumns();
		model.addTableModelListener(modelListener);
	}
}
