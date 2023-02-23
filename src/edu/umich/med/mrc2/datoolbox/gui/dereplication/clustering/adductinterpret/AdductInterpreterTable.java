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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ChemicalModificationEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RowEditorModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureSuggestedModificationRenderer;

public class AdductInterpreterTable extends BasicFeatureTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6783435341041065498L;
	private AdductInterpreterTableModel model;
	private TableRowSorter<AdductInterpreterTableModel> featureSorter;
	private FormattedDecimalRenderer ppmRenderer;
	private RowEditorModel rowEditorModel;
	private MsFeatureSuggestedModificationRenderer suggestedModificationRenderer;

	public AdductInterpreterTable() {

		super();
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model = new AdductInterpreterTableModel();
		setModel(model);

		featureSorter = new TableRowSorter<AdductInterpreterTableModel>();
		setRowSorter(featureSorter);
		featureSorter.setModel(model);
		getTableHeader().setReorderingAllowed(false);

		featureSorter.setComparator(model.getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));
		featureSorter.setSortable(model.getColumnIndex(AdductInterpreterTableModel.SUGGESTED_CHEM_MOD_COLUMN),false);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		chmodRenderer = new AdductRenderer();
		ppmRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.#"), true);
		suggestedModificationRenderer = new MsFeatureSuggestedModificationRenderer();

		columnModel.getColumnById(AdductInterpreterTableModel.FEATURE_COLUMN)
			.setCellRenderer(cfRenderer); // Feature name
		columnModel.getColumnById(AdductInterpreterTableModel.RETENTION_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time
		columnModel.getColumnById(AdductInterpreterTableModel.BASE_PEAK_COLUMN)
			.setCellRenderer(mzRenderer); // base peakMZ
		columnModel.getColumnById(AdductInterpreterTableModel.KMD_COLUMN)
			.setCellRenderer(mzRenderer); // Kendick mass diff
		columnModel.getColumnById(AdductInterpreterTableModel.CHEM_MOD_COLUMN)
			.setCellRenderer(chmodRenderer); // Compound form column
		columnModel.getColumnById(AdductInterpreterTableModel.SUGGESTED_CHEM_MOD_COLUMN)
			.setCellRenderer(suggestedModificationRenderer); // Compound form column
		columnModel.getColumnById(AdductInterpreterTableModel.CHEM_MOD_ERROR)
			.setCellRenderer(ppmRenderer); // Compound form column
		columnModel.getColumnById(AdductInterpreterTableModel.SUGGESTED_CHEM_MOD_ERROR)
			.setCellRenderer(ppmRenderer); // Compound form column
		columnModel.getColumnById(AdductInterpreterTableModel.POOLED_MEAN_COLUMN)
			.setCellRenderer(areaRenderer); // Pooled mean
		columnModel.getColumnById(AdductInterpreterTableModel.SAMPLE_MEAN_COLUMN)
			.setCellRenderer(areaRenderer); // Sample mean

		rowEditorModel = new RowEditorModel();

		addColumnSelectorPopup();

		thf = new TableFilterHeader(this, AutoChoices.DISABLED);
		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
	}

	@Override
	public TableCellEditor getCellEditor(int row, int col) {

		if(col == this.getColumnIndex(AdductInterpreterTableModel.SUGGESTED_CHEM_MOD_COLUMN))
			return rowEditorModel.getEditor(row);
		else
			return super.getCellEditor(row,col);
	}

	public int getFeatureRow(MsFeature feature) {

		int row = -1;
		int col = model.getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN);

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

		thf.setTable(null);
		rowEditorModel = new RowEditorModel();
		for(int i=0; i<featureCluster.getFeatures().size(); i++)
			rowEditorModel.addEditorForRow(i, new ChemicalModificationEditor());

		model.setTableModelFromFeatureCluster(featureCluster);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public void setTableModelFromFeatureCluster(MsFeatureCluster featureCluster, MsFeature referenceFeature,
			Adduct referenceModification) {

		thf.setTable(null);

		rowEditorModel = new RowEditorModel();
		for(int i=0; i<featureCluster.getFeatures().size(); i++)
			rowEditorModel.addEditorForRow(i, new ChemicalModificationEditor());

		model.setTableModelFromFeatureCluster(featureCluster, referenceFeature, referenceModification);
		thf.setTable(this);
		tca.adjustColumns();
	}

	@Override
	public Collection<MsFeature> getSelectedFeatures() {

		ArrayList<MsFeature> selected = new ArrayList<MsFeature>();
		int col = model.getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN);
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

		return (MsFeature)getValueAt(row, 
				getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN));
	}

	@Override
	public Map<DataPipeline, Collection<MsFeature>> getSelectedFeaturesMap() {
		
		Map<DataPipeline, Collection<MsFeature>>featureMap = 
				new TreeMap<DataPipeline, Collection<MsFeature>>();
		int fcol = model.getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN);
		int dpcol = model.getColumnIndex(AdductInterpreterTableModel.DATA_PIPELINE_COLUMN);
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























