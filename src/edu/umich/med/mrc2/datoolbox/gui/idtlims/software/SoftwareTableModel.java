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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.software;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SoftwareTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -1135263422810449762L;
	public static final String SOFTWARE_COLUMN = "Name";
	public static final String SOFTWARE_TYPE_COLUMN = "Type";
	public static final String SOFTWARE_DESCRIPTION_COLUMN = "Description";
	public static final String VENDOR_COLUMN = "Vendor";

	public SoftwareTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SOFTWARE_COLUMN, DataProcessingSoftware.class, false),
			new ColumnContext(SOFTWARE_TYPE_COLUMN, String.class, false),
			new ColumnContext(SOFTWARE_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(VENDOR_COLUMN, Manufacturer.class, false),
		};
	}

	public void setTableModelFromSoftwareList(Collection<DataProcessingSoftware>softwareItems) {

		setRowCount(0);
		if(softwareItems.isEmpty())
			return;

		for (DataProcessingSoftware item : softwareItems) {

			Object[] obj = {
				item,
				item.getSoftwareType().getName(),
				item.getDescription(),
				item.getVendor(),			
			};
			super.addRow(obj);
		}
	}
}
