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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.sampletype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MotrpacSampleType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SampleTypeTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5080359273837607892L;
	public static final String SAMPLE_TYPE_COLUMN = "MoTrPAC sample type";
	public static final String SAMPLE_DESCRIPTION_COLUMN = "Description";

	public SampleTypeTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SAMPLE_TYPE_COLUMN, SAMPLE_TYPE_COLUMN, MotrpacSampleType.class, false),
			new ColumnContext(SAMPLE_DESCRIPTION_COLUMN, SAMPLE_DESCRIPTION_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromSamples(Collection<MotrpacSampleType>samples) {

		setRowCount(0);
		if(samples == null || samples.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MotrpacSampleType sample : samples) {

			Object[] obj = {

				sample,
				sample.getDescription()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
