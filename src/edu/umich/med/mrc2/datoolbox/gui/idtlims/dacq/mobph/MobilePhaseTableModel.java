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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MobilePhaseTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String MOBILE_PHASE_ID_COLUMN = "ID";
	public static final String MOBILE_PHASE_DESCRIPTION_COLUMN = "Description";

	public MobilePhaseTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MOBILE_PHASE_ID_COLUMN, MOBILE_PHASE_ID_COLUMN, String.class, false),
			new ColumnContext(MOBILE_PHASE_DESCRIPTION_COLUMN, MOBILE_PHASE_DESCRIPTION_COLUMN, MobilePhase.class, false),
		};
	}

	public void setTableModelFromMobilePhaseCollection(Collection<MobilePhase>phases) {

		setRowCount(0);
		if(phases.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MobilePhase phase : phases) {

			if(phase == null)
				continue;
			
			Object[] obj = {
					phase.getId(),
					phase,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














