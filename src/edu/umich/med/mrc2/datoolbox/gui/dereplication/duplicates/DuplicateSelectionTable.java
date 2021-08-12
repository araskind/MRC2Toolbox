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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.duplicates;

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
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalModificationRenderer;

public class DuplicateSelectionTable extends FeatureSelectionTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4922558139324447994L;

	private MsFeatureCluster activeCluster;
	private DuplicateSelectionTableModel model;
	private DuplicateSelectionTableModelListener modelListener;

	public DuplicateSelectionTable() {

		super();

		model = new DuplicateSelectionTableModel();
		// model
		setModel(model);
		modelListener = new DuplicateSelectionTableModelListener();
		model.addTableModelListener(modelListener);

		featureSorter = new TableRowSorter<DuplicateSelectionTableModel>(model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		chmodRenderer = new ChemicalModificationRenderer();

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(DuplicateSelectionTableModel.ID_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.ID_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(DuplicateSelectionTableModel.DATA_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.DATA_COLUMN)
				.setCellEditor(radioEditor);
		columnModel.getColumnById(DuplicateSelectionTableModel.FEATURE_COLUMN)
			.setCellRenderer(cfRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.SCORE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.RETENTION_COLUMN)
				.setCellRenderer(rtRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.OBSERVED_RETENTION_COLUMN)
			.setCellRenderer(rtRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.BASE_PEAK_COLUMN)
				.setCellRenderer(mzRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.POOLED_MEAN_COLUMN)
				.setCellRenderer(pooledMeanRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.POOLED_RSD_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.POOLED_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.SAMPLE_MEAN_COLUMN)
				.setCellRenderer(sampleMeanRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.SAMPLE_RSD_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DuplicateSelectionTableModel.SAMPLE_FREQUENCY_COLUMN)
				.setCellRenderer(pieChartFrequencyRenderer);

		addColumnSelectorPopup();

		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
	}

	private class DuplicateSelectionTableModelListener implements TableModelListener {

		public void tableChanged(TableModelEvent e) {

			int featureCol = getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN);
			MsFeature selectedFeature = (MsFeature) getValueAt(e.getFirstRow(), featureCol);
			int row = convertRowIndexToView(e.getFirstRow());
			int col = convertColumnIndexToView(e.getColumn());

			if (col == getColumnIndex(DuplicateSelectionTableModel.ID_COLUMN)) {

				if((boolean) getValueAt(row, col))
					activeCluster.setPrimaryFeature(selectedFeature);

				model.removeTableModelListener(modelListener);
				model.reloadData();
				model.addTableModelListener(modelListener);
			}
			if (col == getColumnIndex(DuplicateSelectionTableModel.DATA_COLUMN)) {

				if((boolean) getValueAt(row, col)) {

					for(MsFeature f : activeCluster.getFeatures()) {

						if(f.equals(selectedFeature))
							activeCluster.setFeatureEnabled(f, true);
						else
							activeCluster.setFeatureEnabled(f, false);
					}
				}
				model.removeTableModelListener(modelListener);
				model.reloadData();
				model.addTableModelListener(modelListener);
			}
		}
	}

	@Override
	public void clearTable() {

		model.removeTableModelListener(modelListener);
		model.setRowCount(0);
		model.addTableModelListener(modelListener);
	}

/*
	@Override
	public void editingStopped(ChangeEvent event) {

		super.editingStopped(event);
		if(this.getSelectedRow() > -1) {

			MsFeature selectedFeature;
			boolean selected;
			boolean selectionPresent;
			int idColumn  = getColumnModel().getColumnIndex(DuplicateSelectionTableModel.ID_COLUMN);
			int dataColumn  = getColumnModel().getColumnIndex(DuplicateSelectionTableModel.DATA_COLUMN);
			int featureColumn = getColumnModel().getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN);

			if (event.getSource().getClass().equals(RadioButtonEditor.class)) {

				selected = (boolean) ((DefaultCellEditor) event.getSource()).getCellEditorValue();
				selectedFeature = (MsFeature) this.getValueAt(this.getSelectedRow(), featureColumn);
				selectionPresent = false;

				if(this.getSelectedColumn() == idColumn) {

					if (!selected) {

						selectedFeature.setPrinmary(false);

						for (MsFeature cf : activeCluster.getFeatures()) {

							if (cf.isPrinmary()) {

								selectionPresent = true;
								break;
							}
						}
						if (!selectionPresent)
							selectedFeature.setPrinmary(true);
					}
					else {
						selectedFeature.setPrinmary(true);

						for (MsFeature cf : activeCluster.getFeatures()) {

							if (!cf.equals(selectedFeature))
								cf.setPrinmary(false);
						}
					}
				}
				if(this.getSelectedColumn() == dataColumn) {

					if (selected) {

						selectedFeature.setActive(true);

						for (MsFeature cf : activeCluster.getFeatures()) {

							if (!cf.equals(selectedFeature))
								cf.setActive(false);
						}
					}
				}
				model.setTableModelFromFeatures(activeCluster.getFeatures());
			}
		}
	}*/

	public int getFeatureRow(MsFeature feature) {

		int row = -1;
		int col = getColumnModel().getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN);

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
		model.removeTableModelListener(modelListener);
		setrendererReferences(activeCluster.getFeatures());
		model.setTableModelFromFeatureCluster(activeCluster);
		tca.adjustColumns();

		columnModel.getColumnById(DuplicateSelectionTableModel.POOLED_MEAN_COLUMN)
				.setPreferredWidth(preferredWidth);
		columnModel.getColumnById(DuplicateSelectionTableModel.SAMPLE_MEAN_COLUMN)
				.setPreferredWidth(preferredWidth);

		model.addTableModelListener(modelListener);
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int col = getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN);
		for (int i : getSelectedRows()) {
			MsFeature rowFeature = (MsFeature) getValueAt(i, col);
			selected.add(rowFeature);
		}
		return selected;
	}

	@Override
	public MsFeature getSelectedFeature() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MsFeature) model.getValueAt(row, 
				getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN));
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		int fcol = model.getColumnIndex(DuplicateSelectionTableModel.FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(DuplicateSelectionTableModel.DATA_PIPELINE_COLUMN);
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






















