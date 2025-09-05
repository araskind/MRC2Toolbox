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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.FeatureSelectionTable;

public class AlignedDataSetSummaryTable extends FeatureSelectionTable {

	private static final long serialVersionUID = 7042158268272205223L;

	public AlignedDataSetSummaryTable() {

		super();

		model = new AlignedDataSetSummaryTableModel();
		setModel(model);

		featureSorter = new TableRowSorter<AlignedDataSetSummaryTableModel>(
				(AlignedDataSetSummaryTableModel)model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(AlignedDataSetSummaryTableModel.REFERENCE_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.FEATURE_COLUMN)
//				.setCellRenderer(cfRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.SCORE_COLUMN)
//				.setCellRenderer(percentRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.RETENTION_COLUMN)
//				.setCellRenderer(rtRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.BASE_PEAK_COLUMN)
//				.setCellRenderer(mzRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.POOLED_MEAN_COLUMN)
//				.setCellRenderer(pooledMeanRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.POOLED_RSD_COLUMN)
//				.setCellRenderer(percentRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.POOLED_FREQUENCY_COLUMN)
//				.setCellRenderer(pieChartFrequencyRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.SAMPLE_MEAN_COLUMN)
//				.setCellRenderer(sampleMeanRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.SAMPLE_RSD_COLUMN)
//				.setCellRenderer(percentRenderer);
//		columnModel.getColumnById(AlignedDataSetFeatureTableModel.SAMPLE_FREQUENCY_COLUMN)
//				.setCellRenderer(pieChartFrequencyRenderer);

		finalizeLayout();
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		if(convertColumnIndexToModel(column) == 
				model.getColumnIndex(AlignedDataSetSummaryTableModel.ORDER_COLUMN))
			return row+1;
		else
			return super.getValueAt(row, column);
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<>();
//		int col = getColumnModel().getColumnIndex(AlignedDataSetFeatureTableModel.FEATURE_COLUMN);
//
//		for (int i : getSelectedRows()) {
//
//			MsFeature rowFeature = (MsFeature) model.getValueAt(convertRowIndexToModel(i), col);
//			selected.add(rowFeature);
//		}
		return selected;
	}

	@Override
	public MsFeature getSelectedFeature() {

		MsFeature selected = null;
//		int col = getColumnModel().getColumnIndex(AlignedDataSetFeatureTableModel.FEATURE_COLUMN);
//		int row = getSelectedRow();
//
//		if(row > -1)
//			selected = (MsFeature) model.getValueAt(convertRowIndexToModel(row), col);

		return selected;
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = new TreeMap<>();
//		int fcol = model.getColumnIndex(AlignedDataSetFeatureTableModel.FEATURE_COLUMN);
//		int dpcol = model.getColumnIndex(AlignedDataSetFeatureTableModel.DATA_PIPELINE_COLUMN);
//		for (int i : getSelectedRows()) {
//			
//			MsFeature rowFeature = (MsFeature) getValueAt(i, fcol);
//			DataPipeline dp = (DataPipeline) getValueAt(i, dpcol);
//			featureMap.computeIfAbsent(dp, v -> new TreeSet<>(new MsFeatureComparator(SortProperty.Name)));
//			featureMap.get(dp).add(rowFeature);
//		}
		return featureMap;
	}

	@Override
	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFeatureRow(MsFeature f) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setTableModelFromAlignmentResults(
			DataPipelineAlignmentResults alignmentResults, CompoundLibrary avgLib) {
		((AlignedDataSetSummaryTableModel)model).setTableModelFromAlignmentResults(alignmentResults, avgLib);
	}
}
