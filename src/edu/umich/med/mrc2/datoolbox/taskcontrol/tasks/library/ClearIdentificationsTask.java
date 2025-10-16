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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ClearIdentificationsTask  extends AbstractTask {


	public ClearIdentificationsTask() {
		super();
		taskDescription = "Clearing identification data ...";
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		Set<MsFeature> featureSet = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getMsFeaturesForDataPipeline(
						MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline());
//				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
//				getActiveFeatureSetForDataPipeline(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline()).
//				getFeatures().stream().filter(f -> f.isIdentified()).collect(Collectors.toSet());

		total = featureSet.size();
		processed = 0;
		featureSet.stream().forEach(f -> {
			f.setSuppressEvents(true);
			f.clearIdentification();
			f.setSuppressEvents(false);
			processed++;
		});
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {

		return new ClearIdentificationsTask();
	}
}
