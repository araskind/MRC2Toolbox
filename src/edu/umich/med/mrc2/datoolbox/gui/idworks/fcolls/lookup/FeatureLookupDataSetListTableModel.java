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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class FeatureLookupDataSetListTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7524456898019646208L;

	public static final String DATA_SET_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
//	public static final String NUM_FEATURES_COLUMN = "# of features";
	public static final String OWNER_COLUMN = "Owner";
	public static final String DATE_CREATED_COLUMN = "Date created";
	public static final String LAST_MODIFIED_COLUMN = "Last modified";

	public FeatureLookupDataSetListTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(DATA_SET_COLUMN, FeatureLookupDataSet.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
//			new ColumnContext(NUM_FEATURES_COLUMN, Integer.class, true),
			new ColumnContext(OWNER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, true),
			new ColumnContext(LAST_MODIFIED_COLUMN, Date.class, true),
		};
	}

	public void setTableModelFromFeatureLookupDataSetList(
			Collection<FeatureLookupDataSet>dataSetList) {

		setRowCount(0);
		if(dataSetList == null || dataSetList .isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (FeatureLookupDataSet dataSet : dataSetList) {

			Object[] obj = {
				dataSet,
				dataSet.getDescription(),
//				dataSet.getFeatures().size(),
				dataSet.getCreatedBy(),
				dataSet.getDateCreated(),
				dataSet.getLastModified(),
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
	
	public int getFeatureLookupDataSetRow(FeatureLookupDataSet dataSet) {

		int col = getColumnIndex(DATA_SET_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (dataSet.equals(getValueAt(i, col)))
				return i;
		}
		return -1;
	}
	
	public void updateCollectionData(FeatureLookupDataSet edited) {
		
		int row = getFeatureLookupDataSetRow(edited);
		if(row == -1)
			return;
		
		setValueAt(edited, row, getColumnIndex(DATA_SET_COLUMN));
		setValueAt(edited.getDescription(), row, getColumnIndex(DESCRIPTION_COLUMN));
//		setValueAt(edited.getFeatures().size(), row, getColumnIndex(NUM_FEATURES_COLUMN));
		setValueAt(edited.getCreatedBy(), row, getColumnIndex(OWNER_COLUMN));
		setValueAt(edited.getDateCreated(), row, getColumnIndex(DATE_CREATED_COLUMN));
		setValueAt(edited.getLastModified(), row, getColumnIndex(LAST_MODIFIED_COLUMN));		
	}
}
