/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class AgilentDataCompressionTask extends AbstractTask implements TaskListener{

	private static final Logger logger = LogManager.getLogger(AgilentDataCompressionTask.class);
	
	private File inputFileList;
	private Collection<String>inputFileNames;
	private Collection<String>processedFileNames;
	private Collection<File> rawDataDirectories;	
	private File destinationDir;
	private Collection<Path>rawFilesPathList;
	private String assayName;
	private Collection<String>fileCheckSums;
	private int compressedCount;
		
	public AgilentDataCompressionTask(
			File inputFileList, 
			Collection<File> rawDataDirectories, 
			File destinationDir,
			String assayName) {
		super();
		this.inputFileList = inputFileList;
		this.rawDataDirectories = rawDataDirectories;
		this.destinationDir = destinationDir;
		this.assayName = assayName;
		fileCheckSums = new ArrayList<>();
		compressedCount = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseInputFileNamesList();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			collectFilesForCompression();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			compressFiles();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
	
	private void parseInputFileNamesList() {
		
		inputFileNames = new TreeSet<>();
		Path includeFileListPath = Paths.get(inputFileList.getAbsolutePath());
		try {
			inputFileNames = Files.readAllLines(includeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	private void collectFilesForCompression() {
		
		taskDescription = "Collecting .D files for compression for " + assayName;
		total = rawDataDirectories.size();
		processed = 0;
		rawFilesPathList = new TreeSet<>();
		Set<Path>completePathList = new TreeSet<>();
		for(File sourceDir : rawDataDirectories) {
			
			if(isCanceled())
				return;
			
			Path sourcePath = Paths.get(sourceDir.getAbsolutePath());
			List<Path> pathList = new ArrayList<>();
			try (Stream<Path> stream = Files.find(sourcePath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d")))){
				pathList = stream.collect(Collectors.toList());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if(pathList != null && !pathList.isEmpty())
				completePathList.addAll(pathList);
			
			processed++;
		}
		for(Path rawFilePath : completePathList) {
			
			String rawFileName = PathUtils.getBaseName(rawFilePath);
			if(inputFileNames.contains(rawFileName))
				rawFilesPathList.add(rawFilePath);
		}		
	}
	
	private void compressFiles() {
		
		taskDescription = "Compressing raw data for " + assayName;
		total = 100;
		processed = 20;
		
		processedFileNames = new TreeSet<>();		
		Path destinationPath = Paths.get(destinationDir.getAbsolutePath());		
		for(Path rawFilePath : rawFilesPathList) {
			
			if(isCanceled())
				return;
			
			CompressAgilentDataFileTask task = 
					new CompressAgilentDataFileTask(destinationPath, rawFilePath);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);		
		}
	}
	
	@Override
	public void statusChanged(TaskEvent e) {
		
	    if (e.getStatus() == TaskStatus.FINISHED) {

	        ((AbstractTask)e.getSource()).removeTaskListener(this);

	        if (e.getSource().getClass().equals(CompressAgilentDataFileTask.class)) {

	        	compressedCount++;
	        	CompressAgilentDataFileTask task = (CompressAgilentDataFileTask)e.getSource();
	        	processedFileNames.add(task.getRawFileName());
	        	fileCheckSums.add("RAW/" + task.getDestination().getName() + "," + task.getCheckSum());        	
	        	if(compressedCount == rawFilesPathList.size()) {
	        		try {
	        			writeLog();
	        		} catch (Exception ex) {
	        			ex.printStackTrace();
	        			setStatus(TaskStatus.ERROR);
	        			return;
	        		}
	        		try {
	        			writeCheckSumFile();
	        		} catch (Exception ex) {
	        			ex.printStackTrace();
	        			setStatus(TaskStatus.ERROR);
	        			return;
	        		}
	        		setStatus(TaskStatus.FINISHED);
	        	}	        		
	        }
	    }	
	}
	
	private void writeLog() {
		
		if(isCanceled())
			return;
		
		Set<String> missingfiles = inputFileNames.stream().
				filter(f -> !processedFileNames.contains(f)).
				collect(Collectors.toSet());
		if(!missingfiles.isEmpty()) {
			
			Path outputPath = Paths.get(
					inputFileList.getParentFile().getAbsolutePath(), 
					FilenameUtils.getBaseName(inputFileList.getName()) + "_MISSING_RAW_FILES.TXT");
			try {
				Files.write(outputPath, 
						missingfiles, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void writeCheckSumFile() {
		
		Path checkSumPath = 
				Paths.get(destinationDir.getAbsolutePath(), "raw_data_checksums.txt");
		try {
			Files.write(
					checkSumPath, 
					fileCheckSums, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Task cloneTask() {

		return new AgilentDataCompressionTask(
				 inputFileList, 
				 rawDataDirectories, 
				 destinationDir,
				 assayName);
	}

}
