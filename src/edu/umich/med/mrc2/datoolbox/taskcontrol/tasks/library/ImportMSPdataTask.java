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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;

public class ImportMSPdataTask extends AbstractTask {

	private File inputFile;
	private Collection<TandemMassSpectrum>msmsDataSet;

	public ImportMSPdataTask(File inputFile) {
		super();
		this.inputFile = inputFile;
		msmsDataSet = new ArrayList<TandemMassSpectrum>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 20;

		if (inputFile != null) {

			if (inputFile.exists()) {
				try {
					List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(inputFile);

					if(!mspChunks.isEmpty()) {

						total = mspChunks.size();
						processed = 0;

						for(List<String> chunk : mspChunks) {

							TandemMassSpectrum msms = null;
							try {
								LibraryMsFeature activeFeature = null;
								msms = MsImportUtils.parseMspDataSource(chunk, activeFeature);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(msms != null)
								msmsDataSet.add(msms);

							processed++;
						}
					}
					setStatus(TaskStatus.FINISHED);
				}
				catch (Exception e) {

					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {

		return new ImportMSPdataTask(inputFile);
	}

	/**
	 * @return the msmsDataSet
	 */
	public Collection<TandemMassSpectrum> getMsmsDataSet() {
		return msmsDataSet;
	}

}
















