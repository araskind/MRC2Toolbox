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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DatabaseCompoundTable;

public class SynonymTableModelListener implements TableModelListener {

	private CompoundSynonymTable synonymTable;
	private DatabaseCompoundTable compoundTable;

	public SynonymTableModelListener(
			CompoundSynonymTable synonymTable, DatabaseCompoundTable compoundTable) {
		super();
		this.synonymTable = synonymTable;
		this.compoundTable = compoundTable;
		synonymTable.setSynonymsModelListener(this);
	}

	@Override
	public void tableChanged(TableModelEvent e) {

		int row = synonymTable.convertRowIndexToView(e.getFirstRow());
		int col = synonymTable.convertColumnIndexToView(e.getColumn());
		if (col == synonymTable.getColumnIndex(CompoundSynonymTableModel.DEFAULT_COLUMN)
				&& (boolean)synonymTable.getValueAt(row, col)) {
			
			CompoundNameSet nameSet = synonymTable.getCurrentNameSet();
			String selectedName = (String) synonymTable.getValueAt(row,
					synonymTable.getColumnIndex(CompoundSynonymTableModel.SYNONYM_COLUMN));
			
			if(nameSet.getPrimaryName().equals(selectedName))
				return;
			
			nameSet.setPrimaryName(selectedName);
			try {
				CompoundDatabaseUtils.updateSynonyms(synonymTable.getCurrentNameSet());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			synonymTable.setModelFromCompoundNameSet(nameSet);
			compoundTable.getSelectedIdentity().getCompoundIdentity().setCommonName(selectedName);
			compoundTable.updateCidData(compoundTable.getSelectedIdentity());
		}
	}
}
