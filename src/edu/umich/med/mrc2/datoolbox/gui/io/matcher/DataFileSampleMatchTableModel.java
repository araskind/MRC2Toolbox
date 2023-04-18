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

package edu.umich.med.mrc2.datoolbox.gui.io.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataFileSampleMatchTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 993683172691044046L;
	
	public static final String ENABLED_COLUMN = "Import";
	public static final String SDR_OBJECT_COLUMN = "Data file";
	public static final String SAMPLE_COLUMN = "Sample ID (Name)";
	
	public DataFileSampleMatchTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),	
			new ColumnContext(SDR_OBJECT_COLUMN, SampleDataResultObject.class, false),
			new ColumnContext(SAMPLE_COLUMN, ExperimentalSample.class, true)
		};
	}
		
	public void setModelFromSampleDataResultObjects(Set<SampleDataResultObject>objects) {
		
		setRowCount(0);
		if(objects == null || objects.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(SampleDataResultObject o : objects) {
			
			Object[] obj = {
					true,
					o,
					o.getSample()
				};
			rowData.add(obj);	
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}






















