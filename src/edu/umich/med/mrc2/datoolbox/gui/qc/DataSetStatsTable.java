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

package edu.umich.med.mrc2.datoolbox.gui.qc;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataFileCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PercentValueRenderer;

public class DataSetStatsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8420590862541088371L;
	private DataFileCellRenderer dfRenderer;
	
	public DataSetStatsTable() {

		super();

		model = new DataSetStatsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataSetStatsTableModel>((DataSetStatsTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// Data file
		dfRenderer = new DataFileCellRenderer();
		dtRenderer = new DateTimeCellRenderer();
		percentRenderer = new PercentValueRenderer();

		columnModel.getColumnById(DataSetStatsTableModel.DATA_FILE_COLUMN)
			.setCellRenderer(dfRenderer);
		columnModel.getColumnById(DataSetStatsTableModel.INJECTION_TIME_COLUMN)
			.setCellRenderer(dtRenderer);
		columnModel.getColumnById(DataSetQcField.MIN.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.MAX.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.MEAN.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.MEAN_TRIM.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.MEDIAN.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.SD.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.SD_TRIM.getName())
			.setCellRenderer(areaRenderer);
		columnModel.getColumnById(DataSetQcField.RSD.getName())
				.setCellRenderer(percentRenderer);
		columnModel.getColumnById(DataSetQcField.RSD_TRIM.getName())
				.setCellRenderer(percentRenderer);

		addTablePopupMenu(new DataSetStatsTablePopupMenu(this, this));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromStatsList(Collection<DataFileStatisticalSummary> statsList) {
		thf.setTable(null);
		((DataSetStatsTableModel)model).setTableModelFromDataSetStats(statsList);
		thf.setTable(this);
		adjustColumns();
	}
}
