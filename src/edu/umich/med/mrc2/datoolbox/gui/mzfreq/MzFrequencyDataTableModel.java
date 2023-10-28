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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MzFrequencyDataTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3265763908033362302L;

	public static final String AVG_MZ_COLUMN = "M/Z (Avg)";
	public static final String MZ_RANGE_COLUMN = "M/Z range";
	public static final String RT_RANGE_COLUMN = "RT range";
	public static final String RT_RSD_COLUMN = "RT RSD, %";
	public static final String FEATURE_COUNT_COLUMN = "#Features";
	public static final String FREQUENCY_COLUMN = "Frequency, %";
	public static final String PERCENT_IDENTIFIED_COLUMN = "Identified, %";

	public MzFrequencyDataTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(AVG_MZ_COLUMN, "Average M/Z value", MzFrequencyObject.class, false),
			new ColumnContext(MZ_RANGE_COLUMN, MZ_RANGE_COLUMN, Range.class, false),
			new ColumnContext(RT_RANGE_COLUMN, "Retention time range", Range.class, false),
			new ColumnContext(RT_RSD_COLUMN, "Retention time relative standard deviation, %", MzFrequencyObject.class, false),
			new ColumnContext(FEATURE_COUNT_COLUMN, "Number of features", Integer.class, false),
			new ColumnContext(FREQUENCY_COLUMN, FREQUENCY_COLUMN, Double.class, false),
			new ColumnContext(PERCENT_IDENTIFIED_COLUMN, PERCENT_IDENTIFIED_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromMzFrequencyObjectCollection(Collection<MzFrequencyObject> collection) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MzFrequencyObject mzfo : collection) {

			Object[] obj = {

				mzfo,
				mzfo.getMzRange(),
				mzfo.getRTRange(),
				mzfo,
				mzfo.getFeatureCount(),
				mzfo.getFrequency() * 100.0d,
				mzfo.getPercentIdentified(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
