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

package edu.umich.med.mrc2.datoolbox.gui.integration.dsmanager;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsFeatureClusterSetTableModel extends BasicTableModel {

	private static final long serialVersionUID = 8757581409205015995L;

	public static final String FEATURE_CLUSTER_SET_COLUMN = "Feature cluster set";
	public static final String CLUSTER_COUNT_COLUMN = "# Clusters";
	public static final String FEATURE_COUNT_COLUMN = "# Features";

	public MsFeatureClusterSetTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FEATURE_CLUSTER_SET_COLUMN, FEATURE_CLUSTER_SET_COLUMN, MsFeatureClusterSet.class, false),
			new ColumnContext(CLUSTER_COUNT_COLUMN, CLUSTER_COUNT_COLUMN, Integer.class, false),
			new ColumnContext(FEATURE_COUNT_COLUMN, FEATURE_COUNT_COLUMN, Integer.class, false),
		};
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {
		
		if(currentProject == null 
				|| currentProject.getFeatureClusterSets().isEmpty()) {
			setRowCount(0);
			return;
		}
		setRowCount(0);
		List<Object[]>rowData = new ArrayList<>();
		for (MsFeatureClusterSet cs : currentProject.getFeatureClusterSets()) {

			Object[] obj = {
					cs,
					cs.getNumberOfClusters(),
					cs.getNumberOfFeatures(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}










