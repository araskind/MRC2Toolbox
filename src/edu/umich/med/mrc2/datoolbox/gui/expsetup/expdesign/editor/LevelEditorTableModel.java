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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class LevelEditorTableModel extends BasicTableModel {

	private static final long serialVersionUID = 63624132728676065L;

	public static final String ACTIVE_COLUMN = "Active";
	public static final String LEVEL_COLUMN = "Level";

	private boolean allowEdit;

	public LevelEditorTableModel() {
		super();
		columnArray = new ColumnContext[] {
			//new ColumnContext(ACTIVE_COLUMN, Boolean.class, true),
			new ColumnContext(LEVEL_COLUMN, "Level name", ExperimentDesignLevel.class, false),
		};
	}

	@Override
	public boolean isCellEditable(int row, int col) {

		if(!allowEdit)
			return false;
		else
			return columnArray[col].isEditable;
	}

	public void setEditingAllowed(boolean allowEdit) {

		this.allowEdit = allowEdit;
	}

	public void setTableModelFromDesignSubsetFactor(
			ExperimentDesignSubset designSubset, ExperimentDesignFactor factor) {

		setRowCount(0);
		allowEdit = !designSubset.isLocked();
		ExperimentDesignLevel[] activeSet = 
				designSubset.getOrderedDesign().get(factor);
		List<ExperimentDesignLevel> activeList = null;
		List<Object[]>rowData = new ArrayList<Object[]>();
				
		//	Add active levels
		if(activeSet != null) {

			for(ExperimentDesignLevel activeLevel : activeSet) {

				Object[] obj = {
					//Boolean.TRUE,
					activeLevel
				};
				rowData.add(obj);
			}
			//	Add inactive levels
			activeList = Arrays.asList(activeSet);
			for(ExperimentDesignLevel level : factor.getLevels()) {

				if(!activeList.contains(level)) {

					Object[] obj = {
						//Boolean.FALSE,
						level
					};
					rowData.add(obj);
				}
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}






















