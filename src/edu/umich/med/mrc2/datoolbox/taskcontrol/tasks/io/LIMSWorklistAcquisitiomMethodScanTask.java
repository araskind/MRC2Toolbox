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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class LIMSWorklistAcquisitiomMethodScanTask extends WorklistTask {
	
	private TreeMap<String,File>methodMap;
	private Collection<File> rawDataFiles;

	//	TODO - handle different instrument vendors
	public LIMSWorklistAcquisitiomMethodScanTask(
			File inFile,
			WorklistImportType importType) {
		super(inFile, importType);
		taskDescription = "Scanning worklist data from " + sourceFileOrDirectory.getName();
		worklist = null;
		processed = 0;
		total = 100;
		rawDataFiles = new TreeSet<File>();
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
return;

			}
		}
		if(importType.equals(WorklistImportType.RAW_DATA_DIRECTORY_SCAN)) {

			methodMap = new TreeMap<String,File>();
			rawDataFiles = getAgiletDFileList(sourceFileOrDirectory);
			if (!rawDataFiles.isEmpty())
				collectMethodsFromAgilentRawDataFiles();				
		}
		if(importType.equals(WorklistImportType.PLAIN_TEXT_FILE)) {
			//	TODO
		}
		if(importType.equals(WorklistImportType.EXCEL_FILE)) {
			//	TODO
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void collectMethodsFromAgilentRawDataFiles() {		
		
		IOFileFilter dotMfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[mM]$"));				
		for(File dotd : rawDataFiles) {

			File acqDataDir = Paths.get(dotd.getAbsolutePath(), "AcqData").toFile();
			Collection<File> methodFiles = 
					FileUtils.listFilesAndDirs(acqDataDir, DirectoryFileFilter.DIRECTORY, dotMfilter);
			methodFiles.remove(acqDataDir);
			File mmf = methodFiles.stream().findFirst().orElse(null);
			if(mmf != null)
				methodMap.put(mmf.getName(), mmf);				
		}
	}

	@Override
	public Task cloneTask() {
		return new LIMSWorklistAcquisitiomMethodScanTask(
			sourceFileOrDirectory, importType);
	}
	
	public TreeMap<String, File> getMethodNameToFileMap() {
		return methodMap;
	}
}
