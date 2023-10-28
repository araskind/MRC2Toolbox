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

package edu.umich.med.mrc2.datoolbox.gui.io.txt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;

public class ColumnFieldMatchTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 993683172691044046L;

	public static final String ENABLED_COLUMN = "Import";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String SAMPLE_ID_COLUMN = "Sample ID (Name)";

	private String sampleIdMask, sampleNameMask;
	private Matcher regexMatcher;
	private Pattern sampleIdPattern, sampleNamePattern;

	public ColumnFieldMatchTableModel() {

		super();

		columnArray = new ColumnContext[] {
			new ColumnContext(ENABLED_COLUMN, ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(DATA_FILE_COLUMN, DATA_FILE_COLUMN, DataFile.class, false),
			new ColumnContext(SAMPLE_ID_COLUMN, SAMPLE_ID_COLUMN, ExperimentalSample.class, true)
		};
		sampleIdMask = MRC2ToolBoxConfiguration.getSampleIdMask();
		sampleIdPattern = Pattern.compile(sampleIdMask);
		sampleNameMask = MRC2ToolBoxConfiguration.getSampleNameMask();
		sampleNamePattern = Pattern.compile(sampleNameMask);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		Object oldValue = super.getValueAt(row, col);

		if (col == 2) {

			ExperimentalSample es = (ExperimentalSample) value;
			DataFile df = (DataFile) getValueAt(row, 1);
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

		ArrayList<DataFile>files = new ArrayList<DataFile>();
		for(int i=0; i<getRowCount(); i++)
			files.add((DataFile) getValueAt(i, 1));

		return files.toArray(new DataFile[files.size()]);
	}

	public void setTableModelFromFiles(
			File[] inputFiles, DataAcquisitionMethod acquisitionMethod) {

		//	Add data, no cleanup, but check for dups
		//	setRowCount(0);
		DataFile[] presentFiles = getDataFiles();
		boolean add = true;
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (File f : inputFiles) {

			add = true;
			String fileBaseName = FilenameUtils.removeExtension(f.getName());
			for(DataFile pdf : presentFiles) {

				if(fileBaseName.equals(pdf.getName())) {
					add = false;
					break;
				}
			}
			if(add) {
				DataFile df = new DataFile(fileBaseName, acquisitionMethod);
				df.setFullPath(f.getAbsolutePath());
				String sampleId = "";
				String sampleName = "";
				ExperimentalSample matchedSample = null;
				regexMatcher = sampleIdPattern.matcher(f.getName());
				if (regexMatcher.find())
					sampleId = regexMatcher.group();

				if (!sampleId.isEmpty())
					matchedSample = DataImportUtils.findSampleById(sampleId);

				if (matchedSample == null) {

					regexMatcher = sampleNamePattern.matcher(f.getName());
					if (regexMatcher.find())
						sampleName = regexMatcher.group();

					matchedSample = DataImportUtils.findSampleByName(sampleName);
				}
				if (matchedSample != null)
					matchedSample.addDataFile(df);

				Object[] obj = {
					true,
					df,
					df.getParentSample()
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}






















