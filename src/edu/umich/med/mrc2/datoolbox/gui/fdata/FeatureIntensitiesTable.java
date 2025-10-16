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

package edu.umich.med.mrc2.datoolbox.gui.fdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DecimalAlignRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IntensityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;

public class FeatureIntensitiesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1435205828153798672L;
	private DataFileCellRenderer fileRenderer;
	private DecimalAlignRenderer lefttRenderer;

	public FeatureIntensitiesTable() {

		super();

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		model = new FeatureIntensitiesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FeatureIntensitiesTableModel>(
				(FeatureIntensitiesTableModel)model);
		setRowSorter(rowSorter);
		
		rowSorter.setComparator(model.getColumnIndex(FeatureIntensitiesTableModel.MS_FEATURE_COLUMN),
				new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		fileRenderer = new DataFileCellRenderer();
		setDefaultRenderer(DataFile.class, fileRenderer);
		cfRenderer = new MsFeatureRenderer(SortProperty.Name);
		setDefaultRenderer(MsFeature.class, cfRenderer);

		intensityRenderer = new IntensityRenderer();
		lefttRenderer = new DecimalAlignRenderer();
		columnModel.getColumnById(FeatureIntensitiesTableModel.AREA_COLUMN)
			.setCellRenderer(lefttRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFeatureAndPipeline(
				MsFeature feature, DataPipeline dataPipeline) {

		thf.setTable(null);
		((FeatureIntensitiesTableModel)model).setTableModelFromFeatureAndPipeline(feature, dataPipeline);
		thf.setTable(this);
		sortByFeatureAndSample();		
		adjustColumns();
	}

	public void setTableModelFromFeatureMap(
			Map<DataPipeline, Collection<MsFeature>> selectedFeaturesMap) {
		thf.setTable(null);
		((FeatureIntensitiesTableModel)model).setTableModelFromFeatureMap(selectedFeaturesMap);
		thf.setTable(this);
		adjustColumns();
	}

	public void sortByFeatureAndSample() {

		ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
		int featureColumn = model.getColumnIndex(FeatureIntensitiesTableModel.MS_FEATURE_COLUMN);
		int sampleColumn = model.getColumnIndex(FeatureIntensitiesTableModel.SAMPLE_COLUMN);

		if(featureColumn > -1)
			sortKeys.add(new RowSorter.SortKey(featureColumn, SortOrder.ASCENDING));

		if(sampleColumn > -1)
			sortKeys.add(new RowSorter.SortKey(sampleColumn, SortOrder.ASCENDING));

		if(!sortKeys.isEmpty()) {
			rowSorter.setSortKeys(sortKeys);
			rowSorter.sort();
		}
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		if(convertColumnIndexToModel(column) == 
				model.getColumnIndex(FeatureIntensitiesTableModel.ORDER_COLUMN))
			return row+1;
		else
			return super.getValueAt(row, column);
	}
}
