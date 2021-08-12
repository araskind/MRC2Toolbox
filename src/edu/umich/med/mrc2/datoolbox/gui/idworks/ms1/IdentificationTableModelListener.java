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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms1;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.gui.idtable.uni.UniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.idtable.uni.UniversalIdentificationResultsTableModel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;

public class IdentificationTableModelListener  implements TableModelListener {

	private UniversalIdentificationResultsTable table;
	private IDWorkbenchPanel parentPanel;

	public IdentificationTableModelListener(UniversalIdentificationResultsTable table, IDWorkbenchPanel parentPanel) {
		super();
		this.table = table;
		this.parentPanel = parentPanel;
	}

	public void tableChanged(TableModelEvent e) {

		int row = table.convertRowIndexToView(e.getFirstRow());
		int col = table.convertColumnIndexToView(e.getColumn());
		MsFeatureInfoBundle selectedMS1FeatureBundle = parentPanel.getSelectedMSFeatureBundle();		
		MsFeatureInfoBundle selectedMsMsFeatureBundle = parentPanel.getSelectedMSMSFeatureBundle();
		if(selectedMsMsFeatureBundle == null && selectedMS1FeatureBundle == null)
			return;

		boolean dataChanged = false;
		if (col == table.getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN)) {

			MsFeatureIdentity selectedId = (MsFeatureIdentity) table.getValueAt(row,
					table.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN));

			if(selectedMsMsFeatureBundle != null) {
				
				if(!selectedId.equals(selectedMsMsFeatureBundle.getMsFeature().getPrimaryIdentity())) {
					selectedMsMsFeatureBundle.getMsFeature().setPrimaryIdentity(selectedId);
					dataChanged = true;
				}
			}
			if(selectedMS1FeatureBundle != null) {
				
				if(!selectedId.equals(selectedMS1FeatureBundle.getMsFeature().getPrimaryIdentity())) {
					selectedMS1FeatureBundle.getMsFeature().setPrimaryIdentity(selectedId);
					dataChanged = true;
				}
			}
		}
		
		if (col == table.getColumnIndex(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN))
			dataChanged = true;	
		
//		TODO handle other changes if necessary
		
		if(dataChanged) {
			
			if(selectedMsMsFeatureBundle != null)
				parentPanel.updateSelectedMSMSFeatures();

			if(selectedMS1FeatureBundle != null)
				parentPanel.updateSelectedMSFeatures();
		}
	}
}
