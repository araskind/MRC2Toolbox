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
import java.util.Collections;
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
	private SubsetFeaturesTableModel model;

	public SubsetFeaturesTable() {

		super();
		model = new SubsetFeaturesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SubsetFeaturesTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(columnModel.getColumnIndex(SubsetFeaturesTableModel.MS_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		cfRenderer = new MsFeatureRenderer(SortProperty.Name);
		columnModel.getColumnById(SubsetFeaturesTableModel.MS_FEATURE_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(SubsetFeaturesTableModel.ORDER_COLUMN).setMaxWidth(50);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFeatureSet(MsFeatureSet featureSet) {

		thf.setTable(null);
		model.setTableModelFromFeatureSet(featureSet);
		thf.setTable(this);
		tca.adjustColumnsExcluding(Collections.singleton(
				columnModel.getColumnIndex(SubsetFeaturesTableModel.ORDER_COLUMN)));
	}
	
	public void addFeatures(Collection<MsFeature> features) {
		
		thf.setTable(null);
		Collection<MsFeature> current = getFeatures();
		Collection<MsFeature>toAdd = features.stream().
				filter(f -> !current.contains(f)).collect(Collectors.toSet());
		if(toAdd.isEmpty())
			return;
		
		current.addAll(toAdd);
		
		model.setTableModelFromFeatures(current);
		thf.setTable(this);
		tca.adjustColumnsExcluding(Collections.singleton(
				columnModel.getColumnIndex(SubsetFeaturesTableModel.ORDER_COLUMN)));
	}
	
	public void setTableModelFromFeatures(Collection<MsFeature> features) {
		thf.setTable(null);
		model.setTableModelFromFeatures(features);
		thf.setTable(this);
		tca.adjustColumnsExcluding(Collections.singleton(
				columnModel.getColumnIndex(SubsetFeaturesTableModel.ORDER_COLUMN)));
	}
	
	public Collection<MsFeature> getFeatures() {
		
		Collection<MsFeature> features = new ArrayList<MsFeature>();
		int col = model.getColumnIndex(SubsetFeaturesTableModel.MS_FEATURE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) 
			features.add((MsFeature)model.getValueAt(i, col));
		
		return features;	
	}
}















