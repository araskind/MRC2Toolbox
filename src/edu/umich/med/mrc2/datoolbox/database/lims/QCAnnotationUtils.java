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

package edu.umich.med.mrc2.datoolbox.database.lims;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.text.Document;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.SQLParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

public class QCAnnotationUtils {

	//	TODO rewire to current database
	public static void insertAnnotation(
			AnalysisQcEventAnnotation annotation,
			Document rtfDocument) throws Exception {

		LIMSUser sysUser = annotation.getCreateBy();
		if(sysUser == null) {
			sysUser = MRC2ToolBoxCore.getIdTrackerUser();
			annotation.setCreateBy(sysUser);
		}
		if(sysUser == null)
			return;

		annotation.setLastModifiedBy(sysUser);
		
//		String annotationId = null;
//		Connection conn = MetLIMSConnectionManager.getConnection();
//		String query  =
//				"SELECT '" + DataPrefix.OBJECT_ANNOTATION.getName() +
//				"' || LPAD(ID_ANNOTATION_SEQ.NEXTVAL, 9, '0') AS ANNOTATION_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) {
//			annotationId = rs.getString("ANNOTATION_ID");
//			break;
//		}
//		annotation.setId(annotationId);
//		rs.close();
		
		Connection conn = ConnectionManager.getConnection();
		String annotationId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_ANNOTATION_SEQ",
				DataPrefix.OBJECT_ANNOTATION,
				"0",
				9);
		
