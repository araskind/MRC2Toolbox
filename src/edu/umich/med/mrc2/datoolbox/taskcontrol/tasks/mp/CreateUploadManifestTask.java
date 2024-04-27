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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class CreateUploadManifestTask extends AbstractTask {

	private File batchDirectory;
	private String assayName;
	private Date manifestDate;
	private Collection<String>manifestData;
	
	public static final DateFormat defaultDateFormat = 
			new SimpleDateFormat("yyyyMMdd");
		
	public CreateUploadManifestTask(
			File batchDirectory, 
			Date manifestDate,
			String assayName) {
		super();
		this.batchDirectory = batchDirectory;
		this.manifestDate = manifestDate;
		this.assayName = assayName;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		manifestData = new ArrayList<String>();
		manifestData.add("file_name,md5");
		try {
			parseProcessedFolders();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			parseRawDataFolder();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			writeManifestFile();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void parseProcessedFolders() throws IOException {
		
		taskDescription = "Processing results folder(s) for " + assayName;
		Path batchPath = Paths.get(batchDirectory.getAbsolutePath());
		List<Path> processedPathList = new ArrayList<Path>();
		try {
			processedPathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> 
					(filePath.getFileName().toString().toUpperCase().startsWith("PROCESSED_"))
					&& fileAttr.isDirectory()).
				collect(Collectors.toList());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<Path> pathList = null;
		for(Path processedPath : processedPathList) {
			
			String processedFolder = processedPath.getFileName().toString();
			Path namedDirPath = Paths.get(processedPath.toString(), "NAMED");
			pathList = Files.find(namedDirPath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
				collect(Collectors.toList());
			for(Path filePath : pathList) {
				String zipHash = DigestUtils.sha256Hex(new FileInputStream(filePath.toString()));
				String localPath = Paths.get(
						processedFolder, "NAMED", filePath.getFileName().toString()).toString();
				manifestData.add(localPath + "," + zipHash);
			}
			Path unnamedDirPath = Paths.get(processedPath.toString(), "UNNAMED");
			pathList = Files.find(unnamedDirPath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".txt"))).
				collect(Collectors.toList());
			for(Path filePath : pathList) {
				String zipHash = DigestUtils.sha256Hex(new FileInputStream(filePath.toString()));
				String localPath = Paths.get(
						processedFolder, "UNNAMED", filePath.getFileName().toString()).toString();
				manifestData.add(localPath + "," + zipHash);
			}
			pathList = Files.find(processedPath,
					1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_failedsamples_"))).
				collect(Collectors.toList());
			for(Path filePath : pathList) {
				String zipHash = DigestUtils.sha256Hex(new FileInputStream(filePath.toString()));
				String localPath = Paths.get(
						processedFolder, filePath.getFileName().toString()).toString();
				manifestData.add(localPath + "," + zipHash);
			}
		}
		pathList = Files.find(batchPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().contains("metadata_phase"))).
			collect(Collectors.toList());
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			manifestData.add(filePath.toFile().getName() + "," + zipHash);
		}
	}
	
	private void parseRawDataFolder() throws FileNotFoundException, IOException {
		
		taskDescription = "Processing raw data folder for " + assayName;
		Path rawDirPath = Paths.get(batchDirectory.getAbsolutePath(), "RAW");
		List<Path> pathList = Files.find(rawDirPath,
				1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".zip"))).
			collect(Collectors.toList());
		
		total = pathList.size();
		processed = 0;
		for(Path filePath : pathList) {
			String zipHash = DigestUtils.sha256Hex(
					new FileInputStream(filePath.toString()));
			String localPath = filePath.toFile().getName().replace(".zip", "");
			manifestData.add(localPath + "," + zipHash);
			processed++;
		}
	}
	
	private void writeManifestFile() {
		
		if(isCanceled())
			return;
			
		Path outputPath = Paths.get(
				batchDirectory.getAbsolutePath(), 
				"file_manifest_" + defaultDateFormat.format(manifestDate) + ".csv");
		try {
			Files.write(outputPath, 
					manifestData, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public Task cloneTask() {

		return new CreateUploadManifestTask(
				 batchDirectory, 
				 manifestDate,
				 assayName);
	}
}
