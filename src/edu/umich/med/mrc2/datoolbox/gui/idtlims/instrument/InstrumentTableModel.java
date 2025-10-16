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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class InstrumentTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String INSTRUMENT_ID_COLUMN = "Instrument ID";
	public static final String INSTRUMENT_COLUMN = "Name";
	public static final String INSTRUMENT_DESCRIPTION_COLUMN = "Description";
	public static final String MASS_ANALYZER_COLUMN = "Mass analyzer";
	public static final String SEPARATION_TYPE_COLUMN = "Separation type";
	public static final String MANUFACTURER_COLUMN = "Manufacturer";
	public static final String SERIAL_NUMBER_COLUMN = "Serial #";

	public InstrumentTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(INSTRUMENT_ID_COLUMN, INSTRUMENT_ID_COLUMN, String.class, false),
			new ColumnContext(INSTRUMENT_COLUMN, "Instrument model name", LIMSInstrument.class, false),
			new ColumnContext(INSTRUMENT_DESCRIPTION_COLUMN, INSTRUMENT_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(MASS_ANALYZER_COLUMN, "Mass analyzer type", String.class, false),
			new ColumnContext(SEPARATION_TYPE_COLUMN, "Chromatographic separation type", String.class, false),
			new ColumnContext(MANUFACTURER_COLUMN, MANUFACTURER_COLUMN, String.class, false),
			new ColumnContext(SERIAL_NUMBER_COLUMN, "Instrument serial number", String.class, false),
		};
	}

	public void setTableModelFromInstrumentList(Collection<LIMSInstrument> instruments) {

		setRowCount(0);

		if(instruments == null || instruments.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (LIMSInstrument instrument : instruments) {

			Object[] obj = {
					instrument.getInstrumentId(),
					instrument,
					instrument.getDescription(),
					instrument.getMassAnalyzerType().getDescription(),
					instrument.getChromatographicSeparationType().getDescription(),
					instrument.getManufacturer(),instrument.getSerialNumber(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














