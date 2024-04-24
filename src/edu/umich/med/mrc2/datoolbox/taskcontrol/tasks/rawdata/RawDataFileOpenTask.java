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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;
import umich.ms.datatypes.LCMSData;

public class RawDataFileOpenTask extends AbstractTask implements TaskListener{

	private Collection<File> filesToOpen;
	private Collection<DataFile>newRawFiles;
	private int ticCount;
	
	
	public RawDataFileOpenTask(Collection<File> filesToOpen) {
		super();
		this.filesToOpen = filesToOpen;
		newRawFiles = new TreeSet<DataFile>();
		ticCount = 0;
	}
	
	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Opening selected raw data files ...";
		total = filesToOpen.size();
		processed = 0;		
		try {
			for(File rawFile : filesToOpen) {

				DataFile newDataFile = new DataFile(rawFile);
				LCMSData rawData = RawDataManager.getRawData(newDataFile);
				if(rawData != null) {
					newRawFiles.add(newDataFile);
					initTicExtraction(newDataFile);
				}
				processed++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);		
		}
	}
	
	private void initTicExtraction(DataFile dataFile) {
		
		Collection<Double> mzList = new ArrayList<Double>();
		ChromatogramExtractionTask xicTask = new ChromatogramExtractionTask(
				Collections.singleton(dataFile), 
				ChromatogramPlotMode.TIC, 
				null, 
				1, 
				mzList,
				false, 
				Double.NaN, 
				null, 
				null,
				true,
				SavitzkyGolayWidth.FIVE);
		xicTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(xicTask);
	}

	@Override
	public Task cloneTask() {
		return new RawDataFileOpenTask(filesToOpen);
	}

	public Collection<DataFile>getOpenedFiles(){
		return newRawFiles;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class)) {
				
				ticCount++;
				if(ticCount == filesToOpen.size())
					setStatus(TaskStatus.FINISHED);
			}
		}		
	}
}
