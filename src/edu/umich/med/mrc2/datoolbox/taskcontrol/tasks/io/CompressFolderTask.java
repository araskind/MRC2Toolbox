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

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;

public class CompressFolderTask extends AbstractTask {

	private File directoryToCompress;
	private File destinationFile;

	public CompressFolderTask(File directoryToCompress, File destinationFile) {
		super();
		this.directoryToCompress = directoryToCompress;
		this.destinationFile = destinationFile;
	}

	@Override
	public void run() {

		if(directoryToCompress == null) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!directoryToCompress.exists()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!directoryToCompress.isDirectory()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		taskDescription = "Compressing "+ directoryToCompress.getName() +" ...";
		total = 100;
		processed = 40;
		setStatus(TaskStatus.PROCESSING);
		try {
			CompressionUtils.zipFolder(directoryToCompress, destinationFile);
			processed = 100;
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}

	@Override
	public Task cloneTask() {
		return new CompressFolderTask(directoryToCompress, destinationFile);
	}

	/**
	 * @return the destinationFile
	 */
	public File getDestinationFile() {
		return destinationFile;
	}
}
