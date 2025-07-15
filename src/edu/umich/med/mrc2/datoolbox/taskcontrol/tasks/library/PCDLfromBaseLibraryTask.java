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
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class PCDLfromBaseLibraryTask extends AbstractTask {

	private CompoundLibrary basePCDLlibrary;
	private CompoundLibrary newLlibrary;
	private File inputLibraryFile;
	private Collection<Adduct>selectedAdducts;
	private Map<PCDLFields, Integer>dataFieldMap;
	private Collection<CompoundIdentity>unmatchedFeatures = new ArrayList<CompoundIdentity>();
	
	public PCDLfromBaseLibraryTask(
			CompoundLibrary basePCDLlibrary, 
			CompoundLibrary newLlibrary, 
			File inputLibraryFile,
			Collection<Adduct> selectedAdducts) {
		super();
		this.basePCDLlibrary = basePCDLlibrary;
		this.newLlibrary = newLlibrary;
		this.inputLibraryFile = inputLibraryFile;
		this.selectedAdducts = selectedAdducts;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (inputLibraryFile == null || !inputLibraryFile.exists()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		String newLibId = null;
		try {
			newLibId = MSRTLibraryUtils.createNewLibrary(newLlibrary);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newLibId == null) {
			errorMessage = "Failed to crete new library!";
			setStatus(TaskStatus.ERROR);
			return;
		}
		boolean dataValid = false;
		try {
			dataValid = parseTextLibrary();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(!dataValid) {			
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(unmatchedFeatures.isEmpty()) {
			
			try {
				writeFeaturesToDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		setStatus(TaskStatus.FINISHED);

	}
	
	private boolean parseTextLibrary() {
		
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				inputLibraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		taskDescription = "Creating new library entries ...";
		total = compoundDataArray.length -1;
		processed = 0;
		

		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
		if(!dataValid)
			return false;
		
		for(int i=1; i<compoundDataArray.length; i++) {
			
			String entryName = compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)];
			LibraryMsFeature newLibFeature = basePCDLlibrary.getFeatureByName(entryName);
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(rt > 0.0d) {
						newLibFeature.setRetentionTime(rt);
						MSRTLibraryUtils.generateMassSpectrumFromAdducts(newLibFeature, selectedAdducts);
						newLibFeature.getPrimaryIdentity().setConfidenceLevel(
								CompoundIdentificationConfidence.ACCURATE_MASS_RT);
						newLlibrary.addFeature(newLibFeature);
					}					
				}				
			}
			processed++;
		}		
		return true;
	}
	
	private void writeFeaturesToDatabase() throws Exception {

		taskDescription = "Writing library to database ...";
		total = newLlibrary.getFeatures().size();
		processed = 0;
		
		String libId = newLlibrary.getLibraryId();		
		Connection conn = ConnectionManager.getConnection();
		for(LibraryMsFeature lt : newLlibrary.getFeatures()){

			try {
				MSRTLibraryUtils.loadLibraryFeature(lt, libId, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(lt.getName().toUpperCase().contains("[ISTD]")) {
				
				try {
					MSRTLibraryUtils.setTargetQcStatus(lt.getId(), true, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private boolean createAndValidateFieldMap(String[]header) {
		
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
	
	@Override
	public Task cloneTask() {

		return new PCDLfromBaseLibraryTask(
				basePCDLlibrary, newLlibrary, inputLibraryFile, selectedAdducts);
	}

	public Collection<CompoundIdentity> getUnmatchedFeatures() {
		return unmatchedFeatures;
	}

	public CompoundLibrary getNewLlibrary() {
		return newLlibrary;
	}
}
