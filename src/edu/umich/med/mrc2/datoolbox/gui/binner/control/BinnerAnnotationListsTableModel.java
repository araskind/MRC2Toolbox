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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class BinnerAnnotationListsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;

	public static final String NAME_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String DATE_CREATED_COLUMN = "Created on";
	public static final String LAST_MODIFIED_COLUMN = "Last modified";
	public static final String OWNER_COLUMN = "Owner";

	public BinnerAnnotationListsTableModel() {
		
		super();
		columnArray = new ColumnContext[] {

				new ColumnContext(NAME_COLUMN, NAME_COLUMN, BinnerAdductList.class, false),
				new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
				new ColumnContext(DATE_CREATED_COLUMN, DATE_CREATED_COLUMN, Date.class, false),	
				new ColumnContext(LAST_MODIFIED_COLUMN, LAST_MODIFIED_COLUMN, Date.class, false),
				new ColumnContext(OWNER_COLUMN, OWNER_COLUMN, LIMSUser.class, false),
			};		
	}

	public void setTableModelFromBinnerAdductListCollection(Collection<BinnerAdductList> collection) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();			
		for (BinnerAdductList adList : collection) {

			Object[] obj = {
					adList,
					adList.getDescription(),
					adList.getDateCreated(),
					adList.getLastModified(),
					adList.getOwner(),
			};
			rowData.add(obj);
		}	
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
