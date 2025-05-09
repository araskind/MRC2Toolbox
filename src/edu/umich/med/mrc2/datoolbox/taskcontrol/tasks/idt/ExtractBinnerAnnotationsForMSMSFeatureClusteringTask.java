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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ExtractBinnerAnnotationsForMSMSFeatureClusteringTask extends BinnerReportParserTask {

	private Collection<BinnerAnnotationCluster>binnerAnnotationClusters;
	private double mergeMzWindow;
	private MassErrorType masErrorType;
	private double mergeRtWindow;

	public ExtractBinnerAnnotationsForMSMSFeatureClusteringTask(
			File binnerDataFile, 
			double mergeMzWindow,
			MassErrorType masErrorType, 
			double mergeRtWindow) {
		super();
		this.binnerDataFile = binnerDataFile;
		this.mergeMzWindow = mergeMzWindow;
		this.masErrorType = masErrorType;
		this.mergeRtWindow = mergeRtWindow;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
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
		createLookupClusters();
		setStatus(TaskStatus.FINISHED);
	}
	
	private void createLookupClusters() {

		Map<Integer,BinnerAnnotationCluster>featureGroupMap = 
				new TreeMap<Integer,BinnerAnnotationCluster>();
		
		for(BinnerAnnotation annotation : binnerAnnotations) {
			
			featureGroupMap.computeIfAbsent(
					annotation.getMolIonNumber(), 
					s -> new BinnerAnnotationCluster(annotation));

			featureGroupMap.get(annotation.getMolIonNumber()).
				addUniqueAnnotation(annotation,
									mergeMzWindow,
									masErrorType, 
									mergeRtWindow);			
		}		
		binnerAnnotationClusters = featureGroupMap.values();
	}

	@Override
	public Task cloneTask() {
		return new ExtractBinnerAnnotationsForMSMSFeatureClusteringTask(
				binnerDataFile, mergeMzWindow, masErrorType, mergeRtWindow);
	}

	public Collection<BinnerAnnotationCluster> getBinnerAnnotationClusters() {
		return binnerAnnotationClusters;
	}
}
