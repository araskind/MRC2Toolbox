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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project;

import java.io.File;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class RawDataFilesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3417856068405445036L;

	public static final String RAW_DATA_FILE_COLUMN = "Data file";
	public static final String FULL_PATH_COLUMN = "Full path";

	public RawDataFilesTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(RAW_DATA_FILE_COLUMN, File.class, false),
			new ColumnContext(FULL_PATH_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromDataFiles(File[]dataFiles, boolean clear) {

		if(clear)
			setRowCount(0);
		
		if(dataFiles.length == 0)
			return;

		for (File df : dataFiles) {

			Object[] obj = {
					df,
					df.getAbsolutePath(),
			};
			super.addRow(obj);
		}
	}
}













