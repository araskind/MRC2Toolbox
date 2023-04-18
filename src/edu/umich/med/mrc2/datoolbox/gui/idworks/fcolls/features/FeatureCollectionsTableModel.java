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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;

public class FeatureCollectionsTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7524456898019646208L;

	public static final String COLLECTION_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String NUM_FEATURES_COLUMN = "# of features";
	public static final String OWNER_COLUMN = "Owner";
	public static final String DATE_CREATED_COLUMN = "Date created";
	public static final String LAST_MODIFIED_COLUMN = "Last modified";

	public FeatureCollectionsTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(COLLECTION_COLUMN, MsFeatureInfoBundleCollection.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(NUM_FEATURES_COLUMN, Integer.class, true),
			new ColumnContext(OWNER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, true),
			new ColumnContext(LAST_MODIFIED_COLUMN, Date.class, true),
		};
	}

	public void setTableModelFromFeatureCollectionList(
			Collection<MsFeatureInfoBundleCollection>featureCollections) {

		setRowCount(0);
		if(featureCollections == null || featureCollections.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MsFeatureInfoBundleCollection collection : featureCollections) {
			
			if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(collection)
					&& FeatureCollectionManager.getMsFeatureInfoBundleCollectionSize(collection) == 0) {
				continue;
			}
			Object[] obj = {
				collection,
				collection.getDescription(),
				FeatureCollectionManager.getMsFeatureInfoBundleCollectionSize(collection),
				collection.getOwner(),
				collection.getDateCreated(),
				collection.getLastModified(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public int getFeatureCollectionRow(MsFeatureInfoBundleCollection fCol) {

		int col = getColumnIndex(COLLECTION_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (fCol.equals(getValueAt(i, col)))
				return i;
		}
		return -1;
	}
	
	public void updateCollectionData(MsFeatureInfoBundleCollection edited) {
		
		int row = getFeatureCollectionRow(edited);
		if(row == -1)
			return;
		
		setValueAt(edited, row, getColumnIndex(COLLECTION_COLUMN));
		setValueAt(edited.getDescription(), 
				row, getColumnIndex(DESCRIPTION_COLUMN));
		setValueAt(FeatureCollectionManager.getMsFeatureInfoBundleCollectionSize(edited), 
				row, getColumnIndex(NUM_FEATURES_COLUMN));
		setValueAt(edited.getOwner(), row, getColumnIndex(OWNER_COLUMN));
		setValueAt(edited.getDateCreated(), row, getColumnIndex(DATE_CREATED_COLUMN));
		setValueAt(edited.getLastModified(), row, getColumnIndex(LAST_MODIFIED_COLUMN));		
	}
}
