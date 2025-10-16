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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerSiriusMsExportWithClusteringTask extends IDTrackerSiriusMsExportTask {

	private Collection<MSFeatureInfoBundle>featuresToExport;
	private double rtError;
	private double mzError;

	public IDTrackerSiriusMsExportWithClusteringTask(
			Collection<MSFeatureInfoBundle> featuresToExport,
			double rtError, 
			double mzError, 
			File outputFile) {
		super(outputFile);
		this.featuresToExport = featuresToExport;
		this.rtError = rtError;
		this.mzError = mzError;
		this.outputFile = outputFile;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);	
		try {
			createFeatureGroups();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			writeMsFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
	
	private void createFeatureGroups() {
		
		msmsclusters = new ArrayList<SiriusMsMsCluster>();
		MSFeatureInfoBundle[] msmsFeatures = 
				featuresToExport.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum())).
				filter(f -> Math.abs(f.getMsFeature().getCharge()) == 1).
				toArray(size -> new MSFeatureInfoBundle[size]);
		
		if(msmsFeatures.length == 0)
			return;
		
		taskDescription = "Grouping related features";
		total = msmsFeatures.length;
		processed = 1;
				
		SiriusMsMsCluster firstCluster = new SiriusMsMsCluster(msmsFeatures[0], rtError, mzError);
		msmsclusters.add(firstCluster);
		for(int i=1; i<msmsFeatures.length; i++) {
			
			boolean added = false;
			for(SiriusMsMsCluster cluster : msmsclusters) {
				
				if(cluster.addFeatureBundle(msmsFeatures[i])) {
					added = true;
					break;
				}				
			}
			if(!added) {
				SiriusMsMsCluster newCluster = new SiriusMsMsCluster(msmsFeatures[i], rtError, mzError);
				msmsclusters.add(newCluster);
			}
			processed++;
		}
	}
	
	@Override
	public Task cloneTask() {

		return new IDTrackerSiriusMsExportWithClusteringTask(
				 featuresToExport,
				 rtError,
				 mzError,
				 outputFile);
	}

}
