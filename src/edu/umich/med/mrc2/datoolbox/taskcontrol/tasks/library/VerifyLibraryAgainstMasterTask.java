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
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.BasePCDLutils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class VerifyLibraryAgainstMasterTask extends PCDLAbstractTask {

	private static final Logger logger = LogManager.getLogger(VerifyLibraryAgainstMasterTask.class);

	private File inputLibraryFile;
	private CompoundLibrary masterLibrary;
	private List<String>unmatchedCompounds;
	
	public VerifyLibraryAgainstMasterTask(File inputLibraryFile, CompoundLibrary masterLibrary) {
		super();
		this.inputLibraryFile = inputLibraryFile;
		this.masterLibrary = masterLibrary;
	}

	@Override
	public void run() {

		
		setStatus(TaskStatus.PROCESSING);
		fetchMasterLibrary();
		parseInputLibraryFile();
		validateInputData();

		setStatus(TaskStatus.FINISHED);
	}

	private void fetchMasterLibrary() {

		taskDescription = "Populating master library from database";
		total = 100;
		processed = 20;
		if(masterLibrary.getFeatures().isEmpty()) {
			try {
				BasePCDLutils.populateBasePCDLlibrary(masterLibrary);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		processed = 40;
	}	

	private void parseInputLibraryFile() {
		// TODO Auto-generated method stub
		
	}

	private void validateInputData() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Task cloneTask() {
		return new VerifyLibraryAgainstMasterTask(inputLibraryFile, masterLibrary);
	}
}
