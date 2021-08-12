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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

@SuppressWarnings("unused")
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
			new ColumnContext(PREP_COLUMN_ID_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_PREP_COLUMN, LIMSSamplePreparation.class, false),
			new ColumnContext(PREP_DATE_COLUMN, Date.class, false),
			new ColumnContext(PREP_AUTHOR_COLUMN, LIMSUser.class, false),
		};
	}

	public void setTableModelFromPreps(Collection<LIMSSamplePreparation>preps) {

		setRowCount(0);

		for (LIMSSamplePreparation prep : preps) {

			Object[] obj = {
					prep.getId(),
					prep,
					prep.getPrepDate(),
					prep.getCreator()
			};
			super.addRow(obj);
		}
	}
}
