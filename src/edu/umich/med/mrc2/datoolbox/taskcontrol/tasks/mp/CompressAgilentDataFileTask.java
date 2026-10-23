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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;

public class CompressAgilentDataFileTask extends AbstractTask {
	
	private static final Logger logger = LogManager.getLogger(CompressAgilentDataFileTask.class);
	
	private Path destinationPath;
	private Path rawFilePath;
	private File destination;
	private String zipHash = "";

	public CompressAgilentDataFileTask(Path destinationPath, Path rawFilePath) {
		super();
		this.destinationPath = destinationPath;
		this.rawFilePath = rawFilePath;
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			compressAgiletDataFile();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void compressAgiletDataFile() {
		
		if(isCanceled())
			return;
		
		String pathString = rawFilePath.toString();	
		try {
			FileUtils.deleteDirectory(Paths.get(pathString, "Results").toFile());
		} catch (IOException e) {
			logger.error(String.format(
					"Failed to delete Results directory in  %s", getRawFileName()), e);
		}
		destination = Paths.get(destinationPath.toString(),
				FilenameUtils.getBaseName(pathString) + ".zip").toFile();
		try {
			CompressionUtils.zipFolder(rawFilePath.toFile(), destination);
		} catch (IOException e) {
			logger.error(String.format(
					"Failed to compress  %s", rawFilePath.toString()), e);
		}
		if(destination.exists()) {
			//	Checksum	
			try {
				zipHash = DigestUtils.sha256Hex(new FileInputStream(destination));
			} catch (IOException e) {
				logger.error(String.format(
						"Failed to calculate MD5 for  %s", destination.toString()), e);
			}
		}
		processed++;
	}
	
	public String getRawFileName() {
		return PathUtils.getBaseName(rawFilePath);
	}
	
	public String getCheckSum() {
		return zipHash;
	}
	
	@Override
	public Task cloneTask() {
		return new CompressAgilentDataFileTask(destinationPath, rawFilePath);
	}

	public File getDestination() {
		return destination;
	}
}
