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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class LevelsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 552344938848875599L;
	public static final String LEVEL_COLUMN = "Level";

	public LevelsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(LEVEL_COLUMN, "Level name", ExperimentDesignLevel.class, true),
		};
	}

	public void setTableModelFromFactor(ExperimentDesignFactor factor) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(ExperimentDesignLevel level : factor.getLevels()) {

			Object[] obj = {
					level
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void addLevel(ExperimentDesignLevel newLevel) {

		Object[] obj = {
				newLevel
			};
		super.addRow(obj);
	}

	//	TODO ???
	public void reload() {

		ArrayList<ExperimentDesignLevel>levels = 
				new ArrayList<ExperimentDesignLevel>();
		int col = getColumnIndex(LEVEL_COLUMN);
		for(int i=0; i<getRowCount(); i++)
			levels.add((ExperimentDesignLevel) getValueAt(i, col));

		setRowCount(0);

		for(ExperimentDesignLevel level : levels) {

			Object[] obj = {
					level
				};
			super.addRow(obj);
		}
	}
}

















