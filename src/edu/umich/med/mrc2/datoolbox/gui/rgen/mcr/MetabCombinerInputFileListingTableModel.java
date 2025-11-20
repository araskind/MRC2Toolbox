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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MetabCombinerInputFileListingTableModel extends BasicTableModel {

	private static final long serialVersionUID = -707103850490099320L;
	public static final String EXPERIMENT_COLUMN = "Experiment";
	public static final String BATCH_COLUMN = "Batch";
	public static final String DATA_FILE_COLUMN = "Data file";

	public MetabCombinerInputFileListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(EXPERIMENT_COLUMN, EXPERIMENT_COLUMN, String.class, true),
			new ColumnContext(BATCH_COLUMN, BATCH_COLUMN, String.class, true),
			new ColumnContext(DATA_FILE_COLUMN, DATA_FILE_COLUMN, File.class, false),
		};
	}

	public void setModelFromDataFiles(Collection<File> files, boolean replace) {

		if(replace)
			setRowCount(0);
		
		if(files == null || files.isEmpty())
			return;
		
		TreeSet<File>sortedFiles = new TreeSet<File>(files);
		List<Object[]>rowData = new ArrayList<>();
		for (File file : sortedFiles) {

			Object[] obj = new Object[] { 
					null,
					null,
					file, 
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setModelFromInputObjects(
			Collection<MetabCombinerFileInputObject> mcioCollection, boolean replace) {
		
		if(replace)
			setRowCount(0);
		
		if(mcioCollection == null || mcioCollection.isEmpty())
			return;
		
		TreeSet<MetabCombinerFileInputObject>sortedInputObjects 
			= new TreeSet<MetabCombinerFileInputObject>(mcioCollection);
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MetabCombinerFileInputObject mcio : sortedInputObjects) {

			Object[] obj = new Object[] { 
					mcio.getExperimentId(),
					mcio.getBatchId(),
					mcio.getDataFile(), 
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);		
	}
}












