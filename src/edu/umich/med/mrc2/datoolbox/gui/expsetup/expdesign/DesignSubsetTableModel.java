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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DesignSubsetTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -2948119245732438175L;

	public static final String ACTIVE_COLUMN = "Active";
	public static final String DESIGN_SUBSET_COLUMN = "Design subset";

	public DesignSubsetTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ACTIVE_COLUMN, ACTIVE_COLUMN, Boolean.class, true),
			new ColumnContext(DESIGN_SUBSET_COLUMN, "Experimental design subset", ExperimentDesignSubset.class, false)
		};
	}

	public void setModelFromProject(DataAnalysisProject currentProject) {

		setRowCount(0);
		if(currentProject != null) 
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (ExperimentDesignSubset eds : currentProject.getExperimentDesign().getDesignSubsets()) {

			Object[] obj = new Object[] { eds.isActive(), eds };
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}












