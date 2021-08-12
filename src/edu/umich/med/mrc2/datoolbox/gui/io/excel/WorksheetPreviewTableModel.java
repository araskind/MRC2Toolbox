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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;

public class WorksheetPreviewTableModel extends DefaultTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4507846646468014729L;


	public static final String DATA_TYPE_COLUMN = "Data type";
	public static final String ENABLED_COLUMN = "Enabled";

	public WorksheetPreviewTableModel() {
		super();
		addColumn(DATA_TYPE_COLUMN);
		addColumn(ENABLED_COLUMN);
	}

	public void clearModel() {

		setRowCount(0);
		for (int i = columnIdentifiers.size() - 1; i > 1; i--)
			columnIdentifiers.removeElementAt(i);
	}

	@Override
	public Class getColumnClass(int col) {

		if (col == 0)
			return ReportDataType.class;
		else if (col == 1)
			return Boolean.class;
		else
			return String.class; // Level columns
	}

	@Override
	public boolean isCellEditable(int row, int column) {

		if ((column < 2 && row > 1) || (column > 1 && row < 2))
			return true;
		else
			return false;
	}

	public void setTableModelFromWorksheet(Sheet sheet, DataDirection direction) {

	    if(direction.equals(DataDirection.SAMPLES_IN_ROWS))
	    	populateModelWithSourceSamplesInRows(sheet);

	    if(direction.equals(DataDirection.SAMPLES_IN_COLUMNS))
	    	populateModelWithSourceSamplesInColumns(sheet);
	}

	private void populateModelWithSourceSamplesInRows(Sheet sheet) {

	    int rowStart = sheet.getFirstRowNum();
	    int rowEnd = sheet.getLastRowNum();
	    int colEnd = getMaxColumnNumber(sheet);

	    setRowCount(rowEnd - rowStart + 3);

		for (int i = 2; i <= colEnd+1; i++) {
			addColumn("Column " + Integer.toString(i));
			setValueAt(ReportDataType.QUANT_DATA, 0, i);
			setValueAt(Boolean.TRUE, 1, i);
		}
		for (int i = 2; i < getRowCount(); i++) {
			setValueAt(ReportDataType.QUANT_DATA, i, 0);
			setValueAt(Boolean.TRUE, i, 1);
		}
		int tableRowCount = 2;
		for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {

			Row r = sheet.getRow(rowNum);

			if (r != null) {

				int lastColumn = Math.max(r.getLastCellNum(), colEnd);

				for (int cn = 0; cn < lastColumn; cn++) {
					Cell c = r.getCell(cn, MissingCellPolicy.RETURN_BLANK_AS_NULL);
					if (c != null) {

						if(c.getCellType().equals(CellType.NUMERIC) || c.getCellType().equals(CellType.FORMULA))
							setValueAt(c.getNumericCellValue(), tableRowCount, cn+2);

						if(c.getCellType().equals(CellType.STRING))
							setValueAt(c.getStringCellValue().trim(), tableRowCount, cn+2);
					}
				}
				tableRowCount++;
			}
		}
	}

	private void populateModelWithSourceSamplesInColumns(Sheet sheet) {

	    int rowStart = sheet.getFirstRowNum();
	    int rowEnd = sheet.getLastRowNum();
	    int colEnd = getMaxColumnNumber(sheet);

    	setRowCount(colEnd + 2);

		for (int i = 2; i <= (rowEnd - rowStart + 2); i++) {

			addColumn("Column " + Integer.toString(i));
			setValueAt(ReportDataType.QUANT_DATA, 0, i);
			setValueAt(Boolean.TRUE, 1, i);
		}
		for (int i = 2; i < getRowCount(); i++) {
			setValueAt(ReportDataType.QUANT_DATA, i, 0);
			setValueAt(Boolean.TRUE, i, 1);
		}
		int tableColCount = 2;
		for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {

			Row r = sheet.getRow(rowNum);

			if (r != null) {

				int lastColumn = Math.max(r.getLastCellNum(), colEnd);

				for (int cn = 0; cn < lastColumn; cn++) {

					Cell c = r.getCell(cn, MissingCellPolicy.RETURN_BLANK_AS_NULL);
					if (c != null) {

						if(c.getCellType().equals(CellType.NUMERIC) || c.getCellType().equals(CellType.FORMULA))
							setValueAt(c.getNumericCellValue(), cn+2, tableColCount);

						if(c.getCellType().equals(CellType.STRING))
							setValueAt(c.getStringCellValue().trim(), cn+2, tableColCount);
					}
				}
				tableColCount++;
			}
		}
	}

	private int getMaxColumnNumber(Sheet sheet) {

	    int rowStart = sheet.getFirstRowNum();
	    int rowEnd = sheet.getLastRowNum();
	    int maxCols = 0;

	    for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {

	    	Row r = sheet.getRow(rowNum);
	    	if(r != null) {

	    		int rowCols = r.getLastCellNum();
	    		if(rowCols > maxCols)
	    			maxCols = rowCols;
	    	}
	    }
	    return maxCols;
	}
}
