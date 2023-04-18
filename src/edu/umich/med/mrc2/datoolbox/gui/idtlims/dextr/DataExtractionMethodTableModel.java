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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataExtractionMethodTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 38251890009730840L;

	public static final String DEX_METHOD_ID_COLUMN = "Method ID";
	public static final String DEX_METHOD_COLUMN = "Method name";
	public static final String DEX_METHOD_DESCRIPTION_COLUMN = "Description";
	public static final String USER_COLUMN = "Created by";
	public static final String DATE_CREATED_COLUMN = "Created on";
	public static final String SOFTWARE_COLUMN = "Software";

	public DataExtractionMethodTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DEX_METHOD_ID_COLUMN, String.class, false),
			new ColumnContext(DEX_METHOD_COLUMN, DataExtractionMethod.class, false),
			new ColumnContext(DEX_METHOD_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(USER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, false),
			new ColumnContext(SOFTWARE_COLUMN, DataProcessingSoftware.class, false),
		};
	}

	public void setTableModelFromMethods(Collection<DataExtractionMethod>methods) {

		setRowCount(0);

		if(methods == null || methods.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (DataExtractionMethod method : methods) {

			Object[] obj = {
					method.getId(),
					method,
					method.getDescription(),
					method.getCreatedBy(),
					method.getCreatedOn(),
					method.getSoftware(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
