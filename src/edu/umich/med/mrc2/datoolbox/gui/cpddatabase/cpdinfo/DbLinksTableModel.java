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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DbLinksTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 4216625502632331217L;

	public static final String DATABASE_COLUMN = "Database";
	public static final String LINK_COLUMN = "ID";


	public DbLinksTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(DATABASE_COLUMN, DATABASE_COLUMN, String.class, false),
			new ColumnContext(LINK_COLUMN, "Accession and web link to the source",CompoundIdentity.class, false)
		};
	}

	public void setModelFromLinks(List<CompoundIdentity> dbLinks) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (CompoundIdentity id : dbLinks) {

			if(id.getPrimaryDatabase() == null)
				continue;

			Object[] obj = {
				id.getPrimaryDatabase().getName(),
				id
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}




























