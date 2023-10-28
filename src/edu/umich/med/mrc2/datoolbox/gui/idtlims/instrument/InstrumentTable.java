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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.InstrumentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.InstrumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;

public class InstrumentTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1405921543482090501L;

	public InstrumentTable() {
		super();
		model =  new InstrumentTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<InstrumentTableModel>((InstrumentTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(InstrumentTableModel.INSTRUMENT_COLUMN),
				new InstrumentComparator(SortProperty.Name));
	
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columnModel.getColumnById(InstrumentTableModel.INSTRUMENT_DESCRIPTION_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(LIMSInstrument.class, new InstrumentComparator(SortProperty.Name));
		thf.getParserModel().setFormat(LIMSInstrument.class, new InstrumentFormat(SortProperty.Name));
		finalizeLayout();
	}

	public void setTableModelFromInstrumentList(Collection<LIMSInstrument> instruments) {
		
		thf.setTable(null);
		((InstrumentTableModel)model).setTableModelFromInstrumentList(instruments);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSInstrument getSelectedInstrument() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSInstrument) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(InstrumentTableModel.INSTRUMENT_COLUMN));
	}
}
















