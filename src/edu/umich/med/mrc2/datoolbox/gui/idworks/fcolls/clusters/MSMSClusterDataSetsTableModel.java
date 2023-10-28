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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;

public class MSMSClusterDataSetsTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7524456898019646208L;

	public static final String CLUSTER_DATA_SET_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String NUM_CLUSTERS_COLUMN = "# of clusters";
	public static final String OWNER_COLUMN = "Created by";
	public static final String DATE_CREATED_COLUMN = "Date created";
	public static final String LAST_MODIFIED_COLUMN = "Last modified";

	public MSMSClusterDataSetsTableModel() {

		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(CLUSTER_DATA_SET_COLUMN, "Cluster data set name", MSMSClusterDataSet.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(NUM_CLUSTERS_COLUMN, "Number of clusters", Integer.class, true),
			new ColumnContext(OWNER_COLUMN, OWNER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, DATE_CREATED_COLUMN, Date.class, true),
			new ColumnContext(LAST_MODIFIED_COLUMN, LAST_MODIFIED_COLUMN, Date.class, true),
		};
	}

	public void setTableModelFromMSMSClusterDataSetList(
			Collection<MSMSClusterDataSet> msmsClusterDataSetCollections) {

		setRowCount(0);
		if(msmsClusterDataSetCollections == null 
				|| msmsClusterDataSetCollections.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MSMSClusterDataSet dataSet : msmsClusterDataSetCollections) {

			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(!MSMSClusterDataSetManager.getEditableMSMSClusterDataSets().contains(dataSet)
						&& MSMSClusterDataSetManager.getMSMSClusterDataSetSize(dataSet) == 0) {
					continue;
				}
			}
			else{
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
							getMsmsClusterDataSets().contains(dataSet) && dataSet.getClusters().size() == 0)
					continue;
			}
			Object[] obj = {
				dataSet,
				dataSet.getDescription(),
				MSMSClusterDataSetManager.getMSMSClusterDataSetSize(dataSet),
				dataSet.getCreatedBy(),
				dataSet.getDateCreated(),
				dataSet.getLastModified(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public int getMSMSClusterDataSetRow(MSMSClusterDataSet fCol) {

		int col = getColumnIndex(CLUSTER_DATA_SET_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (fCol.equals(getValueAt(i, col)))
				return i;
		}
		return -1;
	}
	
	public void updateMSMSClusterDataSetData(MSMSClusterDataSet edited) {
		
		int row = getMSMSClusterDataSetRow(edited);
		if(row == -1)
			return;
		
		setValueAt(edited, row, getColumnIndex(CLUSTER_DATA_SET_COLUMN));
		setValueAt(edited.getDescription(), row, getColumnIndex(DESCRIPTION_COLUMN));
		setValueAt(MSMSClusterDataSetManager.getMSMSClusterDataSetSize(edited), 
				row, getColumnIndex(NUM_CLUSTERS_COLUMN));
		setValueAt(edited.getCreatedBy(), row, getColumnIndex(OWNER_COLUMN));
		setValueAt(edited.getDateCreated(), row, getColumnIndex(DATE_CREATED_COLUMN));
		setValueAt(edited.getLastModified(), row, getColumnIndex(LAST_MODIFIED_COLUMN));		
	}
}