		String query =
			"INSERT INTO ANALYSIS_QC_LOG (ANNOTATION_ID, EXPERIMENT_ID, "
			+ "SAMPLE_ID, ASSAY_ID, INSTRUMENT_ID, ANNOTATION_TEXT, CREATED_BY, "
			+ "CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, EVENT_TYPE, FORMATTED_ANNOTATION) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotationId);
		ps.setString(2, annotation.getExperimentId());
		ps.setString(3, annotation.getSampleId());
		ps.setString(4, annotation.getAssayId());
		ps.setString(5, annotation.getInstrumentId());
		ps.setString(6, annotation.getText());
		ps.setString(7, annotation.getCreateBy().getId());
		ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));		
		ps.setString(9, annotation.getLastModifiedBy().getId());
		ps.setDate(10, new java.sql.Date(new java.util.Date().getTime()));	
		ps.setString(11, annotation.getQcEventType().name());

		FileInputStream fis = null;
		File tmpRtf = null;
		if(rtfDocument == null) {
			ps.setBinaryStream(12, null, 0);
		}
		else {
			//	Write temp RTF file
			AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();
			tmpRtf = new File(MRC2ToolBoxCore.tmpDir + UUID.randomUUID().toString() + ".rtf");
			try {
				editor.write(tmpRtf.getAbsolutePath(), rtfDocument);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if(tmpRtf.exists()) {
				fis = new FileInputStream(tmpRtf);
				ps.setBinaryStream(12, fis, (int) tmpRtf.length());
			}
			else {
				ps.setBinaryStream(12, null, 0);
			}
		}
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		if(fis != null)
			fis.close();

		if(tmpRtf != null) {
			Path path = Paths.get(tmpRtf.getAbsolutePath());
	        Files.delete(path);
	    }		
	}

	public static void updateAnnotation(
			AnalysisQcEventAnnotation annotation, Document rtfDocument, LIMSUser noteEditor) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		annotation.setLastModifiedBy(noteEditor);
		annotation.setLastModified(new Date());

		String query =
			"UPDATE ANALYSIS_QC_LOG SET EXPERIMENT_ID = ?, "
			+ "SAMPLE_ID = ?, ASSAY_ID = ?, INSTRUMENT_ID = ?, ANNOTATION_TEXT = ?,  "
			+ "LAST_EDITED_BY = ?, LAST_EDITED_ON = ?, EVENT_TYPE = ?, "
			+ "FORMATTED_ANNOTATION = ? WHERE ANNOTATION_ID = ?";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getId());
		ps.setString(2, annotation.getSampleId());
		ps.setString(3, annotation.getAssayId());
		ps.setString(4, annotation.getInstrumentId());
		ps.setString(5, annotation.getText());
		ps.setString(6, annotation.getLastModifiedBy().getId());
		ps.setDate(7, new java.sql.Date(annotation.getLastModified().getTime()));
		ps.setString(8, annotation.getQcEventType().name());

		FileInputStream fis = null;
		File tmpRtf = null;
		if (rtfDocument == null) {
			ps.setBinaryStream(9, null, 0);
		} else {
			// Write temp RTF file
			AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();			
			tmpRtf = new File(MRC2ToolBoxCore.tmpDir + UUID.randomUUID().toString() + ".rtf");
			try {
				editor.write(tmpRtf.getAbsolutePath(), rtfDocument);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (tmpRtf.exists()) {
				fis = new FileInputStream(tmpRtf);
				ps.setBinaryStream(9, fis, (int) tmpRtf.length());
			} else {
				ps.setBinaryStream(9, null, 0);
			}
		}
		ps.setString(10, annotation.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		if (fis != null)
			fis.close();
	
		if (tmpRtf != null) {
			Path path = Paths.get(tmpRtf.getAbsolutePath());
			Files.delete(path);
		}	
	}

	public static void deleteAnnotation(AnalysisQcEventAnnotation annotation) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query =
			"DELETE FROM ANALYSIS_QC_LOG WHERE ANNOTATION_ID = ?";

		PreparedStatement  stmt = conn.prepareStatement(query);
		stmt.setString(1, annotation.getId());
		stmt.executeUpdate();
		stmt.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<AnalysisQcEventAnnotation> findAnnotations(
			QcEventType annotationCategory,
			LIMSInstrument instrument,
			LIMSExperiment experiment,
			Assay assay,
			ExperimentalSample sample,
			Date startDate,
			Date endDate,
			LIMSUser author) throws Exception {

		Collection<AnalysisQcEventAnnotation> annotations =
				new ArrayList<AnalysisQcEventAnnotation>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT ANNOTATION_ID, EXPERIMENT_ID, SAMPLE_ID,  " +
			"ASSAY_ID, INSTRUMENT_ID, ANNOTATION_TEXT, CREATED_BY,  " +
			"CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, EVENT_TYPE  " +
			"FROM ANALYSIS_QC_LOG WHERE  " +
			"CREATED_ON >= ? AND CREATED_ON <= ? ";

		Map<Integer,SQLParameter>parameterMap = new TreeMap<Integer,SQLParameter>();
		int paramCount = 3;
		if(annotationCategory != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, annotationCategory.name()));
			query += "AND EVENT_TYPE = ? ";
		}
		if(instrument != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, instrument.getInstrumentId()));
			query += "AND INSTRUMENT_ID = ? ";
		}
		if(experiment != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, experiment.getId()));
			query += "AND EXPERIMENT_ID = ? ";
		}
		if(assay != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, assay.getId()));
			query += "AND ASSAY_ID = ? ";
		}
		if(sample != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, sample.getId()));
			query += "AND SAMPLE_ID = ? ";
		}
		if(author != null) {
			parameterMap.put(paramCount++, new SQLParameter(String.class, author.getId()));
			query += "AND CREATED_BY = ? ";
		}
		//	System.out.println(query);

		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.add(Calendar.DATE, 1);

		PreparedStatement  ps = conn.prepareStatement(query);
		ps.setDate(1, new java.sql.Date(startDate.getTime()));
		ps.setDate(2, new java.sql.Date(c.getTime().getTime()));

		for(Entry<Integer, SQLParameter> entry : parameterMap.entrySet()) {

			if(entry.getValue().getClazz().equals(String.class))
				ps.setString(entry.getKey(), (String)entry.getValue().getValue());

			if(entry.getValue().getClazz().equals(Double.class))
				ps.setDouble(entry.getKey(), (Double)entry.getValue().getValue());
		}
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			String uniqueId = rs.getString("ANNOTATION_ID");
			String text = rs.getString("ANNOTATION_TEXT");
			Date dateCreated = new Date(rs.getDate("CREATED_ON").getTime());
			Date lastModified = dateCreated;
			if(rs.getDate("LAST_EDITED_ON") != null)
				lastModified = new Date(rs.getDate("LAST_EDITED_ON").getTime());

			LIMSUser createdBy = IDTDataCache.getUserById(rs.getString("CREATED_BY"));
			LIMSUser lastModifiedBy = createdBy;
			if(rs.getString("LAST_EDITED_BY") != null)
				lastModifiedBy = IDTDataCache.getUserById(rs.getString("LAST_EDITED_BY"));

			LIMSInstrument limsInstrument = null;
			if(rs.getString("INSTRUMENT_ID") != null)
				limsInstrument = IDTDataCache.getInstrumentById(rs.getString("INSTRUMENT_ID"));

			LIMSExperiment limsExperiment = null;
			if(rs.getString("EXPERIMENT_ID") != null)
				limsExperiment = LIMSDataCache.getExperimentById(rs.getString("EXPERIMENT_ID"));

			ExperimentalSample limsSample = null;
			if(rs.getString("SAMPLE_ID") != null)
				limsSample = new ExperimentalSample(rs.getString("SAMPLE_ID"), rs.getString("SAMPLE_ID"));

			Assay limsAssay = null;
			if(rs.getString("ASSAY_ID") != null)
				limsAssay = LIMSDataCache.getAssayById(rs.getString("ASSAY_ID"));

			QcEventType limsCategory = null;
			if(rs.getString("EVENT_TYPE") != null)
				limsCategory = QcEventType.getOptionByName(rs.getString("EVENT_TYPE"));

			AnalysisQcEventAnnotation annotation = new AnalysisQcEventAnnotation(
					 uniqueId,
					 text,
					 dateCreated,
					 lastModified,
					 createdBy,
					 lastModifiedBy,
					 limsInstrument,
					 limsExperiment,
					 limsSample,
					 limsAssay,
					 limsCategory);

			annotations.add(annotation);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return annotations;
	}

	public static Document getAnnotationDocument(String annotationId) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		AdvancedRTFEditorKit editor = new  AdvancedRTFEditorKit();
		AdvancedRTFDocument doc = null;
		String query =
			"SELECT FORMATTED_ANNOTATION FROM ANALYSIS_QC_LOG WHERE ANNOTATION_ID = ?";
		PreparedStatement  ps = conn.prepareStatement(query);
		ps.setString(1, annotationId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
//		   Blob blob = rs.getBlob("FORMATTED_ANNOTATION");
		   InputStream fas = rs.getBinaryStream("FORMATTED_ANNOTATION");
		   if(fas != null) {
			   BufferedInputStream is = new BufferedInputStream(fas);
			   doc = (AdvancedRTFDocument) editor.createDefaultDocument();
			   editor.read(is, doc, 0);
			   is.close();
//			   blob.free();
		   }
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return doc;
	}

}

















