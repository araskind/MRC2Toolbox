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

package edu.umich.med.mrc2.datoolbox.gui.integration;

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

public class DataIntegrationFeatureSelectionTable extends FeatureSelectionTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7042158268272205223L;
	private MsFeatureCluster activeCluster;
	private DataIntegrationFeatureSelectionTableModelListener modelListener;

	public DataIntegrationFeatureSelectionTable() {

		super();

		model = new DataIntegrationFeatureSelectionTableModel();
		setModel(model);
		modelListener = new DataIntegrationFeatureSelectionTableModelListener();
		model.addTableModelListener(modelListener);

		featureSorter = new TableRowSorter<DataIntegrationFeatureSelectionTableModel>(
				(DataIntegrationFeatureSelectionTableModel)model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.SCORE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.RETENTION_COLUMN)
				.setCellRenderer(rtRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.BASE_PEAK_COLUMN)
				.setCellRenderer(mzRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.POOLED_MEAN_COLUMN)
				.setCellRenderer(pooledMeanRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.POOLED_RSD_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.POOLED_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.SAMPLE_MEAN_COLUMN)
				.setCellRenderer(sampleMeanRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.SAMPLE_RSD_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.SAMPLE_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer);
		
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN).setWidth(50);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN).setWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.ID_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN));

		finalizeLayout();
	}

	private class DataIntegrationFeatureSelectionTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int row = convertRowIndexToView(e.getFirstRow());
			int col = convertColumnIndexToView(e.getColumn());
			int featureCol = getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);
			MsFeature selectedFeature = (MsFeature) getValueAt(row, featureCol);
			if (col == getColumnIndex(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)) {

				boolean isPrimary = (boolean) getValueAt(row, col);
				if(isPrimary)
					activeCluster.setPrimaryFeature(selectedFeature);

				model.removeTableModelListener(modelListener);
				((DataIntegrationFeatureSelectionTableModel)model).reloadData();
				model.addTableModelListener(modelListener);
			}
			if (col == getColumnIndex(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN)) {

				boolean included = (boolean) getValueAt(row, col);
				activeCluster.setFeatureEnabled(selectedFeature, included);
				model.removeTableModelListener(modelListener);
				((DataIntegrationFeatureSelectionTableModel)model).reloadData();
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
		int col = getColumnModel().getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);

		for (int i = 0; i < getRowCount(); i++) {

			MsFeature rowFeature = (MsFeature)getValueAt(i, col);

			if (rowFeature.equals(feature)) {
				row = i;
				break;
			}
		}
		return row;
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {

		activeCluster = featureCluster;

		if(activeCluster != null) {

			model.removeTableModelListener(modelListener);
			((DataIntegrationFeatureSelectionTableModel)model).setCurrentCluster(featureCluster);
			setrendererReferences(featureCluster.getFeatures());
			((DataIntegrationFeatureSelectionTableModel)model).setTableModelFromFeatureCluster(activeCluster);;
			adjustColumns();
			model.addTableModelListener(modelListener);
		}
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int col = getColumnModel().getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);

		for (int i : getSelectedRows()) {

			MsFeature rowFeature = (MsFeature) model.getValueAt(convertRowIndexToModel(i), col);
			selected.add(rowFeature);
		}
		return selected;
	}

	@Override
	public MsFeature getSelectedFeature() {

		MsFeature selected = null;
		int col = getColumnModel().getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);
		int row = getSelectedRow();

		if(row > -1)
			selected = (MsFeature) model.getValueAt(convertRowIndexToModel(row), col);

		return selected;
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		int fcol = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.DATA_PIPELINE_COLUMN);
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
