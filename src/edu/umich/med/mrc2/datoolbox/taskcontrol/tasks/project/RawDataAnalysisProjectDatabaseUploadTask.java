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

import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class RawDataAnalysisProjectDatabaseUploadTask extends AbstractTask implements TaskListener {

	private RawDataAnalysisProject project;
	private double msOneMZWindow;
	private int processedFiles;
	
	public RawDataAnalysisProjectDatabaseUploadTask(
			RawDataAnalysisProject project,
			double msOneMZWindow) {
		super();
		this.project = project;
		this.msOneMZWindow = msOneMZWindow;
		processedFiles = 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		try {
			uploadExperimentMetadata();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			initFeatureDataUpload();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}
	
	private void uploadExperimentMetadata() {
		
		taskDescription = "Uploading metadata ...";
		total = 100;
		processed = 20;
		
		//	Add experiment
		if(!insertNewExperiment()) {
			setStatus(TaskStatus.ERROR);
			return;
		}	
		if(!insertSamples()) {
			setStatus(TaskStatus.ERROR);
			return;
		}	
	}
	
	private boolean insertSamples() {
		
		TreeSet<ExperimentalSample> samples = 
				project.getIdTrackerExperiment().getExperimentDesign().getSamples();
		for(ExperimentalSample sample : samples) {
			
			ExperimentalSample existingSample = null;
			if(sample.getId() != null) {
				try {
					existingSample = IDTUtils.getExperimentalSampleById(sample.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(existingSample == null) {
				
				try {
					String sampleId = IDTUtils.addNewIDTSample(
							(IDTExperimentalSample) sample, project.getIdTrackerExperiment());
					sample.setId(sampleId);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}		
		}		
		return true;
	}
	
	private boolean insertNewExperiment() {
		
		LIMSExperiment newExperiment = project.getIdTrackerExperiment();
		String experimentId = null;
		try {
			experimentId = IDTUtils.addNewExperiment(newExperiment);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(experimentId != null) {
			newExperiment.setId(experimentId);
			IDTDataCash.getExperiments().add(newExperiment);
			newExperiment.getProject().getExperiments().add(newExperiment);
			return true;
		}
		else {
			return false;
		}		
	}
	
	private void initFeatureDataUpload() {
		
		taskDescription = "Uploading MSMS features and chromatograms ...";
		total = 100;
		processed = 40;		
		for(DataFile df : project.getMSMSDataFiles()) {
			
			RawDataAnalysisMSFeatureDatabaseUploadTask task = 				
					new RawDataAnalysisMSFeatureDatabaseUploadTask(project, df, msOneMZWindow);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	@Override
	public Task cloneTask() {
		return new RawDataAnalysisProjectDatabaseUploadTask(
				project, msOneMZWindow);
	}

	public RawDataAnalysisProject getProject() {
		return project;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(OpenMsFeatureBundleFileTask.class))	
				processedFiles++;
			
			if(processedFiles == project.getMSMSDataFiles().size()) {
				setStatus(TaskStatus.FINISHED);
				return;
			}
		}		
	}
}