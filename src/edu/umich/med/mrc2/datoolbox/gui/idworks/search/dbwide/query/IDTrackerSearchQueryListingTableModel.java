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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.IDTSearchQuery;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class IDTrackerSearchQueryListingTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3261097067591853752L;
	public static final String QUERY_COLUMN = "Description";
	public static final String USER_COLUMN = "Created by";
	public static final String DATE_COLUMN = "Created on";

	public IDTrackerSearchQueryListingTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(QUERY_COLUMN, QUERY_COLUMN, IDTSearchQuery.class, false),
			new ColumnContext(USER_COLUMN, USER_COLUMN, LIMSUser.class, false),
			new ColumnContext(DATE_COLUMN, DATE_COLUMN, Date.class, false),
		};
	}
	
	public void setTableModelFromQueryList(Collection<IDTSearchQuery>queryList) {

		setRowCount(0);	
		if(queryList == null || queryList.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(IDTSearchQuery query : queryList) {
			
			Object[] obj = {
					query,
					query.getAuthor(),
					query.getCreatedOn(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
