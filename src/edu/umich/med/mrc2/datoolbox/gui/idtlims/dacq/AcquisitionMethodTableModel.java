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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class AcquisitionMethodTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String ACQ_METHOD_ID_COLUMN = "Method ID";
	public static final String ACQ_METHOD_COLUMN = "Method name";
	public static final String ACQ_METHOD_DESCRIPTION_COLUMN = "Description";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String IONIZATION_TYPE_COLUMN = "Ionization";
	public static final String MS_TYPE_COLUMN = "MS type";
	public static final String COLUMN_NAME_COLUMN = "Column";
	public static final String USER_COLUMN = "Created by";
	public static final String DATE_CREATED_COLUMN = "Created on";
	public static final String SOFTWARE_COLUMN = "Software";

	public AcquisitionMethodTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ACQ_METHOD_ID_COLUMN, ACQ_METHOD_ID_COLUMN, String.class, false),
			new ColumnContext(ACQ_METHOD_COLUMN, ACQ_METHOD_COLUMN, DataAcquisitionMethod.class, false),
			new ColumnContext(ACQ_METHOD_DESCRIPTION_COLUMN, ACQ_METHOD_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(POLARITY_COLUMN, POLARITY_COLUMN, String.class, false),
			new ColumnContext(IONIZATION_TYPE_COLUMN, "Ionization method", String.class, false),
			new ColumnContext(MS_TYPE_COLUMN, MS_TYPE_COLUMN, String.class, false),
			new ColumnContext(COLUMN_NAME_COLUMN, "Chromatographic column", LIMSChromatographicColumn.class, false),
			new ColumnContext(USER_COLUMN, USER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, DATE_CREATED_COLUMN, Date.class, false),
			new ColumnContext(SOFTWARE_COLUMN, "Instrument control software", DataProcessingSoftware.class, false),
		};
	}

	public void setTableModelFromMethods(Collection<DataAcquisitionMethod>methods) {

		setRowCount(0);
		if(methods == null || methods.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (DataAcquisitionMethod method : methods) {

			LIMSChromatographicColumn columnName = null;
			if(method.getColumn() != null)
				columnName = method.getColumn();
			
			String polName = null;
			if(method.getPolarity() != null)
				polName = method.getPolarity().name();

			Object[] obj = {
					method.getId(),
					method,
					method.getDescription(),
					polName,
					method.getIonizationType(),
					method.getMsType(),
					columnName,
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














