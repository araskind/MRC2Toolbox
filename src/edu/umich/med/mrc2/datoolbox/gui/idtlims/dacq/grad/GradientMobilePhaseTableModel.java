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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class GradientMobilePhaseTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String CHANNEL = "Channel";
	public static final String MOBILE_PHASE_DESCRIPTION_COLUMN = "Mobile phase";

	public static final String[]channelArray = new String[] {
			"Channel A","Channel B","Channel C","Channel D"};
	public GradientMobilePhaseTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(CHANNEL, CHANNEL, String.class, false),
			new ColumnContext(MOBILE_PHASE_DESCRIPTION_COLUMN, 
					MOBILE_PHASE_DESCRIPTION_COLUMN, MobilePhase.class, false),
		};
	}

	public void setTableModelFromMobilePhaseArray(MobilePhase[]phases) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (int i=0; i<4; i++) {

			MobilePhase current = null;
			if(phases != null && phases.length > i)
				current = phases[i];
			
			Object[] obj = {
					channelArray[i],
					current,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














