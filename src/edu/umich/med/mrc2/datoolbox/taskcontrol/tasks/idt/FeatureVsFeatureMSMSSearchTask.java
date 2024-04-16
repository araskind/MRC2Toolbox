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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class FeatureVsFeatureMSMSSearchTask extends AbstractTask implements TaskListener {
	
	private Collection<MSFeatureInfoBundle>inputFeatures;
	private MsFeatureInfoBundleCollection featureLib;
	private MSMSSearchParameterSet searchParameters;
	private Collection<IMsFeatureInfoBundleCluster>searchResults;
	private IMSMSClusterDataSet searchResultsDataSet;

	public FeatureVsFeatureMSMSSearchTask(
			Collection<MSFeatureInfoBundle> inputFeatures,
			MsFeatureInfoBundleCollection featureLib, 
			MSMSSearchParameterSet searchParameters) {
		super();
		this.inputFeatures = inputFeatures;
		this.featureLib = featureLib;
		this.searchParameters = searchParameters;
		searchResults = new ArrayList<IMsFeatureInfoBundleCluster>();
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		if(!mustFetchLibraryFeatures()) {
			
			try {
				searchFeaturesAgainstLibrary();				
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
	}
	
	private void searchFeaturesAgainstLibrary() {
		// TODO Auto-generated method stub
		taskDescription = "Running MSMS search against feature library";
		total = inputFeatures.size();
		processed = 0;
		
		
		setStatus(TaskStatus.FINISHED);
	}
	
	private void prepareSpectra() {
		
	}

	private boolean mustFetchLibraryFeatures() {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			IDTMSMSFeatureDataPullTask task = 
					FeatureCollectionManager.getMsFeatureInfoBundleCollectionData(featureLib);
			if(task == null) {
				 return false;
			}
			else {
				taskDescription = "Fetching library features";
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
				return true;
			}
		}
		else {
			 return false;
		}
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(IDTMSMSFeatureDataPullTask.class))
				searchFeaturesAgainstLibrary();			
		}
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new FeatureVsFeatureMSMSSearchTask(
				inputFeatures, featureLib, searchParameters);
	}

	public Collection<IMsFeatureInfoBundleCluster> getSearchResults() {
		return searchResults;
	}
}
