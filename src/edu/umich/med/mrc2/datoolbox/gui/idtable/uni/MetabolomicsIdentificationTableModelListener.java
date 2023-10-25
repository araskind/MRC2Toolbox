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

package edu.umich.med.mrc2.datoolbox.gui.idtable.uni;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;

public class MetabolomicsIdentificationTableModelListener  implements TableModelListener {

	private FeatureDataPanel parentPanel;

	public MetabolomicsIdentificationTableModelListener(FeatureDataPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
	}

	public void tableChanged(TableModelEvent e) {
		
		if(e.getType() != TableModelEvent.UPDATE)
			return;
		
		MsFeature feature = parentPanel.getSelectedFeature();
		if(feature == null)
			return;
		
		UniversalIdentificationResultsTableModel model = 
				(UniversalIdentificationResultsTableModel)e.getSource();
		int row = e.getFirstRow();
		int col = e.getColumn();
		boolean dataChanged = false;
		if (col == model.getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN)) {
			
			MsFeatureIdentity selectedId = (MsFeatureIdentity) model.getValueAt(row,
					model.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN));
			if(feature.getPrimaryIdentity() == null) {
				feature.setPrimaryIdentity(selectedId);
				dataChanged = true;
			}
			else {
				if(!feature.getPrimaryIdentity().equals(selectedId)) {
					
					feature.setPrimaryIdentity(selectedId);
					dataChanged = true;
				}
			}			
		}
		if(dataChanged) {
			parentPanel.updateFeatureData(feature);
			parentPanel.refreshIdentificationsTable();
		}
	}
}
