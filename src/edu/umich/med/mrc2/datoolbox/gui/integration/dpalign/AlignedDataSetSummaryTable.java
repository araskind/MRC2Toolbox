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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.FeatureSelectionTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentityWordWrapCellRenderer;

public class AlignedDataSetSummaryTable extends FeatureSelectionTable {

	private static final long serialVersionUID = 7042158268272205223L;

	public AlignedDataSetSummaryTable() {

		super();

		model = new AlignedDataSetSummaryTableModel();
		setModel(model);

		featureSorter = new TableRowSorter<AlignedDataSetSummaryTableModel>(
				(AlignedDataSetSummaryTableModel)model);
		setRowSorter(featureSorter);
		featureSorter.setComparator(model.getColumnIndex(
				AlignedDataSetSummaryTableModel.REFERENCE_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(AlignedDataSetSummaryTableModel.FOUND_COLUMN)
				.setCellRenderer(radioRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.REFERENCE_FEATURE_COLUMN)
				.setCellRenderer(cfRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.COMPOUND_NAME_COLUMN)
				.setCellRenderer(new IdentityWordWrapCellRenderer(CompoundIdentityField.NAME));
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.TOP_MATCH_COLUMN)
				.setCellRenderer(cfRenderer);		
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.MERGED_COLUMN)
				.setCellRenderer(radioRenderer);		
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.CORRELATION_COLUMN)
				.setCellRenderer(new FormattedDecimalRenderer(new DecimalFormat("##.###"), true));
		
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.POOLED_RSD_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.SAMPLE_RSD_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.POOLED_AREA_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.SAMPLE_AREA_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.POOLED_MISSING_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.SAMPLE_MISSING_DIFFERENCE_COLUMN)
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.BASE_PEAK_COLUMN)
				.setCellRenderer(mzRenderer);
		
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.ORDER_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.FOUND_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(AlignedDataSetSummaryTableModel.MERGED_COLUMN).setMaxWidth(50);
		fixedWidthColumns.add(model.getColumnIndex(AlignedDataSetSummaryTableModel.ORDER_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(AlignedDataSetSummaryTableModel.FOUND_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(AlignedDataSetSummaryTableModel.MERGED_COLUMN));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MsFeature.class, new MsFeatureFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MsFeature.class, new MsFeatureComparator(SortProperty.Name));
		
		thf.getParserModel().setFormat(CompoundIdentity.class, 
				new CompoundIdentityFormat(CompoundIdentityField.NAME));
		thf.getParserModel().setComparator(CompoundIdentity.class, 
				new CompoundIdentityComparator(SortProperty.Name));

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
	
	public void setTableModelFromAlignmentResults(
			DataPipelineAlignmentResults alignmentResults, CompoundLibrary avgLib) {
		thf.setTable(null);
		((AlignedDataSetSummaryTableModel)model).setTableModelFromAlignmentResults(alignmentResults, avgLib);
		thf.setTable(this);
		adjustColumns();
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<>();
//		int col = getColumnModel().getColumnIndex(AlignedDataSetSummaryTableModel.FEATURE_COLUMN);
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
//		int col = getColumnModel().getColumnIndex(AlignedDataSetSummaryTableModel.FEATURE_COLUMN);
//		int row = getSelectedRow();
//
//		if(row > -1)
//			selected = (MsFeature) model.getValueAt(convertRowIndexToModel(row), col);

		return selected;
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {

		Map<DataPipeline, Collection<MsFeature>>featureMap = new TreeMap<>();
//		int fcol = model.getColumnIndex(AlignedDataSetSummaryTableModel.FEATURE_COLUMN);
//		int dpcol = model.getColumnIndex(AlignedDataSetSummaryTableModel.DATA_PIPELINE_COLUMN);
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
}
