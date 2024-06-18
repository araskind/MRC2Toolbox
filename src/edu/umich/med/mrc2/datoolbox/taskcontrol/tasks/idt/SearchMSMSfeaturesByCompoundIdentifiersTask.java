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

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class SearchMSMSfeaturesByCompoundIdentifiersTask extends IDTMSMSFeatureDataPullTask {

	private CompoundIdFilter compoundIdFilter;
	private Polarity polarity;
	
	public SearchMSMSfeaturesByCompoundIdentifiersTask(
			CompoundIdFilter compoundIdFilter, 
			Polarity polarity) {
		super();
		this.compoundIdFilter = compoundIdFilter;
		this.polarity = polarity;
	}
	
	@Override
	public void run() {
		taskDescription = "Looking up features in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			selectMsMsFeaturesUsingCompoundIdentifiers();
			if(!features.isEmpty()) {
				
				attachExperimentalTandemSpectra();
				attachMsMsLibraryIdentifications();
				attachMsMsManualIdentities();
				retievePepSearchParameters();
				attachAnnotations();
				attachFollowupSteps();
				putDataInCache();
				attachChromatograms();
				fetchBinnerAnnotations();
			}
			finalizeFeatureList();
			applyAdditionalFilters();
			
			//	updateAutomaticDefaultIdsBasedOnScores();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void selectMsMsFeaturesUsingCompoundIdentifiers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Task cloneTask() {
		return new SearchMSMSfeaturesByCompoundIdentifiersTask(
				compoundIdFilter, polarity);
	}
}
