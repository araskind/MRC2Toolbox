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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MinimalMSOneFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3232039944841480790L;

	public MinimalMSOneFeatureTable() {

		super();
		model = new MinimalMSOneFeatureTableModel();		
		setModel(model);
		rowSorter = new TableRowSorter<MinimalMSOneFeatureTableModel>(
				(MinimalMSOneFeatureTableModel)model);
		setRowSorter(rowSorter);
		
		columnModel.getColumnById(MinimalMSOneFeatureTableModel.SMILES_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(MinimalMSOneFeatureTableModel.INCHI_KEY_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());
		columnModel.getColumnById(MinimalMSOneFeatureTableModel.FOLD_CHANGE_COLUMN).
			setCellRenderer(new FormattedDecimalRenderer(
				MRC2ToolBoxConfiguration.getPpmFormat(), true));
		columnModel.getColumnById(MinimalMSOneFeatureTableModel.P_VALUE_COLUMN).
			setCellRenderer(new FormattedDecimalRenderer(
					MRC2ToolBoxConfiguration.defaultMzFormat, true, true));
	
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);	
		finalizeLayout();
	}
	
	public void setTableModelFromFeatureCollection(Collection<MinimalMSOneFeature>features) {
		thf.setTable(null);
		((MinimalMSOneFeatureTableModel)model).setTableModelFromFeatureCollection(features);
		thf.setTable(this);
		adjustColumns();
	}
	
	public MinimalMSOneFeature getSelectedFeature(){
		
		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MinimalMSOneFeature)model.getValueAt(
				convertRowIndexToModel(row), 
				model.getColumnIndex(MinimalMSOneFeatureTableModel.FEATURE_COLUMN));
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		
		Collection<MinimalMSOneFeature>selectedFeatures = new ArrayList<MinimalMSOneFeature>();
		int[]selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return selectedFeatures;
		
		int featureCol = model.getColumnIndex(MinimalMSOneFeatureTableModel.FEATURE_COLUMN);
		for(int i : selectedRows)
			selectedFeatures.add(
					(MinimalMSOneFeature)model.getValueAt(convertRowIndexToModel(i), featureCol));
		
		return selectedFeatures;
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		
		Collection<MinimalMSOneFeature>selectedFeatures = new ArrayList<MinimalMSOneFeature>();
		int featureCol = model.getColumnIndex(MinimalMSOneFeatureTableModel.FEATURE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			selectedFeatures.add((MinimalMSOneFeature)model.getValueAt(i, featureCol));
		
		return selectedFeatures;
	}
}
