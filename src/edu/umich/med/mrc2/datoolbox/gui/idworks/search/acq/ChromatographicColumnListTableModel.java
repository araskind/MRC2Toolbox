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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.acq;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

@SuppressWarnings("unused")
public class ChromatographicColumnListTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String CHROM_COLUMN_COLUMN = "Name";
	public static final String CHEMISTRY_COLUMN = "Chemistry";
	public static final String MANUFACTURER_COLUMN = "Manufacturer";

	public ChromatographicColumnListTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(CHROM_COLUMN_COLUMN, LIMSChromatographicColumn.class, false),
			new ColumnContext(CHEMISTRY_COLUMN, String.class, false),
			new ColumnContext(MANUFACTURER_COLUMN, Manufacturer.class, false),
		};
	}

	public void setTableModelFromColumns(Collection<LIMSChromatographicColumn>columns) {

		setRowCount(0);

		for (LIMSChromatographicColumn column : columns) {

			Object[] obj = {
				column,
				column.getChemistry(),
				column.getManufacturer(),
			};
			super.addRow(obj);
		}
	}
}
