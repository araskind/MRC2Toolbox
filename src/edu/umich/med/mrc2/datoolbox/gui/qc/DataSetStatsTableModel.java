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

package edu.umich.med.mrc2.datoolbox.gui.qc;

import java.util.Collection;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.DataFileStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataSetQcField;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataSetStatsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6956836392142526044L;

	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String INJECTION_TIME_COLUMN = "Injection timee";

	public DataSetStatsTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(INJECTION_TIME_COLUMN, Date.class, false),
			new ColumnContext(DataSetQcField.OBSERVATIONS.getName(), Integer.class, false),
			new ColumnContext(DataSetQcField.MISSING.getName(), Integer.class, false),
			new ColumnContext(DataSetQcField.OUTLIERS.getName(), Integer.class, false),
			new ColumnContext(DataSetQcField.MIN.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.MAX.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.MEAN.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.MEAN_TRIM.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.MEDIAN.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.SD.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.RSD.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.SD_TRIM.getName(), Double.class, false),
			new ColumnContext(DataSetQcField.RSD_TRIM.getName(), Double.class, false)
		};
	}

	public void setTableModelFromDataSetStats(Collection<DataFileStatisticalSummary> statsList) {

		setRowCount(0);

		for (DataFileStatisticalSummary dfss : statsList) {

			Object[] obj = {
					dfss.getFile(),
					dfss.getFile().getInjectionTime(),
					dfss.getObservations(),
					dfss.getMissing(),
					dfss.getOutliers(),
					dfss.getMin(),
					dfss.getMax(),
					dfss.getMean(),
					dfss.getMeanTrimmed(),
					dfss.getMedian(),
					dfss.getSd(),
					dfss.getRsd(),
					dfss.getSdTrimmed(),
					dfss.getRsdTrimmed() };
			super.addRow(obj);
		}
	}
}














