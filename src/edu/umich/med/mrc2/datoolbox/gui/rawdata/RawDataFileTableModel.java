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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class RawDataFileTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -707103850490099320L;
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String ACQ_METHOD_COLUMN = "Acq. method";

	public RawDataFileTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(ACQ_METHOD_COLUMN, DataAcquisitionMethod.class, false)
		};
	}

	public void setModelFromDataFiles(Collection<DataFile> files, boolean append) {

		if(!append)
			setRowCount(0);
		
		if(files == null || files.isEmpty())
			return;
		
		//TreeSet<DataFile>sortedFiles = new TreeSet<DataFile>(files);
		List<DataFile> sortedFiles = 
				files.stream().distinct().
				sorted().collect(Collectors.toList());
		for (DataFile file : sortedFiles) {

			Object[] obj = new Object[] { 
					file, 
					file.getDataAcquisitionMethod(), 
				};
			super.addRow(obj);
		}		
	}

	public void removeFiles(Collection<DataFile> filesToRemove) {
		
		TreeSet<DataFile>sortedFiles = new TreeSet<DataFile>();
		int fleColumn = getColumnIndex(DATA_FILE_COLUMN);
		for(int i=0; i<getRowCount(); i++)
			sortedFiles.add((DataFile)getValueAt(i, fleColumn));
			
		sortedFiles.removeAll(filesToRemove);
		setModelFromDataFiles(sortedFiles, false);
	}
}












