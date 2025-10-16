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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.apache.poi.ss.usermodel.Sheet;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.TableColumnAdjuster;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ReportDataTypeEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ReportDataTypeRenderer;

public class WorksheetPreviewTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5253835517775220524L;
	private WorksheetPreviewTableModel model;
	private ReportDataTypeEditor rdtEditor;
	private ReportDataTypeRenderer rdtRenderer;
	private DataDirection direction;

	public WorksheetPreviewTable() {

		super();
		setAutoCreateColumnsFromModel(false);
		setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		model = new WorksheetPreviewTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<WorksheetPreviewTableModel>(model);
		setRowSorter(rowSorter);
		createDefaultColumnsFromModel();
		
		rdtEditor = new ReportDataTypeEditor();
		setDefaultEditor(ReportDataType.class, rdtEditor);
		rdtRenderer = new ReportDataTypeRenderer();
		setDefaultRenderer(ReportDataType.class, rdtRenderer);

		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();
		getColumnModel().getColumn(0).setMinWidth(100);
		getColumnModel().getColumn(1).setMinWidth(50);
	}

	public void setTableModelFromWorksheet(Sheet sheet, DataDirection direction) {

		this.direction = direction;

		model.removeTableModelListener(this);
		model.clearModel();
		model.setTableModelFromWorksheet(sheet, direction);

		model.addTableModelListener(this);
		createDefaultColumnsFromModel();
		//	TODO  is it necessary?
		model.fireTableStructureChanged();
		rowSorter = new TableRowSorter<WorksheetPreviewTableModel>(model);
		setRowSorter(rowSorter);

		tca = new TableColumnAdjuster(this);
		tca.adjustColumns();

		getColumnModel().getColumn(0).setMinWidth(100);
		getColumnModel().getColumn(1).setMinWidth(50);
	}

	@Override
	public synchronized void clearTable() {

		model = new WorksheetPreviewTableModel();
		setModel(model);
		createDefaultColumnsFromModel();
		rowSorter = new TableRowSorter<WorksheetPreviewTableModel>(model);
		setRowSorter(rowSorter);
	}

	@Override
    public TableCellEditor getCellEditor(int row, int column) {

		if(row == 0 && column > 1)
			return rdtEditor;
		else if(row == 1 && column > 1)
			return getDefaultEditor(Boolean.class);
		else
			return super.getCellEditor(row, column);
    }

	@Override
    public TableCellRenderer getCellRenderer(int row, int column) {

		if(row == 0 && column > 1)
			return rdtRenderer;
		else if(row == 1 && column > 1)
			return  getDefaultRenderer(Boolean.class);
		else
			return super.getCellRenderer(row, column);
    }

	public String[] getReportDataByDataType(ReportDataType dataType){

		Collection<String>data = new ArrayList<String>();
		if(direction == null)
			return null;

		if(dataType.equals(ReportDataType.SAMPLE_ID)
			|| dataType.equals(ReportDataType.SAMPLE_NAME)
			|| dataType.equals(ReportDataType.DATA_FILE_NAME)) {

			if(direction.equals(DataDirection.SAMPLES_IN_COLUMNS)) {
				int dataRow = getRowForDataType(dataType);
				if(dataRow >=0) {

					Set<Integer>quantDataColumns = getQuantDataColumns(true);
					if(quantDataColumns.isEmpty())
						return null;

					for(int i : quantDataColumns)
						data.add((String)getValueAt(dataRow, i));
				}
			}
			if(direction.equals(DataDirection.SAMPLES_IN_ROWS)) {
				int dataColumn = getColumnForDataType(dataType);
				if(dataColumn >=0) {

					Set<Integer>quantDataRows = getQuantDataRows(true);
					if(quantDataRows.isEmpty())
						return null;

					for(int i : quantDataRows)
						data.add((String)getValueAt(i, dataColumn));
				}
			}
		}
		if(dataType.equals(ReportDataType.COMPOUND_ID)
			|| dataType.equals(ReportDataType.COMPOUND_NAME)) {

			if(direction.equals(DataDirection.SAMPLES_IN_COLUMNS)) {

				int dataColumn = getColumnForDataType(dataType);
				if(dataColumn >=0) {

					Set<Integer>quantDataRows = getQuantDataRows(true);
					if(quantDataRows.isEmpty())
						return null;

					for(int i : quantDataRows)
						data.add((String)getValueAt(i, dataColumn));
				}
			}
			if(direction.equals(DataDirection.SAMPLES_IN_ROWS)) {

				int dataRow = getRowForDataType(dataType);
				if(dataRow >=0) {

					Set<Integer>quantDataColumns = getQuantDataColumns(true);
					if(quantDataColumns.isEmpty())
						return null;

					for(int i : quantDataColumns)
						data.add((String)getValueAt(dataRow, i));
				}
			}
		}
		if(data.isEmpty())
			return null;
		else
			return data.toArray(new String[data.size()]);
	}

	public int getRowForDataType(ReportDataType dataType) {

		int rdt = -1;
		for(int i=2; i<getRowCount(); i++) {
			if(getValueAt(i, 0).equals(dataType))
				return i;
		}
		return rdt;
	}

	public int getColumnForDataType(ReportDataType dataType) {

		int cdt = -1;
		for(int i=2; i<getColumnCount(); i++) {
			if(getValueAt(0, i).equals(dataType))
				return i;
		}
		return cdt;
	}

	public Set<Integer>getQuantDataColumns(boolean enabledOnly){

		Set<Integer>quantDataColumns = new TreeSet<Integer>();
		for(int i=2; i<getColumnCount(); i++) {
			if(getValueAt(0, i).equals(ReportDataType.QUANT_DATA)
					&& getValueAt(1, i).equals(Boolean.TRUE))
				quantDataColumns.add(i);
		}
		return quantDataColumns;
	}

	public Set<Integer>getQuantDataRows(boolean enabledOnly){

		Set<Integer>quantDataRows = new TreeSet<Integer>();
		for(int i=2; i<getRowCount(); i++) {

			if(getValueAt(i, 0).equals(ReportDataType.QUANT_DATA)
					&& getValueAt(i, 1).equals(Boolean.TRUE))
				quantDataRows.add(i);
		}
		return quantDataRows;
	}
}



























