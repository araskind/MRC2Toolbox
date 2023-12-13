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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MinimalIdLevelTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String LEVEL_COLUMN = "Identification level";
	public static final String LEVEL_COLOR_CODE_COLUMN = "Color";

	public MinimalIdLevelTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(LEVEL_COLOR_CODE_COLUMN, "Color code",
					MSFeatureIdentificationLevel.class, false),
			new ColumnContext(LEVEL_COLUMN, LEVEL_COLUMN, 
					MSFeatureIdentificationLevel.class, false),			
		};
	}

	public void setTableModelFromLevelList(Collection<MSFeatureIdentificationLevel>levelList) {

		setRowCount(0);

		if(levelList == null || levelList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MSFeatureIdentificationLevel level : levelList) {

			Object[] obj = {
				level,
				level,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














