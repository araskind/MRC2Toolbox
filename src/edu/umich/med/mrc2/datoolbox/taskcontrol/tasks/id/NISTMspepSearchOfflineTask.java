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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import java.io.File;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class NISTMspepSearchOfflineTask extends NISTMsPepSearchRoundTripTask {

	public NISTMspepSearchOfflineTask(
			String searchCommand, 
			Collection<MsFeatureInfoBundle> featuresToSearch,
			File inputFile, 
			File resultFile) {
		super(searchCommand, featuresToSearch, inputFile, resultFile);
	}
	
	public NISTMspepSearchOfflineTask(
			List<String> commandParts, 
			Collection<MsFeatureInfoBundle> featuresToSearch,
			File inputFile, 
			File resultFile) {
		super(commandParts, featuresToSearch, inputFile, resultFile);
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if(featuresToSearch != null) {
			try {
				createInputMspFile();
			}
			catch (Exception e) {
				e.printStackTrace();
				errorMessage = "Failed to create MSP input file.";
				setStatus(TaskStatus.ERROR);
			}
		}
		try {
			runPepSearch();
		} catch (Exception e1) {
			e1.printStackTrace();
			errorMessage = "Failed to run PepSearch.";
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
}
