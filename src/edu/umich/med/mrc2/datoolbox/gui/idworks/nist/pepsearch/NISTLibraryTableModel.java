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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class NISTLibraryTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6655746948950669281L;

	public static final String ACTIVE_COLUMN = "Enabled";
	public static final String LIBRARY_FILE_COLUMN = "Library path";

	public NISTLibraryTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ACTIVE_COLUMN, Boolean.class, true),
			new ColumnContext(LIBRARY_FILE_COLUMN, File.class, false),
		};
	}

	public void addLibraryFile(File libFile) {

		if(libFile.exists())
			super.addRow(new Object[] {true, libFile});
	}

	public void removeLibraryFiles(Collection<File> libFiles) {

		int colIdx = getColumnIndex(LIBRARY_FILE_COLUMN);
		for(File libFile : libFiles) {

			for(int i=0; i<getRowCount(); i++) {
				if(getValueAt(i, colIdx).equals(libFile))
				super.removeRow(i);
			}
		}
	}

	public void setModelFromFiles(Map<File, Boolean> libFiles) {

		setRowCount(0);
		for(Entry<File, Boolean>fd  : libFiles.entrySet()) {
			if(fd.getKey().exists())
				super.addRow(new Object[] {fd.getValue(), fd.getKey()});
		}
	}
}













