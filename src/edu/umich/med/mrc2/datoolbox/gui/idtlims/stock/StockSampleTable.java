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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.StockSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.format.StockSampleFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.StockSampleRenderer;

public class StockSampleTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2069272556941448636L;
	private StockSampleTableModel model;

	public StockSampleTable() {
		super();
		model = new StockSampleTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<StockSampleTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);
		setDefaultRenderer(StockSample.class, new StockSampleRenderer(SortProperty.ID));
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(StockSample.class, new StockSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(StockSample.class, new StockSampleComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void setTableModelFromSamples(Collection<StockSample>samples) {

		thf.setTable(null);
		model.setTableModelFromSamples(samples);
		thf.setTable(this);
		tca.adjustColumns();
	}

	public StockSample getSelectedSample(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (StockSample) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(StockSampleTableModel.SAMPLE_ID_COLUMN));
	}

	public void selectSample(StockSample sample) {

		int colIdx = model.getColumnIndex(StockSampleTableModel.SAMPLE_ID_COLUMN);
		if(colIdx == -1)
			return;
		
		for(int i=0; i<model.getRowCount(); i++) {

			if(getValueAt(i,colIdx).equals(sample)){
				
				int row = convertRowIndexToView(i);			
				setRowSelectionInterval(row, row);
				this.scrollToSelected();
				return;
			}
		}
	}
}
