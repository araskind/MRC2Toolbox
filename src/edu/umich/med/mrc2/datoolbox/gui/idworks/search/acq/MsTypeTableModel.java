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

import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MsTypeTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String ID_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";

	public MsTypeTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, MsType.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromMsTypeList(Collection<MsType>typeList) {

		setRowCount(0);
		if(typeList.isEmpty())
			return;

		for (MsType type : typeList) {

			Object[] obj = {
				type,
				type.getDescription(),
			};
			super.addRow(obj);
		}
	}
}














