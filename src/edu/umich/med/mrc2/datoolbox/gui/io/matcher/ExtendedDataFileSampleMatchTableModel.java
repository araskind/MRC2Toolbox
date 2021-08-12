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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExtendedDataFileSampleMatchTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 993683172691044046L;

	public static final String ENABLED_COLUMN = "Import";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String IMPORTED_SAMPLE_ID_COLUMN = "Imported Sample ID";
	public static final String IMPORTED_SAMPLE_NAME_COLUMN = "Imported Sample name";
	public static final String SAMPLE_ID_COLUMN = "Sample ID";

	private Matcher regexMatcher;
	private Pattern sampleIdPattern, sampleNamePattern;
	private DataAcquisitionMethod acquisitionMethod;
	private DataAnalysisProject project;
	private int sampleColumnIdx, dataFileColumnIdx;

	public ExtendedDataFileSampleMatchTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(IMPORTED_SAMPLE_ID_COLUMN, String.class, false),
			new ColumnContext(IMPORTED_SAMPLE_NAME_COLUMN, String.class, false),
			new ColumnContext(SAMPLE_ID_COLUMN, ExperimentalSample.class, true)
		};
		sampleIdPattern = Pattern.compile(MRC2ToolBoxConfiguration.getSampleIdMask());
		sampleNamePattern = Pattern.compile(MRC2ToolBoxConfiguration.getSampleNameMask());
		sampleColumnIdx = getColumnIndex(SAMPLE_ID_COLUMN);
		dataFileColumnIdx = getColumnIndex(DATA_FILE_COLUMN);
	}

	public void setTableModelFromReportData(
		String[] sampleIds,
		String[] sampleNames,
		String[] dataFileNames,
		DataAcquisitionMethod acquisitionMethod) {

		setRowCount(0);
		project = MRC2ToolBoxCore.getCurrentProject();
		this.acquisitionMethod = acquisitionMethod;

		String[] rows = null;
		if(sampleIds != null)
			rows = sampleIds;

		if(rows == null && sampleNames != null)
			rows = sampleNames;

		if(rows == null && dataFileNames != null)
			rows = dataFileNames;

		for(int i=0; i<rows.length; i++) {

			DataFile df = null;
			ExperimentalSample sample = null;
			String importedSampleId = null;
			String importedSampleName = null;
			if(sampleIds != null) {
				importedSampleId = sampleIds[i];
				sample = findSampleInString(importedSampleId);
			}
			if(dataFileNames != null) {
				df = new DataFile(FilenameUtils.getBaseName(dataFileNames[i]), acquisitionMethod);
				if(sample == null)
					sample = findSampleInString(df.getName());
			}
			if(sampleNames != null) {
				importedSampleName = sampleNames[i];
				if(sample == null)
					sample = findSampleInString(importedSampleName);
			}
			Object[] obj = {
				true,
				df,
				importedSampleId,
				importedSampleName,
				sample,
			};
			super.addRow(obj);
		}
	}

	private ExperimentalSample findSampleInString(String data) {

		String sampleId = "";
		String sampleName = "";
		ExperimentalSample matchedSample = null;
		regexMatcher = sampleIdPattern.matcher(data);
		if (regexMatcher.find())
			sampleId = regexMatcher.group();

		if (!sampleId.isEmpty())
			matchedSample = project.getExperimentDesign().getSampleById(sampleId);

		if (matchedSample == null) {

			regexMatcher = sampleNamePattern.matcher(data);
			if (regexMatcher.find())
				sampleName = regexMatcher.group();

			if (!sampleName.isEmpty())
				matchedSample = project.getExperimentDesign().getSampleByName(sampleName);
		}
		return matchedSample;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		Object oldValue = super.getValueAt(row, col);
		if (col == sampleColumnIdx) {

			ExperimentalSample es = (ExperimentalSample) value;
			DataFile df = (DataFile) getValueAt(row, dataFileColumnIdx);
			df.setParentSample(es);
			if (oldValue != null) {

				ExperimentalSample oldSample = (ExperimentalSample) oldValue;
				if (!oldSample.equals(es))
					oldSample.removeDataFile(df);
			}
			if (es != null)
				es.addDataFile(df);
			else
				df.setParentSample(null);
		}
		super.setValueAt(value, row, col);
	}

	public DataFile[] getDataFiles() {

		ArrayList<DataFile> files = new ArrayList<DataFile>();
		for (int i = 0; i < getRowCount(); i++)
			files.add((DataFile) getValueAt(i, 1));

		return files.toArray(new DataFile[files.size()]);
	}
}