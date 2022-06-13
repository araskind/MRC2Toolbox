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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MSMSFeatureClusteringTask extends AbstractTask {
	
	private Collection<MsFeatureInfoBundle> msmsFeatures;
	private Collection<MsFeatureInfoBundle> filteredMsmsFeatures;
	private MSMSClusteringParameterSet params;
	private Collection<MinimalMSOneFeature> lookupFeatures;
	private Collection<MsFeatureInfoBundleCluster>featureClusters;
	private MSMSClusterDataSet msmsClusterDataSet;
	
	public MSMSFeatureClusteringTask(
			Collection<MsFeatureInfoBundle> msmsFeatures, 
			MSMSClusteringParameterSet params,
			Collection<MinimalMSOneFeature> lookupFeatures) {
		super();
		this.msmsFeatures = msmsFeatures;
		this.params = params;
		this.lookupFeatures = lookupFeatures;
		
		msmsClusterDataSet = new MSMSClusterDataSet(
				"Active data set", 
				"", 
				MRC2ToolBoxCore.getIdTrackerUser(), 
				new Date());
		msmsClusterDataSet.setParameters(params);	
		featureClusters = msmsClusterDataSet.getClusters();
	}

	@Override
	public void run() {
		taskDescription = "Clustering selected MSMS features";
		setStatus(TaskStatus.PROCESSING);
		if(lookupFeatures != null && !lookupFeatures.isEmpty()) {
			try {
				filterFeaturesForClustering();
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
		else {
			filteredMsmsFeatures = msmsFeatures;
		}
		if(!filteredMsmsFeatures.isEmpty()) {
			try {
				clusterMSMSFeatures();
			}
			catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void filterFeaturesForClustering() {
		// TODO Auto-generated method stub
		taskDescription = "Filtering MSMS features for clustering ...";
		total = msmsFeatures.size();
		processed = 0;
		filteredMsmsFeatures = new HashSet<MsFeatureInfoBundle>();
		for(MsFeatureInfoBundle b : msmsFeatures) {
			
			if(hasMatch(b))
				filteredMsmsFeatures.add(b);
			
			processed++;
		}
	}
	
	private void clusterMSMSFeatures() {
		
		taskDescription = "Clustering MSMS features ...";
		total = filteredMsmsFeatures.size();
		processed = 0;
		
		for(MsFeatureInfoBundle b : filteredMsmsFeatures) {
			
			
			processed++;
		}
	}
	
	private boolean hasMatch(MsFeatureInfoBundle b) {
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Task cloneTask() {
		
		return new MSMSFeatureClusteringTask(
				msmsFeatures, params, lookupFeatures);
	}

	public Collection<MinimalMSOneFeature> getLookupFeatures() {
		return lookupFeatures;
	}

	public MSMSClusterDataSet getMsmsClusterDataSet() {
		return msmsClusterDataSet;
	}
}





