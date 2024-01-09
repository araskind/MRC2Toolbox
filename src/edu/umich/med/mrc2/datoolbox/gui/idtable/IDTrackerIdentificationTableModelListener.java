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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.util.Collections;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;

public class IDTrackerIdentificationTableModelListener  implements TableModelListener {

	private IDWorkbenchPanel parentPanel;

	public IDTrackerIdentificationTableModelListener(IDWorkbenchPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
	}

	public void tableChanged(TableModelEvent e) {
		
		if(e.getType() != TableModelEvent.UPDATE)
			return;
		
		UniversalIdentificationResultsTableModel model = 
				(UniversalIdentificationResultsTableModel)e.getSource();
		int row = e.getFirstRow();
		int col = e.getColumn();
		
		MSFeatureInfoBundle selectedMS1FeatureBundle = 
				parentPanel.getSelectedMSFeatureBundle();		
		MSFeatureInfoBundle selectedMsMsFeatureBundle = 
				parentPanel.getSelectedMSMSFeatureBundle();
		if(selectedMsMsFeatureBundle == null && selectedMS1FeatureBundle == null)
			return;

		MsFeatureIdentity selectedId = (MsFeatureIdentity) model.getValueAt(row,
				model.getColumnIndex(UniversalIdentificationResultsTableModel.IDENTIFICATION_COLUMN));
		
//		boolean dataChanged = false;
		if (col == model.getColumnIndex(UniversalIdentificationResultsTableModel.DEFAULT_ID_COLUMN)) {

			if(selectedMsMsFeatureBundle != null 
					&& !selectedId.equals(selectedMsMsFeatureBundle.getMsFeature().getPrimaryIdentity())) {
				
				selectedMsMsFeatureBundle.getMsFeature().setPrimaryIdentity(selectedId);
				parentPanel.updateIdentificationsForMSMSFeatures(Collections.singleton(selectedMsMsFeatureBundle));
//				dataChanged = true;				
			}
			if(selectedMS1FeatureBundle != null
					&& !selectedId.equals(selectedMS1FeatureBundle.getMsFeature().getPrimaryIdentity())) {
				
				selectedMS1FeatureBundle.getMsFeature().setPrimaryIdentity(selectedId);
				parentPanel.updateIdentificationsForMSFeatures(Collections.singleton(selectedMS1FeatureBundle));
//				dataChanged = true;
			}
		}		
//		if (col == model.getColumnIndex(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN)) {
//			
//			MSFeatureIdentificationLevel selectedLevel = (MSFeatureIdentificationLevel) model.getValueAt(row,
//					model.getColumnIndex(UniversalIdentificationResultsTableModel.ID_LEVEL_COLUMN));
//			MSFeatureIdentificationLevel current = selectedId.getIdentificationLevel();
//			boolean updateIdLevel = false;
//			if((selectedLevel == null && current != null) || (selectedLevel != null && current == null)
//					|| (selectedLevel != null && current != null && !selectedLevel.equals(current))) {
//				updateIdLevel = true;
//				dataChanged = true;	
//			}		
//			if(selectedMsMsFeatureBundle != null && updateIdLevel) {
//				
//				try {
//					parentPanel.setPrimaryIdLevelForMultipleFeatures(
//							selectedLevel, 
//							2,
//							Collections.singleton(selectedMsMsFeatureBundle.getMsFeature()),
//							true);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}			
//			}
//			if(selectedMS1FeatureBundle != null && updateIdLevel) {
//				
//				try {
//					parentPanel.setPrimaryIdLevelForMultipleFeatures(
//							selectedLevel, 
//							1,
//							Collections.singleton(selectedMS1FeatureBundle.getMsFeature()),
//							true);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//		}
//		TODO handle other changes if necessary
		
//		if(dataChanged) {
//			
//			if(selectedMsMsFeatureBundle != null) 
//				parentPanel.selectMSMSFeature(selectedMsMsFeatureBundle);
//
//			if(selectedMS1FeatureBundle != null)
//				parentPanel.selectMSFeature(selectedMS1FeatureBundle);
//			
//			parentPanel.refreshIdentificationsTable();
//		}
	}
}
