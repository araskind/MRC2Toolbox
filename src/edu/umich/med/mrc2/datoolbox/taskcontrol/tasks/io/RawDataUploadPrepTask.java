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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;

public class RawDataUploadPrepTask extends AbstractTask implements TaskListener{

	private String rawDataDirectory;
	private String zipDirectory;
	private boolean recursiveScan;
	private Map<File,Long>fileSizeMap;
	private Map<File,String>fileHashMap;
	private int dataFileCount;
	private List<Path> pathList;
	private boolean compressFiles;

	public RawDataUploadPrepTask(
			String rawDataDirectory, 
			String zipDirectory,  
			boolean recursiveScan,
			boolean compressFiles) {
		super();
		this.rawDataDirectory = rawDataDirectory;
		this.zipDirectory = zipDirectory;
		this.recursiveScan = recursiveScan;
		this.compressFiles = compressFiles;

		fileSizeMap = new TreeMap<File,Long>();
		fileHashMap = new TreeMap<File,String>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		pathList = new ArrayList<Path>();
		try {
			getPathList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(pathList.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		try {
			deleteResultsFolders();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		if(compressFiles) {
			try {
				zipAndHashRawData();
			}
			catch (Exception e1) {
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		else {
			setStatus(TaskStatus.FINISHED);
			return;
		}
	}
	
	private void getPathList() throws IOException {
		
		int scanDepth = 1;
		if(recursiveScan)
			 scanDepth = Integer.MAX_VALUE;
		
		pathList = Files.find(Paths.get(rawDataDirectory),
				scanDepth, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
			collect(Collectors.toList());
	}

	private void deleteResultsFolders() throws IOException {

		taskDescription = "Deleting \"Results\" folders ...";
		total = pathList.size();
		processed = 0;
		for(Path p : pathList) {
			FileUtils.deleteDirectory(Paths.get(p.toString(), "Results").toFile());
			processed++;
		}
	}

	private void zipAndHashRawData() throws IOException, ArchiveException {

		taskDescription = "Compressing data folders and calculating checksums ...";
		total = pathList.size();
		processed = 0;
		File zipDir = CompressionUtils.createOrRetrieveDirectory(zipDirectory);
		for(Path p : pathList) {

			File destination = Paths.get(zipDir.getAbsolutePath(),
					FilenameUtils.getBaseName(p.toString()) + ".zip").toFile();
			CompressFolderTask task = new CompressFolderTask(p.toFile(), destination);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}

	//	TODO combine manifest and checksum
	private void writeCheckSumFile() throws IOException {

		File zipDir = Paths.get(zipDirectory).toFile();
		if(zipDir.exists()) {

			File checkSumFile = Paths.get(zipDir.getAbsolutePath(), "checksum.txt").toFile();
			StringBuffer checkSumData = new StringBuffer();
			checkSumData.append("File name\t");
			checkSumData.append("SHA256\t");
			checkSumData.append("Size\n");

			for(Entry<File, String> entry : fileHashMap.entrySet()) {
				
				if(fileSizeMap.get(entry.getKey()) != null) {
					
					checkSumData.append(entry.getKey().getName() +"\t");
					checkSumData.append(entry.getValue() +"\t");
					checkSumData.append(Long.toString(fileSizeMap.get(entry.getKey())) + "\n");
				}
			}
			FileUtils.writeStringToFile(checkSumFile, checkSumData.toString(), Charset.defaultCharset());
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {
		return new RawDataUploadPrepTask(
				rawDataDirectory, 
				zipDirectory, 
				recursiveScan, 
				compressFiles);
	}

	/**
	 * @return the fileSizeMap
	 */
	public Map<File, Long> getFileSizeMap() {
		return fileSizeMap;
	}

	/**
	 * @return the fileHashMap
	 */
	public Map<File, String> getFileHashMap() {
		return fileHashMap;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(CompressFolderTask.class)) {

				CompressFolderTask task = (CompressFolderTask)e.getSource();
				File destination = task.getDestinationFile();
				if(destination.exists()) {
					try {
						String zipHash = DigestUtils.sha256Hex(
								new FileInputStream(destination.getAbsolutePath()));

						fileHashMap.put(destination, zipHash);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					fileSizeMap.put(destination, destination.length());
					processed++;

					if(processed == total) {
						try {
							writeCheckSumFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}
}










