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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openscience.cdk.formula.IsotopeContainer;
import org.openscience.cdk.formula.IsotopePattern;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMs;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -9094237250900353734L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";

	public MsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, INTENSITY_COLUMN, Double.class, false)
		};
	}

	public void setTableModelFromIsotopePattern(IsotopePattern isoPattern) {

		setRowCount(0);
		if(isoPattern == null || isoPattern.getIsotopes().isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (IsotopeContainer dp : isoPattern.getIsotopes()) {

			Object[] obj = {
				dp.getMass(),
				dp.getIntensity() * 100
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setTableModelFromSimpleMs(SimpleMs ms) {

		setRowCount(0);
		if(ms == null || ms.getDataPoints().length == 0)
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MsPoint dp : ms.getDataPoints()) {

			Object[] obj = {
				dp.getMz(),
				dp.getIntensity()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setTableModelFromMsPointCollection(Collection<MsPoint> msPoints) {

		setRowCount(0);
		if(msPoints == null || msPoints.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MsPoint dp : msPoints) {

			Object[] obj = {
				dp.getMz(),
				dp.getIntensity()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}











