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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class WorklistExtractionTask extends WorklistTask {

	private boolean writeWorklistToFile;
	private boolean appendWorklist;

	//	TODO - handle different instrument vendors
	public WorklistExtractionTask(
			File sourceFileOrDirectory,
			WorklistImportType importType) {
		this(sourceFileOrDirectory, importType, true, false);		
	}
	
	public WorklistExtractionTask(
			File sourceFileOrDirectory,
			WorklistImportType importType,
			boolean writeWorklistToFile,
			boolean appendWorklist) {
		super(sourceFileOrDirectory, importType);
		this.writeWorklistToFile = writeWorklistToFile;
		this.appendWorklist = appendWorklist;
		taskDescription = "Importing worklist data from " + 
				sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if(importType.equals(WorklistImportType.VENDOR_WORKLIST)) {
			try {
				readWorklistFile();
			} catch (Exception e) {

				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(importType.equals(WorklistImportType.RAW_DATA_DIRECTORY_SCAN)) {

			if (sourceFileOrDirectory == null || !sourceFileOrDirectory.exists() || !sourceFileOrDirectory.canRead()) {
				setStatus(TaskStatus.ERROR);
				return;
			}
			IOFileFilter dotDfilter = 
					FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
			Collection<File> dotDfiles = FileUtils.listFilesAndDirs(
					sourceFileOrDirectory,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dotDfiles.isEmpty()) {

				try {
					scanDirectoryForSampleInfo(dotDfiles);
				} catch (Exception e) {

					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		}
		if(importType.equals(WorklistImportType.PLAIN_TEXT_FILE)) {
			//	TODO
		}
		if(importType.equals(WorklistImportType.EXCEL_FILE)) {
			//	TODO
		}
		if(writeWorklistToFile)
			writeOutExtractedWorklist();
		
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new WorklistExtractionTask(
				sourceFileOrDirectory, 
				importType, 
				writeWorklistToFile, 
				appendWorklist);
	}

	public boolean isAppendWorklist() {
		return appendWorklist;
	}
}
















