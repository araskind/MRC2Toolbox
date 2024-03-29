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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MSMSLibraryListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String ID_COLUMN = "ID";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String DECOY_COLUMN = "Decoy";

	public MSMSLibraryListingTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, "Library ID", ReferenceMsMsLibrary.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(DECOY_COLUMN, "Is it the decoy library", Boolean.class, false),
		};
	}

	public void setTableModelFromReferenceMsMsLibraryList(Collection<ReferenceMsMsLibrary>libList) {

		setRowCount(0);
		if(libList == null || libList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (ReferenceMsMsLibrary lib : libList) {

			Object[] obj = {
				lib,
				lib.getDescription(),
				lib.isDecoy(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














