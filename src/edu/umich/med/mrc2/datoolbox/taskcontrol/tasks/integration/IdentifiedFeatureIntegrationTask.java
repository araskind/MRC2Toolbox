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

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IdentifiedFeatureIntegrationTask  extends AbstractTask {
	
	private Collection<DataPipeline>pipelines;
	private MsFeatureClusterSet clusterSet;
	private DataAnalysisProject currentProject;

	public IdentifiedFeatureIntegrationTask(
			Collection<DataPipeline>assays, 
			MsFeatureClusterSet integratedSet) {
		
		super();		
		this.pipelines = assays;
		this.clusterSet = integratedSet;
		taskDescription = 
				"Combining identified features from multiple assays";
		currentProject = MRC2ToolBoxCore.getCurrentProject();
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		try {
			createIdClusters();
		} 
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void createIdClusters() {
		
		TreeMap<String, MsFeatureCluster>idClusterMap = 
				new TreeMap<String, MsFeatureCluster>();
		for(DataPipeline pipeine : pipelines) {		
			
			Set<MsFeature> features = currentProject.getMsFeaturesForDataPipeline(pipeine);
			total = features.size();
			processed = 0;
			for(MsFeature f : features) {
					
					String label = f.getPrimaryIdentity().getPrimaryLinkLabel();			
					if(idClusterMap.get(label) == null)
						idClusterMap.put(label, new MsFeatureCluster());
					
					idClusterMap.get(label).addFeature(f,pipeine);
				
				processed++;
			}
		}
		clusterSet.clearClusters();
		clusterSet.addClusterCollection(idClusterMap.values());
	}

	public MsFeatureClusterSet getIdClusterSet() {		
		return clusterSet;
	}
	
	@Override
	public Task cloneTask() {
		return new IdentifiedFeatureIntegrationTask(pipelines, clusterSet);
	}
}

















