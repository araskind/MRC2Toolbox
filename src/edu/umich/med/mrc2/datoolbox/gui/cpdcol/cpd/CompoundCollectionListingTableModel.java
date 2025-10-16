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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.cpd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundCollectionListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707538261469467980L;

	public static final String ID_COLUMN = "ID";
	public static final String COLLECTION_NAME_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String URL_COLUMN = "URL";

	public CompoundCollectionListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, "Compound collection ID", String.class, false),
			new ColumnContext(COLLECTION_NAME_COLUMN, COLLECTION_NAME_COLUMN, CompoundCollection.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(URL_COLUMN, "Web address (manufacturer)", String.class, false)
		};
	}

	public void setTableModelFromCompoundCollections(
			Collection<CompoundCollection> compoundCollections) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();

		for(CompoundCollection coll : compoundCollections){

			Object[] obj = {
					coll.getId(),
					coll,
					coll.getDescription(),
					coll.getUrl(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}



































