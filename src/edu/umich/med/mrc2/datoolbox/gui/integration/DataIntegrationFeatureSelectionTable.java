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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.FeatureSelectionTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

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

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN)
			.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN)
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
		
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.ID_COLUMN).setMinWidth(30);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN).setMinWidth(50);
		columnModel.getColumnById(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN).setMinWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.ID_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN));

		finalizeLayout();
	}

	private class DataIntegrationFeatureSelectionTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {
			
			if(activeCluster == null)
				return;

			int row = e.getFirstRow();
			int col = e.getColumn();
			int featureCol = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);
			MsFeature selectedFeature = (MsFeature) model.getValueAt(row, featureCol);
			int pipelineColumn = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.DATA_PIPELINE_COLUMN);
			DataPipeline dataPipeline = (DataPipeline) model.getValueAt(row, pipelineColumn);
			
			if (col == model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.ID_COLUMN)) {

				boolean isPrimary = (boolean) model.getValueAt(row, col);
				if(isPrimary)
					activeCluster.setPrimaryFeature(selectedFeature);
			}
			if (col == model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.MERGE_COLUMN)) {

				boolean included = (boolean) model.getValueAt(row, col);
	
				if(included && ((!activeCluster.getMarkedForMerge().isEmpty() 
						&& !pipelineMatches(dataPipeline)) 
						|| (selectedFeature instanceof LibraryMsFeature 
								&& ((LibraryMsFeature)selectedFeature).isMerged()))) {
						
						String message = "Only individual features from the same"
								+ " data pipeline may be merged.";
						MessageDialog.showWarningMsg(
								message, DataIntegrationFeatureSelectionTable.this);
						reloadData();
						return;
				}
				else {
					activeCluster.setFeatureEnabled(selectedFeature, included);
					activeCluster.markFeatureForMerging(selectedFeature, included);
				}
			}
			if (col == model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.TOP_MATCH_COLUMN)) {
				
				boolean isTopMatch = (boolean)  model.getValueAt(row, col);
				if(selectedFeature.equals(activeCluster.getPrimaryFeature())){
					
					if(isTopMatch) {
						String message = "Top match must be other than primary feature.";
						MessageDialog.showWarningMsg(
								message, DataIntegrationFeatureSelectionTable.this);
						reloadData();
						return;
					}
				}
				else {
					if(isTopMatch)
						activeCluster.setTopMatchFeature(selectedFeature);
					else
						activeCluster.setTopMatchFeature(null);
				}				
			}
			reloadData();
		}
		
		private boolean pipelineMatches(DataPipeline dataPipeline) {
			
			MsFeature firstMarked = activeCluster.getMarkedForMerge().iterator().next();
			DataPipeline markedPipeline = activeCluster.getDataPipelineForFeature(firstMarked);
			return dataPipeline.equals(markedPipeline);
		}
	}
	
	private void reloadData() {
		model.removeTableModelListener(modelListener);
		((DataIntegrationFeatureSelectionTableModel)model).reloadData();
		model.addTableModelListener(modelListener);
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

		ArrayList<MsFeature> selected = new ArrayList<>();
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
				new TreeMap<>();
		int fcol = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(DataIntegrationFeatureSelectionTableModel.DATA_PIPELINE_COLUMN);
		for (int i : getSelectedRows()) {
			
			MsFeature rowFeature = (MsFeature) getValueAt(i, fcol);
			DataPipeline dp = (DataPipeline) getValueAt(i, dpcol);
			featureMap.computeIfAbsent(dp, v -> new HashSet<MsFeature>());

			featureMap.get(dp).add(rowFeature);
		}
		return featureMap;
	}
}
