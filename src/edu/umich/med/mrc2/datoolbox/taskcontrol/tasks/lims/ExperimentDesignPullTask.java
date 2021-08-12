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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ExperimentDesignPullTask  extends AbstractTask {

	private LIMSExperiment experiment;
	private ExperimentDesign design;

	public ExperimentDesignPullTask(LIMSExperiment selected) {
		super();
		this.experiment = selected;
	}

	@Override
	public Task cloneTask() {
		return new ExperimentDesignPullTask(experiment);
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Getting experiment design data for " + experiment.getName();
		total = 100;
		processed = 30;
		try {
			design = LIMSUtils.getDesignForExperiment(experiment.getId());
			if(design != null)
				experiment.setDesign(design);

			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}

	/**
	 * @return the design
	 */
	public ExperimentDesign getDesign() {
		return design;
	}
}