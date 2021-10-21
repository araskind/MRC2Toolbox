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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class FindDuplicateNamesTask extends AbstractTask {

	private DataPipeline dataPipeline;
	private DataAnalysisProject currentProject;
	private Collection<MsFeature> featureList;
	private Collection<MsFeatureCluster> duplicateNameList;

	public FindDuplicateNamesTask(
			DataAnalysisProject currentProject,
			DataPipeline dataPipeline) {

		this.dataPipeline = dataPipeline;
		this.currentProject = currentProject;
		taskDescription = "Finding duplicate names for " + 
				dataPipeline.getName();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			findDuplicateNames();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	public Collection<MsFeatureCluster> findDuplicateNames() {

		featureList =
				currentProject.getMsFeaturesForDataPipeline(dataPipeline);
		total = featureList.size();
		processed = 0;
		taskDescription = "Finding duplicates";
		duplicateNameList = new HashSet<MsFeatureCluster>();
		HashSet<MsFeature>assigned = new HashSet<MsFeature>();

		ArrayList<MsFeatureCluster> clusters = new ArrayList<MsFeatureCluster>();
		for (MsFeature cf : featureList) {

			for (MsFeatureCluster fClust : clusters) {

				if (fClust.nameMatches(cf, dataPipeline)) {

					fClust.addFeature(cf, dataPipeline);
					assigned.add(cf);
					break;
				}
			}
			if (!assigned.contains(cf)) {

				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(cf, dataPipeline);
				assigned.add(cf);
				clusters.add(newCluster);
			}
			processed++;
		}
		duplicateNameList = clusters.stream().
				filter(c -> c.getFeatures().size() > 1).
				collect(Collectors.toList());
		return duplicateNameList;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}

	public Collection<MsFeatureCluster> getDuplicateNameList() {
		return duplicateNameList;
	}

	@Override
	public Task cloneTask() {

		return new FindDuplicateNamesTask(
				currentProject, dataPipeline);
	}
}
