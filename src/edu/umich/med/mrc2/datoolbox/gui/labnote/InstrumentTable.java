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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

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
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.InstrumentRenderer;

public class InstrumentTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3580581442927796511L;
	private InstrumentTableModel model;

	public InstrumentTable() {
		super();
		model = new InstrumentTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<InstrumentTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(InstrumentTableModel.INSTRUMENT_COLUMN),
				new InstrumentComparator(SortProperty.ID));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(LIMSInstrument.class, new InstrumentRenderer(SortProperty.ID));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSInstrument.class, new InstrumentFormat(SortProperty.ID));
		thf.getParserModel().setComparator(LIMSInstrument.class, new InstrumentComparator(SortProperty.ID));

		finalizeLayout();
	}

	public void setTableModelFromInstrumentCollection(Collection<LIMSInstrument>instruments) {
		model.setTableModelFromInstrumentCollection(instruments);
		tca.adjustColumns();
	}

	public LIMSInstrument getSelectedInstrument() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (LIMSInstrument)model.getValueAt(getSelectedRow(),
				model.getColumnIndex(InstrumentTableModel.INSTRUMENT_COLUMN));
	}
}















