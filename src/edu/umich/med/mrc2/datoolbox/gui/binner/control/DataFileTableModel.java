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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class DataFileTableModel extends BasicTableModel {

	private static final long serialVersionUID = 4183399206967466908L;

	public static final String SELECTED_COLUMN = "Selected";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String SAMPLE_COLUMN = "Sample name";

	public DataFileTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(SELECTED_COLUMN, SELECTED_COLUMN, Boolean.class, true),
			new ColumnContext(DATA_FILE_COLUMN, DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(SAMPLE_COLUMN, SAMPLE_COLUMN, ExperimentalSample.class, false),
		};
	}

	public void setTableModelFromFileCollection(Collection<DataFile> files) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(DataFile a : files) {

			Object[] obj = {
					true,
					a,
					a.getParentSample(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void changeStatusForDataFiles(Collection<DataFile> files, boolean setEnabled) {
		
		suppressEvents = true;
		int dfColumnIndex = getColumnIndex(DATA_FILE_COLUMN);
		int enabledColumnIndex = getColumnIndex(SELECTED_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			DataFile df = (DataFile)getValueAt(i, dfColumnIndex);
			if(files.contains(df))
				setValueAt(setEnabled, i, enabledColumnIndex);
		}
		suppressEvents = false;
		fireTableDataChanged();
	}
}





























