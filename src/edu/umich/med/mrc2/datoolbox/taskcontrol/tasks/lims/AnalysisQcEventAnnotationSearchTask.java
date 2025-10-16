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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims;

import java.util.Date;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;

public class AnalysisQcEventAnnotationSearchTask extends AbstractTask {

	private QcEventType annotationCategory;
	private LIMSExperiment experiment;
	private Assay assay;
	private ExperimentalSample sample;
	private Date startDate;
	private Date endDate;
	private LIMSUser author;

	public AnalysisQcEventAnnotationSearchTask(
			QcEventType annotationCategory,
			LIMSExperiment experiment,
			Assay assay,
			ExperimentalSample sample,
			Date startDate,
			Date endDate,
			LIMSUser author) {
		super();
		this.annotationCategory = annotationCategory;
		this.experiment = experiment;
		this.assay = assay;
		this.sample = sample;
		this.startDate = startDate;
		this.endDate = endDate;
		this.author = author;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public Task cloneTask() {

		return new AnalysisQcEventAnnotationSearchTask(
				 annotationCategory,
				 experiment,
				 assay,
				 sample,
				 startDate,
				 endDate,
				 author);
	}



}
