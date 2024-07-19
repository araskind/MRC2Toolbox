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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MobilePhaseSynonymsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String SYNONYM_COLUMN = "Synonym";

	public MobilePhaseSynonymsTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SYNONYM_COLUMN, SYNONYM_COLUMN, String.class, true),
		};
	}

	public void setTableModelFromSynonymList(Collection<String>synonyms) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (String synonym : synonyms) {

			Object[] obj = { synonym };
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void addNewSynonym() {

		Object[] obj = {
				null,
			};
		super.addRow(obj);
	}
}
