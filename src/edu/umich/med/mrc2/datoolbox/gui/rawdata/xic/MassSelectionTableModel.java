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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MassSelectionTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";

	public MassSelectionTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromDataPoints(Collection<MsPoint>points) {

		setRowCount(0);
		if(points == null && points.isEmpty())
			return;

		for(MsPoint dp : points) {

			Object[] obj = {
					dp.getMz(),
					dp.getIntensity(),
			};
			super.addRow(obj);
		}		
	}
}























