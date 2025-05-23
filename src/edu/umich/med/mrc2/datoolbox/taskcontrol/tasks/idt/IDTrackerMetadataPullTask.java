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

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.BinnerAnnotationDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupListManager;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerMetadataPullTask extends AbstractTask {

	public IDTrackerMetadataPullTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return new IDTrackerMetadataPullTask();
	}

	@Override
	public void run() {

		taskDescription = "Retrieving data from database ...";
		setStatus(TaskStatus.PROCESSING);
		total = 150;
		processed = 0;

		try {
			taskDescription = "Refreshing user list";
			IDTDataCache.refreshUserList();
			processed = processed + 3;

			taskDescription = "Refreshing organization list";
			IDTDataCache.refreshOrganizationList();
			processed = processed + 3;

			taskDescription = "Refreshing project list";
			IDTDataCache.refreshProjectList();
			processed = processed + 3;

			taskDescription = "Refreshing experimenty list";
			IDTDataCache.refreshExperimentList();
			processed = processed + 3;

			taskDescription = "Refreshing stock sample list";
			IDTDataCache.refreshStockSampleList();
			processed = processed + 3;
			
			taskDescription = "Refreshing experiment/stock sample map";
			IDTDataCache.refreshExperimentStockSampleMap();
			processed = processed + 3;
			
			taskDescription = "Refreshing experiment polarity map ";
			IDTDataCache.refreshExperimentPolarityMap();
			processed = processed + 3;
			
			taskDescription = "Refreshing manufacturers list ";
			IDTDataCache.refreshManufacturers();
			processed = processed + 3;
			
			taskDescription = "Refreshing manufacturers list ";
			IDTDataCache.refreshSoftwareList();
			processed = processed + 3;
			
			taskDescription = "Refreshing ionization types";
			IDTDataCache.refreshIonizationTypes();
			processed = processed + 3;

			taskDescription = "Refreshing mass analyzer list";
			IDTDataCache.refreshMassAnalyzers();
			processed = processed + 3;

			taskDescription = "Refreshing MS Type list ";
			IDTDataCache.refreshMsTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing instrument list";
			IDTDataCache.refreshInstrumentList();
			processed = processed + 3;
			
			taskDescription = "Refreshing experiment / instrument map";
			IDTDataCache.refreshExperimentInstrumentMap();
			processed = processed + 3;
			
			taskDescription = "Refreshing chromatographic separation types";
			IDTDataCache.refreshChromatographicSeparationTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing chromatographic column list ";
			IDTDataCache.refreshChromatographicColumnList();
			processed = processed + 3;
					
			taskDescription = "Refreshing mobile phase list ";
			IDTDataCache.refreshMobilePhaseList();
			processed = processed + 3;
			
			taskDescription = "Refreshing gradient list ";
			IDTDataCache.refreshChromatographicGradientList();
			processed = processed + 3;

			taskDescription = "Refreshing acquisition method list ";
			IDTDataCache.refreshAcquisitionMethodList();
			processed = processed + 3;

			taskDescription = "Refreshing data extraction method list ";
			IDTDataCache.refreshDataExtractionMethodList();
			processed = processed + 3;

			taskDescription = "Refreshing SOP categories";
			IDTDataCache.refreshSopCategories();
			processed = processed + 3;

			taskDescription = "Refreshing protocol list ";
			IDTDataCache.refreshProtocols();
			processed = processed + 3;

			taskDescription = "Refreshing sample preparation list ";
			IDTDataCache.refreshSamplePreps();
			processed = processed + 3;

			taskDescription = "Refreshing experiment/sample prep map";
			IDTDataCache.refreshExperimentSamplePrepMap();
			processed = processed + 3;

			taskDescription = "Refreshing sample prep / data pipeline map";
			IDTDataCache.refreshSamplePrepDataPipelineMap();
			processed = processed + 3;

//			taskDescription = "Refreshing Acquisition/Data Extraction method map";
//			IDTDataCache.refreshAcquisitionDataExtractionMethodMap();
//			processed = processed + 3;

			taskDescription = "Refreshing measurement unit list";
			IDTDataCache.refreshUnits();
			processed = processed + 3;

			taskDescription = "Refreshing common MSMS library list ";
			IDTDataCache.refreshReferenceMsMsLibraryList();
			processed = processed + 3;
			
			taskDescription = "Refreshing MS feature identification follow-up step list";
			IDTDataCache.refreshMsFeatureIdentificationFollowupStepList();
			processed = processed + 3;
			
			taskDescription = "Refreshing standard feature annotation list";
			IDTDataCache.refreshStandardFeatureAnnotationList();
			processed = processed + 3;
			
			taskDescription = "Refreshing MSMS decoy generation methods list";
			IDTDataCache.refreshMSMSDecoyGenerationMethodList();
			processed = processed + 3;		
						
			taskDescription = "Refreshing MS feature identification level list";
			IDTDataCache.refreshMsFeatureIdentificationLevelList();
			processed = processed + 3;
			
			taskDescription = "Refreshing NIST PepSearch parameters list ";
			IDTDataCache.refreshNISTPepSearchParameters();
			processed = processed + 3;
			
			taskDescription = "Refreshing feature collection list ";
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			processed = processed + 3;
			
			taskDescription = "Refreshing sample type list ";
			IDTDataCache.refreshSampleTypes();
			processed = processed + 3;
			
			taskDescription = "Refreshing collision energies list ";
			IDTDataCache.refreshCollisionEnergies();
			processed = processed + 3;
			
			taskDescription = "Refreshing MSMS clustering parameters list";
			MSMSClusterDataSetManager.refreshMsmsClusteringParameters();
			processed = processed + 3;
			
			taskDescription = "Refreshing MSMS extraction parameters ";
			IDTDataCache.refreshMSMSExtractionParameters();
			processed = processed + 3;
			
//			taskDescription = "Refreshing fetaure collection data ";
//			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
//			processed = processed + 3;
			
			taskDescription = "Refreshing featue lookup data sets ";
			FeatureLookupListManager.refreshFeatureLookupListCollection();
			processed = processed + 3;
						
			taskDescription = "Refreshing the list of clustered MSMS sets ";
			MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();
			processed = processed + 3;
			
			taskDescription = "Refreshing MS/RT library list ";
			IDTDataCache.refreshMsRtLibraryList();
			processed = processed + 3;
			
			taskDescription = "Refreshing Binner annotation data set list ";
			BinnerAnnotationDataSetManager.refreshBinnerAnnotationLookupDataSetList();
			processed = processed + 3;
			
			taskDescription = "Refreshing Binner annotation lists ";
			IDTDataCache.refreshBinnerAdductListCollection();
			processed = processed + 3;
			
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
}











