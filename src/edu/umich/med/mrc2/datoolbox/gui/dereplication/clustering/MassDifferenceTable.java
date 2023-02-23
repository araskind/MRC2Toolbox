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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;

public class MassDifferenceTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3341027440098202037L;
	private MassDifferenceTableModel model;

	public MassDifferenceTable() {

		super();
		model = new MassDifferenceTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MassDifferenceTableModel>(model);
		setRowSorter(rowSorter);	
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rowSorter.setComparator(model.getColumnIndex(MassDifferenceTableModel.FEATURE_ONE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));
		rowSorter.setComparator(model.getColumnIndex(MassDifferenceTableModel.FEATURE_TWO_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		chmodRenderer = new AdductRenderer();
		cfRenderer = new MsFeatureRenderer(SortProperty.Name);

		columnModel.getColumnById(MassDifferenceTableModel.DELTA_COLUMN)
				.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MassDifferenceTableModel.FEATURE_ONE_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(MassDifferenceTableModel.FEATURE_TWO_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(MassDifferenceTableModel.ANNOTATION_COLUMN)
				.setCellRenderer(chmodRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFeatureCluster(
			MsFeatureCluster selectedCluster) {

		thf.setTable(null);
		model.setTableModelFromFeatureCluster(selectedCluster);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public void setTableModelFromFeatures(
			Collection<MsFeature> selectedFeatures, MsFeatureCluster activeCluster) {

		thf.setTable(null);
		model.setTableModelFromFeatures(selectedFeatures, activeCluster);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public Collection<MsFeature>getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int row = getSelectedRow();
		if(row == -1)
			return selected;		

		int modelRow = convertRowIndexToModel(row);
		selected.add((MsFeature) model.getValueAt(modelRow, 
				model.getColumnIndex(MassDifferenceTableModel.FEATURE_ONE_COLUMN)));
		selected.add((MsFeature) model.getValueAt(modelRow, 
				model.getColumnIndex(MassDifferenceTableModel.FEATURE_TWO_COLUMN)));
		
		return selected;
	}
	
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		
		int f1col = model.getColumnIndex(MassDifferenceTableModel.FEATURE_ONE_COLUMN);
		int dp1col = model.getColumnIndex(MassDifferenceTableModel.DATA_PIPELINE_ONE_COLUMN);
		int f2col = model.getColumnIndex(MassDifferenceTableModel.FEATURE_TWO_COLUMN);
		int dp2col = model.getColumnIndex(MassDifferenceTableModel.DATA_PIPELINE_TWO_COLUMN);
		for (int i : getSelectedRows()) {
			
			int modelRow = convertRowIndexToModel(i);
			MsFeature fOne = (MsFeature) model.getValueAt(modelRow, f1col);
			DataPipeline dpOne = (DataPipeline) model.getValueAt(modelRow, dp1col);
			if(!featureMap.containsKey(dpOne))
				featureMap.put(dpOne, new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.Name)));
			
			featureMap.get(dpOne).add(fOne);
			
			MsFeature fTwo = (MsFeature) model.getValueAt(modelRow, f2col);
			DataPipeline dpTwo = (DataPipeline) model.getValueAt(modelRow, dp2col);
			if(!featureMap.containsKey(dpTwo))
				featureMap.put(dpTwo, new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.Name)));
			
			featureMap.get(dpTwo).add(fTwo);
		}
		return featureMap;
	}
}









