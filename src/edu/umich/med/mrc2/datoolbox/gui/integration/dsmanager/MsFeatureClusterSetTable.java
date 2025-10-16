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

import java.util.Set;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MsFeatureClusterSetTable extends BasicTable {

	private static final long serialVersionUID = -4918759221443655308L;

	public MsFeatureClusterSetTable() {

		super();
		model = new MsFeatureClusterSetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsFeatureClusterSetTableModel>(
				(MsFeatureClusterSetTableModel)model);
		setRowSorter(rowSorter);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		
		columnModel.getColumnById(MsFeatureClusterSetTableModel.CLUSTER_COUNT_COLUMN).setPreferredWidth(80);
		columnModel.getColumnById(MsFeatureClusterSetTableModel.CLUSTER_COUNT_COLUMN).setMaxWidth(80);
		fixedWidthColumns.add(model.getColumnIndex(MsFeatureClusterSetTableModel.CLUSTER_COUNT_COLUMN));
		columnModel.getColumnById(MsFeatureClusterSetTableModel.FEATURE_COUNT_COLUMN).setPreferredWidth(80);
		columnModel.getColumnById(MsFeatureClusterSetTableModel.FEATURE_COUNT_COLUMN).setMaxWidth(80);
		fixedWidthColumns.add(model.getColumnIndex(MsFeatureClusterSetTableModel.FEATURE_COUNT_COLUMN));
	
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromExperiment(DataAnalysisProject currentProject) {
		((MsFeatureClusterSetTableModel)model).setTableModelFromExperiment(currentProject);
		adjustColumns();
	}

	public MsFeatureClusterSet getSelectedMsFeatureClusterSet() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		else {
			return (MsFeatureClusterSet)model.getValueAt(convertRowIndexToModel(row), 
					model.getColumnIndex(MsFeatureClusterSetTableModel.FEATURE_CLUSTER_SET_COLUMN));
		}
	}
	
	public Set<MsFeatureClusterSet> getSelectedMsFeatureClusterSets() {
		
		Set<MsFeatureClusterSet>selected = new TreeSet<>();
		int col = model.getColumnIndex(MsFeatureClusterSetTableModel.FEATURE_CLUSTER_SET_COLUMN);
		int[]rows = getSelectedRows();
		for(int row : rows)
			selected.add((MsFeatureClusterSet)model.getValueAt(convertRowIndexToModel(row), col));
		
		return selected;
	}
}
