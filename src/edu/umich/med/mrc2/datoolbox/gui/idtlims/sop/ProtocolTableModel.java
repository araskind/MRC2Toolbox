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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.sop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.SopCategory;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

@SuppressWarnings("unused")
public class ProtocolTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String SOP_ID_COLUMN = "SOP ID";
	public static final String SOP_GROUP_COLUMN = "Group";
	public static final String SOP_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String VERSION_COLUMN = "Version";
	public static final String DATE_CREATED_COLUMN = "Created on";
	public static final String CRERATED_BY_COLUMN = "Created by";
	public static final String CATEGORY_COLUMN = "Category";

	public ProtocolTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SOP_ID_COLUMN, String.class, false),
			new ColumnContext(SOP_GROUP_COLUMN, String.class, false),
			new ColumnContext(SOP_COLUMN, LIMSProtocol.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(VERSION_COLUMN, String.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, false),
			new ColumnContext(CRERATED_BY_COLUMN, LIMSUser.class, false),
			new ColumnContext(CATEGORY_COLUMN, SopCategory.class, false),
		};
	}

	public void setTableModelFromProtocols(Collection<LIMSProtocol>protocols) {

		setRowCount(0);
		if(protocols == null || protocols.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (LIMSProtocol protocol : protocols) {

			Object[] obj = {
					protocol.getSopId(),
					protocol.getSopGroup(),
					protocol,
					protocol.getSopDescription(),
					protocol.getSopVersion(),
					protocol.getDateCrerated(),
					protocol.getCreatedBy(),
					protocol.getSopCategory()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
