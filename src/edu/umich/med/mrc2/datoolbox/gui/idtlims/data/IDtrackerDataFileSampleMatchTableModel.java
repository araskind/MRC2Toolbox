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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class IDtrackerDataFileSampleMatchTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 993683172691044046L;

//	public static final String ENABLED_COLUMN = "Import";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String ACQUISITION_METHOD_COLUMN = "Acquisition method";
	public static final String INJETION_TIMESTAMP_COLUMN = "Injetion timestamp";
	public static final String DA_METHOD_COLUMN = "Data analysis method";

	public IDtrackerDataFileSampleMatchTableModel() {

		super();

		columnArray = new ColumnContext[] {
//			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(ACQUISITION_METHOD_COLUMN, DataAcquisitionMethod.class, false),
			new ColumnContext(INJETION_TIMESTAMP_COLUMN, Date.class, false),
			new ColumnContext(DA_METHOD_COLUMN, DataExtractionMethod.class, true),
		};
	}

	public DataFile[] getDataFiles() {

		ArrayList<DataFile>files = new ArrayList<DataFile>();
		int dfColumn = getColumnIndex(DATA_FILE_COLUMN);
		for(int i=0; i<getRowCount(); i++)
			files.add((DataFile) getValueAt(i, dfColumn));

		return files.toArray(new DataFile[files.size()]);
	}

	private Collection<String>getLoadedFileNames(){

		Collection<String>loadedFileNames = new TreeSet<String>();
		int dfColumn = getColumnIndex(DATA_FILE_COLUMN);
		for(int i=0; i<getRowCount(); i++)
			loadedFileNames.add(((DataFile) getValueAt(i, dfColumn)).getName());

		return loadedFileNames;
	}

	public void setTableModelFromFiles(File[] inputFiles, LIMSExperiment experiment) {

		//	Add data, no cleanup, but check for dups
		//	setRowCount(0);
		Collection<String>loadedFileNames = getLoadedFileNames();
		List<File> filesToAdd = Arrays.asList(inputFiles).stream().
				filter(f -> !loadedFileNames.contains(f.getName())).
				collect(Collectors.toList());

		if(filesToAdd.isEmpty())
			return;

		Collection<DataFile>newDataFiles = new TreeSet<DataFile>();
		for(File f : filesToAdd) {
			DataFile df = new DataFile(f.getName(),null);
			df.setFullPath(f.getAbsolutePath());
			newDataFiles.add(df);
		}
		try {
			IDTUtils.mapInjectionsToDataFiles(newDataFiles, experiment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(DataFile df : newDataFiles) {

			Object[] obj = {
//					df.getDataAcquisitionMethod() != null,
					df,
					df.getDataAcquisitionMethod(),
					df.getInjectionTime(),
					null,
				};
			super.addRow(obj);
		}
	}

	public void addDataFilesUsingWorklist(File[] inputFiles, Worklist worklist) {

		Collection<String>loadedFileNames = getLoadedFileNames();
		List<File> filesToAdd = Arrays.asList(inputFiles).stream().
				filter(f -> !loadedFileNames.contains(f.getName())).
				collect(Collectors.toList());

		if(filesToAdd.isEmpty())
			return;
		
		Collection<LIMSWorklistItem>wlItems = worklist.getWorklistItems().stream().
				filter(LIMSWorklistItem.class::isInstance).
				map(LIMSWorklistItem.class::cast).
				collect(Collectors.toList());

		Collection<DataFile>newDataFiles = new TreeSet<DataFile>();
		for(File f : filesToAdd) {
			DataFile df = new DataFile(f.getName(),null);
			df.setFullPath(f.getAbsolutePath());
			setFileDataFromWorklistItem(df, wlItems);
			newDataFiles.add(df);
		}

		for(DataFile df : newDataFiles) {

			Object[] obj = {
//					df.getDataAcquisitionMethod() != null,
					df,
					df.getDataAcquisitionMethod(),
					df.getInjectionTime(),
					null,
				};
			super.addRow(obj);
		}
		
	}
	
	private void setFileDataFromWorklistItem(DataFile file, Collection<LIMSWorklistItem>wlItems) {
		
		String fName = FilenameUtils.getBaseName(file.getName());
		LIMSWorklistItem fileItem = wlItems.stream().
				filter(i -> FilenameUtils.getBaseName(i.getDataFile().getName()).equals(fName)).
				findFirst().orElse(null);
		
		if(fileItem == null)
			return;
		
		file.setDataAcquisitionMethod(fileItem.getAcquisitionMethod());
		file.setInjectionTime(fileItem.getTimeStamp());
	}
}
