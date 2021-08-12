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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;

public class MSMSLibMatchTableModelListener  implements TableModelListener {

	private MSMSLibraryMatchTable table;
	private IDWorkbenchPanel parentPanel;

	public MSMSLibMatchTableModelListener(MSMSLibraryMatchTable table, IDWorkbenchPanel parentPanel) {
		super();
		this.table = table;
		this.parentPanel = parentPanel;
	}

	public void tableChanged(TableModelEvent e) {

		int row = table.convertRowIndexToView(e.getFirstRow());
		int col = table.convertColumnIndexToView(e.getColumn());
		MsFeatureInfoBundle selectedMsMsFeatureBundle = parentPanel.getSelectedMSMSFeatureBundle();
		if(selectedMsMsFeatureBundle == null)
			return;

		boolean dataChanged = false;
		if (col == table.getColumnIndex(MSMSLibraryMatchTableModel.DEFAULT_ID_COLUMN)) {

			MsFeatureIdentity selectedId = (MsFeatureIdentity) table.getValueAt(row,
					table.getColumnIndex(MSMSLibraryMatchTableModel.MS_FEATURE_ID_COLUMN));

			if(!selectedId.equals(selectedMsMsFeatureBundle.getMsFeature().getPrimaryIdentity())) {
				selectedMsMsFeatureBundle.getMsFeature().setPrimaryIdentity(selectedId);
				dataChanged = true;
			}
		}
		//	TODO handle other changes if necessary
		if(dataChanged)
			parentPanel.updateSelectedMSMSFeatures();
		
		// table.setRowSelectionInterval(row, row);		
	}
}
