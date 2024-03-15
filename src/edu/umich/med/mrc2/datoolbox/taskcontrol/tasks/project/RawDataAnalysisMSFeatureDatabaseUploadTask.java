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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.StandardAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class RawDataAnalysisMSFeatureDatabaseUploadTask extends AbstractTask {
	
	private static final int BATCH_SIZE = 100;

	private RawDataAnalysisExperiment experiment;
	private DataFile dataFile;
	private double msOneMZWindow;
	private Map<String,String>featureIdMap;
	private LIMSExperiment limsExperiment;
	private DataExtractionMethod deMethod;
	private Map<String, MsFeatureChromatogramBundle> chromatogramMap;
	
	public RawDataAnalysisMSFeatureDatabaseUploadTask(
			RawDataAnalysisExperiment experiment, 
			DataFile dataFile,
			DataExtractionMethod deMethod,
			double msOneMZWindow) {
		super();
		this.experiment = experiment;
		this.dataFile = dataFile;
		this.deMethod = deMethod;
		this.msOneMZWindow = msOneMZWindow;
		this.limsExperiment = experiment.getLimsExperiment();
		featureIdMap = new HashMap<String,String>();
		taskDescription = "Uploading results for " + dataFile.getName();
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			 uploadMSMSFeatureData();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void uploadMSMSFeatureData() throws Exception {
		
		taskDescription = "Uploading results for " + dataFile.getName();
		Collection<MSFeatureInfoBundle> bundles = 
				experiment.getMsFeaturesForDataFile(dataFile);
		total = bundles.size();
		processed = 0;	
		
		Connection conn = ConnectionManager.getConnection();
		String dataAnalysisId = 
				IDTUtils.addNewDataAnalysis(deMethod, dataFile.getInjectionId(), conn);		
							
		String parentFeatureQuery =
				"INSERT INTO MSMS_PARENT_FEATURE "
				+ "(FEATURE_ID, DATA_ANALYSIS_ID, RETENTION_TIME, HEIGHT, "
				+ "AREA, DETECTION_ALGORITHM, BASE_PEAK, POLARITY, HAS_CHROMATOGRAM) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement parentFeaturePs = conn.prepareStatement(parentFeatureQuery);
		parentFeaturePs.setString(2, dataAnalysisId);
		
		String msOneQuery = "INSERT INTO MSMS_PARENT_FEATURE_PEAK "
				+ "(FEATURE_ID, MZ, HEIGHT) VALUES (?, ?, ?)";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		
		String msmsFeatureQuery =
				"INSERT INTO MSMS_FEATURE (PARENT_FEATURE_ID, MSMS_FEATURE_ID, DATA_ANALYSIS_ID, "
				+ "RETENTION_TIME, PARENT_MZ, FRAGMENTATION_ENERGY, COLLISION_ENERGY, POLARITY, "
				+ "ISOLATION_WINDOW_MIN, ISOLATION_WINDOW_MAX, HAS_SCANS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement msmsFeaturePs = conn.prepareStatement(msmsFeatureQuery);
		msmsFeaturePs.setString(3, dataAnalysisId);
		
		String msTwoQuery = 
				"INSERT INTO MSMS_FEATURE_PEAK (MSMS_FEATURE_ID, MZ, HEIGHT) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement msTwoPs = conn.prepareStatement(msTwoQuery);
		
		String precursorQuery = 
				"UPDATE MSMS_PARENT_FEATURE SET MZ_OF_INTEREST = ? WHERE FEATURE_ID = ?";
		PreparedStatement precursorPs = conn.prepareStatement(precursorQuery);
		
		String scanMapQuery = 
				"INSERT INTO MSMS_FEATURE_SCAN_MAP "
				+ "(MSMS_FEATURE_ID, SCAN, PARENT_SCAN, MS_LEVEL, "
				+ "RT, PARENT_MS_LEVEL, PARENT_RT) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement scanMapPs = conn.prepareStatement(scanMapQuery);
		
		String libMatchQuery =
				"INSERT INTO MSMS_FEATURE_LIBRARY_MATCH (" +
				"MATCH_ID, MSMS_FEATURE_ID, MRC2_LIB_ID, MATCH_SCORE, IS_PRIMARY, MATCH_TYPE, " +
				"FWD_SCORE, REVERSE_SCORE, PROBABILITY, DOT_PRODUCT,  " +
				"SEARCH_PARAMETER_SET_ID, IDENTIFICATION_LEVEL_ID,  " +
				"REVERSE_DOT_PRODUCT, HYBRID_DOT_PRODUCT,  " +
				"HYBRID_SCORE, HYBRID_DELTA_MZ) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";		
		PreparedStatement libMatchPs = conn.prepareStatement(libMatchQuery);
		
		String chromatogramQuery = 
				"INSERT INTO MSMS_PARENT_FEATURE_CHROMATOGRAM  " +
				"(FEATURE_ID, INJECTION_ID, MS_LEVEL, EXTRACTED_MASS,  " +
				"MASS_ERROR_VALUE, MASS_ERROR_TYPE, START_RT, END_RT,  " +
				"TITLE, TIME_VALUES, INTENSITY_VALUES)  " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement chromPs = conn.prepareStatement(chromatogramQuery);
		
		Map<String, MsFeatureIdentity>manualIds = 
				new TreeMap<String, MsFeatureIdentity>();
		ArrayList<MSFeatureInfoBundle>withStdAnnotations = 
				new ArrayList<MSFeatureInfoBundle>();
		ArrayList<MSFeatureInfoBundle>withFollowups = 
				new ArrayList<MSFeatureInfoBundle>();
		Map<String,MsFeatureChromatogramBundle>featureChromatogramMap = 
				new TreeMap<String, MsFeatureChromatogramBundle>();
		chromatogramMap = 
				new HashMap<String, MsFeatureChromatogramBundle>();
		
		for(MSFeatureInfoBundle bundle : bundles) {

			insertParentMsFeature(
					bundle,
					dataAnalysisId,
					parentFeaturePs,
					msOnePs,
					conn);
			MsFeature feature = bundle.getMsFeature();
			TandemMassSpectrum instrumentMsms =
					feature.getSpectrum().getExperimentalTandemSpectrum();				

			if(instrumentMsms != null) {
			
				insertMsms(
						feature,
						instrumentMsms, 
						dataAnalysisId,
						msmsFeaturePs, 
						msTwoPs,
						precursorPs,
						scanMapPs,
						conn);

				insertIdentifications(feature, manualIds, libMatchPs, conn);
			}
			//	Insert standard annotations
			if(!bundle.getStandadAnnotations().isEmpty())
				withStdAnnotations.add(bundle);
										
			//	Insert followup steps
			if(!bundle.getIdFollowupSteps().isEmpty())
				withFollowups.add(bundle);
					
			//	Insert chromatograms
			MsFeatureChromatogramBundle msfCb = 
					chromatogramMap.get(bundle.getMsFeature().getId());			
			if(msfCb != null)			
				featureChromatogramMap.put(feature.getId(), msfCb);
			
			//	MS-RT library match
			//	TODO

			processed++;
			if(processed % BATCH_SIZE == 0) {

				uploadBatch(
						parentFeaturePs,
						msOnePs,
						msmsFeaturePs,
						msTwoPs,
						precursorPs,
						scanMapPs,
						libMatchPs,
						chromPs,
						manualIds,
						withStdAnnotations,
						withFollowups,
						featureChromatogramMap,
						conn);
			}
		}
		//	Execute last batch
		uploadBatch(
				parentFeaturePs,
				msOnePs,
				msmsFeaturePs,
				msTwoPs,
				precursorPs,
				scanMapPs,
				libMatchPs,
				chromPs,
				manualIds,
				withStdAnnotations,
				withFollowups,
				featureChromatogramMap,
				conn);
		
		//	Close all statements
		parentFeaturePs.close();
		msOnePs.close();
		msmsFeaturePs.close();
		msTwoPs.close();
		precursorPs.close();
		scanMapPs.close();
		libMatchPs.close();
		chromPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void insertParentMsFeature(
			MSFeatureInfoBundle bundle,
			String dataAnalysisId,
			PreparedStatement parentFeaturePs,
			PreparedStatement msOnePs,
			Connection conn) throws Exception {
		
		MsFeature feature = bundle.getMsFeature();
		String parentFeatureId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_PARENT_FEATURE_SEQ",
				DataPrefix.MS_FEATURE,
				"0",
				12);			
		chromatogramMap.put(parentFeatureId, 
				experiment.getChromatogramMap().get(feature.getId()));
		featureIdMap.put(feature.getId(), parentFeatureId);
		feature.setId(parentFeatureId);
				
		Collection<MsPoint>msOne = getTrimmedMsOne(feature);
		double bpMz = 0.0d;
		if(msOne != null) {
			MsPoint bp = msOne.stream().
					sorted(MsUtils.reverseIntensitySorter).
					findFirst().orElse(null);
			if(bp != null)
				bpMz = bp.getMz();
		}
		parentFeaturePs.setString(1, parentFeatureId);
		parentFeaturePs.setString(2, dataAnalysisId);
		parentFeaturePs.setDouble(3, feature.getRetentionTime());
		parentFeaturePs.setDouble(4, feature.getSpectrum().getTotalArea());
		parentFeaturePs.setDouble(5, feature.getSpectrum().getTotalArea());
		
		// Set null for now
		parentFeaturePs.setNull(6, java.sql.Types.NULL);
		if(msOne == null)
			parentFeaturePs.setNull(7, java.sql.Types.NULL);
		else
			parentFeaturePs.setDouble(7, bpMz);

		parentFeaturePs.setString(8, feature.getPolarity().getCode());
		
		if(chromatogramMap.get(bundle.getMsFeature().getId()) != null){
			parentFeaturePs.setString(9, "Y");
		}
		else {
			parentFeaturePs.setNull(9, java.sql.Types.NULL);
		}
		parentFeaturePs.addBatch();
		
		//	MS1
		msOnePs.setString(1, parentFeatureId);
		for(MsPoint point : msOne) {
			
			msOnePs.setDouble(2, point.getMz());
			msOnePs.setDouble(3, point.getIntensity());
			msOnePs.addBatch();
		}
	}
	
	private void insertMsms(
			MsFeature feature,
			TandemMassSpectrum instrumentMsms, 
			String dataAnalysisId,
			PreparedStatement msmsFeaturePs, 
			PreparedStatement msTwoPs,
			PreparedStatement precursorPs,
			PreparedStatement scanMapPs,
			Connection conn) throws Exception {
		
		String msmsId = SQLUtils.getNextIdFromSequence(conn, 
				"MSMS_FEATURE_SEQ",
				DataPrefix.MSMS_SPECTRUM,
				"0",
				12);	
		instrumentMsms.setId(msmsId);
		
		msmsFeaturePs.setString(1, feature.getId());
		msmsFeaturePs.setString(2, msmsId);
		msmsFeaturePs.setString(3, dataAnalysisId);
		msmsFeaturePs.setDouble(4, feature.getRetentionTime());
		msmsFeaturePs.setDouble(5, instrumentMsms.getParent().getMz());
		msmsFeaturePs.setDouble(6, instrumentMsms.getFragmenterVoltage());
		msmsFeaturePs.setDouble(7, instrumentMsms.getCidLevel());
		msmsFeaturePs.setString(8, feature.getPolarity().getCode());
		
		Range isolationWindow = instrumentMsms.getIsolationWindow();
		if(isolationWindow != null && isolationWindow.getAverage() > 0.0) {
			msmsFeaturePs.setDouble(9, isolationWindow.getMin());
			msmsFeaturePs.setDouble(10, isolationWindow.getMax());
		}
		else {
			msmsFeaturePs.setNull(9, java.sql.Types.NULL);
			msmsFeaturePs.setNull(10, java.sql.Types.NULL);
		}
		if(instrumentMsms.getAveragedScanNumbers() != null 
				&& !instrumentMsms.getAveragedScanNumbers().isEmpty()) {
			msmsFeaturePs.setString(11, "Y");
		}
		else {
			msmsFeaturePs.setNull(11, java.sql.Types.NULL);
		}
		msmsFeaturePs.addBatch();
		
		//	MS2 points
		msTwoPs.setString(1, msmsId);
		for(MsPoint point : instrumentMsms.getSpectrum()) {

			msTwoPs.setDouble(2, point.getMz());
			msTwoPs.setDouble(3, point.getIntensity());
			msTwoPs.addBatch();
		}				
		//	Precursor
		precursorPs.setDouble(1, instrumentMsms.getParent().getMz());
		precursorPs.setString(2, feature.getId());
		precursorPs.addBatch();
		
		// Scan map
		if(instrumentMsms.getAveragedScanNumbers() != null 
				&& !instrumentMsms.getAveragedScanNumbers().isEmpty()) {
			scanMapPs.setString(1, msmsId);		
			Map<Integer, Double> srtMap = instrumentMsms.getScanRtMap();
			for(Entry<Integer, Integer> entry : instrumentMsms.getAveragedScanNumbers().entrySet()){
				
				scanMapPs.setInt(2, entry.getKey());
				scanMapPs.setInt(3, entry.getValue());	
				scanMapPs.setInt(4, 2);	//	TODO may need update if going above MS2
				scanMapPs.setDouble(5, srtMap.get(entry.getKey()));
				scanMapPs.setInt(6, 1);	//	TODO may need update if going above MS2
				scanMapPs.setDouble(7, srtMap.get(entry.getValue()));
				scanMapPs.addBatch();
			}
		}
	}
	
	private void uploadBatch(
			PreparedStatement parentFeaturePs,
			PreparedStatement msOnePs,
			PreparedStatement msmsFeaturePs,
			PreparedStatement msTwoPs,
			PreparedStatement precursorPs,
			PreparedStatement scanMapPs,
			PreparedStatement libMatchPs,
			PreparedStatement chromPs,
			Map<String, MsFeatureIdentity>manualIds,
			Collection<MSFeatureInfoBundle>withStdAnnotations,
			Collection<MSFeatureInfoBundle>withFollowups,
			Map<String,MsFeatureChromatogramBundle>featureChromatogramMap,
			Connection conn) throws Exception {
		
		parentFeaturePs.executeBatch();
		msOnePs.executeBatch();
		msmsFeaturePs.executeBatch();
		msTwoPs.executeBatch();
		precursorPs.executeBatch();
		scanMapPs.executeBatch();
		libMatchPs.executeBatch();
		
		if(!manualIds.isEmpty()) {
			
			for(Entry<String, MsFeatureIdentity> entry : manualIds.entrySet())
				IdentificationUtils.addMSMSFeatureManualId(entry.getKey(), entry.getValue(), conn);
			
			manualIds.clear();
		}
		if(!withStdAnnotations.isEmpty()) {
			
			for(MSFeatureInfoBundle b : withStdAnnotations)
				StandardAnnotationUtils.setStandardFeatureAnnotationsForMSMSFeature(b, conn);
			
			withStdAnnotations.clear();
		}
		if(!withFollowups.isEmpty()) {
			
			for(MSFeatureInfoBundle b : withFollowups)
				IdFollowupUtils.setIdFollowupStepsForMSMSFeature(b, conn);
			
			withStdAnnotations.clear();
		}
		if(!featureChromatogramMap.isEmpty()) {
			insertFeatureChromatograms(featureChromatogramMap, chromPs);
			featureChromatogramMap.clear();
		}
	}
	
	private void insertIdentifications(
			MsFeature feature, 
			Map<String, MsFeatureIdentity>manualIds,
			PreparedStatement libMatchPs, 
			Connection conn) throws Exception {
		
		for(MsFeatureIdentity identification : feature.getIdentifications()) {
			
			String msmsId = 
					feature.getSpectrum().getExperimentalTandemSpectrum().getId();
			
			//	MSMS library match
			ReferenceMsMsLibraryMatch refMatch = 
					identification.getReferenceMsMsLibraryMatch();
			if(refMatch != null) {
				
				String msmsMatchId = SQLUtils.getNextIdFromSequence(conn, 
						"MSMS_LIB_MATCH_SEQ",
						DataPrefix.MSMS_LIBRARY_MATCH,
						"0",
						15);	
				identification.setUniqueId(msmsMatchId);
				
				libMatchPs.setString(1, msmsMatchId);
				libMatchPs.setString(2, msmsId);
				libMatchPs.setString(3, refMatch.getMatchedLibraryFeature().getUniqueId());
				libMatchPs.setDouble(4, refMatch.getScore());
				
				if(identification.isPrimary())
					libMatchPs.setString(5,"Y");
				else
					libMatchPs.setNull(5,java.sql.Types.NULL);
				
				if(refMatch.getMatchType() != null)
					libMatchPs.setString(6, refMatch.getMatchType().name());
				else
					libMatchPs.setNull(6,java.sql.Types.NULL);
				
				if(refMatch.getForwardScore() > 0.0d)
					libMatchPs.setDouble(7, refMatch.getForwardScore());
				else
					libMatchPs.setNull(7,java.sql.Types.NULL);

				if(refMatch.getReverseScore() > 0.0d)
					libMatchPs.setDouble(8, refMatch.getReverseScore());
				else
					libMatchPs.setNull(8,java.sql.Types.NULL);
				
				if(refMatch.getProbability() > 0.0d)
					libMatchPs.setDouble(9, refMatch.getProbability());
				else
					libMatchPs.setNull(9,java.sql.Types.NULL);
				
				if(refMatch.getDotProduct() > 0.0d)
					libMatchPs.setDouble(10, refMatch.getDotProduct());
				else
					libMatchPs.setNull(10,java.sql.Types.NULL);
										
				if(refMatch.getSearchParameterSetId() != null)
					libMatchPs.setString(11, refMatch.getSearchParameterSetId());
				else
					libMatchPs.setNull(11,java.sql.Types.NULL);
				
				if(identification.getIdentificationLevel() != null)
					libMatchPs.setString(12, identification.getIdentificationLevel().getId());
				else
					libMatchPs.setNull(12,java.sql.Types.NULL);
				
				if(refMatch.getReverseDotProduct() > 0.0d)
					libMatchPs.setDouble(13, refMatch.getReverseDotProduct());
				else
					libMatchPs.setNull(13,java.sql.Types.NULL);
				
				if(refMatch.getMatchType().equals(MSMSMatchType.Hybrid)) {
					
					libMatchPs.setDouble(14, refMatch.getHybridDotProduct());
					libMatchPs.setDouble(15, refMatch.getHybridScore());
					libMatchPs.setDouble(16, refMatch.getHybridDeltaMz());
				}
				else {
					libMatchPs.setNull(14,java.sql.Types.NULL);
					libMatchPs.setNull(15,java.sql.Types.NULL);
					libMatchPs.setNull(16,java.sql.Types.NULL);
				}					
				libMatchPs.addBatch();
			}
			if(identification.getIdSource().equals(CompoundIdSource.MANUAL))
				manualIds.put(msmsId, identification);	
			
			//	TODO
//			MsRtLibraryMatch msRtLibMatch = identification.getMsRtLibraryMatch();
//			if(msRtLibMatch != null) {
//				
//			}
		}
	}
	
	private void insertFeatureChromatograms(
			Map<String,MsFeatureChromatogramBundle>featureChromatogramMap, 
			PreparedStatement chromPs) throws Exception {
		
		for(Entry<String, MsFeatureChromatogramBundle> entry : featureChromatogramMap.entrySet()) {
			
			MsFeatureChromatogramBundle msfCb = entry.getValue();
			ChromatogramDefinition cd = msfCb.getChromatogramDefinition();
			
			chromPs.setString(1, entry.getKey());			
			chromPs.setInt(3, cd.getMsLevel());
			chromPs.setDouble(5, cd.getMzWindowValue());
			chromPs.setString(6, cd.getMassErrorType().name());
			chromPs.setDouble(7, cd.getRtRange().getMin());
			chromPs.setDouble(8, cd.getRtRange().getMax());
			
			for(Entry<DataFile, Collection<ExtractedIonData>> fileEntry : msfCb.getChromatograms().entrySet()) {
				
				chromPs.setString(2, fileEntry.getKey().getInjectionId());				
				for(ExtractedIonData eid : fileEntry.getValue()) {
					
					chromPs.setDouble(4, eid.getExtractedMass());
					chromPs.setString(9, eid.getName());

					byte[] compressedTimeString = 
							eid.getEncodedTimeString().getBytes(StandardCharsets.US_ASCII);
					InputStream timeIs = null;
					try {
						timeIs = new ByteArrayInputStream(compressedTimeString);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(timeIs != null)
						chromPs.setBinaryStream(10, timeIs, compressedTimeString.length);
					else
						chromPs.setBinaryStream(10, null, 0);

					byte[] compressedIntensityString = 
							eid.getEncodedIntensityString().getBytes(StandardCharsets.US_ASCII);
					InputStream intensityIs = null;
					try {
						intensityIs = new ByteArrayInputStream(compressedIntensityString);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(intensityIs != null)
						chromPs.setBinaryStream(11, intensityIs, compressedIntensityString.length);
					else
						chromPs.setBinaryStream(11, null, 0);
					
					chromPs.executeUpdate();
					
					if(timeIs != null)
						timeIs.close();
					
					if(intensityIs != null)
						intensityIs.close();				
				}
			}	
		}
	}
	
	private Collection<MsPoint>getTrimmedMsOne(MsFeature feature){
		
		if(feature.getSpectrum() == null 
				|| feature.getSpectrum().getExperimentalTandemSpectrum() == null)
			return null;
		
		double center = feature.getSpectrum().
				getExperimentalTandemSpectrum().getParent().getMz();
		
		return MsUtils.trimSpectrum(feature.getSpectrum().getMsPoints(), center, msOneMZWindow);
	}
	
	@Override
	public Task cloneTask() {

		return new RawDataAnalysisMSFeatureDatabaseUploadTask(
				experiment, 
				dataFile,
				deMethod,
				msOneMZWindow);
	}

	public Map<String, String> getFeatureIdMap() {
		return featureIdMap;
	}

	public Map<String, MsFeatureChromatogramBundle> getChromatogramMap() {
		return chromatogramMap;
	}
}
