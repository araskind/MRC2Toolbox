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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.curator.ctab;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentityCluster;

public class CompoundIdentityClusterTableModelListener implements TableModelListener {

	private CompoundIdentityClusterTable compoundIdentityClusterTable;
	
	public CompoundIdentityClusterTableModelListener(CompoundIdentityClusterTable compoundIdentityClusterTable) {
		super();
		this.compoundIdentityClusterTable = compoundIdentityClusterTable;
	}

	@Override
	public void tableChanged(TableModelEvent e) {

		int row = compoundIdentityClusterTable.convertRowIndexToView(e.getFirstRow());
		int col = compoundIdentityClusterTable.convertColumnIndexToView(e.getColumn());
		if (col == compoundIdentityClusterTable.getColumnIndex(CompoundIdentityClusterTableModel.PRIMARY_COLUMN)
				&& (boolean)compoundIdentityClusterTable.getValueAt(row, col)) {
			
			CompoundIdentity primaryId = compoundIdentityClusterTable.getSelectedCompoundId();
			CompoundIdentityCluster cluster = compoundIdentityClusterTable.getActiveCluster();
			if(primaryId != null && cluster != null)
				cluster.setPrimaryIdentity(primaryId);
			
			compoundIdentityClusterTable.setModelFromCompoundIdentityCluster(
					compoundIdentityClusterTable.getActiveCluster());
		}
	}
}
