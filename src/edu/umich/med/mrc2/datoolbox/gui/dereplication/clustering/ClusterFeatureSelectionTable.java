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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.FeatureSelectionTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;

public class ClusterFeatureSelectionTable extends FeatureSelectionTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 479098239886904475L;
	private ClusterFeatureSelectionTableModelListener modelListener;
	private MsFeatureCluster activeCluster;

	public ClusterFeatureSelectionTable() {

		super();
		model = new ClusterFeatureSelectionTableModel();
		setModel(model);
		modelListener = new ClusterFeatureSelectionTableModelListener();
		model.addTableModelListener(modelListener);

		featureSorter = new TableRowSorter<ClusterFeatureSelectionTableModel>(
				(ClusterFeatureSelectionTableModel)model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		chmodRenderer = new AdductRenderer();

		columnModel.getColumnById(ClusterFeatureSelectionTableModel.PRIMARY_COLUMN)
				.setCellRenderer(radioRenderer); // Primary feature
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.PRIMARY_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.FEATURE_COLUMN)
				.setCellRenderer(cfRenderer); // Feature name
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.CHEM_MOD_COLUMN)
				.setCellRenderer(chmodRenderer); // Compound form column
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.RETENTION_COLUMN)
				.setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.NEUTRAL_MASS_COLUMN)
				.setCellRenderer(mzRenderer); // base peakMZ
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.BASE_PEAK_COLUMN)
				.setCellRenderer(mzRenderer); // base peakMZ
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.POOLED_MEAN_COLUMN)
				.setCellRenderer(pooledMeanRenderer); // Pooled median
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.POOLED_RSD_COLUMN)
				.setCellRenderer(percentRenderer); // Pooled RSD
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.POOLED_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer); // Pooled frequency
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.SAMPLE_MEAN_COLUMN)
				.setCellRenderer(sampleMeanRenderer); // Sample median
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.SAMPLE_RSD_COLUMN)
				.setCellRenderer(percentRenderer); // Sample RSD
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.SAMPLE_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer); // Sample frequency
		
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.PRIMARY_COLUMN)
			.setWidth(50);
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.PRIMARY_COLUMN)
			.setMaxWidth(50);
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.INCLUDE_COLUMN)
			.setWidth(50);
		columnModel.getColumnById(ClusterFeatureSelectionTableModel.INCLUDE_COLUMN)
			.setMaxWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(
				ClusterFeatureSelectionTableModel.PRIMARY_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(
				ClusterFeatureSelectionTableModel.INCLUDE_COLUMN));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	private class ClusterFeatureSelectionTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = convertRowIndexToView(e.getFirstRow());
			int col = convertColumnIndexToView(e.getColumn());
			int featureCol = getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN);
			MsFeature selectedFeature = (MsFeature) getValueAt(row, featureCol);

			if (col == getColumnIndex(ClusterFeatureSelectionTableModel.PRIMARY_COLUMN)) {

				if((boolean) getValueAt(row, col))
					activeCluster.setPrimaryFeature(selectedFeature);

				model.removeTableModelListener(modelListener);
				((ClusterFeatureSelectionTableModel)model).reloadData();
				model.addTableModelListener(modelListener);
			}
			if (col == getColumnIndex(ClusterFeatureSelectionTableModel.INCLUDE_COLUMN)) {

				activeCluster.setFeatureEnabled(selectedFeature, (boolean) getValueAt(row, col));
				model.removeTableModelListener(modelListener);
				((ClusterFeatureSelectionTableModel)model).reloadData();
				model.addTableModelListener(modelListener);
			}
		}
	}

	@Override
	public synchronized void clearTable() {

		model.removeTableModelListener(modelListener);
		model.setRowCount(0);
		model.addTableModelListener(modelListener);
	}
	public int getFeatureRow(MsFeature feature) {

		int row = -1;
		int col = getColumnModel().getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN);

		for (int i = 0; i < model.getRowCount(); i++) {

			MsFeature rowFeature = (MsFeature) model.getValueAt(i, col);

			if (rowFeature.equals(feature)) {
				row = convertRowIndexToView(i);
				break;
			}
		}
		return row;
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {

		activeCluster = featureCluster;
		Collection<MsFeature> featureList = featureCluster.getFeatures();
		model.removeTableModelListener(modelListener);
		setrendererReferences(featureList);
		((ClusterFeatureSelectionTableModel)model).setTableModelFromFeatureCluster(featureCluster);
		adjustColumns();
		model.addTableModelListener(modelListener);
	}

	@Override
	public MsFeature getSelectedFeature() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeature) getValueAt(row, 
				getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN));
	}
	
	@Override
	public Collection<MsFeature>getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int col = getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN);
		for (int i : getSelectedRows()) {

			MsFeature rowFeature = (MsFeature) getValueAt(i, col);
			selected.add(rowFeature);
		}
		return selected;
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		int fcol = model.getColumnIndex(ClusterFeatureSelectionTableModel.FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(ClusterFeatureSelectionTableModel.DATA_PIPELINE_COLUMN);
		for (int i : getSelectedRows()) {
			
			MsFeature rowFeature = (MsFeature) getValueAt(i, fcol);
			DataPipeline dp = (DataPipeline) getValueAt(i, dpcol);
			if(!featureMap.containsKey(dp))
				featureMap.put(dp, new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.Name)));

			featureMap.get(dp).add(rowFeature);
		}
		return featureMap;
	}
}






















