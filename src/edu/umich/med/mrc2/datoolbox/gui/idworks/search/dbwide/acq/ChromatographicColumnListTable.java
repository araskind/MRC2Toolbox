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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.acq;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.ChromatographicColumnComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ChromatographicColumnFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChromatographicColumnRenderer;

public class ChromatographicColumnListTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private ChromatographicColumnListTableModel model;

	public ChromatographicColumnListTable() {
		super();
		model = new ChromatographicColumnListTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<ChromatographicColumnListTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(ChromatographicColumnListTableModel.CHROM_COLUMN_COLUMN),
				new ChromatographicColumnComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setDefaultRenderer(LIMSChromatographicColumn.class, new ChromatographicColumnRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSChromatographicColumn.class,
				new ChromatographicColumnFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSChromatographicColumn.class,
				new ChromatographicColumnComparator(SortProperty.Name));

		finalizeLayout();
	}

	public void setTableModelFromColumns(Collection<LIMSChromatographicColumn>columns) {
		model.setTableModelFromColumns(columns);
		tca.adjustColumns();
	}
	
	public void selectColumns(Collection<LIMSChromatographicColumn>columns) {
		
		int col = model.getColumnIndex(ChromatographicColumnListTableModel.CHROM_COLUMN_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(columns.contains(model.getValueAt(convertRowIndexToModel(i), col)))
				addRowSelectionInterval(i, i);
		}			
	}

	public LIMSChromatographicColumn getSelectedChromatographicColumn() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSChromatographicColumn)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(ChromatographicColumnListTableModel.CHROM_COLUMN_COLUMN));
	}
	
	public Collection<LIMSChromatographicColumn> getSelectedChromatographicColumns() {

		Collection<LIMSChromatographicColumn> selected = new ArrayList<LIMSChromatographicColumn>();
		int col = model.getColumnIndex(ChromatographicColumnListTableModel.CHROM_COLUMN_COLUMN);
		for(int i : getSelectedRows())
			selected.add((LIMSChromatographicColumn)model.getValueAt(convertRowIndexToModel(i), col));

		return selected;
	}
}








