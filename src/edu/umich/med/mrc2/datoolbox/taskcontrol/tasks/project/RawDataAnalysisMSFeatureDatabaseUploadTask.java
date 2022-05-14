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

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class RawDataAnalysisMSFeatureDatabaseUploadTask extends AbstractTask {

	private RawDataAnalysisProject project;
	private DataFile dataFile;
	private double msOneMZWindow;
	
	public RawDataAnalysisMSFeatureDatabaseUploadTask(
			RawDataAnalysisProject project, 
			DataFile dataFile,
			double msOneMZWindow) {
		super();
		this.project = project;
		this.dataFile = dataFile;
		this.msOneMZWindow = msOneMZWindow;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			uploadMSMSFeatureData();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void uploadMSMSFeatureData() {
		// TODO Auto-generated method stub
		taskDescription = "Uploading results for " + dataFile.getName();
		Collection<MsFeatureInfoBundle> bundles = 
				project.getMsFeaturesForDataFile(dataFile);
		total = bundles.size();
		processed = 0;
		for(MsFeatureInfoBundle bundle : bundles) {
			
			//	Insert feature
			
			
			//	Insert chromatograms
			MsFeatureChromatogramBundle msfCb = 
					MRC2ToolBoxCore.getActiveRawDataAnalysisProject().
					getChromatogramMap().get(bundle.getMsFeature().getId());
			
			processed++;
		}
	}
	
	@Override
	public Task cloneTask() {

		return new RawDataAnalysisMSFeatureDatabaseUploadTask(
				project, 
				dataFile,
				msOneMZWindow);
	}


}
