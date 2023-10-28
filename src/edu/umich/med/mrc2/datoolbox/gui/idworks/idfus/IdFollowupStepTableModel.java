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

package edu.umich.med.mrc2.datoolbox.gui.idworks.idfus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class IdFollowupStepTableModel extends BasicTableModel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5912536371038326183L;
	public static final String STEP_ID_COLUMN = "ID";
	public static final String STEP_COLUMN = "Name";

	public IdFollowupStepTableModel() {

		super();
		columnArray = new ColumnContext[] {
			//	new ColumnContext(STEP_ID_COLUMN, String.class, false),
			new ColumnContext(STEP_COLUMN, "Identification followup step", 
					MSFeatureIdentificationFollowupStep.class, false),
		};
	}

	public void setTableModelFromFollowupStepList(
			Collection<MSFeatureIdentificationFollowupStep> followupStepList) {

		setRowCount(0);
		if(followupStepList == null || followupStepList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MSFeatureIdentificationFollowupStep step : followupStepList) {

			Object[] obj = {
				//	step.getId(),
				step
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














