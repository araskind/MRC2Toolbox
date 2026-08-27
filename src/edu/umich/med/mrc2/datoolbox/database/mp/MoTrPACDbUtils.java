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

package edu.umich.med.mrc2.datoolbox.database.mp;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotracSubjectType;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotrpacSampleType;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.DocumentUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MoTrPACDbUtils {
	
	private static final Logger logger = LogManager.getLogger(MoTrPACDbUtils.class);

	private MoTrPACDbUtils() {
		/* This utility class should not be instantiated */
	}
	
	/*
	 * Motrpac studies
	 * */
	
	public static Collection<MoTrPACStudy>getMotrpacStudies() throws SQLException {

		ArrayList<MoTrPACStudy> studies = new ArrayList<MoTrPACStudy>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT STUDY_ID, STUDY_CODE, SUBJECT_TYPE, "
			+ "DESCRIPTION FROM MOTRPAC_STUDY ORDER BY STUDY_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();		
			while (rs.next()) {				
				MotracSubjectType subjectType = 
						MoTrPACDatabaseCache.getMotracSubjectTypeByName(
								rs.getString("SUBJECT_TYPE"));
				MoTrPACStudy study = new MoTrPACStudy(
						rs.getString("STUDY_ID"), 
						rs.getString("STUDY_CODE"), 
						subjectType,
						rs.getString("DESCRIPTION"));		
				studies.add(study);
				attachLimsExperimentsToMotrpacStudy(study, conn);
				attachAssaysToMotrpacStudy(study, conn);
				attachTissuesToMotrpacStudyExperiments(study, conn);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return studies;
	}

	public static void attachLimsExperimentsToMotrpacStudy(
			MoTrPACStudy study, 
			Connection conn) throws SQLException {
		String query =
				"SELECT EXPERIMENT_ID FROM MOTRPAC_LIMS_EXPERIMENT_MAP "
				+ "WHERE STUDY_ID = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				String exId = rs.getString("EXPERIMENT_ID");
				LIMSExperiment experiment = 
						LIMSDataCache.getExperimentById(exId);
				if(experiment != null)				
					study.addExperiment(experiment);			
			}
			rs.close();
		}
	}
	
	public static void attachAssaysToMotrpacStudy(
			MoTrPACStudy study, 
			Connection conn) throws SQLException {
		String query =
				"SELECT ASSAY_ID FROM MOTRPAC_STUDY_ASSAYS WHERE STUDY_ID = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {				
				String assayId = rs.getString("ASSAY_ID");
				MoTrPACAssay assay = 
						MoTrPACDatabaseCache.getMotrpacAssayById(assayId);
				if(assay != null)				
					study.addAssay(assay);			
			}
			rs.close();
		}
	}

	public static void attachTissuesToMotrpacStudyExperiments(
			MoTrPACStudy study, 
			Connection conn) throws SQLException {
		String query =
				"SELECT TISSUE_CODE FROM MOTRPAC_EXPERIMENT_TISSUE_MAP "
				+ "WHERE EXPERIMENT_ID = ? ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(LIMSExperiment experiment : study.getExperiments()) {			
				ps.setString(1, experiment.getId());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {			
					String tissueCode = rs.getString("TISSUE_CODE");
					MoTrPACTissueCode code = 
							MoTrPACDatabaseCache.getMotrpacTissueCodeById(tissueCode);
					if(tissueCode != null)				
						study.addTissueForExperiment(experiment, code);			
				}
				rs.close();
			}
		}
	}

	public static void addNewMotrpacStudy(MoTrPACStudy study) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String studyId = SQLUtils.getNextIdFromSequence(conn, 
				"MOTRPAC_STUDY_SEQ",
				DataPrefix.MOTRPAC_STUDY,
				"0",
				3);
		study.setId(studyId);
		String query =
			"INSERT INTO MOTRPAC_STUDY (STUDY_ID, STUDY_CODE, "
			+ "SUBJECT_TYPE, DESCRIPTION) VALUES (?, ?, ?, ?)";	
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ps.setString(2, study.getCode());
			ps.setString(3, study.getSubjectType().getSubjectType());
			ps.setString(4, study.getDescription());
			ps.executeUpdate();
		}		
		for(MoTrPACAssay assay : study.getAssays())
			addAssayToStudy(study, assay, conn);
		
		for(LIMSExperiment experiment : study.getExperiments()) {			
			addExperimentToStudy(study, experiment, conn);		
			for(MoTrPACTissueCode code : study.getTissueCodesForExperiment(experiment))
				addTissueCodeForExperiment(experiment, code, conn);
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editMotrpacStudy(MoTrPACStudy study) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		MoTrPACStudy original = getMotrpacStudyById(study.getId(), conn);
		//	Update study data
		String query =
			"UPDATE MOTRPAC_STUDY SET STUDY_CODE = ?, SUBJECT_TYPE = ?, "
			+ "DESCRIPTION = ? WHERE STUDY_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getCode());
			ps.setString(2, study.getSubjectType().getSubjectType());
			ps.setString(3, study.getDescription());
			ps.setString(4, study.getId());		
			ps.executeUpdate();
		}		
		//	Update assays
		Collection<MoTrPACAssay> assignedAssays = study.getAssays();
		Collection<MoTrPACAssay> assays2remove = new ArrayList<MoTrPACAssay>(); 
		Collection<MoTrPACAssay> assays2add = new ArrayList<MoTrPACAssay>(); 
		Collection<MoTrPACAssay>currentAssays = original.getAssays();
		for(MoTrPACAssay a : currentAssays) {
			
			if(!assignedAssays.contains(a))
				assays2remove.add(a);			
		}
		for(MoTrPACAssay a : assignedAssays) {
			
			if(!currentAssays.contains(a))
				assays2add.add(a);			
		}			 
		for(MoTrPACAssay a : assays2remove)
			removeAssayFromStudy(original, a, conn);
		
		for(MoTrPACAssay a : assays2add)
			addAssayToStudy(original, a, conn);
		
		//	Update experiments
		Collection<LIMSExperiment> assignedExperiments = study.getExperiments();
		Collection<LIMSExperiment> experiments2remove = new ArrayList<LIMSExperiment>(); 
		Collection<LIMSExperiment> experiments2add = new ArrayList<LIMSExperiment>(); 
		Collection<LIMSExperiment> currentExperiments = original.getExperiments();
		for(LIMSExperiment a : currentExperiments) {
			
			if(!assignedExperiments.contains(a))
				experiments2remove.add(a);			
		}
		for(LIMSExperiment a : assignedExperiments) {
			
			if(!currentExperiments.contains(a))
				experiments2add.add(a);			
		}
		for(LIMSExperiment a : experiments2remove)
			removeExperimentFromStudy(original, a, conn);
		
		for(LIMSExperiment a : experiments2add)
			addExperimentToStudy(original, a, conn);
					
		//	Update experiment tissues
		for(LIMSExperiment experiment : study.getExperiments()) {
			
			Collection<MoTrPACTissueCode> assignedTissueCodes = study.getTissueCodesForExperiment(experiment);
			Collection<MoTrPACTissueCode> tissueCodes2remove = new ArrayList<MoTrPACTissueCode>(); 
			Collection<MoTrPACTissueCode> tissueCodes2add = new ArrayList<MoTrPACTissueCode>(); 
			Collection<MoTrPACTissueCode>currentTissueCodes = original.getTissueCodesForExperiment(experiment);
			if(currentTissueCodes == null)
				currentTissueCodes = new TreeSet<MoTrPACTissueCode>();
			
			for(MoTrPACTissueCode a : currentTissueCodes) {
				
				if(!assignedTissueCodes.contains(a))
					tissueCodes2remove.add(a);			
			}
			for(MoTrPACTissueCode a : assignedTissueCodes) {
				
				if(!currentTissueCodes.contains(a))
					tissueCodes2add.add(a);			
			}			 
			for(MoTrPACTissueCode a : tissueCodes2remove)
				removeTissueCodeFromExperiment(experiment, a, conn);
			
			for(MoTrPACTissueCode a : tissueCodes2add)
				addTissueCodeForExperiment(experiment, a, conn);
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static MoTrPACStudy getMotrpacStudyById(String studyId, Connection conn) throws SQLException {
		
		MoTrPACStudy study = null;
		String query =
			"SELECT STUDY_ID, STUDY_CODE, SUBJECT_TYPE, "
			+ "DESCRIPTION FROM MOTRPAC_STUDY WHERE STUDY_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, studyId);
			ResultSet rs = ps.executeQuery();		
			while (rs.next()) {
				
				MotracSubjectType subjectType = 
						MoTrPACDatabaseCache.getMotracSubjectTypeByName(rs.getString("SUBJECT_TYPE"));
				study = new MoTrPACStudy(
						rs.getString("STUDY_ID"), 
						rs.getString("STUDY_CODE"), 
						subjectType,
						rs.getString("DESCRIPTION"));		
				attachLimsExperimentsToMotrpacStudy(study, conn);
				attachAssaysToMotrpacStudy(study, conn);
				attachTissuesToMotrpacStudyExperiments(study, conn);
			}
			rs.close();
		}
		return study;
	}
	
	public static void deleteMotrpacStudy(MoTrPACStudy study) throws SQLException {

		String query = "DELETE FROM MOTRPAC_STUDY WHERE STUDY_CODE = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getCode());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addExperimentToStudy(MoTrPACStudy study, LIMSExperiment experiment) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		addExperimentToStudy(study, experiment, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addExperimentToStudy(
			MoTrPACStudy study, LIMSExperiment experiment, Connection conn) throws SQLException {
		String query =
				"INSERT INTO MOTRPAC_LIMS_EXPERIMENT_MAP "
				+ "(STUDY_ID, EXPERIMENT_ID) VALUES (?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ps.setString(2, experiment.getId());
			ps.executeUpdate();
		}
	}
	
	public static void removeExperimentFromStudy(MoTrPACStudy study, LIMSExperiment experiment) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		removeExperimentFromStudy(study, experiment, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void removeExperimentFromStudy(
			MoTrPACStudy study, LIMSExperiment experiment, Connection conn) throws SQLException {
		String query =
				"DELETE FROM MOTRPAC_LIMS_EXPERIMENT_MAP "
				+ "WHERE STUDY_ID = ? AND EXPERIMENT_ID = ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ps.setString(2, experiment.getId());
			ps.executeUpdate();
		}
	}
	
	public static void addAssayToStudy(MoTrPACStudy study, MoTrPACAssay assay) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		addAssayToStudy(study, assay, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void addAssayToStudy(MoTrPACStudy study, MoTrPACAssay assay, Connection conn) throws SQLException {

		String query =
				"INSERT INTO MOTRPAC_STUDY_ASSAYS (STUDY_ID, ASSAY_ID) VALUES (?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ps.setString(2, assay.getAssayId());
			ps.executeUpdate();
		}
	}
	
	public static void removeAssayFromStudy(MoTrPACStudy study, MoTrPACAssay assay) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		removeAssayFromStudy(study, assay, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void removeAssayFromStudy(MoTrPACStudy study, MoTrPACAssay assay, Connection conn) throws SQLException {

		String query =
				"DELETE FROM MOTRPAC_STUDY_ASSAYS WHERE STUDY_ID = ? AND ASSAY_ID = ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, study.getId());
			ps.setString(2, assay.getAssayId());
			ps.executeUpdate();
		}
	}
	
	public static void addTissueCodeForExperiment(LIMSExperiment experiment, MoTrPACTissueCode tissueCode) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		addTissueCodeForExperiment(experiment, tissueCode, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void addTissueCodeForExperiment(
			LIMSExperiment experiment, MoTrPACTissueCode tissueCode, Connection conn) throws SQLException {
		String query =
				"INSERT INTO MOTRPAC_EXPERIMENT_TISSUE_MAP (EXPERIMENT_ID, TISSUE_CODE) VALUES (?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experiment.getId());
			ps.setString(2, tissueCode.getCode());
			ps.executeUpdate();
		}
	}

	public static void removeTissueCodeFromExperiment(LIMSExperiment experiment, MoTrPACTissueCode tissueCode) throws SQLException {
		
		Connection conn = ConnectionManager.getConnection();
		removeTissueCodeFromExperiment(experiment, tissueCode, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void removeTissueCodeFromExperiment(
			LIMSExperiment experiment, MoTrPACTissueCode tissueCode, Connection conn) throws SQLException {
		String query =
				"DELETE FROM MOTRPAC_EXPERIMENT_TISSUE_MAP WHERE EXPERIMENT_ID = ? AND TISSUE_CODE = ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, experiment.getId());
			ps.setString(2, tissueCode.getCode());
			ps.executeUpdate();
		}
	}
	
	/*
	 * Motrpac assays
	 * */
	public static Collection<MoTrPACAssay>getMotrpacAssays() throws SQLException {

		ArrayList<MoTrPACAssay> assays = new ArrayList<MoTrPACAssay>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ASSAY_ID, CODE, DESCRIPTION, POLARITY, BUCKET_CODE "
			+ "FROM MOTRPAC_ASSAY ORDER BY ASSAY_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();		
			while (rs.next()) {
				MoTrPACAssay assay = new MoTrPACAssay(
						rs.getString("ASSAY_ID"), 
						rs.getString("CODE"),
						rs.getString("DESCRIPTION"),
						rs.getString("POLARITY"),
						rs.getString("BUCKET_CODE"));		
				assays.add(assay);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return assays;
	}

	public static void addNewMotrpacAssay(MoTrPACAssay assay) throws SQLException {

		String query =
			"INSERT INTO MOTRPAC_ASSAY (ASSAY_ID, CODE, DESCRIPTION, "
			+ "POLARITY, BUCKET_CODE) VALUES (?, ?, ?, ?, ?)";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, assay.getAssayId());
			ps.setString(2, assay.getCode());
			ps.setString(3, assay.getDescription());
			ps.setString(4, assay.getPolarity());
			ps.setString(5, assay.getBucketCode());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editMotrpacAssay(MoTrPACAssay assay) throws SQLException {

		String query =
			"UPDATE MOTRPAC_ASSAY SET CODE = ?, DESCRIPTION = ?, "
			+ "POLARITY = ?, BUCKET_CODE = ? WHERE ASSAY_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, assay.getCode());
			ps.setString(2, assay.getDescription());
			ps.setString(3, assay.getPolarity());		
			ps.setString(4, assay.getBucketCode());
			ps.setString(5, assay.getAssayId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMotrpacAsssay(MoTrPACAssay assay) throws SQLException {

		String query = "DELETE FROM MOTRPAC_ASSAY WHERE ASSAY_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, assay.getAssayId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	/*
	 * Motrpac subject types
	 * 
	 * */
	public static Collection<MotracSubjectType>getMotrpacSubjectTypes() throws SQLException {

		ArrayList<MotracSubjectType> subjectTypes = new ArrayList<MotracSubjectType>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT SUBJECT_TYPE FROM MOTRPAC_SUBJECT_TYPE ORDER BY 1";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();			
			while (rs.next()) {
				MotracSubjectType subjectType = new MotracSubjectType(rs.getString("SUBJECT_TYPE"));		
				subjectTypes.add(subjectType);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return subjectTypes;
	}
	
	/*
	 * Motrpac sample types
	 * 
	 * */
	
	public static Collection<MotrpacSampleType>getMotrpacSampleTypes() throws SQLException {

		ArrayList<MotrpacSampleType> sampleTypes = new ArrayList<MotrpacSampleType>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT TYPE_ID, DESCRIPTION FROM MOTRPAC_SAMPLE_TYPE ORDER BY TYPE_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();	
			while (rs.next()) {
				MotrpacSampleType sampleType = new MotrpacSampleType(
						rs.getString("TYPE_ID"), 
						rs.getString("DESCRIPTION"));
				
				sampleTypes.add(sampleType);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return sampleTypes;
	}

	public static void addNewMotrpacSampleType(MotrpacSampleType sampleType) throws SQLException {

		String query =
			"INSERT INTO MOTRPAC_SAMPLE_TYPE (TYPE_ID, DESCRIPTION) VALUES (?, ?)";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleType.getSampleType());
			ps.setString(2, sampleType.getDescription());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editMotrpacSampleType(MotrpacSampleType sampleType) throws SQLException {

		String query =
			"UPDATE MOTRPAC_SAMPLE_TYPE SET DESCRIPTION = ? WHERE TYPE_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleType.getDescription());
			ps.setString(2, sampleType.getSampleType());		
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMotrpacSampleType(MotrpacSampleType sampleType) throws SQLException {

		String query = "DELETE FROM MOTRPAC_SAMPLE_TYPE WHERE TYPE_ID = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, sampleType.getSampleType());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	/*
	 * Motrpac tissue codes
	 * */
	
	public static Collection<MoTrPACTissueCode>getMotrpacTissueCodes() throws SQLException {
		
		ArrayList<MoTrPACTissueCode> tissueCodes = new ArrayList<MoTrPACTissueCode>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT TISSUE_CODE, DESCRIPTION FROM MOTRPAC_TISSUE_CODE ORDER BY TISSUE_CODE";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MoTrPACTissueCode method = new MoTrPACTissueCode(
						rs.getString("TISSUE_CODE"), 
						rs.getString("DESCRIPTION"));
				
				tissueCodes.add(method);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return tissueCodes;
	}

	public static void addNewMotrpacTissueCode(MoTrPACTissueCode tissueCode) throws SQLException {

		String query =
			"INSERT INTO MOTRPAC_TISSUE_CODE (TISSUE_CODE, DESCRIPTION) VALUES (?, ?)";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, tissueCode.getCode());
			ps.setString(2, tissueCode.getDescription());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editMotrpacTissueCode(MoTrPACTissueCode tissueCode) throws SQLException {

		String query =
			"UPDATE MOTRPAC_TISSUE_CODE SET DESCRIPTION = ? WHERE TISSUE_CODE = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, tissueCode.getDescription());
			ps.setString(2, tissueCode.getCode());		
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMotrpacTissueCode(MoTrPACTissueCode tissueCode) throws SQLException {

		String query = "DELETE FROM MOTRPAC_TISSUE_CODE WHERE TISSUE_CODE = ?";
		Connection conn = ConnectionManager.getConnection();
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, tissueCode.getCode());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<MoTrPACReportCodeBlock> getMotrpacReportCodeBlocks() throws SQLException {
		
		Collection<MoTrPACReportCodeBlock>codeBlocks = new TreeSet<MoTrPACReportCodeBlock>();
		Connection conn = ConnectionManager.getConnection();
		String sql =
			"SELECT BLOCK_ID, STAGE_ORDER FROM MOTRPAC_REPORT_STAGE";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MoTrPACReportCodeBlock block = new MoTrPACReportCodeBlock(
						rs.getString("BLOCK_ID"), 
						rs.getInt("STAGE_ORDER"));
				
				codeBlocks.add(block);
			}
			rs.close();
		}
		sql = "SELECT OPTION_NAME, OPTION_CODE FROM MOTRPAC_REPORT_CODES WHERE BLOCK_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			for(MoTrPACReportCodeBlock b : codeBlocks) {
				ps.setString(1, b.getBlockId());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					MoTrPACReportCode code = new MoTrPACReportCode(
							rs.getString("OPTION_NAME"), 
							rs.getString("OPTION_CODE"));				
					b.getBlockCodes().add(code);
				}
				rs.close();
			}
		}
		ConnectionManager.releaseConnection(conn);
		return codeBlocks;
	}
	
	public static void insertNewMotrpacReport(
			File reportFile, 
			int versionNumber,
			MoTrPACStudy study,
			LIMSExperiment experiment,
			MoTrPACAssay assay,
			MoTrPACTissueCode tissueCode,
			Map<MoTrPACReportCodeBlock,MoTrPACReportCode>reportStageDefinition,
			LIMSUser user) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		
		//	Upload file 
		String extension = FilenameUtils.getExtension(reportFile.getName());
		DocumentFormat format = DocumentFormat.getFormatByFileExtension(extension);
		String documentId = null;
		try {
			documentId = DocumentUtils.insertDocument(
					reportFile, 
					reportFile.getName(), 
					format, 
					conn);
		} catch (Exception e) {
			logger.error(String.format("Failed to insert document file %s", reportFile.getName()), e);
		}
		if(documentId == null) {
			throw new SQLException("Failed to upload report document");
		}		
		String newReportId = SQLUtils.getNextIdFromSequence(conn, 
				"MOTRPAC_REPORT_SEQ",
				DataPrefix.MOTRPAC_REPORT,
				"0",
				9);
		
		String sql = "INSERT INTO MOTRPAC_REPORT ("
				+ "MOTRPAC_REPORT_ID, DOCUMENT_ID, RESEARCHER_ID, DATE_CREATED, "
				+ "STUDY_ID, EXPERIMENT_ID, ASSAY_ID, TISSUE_CODE, VERSION_NUMBER) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)){	
			ps.setString(1, newReportId);
			ps.setString(2, documentId);
			ps.setString(3, user.getId());
			ps.setDate(4, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(5, study.getId());
			ps.setString(6, experiment.getId());
			ps.setString(7, assay.getAssayId());
			ps.setString(8, tissueCode.getCode());
			ps.setInt(9, versionNumber);		
			ps.executeUpdate();		
		}		
		sql = "INSERT INTO MOTRPAC_REPORT_STAGE_DEFINITION "
				+ "(MOTRPAC_REPORT_ID, BLOCK_ID, OPTION_NAME) VALUES (?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, newReportId);
			for(Entry<MoTrPACReportCodeBlock,MoTrPACReportCode>stage : reportStageDefinition.entrySet()) {		
				ps.setString(2, stage.getKey().getBlockId());
				ps.setString(3, stage.getValue().getOptionName());
				ps.addBatch();
			}
			ps.executeBatch();
		}	
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<MoTrPACReport> getMoTrPACReports() throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		Collection<MoTrPACReport> reports = getMoTrPACReports(conn);
		ConnectionManager.releaseConnection(conn);
		return reports;
	}
	
	public static Collection<MoTrPACReport> getMoTrPACReports(Connection conn) throws SQLException {

		Collection<MoTrPACReport> reports = new TreeSet<MoTrPACReport>();
		String sql = 
				"SELECT R.MOTRPAC_REPORT_ID, R.RESEARCHER_ID, R.DATE_CREATED, "
				+ "R.STUDY_ID, R.EXPERIMENT_ID, R.ASSAY_ID, R.TISSUE_CODE, R.VERSION_NUMBER, "
				+ "D.DOCUMENT_ID, D.DOCUMENT_NAME, D.DOCUMENT_FORMAT "
				+ "FROM MOTRPAC_REPORT R, DOCUMENTS D "
				+ "WHERE R.DOCUMENT_ID = D.DOCUMENT_ID "
				+ "ORDER BY MOTRPAC_REPORT_ID";
		String stageSql = 
				"SELECT BLOCK_ID, OPTION_NAME FROM MOTRPAC_REPORT_STAGE_DEFINITION "
				+ "WHERE MOTRPAC_REPORT_ID = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(sql)){
		
			try(PreparedStatement stagePs = conn.prepareStatement(stageSql)){		
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {				
					String id = rs.getString("MOTRPAC_REPORT_ID");
					Date dateCreated = new java.util.Date(rs.getDate("DATE_CREATED").getTime());
					LIMSUser createBy = IDTDataCache.getUserById(rs.getString("RESEARCHER_ID"));
					DocumentFormat linkedDocumentFormat = 
							DocumentFormat.getFormatByFileExtension(rs.getString("DOCUMENT_FORMAT"));			
					MoTrPACReport report = new MoTrPACReport(
							id, 
							dateCreated, 
							createBy, 
							rs.getString("DOCUMENT_ID"),
							rs.getString("DOCUMENT_NAME"), 
							linkedDocumentFormat);			
					report.setVersionNumber(rs.getInt("VERSION_NUMBER"));
					MoTrPACStudy study = MoTrPACDatabaseCache.getMotrpacStudyById(rs.getString("STUDY_ID"));
					report.setStudy(study);
					LIMSExperiment experiment = LIMSDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));
					report.setExperiment(experiment);		
					MoTrPACAssay assay = MoTrPACDatabaseCache.getMotrpacAssayById(rs.getString("ASSAY_ID"));
					report.setAssay(assay);
					MoTrPACTissueCode tissue = MoTrPACDatabaseCache.getMotrpacTissueCodeById(rs.getString("TISSUE_CODE"));
					report.setTissueCode(tissue);			
					stagePs.setString(1, id);
					ResultSet stageRs = stagePs.executeQuery();
					while(stageRs.next()) {
		
						MoTrPACReportCodeBlock block = 
								MoTrPACDatabaseCache.getMoTrPACReportCodeBlockById(stageRs.getString("BLOCK_ID"));
						MoTrPACReportCode code = block.getMoTrPACReportCodeByName(stageRs.getString("OPTION_NAME"));
						report.setReportStageBlock(block, code);
					}
					stageRs.close();
					reports.add(report);
				}
				rs.close();
			}
		}
		return reports;
	}
	
	public static void updateMotrpacReportMetadata(MoTrPACReport report) throws SQLException {

		Connection conn = ConnectionManager.getConnection();

		String sql = 
				"UPDATE MOTRPAC_REPORT SET STUDY_ID = ?, "
				+ "EXPERIMENT_ID = ?, ASSAY_ID = ?, TISSUE_CODE = ?, VERSION_NUMBER = ? "
				+ "WHERE MOTRPAC_REPORT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){		
			ps.setString(1, report.getStudy().getId());
			ps.setString(2, report.getExperiment().getId());
			ps.setString(3, report.getAssay().getAssayId());
			ps.setString(4, report.getTissueCode().getCode());
			ps.setInt(5, report.getVersionNumber());
			ps.setString(6, report.getId());
			ps.executeUpdate();
		}
		sql = "DELETE FROM MOTRPAC_REPORT_STAGE_DEFINITION WHERE MOTRPAC_REPORT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, report.getId());
			ps.executeUpdate();
		}
		sql = "INSERT INTO MOTRPAC_REPORT_STAGE_DEFINITION (MOTRPAC_REPORT_ID, BLOCK_ID, OPTION_NAME) VALUES (?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, report.getId());
			for(Entry<MoTrPACReportCodeBlock,MoTrPACReportCode>stage : report.getReportStage().entrySet()) {			
				ps.setString(2, stage.getKey().getBlockId());
				ps.setString(3, stage.getValue().getOptionName());
				ps.addBatch();
			}
			ps.executeBatch();
		}		
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteMoTrPACReport(MoTrPACReport report) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		DocumentUtils.deleteDocument(report.getLinkedDocumentId(), conn);	
		String query = "DELETE FROM MOTRPAC_REPORT WHERE MOTRPAC_REPORT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, report.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
}














