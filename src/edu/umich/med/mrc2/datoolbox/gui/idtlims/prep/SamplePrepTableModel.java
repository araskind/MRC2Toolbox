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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SamplePrepTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String PREP_COLUMN_ID_COLUMN = "Prep ID";
	public static final String SAMPLE_PREP_COLUMN = "Name";
	public static final String PREP_DATE_COLUMN = "Date";
	public static final String PREP_AUTHOR_COLUMN = "Performed by";

	public SamplePrepTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PREP_COLUMN_ID_COLUMN, PREP_COLUMN_ID_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_PREP_COLUMN, SAMPLE_PREP_COLUMN, LIMSSamplePreparation.class, false),
			new ColumnContext(PREP_DATE_COLUMN, PREP_DATE_COLUMN, Date.class, false),
			new ColumnContext(PREP_AUTHOR_COLUMN, PREP_AUTHOR_COLUMN, LIMSUser.class, false),
		};
	}

	public void setTableModelFromPreps(Collection<LIMSSamplePreparation>preps) {

		setRowCount(0);
		if(preps == null || preps.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (LIMSSamplePreparation prep : preps) {

			Object[] obj = {
					prep.getId(),
					prep,
					prep.getPrepDate(),
					prep.getCreator()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void updatePrepData(LIMSSamplePreparation prep) {

		int prepRow = getPrepRow(prep);
		if(prepRow == -1)
			return;
		
		setValueAt(prep.getId(), prepRow, getColumnIndex(PREP_COLUMN_ID_COLUMN));
		setValueAt(prep, prepRow, getColumnIndex(SAMPLE_PREP_COLUMN));
		setValueAt(prep.getPrepDate(), prepRow, getColumnIndex(PREP_DATE_COLUMN));
		setValueAt(prep.getCreator(), prepRow, getColumnIndex(PREP_AUTHOR_COLUMN));
	}
	
	public int getPrepRow(LIMSSamplePreparation prep) {

		int col = getColumnIndex(SAMPLE_PREP_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (prep.equals(getValueAt(i, col)))
				return i;
		}
		return -1;
	}
}
