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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.mzdiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.DoubleValueBin;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MassDifferenceSummaryTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7222450809135744946L;
	public static final String DELTA_MZ_COLUMN = "M/Z difference";
	public static final String BIN_WIDTH_COLUMN = "Binning window, Da";
	public static final String COUNTS_COLUMN = "Counts";
	public static final String STDEV_COLUMN = "SD";

	public MassDifferenceSummaryTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DELTA_MZ_COLUMN, Double.class, true),
			new ColumnContext(BIN_WIDTH_COLUMN, Double.class, false),
			new ColumnContext(COUNTS_COLUMN, Integer.class, false),
			new ColumnContext(STDEV_COLUMN, Double.class, false),
		};
	}

	public void setModelFromBins(Collection<DoubleValueBin>bins) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(DoubleValueBin bin : bins) {

			Object[] obj = {
				bin.getStatistics().getPercentile(50.0),
				bin.getStatistics().getMax() - bin.getStatistics().getMin(),
				bin.getStatistics().getN(),
				bin.getStatistics().getStandardDeviation()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
