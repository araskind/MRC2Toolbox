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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;

import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.zip.ParallelZip;

public class OpenStoredRawDataAnalysisProjectTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisProject project;
	private File projectFile;
	private File xmlFile;
	private Document projectDocument;
	private File xmlTmpDir;
	private int featureFileCount;
	private int processedFiles;
	public OpenStoredRawDataAnalysisProjectTask(File projectFile) {

		this.projectFile = projectFile;		

	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 0;
		processedFiles = 0;
		try {
			Path tmpDir = Paths.get(projectFile.getParentFile().getAbsolutePath(), "xmlpParts");
			xmlTmpDir = tmpDir.toFile();
			xmlTmpDir.mkdirs();			
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			extractProject();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		Iterator<File> i = 
				FileUtils.iterateFiles(xmlTmpDir, new String[] { "xml" }, true);
		Collection<File>featureFiles = new ArrayList<File>();
		while (i.hasNext()) {
			
			File file = i.next();
			if(FilenameUtils.getBaseName(file.getName()).equals("Project")) {
				parseProjectFile(file);
			}
			else {
				featureFiles.add(file);
			}
		}
		featureFileCount = featureFiles.size();
		if(featureFileCount == 0)
			cleanup();
		else {
			for(File ff : featureFiles) {
				
			}
		}
	}
	
	private void parseProjectFile(File file) {
		// TODO Auto-generated method stub
		
	}

	private void extractProject() throws Exception {
		taskDescription = "Extracting project files ...";
		processed = 10;
		ParallelZip.extractZip(
				projectFile.getAbsolutePath(), 
				xmlTmpDir.getAbsolutePath());
	}

	@Override
	public Task cloneTask() {
		return new OpenStoredRawDataAnalysisProjectTask(projectFile);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(SaveFileMsFeaturesTask.class)) {	
				processedFiles++;
				if(processedFiles == featureFileCount) {
					cleanup();
				}				
			}
		}
	}

	private void cleanup() {
				
		if(xmlTmpDir != null) {
			try {
				FileUtils.deleteDirectory(xmlTmpDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
}
