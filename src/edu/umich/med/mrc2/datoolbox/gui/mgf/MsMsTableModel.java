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

package edu.umich.med.mrc2.datoolbox.gui.mgf;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsMsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6270927228012796996L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";
	public static final String PARENT_ION_COLUMN = "Parent ion";

	public MsMsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(PARENT_ION_COLUMN, String.class, false)
		};
	}

	public void setTableModelFromMsMsCluster(MsMsCluster featureCluster) {

		setRowCount(0);
		if(featureCluster == null)
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		String parent;

		//	TODO add preferences for this value
		//	double accuracyPpm = CaConfiguration.getMassAccuracy();
		double accuracyPpm = 100.0d;
		Range parentRange = 
				MsUtils.createPpmMassRange(featureCluster.getParentMass(), accuracyPpm);
		for (MsPoint dp : featureCluster.getAverageMsMs()) {

			parent = "";
			if (parentRange.contains(dp.getMz()))
				parent = "Yes";

			Object[] obj = {
				dp.getMz(),
				dp.getIntensity(),
				parent
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setTableModelFromSimpleMsMs(SimpleMsMs msms) {

		setRowCount(0);
		if(msms == null || msms.getDataPoints().length == 0)
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		String parent;
		for (MsPoint dp : msms.getDataPoints()) {

			parent = "";
			if (dp.equals(msms.getParentMass()))
				parent = "Yes";

			Object[] obj = {
				dp.getMz(),
				dp.getIntensity(),
				parent
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}



















