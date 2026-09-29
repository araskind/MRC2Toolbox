/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundValidationType;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ValidateAndUploadPCDLLibraryTask extends LibraryValidateAndUploadAbstractTask {

	private static final Logger logger = LogManager.getLogger(ValidateAndUploadPCDLLibraryTask.class);
	
	public ValidateAndUploadPCDLLibraryTask(
			CompoundLibrary masterLibrary, 
			CompoundLibrary targetUploadLlibrary, 
			File inputLibraryFile,
			CompoundValidationType compoundValidationType,
			boolean validateOnly,
			Collection<Adduct> selectedAdducts) {
		super();
		this.masterLibrary = masterLibrary;
		this.targetUploadLlibrary = targetUploadLlibrary;
		this.inputLibraryFile = inputLibraryFile;
		this.compoundValidationType = compoundValidationType;
		this.validateOnly = validateOnly;
		this.selectedAdducts = selectedAdducts;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (inputLibraryFile == null || !inputLibraryFile.exists()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!parseAndValidatePCDLtextFile())
			return;
		
		if(masterLibrary == null 
				|| compoundValidationType.equals(CompoundValidationType.AGAINST_COMPOUND_DATABASE)){
			validateLibraryAgainstCompoundDatabase();
		}			
		if(masterLibrary != null 
				&& compoundValidationType.equals(CompoundValidationType.AGAINST_MASTER_LIBRARY)){
			validateLibraryAgainstMasterLibrary();
		}
		if(validateOnly || !unmatchedFeatures.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!newLibraryMsFeatures.isEmpty()) {
			
			if(selectedAdducts != null && !selectedAdducts.isEmpty())
				generateSpectraForAdducts(selectedAdducts);
			
			uploadNewFeaturesToTargetLibrary();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	protected void validateLibraryAgainstCompoundDatabase() {
		
	}

	protected void validateLibraryAgainstMasterLibrary() {

		newLibraryMsFeatures = new ArrayList<>();
		if(masterLibrary.getFeatureCount() == 0) {
			Collection<LibraryMsFeature> masterLibraryFeatures = 
					fetchFeaturesForLibrary(masterLibrary.getLibraryId());
			masterLibraryFeatures.forEach(masterLibrary::addFeature);
		}
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String entryName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			LibraryMsFeature newLibFeature = masterLibrary.getFeatureByNameOrEntryNameIgnoreCase(entryName);
			if(newLibFeature == null) {
				
				CompoundIdentity identity = new CompoundIdentity(entryName, 
								compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)]);
				unmatchedFeatures.add(identity);
			}
			else {
				newLibraryMsFeatures.add(newLibFeature);			
				if(dataFieldMap.containsKey(PCDLFields.RETENTION_TIME)) {			
					String rtString = compoundDataArray[i][dataFieldMap.get(PCDLFields.RETENTION_TIME)];
					if(rtString != null && !rtString.isBlank()) {
						double rt = 0.0d;
						try {
							rt = Double.valueOf(rtString);
						} catch (NumberFormatException e) {
							logger.error(String.format("Failed to parse retention time value  %s", rtString), e);
						}
						newLibFeature.setRetentionTime(rt);
					}				
				}				
			}
			processed++;
		}
	}

	@Override
	public Task cloneTask() {
		return new ValidateAndUploadPCDLLibraryTask(
				masterLibrary, 
				targetUploadLlibrary, 
				inputLibraryFile, 
				compoundValidationType, 
				validateOnly, 
				selectedAdducts);
	}
}
