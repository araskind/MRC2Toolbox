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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTMsDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTRawDataUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTMSMSDuplicateMSMSFeatureCleanupTask extends AbstractTask {
	
	private Polarity polarity;
	private LIMSExperiment experiment;
	private Collection<MsFeatureInfoBundle>features;
	private Collection<MsFeatureInfoBundle>featuresToDelete;
	private Collection<MsFeatureInfoBundle>featuresToUpdateFollowups;
	private Collection<MsFeatureInfoBundle>featuresToUpdateIds;
	private Collection<MsFeatureInfoBundle>featuresToUpdateAnnotations;
	private Map<String,String>duplicateIdMap;
	
	public IDTMSMSDuplicateMSMSFeatureCleanupTask(
			Polarity polarity, 
			LIMSExperiment experiment) {
		super();
		this.polarity = polarity;
		this.experiment = experiment;	
//		features = new HashSet<MsFeatureInformationBundle>();	
//		featuresToDelete = new HashSet<MsFeatureInformationBundle>();
//		featuresToUpdateFollowups = new HashSet<MsFeatureInformationBundle>();
//		featuresToUpdateIds = new HashSet<MsFeatureInformationBundle>();
//		featuresToUpdateAnnotations = new HashSet<MsFeatureInformationBundle>();
		
		duplicateIdMap = new TreeMap<String,String>();
	}

	@Override
	public void run() {
		taskDescription = "Looking up feature IDs in IDTracker database";
		setStatus(TaskStatus.PROCESSING);
		try {
			clearMultipleDefaultIds();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		setStatus(TaskStatus.FINISHED);
	}
	
	private void clearMultipleDefaultIds() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Fetching MS2 features ...";
		String query = 
				"SELECT MSMS_FEATURE_ID, COUNT(IS_PRIMARY) AS DUPE_CNT " +
				"FROM MSMS_FEATURE_LIBRARY_MATCH " +
				"WHERE IS_PRIMARY IS NOT NULL " +
				"GROUP BY MSMS_FEATURE_ID " +
				"HAVING   COUNT(MRC2_LIB_ID) > 1 " +
				"ORDER BY MSMS_FEATURE_ID, COUNT(IS_PRIMARY) DESC ";
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while(rs.next()) {
			String featureId = rs.getString("MSMS_FEATURE_ID");
			Collection<MsFeatureIdentity>dupIds = IdentificationUtils.getMSMSFeatureLibraryMatches(
					featureId, conn);
			
			dupIds = dupIds.stream().filter(i -> i.isPrimary()).collect(Collectors.toList());
			setSingleDefaultId(featureId, dupIds, conn);
			processed++;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private void setSingleDefaultId(String featureId, Collection<MsFeatureIdentity>dupIds, Connection conn) throws Exception {
		
		Collection<MsFeatureIdentity>curated = dupIds.stream().
			filter(i -> i.getIdentificationLevel() != null).
			filter(i -> !i.getIdentificationLevel().getId().equals("IDS002")).
			collect(Collectors.toList());
		if(curated.isEmpty()) {
			MsFeatureIdentity primary = dupIds.stream().
					sorted(new MsFeatureIdentityComparator(SortProperty.Quality)).findFirst().orElse(null);
			IdentificationUtils.setMSMSFeaturePrimaryIdentity(featureId, primary, conn);
		}
		else {
			System.out.println("Curated data for " + featureId);
		}
	}

	private void clearDuplicateLibraryMatches() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Fetching MS2 features ...";
		String query = 
				"SELECT   MSMS_FEATURE_ID, MRC2_LIB_ID,  " +
				"COUNT(MRC2_LIB_ID) AS DUPE_CNT " +
				"FROM MSMS_FEATURE_LIBRARY_MATCH " +
				"GROUP BY MSMS_FEATURE_ID, MRC2_LIB_ID " +
				"HAVING COUNT(MRC2_LIB_ID) > 1 " +
				"ORDER BY MSMS_FEATURE_ID ";
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while(rs.next()) {
			
			Collection<MsFeatureIdentity>dupIds = IdentificationUtils.getMSMSFeatureLibraryMatchesForLibraryId(
					rs.getString("MSMS_FEATURE_ID"), rs.getString("MRC2_LIB_ID"),  conn);

			if(removeCompleteDuplicates(dupIds, conn))
				continue;
			else
				System.out.println("Multiple search params for " + 
						rs.getString("MSMS_FEATURE_ID") + " and " + rs.getString("MRC2_LIB_ID"));

			processed++;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private boolean removeCompleteDuplicates(Collection<MsFeatureIdentity>dupIds, Connection conn){
		
		Collection<ReferenceMsMsLibraryMatch>distinctMatches = new ArrayList<ReferenceMsMsLibraryMatch>();
		for(MsFeatureIdentity id : dupIds) {
			
			boolean isPresent = false;
			for(ReferenceMsMsLibraryMatch m : distinctMatches) {
				
				if(id.getReferenceMsMsLibraryMatch().isIdentical(m)) {
					isPresent = true;
					break;
				}
			}
			if(!isPresent)
				distinctMatches.add(id.getReferenceMsMsLibraryMatch());		
		}
		if(distinctMatches.size() == 1) {
			
			//	Check for manually curated
			Collection<String>curatedIds = dupIds.stream().
					filter(i -> i.getIdentificationLevel() != null).
					filter(i -> !i.getIdentificationLevel().getId().equals("IDS002")).
					map(i -> i.getUniqueId()).
					collect(Collectors.toList());
			
			// If no curated find first primary or 
			if(curatedIds.isEmpty()) {
				
				MsFeatureIdentity primId = dupIds.stream().
						filter(i -> i.isPrimary()).findFirst().orElse(null);
				if(primId == null)
					primId = dupIds.stream().
						sorted(new MsFeatureIdentityComparator(SortProperty.pimaryId)).
						findFirst().orElse(null);
				
				if(primId != null) {
					
					for(MsFeatureIdentity id : dupIds) {
						
						if(!id.getUniqueId().equals(primId.getUniqueId())) {
							try {
								//	System.out.println("Deleting " + id.getUniqueId());
								IdentificationUtils.removeMSMSFeatureLibraryMatch(id, conn);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					return true;
				}	
			}
			else {
				for(MsFeatureIdentity id : dupIds) {
					if(!curatedIds.contains(id.getUniqueId())) {
						try {
							//	System.out.println("Deleting " + id.getUniqueId());
							IdentificationUtils.removeMSMSFeatureLibraryMatch(id, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return true;
			}	
		}
		else {
			
		}
		return false;
	}

	private void selectMsMsFeatures() throws Exception {

		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Fetching MS2 features ...";
		String query =
			"SELECT DISTINCT F.FEATURE_ID, F.MZ_OF_INTEREST, F.RETENTION_TIME, " +
			"I.ACQUISITION_METHOD_ID, M.EXTRACTION_METHOD_ID, S.EXPERIMENT_ID, S.SAMPLE_ID, " +
			"T.STOCK_SAMPLE_ID, I.INJECTION_ID " + 
			"FROM MSMS_PARENT_FEATURE F, " +
			"DATA_ANALYSIS_MAP M, " +
			"INJECTION I, " +
			"PREPARED_SAMPLE P, " +
			"SAMPLE S, " +
			"STOCK_SAMPLE T " +
			"WHERE F.POLARITY = ? " +
			"AND S.EXPERIMENT_ID = ? " +
			"AND  F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID " +
			"AND M.INJECTION_ID = I.INJECTION_ID " +		
			"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
			"AND P.SAMPLE_ID = S.SAMPLE_ID " +
			"AND S.STOCK_SAMPLE_ID = T.STOCK_SAMPLE_ID " +
			"AND F.BASE_PEAK IS NOT NULL " +
			"ORDER BY F.MZ_OF_INTEREST, F.RETENTION_TIME";
			
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		
		ps.setString(1, this.polarity.getCode());
		ps.setString(2, this.experiment.getId());
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		HashSet<DataFile>dataFiles = new HashSet<DataFile>();
		while (rs.next()) {

			String id = rs.getString("FEATURE_ID");
			double rt = rs.getDouble("RETENTION_TIME");
			double mz = rs.getDouble("MZ_OF_INTEREST");
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(mz) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);

			MsFeature f = new MsFeature(id, name, rt);
			f.setPolarity(polarity);
			f.setAnnotatedObjectType(AnnotatedObjectType.MSMS_FEATURE);
			IDTMsDataUtils.attachMS1SpectrumForMsMs(f, conn);
			MsFeatureInfoBundle bundle = new MsFeatureInfoBundle(f);
			bundle.setAcquisitionMethod(
				IDTDataCash.getAcquisitionMethodById(rs.getString("ACQUISITION_METHOD_ID")));
			bundle.setDataExtractionMethod(
				IDTDataCash.getDataExtractionMethodById(rs.getString("EXTRACTION_METHOD_ID")));
			bundle.setExperiment(
				IDTDataCash.getExperimentById(rs.getString("EXPERIMENT_ID")));
			StockSample stockSample =
				IDTDataCash.getStockSampleById(rs.getString("STOCK_SAMPLE_ID"));
			bundle.setStockSample(stockSample);
			IDTExperimentalSample sample =
				IDTUtils.getExperimentalSampleById(rs.getString("SAMPLE_ID"), conn);
			bundle.setSample(sample);
			bundle.setInjectionId(rs.getString("INJECTION_ID"));
			
			DataFile df = dataFiles.stream().
					filter(file -> file.getInjectionId().equals(bundle.getInjectionId())).
					findFirst().orElse(null);
			if(df == null) {
				Injection inj = IDTRawDataUtils.getInjectionForId(bundle.getInjectionId());
				if(inj != null) {
					df = new DataFile(inj);
					dataFiles.add(df);
				}
			}
			if(df != null)
				bundle.setDataFile(df);
			
			features.add(bundle);
			processed++;
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	private void attachExperimentalTandemSpectra() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MSMS data ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			try {
				IDTMsDataUtils.attachExperimentalTandemSpectra(fb.getMsFeature(), conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private void attachMsMsLibraryIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding MSMS library identifications ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			try {
				IDTMsDataUtils.attachMsMsLibraryIdentifications(fb.getMsFeature(), conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void retievePepSearchParameters() {
				
		Set<String> pepSearchParIds = features.stream().
			flatMap(f -> f.getMsFeature().getIdentifications().stream().
					filter(i -> i.getReferenceMsMsLibraryMatch() != null)).
			map(m -> m.getReferenceMsMsLibraryMatch().getSearchParameterSetId()).
			distinct().collect(Collectors.toSet());
		
		if(pepSearchParIds.isEmpty())
			return;
		
		for(String id : pepSearchParIds)
			IDTDataCash.getNISTPepSearchParameterObjectById(id);		
	}
	
	private void attachMsMsManualIdentities() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding manual identifications ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			try {
				IDTMsDataUtils.attachMsMsManualIdentifications(fb.getMsFeature(), conn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void attachAnnotations() throws Exception {

		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding annotations ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			
			Collection<ObjectAnnotation>featureAnnotations = new ArrayList<ObjectAnnotation>();
			try {
				 featureAnnotations = AnnotationUtils.getObjectAnnotations(
						 AnnotatedObjectType.MSMS_FEATURE, 
						 fb.getMsFeature().getId(), conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!featureAnnotations.isEmpty())
				featureAnnotations.stream().forEach(a -> fb.getMsFeature().addAnnotation(a));
			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	private void attachFollowupSteps() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Adding follow-up steps ...";
		total = features.size();
		processed = 0;
		for(MsFeatureInfoBundle fb : features) {
			
			IdFollowupUtils.attachIdFollowupStepsToMSMSFeature(fb, conn);			
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void selectFeaturesToDelete() {
		
		taskDescription = "Finding duplicates to delete ...";
		total = features.size();
		processed = 0;
		
		for(MsFeatureInfoBundle feature : features) {
			
			if(featuresToDelete.contains(feature) || feature.getPrecursorMz() == null) {
				processed++;
				continue;
			}
			String df = feature.getDataFile().getName();
			double rt = feature.getMsFeature().getRetentionTime();
			double mz = feature.getPrecursorMz();	
			DataExtractionMethod method = feature.getDataExtractionMethod();
			MsFeatureInfoBundle[] duplicates = 
					features.stream().
					filter(f -> !featuresToDelete.contains(f)).
//					filter(f -> f.getDataFile().getName().equals(df)).
//					filter(f -> f.getDataExtractionMethod().equals(method)).
					filter(f -> f.getRetentionTime() == rt).
					filter(f -> f.getPrecursorMz() != null).
					filter(f -> f.getPrecursorMz() == mz).
					toArray(size -> new MsFeatureInfoBundle[size]) ;
			
			if(duplicates.length <= 1) {
				processed++;
				continue;
			}
			MsFeatureInfoBundle toLeave = duplicates[0];
			boolean updateFollowups = false;
			boolean updateIds = false;
			boolean updateAnnotations = false;
			for(int i=1; i<duplicates.length; i++) {
				
				//	Add followup steps
				if(!duplicates[i].getIdFollowupSteps().isEmpty()) {
					
					for(MSFeatureIdentificationFollowupStep flStep : duplicates[i].getIdFollowupSteps()) {
						
						if(!toLeave.getIdFollowupSteps().contains(flStep)) {
							toLeave.getIdFollowupSteps().add(flStep);
							updateFollowups = true;
						}
					}
				}				
				//	Add identifications
				if(!duplicates[i].getMsFeature().getIdentifications().isEmpty()) {
					
					for(MsFeatureIdentity id : duplicates[i].getMsFeature().getIdentifications()) {
						
						if(!toLeave.getMsFeature().getIdentifications().contains(id)) {
							toLeave.getMsFeature().getIdentifications().add(id);
							updateIds = true;
						}
					}
				}
				if(toLeave.getMsFeature().getPrimaryIdentity() == null && duplicates[i].getMsFeature().getPrimaryIdentity() != null) {
					toLeave.getMsFeature().setPrimaryIdentity(duplicates[i].getMsFeature().getPrimaryIdentity());
					updateIds = true;
				}
				//	Manual annotations
				if(!duplicates[i].getMsFeature().getAnnotations().isEmpty()) {
					
					for(ObjectAnnotation annotation : duplicates[i].getMsFeature().getAnnotations())
						toLeave.getMsFeature().addAnnotation(annotation);
					
					updateAnnotations = true;
				}	
				featuresToDelete.add(duplicates[i]);
			}
			if(updateFollowups)
				featuresToUpdateFollowups.add(toLeave);
			
			if(updateIds)
				featuresToUpdateIds.add(toLeave);
			
			if(updateAnnotations)
				featuresToUpdateAnnotations.add(toLeave);
			
			processed++;
		}
	}
	
	private void updateFeatureData() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Updating data for unique features ...";	
		String sql = null;
		String sql2 = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		
		if(!featuresToUpdateFollowups.isEmpty()) {
			
			total = featuresToUpdateFollowups.size();
			processed = 0;
			
			sql = "DELETE FROM MSMS_FEATURE_FOLLOWUP_STEPS WHERE MSMS_PARENT_FEATURE_ID = ?";
			ps = conn.prepareStatement(sql);
			sql2 = "INSERT INTO MSMS_FEATURE_FOLLOWUP_STEPS (MSMS_PARENT_FEATURE_ID, FOLLOWUP_STEP_ID) VALUES (?, ?)";
			ps2 = conn.prepareStatement(sql2);
			
			for(MsFeatureInfoBundle feature : featuresToUpdateFollowups) {
				ps.setString(1, feature.getMsFeature().getId());
				ps.executeUpdate();
				
				ps2.setString(1, feature.getMsFeature().getId());
				for(MSFeatureIdentificationFollowupStep fuStep : feature.getIdFollowupSteps()) {
					ps2.setString(2, fuStep.getId());
					ps2.addBatch();
				}
				ps2.executeBatch();
				processed++;
			}
			ps.close();
			ps2.close();
		}
		//	TODO this is limited to MSMS library IDs for now for simplicity
		if(!featuresToUpdateIds.isEmpty()) {
			
			sql = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET MSMS_FEATURE_ID = ? MATCH_ID = ?";
			ps = conn.prepareStatement(sql);		
			total = featuresToUpdateIds.size();
			processed = 0;
			for(MsFeatureInfoBundle feature : featuresToUpdateIds) {
				
				ps.setString(1, feature.getMsFeature().getId());
				for(MsFeatureIdentity cid : feature.getMsFeature().getIdentifications()) {
					ps.setString(2, cid.getUniqueId());
					ps.addBatch();
				}
				ps.executeBatch();
				IdentificationUtils.setMSMSFeaturePrimaryIdentity(
						feature.getMsFeature().getId(), feature.getMsFeature().getPrimaryIdentity(), conn);
				processed++;
			}
			ps.close();
		}
		if(!featuresToUpdateAnnotations.isEmpty()) {
			
			total = featuresToUpdateAnnotations.size();
			processed = 0;
			sql = "UPDATE OBJECT_ANNOTATIONS SET OBJECT_ID = ? WHERE ANNOTATION_ID = ?";
			ps = conn.prepareStatement(sql);
			for(MsFeatureInfoBundle feature : featuresToUpdateAnnotations) {
				
				ps.setString(1, feature.getMsFeature().getId());
				for(ObjectAnnotation annotation : feature.getMsFeature().getAnnotations()) {
					ps.setString(2, annotation.getUniqueId());
					ps.addBatch();
				}
				ps.executeBatch();
				processed++;
			}
			ps.close();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private void deleteDuplicates() throws Exception {
	
		if(featuresToDelete.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		taskDescription = "Deleting duplicates ...";
		total = features.size();
		processed = 0;
		String sql = "DELETE FROM MSMS_PARENT_FEATURE WHERE FEATURE_ID = ?";
		PreparedStatement ps = conn.prepareStatement(sql);
		int count = 0;
		for(MsFeatureInfoBundle feature : featuresToDelete) {
						
			ps.setString(1, feature.getMsFeature().getId());
			ps.addBatch();
			count++;
			if(count % 100 == 0)
				ps.executeBatch();
		}
		ps.executeBatch();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public Task cloneTask() {

		return new IDTMSMSDuplicateMSMSFeatureCleanupTask(
				 polarity, 
				 experiment);
	}
	
	public Collection<MsFeatureInfoBundle> getSelectedFeatures() {
		return features;
	}
}
