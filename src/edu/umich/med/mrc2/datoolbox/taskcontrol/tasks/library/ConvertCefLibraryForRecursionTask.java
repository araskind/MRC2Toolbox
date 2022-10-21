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
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class ConvertCefLibraryForRecursionTask extends CEFProcessingTask {

	private boolean combineAdducts;
	private TreeSet<String> unmatchedAdducts;

	public ConvertCefLibraryForRecursionTask(			
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts) {

		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public void run() {

		taskDescription = 
				"Creating library for recursion from " + inputCefFile.getName();
		setStatus(TaskStatus.PROCESSING);
		createLibraryFeaturetListFromCefFile();
		if(!unmatchedAdducts.isEmpty()){
			errorMessage = "Unmatched adducts: " + 
					StringUtils.join(unmatchedAdducts, "; ");
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(libraryFeatureListForExport == null || libraryFeatureListForExport.isEmpty()) {			
			errorMessage = "Failed to parse input file, no features to export";
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			writeCefLibrary(
					libraryFeatureListForExport,
					combineAdducts);
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				inputCefFile,
				outputCefFile,
				combineAdducts);
	}
}









































