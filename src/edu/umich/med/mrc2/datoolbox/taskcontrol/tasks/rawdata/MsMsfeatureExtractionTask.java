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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.SupportedRawDataTypes;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.fileio.exceptions.FileParsingException;
import umich.ms.fileio.filetypes.mzml.MZMLFile;
import umich.ms.fileio.filetypes.mzxml.MZXMLFile;
import umich.ms.fileio.filetypes.xmlbased.AbstractXMLBasedDataSource;

public class MsMsfeatureExtractionTask extends AbstractTask {

	private File sourceRawFile;
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;

	private LCMSData data;
	private Collection<MsFeature>features;

	public MsMsfeatureExtractionTask(
			File sourceRawFile,
			Range dataExtractionRtRange,
			boolean removeAllMassesAboveParent,
			double msMsCountsCutoff,
			int maxFragmentsCutoff) {
		super();
		this.sourceRawFile = sourceRawFile;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Importing data from file " + sourceRawFile.getName();
		total = 100;
		processed = 0;
		try {
			createDataSource();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}

	private void createDataSource() throws FileParsingException {

		Path path = Paths.get(sourceRawFile.getAbsolutePath());
		AbstractXMLBasedDataSource source = null;

		if (FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(SupportedRawDataTypes.MZML.name()))
			source = new MZMLFile(path.toString());

		if (FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(SupportedRawDataTypes.MZXML.name()))
			source = new MZXMLFile(path.toString());

		if (source == null) {
			setStatus(TaskStatus.ERROR);
			return;
		}
		data = new LCMSData(source);
		data.load(LCMSDataSubset.STRUCTURE_ONLY, this);
		
		//LCMSData
	}

	@Override
	public Task cloneTask() {
		return new MsMsfeatureExtractionTask(
				sourceRawFile,
				dataExtractionRtRange,
				removeAllMassesAboveParent,
				msMsCountsCutoff,
				maxFragmentsCutoff);
	}
}
