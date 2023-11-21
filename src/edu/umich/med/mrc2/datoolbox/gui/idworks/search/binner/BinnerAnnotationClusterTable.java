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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.binner;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class BinnerAnnotationClusterTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3232039944841480790L;

	public BinnerAnnotationClusterTable() {

		super();
		model = new BinnerAnnotationClusterTableModel();		
		setModel(model);
		rowSorter = new TableRowSorter<BinnerAnnotationClusterTableModel>(
				(BinnerAnnotationClusterTableModel)model);
		setRowSorter(rowSorter);
		
		columnModel.getColumnById(BinnerAnnotationClusterTableModel.LIST_OF_ANNOTATIONS_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(BinnerAnnotationClusterTableModel.MZ_COLUMN).
			setCellRenderer(new FormattedDecimalRenderer(
				MRC2ToolBoxConfiguration.getMzFormat(), true));
		columnModel.getColumnById(BinnerAnnotationClusterTableModel.RT_COLUMN).
			setCellRenderer(new FormattedDecimalRenderer(
					MRC2ToolBoxConfiguration.getRtFormat(), true, true));
	
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);	
		finalizeLayout();
	}
	
	public void setTableModelFromBinnerAnnotationClusterCollection(
			Collection<BinnerAnnotationCluster>annotationClusters) {
		thf.setTable(null);
		((BinnerAnnotationClusterTableModel)model).setTableModelFromBinnerAnnotationClusterCollection(annotationClusters);
		thf.setTable(this);
		adjustColumns();
	}
	
	public BinnerAnnotationCluster getSelectedCluster(){
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (BinnerAnnotationCluster)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(BinnerAnnotationClusterTableModel.CLUSTER_COLUMN));
	}
	
	public Collection<BinnerAnnotationCluster>getSelectedClusters(){
		
		Collection<BinnerAnnotationCluster>selectedFeatures = new ArrayList<BinnerAnnotationCluster>();
		int[]selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selectedFeatures;
		
		int featureCol = model.getColumnIndex(BinnerAnnotationClusterTableModel.CLUSTER_COLUMN);
		for(int i : selectedRows)
			selectedFeatures.add(
					(BinnerAnnotationCluster)model.getValueAt(convertRowIndexToModel(i), featureCol));
		
		return selectedFeatures;
	}
	
	public Collection<BinnerAnnotationCluster>getAllClusters(){
		
		Collection<BinnerAnnotationCluster>selectedFeatures = new ArrayList<BinnerAnnotationCluster>();
		int featureCol = model.getColumnIndex(BinnerAnnotationClusterTableModel.CLUSTER_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selectedFeatures.add((BinnerAnnotationCluster)model.getValueAt(i, featureCol));
		
		return selectedFeatures;
	}
}
