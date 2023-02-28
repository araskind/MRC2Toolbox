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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class VendorRawDataFileTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -707103850490099320L;
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String PATH_COLUMN = "Full path";

	public VendorRawDataFileTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATA_FILE_COLUMN, File.class, false),
			new ColumnContext(PATH_COLUMN, String.class, false)
		};
	}

	public void setModelFromDataFiles(Collection<File> files, boolean replace) {

		if(replace)
			setRowCount(0);
		
		if(files == null || files.isEmpty())
			return;
		
		TreeSet<File>sortedFiles = new TreeSet<File>(files);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (File file : sortedFiles) {

			Object[] obj = new Object[] { 
					file, 
					file.getAbsolutePath()
				};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}












