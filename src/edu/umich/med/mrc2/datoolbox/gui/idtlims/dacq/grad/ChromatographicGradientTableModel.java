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
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ChromatographicGradientTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String START_TIME_COLUMN = "Start time, min";
	public static final String PERCENT_A_COLUMN = "%A";
	public static final String PERCENT_B_COLUMN = "%B";
	public static final String PERCENT_C_COLUMN = "%C";
	public static final String PERCENT_D_COLUMN = "%D";
	public static final String FLOW_RATE_COLUMN = "Flow rate, ml/min";

	public ChromatographicGradientTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(START_TIME_COLUMN, START_TIME_COLUMN, Double.class, false),
			new ColumnContext(PERCENT_A_COLUMN, "% solvent A", Double.class, false),
			new ColumnContext(PERCENT_B_COLUMN, "% solvent B", Double.class, false),
			new ColumnContext(PERCENT_C_COLUMN, "% solvent C", Double.class, false),
			new ColumnContext(PERCENT_D_COLUMN, "% solvent D", Double.class, false),
			new ColumnContext(FLOW_RATE_COLUMN, FLOW_RATE_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromGradient(ChromatographicGradient gradient) {

		setRowCount(0);
		Set<ChromatographicGradientStep> steps = gradient.getGradientSteps();
		if(steps.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (ChromatographicGradientStep step : steps) {

			Object[] obj = {
				step.getStartTime(),
				step.getMobilePhaseStartingPercent()[0],
				step.getMobilePhaseStartingPercent()[1],
				step.getMobilePhaseStartingPercent()[2],
				step.getMobilePhaseStartingPercent()[3],
				step.getFlowRate()					
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}














