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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.compare.LIMSUserComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class UserTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -7757896216107421231L;
	public static final String USER_COLUMN = "User";
	public static final String CONTACT_COLUMN = "Contact info";

	public UserTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(USER_COLUMN, LIMSUser.class, false),
			new ColumnContext(CONTACT_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromUserList(Collection<LIMSUser>users) {

		setRowCount(0);
		if(users == null || users.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		users.stream().
			sorted(new LIMSUserComparator(SortProperty.Name)).
			forEach(user -> {
				Object[] obj = {
						user,
						user.getInfo()
					};
				rowData.add(obj);
			});
		addRows(rowData);
	}
}
