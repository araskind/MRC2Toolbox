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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class RawDataAnalysisMSFeatureDatabaseUploadTask extends AbstractTask {
	
	private static final int BATCH_SIZE = 100;

	private RawDataAnalysisProject project;
	private DataFile dataFile;
	private double msOneMZWindow;
	private Map<String,String>featureIdMap;
	private LIMSExperiment experiment;
	private DataExtractionMethod deMethod;
	
	public RawDataAnalysisMSFeatureDatabaseUploadTask(
			RawDataAnalysisProject project, 
			DataFile dataFile,
			DataExtractionMethod deMethod,
			double msOneMZWindow) {
		super();
		this.project = project;
		this.dataFile = dataFile;
		this.deMethod = deMethod;
		this.msOneMZWindow = msOneMZWindow;
		this.experiment = project.getIdTrackerExperiment();
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
		Collection<MsFeatureInfoBundle> bundles = 
				project.getMsFeaturesForDataFile(dataFile);
		total = bundles.size();
		processed = 0;	
		Map<String, MsFeatureChromatogramBundle> chromatogramMap = 
				project.getChromatogramMap();
		
		Connection conn = ConnectionManager.getConnection();
		
		String dataAnalysisId = IDTUtils.addNewDataAnalysis(
				deMethod, dataFile.getInjectionId(), conn);
			
		String parentFeatureQuery =
				"INSERT INTO MSMS_PARENT_FEATURE "
				+ "(FEATURE_ID, DATA_ANALYSIS_ID, RETENTION_TIME, HEIGHT, "
				+ "AREA, DETECTION_ALGORITHM, BASE_PEAK, POLARITY) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement parentFeaturePs = conn.prepareStatement(parentFeatureQuery);
		parentFeaturePs.setString(2, dataAnalysisId);
		
		String msOneQuery = "INSERT INTO MSMS_PARENT_FEATURE_PEAK "
				+ "(FEATURE_ID, MZ, HEIGHT) VALUES (?, ?, ?)";
		PreparedStatement msOnePs = conn.prepareStatement(msOneQuery);
		
		String msmsFeatureQuery =
				"INSERT INTO MSMS_FEATURE (PARENT_FEATURE_ID, MSMS_FEATURE_ID, DATA_ANALYSIS_ID, "
				+ "RETENTION_TIME, PARENT_MZ, FRAGMENTATION_ENERGY, COLLISION_ENERGY, POLARITY) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement msmsFeaturePs = conn.prepareStatement(msmsFeatureQuery);
		msmsFeaturePs.setString(3, dataAnalysisId);
		
		String msTwoQuery = 
				"INSERT INTO MSMS_FEATURE_PEAK (MSMS_FEATURE_ID, MZ, HEIGHT) "
				+ "VALUES (?, ?, ?)";
		PreparedStatement msTwoPs = conn.prepareStatement(msTwoQuery);
		
		String precursorQuery = 
				"UPDATE MSMS_PARENT_FEATURE SET MZ_OF_INTEREST = ? WHERE FEATURE_ID = ?";
		PreparedStatement precursorPs = conn.prepareStatement(precursorQuery);
		
		String libMatchQuery =
				"INSERT INTO MSMS_FEATURE_LIBRARY_MATCH ( " +
				"MATCH_ID, MSMS_FEATURE_ID, MRC2_LIB_ID, MATCH_SCORE, IS_PRIMARY, MATCH_TYPE) " +
				"VALUES (?,?,?,?,?,?) ";
		PreparedStatement libMatchPs = conn.prepareStatement(libMatchQuery);
		
		for(MsFeatureInfoBundle bundle : bundles) {
			
			//	Parent feature
			MsFeature feature = bundle.getMsFeature();
			String parentFeatureId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_PARENT_FEATURE_SEQ",
					DataPrefix.MS_FEATURE,
					"0",
					12);	
				
			featureIdMap.put(feature.getId(), parentFeatureId);
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
			parentFeaturePs.addBatch();
			
			//	MS1
			msOnePs.setString(1, parentFeatureId);
			for(MsPoint point : msOne) {
				
				msOnePs.setDouble(2, point.getMz());
				msOnePs.setDouble(3, point.getIntensity());
				msOnePs.addBatch();
			}
			//	MS2
			TandemMassSpectrum instrumentMsms =
					feature.getSpectrum().getExperimentalTandemSpectrum();				

			if(instrumentMsms != null) {
				
				//	MSMS feature
				String msmsId = SQLUtils.getNextIdFromSequence(conn, 
						"MSMS_FEATURE_SEQ",
						DataPrefix.MSMS_SPECTRUM,
						"0",
						12);			
				msmsFeaturePs.setString(1, parentFeatureId);
				msmsFeaturePs.setString(2, msmsId);
				msmsFeaturePs.setString(3, dataAnalysisId);
				msmsFeaturePs.setDouble(4, feature.getRetentionTime());
				msmsFeaturePs.setDouble(5, instrumentMsms.getParent().getMz());
				msmsFeaturePs.setDouble(6, instrumentMsms.getFragmenterVoltage());
				msmsFeaturePs.setDouble(7, instrumentMsms.getCidLevel());
				msmsFeaturePs.setString(8, feature.getPolarity().getCode());
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
				precursorPs.setString(2, parentFeatureId);
				precursorPs.addBatch();
				
				//	MSMS Identifications
				for(MsFeatureIdentity identification : feature.getIdentifications()) {
					
					//	MSMS library match
					ReferenceMsMsLibraryMatch refMatch = 
							identification.getReferenceMsMsLibraryMatch();
					if(refMatch != null) {
						
						String msmsMatchId = SQLUtils.getNextIdFromSequence(conn, 
								"MSMS_LIB_MATCH_SEQ",
								DataPrefix.MSMS_LIBRARY_MATCH,
								"0",
								15);				
						libMatchPs.setString(1, msmsMatchId);
						libMatchPs.setString(2, msmsId);
						libMatchPs.setString(3, refMatch.getMatchedLibraryFeature().getUniqueId());
						libMatchPs.setDouble(4, refMatch.getScore());
						libMatchPs.setString(5,"Y");
						
						String matchType = null;
						if(refMatch.getMatchType() != null)
							matchType = refMatch.getMatchType().name();
							
						libMatchPs.setString(6, matchType);
						libMatchPs.addBatch();
					}
					if(identification.getIdSource().equals(CompoundIdSource.MANUAL))
						IdentificationUtils.addMSMSFeatureManualId(msmsId, identification, conn);			
				}
			}
			//	Insert chromatograms
			MsFeatureChromatogramBundle msfCb = 
					chromatogramMap.get(bundle.getMsFeature().getId());
			
			//	MS-RT library match and manula IDs
//			for(MsFeatureIdentity identification : feature.getIdentifications()) {
//							
//				MsRtLibraryMatch msRtLibMatch = 
//						identification.getMsRtLibraryMatch();
//				if(msRtLibMatch != null) {
//					//	TODO
//				}	
//			}
			processed++;
			if(processed % BATCH_SIZE == 0) {

				parentFeaturePs.executeBatch();
				msOnePs.executeBatch();
				msmsFeaturePs.executeBatch();
				msTwoPs.executeBatch();
				precursorPs.executeBatch();
				libMatchPs.executeBatch();
			}
		}
		//	Execute last batch
		parentFeaturePs.executeBatch();
		msOnePs.executeBatch();
		msmsFeaturePs.executeBatch();
		msTwoPs.executeBatch();
		precursorPs.executeBatch();
		libMatchPs.executeBatch();
		
		//	Close all statements
		parentFeaturePs.close();
		msOnePs.close();
		msmsFeaturePs.close();
		msTwoPs.close();
		precursorPs.close();
		libMatchPs.close();
		ConnectionManager.releaseConnection(conn);
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
				project, 
				dataFile,
				deMethod,
				msOneMZWindow);
	}

	public Map<String, String> getFeatureIdMap() {
		return featureIdMap;
	}
}
