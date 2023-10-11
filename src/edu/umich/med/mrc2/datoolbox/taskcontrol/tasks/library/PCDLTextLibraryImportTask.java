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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.PCDLFields;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class PCDLTextLibraryImportTask extends AbstractTask {

	private File inputLibraryFile;
	private CompoundLibrary library;
	
	private Collection<Adduct> adductList;
	private Map<PCDLFields, Integer>dataFieldMap;
	private CompoundIdentity[]rawIdArray;
	private CompoundIdentity[]matchedIdArray;
	private CompoundIdentity[]unmatchedIdArray;
	private Double[]retentionTimesArray;
	private boolean validateOnly;
			
	public PCDLTextLibraryImportTask(
			File inputLibraryFile, 
			CompoundLibrary library, 
			Collection<Adduct> adductList,
			boolean validateOnly) {
		super();
		this.inputLibraryFile = inputLibraryFile;
		this.library = library;
		this.adductList = adductList;
		this.validateOnly = validateOnly;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (inputLibraryFile == null || !inputLibraryFile.exists()) {
			setStatus(TaskStatus.FINISHED);
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
		try {
			matchCompoundIdentiesToDatabase();
		}
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(validateOnly) {
			
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(getUnmatchedIdList().size() == 0) {
			
			createLibraryFeatures();
			try {
				writeFeaturesToDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void matchCompoundIdentiesToDatabase() throws Exception{

		taskDescription = "Matching library compounds to database";
		total  = rawIdArray.length;
		processed = 0;
		Connection conn = ConnectionManager.getConnection();	
		for(int i=0; i<rawIdArray.length; i++) {
			
			if(rawIdArray[i] == null) {
				processed++;
				continue;
			}
			CompoundIdentity newId = null;
			try {
				newId = CompoundDatabaseUtils.mapLibraryCompoundIdentity(rawIdArray[i], conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (newId != null)
				matchedIdArray[i] = newId;
			else 
				unmatchedIdArray[i] = rawIdArray[i];
			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private boolean parseTextLibrary() {
		
		String[][] compoundDataArray = DelimitedTextParser.parseTextFile(
				inputLibraryFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		rawIdArray = new CompoundIdentity[compoundDataArray.length - 1];
		matchedIdArray = new CompoundIdentity[compoundDataArray.length - 1];
		unmatchedIdArray = new CompoundIdentity[compoundDataArray.length - 1];
		retentionTimesArray = new Double[compoundDataArray.length - 1];
		
		boolean dataValid = createAndValidateFieldMap(compoundDataArray[0]);
		if(!dataValid)
			return false;
		
		
		for(int i=1; i<compoundDataArray.length; i++) {
			
			CompoundIdentity identity = null;
			try {
				identity = 
						new CompoundIdentity(
								compoundDataArray[i][dataFieldMap.get(PCDLFields.NAME)], 
								compoundDataArray[i][dataFieldMap.get(PCDLFields.FORMULA)]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(identity != null) {
				
				if(dataFieldMap.containsKey(PCDLFields.HMP)) {
					
					String hmdbId = compoundDataArray[i][dataFieldMap.get(PCDLFields.HMP)];
					if(hmdbId != null && !hmdbId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.HMDB, hmdbId);
				}
				if(dataFieldMap.containsKey(PCDLFields.PUBCHEM)) {
					
					String pubchemId = compoundDataArray[i][dataFieldMap.get(PCDLFields.PUBCHEM)];
					if(pubchemId != null && !pubchemId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.PUBCHEM, pubchemId);
				}
				if(dataFieldMap.containsKey(PCDLFields.LMP)) {
					
					String lipidMapsId = compoundDataArray[i][dataFieldMap.get(PCDLFields.LMP)];
					if(lipidMapsId != null && !lipidMapsId.isEmpty())
						identity.addDbId(CompoundDatabaseEnum.LIPIDMAPS, lipidMapsId);
				}
				if(dataFieldMap.containsKey(PCDLFields.INCHI_KEY)) {
					
					String inchiKey = compoundDataArray[i][dataFieldMap.get(PCDLFields.INCHI_KEY)];
					if(inchiKey != null && !inchiKey.isEmpty())
						identity.setInChiKey(inchiKey);
				}
				if(dataFieldMap.containsKey(PCDLFields.SMILES)) {
					
					String smiles = compoundDataArray[i][dataFieldMap.get(PCDLFields.SMILES)];
					if(smiles != null && !smiles.isEmpty())
						identity.setSmiles(smiles);
				}
				rawIdArray[i-1] = identity;
				if(dataFieldMap.containsKey(PCDLFields.RETENTION_TIME)) {
					
					String rtString = compoundDataArray[i][dataFieldMap.get(PCDLFields.RETENTION_TIME)];
					if(rtString != null && !rtString.isEmpty()) {
						double rt = 0.0d;
						try {
							rt = Double.valueOf(rtString);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						retentionTimesArray[i-1] = rt;
					}

				}
			}
		}
		return true;
	}
	
	private boolean createAndValidateFieldMap(String[]header) {
		
		dataFieldMap = new TreeMap<PCDLFields, Integer>();
		//	;
		for(int i=0; i<header.length; i++) {
			
			PCDLFields f = PCDLFields.getPCDLFieldByUIName(header[i]);
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
	
	private void createLibraryFeatures() {
		
		taskDescription = "Creating new library features";
		total  = matchedIdArray.length;
		processed = 0;
		
		for(int i=0; i<matchedIdArray.length; i++) {
			
			CompoundIdentity cid = matchedIdArray[i];
			if(cid == null) {
				processed++;
				continue;
			}
			MsFeatureIdentity mid = new MsFeatureIdentity(
					cid, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
			
			MassSpectrum spectrum = new MassSpectrum();
			
			if(adductList != null && !adductList.isEmpty()) {
				
				Map<Adduct, Collection<MsPoint>> adductMap =
						MsUtils.createIsotopicPatternCollection(cid, adductList);
				adductMap.entrySet().stream().
					forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
			}
			LibraryMsFeature newTarget = 
					new LibraryMsFeature(cid.getName(), spectrum, retentionTimesArray[i]);

			newTarget.setNeutralMass(cid.getExactMass());
			newTarget.setPrimaryIdentity(mid);
			library.addFeature(newTarget);
		}
	}
	
	private void writeFeaturesToDatabase() throws Exception {

		taskDescription = "Writing library to database ...";
		total = library.getFeatures().size();
		processed = 0;
		
		String libId = library.getLibraryId();		
		Connection conn = ConnectionManager.getConnection();
		for(MsFeature lt : library.getFeatures()){

			try {
				MSRTLibraryUtils.loadLibraryFeature(
						(LibraryMsFeature) lt, libId, conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	public File getInputLibraryFile() {
		return inputLibraryFile;
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	@Override
	public Task cloneTask() {
		return new PCDLTextLibraryImportTask(
				inputLibraryFile, library, adductList, validateOnly);
	}

	public Collection<CompoundIdentity> getUnmatchedIdList() {
		return Arrays.asList(unmatchedIdArray).stream().
				filter(id -> Objects.nonNull(id)).collect(Collectors.toList());
	}
}
