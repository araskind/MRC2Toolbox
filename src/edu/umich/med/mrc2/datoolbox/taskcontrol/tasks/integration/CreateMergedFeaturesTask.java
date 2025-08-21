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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class CreateMergedFeaturesTask extends AbstractTask {
	
	private DataAnalysisProject currentExperiment;
	private Set<MsFeatureCluster> clusterSet;
	private Matrix combinedFeaturesMatrix;
	private Matrix combinedDatasMatrix;
	private Map<MsFeatureCluster,LibraryMsFeature>mergedFeaturesMap;

	public CreateMergedFeaturesTask(
			DataAnalysisProject currentExperiment, 
			Set<MsFeatureCluster> clusterSet) {
		super();
		this.currentExperiment = currentExperiment;
		this.clusterSet = clusterSet;
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {
			createCombinedFeatures();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			createMatricesForCombinedFeatures();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		try {
			calculateCombinedFeaturesStats();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void createCombinedFeatures() {

		taskDescription = "Merging marked features";
		total = clusterSet.size();
		processed = 0;
		mergedFeaturesMap = new HashMap<MsFeatureCluster,LibraryMsFeature>();
		for(MsFeatureCluster cluster : clusterSet) {
			
			LibraryMsFeature merged = mergeMarkedFeatures(cluster);
			if(merged != null)
				mergedFeaturesMap.put(cluster, merged);

			processed++;
		}
	}
	
	private LibraryMsFeature mergeMarkedFeatures(MsFeatureCluster cluster) {
		
		if(cluster.getMarkedForMerge().size() < 2)
			return null;
		
		return null;
	}
	
	private void createMatricesForCombinedFeatures() {
		// TODO Auto-generated method stub
		
	}
	
	private void calculateCombinedFeaturesStats() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task cloneTask() {
		return new CreateMergedFeaturesTask(currentExperiment, clusterSet);
	}
}
