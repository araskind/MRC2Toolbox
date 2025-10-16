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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import com.google.common.io.Files;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import umich.ms.datatypes.LCMSData;

public class ExperimentRawDataFileOpenTask extends AbstractTask implements TaskListener{


	private int ticCount;
	private RawDataAnalysisProject experiment;
	private boolean copyFilesToExperiment;
	private Collection<String>errors;
	
	
	public ExperimentRawDataFileOpenTask(
			RawDataAnalysisProject experiment,
			boolean copyFilesToExperiment) {
		super();
		this.experiment = experiment;
		this.copyFilesToExperiment = copyFilesToExperiment;
		ticCount = 0;
		errors = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		if(copyFilesToExperiment)
			copyDataFiles();
		
		loadRawData();
		initTicExtraction();
	}
	
	private void copyDataFiles() {
		
		taskDescription = "Copying raw data files to experiment directory ...";
		TreeSet<DataFile>filesToCopy = new TreeSet<DataFile>();
		for(DataFile df : experiment.getDataFiles()) {
			
			File existingFile = 
					Paths.get(experiment.getRawDataDirectory().getAbsolutePath(), 
					df.getName()).toFile();
			if(!existingFile.exists())
				filesToCopy.add(df);			
		}
		if(filesToCopy.isEmpty())
			return;
		
		total = filesToCopy.size();
		processed = 0;		
		for(DataFile df : filesToCopy) {
			File source = Paths.get(df.getFullPath()).toFile();
			File destination = Paths.get(experiment.getRawDataDirectory().getAbsolutePath(), 
					df.getName()).toFile();
			try {
				Files.copy(source, destination);
			} catch (IOException e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
			df.setFullPath(destination.getAbsolutePath());
			processed++;
		}
	}
	
	private synchronized void loadRawData() {
		
		taskDescription = "Loading raw data files for experiment ...";
		total = experiment.getDataFiles().size();
		processed = 0;	
		
		for(DataFile df : experiment.getMSMSDataFiles()) {
			
			LCMSData dataSource = RawDataManager.getRawData(df);
			if(!dataSource.getScans().getMapMsLevel2index().containsKey(2)) {
				errors.add("Data file \"" + df.getName() + 
						"\" does not contatin MSMS data; not included in the experiment.");
				experiment.removeMSMSDataFile(df);
				RawDataManager.removeDataSource(df);
			}
			processed++;
		}
		for(DataFile df : experiment.getMSOneDataFiles()) {
			
			LCMSData dataSource = RawDataManager.getRawData(df);
			if(dataSource.getScans().getMapMsLevel2index().containsKey(2)) {
				errors.add("Data file \"" + df.getName() + 
						"\" contatin MSMS data and can not be used as MS1 "
						+ "reference file; not included in the experiment.");
				experiment.removeMSOneDataFile(df);
				RawDataManager.removeDataSource(df);
			}
			processed++;
		}
	}
		
	private void initTicExtraction() {
		
		taskDescription = "Extracting TICs ...";
		total = experiment.getDataFiles().size();
		processed = 1;	
		Collection<Double> mzList = new ArrayList<Double>();
		int fileCount = 0;
		for(DataFile df : experiment.getDataFiles()) {
			
			df.setColor(ColorUtils.getColor(fileCount));			
			ChromatogramExtractionTask xicTask = 
					new ChromatogramExtractionTask(
							Collections.singleton(df), 
							ChromatogramPlotMode.TIC, 
							null, 
							1, 
							mzList,
							false, 
							Double.NaN, 
							null, 
							null,
							false,
							null);
			xicTask.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(xicTask);
			fileCount++;
		}
	}

	@Override
	public Task cloneTask() {
		return new ExperimentRawDataFileOpenTask(
				experiment, copyFilesToExperiment);
	}

	public RawDataAnalysisProject getExperiment(){
		return experiment;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class))
				finalizeChromatogramExtractionTask((ChromatogramExtractionTask)e.getSource());
		}		
	}
	
	private synchronized void finalizeChromatogramExtractionTask(ChromatogramExtractionTask task) {
		
		ticCount++;
		if(ticCount == experiment.getDataFiles().size())
			setStatus(TaskStatus.FINISHED);
	}

	public Collection<String> getErrors() {
		return errors;
	}
}
