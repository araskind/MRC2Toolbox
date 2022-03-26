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

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerDataPullTask extends AbstractTask {

	public IDTrackerDataPullTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new IDTrackerDataPullTask();
	}

	@Override
	public void run() {

		taskDescription = "Retrieving data from database ...";
		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 1;

		try {
			taskDescription = "Refreshing user list";
			IDTDataCash.refreshUserList();
			processed = processed + 3;

			taskDescription = "Refreshing organization list";
			IDTDataCash.refreshOrganizationList();
			processed = processed + 3;

			taskDescription = "Refreshing project list";
			IDTDataCash.refreshProjectList();
			processed = processed + 3;

			taskDescription = "Refreshing experimenty list";
			IDTDataCash.refreshExperimentList();
			processed = processed + 3;

			taskDescription = "Refreshing stock sample list";
			IDTDataCash.refreshStockSampleList();
			processed = processed + 3;
			
			taskDescription = "Refreshing manufacturers list ";
			IDTDataCash.refreshManufacturers();
			processed = processed + 3;
			
			taskDescription = "Refreshing ionization types";
			IDTDataCash.refreshIonizationTypes();
			processed = processed + 3;

			taskDescription = "Refreshing mass analyzer list";
			IDTDataCash.refreshMassAnalyzers();
			processed = processed + 3;

			taskDescription = "Refreshing MS Type list ";
			IDTDataCash.refreshMsTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing instrument list";
			IDTDataCash.refreshInstrumentList();
			processed = processed + 3;
			
			taskDescription = "Refreshing chromatographic separation types";
			IDTDataCash.refreshChromatographicSeparationTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing chromatographic column list ";
			IDTDataCash.refreshChromatographicColumnList();
			processed = processed + 3;

			taskDescription = "Refreshing acquisition method list ";
			IDTDataCash.refreshAcquisitionMethodList();
			processed = processed + 3;

			taskDescription = "Refreshing data extraction method list ";
			IDTDataCash.refreshDataExtractionMethodList();
			processed = processed + 3;

			taskDescription = "Refreshing SOP categories";
			IDTDataCash.refreshSopCategories();
			processed = processed + 3;

			taskDescription = "Refreshing protocol list ";
			IDTDataCash.refreshProtocols();
			processed = processed + 3;

			taskDescription = "Refreshing sample preparation list ";
			IDTDataCash.refreshSamplePreps();
			processed = processed + 3;

			taskDescription = "Refreshing experiment/sample prep map";
			IDTDataCash.refreshExperimentSamplePrepMap();
			processed = processed + 3;

			taskDescription = "Refreshing sample prep / data pipeline map";
			IDTDataCash.refreshSamplePrepDataPipelineMap();
			processed = processed + 3;

//			taskDescription = "Refreshing Acquisition/Data Extraction method map";
//			IDTDataCash.refreshAcquisitionDataExtractionMethodMap();
//			processed = processed + 3;

			taskDescription = "Refreshing measurement unit list";
			IDTDataCash.refreshUnits();
			processed = processed + 3;

			taskDescription = "Refreshing common MSMS library list ";
			IDTDataCash.refreshReferenceMsMsLibraryList();
			processed = processed + 3;
			
			taskDescription = "Refreshing MS feature identification follow-up step list";
			IDTDataCash.refreshMsFeatureIdentificationFollowupStepList();
			processed = processed + 3;
			
			taskDescription = "Refreshing standard feature annotation list";
			IDTDataCash.refreshStandardFeatureAnnotationList();
			processed = processed + 3;
			
			taskDescription = "Refreshing MSMS decoy generation methods list";
			IDTDataCash.refreshMSMSDecoyGenerationMethodList();
			processed = processed + 3;		
						
			taskDescription = "Refreshing MS feature identification level list";
			IDTDataCash.refreshMsFeatureIdentificationLevelList();
			processed = processed + 3;
			
			taskDescription = "Refreshing mobile phase list ";
			IDTDataCash.refreshMobilePhaseList();
			processed = processed + 3;
			
			taskDescription = "Refreshing NIST PepSearch parameters list ";
			IDTDataCash.refreshNISTPepSearchParameters();
			processed = processed + 3;
			
			taskDescription = "Refreshing feature collection list ";
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			processed = processed + 3;
			
			taskDescription = "Refreshing sample type list ";
			IDTDataCash.refreshSampleTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing collision energies list ";
			IDTDataCash.refreshCollisionEnergies();
			processed = processed + 3;
			
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
}











