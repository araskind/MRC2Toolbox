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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.BinnerReportParserTask;

public class ImportBinnerAnnotationsForUntargetedDataTask extends BinnerReportParserTask {

	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline; 
	private Collection<BinnerAnnotation> unassignedAnnotations;
	
	public ImportBinnerAnnotationsForUntargetedDataTask(
			File binnerDataFile,
			DataAnalysisProject currentExperiment,
			DataPipeline activeDataPipeline) {
		super();
		this.binnerDataFile = binnerDataFile;
		this.currentExperiment = currentExperiment;
		this.activeDataPipeline = activeDataPipeline;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if(binnerDataFile != null) {
			try {
				parseBinnerResults();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		if(binnerAnnotations == null || binnerAnnotations.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		attachBinnerAnnotationsToFeatures();
		setStatus(TaskStatus.FINISHED);
	}
	
	private void attachBinnerAnnotationsToFeatures() {

		taskDescription = "Attaching Binner annotations to MS features ...";
		total = binnerAnnotations.size();
		processed = 0;
		Collection<BinnerAnnotation> assignedAnnotations = new ArrayList<BinnerAnnotation>();
		unassignedAnnotations = new ArrayList<BinnerAnnotation>();
		//	unassignedAnnotations
		Set<MsFeature> features = 
				currentExperiment.getMsFeaturesForDataPipeline(activeDataPipeline);
		for(BinnerAnnotation ba : binnerAnnotations) {
			
			MsFeature target = features.stream().
					filter(f -> f.getName().equals(ba.getFeatureName())).
					findFirst().orElse(null);
			if(target != null) {
				target.setBinnerAnnotation(ba);
				assignedAnnotations.add(ba);
			}			
			processed++;
		}
		if(assignedAnnotations.isEmpty()) {
			errorMessage = "No annotations matching to any of the features";
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(binnerAnnotations.size() > assignedAnnotations.size())			
			unassignedAnnotations = 
					CollectionUtils.subtract(binnerAnnotations, assignedAnnotations);		
	}

	@Override
	public Task cloneTask() {
		
		return new ImportBinnerAnnotationsForUntargetedDataTask(
				binnerDataFile,
				currentExperiment,
				activeDataPipeline);
	}

	public Collection<BinnerAnnotation> getUnassignedAnnotations() {
		return unassignedAnnotations;
	}
}








