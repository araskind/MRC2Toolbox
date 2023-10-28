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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.scan;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.compare.IScanComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.IScanFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IScanRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.scan.IScan;

public class ScanTable extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2087167895707677516L;
	public ScanTable() {

		super();

		model = new ScanTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		rowSorter = new TableRowSorter<ScanTableModel>((ScanTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ScanTableModel.SCAN_COLUMN),
				new IScanComparator(SortProperty.RT));

		setDefaultRenderer(IScan.class, new IScanRenderer(SortProperty.scanNumber));
		columnModel.getColumnById(ScanTableModel.PRECURSOR_MZ_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getMzFormat(), true));		
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(IScan.class, 
				new IScanComparator(SortProperty.scanNumber));
		thf.getParserModel().setFormat(IScan.class, 
				new IScanFormat(SortProperty.scanNumber));
		
		finalizeLayout();
	}

	public void setModelFromDataFile(DataFile dataFile) {

		thf.setTable(null);
		((ScanTableModel)model).setModelFromDataFile(dataFile);
		thf.setTable(this);
		adjustColumns();
	}
	
	public IScan getSelectedScan(){

		if(getSelectedRow() == -1)
			return null;
				
		return (IScan)model.getValueAt(
				convertRowIndexToModel(getSelectedRow()),
				model.getColumnIndex(ScanTableModel.SCAN_COLUMN));
	}
}

























