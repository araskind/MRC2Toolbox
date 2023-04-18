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
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameCategory;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundSynonymTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3429946667191482613L;

	public static final String DEFAULT_COLUMN = "Primary";
	public static final String SYNONYM_COLUMN = "Name";

	public CompoundSynonymTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(DEFAULT_COLUMN, Boolean.class, true),
			new ColumnContext(SYNONYM_COLUMN, String.class, false)
		};
	}
	
	public void disableEditing() {
		columnArray[0] = new ColumnContext(DEFAULT_COLUMN, Boolean.class, false);
	}

	public void setModelFromCompoundNameSet(CompoundNameSet nameSet) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<String, CompoundNameCategory> entry : nameSet.getSynonyms().entrySet()) {

			Object[] obj = {
				entry.getValue().equals(CompoundNameCategory.PRI),
				entry.getKey()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}































