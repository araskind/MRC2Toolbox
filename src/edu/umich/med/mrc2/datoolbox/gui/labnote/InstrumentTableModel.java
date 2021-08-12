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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class InstrumentTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4852795858705685025L;
	public static final String INSTRUMENT_COLUMN = "Instrument ID";
	public static final String INSTRUMENT_NAME_COLUMN = "Instrument name";
	public static final String INSTRUMENT_DESCRIPTION_COLUMN = "Description";
	public static final String INSTRUMENT_TYPE_COLUMN = "Instrument type";
	public static final String MANUFACTURER_COLUMN = "Manufacturer";
	public static final String MODEL_COLUMN = "Model";
	public static final String SERIAL_NUMBER_COLUMN = "Serial number";

	public InstrumentTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(INSTRUMENT_COLUMN, LIMSInstrument.class, false),
			new ColumnContext(INSTRUMENT_NAME_COLUMN, String.class, false),
			new ColumnContext(INSTRUMENT_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(INSTRUMENT_TYPE_COLUMN, String.class, false),
			new ColumnContext(MANUFACTURER_COLUMN, String.class, false),
			new ColumnContext(MODEL_COLUMN, String.class, false),
			new ColumnContext(SERIAL_NUMBER_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromInstrumentCollection(Collection<LIMSInstrument>instruments) {

		setRowCount(0);

		if(instruments.isEmpty())
			return;

		for (LIMSInstrument instrument : instruments) {

			Object[] obj = {
					instrument,
					instrument.getInstrumentName(),
					instrument.getDescription(),
					instrument.getMassAnalyzerType(),
					instrument.getManufacturer(),
					instrument.getModel(),
					instrument.getSerialNumber(),
			};
			super.addRow(obj);
		}
	}
}
