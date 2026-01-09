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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.refmet.RefMetFields;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.RefMetUtils;

public class CompoundNameMatchingTask extends AbstractTask {

	private List<String> errors;
	private Collection<String> compoundErrors;
	private CompoundLibrary referenceLibrary;
	private Map<String,LibraryMsFeature>nameFeatureMap;
	private String[] compoundNames;
	private boolean writeLog;
	private File logDir;
	
	private static final String[]compoundAnnotationMasks 
		= new String[] {
				"\\{.+\\}",
				"\\[contaminant\\]",
				"\\(duplicate \\d\\)",
				"\\[fragment\\]",
				" \\(variant\\)"
			};
	
	private static final Pattern excludePattern = Pattern.compile("\\[[a-zA-Z]+\\]");

	public CompoundNameMatchingTask(
			String[] compoundNames, 
			CompoundLibrary referenceLibrary,			
			boolean writeLog,
			File logDir) {
		super();
		this.referenceLibrary = referenceLibrary;
		this.compoundNames = compoundNames;
		this.writeLog = writeLog;
		this.logDir = logDir;
		compoundErrors = new ArrayList<>();
		errors = new ArrayList<>();
		nameFeatureMap = new TreeMap<>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if(referenceLibrary != null)
			fetchLibraryCompounds();
		
		try {
			matchCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void fetchLibraryCompounds() {
		
		taskDescription = "Fetching library compounds ...";
		total = 100;
		processed = 20;
		
		if(MRC2ToolBoxCore.getActiveMsLibraries().contains(referenceLibrary)) {
			
			referenceLibrary = MRC2ToolBoxCore.getActiveMsLibraries().stream().
				filter(l -> l.getLibraryId().equals(referenceLibrary.getLibraryId())).
				findFirst().orElse(null);
		}
		else {
			try {
				Connection conn = ConnectionManager.getConnection();
				Collection<LibraryMsFeatureDbBundle>bundles =
						MSRTLibraryUtils.createFeatureBundlesForLibrary(referenceLibrary.getLibraryId(), conn);
				
				for(LibraryMsFeatureDbBundle fBundle : bundles) {

					if(fBundle.getConmpoundDatabaseAccession() != null) {

						LibraryMsFeature newTarget = fBundle.getFeature();							
						MSRTLibraryUtils.attachIdentity(
								newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

						if(newTarget.getPrimaryIdentity() != null) {

							newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
							MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);
							MSRTLibraryUtils.attachTandemMassSpectrum(newTarget, conn);
							MSRTLibraryUtils.attachAnnotations(newTarget, conn);
							referenceLibrary.addFeature(newTarget);
						}
					}
				}
				ConnectionManager.releaseConnection(conn);
				MRC2ToolBoxCore.getActiveMsLibraries().add(referenceLibrary);	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}	
		processed = 100;
	}
	
	private void matchCompounds() {
		
		
		if(referenceLibrary != null)
			matchCompoundsToLibrary();
		else {
			try {
				matchCompoundsToDatabase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!compoundErrors.isEmpty()) {
			
			errors.add("\nNo match found in reference library for the following compounds:\n");
			errors.addAll(compoundErrors);
			if(writeLog && logDir != null)
				writeAndOpenLogFile();
		}
	}
	
	private void matchCompoundsToLibrary() {
		
		
		taskDescription = "Matching feature names to compounds ...";
		total = compoundNames.length;
		processed = 0;
		
		for (String cpdName : compoundNames) {

			String cleanCompoundName = getCleanCompoundName(cpdName);
			if(cleanCompoundName == null)
				continue;
			
			LibraryMsFeature libFeature = referenceLibrary.getFeatureByNameIgnoreCase(cleanCompoundName);
			if (libFeature == null)
				compoundErrors.add(cleanCompoundName);
			else {	
				LibraryMsFeature newLibFeature = new LibraryMsFeature(libFeature);
				newLibFeature.setName(cpdName);
				nameFeatureMap.put(cpdName, newLibFeature);
			}
		}
		processed++;
	}
	
	private void matchCompoundsToDatabase() throws Exception{
		
		taskDescription = "Matching feature names to compounds ...";
		total = compoundNames.length;
		processed = 0;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT REFMET_ID FROM REFMET_DATA_NEW WHERE UPPER(NAME) = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		Map<String,String>nameRefMetIdMap = new TreeMap<>();
	
		for (String cpdName : compoundNames) {
		
			String cleanCompoundName = getCleanCompoundName(cpdName);
			if(cleanCompoundName == null)
				continue;
			
			//	Lookup in RefMet
			ps.setString(1, cleanCompoundName.toUpperCase());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				nameRefMetIdMap.put(cpdName, rs.getString(1));
			
			if(nameRefMetIdMap.get(cpdName) == null) {
				
				//	Convert name online through RefMet
				Map<RefMetFields,String>record = 
						RefMetUtils.getRefMetRecordByMatchingName(cleanCompoundName);
				if(!record.isEmpty() 
						&& record.get(RefMetFields.REFMET_ID) != null
						&& record.get(RefMetFields.REFMET_ID).startsWith("RM"))
					nameRefMetIdMap.put(cpdName, record.get(RefMetFields.REFMET_ID));
			}
			if(nameRefMetIdMap.get(cpdName) == null)
				compoundErrors.add(cleanCompoundName);
			
			rs.close();
			processed++;
		}
		ps.close();
		for(Entry<String,String>mapEntry : nameRefMetIdMap.entrySet()) {
			
			total = nameRefMetIdMap.size();
			processed = 0;
			
			CompoundIdentity cpdId = null;
			try {
				cpdId = CompoundDatabaseUtils.getRefMetCompoundById(mapEntry.getValue(), conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(cpdId != null) {
				
				LibraryMsFeature newLibFeature = new LibraryMsFeature(mapEntry.getKey(), 0.0d);
				MsFeatureIdentity mid = new MsFeatureIdentity(cpdId,
						CompoundIdentificationConfidence.ACCURATE_MASS_RT);
				mid.setIdentityName(mapEntry.getKey());
				newLibFeature.setPrimaryIdentity(mid);
				newLibFeature.setNeutralMass(cpdId.getExactMass());
				newLibFeature.removeDefaultPrimaryIdentity();
				nameFeatureMap.put(mapEntry.getKey(), newLibFeature);
			}
			processed++;
		}		
		ConnectionManager.releaseConnection(conn);
	}
	
	private String getCleanCompoundName(String compoundName) {
		
		Matcher m = excludePattern.matcher(compoundName.trim());
		if(m.find())
			return null;
		
		String cleanCompoundName = compoundName;
		for(String annotationMask : compoundAnnotationMasks)			
			cleanCompoundName = cleanCompoundName.replaceAll(annotationMask, "");
		
		return cleanCompoundName.trim();
	}
	
	private void writeAndOpenLogFile() {
		
		String fileName = "Unmatched-compounds-";
		if(referenceLibrary != null)
			fileName += referenceLibrary.getLibraryName();
		
		fileName += "-" + FIOUtils.getTimestamp() + ".txt";
		Path logPath = Paths.get( logDir.getAbsolutePath(), fileName);
		try {
		    Files.write(logPath, 
		    		compoundErrors,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if (Desktop.isDesktopSupported()) {
		    try {
		        Desktop.getDesktop().open(logPath.toFile());
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}
	}

	public List<String> getErrors() {
		return errors;
	}

	public Map<String, LibraryMsFeature> getNameFeatureMap() {
		return nameFeatureMap;
	}

	@Override
	public Task cloneTask() {

		return new CompoundNameMatchingTask(
				compoundNames, 
				referenceLibrary,			
				writeLog,
				logDir);
	}
}
