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

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;

public class DataFileSampleMatchTableModelOld extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 993683172691044046L;
	
	public static final String ENABLED_COLUMN = "Import";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String SAMPLE_ID_COLUMN = "Sample ID (Name)";
	private Map<DataFile, ExperimentalSample> dataFileSampleMap;

	public DataFileSampleMatchTableModelOld() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),	
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(SAMPLE_ID_COLUMN, ExperimentalSample.class, true)
		};
		dataFileSampleMap = new TreeMap<DataFile, ExperimentalSample>();
	}
	
	public void addFilesForDataPipeline(File[] inputFiles, DataPipeline pipeline) {

		DataAnalysisProject project = MRC2ToolBoxCore.getCurrentProject();
		for (File f : inputFiles) {

			String fileBaseName = FilenameUtils.getBaseName(f.getName());
			DataFile df = new DataFile(fileBaseName, pipeline.getAcquisitionMethod());
			df.setFullPath(f.getAbsolutePath());
			if(dataFileSampleMap.containsKey(df) || project.hasDataForFileInPipeline(df, pipeline))
				continue;
								
			ExperimentalSample matchedSample = DataImportUtils.getSampleFromFileName(fileBaseName);
			dataFileSampleMap.put(df, matchedSample);
			Object[] obj = {
				true,
				df,
				df.getParentSample()
			};
			super.addRow(obj);		
		}
	}
	
	public void removeDataFiles(Collection<DataFile>filesToRemove) {
		
		if(filesToRemove == null || filesToRemove.isEmpty())
			return;
			
		int dfCol = getColumnIndex(DATA_FILE_COLUMN);
		for(DataFile df : filesToRemove) {
			
			for(int i=0; i<getRowCount(); i++) {
				
				if(df.equals(getValueAt(i, dfCol))){
					removeRow(i);
					break;
				}
			}
			dataFileSampleMap.remove(df);
		}		
	}
	
	public void updateSampleAssignmentForDataFiles(Collection<DataFile> selectedDataFiles, ExperimentalSample sample) {
		
		int sampleCol = getColumnIndex(SAMPLE_ID_COLUMN);
		int dfCol = getColumnIndex(DATA_FILE_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			DataFile df = (DataFile) getValueAt(i, dfCol);
			if(selectedDataFiles.contains(df)) {
				setValueAt(sample, i, sampleCol);
				dataFileSampleMap.put(df, sample);
			}
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		int sampleCol = getColumnIndex(SAMPLE_ID_COLUMN);
		int dfCol = getColumnIndex(DATA_FILE_COLUMN);
		if (col == sampleCol) {

			ExperimentalSample es = (ExperimentalSample) value;
			DataFile df = (DataFile) getValueAt(row, dfCol);
			dataFileSampleMap.put(df, es);
		}
		super.setValueAt(value, row, col);
	}

	public void assignSamples(Map<DataFile, ExperimentalSample> fileSampleMap) {
		
		dataFileSampleMap.clear();
		int dataFileColumn = getColumnIndex(DATA_FILE_COLUMN);
		int sampleColumn = getColumnIndex(SAMPLE_ID_COLUMN);		
		for(int i=0; i<getRowCount(); i++) {
			
			DataFile df = (DataFile)getValueAt(i, dataFileColumn);
			ExperimentalSample sample = fileSampleMap.get(df);
			setValueAt(sample, i, sampleColumn);		
			dataFileSampleMap.put(df, sample);
		}
	}

	public void forceSampleAssignment() {

		int dataFileColumn = getColumnIndex(DATA_FILE_COLUMN);
		int sampleColumn = getColumnIndex(SAMPLE_ID_COLUMN);
		dataFileSampleMap.clear();
		for(int i=0; i<getRowCount(); i++) {
			
			DataFile df = (DataFile)getValueAt(i, dataFileColumn);
			ExperimentalSample sample = (ExperimentalSample)getValueAt(i, sampleColumn);			
			dataFileSampleMap.put(df, sample);
		}
	}

	public Map<DataFile, ExperimentalSample> getDataFileSampleMap() {
		return dataFileSampleMap;
	}
}






















