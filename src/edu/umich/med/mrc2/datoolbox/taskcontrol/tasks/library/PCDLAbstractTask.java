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
import java.util.ArrayList;
import java.util.Collection;
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
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public abstract class PCDLAbstractTask extends AbstractTask {

	private static final Logger logger = LogManager.getLogger(PCDLAbstractTask.class);

	
	protected CompoundLibrary masterPCDLlibrary;
	protected CompoundLibrary newLlibrary;
	protected File inputLibraryFile;
	protected Map<PCDLFields, Integer>dataFieldMap;
	protected Collection<CompoundIdentity>unmatchedFeatures = new ArrayList<CompoundIdentity>();
	
	protected void loadMasterLibrary() {
		
	}
		
	protected boolean parseTextLibrary() {
		
		taskDescription = "Creating new library entries ...";
		
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				inputLibraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());		
		
		total = compoundDataArray.length -1;
		processed = 0;
		
		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
		if(!dataValid)
			return false;
		
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String entryName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			LibraryMsFeature newLibFeature = masterPCDLlibrary.getFeatureByNameOrEntryNameIgnoreCase(entryName);
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
		return true;
	}
	
	protected void generateSpectraForAdducts(Collection<Adduct>selectedAdducts) {
		
		taskDescription = "Creating new library entries ...";
		total = newLlibrary.getFeatureCount();
		processed = 0;
		
		for(LibraryMsFeature libFeature : newLlibrary.getFeatures()) {
			
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
	
	protected boolean createAndValidateFieldMap(String[]header) {
		
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
			return false;
		}		
	}
	
	protected Set<String>extractCompoundNamesFromTextLibrary(){		
		
		taskDescription = "Extracting compound names from text library ...";
		Set<String>compoundNames = new TreeSet<>();
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				inputLibraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());		
		
		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
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
	
	public Collection<CompoundIdentity> getUnmatchedFeatures() {
		return unmatchedFeatures;
	}

	public CompoundLibrary getNewLlibrary() {
		return newLlibrary;
	}
}
