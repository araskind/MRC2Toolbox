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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class ImportMinimalMSOneFeaturesFromCefTask  extends CEFProcessingTask {

	private DataFile dataFile;
	private Collection<MinimalMSOneFeature>minFeatures;

	public ImportMinimalMSOneFeaturesFromCefTask(DataFile dataFile) {

		this.dataFile = dataFile;
		total = 100;
		processed = 2;
		taskDescription = "Importing MS data from " + dataFile.getName();
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public Task cloneTask() {
		return new ImportMinimalMSOneFeaturesFromCefTask(dataFile);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if(dataFile.getFullPath() == null) {
			errorMessage = "Path for CEF file not specified.";
			setStatus(TaskStatus.ERROR);
return;

		}
		inputCefFile =  new File(dataFile.getFullPath());
		try {
			parseInputCefFile(inputCefFile);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		convertMsFeatureToMinimal();
		setStatus(TaskStatus.FINISHED);
	}

	private void convertMsFeatureToMinimal() {
		
		minFeatures = new TreeSet<MinimalMSOneFeature>(); 
		for(MsFeature sf : inputFeatureList) {
			
			double rt = sf.getRetentionTime();
			for(Adduct aduct : sf.getSpectrum().getAdducts()) {
				
				double mz = sf.getSpectrum().getMsForAdduct(aduct)[0].getMz();
				minFeatures.add(new MinimalMSOneFeature(mz, rt));
			}
		}
	}

	public DataFile getInputCefFile() {
		return dataFile;
	}

	/**
	 * @return the unmatchedAdducts
	 */
	public Set<String> getUnmatchedAdducts() {
		return unmatchedAdducts;
	}

	public Collection<MinimalMSOneFeature> getMinFeatures() {
		return minFeatures;
	}
}
