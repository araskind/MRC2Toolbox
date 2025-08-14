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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;

public class SubsetFeaturesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6510383188876035906L;

	public SubsetFeaturesTable() {

		super();
		model = new SubsetFeaturesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SubsetFeaturesTableModel>(
				(SubsetFeaturesTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(columnModel.getColumnIndex(SubsetFeaturesTableModel.MS_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		cfRenderer = new MsFeatureRenderer(SortProperty.Name);
		columnModel.getColumnById(SubsetFeaturesTableModel.MS_FEATURE_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(SubsetFeaturesTableModel.ORDER_COLUMN).setMaxWidth(50);
		fixedWidthColumns.add(
				columnModel.getColumnIndex(SubsetFeaturesTableModel.ORDER_COLUMN));
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFeatureSet(MsFeatureSet featureSet) {

		thf.setTable(null);
		((SubsetFeaturesTableModel)model).setTableModelFromFeatureSet(featureSet);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addFeatures(Collection<MsFeature> features) {
		
		thf.setTable(null);
		Collection<MsFeature> current = getAllFeatures();
		Collection<MsFeature>toAdd = features.stream().
				filter(f -> !current.contains(f)).collect(Collectors.toSet());
		if(toAdd.isEmpty())
			return;
		
		current.addAll(toAdd);
		
		((SubsetFeaturesTableModel)model).setTableModelFromFeatures(current);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void setTableModelFromFeatures(Collection<MsFeature> features) {
		thf.setTable(null);
		((SubsetFeaturesTableModel)model).setTableModelFromFeatures(features);
		thf.setTable(this);
		adjustColumns();
	}
	
	public Collection<MsFeature> getAllFeatures() {
		
		Collection<MsFeature> features = new ArrayList<MsFeature>();
		int col = model.getColumnIndex(SubsetFeaturesTableModel.MS_FEATURE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) 
			features.add((MsFeature)model.getValueAt(i, col));
		
		return features;	
	}
	
	public Collection<MsFeature> getSelectedFeatures() {
		
		Collection<MsFeature> features = new ArrayList<MsFeature>();
		int[]rows = getSelectedRows();
		if(rows.length == 0)
			return features;
		
		int col = model.getColumnIndex(SubsetFeaturesTableModel.MS_FEATURE_COLUMN);
		for(int i : rows) 
			features.add((MsFeature)model.getValueAt(convertRowIndexToModel(i), col));
		
		return features;	
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		if(convertColumnIndexToModel(column) == 
				model.getColumnIndex(SubsetFeaturesTableModel.ORDER_COLUMN))
			return row+1;
		else
			return super.getValueAt(row, column);
	}
}















