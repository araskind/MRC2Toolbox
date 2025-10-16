/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.io.File;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.MsConvertOutputFormat;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class RawDataBatchCoversionTask extends AbstractTask implements TaskListener {
	
	private File outputDir ;
	private Collection<File>filesToConvert;
	private MsConvertOutputFormat format;

	public RawDataBatchCoversionTask(
			File outputDir, 
			Collection<File> filesToConvert, 
			MsConvertOutputFormat format) {
		super();
		this.outputDir = outputDir;
		this.filesToConvert = filesToConvert;
		this.format = format;
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Converting selected raw data files ...";
		total = filesToConvert.size();
		processed = 0;		
		try {
			for(File rawFile : filesToConvert) {

				RawDataConversionTask task = 
						new RawDataConversionTask(outputDir, rawFile, format);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;
		
		}
	}
	
	@Override
	public Task cloneTask() {
		return new RawDataBatchCoversionTask(
				outputDir, filesToConvert, format);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(RawDataConversionTask.class))
				 finalizeRawDataConversionTask((RawDataConversionTask)e.getSource());
		}
	}
	
	private synchronized void finalizeRawDataConversionTask(RawDataConversionTask task) {
		
		MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(task);
		processed++;
		if(processed == total) {
//			taskDescription = "Re-indexing raw data repository ...";
//			RawDataManager.indexRepository();
			setStatus(TaskStatus.FINISHED);
		}
	}
}
