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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchHitParameters;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public abstract class NISTMsPepSearchTask extends AbstractTask {

	protected File resultFile;
	protected String searchCommand;
	protected List<String>commandParts;
	protected Map<String,MsFeature>msmsIdMap;
	protected static final DecimalFormat intensityFormat = new DecimalFormat("###");
	protected NISTPepSearchParameterObject pepSearchParameterObject;
	protected TreeMap<String, Boolean> defaultIdUpdateMap;
	protected Map<String, ReferenceMsMsLibrary> refLibMap;
	protected Map<String, Double> maxExistingScoreMap;
	protected Map<String, Double> maxNewScoreMap;
	protected Collection<PepSearchOutputObject>pooList, pooListForUpdateOnly;
	protected Path logPath;

	public NISTMsPepSearchTask() {
		super();
		defaultIdUpdateMap = new TreeMap<String, Boolean>();
		msmsIdMap = new TreeMap<String, MsFeature>();		
		maxExistingScoreMap = new TreeMap<String, Double>();
		maxNewScoreMap = new TreeMap<String, Double>();
		pooListForUpdateOnly = new ArrayList<PepSearchOutputObject>();
	}

	protected void initLogFile() {

		logPath = Paths.get(resultFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(resultFile.getName()) + ".log");
		ArrayList<String>logStart = new ArrayList<String>();
		logStart.add(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
		if(searchCommand != null) {
			logStart.add("Search command:");
			logStart.add(searchCommand);
		}
		logStart.add("-------------------------------");
	    try {
			Files.write(logPath, 
					logStart, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void addLogLine(String line) {
	    try {
			Files.writeString(logPath, 
					line + "\n", 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void parseSearchResults() {
		
		if(!resultFile.exists())
			return;

		String[][] searchData = null;
		try {
			searchData =
				DelimitedTextParser.parseTextFileWithEncodingSkippingComments(
						resultFile, MRC2ToolBoxConfiguration.getTabDelimiter(), ">");
		} catch (IOException e1) {
			addLogLine("No output file");
			e1.printStackTrace();
		}
		if(searchData == null) {
			addLogLine("Can't read output file");
			return;
		}
		pooList = NISTPepSearchUtils.parsePepSearchOutputToObjects(
				searchData, pepSearchParameterObject);
		if(pooList.size() == 0) {
			addLogLine("No hits found or no MSMS feature IDs in correct format is present");
			return;
		}
		createResultsSummary();	
	}
	
	protected void filterSearchResults(boolean skipResultsUpload) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		assignMrc2LibIds(conn);	
		if(pooList.size() == 0) {
			addLogLine("No MRC2 library ids assigned");
			ConnectionManager.releaseConnection(conn);
			return;
		}
		filterOutDuplicateHits();
		checkHitPolarity(conn);
		if(pooList.size() == 0) {
			
			addLogLine("No hits of correct polarity found");
			ConnectionManager.releaseConnection(conn);
			return;
		}
		if(skipResultsUpload) {
			filterOutExistingHitsOffline();	
			if(pooList.size() == 0)
				addLogLine("No new hits found");
		}
		else {
			filterOutExistingHits(conn);	
			if(pooList.size() == 0)
				addLogLine("No new hits found");
		}
		ConnectionManager.releaseConnection(conn);
	}
		
	protected void filterOutExistingHitsOffline(){
		
		taskDescription = "Filtering out existing library matches ...";
		total = pooList.size();
		processed = 0;	
		Collection<PepSearchOutputObject>filtered = new ArrayList<PepSearchOutputObject>();
		for(PepSearchOutputObject poo : pooList) {
			
			MsFeature feature = msmsIdMap.get(poo.getMsmsFeatureId());		
			String libId = poo.getMrc2libid();
			MsFeatureIdentity existingHit = feature.getIdentifications().stream().
				filter(i -> i.getReferenceMsMsLibraryMatch() != null).
				filter(i -> i.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().
						getUniqueId().equals(libId)).findFirst().orElse(null);
			if(existingHit == null)
				filtered.add(poo);
			
			processed++;
		}
		pooList.clear();
		pooList.addAll(filtered);
	}

	protected void createResultsSummary() {
		
		taskDescription = "Creating summary ...";
		total = 100;
		processed = 20;
		Collection<String>summary = NISTPepSearchUtils.createResultsSummary(pooList);
		for(String line : summary)
			addLogLine(line);
		
		refLibMap = NISTPepSearchUtils.getMSMSLibraryNameMap(pooList);
	}
	
	protected void assignMrc2LibIds(Connection conn) throws Exception {
		
		taskDescription = "Assigning MRC2 library IDs to hits ...";
		total = pooList.size();
		processed = 0;
		
		Collection<PepSearchOutputObject> assigned = new ArrayList<PepSearchOutputObject>();
		String query =
			"SELECT C.MRC2_LIB_ID  " +
			"FROM REF_MSMS_LIBRARY_COMPONENT C " +
			"WHERE C.LIBRARY_NAME = ? " +
			"AND C.ORIGINAL_LIBRARY_ID = ? ";

		PreparedStatement ps = conn.prepareStatement(query);		
		for(PepSearchOutputObject poo : pooList) {
		
			String mrc2id = null;
			String originalLibraryId = null;
			ReferenceMsMsLibrary refLib = refLibMap.get(poo.getLibraryName());
			if(refLib != null) {
				
				String refLibraryId = refLib.getPrimaryLibraryId();
				if(refLibraryId.equals(NISTReferenceLibraries.nist_msms.name()) || 
						refLibraryId.equals(NISTReferenceLibraries.hr_msms_nist.name()))
					originalLibraryId = poo.getNistRegId();
				else if(refLibraryId.equals(NISTReferenceLibraries.nist_msms2.name()))
					originalLibraryId = poo.getDatabaseNumber();
				else
					originalLibraryId = poo.getPeptide();
				
				poo.setOriginalLibid(originalLibraryId);
				if(refLib.isDecoy())
					poo.setDecoy(true);
				
				if(poo.getPeptide().startsWith(DataPrefix.MSMS_LIBRARY_ENTRY.getName()) 
						&& poo.getPeptide().length() == 12) {
					poo.setMrc2libid(poo.getPeptide());
					assigned.add(poo);
					continue;
				}				
				ps.setString(1, refLibraryId);
				ps.setString(2, originalLibraryId);
				ResultSet rs = ps.executeQuery();
				while(rs.next())
					mrc2id = rs.getString("MRC2_LIB_ID");
				
				rs.close();
				if(mrc2id == null && refLibraryId.equals(NISTReferenceLibraries.hr_msms_nist.name())) {
					
					ps.setString(1, NISTReferenceLibraries.nist_msms.name());
					ps.setString(2, originalLibraryId);
					rs = ps.executeQuery();
					while(rs.next())
						mrc2id = rs.getString("MRC2_LIB_ID");
					
					rs.close();
				}
				if(mrc2id == null)
					addLogLine("Unknown library entry ID " + 
							originalLibraryId + " for library " + poo.getLibraryName());
				
				poo.setMrc2libid(mrc2id);
			}
			else {
				addLogLine("Unknown library " + poo.getLibraryName());
			}
			if(poo.getMrc2libid() != null)
				assigned.add(poo);
			
			processed++;
		}
		ps.close();
		pooList.clear();
		pooList.addAll(assigned);
		addLogLine("Assigned MRC2 MSMS library ID to " + Integer.toString(pooList.size()) + " library matches\n");
	}	
	
	protected void checkHitPolarity(Connection conn) throws Exception {
		
		taskDescription = "Checking hits for correct polarity ...";
		total = pooList.size();
		processed = 0;
		
		Collection<PepSearchOutputObject> clean = new ArrayList<PepSearchOutputObject>();
		String libSql = 
				"SELECT POLARITY FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?"  ;
		PreparedStatement libPs = conn.prepareStatement(libSql);
		String featureSql = 
				"SELECT POLARITY FROM MSMS_FEATURE WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement featurePs = conn.prepareStatement(featureSql);
		ResultSet rs = null;
		for(PepSearchOutputObject poo : pooList) {
			
			libPs.setString(1, poo.getMrc2libid());
			rs = libPs.executeQuery();
			String libPol = null;
			while(rs.next())
				libPol = rs.getString(1);
			
			rs.close();

			featurePs.setString(1, poo.getMsmsFeatureId());
			rs = featurePs.executeQuery();
			String fPol = null;
			while(rs.next())
				fPol = rs.getString(1);
			
			rs.close();
			if(libPol.equals(fPol)) {
				clean.add(poo);
			}
			else {
				addLogLine("Polarity mismatch between " + poo.getOriginalLibid() + 
						" for library " + poo.getLibraryName() + " and MSMS feature # " + poo.getMsmsFeatureId());
			}
			processed++;
		}	
		pooList.clear();
		pooList.addAll(clean);
		addLogLine(Integer.toString(pooList.size()) + " library matches have correct polarity");
	}

	protected void filterOutExistingHits(Connection conn) throws Exception {

		taskDescription = "Filtering out existing library matches ...";
		total = pooList.size();
		processed = 0;		
		
		Collection<PepSearchOutputObject> newHits = new ArrayList<PepSearchOutputObject>();
		Map<String,Double>scoreMap = new TreeMap<String,Double>();
		String query =
			"SELECT MRC2_LIB_ID, MATCH_SCORE FROM MSMS_FEATURE_LIBRARY_MATCH " +
			"WHERE MSMS_FEATURE_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = null; 
		for(PepSearchOutputObject poo : pooList) {
			
			scoreMap.clear();
			ps.setString(1, poo.getMsmsFeatureId());
			rs = ps.executeQuery();
			while(rs.next()) {
				scoreMap.put(
						rs.getString("MRC2_LIB_ID"), 
						rs.getDouble("MATCH_SCORE"));
			}
			rs.close();
			if(!scoreMap.containsKey(poo.getMrc2libid()))
				newHits.add(poo);
			else
				pooListForUpdateOnly.add(poo);
			
			if(!scoreMap.isEmpty())
				maxExistingScoreMap.put(poo.getMsmsFeatureId(), Collections.max(scoreMap.values()));
			else
				maxExistingScoreMap.put(poo.getMsmsFeatureId(), 0.0d);
			
			if(!maxNewScoreMap.containsKey(poo.getMsmsFeatureId()))
				maxNewScoreMap.put(poo.getMsmsFeatureId(), poo.getScore());
			else {
				if(poo.getScore() > maxNewScoreMap.get(poo.getMsmsFeatureId()))
					maxNewScoreMap.put(poo.getMsmsFeatureId(), poo.getScore());
			}
			processed++;
		}
		ps.close();
		pooList.clear();
		pooList.addAll(newHits);
		addLogLine(Integer.toString(pooList.size()) + 
				" new matches (not yet in database for "
				+ "the corresponding MSMS features) present");
	}
	
	protected void filterOutDuplicateHits() {
		
		Set<PepSearchOutputObject>uniqueHits = 
				pooList.stream().distinct().collect(Collectors.toSet());
		pooList.clear();
		pooList.addAll(uniqueHits);
		addLogLine(Integer.toString(pooList.size()) + 
				" new unique matches (not yet in database for "
				+ "the corresponding MSMS features using MRC2 MSMS library ID) present");
	}
	
	protected void uploadSearchResults() throws Exception {
		
		taskDescription = "Uploading search results to database ...";
		total = pooList.size();
		processed = 0;
	
		Connection conn = ConnectionManager.getConnection();		
		String searchParametersId = null;
		if(pepSearchParameterObject != null)		
			searchParametersId = IdentificationUtils.addNewPepSearchParameterSet(pepSearchParameterObject, conn);
		
		String msmsMatchId = null;
//		String midQuery = "SELECT '" + DataPrefix.MSMS_LIBRARY_MATCH.getName() +
//				"' || LPAD(MSMS_LIB_MATCH_SEQ.NEXTVAL, 15, '0') AS MATCH_ID FROM DUAL";
//		PreparedStatement midPs = conn.prepareStatement(midQuery);
//		ResultSet midRs = null;

		String query =
			"INSERT INTO MSMS_FEATURE_LIBRARY_MATCH (" +
			"MSMS_FEATURE_ID, MATCH_ID, MRC2_LIB_ID, MATCH_SCORE,  " +
			"PROBABILITY, DOT_PRODUCT, SEARCH_PARAMETER_SET_ID, "
			+ "REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, "
			+ "HYBRID_DELTA_MZ, MATCH_TYPE, DECOY_MATCH) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String nullLmQuery = 
				"UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = NULL WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement nullLmPs = conn.prepareStatement(nullLmQuery);
		String updateLmQuery = 
				"UPDATE MSMS_FEATURE_LIBRARY_MATCH SET IS_PRIMARY = 'Y' WHERE MATCH_ID = ?";
		PreparedStatement updateLmPs = conn.prepareStatement(updateLmQuery);
		
		int batchSize = 200; //	1 for debug only, normal is 200;
		for(PepSearchOutputObject poo : pooList) {
			
			msmsMatchId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_MATCH_SEQ",
					DataPrefix.MSMS_LIBRARY_MATCH,
					"0",
					15);
			
			//	Insert record
			ps.setString(1, poo.getMsmsFeatureId());
			ps.setString(2, msmsMatchId);
			ps.setString(3, poo.getMrc2libid());
			ps.setDouble(4, poo.getScore());
			ps.setDouble(5, poo.getProbablility());
			ps.setDouble(6, poo.getDotProduct());
			ps.setString(7, searchParametersId);

			if (poo.getReverseDotProduct() == 0.0)
				ps.setNull(8, java.sql.Types.NULL);
			else
				ps.setDouble(8, poo.getReverseDotProduct());
			
			if (poo.getHybridDotProduct() == 0.0)
				ps.setNull(9, java.sql.Types.NULL);
			else
				ps.setDouble(9, poo.getHybridDotProduct());
			
			if (poo.getHybridScore() == 0.0)
				ps.setNull(10, java.sql.Types.NULL);
			else
				ps.setDouble(10, poo.getHybridScore());
			
			if (poo.getHybridDeltaMz() == 0.0)
				ps.setNull(11, java.sql.Types.NULL);
			else
				ps.setDouble(11, poo.getHybridDeltaMz());
			
			ps.setString(12, poo.getMatchType().name());
		
			if (poo.isDecoy())
				ps.setString(13, "1");
			else
				ps.setNull(13, java.sql.Types.NULL);
			
			ps.addBatch();
			
			//	Update primary ID if this is allowed and the score is top score between old and new IDs
			if(defaultIdUpdateMap.get(poo.getMsmsFeatureId()) != null 
				&& defaultIdUpdateMap.get(poo.getMsmsFeatureId()) 
				&& maxNewScoreMap.get(poo.getMsmsFeatureId()) > maxExistingScoreMap.get(poo.getMsmsFeatureId())) {
				
				nullLmPs.setString(1, poo.getMsmsFeatureId());
				nullLmPs.addBatch();
				updateLmPs.setString(1, msmsMatchId);
				updateLmPs.addBatch();					
			}	
			processed++;
			
			if(processed % batchSize == 0) {
				ps.executeBatch();
				nullLmPs.executeBatch();
				updateLmPs.executeBatch();
			}
		}
		ps.executeBatch();
		nullLmPs.executeBatch();
		updateLmPs.executeBatch();
		
//		midPs.close();
		nullLmPs.close();
		updateLmPs.close();
		ps.close();
		
		ConnectionManager.releaseConnection(conn);
		IdentificationUtils.markNewDefaultLibraryIdsAsTentative();
	}
	
	protected void updateExistingHits() throws Exception {
		
		taskDescription = "Adding missing parameters for existing library hits ...";
		total = pooListForUpdateOnly.size();
		processed = 0;
	
		if(pepSearchParameterObject == null) {
			System.out.println("No PepSearch parameters!");
			return;
		}
		Connection conn = ConnectionManager.getConnection();		
		Collection<String> searchParametersIds = 
				IdentificationUtils.getPepSearchParameterSetWithSameSearchSettings(
						pepSearchParameterObject,conn);
		
		if(searchParametersIds.isEmpty()) {
			System.out.println("No PepSearch matching PepSearch parameter set in database!");
			return;
		}		
		StringBuilder builder = null;
		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 3;
		
		Map<PepSearchHitParameters,Double>updateParametersMap = new TreeMap<PepSearchHitParameters,Double>();
		builder = new StringBuilder();
		for(String paramId : searchParametersIds) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, paramId));
			builder.append("?,");
		}
		String paramSetList = builder.deleteCharAt( builder.length() -1 ).toString();
		
		String paramsQuery = 
				"SELECT MATCH_ID, MATCH_SCORE, PROBABILITY, DOT_PRODUCT, " +
				"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT, HYBRID_SCORE, HYBRID_DELTA_MZ " +
				"FROM MSMS_FEATURE_LIBRARY_MATCH " +
				"WHERE MSMS_FEATURE_ID = ? " +
				"AND MRC2_LIB_ID = ? " +
				"AND SEARCH_PARAMETER_SET_ID IN (" + paramSetList + ") ";
		PreparedStatement parPs = conn.prepareStatement(paramsQuery);
		for(Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if(entry.getValue().getClazz().equals(String.class))
				parPs.setString(entry.getKey(), (String)entry.getValue().getValue());
		}
		ResultSet parRs = null;		
		String updQuery = 
				"UPDATE MSMS_FEATURE_LIBRARY_MATCH "
				+ "SET MATCH_SCORE = ?, "
				+ "PROBABILITY = ?, "
				+ "DOT_PRODUCT = ?, "
				+ "REVERSE_DOT_PRODUCT = ?, "
				+ "HYBRID_DOT_PRODUCT = ?, "
				+ "HYBRID_SCORE = ?, "
				+ "HYBRID_DELTA_MZ = ?"
				+ "WHERE MATCH_ID = ? ";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		for(PepSearchOutputObject poo : pooListForUpdateOnly) {
			
			updateParametersMap.clear();
			boolean needsUpdate = false;
			parPs.setString(1, poo.getMsmsFeatureId());
			parPs.setString(2, poo.getMrc2libid());
			parRs = parPs.executeQuery();
			while(parRs.next()) {
				
				updateParametersMap.put(PepSearchHitParameters.MATCH_SCORE, parRs.getDouble("MATCH_SCORE"));
				if(parRs.getDouble("MATCH_SCORE") == 0.0d && poo.getScore() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.MATCH_SCORE, poo.getScore());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.PROBABILITY, parRs.getDouble("PROBABILITY"));
				if(parRs.getDouble("PROBABILITY") == 0.0d && poo.getProbablility() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.PROBABILITY, poo.getProbablility());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.DOT_PRODUCT, parRs.getDouble("DOT_PRODUCT"));
				if(parRs.getDouble("DOT_PRODUCT") == 0.0d && poo.getDotProduct() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.DOT_PRODUCT, poo.getDotProduct());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.REVERSE_DOT_PRODUCT, parRs.getDouble("REVERSE_DOT_PRODUCT"));
				if(parRs.getDouble("REVERSE_DOT_PRODUCT") == 0.0d && poo.getReverseDotProduct() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.REVERSE_DOT_PRODUCT, poo.getReverseDotProduct());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.HYBRID_DOT_PRODUCT, parRs.getDouble("HYBRID_DOT_PRODUCT"));
				if(parRs.getDouble("HYBRID_DOT_PRODUCT") == 0.0d && poo.getHybridDotProduct() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.HYBRID_DOT_PRODUCT, poo.getHybridDotProduct());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.HYBRID_SCORE, parRs.getDouble("HYBRID_SCORE"));
				if(parRs.getDouble("HYBRID_SCORE") == 0.0d && poo.getHybridScore() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.HYBRID_SCORE, poo.getHybridScore());
					needsUpdate = true;
				}
				updateParametersMap.put(PepSearchHitParameters.HYBRID_DELTA_MZ, parRs.getDouble("HYBRID_DELTA_MZ"));
				if(parRs.getDouble("HYBRID_DELTA_MZ") == 0.0d && poo.getHybridDeltaMz() > 0 ) {
					updateParametersMap.put(PepSearchHitParameters.HYBRID_DELTA_MZ, poo.getHybridDeltaMz());
					needsUpdate = true;
				}
				if(needsUpdate) {
					updPs.setDouble(1, updateParametersMap.get(PepSearchHitParameters.MATCH_SCORE));
					updPs.setDouble(2, updateParametersMap.get(PepSearchHitParameters.PROBABILITY));
					updPs.setDouble(3, updateParametersMap.get(PepSearchHitParameters.DOT_PRODUCT));
					updPs.setDouble(4, updateParametersMap.get(PepSearchHitParameters.REVERSE_DOT_PRODUCT));
					updPs.setDouble(5, updateParametersMap.get(PepSearchHitParameters.HYBRID_DOT_PRODUCT));
					updPs.setDouble(6, updateParametersMap.get(PepSearchHitParameters.HYBRID_SCORE));
					updPs.setDouble(7, updateParametersMap.get(PepSearchHitParameters.HYBRID_DELTA_MZ));
					updPs.setString(8, parRs.getString("MATCH_ID"));
					updPs.executeUpdate();
				}
			}
			parRs.close();
			processed++;
		}
		parPs.close();	
		updPs.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	protected Boolean allowDefaultIdUpdate(MsFeatureInfoBundle bundle) {
		
		if(bundle.getMsFeature().getIdentifications().isEmpty())
			return true;
		
		if(!bundle.getIdFollowupSteps().isEmpty())
			return false;
		
		if(!bundle.getMsFeature().getAnnotations().isEmpty())
			return false;
		
		if(!bundle.getStandadAnnotations().isEmpty())
			return false;
		
		MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
		if(primaryId == null || primaryId.getIdentificationLevel() == null)
			return true;
				
		if(primaryId.getAssignedBy() != null)	//	Exclude manually assigned
			return false;
		
		return primaryId.getIdentificationLevel().isAllowToReplaceAsDefault();
	}

	public File getResultsFile() {
		return resultFile;
	}
	
	public File getLogFile() {
		
		if(logPath == null)
			return null;
		else
			return logPath.toFile();
	}
	
	public NISTPepSearchParameterObject getPepSearchParameterObject() {
		return pepSearchParameterObject;
	}

	public void setPepSearchParameterObject(NISTPepSearchParameterObject pepSearchParameterObject) {
		this.pepSearchParameterObject = pepSearchParameterObject;
	}

	public String getSearchCommand() {
		return searchCommand;
	}

	public void setSearchCommand(String searchCommand) {
		this.searchCommand = searchCommand;
	}
}
