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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.dbparse.load.massbank.MassBankFileParser;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class PassatuttoOldDecoyExtractionTask extends AbstractTask {

	private File decoyFolder;
	private Collection<TandemMassSpectrum>msmsDataSet;
		
	public PassatuttoOldDecoyExtractionTask(File decoyFolder) {
		super();
		this.decoyFolder = decoyFolder;
	}

	@Override
	public void run() {

		taskDescription = "Scanning decoy data in " + decoyFolder.getAbsolutePath();
		total = 100;
		processed = 10;
		setStatus(TaskStatus.PROCESSING);
		IOFileFilter decoyFileFilter = FileFilterUtils.makeFileOnly(
				new RegexFileFilter(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}.txt$"));
		Collection<File> decoys = FileUtils.listFilesAndDirs(
				decoyFolder,
				decoyFileFilter,
				null);
		
		if (decoys.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			extractDecoyData(decoys);
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}
	
	private void extractDecoyData(Collection<File> decoys) {

		taskDescription = "Extracting decoy data from files in " + decoyFolder.getAbsolutePath();
		total = decoys.size();
		processed = 0;
		msmsDataSet = new ArrayList<TandemMassSpectrum>();
		for(File decoy : decoys) {
			
			if(decoy.isDirectory())
				continue;
			
			List<List<String>> records = MassBankFileParser.parseInputMassBankFile(decoy);
			
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new PassatuttoOldDecoyExtractionTask(decoyFolder);
	}
}
