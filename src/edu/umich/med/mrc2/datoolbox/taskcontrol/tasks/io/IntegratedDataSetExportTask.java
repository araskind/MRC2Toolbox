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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IntegratedDataSetExportTask extends AbstractTask {

	private static final String doubleValueCleanupString = 
			"\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad";
		
	private DataAnalysisProject currentExperiment;
	MsFeatureClusterSet integratedDataSet;
	private MissingExportType exportMissingAs;
	boolean replaceSpecialCharacters;
	private File exportFile;
	
	private DataExportFields namingField;
	private Collection<MsFeature> msFeatureSet4export;
	private ExperimentDesignSubset experimentDesignSubset;
	private TreeSet<ExperimentalSample>activeSamples;
			
	public IntegratedDataSetExportTask(
			DataAnalysisProject currentExperiment, 
			MsFeatureClusterSet integratedDataSet,
			MissingExportType exportMissingAs, 
			boolean replaceSpecialCharacters,
			File exportFile) {
		super();
		this.currentExperiment = currentExperiment;
		this.integratedDataSet = integratedDataSet;
		this.exportMissingAs = exportMissingAs;
		this.replaceSpecialCharacters = replaceSpecialCharacters;
		this.exportFile = exportFile;
	}

	@Override
	public void run() {

		taskDescription = "";
		setStatus(TaskStatus.PROCESSING);
		try {

		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {

		return new IntegratedDataSetExportTask(
				 currentExperiment, 
				 integratedDataSet,
				 exportMissingAs,
				 replaceSpecialCharacters,
				 exportFile);
	}
}
