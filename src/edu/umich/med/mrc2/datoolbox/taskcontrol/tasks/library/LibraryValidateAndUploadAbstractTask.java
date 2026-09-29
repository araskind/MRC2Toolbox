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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundValidationType;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public abstract class LibraryValidateAndUploadAbstractTask extends DatabaseLibraryAbstractTask {

	private static final Logger logger = LogManager.getLogger(LibraryValidateAndUploadAbstractTask.class);

	
	protected CompoundLibrary masterLibrary;
	protected CompoundLibrary targetUploadLlibrary;
	protected File inputLibraryFile;
	protected CompoundValidationType compoundValidationType;
	protected boolean validateOnly;
	protected Collection<Adduct>selectedAdducts;	
	protected Map<PCDLFields, Integer>dataFieldMap;
	protected Collection<CompoundIdentity>unmatchedFeatures = new ArrayList<CompoundIdentity>();
	protected String[][] compoundDataArray;
	protected List<LibraryMsFeature>newLibraryMsFeatures;
	
	protected boolean parseAndValidatePCDLtextFile() {
		
		compoundDataArray = DelimitedTextParser.parseTextFile(
				inputLibraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		if(compoundDataArray == null || compoundDataArray.length == 0) {
			logger.error(String.format("Failed to parse PCDL text file  %s", inputLibraryFile.getAbsolutePath()));
			setStatus(TaskStatus.ERROR);
			return false;
		}
		else
			return createAndValidatePCDLFieldMap(compoundDataArray[0]);
	}
		
	protected Set<String>extractCompoundNamesFromPCDLTextLibrary(){		
		
		taskDescription = "Extracting compound names from text library ...";
		Set<String>compoundNames = new TreeSet<>();
		boolean dataValid = parseAndValidatePCDLtextFile();
		if(!dataValid)
			return compoundNames;
			
		total = 100;
		processed = 70;
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String entryName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			compoundNames.add(entryName);
		}
		processed = 100;
		return compoundNames;
	}
		
	protected void matchTextPCDLagainstMasterLibrary(String libraryId) {
		
		if(masterLibrary == null || masterLibrary.getFeatureCount() == 0) {
			masterLibrary = MSRTLibraryUtils.getLibrary(libraryId);
			Collection<LibraryMsFeature> masterLibraryFeatures = fetchFeaturesForLibrary(libraryId);
			masterLibraryFeatures.forEach(masterLibrary::addFeature);
		}		
		taskDescription = "Creating new library entries ...";				
		total = compoundDataArray.length -1;
		processed = 0;
		
		boolean dataValid = parseAndValidatePCDLtextFile();
		if(!dataValid)
			return;
		
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String entryName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			LibraryMsFeature newLibFeature = masterLibrary.getFeatureByNameOrEntryNameIgnoreCase(entryName);
			if(newLibFeature == null) {
				
				CompoundIdentity identity = 
						new CompoundIdentity(
								entryName, 
								compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)]);
				unmatchedFeatures.add(identity);
			}
			else {
				double rt = 0.0d;
				if(dataFieldMap.containsKey(PCDLFields.RETENTION_TIME)) {
					
					String rtString = compoundDataArray[i][dataFieldMap.get(PCDLFields.RETENTION_TIME)];
					if(rtString != null && !rtString.isEmpty()) {
						
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
	
	protected void generateSpectraForAdducts(Collection<Adduct>selectedAdducts) {
		
		taskDescription = "Creating new library entries ...";
		total = newLibraryMsFeatures.size();
		processed = 0;
		
		for(LibraryMsFeature libFeature : newLibraryMsFeatures) {
			
			MSRTLibraryUtils.generateMassSpectrumFromAdducts(libFeature, selectedAdducts);
			if(libFeature.getRetentionTime() > 0) {
				libFeature.getPrimaryIdentity().setConfidenceLevel(
						CompoundIdentificationConfidence.ACCURATE_MASS_RT);
			}
			else {
				libFeature.getPrimaryIdentity().setConfidenceLevel(
						CompoundIdentificationConfidence.ACCURATE_MASS);
			}
			processed++;
		}
	}
	
	protected boolean createAndValidatePCDLFieldMap(String[]header) {
		
		dataFieldMap = new TreeMap<PCDLFields, Integer>();
		//	;
		for(int i=0; i<header.length; i++) {
			
			PCDLFields f = PCDLFields.getOptionByUIName(header[i]);
			if(f != null)
				dataFieldMap.put(f, i);
		}
		ArrayList<String>missingFields = new ArrayList<String>();
		if(!dataFieldMap.containsKey(PCDLFields.NAME))
			missingFields.add(PCDLFields.NAME.getName());

		if(!dataFieldMap.containsKey(PCDLFields.FORMULA))
			missingFields.add(PCDLFields.FORMULA.getName());
		
		if(missingFields.isEmpty())
			return true;
		else {
			errorMessage = 
					"The following obligatory fields are missing form the input data:\n"
					+ StringUtils.join(missingFields, ", ");
			setStatus(TaskStatus.ERROR);
			return false;
		}		
	}
	
	protected void uploadNewFeaturesToTargetLibrary() {

		taskDescription = "Writing library to database ...";
		total = targetUploadLlibrary.getFeatures().size();
		processed = 0;
		
		String libId = targetUploadLlibrary.getLibraryId();		
		Connection conn = ConnectionManager.getConnection();
		for(LibraryMsFeature lt : newLibraryMsFeatures){

			try {
				MSRTLibraryUtils.loadLibraryFeature(lt, libId, conn);
			} catch (Exception e) {
				logger.error(String.format("Failed to insert in the database new library feature  %s", lt.getName()), e);
			}
			if(lt.getName().toUpperCase().contains("[ISTD]")) {
				
				try {
					MSRTLibraryUtils.setTargetQcStatus(lt.getId(), true, conn);
				} catch (Exception e) {
					logger.error(String.format("Failed to set target QC status in the database  %s", lt.getName()), e);
				}
			}
			processed++;
		}
		MRC2ToolBoxCore.getActiveMsLibraries().add(targetUploadLlibrary);
		ConnectionManager.releaseConnection(conn);
	}
	
	protected abstract void validateLibraryAgainstCompoundDatabase();

	protected abstract void validateLibraryAgainstMasterLibrary();
	
	public Collection<CompoundIdentity> getUnmatchedFeatures() {
		return unmatchedFeatures;
	}

	public CompoundLibrary getNewLlibrary() {
		return targetUploadLlibrary;
	}

	public CompoundLibrary getMasterLibrary() {
		return masterLibrary;
	}

	public boolean isValidateOnly() {
		return validateOnly;
	}
}
