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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.edl;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

@SuppressWarnings("unused")
public class ExistingDataFilesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String SAMPLE_COLUMN = "Sample ID";
	public static final String SAMPLE_PREP_COLUMN = "Prep ID";
	public static final String SAMPLE_PREP_ITEM_COLUMN = "Prep item ID";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String ACQ_METHOD_COLUMN = "Acquisition method";
	public static final String INJECTION_TIME_COLUMN = "Injection time";
	public static final String INJECTION_VOLUME_COLUMN = "Injection volume";

	public ExistingDataFilesTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(SAMPLE_COLUMN, ExperimentalSample.class, true),
			new ColumnContext(SAMPLE_PREP_ITEM_COLUMN, String.class, false),
			new ColumnContext(ACQ_METHOD_COLUMN, DataAcquisitionMethod.class, false),
			new ColumnContext(INJECTION_TIME_COLUMN, Date.class, false),
			new ColumnContext(INJECTION_VOLUME_COLUMN, Double.class, false),
			new ColumnContext(SAMPLE_PREP_COLUMN, LIMSSamplePreparation.class, false),
		};
	}

	public void setTableModelFromLimsWorklistItems(Collection<LIMSWorklistItem>items) {

		setRowCount(0);

		for (LIMSWorklistItem item : items) {

			Object[] obj = {
				item.getDataFile(),
				item.getSample(),
				item.getPrepItemId(),
				item.getAcquisitionMethod(),
				item.getTimeStamp(),
				item.getInjectionVolume(),
				item.getSamplePrep(),
			};
			super.addRow(obj);
		}
	}

	public void setTableModelFromLimsWorklist(Worklist wkl) {

		Collection<LIMSWorklistItem>items =
				wkl.getTimeSortedWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).
				collect(Collectors.toList());

		setTableModelFromLimsWorklistItems(items);
	}
}
