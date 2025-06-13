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
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.MsFeatureChromatogramExtractionTarget;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;

public class ImportMS1DataFromCEFTask extends CEFProcessingTask implements TaskListener{
		
	private DataFile df;
	private ChromatogramDefinition ccd;
	Collection<MSFeatureInfoBundle>featureBundles;
	private Map<String, MsFeatureChromatogramBundle> chromMap;
	
	public ImportMS1DataFromCEFTask(
			File inputCef, 
			DataFile df, 
			ChromatogramDefinition ccd) {
		super();
		inputCefFile = inputCef;
		this.df = df;
		this.ccd = ccd;
	}

	@Override
	public void run() {

		taskDescription = "Importing data from " + inputCefFile.getName();
		setStatus(TaskStatus.PROCESSING);
		try {
			parseInputCefFile(inputCefFile);
		}
		catch (Exception e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		featureBundles = new ArrayList<MSFeatureInfoBundle>();
		for(MsFeature msFeature : inputFeatureList) {
			MSFeatureInfoBundle b = new MSFeatureInfoBundle(msFeature);
			b.setAcquisitionMethod(df.getDataAcquisitionMethod());
			b.setInjectionId(df.getInjectionId());
			featureBundles.add(b);
			
			//	TODO add extra data?
		}
		MsFeatureChromatogramBatchExtractionTask ceTask = 
				new MsFeatureChromatogramBatchExtractionTask(
				Collections.singleton(df),
				featureBundles, 
				ccd,
				MsFeatureChromatogramExtractionTarget.MS1PrimaryAdduct,
				false);
		ceTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(ceTask);
	}
	
	@Override
	public Task cloneTask() {
		return new ImportMS1DataFromCEFTask(inputCefFile, df, ccd);
	}

	public DataFile getDataFile() {
		return df;
	}

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(MsFeatureChromatogramBatchExtractionTask.class))				
				finalizeMsFeatureChromatogramBatchExtractionTask(
						(MsFeatureChromatogramBatchExtractionTask)e.getSource());		
		}
	}
	
	private synchronized void finalizeMsFeatureChromatogramBatchExtractionTask(
			MsFeatureChromatogramBatchExtractionTask task) {
		
		chromMap = task.getChromatogramMap();
		setStatus(TaskStatus.FINISHED);
	}

	public Collection<MSFeatureInfoBundle> getFeatureBundles() {
		return featureBundles;
	}

	public Map<String, MsFeatureChromatogramBundle> getChromMap() {
		return chromMap;
	}
}












