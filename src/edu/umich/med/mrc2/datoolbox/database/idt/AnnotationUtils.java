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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

public class AnnotationUtils {
	
	private static final Logger logger = LogManager.getLogger(AnnotationUtils.class);

	private AnnotationUtils() {
		/* This utility class should not be instantiated */
	}

	public static void insertNewAnnotation(ObjectAnnotation annotation) {

		//	Do not insert anonymous annotation
		if(annotation.getCreateBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(annotation.getLinkedDocumentFile() == null && annotation.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		insertNewAnnotation(annotation, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void insertNewAnnotation(ObjectAnnotation annotation, Connection conn) {

		//	Do not insert anonymous annotation
		if(annotation.getCreateBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(annotation.getLinkedDocumentFile() == null && annotation.isEmpty())
			return;
		
		String annotationId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_ANNOTATION_SEQ",
				DataPrefix.OBJECT_ANNOTATION,
				"0",
				9);
		annotation.setUniqueId(annotationId);
		
		if(annotation.getLinkedDocumentFile() != null) {
			insertAnnotationWithDocumentFile(annotation, conn);
			return;
		}
		if(annotation.getRtfDocument() != null) {			
			insertRTFAnnotation(annotation, conn);
			return;
		}
		if(annotation.getChemModel() != null) {
			insertStructuralAnnotation(annotation, conn);
		}
	}
	
	private static void insertRTFAnnotation(ObjectAnnotation annotation, Connection conn) {
		
		if(annotation.getRtfDocument() == null)
			return;

		File tmpRtf =  writeTemporaryRtfFile(annotation);
		if (tmpRtf == null)
			return;

		String query =
			"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
			+ "ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, annotation.getUniqueId());
			ps.setString(2, annotation.getAnnotatedObjectType().name());
			ps.setString(3, annotation.getAnnotatedObjectId());
			ps.setString(5, annotation.getCreateBy().getId());
			ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(7, annotation.getLastModifiedBy().getId());
			ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
			try (FileInputStream fis = new FileInputStream(tmpRtf)) { // Add RTF BLOB
				ps.setBinaryStream(4, fis, (int) tmpRtf.length());
				ps.executeUpdate();
			}
		} catch (FileNotFoundException e) {
			logger.error(String.format("File %s not found", tmpRtf.getAbsolutePath()), e);
		} catch (SQLException e) {
			logger.error("Failed to insert RTF annotation", e);
		} catch (IOException e) {
			logger.error("Failed handle file input stream", e);
		}
		try {
			Files.delete(tmpRtf.toPath());
		} catch (IOException e) {
			logger.error(String.format("Failed to delete temporary RTF file  %s", tmpRtf.getAbsolutePath()), e);
		}
	}
	
	private static File writeTemporaryRtfFile(ObjectAnnotation annotation) {
		
		if(annotation.getRtfDocument() == null)
			return null;
		
		AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();
		Document rtfDocument = annotation.getRtfDocument();
		File tmpRtf = new File(MRC2ToolBoxCore.tmpDir + UUID.randomUUID().toString() + ".rtf");
		try {
			editor.write(tmpRtf.getAbsolutePath(), rtfDocument);
			return tmpRtf;
		} catch (IOException | BadLocationException ex) {
			logger.error("Failed to write RTF file", ex);
		}
		return null;		
	}
	
	private static void insertStructuralAnnotation(
			ObjectAnnotation annotation, Connection conn){

		if(annotation.getChemModel() == null)
			return;

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			
			writeStructureToStream(baos, annotation.getChemModel());
			if(baos.size() == 0) {
				logger.error("Structural Annotation is empty");					
			}
			else {
				try(ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())){					
					insertStructuralAnnotationRecord( annotation, bais, baos.size(), conn);
				}
			}		
		} catch (IOException e1) {
			logger.error("CDK failed to parse Structural Annotation", e1);
		}
	}
	
	private static void insertStructuralAnnotationRecord(
			ObjectAnnotation annotation, 
			ByteArrayInputStream bais,
			int size,
			Connection conn) {
		
		String query =
				"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
				+ "CML, CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, CML_NOTE) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			try(PreparedStatement ps = conn.prepareStatement(query)){
				ps.setString(1, annotation.getUniqueId());
				ps.setString(2, annotation.getAnnotatedObjectType().name());
				ps.setString(3, annotation.getAnnotatedObjectId());

				// Add CML BLOB
				ps.setBinaryStream(4, bais, size);
				ps.setString(5, annotation.getCreateBy().getId());
				ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));	
				ps.setString(7, annotation.getLastModifiedBy().getId());
				ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
				ps.setString(9, annotation.getChemModelNotes());
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			logger.error("Failed to insert structural annotation record", e);
		}
	}
	
	private static void writeStructureToStream(ByteArrayOutputStream baos, IChemModel chemModel) {
		
		try(CMLWriter cmlwriter = new CMLWriter(baos)){
			cmlwriter.write(chemModel);
		} catch (IOException e) {
			logger.error("Failed to create CMLWriter", e);
		} catch (CDKException e) {
			logger.error("CDK failed to parse Structural Annotation", e);
		}
	}
	
	private static void insertAnnotationWithDocumentFile(
			ObjectAnnotation annotation, Connection conn) {
		
		if(annotation.getLinkedDocumentFile() == null)
			return;
		
		// Upload new document to get linked document ID
		String linkedDocumentId = DocumentUtils.insertDocument(
							annotation.getLinkedDocumentFile(), 
							annotation.getLinkedDocumentName(), 
							annotation.getLinkedDocumentFormat(), 
							conn);
		if(linkedDocumentId == null) 
			return;
			
		String query =
			"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
			+ "CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, annotation.getUniqueId());
			ps.setString(2, annotation.getAnnotatedObjectType().name());
			ps.setString(3, annotation.getAnnotatedObjectId());
			ps.setString(4, annotation.getCreateBy().getId());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(6, annotation.getLastModifiedBy().getId());
			ps.setDate(7, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(8, linkedDocumentId);			
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to insert annotation with document file", e);
		}	
		annotation.setLinkedDocumentId(linkedDocumentId);		
	}

	public static void updateAnnotation(ObjectAnnotation annotation) {

		//	Do not allow anonymous edits to annotation
		if(annotation.getLastModifiedBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(annotation.getLinkedDocumentFile() == null && annotation.isEmpty()  )
			return;
		
		Connection conn = ConnectionManager.getConnection();
		updateAnnotation(annotation, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateAnnotation(
			ObjectAnnotation annotation, Connection conn) {

		//	Do not allow anonymous edits to annotation
		if(annotation.getLastModifiedBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(annotation.getLinkedDocumentFile() == null && annotation.isEmpty()  )
			return;
		
		//	Update RTF annotation
		if(annotation.getRtfDocument() != null) {
			updateRTFAnnotation(annotation, conn);
			return;
		}
		//	Update document title
		if(annotation.getLinkedDocumentFile() == null && annotation.getLinkedDocumentId() != null) {
			
			DocumentUtils.updateDocumentTitle(
					annotation.getLinkedDocumentName(), annotation.getLinkedDocumentId(), conn);
			return;
		}
		//	Add/replace linked document
		if(annotation.getLinkedDocumentFile() != null) {
			updateAnnotationWithDocumentFile(annotation, annotation.getLinkedDocumentFile(), conn);
			return;
		}
		//	Update CML annotation
		if(annotation.getChemModel() != null) {
			updateStructuralAnnotation(annotation, conn);
		}
	}
	
	private static void updateRTFAnnotation(
			ObjectAnnotation annotation, Connection conn) {
		
		if(annotation.getRtfDocument() == null)
			return;
		
		File tmpRtf =  writeTemporaryRtfFile(annotation);
		if (tmpRtf == null)
			return;
		
		 String query =
			"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
			+ "LAST_EDITED_ON = ?, ANNOTATION_RTF_DOCUMENT = ? " +
			"WHERE ANNOTATION_ID = ?";		 
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, annotation.getLastModifiedBy().getId());
			ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
			try(FileInputStream fis = new FileInputStream(tmpRtf)){
				ps.setBinaryStream(3, fis, (int) tmpRtf.length());
				ps.setString(4, annotation.getUniqueId());
				ps.executeUpdate();
			} 
		} catch (SQLException | IOException e) {
			logger.error("Failed to update RTF annotation", e);
		}
		Path path = Paths.get(tmpRtf.getAbsolutePath());
        try {
			Files.delete(path);
		} catch (IOException e) {
			logger.error("Failed to delete temporary RTF file", e);
		}    
	}
	
	private static void updateAnnotationWithDocumentFile(
			ObjectAnnotation annotation, 
			File linkedDocumentFile, 
			Connection conn){
		
		if(linkedDocumentFile == null)
			return;
		
		if(!linkedDocumentFile.exists())
			return;
		
		String linkedDocumentId = DocumentUtils.insertDocument(
							linkedDocumentFile, 
							annotation.getLinkedDocumentName(), 
							annotation.getLinkedDocumentFormat(), 
							conn);

		if(linkedDocumentId == null)
			return;

		String query =
			"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
			+ "LAST_EDITED_ON = ?, LINKED_DOCUMENT_ID = ? " +
			"WHERE ANNOTATION_ID= ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, annotation.getLastModifiedBy().getId());
			ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(3, linkedDocumentId);
			ps.setString(4, annotation.getUniqueId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(String.format("Failed to update annotation with ID  %s", annotation.getUniqueId()), e);
		}
	}
	
	private static void updateStructuralAnnotation(
			ObjectAnnotation annotation, Connection conn){

		if(annotation.getChemModel() == null)
			return;

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			
			writeStructureToStream( baos, annotation.getChemModel());
			if(baos.size() == 0) {
				logger.error("Structural Annotation is empty");					
			}
			else {
				try(ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())){
					 updateStructuralAnnotationRecord(annotation, bais, baos.size(), conn);
				}
			}		
		} catch (IOException e) {
			logger.error(String.format("Failed to update annotation with ID  %s", annotation.getUniqueId()), e);
		}
	}
	
	private static void updateStructuralAnnotationRecord(
			ObjectAnnotation annotation, 
			ByteArrayInputStream bais,
			int size,
			Connection conn) {
		
		String query =
				"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
				+ "LAST_EDITED_ON = ?, CML = ?, CML_NOTE = ? " +
				"WHERE ANNOTATION_ID= ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){			
			ps.setString(1, annotation.getLastModifiedBy().getId());
			ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
			// Add CML BLOB
			ps.setBinaryStream(3, bais, size);
			ps.setString(4, annotation.getChemModelNotes());
			ps.setString(5, annotation.getUniqueId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(String.format("Failed to update the record for annotation with ID  %s", annotation.getUniqueId()), e);
		}
	}

	public static void deleteAnnotation(ObjectAnnotation annotation) {

		Connection conn = ConnectionManager.getConnection();
		deleteAnnotation(annotation, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteAnnotation(ObjectAnnotation annotation, Connection conn) {

		String query =
			"DELETE FROM OBJECT_ANNOTATIONS WHERE ANNOTATION_ID = ?";
		try(PreparedStatement  stmt = conn.prepareStatement(query)){
			stmt.setString(1, annotation.getUniqueId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error(String.format("Failed to delete annotation with ID  %s", annotation.getUniqueId()), e);
		}
	}

	public static Collection<ObjectAnnotation>getObjetAnnotations(
			AnnotatedObjectType objectType, String objectId) {

		Connection conn = ConnectionManager.getConnection();
		Collection<ObjectAnnotation>annotations = 
				getObjectAnnotations(objectType, objectId, conn);
		ConnectionManager.releaseConnection(conn);
		return annotations;
	}

	public static Collection<ObjectAnnotation>getObjectAnnotations(
			AnnotatedObjectType objectType, String objectId, Connection conn) {

		Collection<ObjectAnnotation>annotations = new ArrayList<ObjectAnnotation>();
		String query =
			"SELECT ANNOTATION_ID, ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, "
			+ "LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID, CML, CML_NOTE " +
			"FROM OBJECT_ANNOTATIONS WHERE OBJECT_TYPE = ? AND OBJECT_ID = ? ";		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, objectType.name());
			ps.setString(2, objectId);
			try(ResultSet rs = ps.executeQuery()){
				while (rs.next()) {
		
					ObjectAnnotation annotation = new ObjectAnnotation(
							rs.getString("ANNOTATION_ID"), 
							objectType, 
							objectId,
							rs.getDate("CREATED_ON"), 
							rs.getDate("LAST_EDITED_ON"),
							IDTDataCache.getUserById(rs.getString("CREATED_BY")),
							IDTDataCache.getUserById(rs.getString("LAST_EDITED_BY")),
							null);
		
					InputStream dbs = rs.getBinaryStream("ANNOTATION_RTF_DOCUMENT");
					if (dbs != null) {
						AdvancedRTFDocument doc = streamToDoc(dbs);
						if(doc != null)
							annotation.setRtfDocument(doc);				
					}
					InputStream cmls = rs.getBinaryStream("CML");
					if (cmls != null) {			
						try(BufferedInputStream is = new BufferedInputStream(cmls)){
							IChemModel chemModel = getChemModelFromStream(is);	
							if(chemModel != null) {
								annotation.setChemModel(chemModel );				
								annotation.setChemModelNotes(rs.getString("CML_NOTE"));
							}
						}
					}
					annotation.setLinkedDocumentId(rs.getString("LINKED_DOCUMENT_ID"));
					attachLinkedDocumentMetaData(annotation, conn);
					annotations.add(annotation);
				}
			}
		} catch (SQLException | IOException e) {
			logger.error(String.format(
					"Failed to get annotation for object of type %s with ID %s", objectType.getName(), objectId), e);
		}
		return annotations;
	}
	
	public static void updateObjectAnnotationsList(AnnotatedObject annotatedObject) {
		Connection conn = ConnectionManager.getConnection();
		updateObjectAnnotationsList(annotatedObject, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateObjectAnnotationsList(
			AnnotatedObject annotatedObject, Connection conn) {
		
		TreeSet<String>annotationIds = new TreeSet<String>();
		String query =
				"SELECT ANNOTATION_ID FROM OBJECT_ANNOTATIONS "
				+ "WHERE OBJECT_TYPE = ? AND OBJECT_ID = ? ";			
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, annotatedObject.getAnnotatedObjectType().name());
			ps.setString(2, annotatedObject.getId());		
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				annotationIds.add(rs.getString("ANNOTATION_ID"));
		} catch (SQLException e) {
			logger.error(String.format(
					"Failed to fetch annotations list for object of type %s with ID %s", 
					annotatedObject.getAnnotatedObjectType().getName(), annotatedObject.getId()), e);
		}
		TreeSet<String>objectAnnotationIds = new TreeSet<String>();
		for(ObjectAnnotation annotation : annotatedObject.getAnnotations()) {
			
			if(annotation.getUniqueId().length() == 12) {
				objectAnnotationIds.add(annotation.getUniqueId());
			}
			else {
				insertNewAnnotation(annotation, conn);
				objectAnnotationIds.add(annotation.getUniqueId());
			}
		}		
		query = "DELETE FROM OBJECT_ANNOTATIONS WHERE ANNOTATION_ID = ?";			
		try(PreparedStatement ps = conn.prepareStatement(query)){
			for(String id : annotationIds) {
				
				if(!objectAnnotationIds.contains(id)) {
					ps.setString(1, id);
					ps.executeUpdate();
				}
			}
		} catch (SQLException e) {
			logger.error(String.format(
					"Failed to remove deleted annotations for object of type %s with ID %s", 
					annotatedObject.getAnnotatedObjectType().getName(), annotatedObject.getId()), e);
		}
	}	

	public static void attachLinkedDocumentMetaData(ObjectAnnotation annotation) {
		
		if(annotation.getLinkedDocumentId() == null)
			return;
		
		Connection conn = ConnectionManager.getConnection();
		attachLinkedDocumentMetaData(annotation, conn);
		ConnectionManager.releaseConnection(conn);		
	}
		
	private static void attachLinkedDocumentMetaData(
			ObjectAnnotation annotation, Connection conn) {

		if(annotation.getLinkedDocumentId() == null)
			return;
	
		String query = 
				"SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		try(PreparedStatement  ps = conn.prepareStatement(query)){
			ps.setString(1, annotation.getLinkedDocumentId());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
		
					annotation.setLinkedDocumentFormat(
							DocumentFormat.getFormatByFileExtension(rs.getString("DOCUMENT_FORMAT")));
					annotation.setLinkedDocumentName(rs.getString("DOCUMENT_NAME"));
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to attach linked document metadata", e);
		}
	}

	public static Document getAnnotationDocument(String annotationId){

		Connection conn = ConnectionManager.getConnection();
		Document doc = getAnnotationDocument(annotationId, conn);
		ConnectionManager.releaseConnection(conn);
		return doc;
	}
	
	public static Document getAnnotationDocument(String annotationId, Connection conn) {

		AdvancedRTFDocument doc = null;
		String query = "SELECT ANN_DOCUMENT FROM OBJECT_ANNOTATIONS WHERE ANNOTATION_ID = ?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, annotationId);
			try(ResultSet rs = ps.executeQuery()){
				while (rs.next()) {
					try (InputStream ads = rs.getBinaryStream("ANN_DOCUMENT")) {				
						if (ads != null) 						
							doc =streamToDoc(ads);
					} catch (IOException e1) {
						logger.error("Failed to read the document from database", e1);
					}
				}
			}
		} catch (SQLException e) {
			logger.error(String.format("Failed to get RTF document for annotation ID  %s", annotationId), e);
		}
		return doc;
	}
	
	private static AdvancedRTFDocument streamToDoc(InputStream ads) {
		
		AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();
		AdvancedRTFDocument doc = null;
		try (BufferedInputStream is = new BufferedInputStream(ads)) {
			doc = (AdvancedRTFDocument) editor.createDefaultDocument();
			editor.read(is, doc, 0);
		} catch (IOException | BadLocationException e) {
			logger.error("Failed to read the document from database", e);
		}
		return doc;
	}
		
	public static IChemModel getChemModelFromStream(InputStream is) {

		ChemModel chemModel = null;
		IChemFile chemFile = null;

		try(ISimpleChemObjectReader cor = new CMLReader(is)){
			
			if (cor.accepts(IChemFile.class)) {
				chemFile = (IChemFile) cor.read((IChemObject) new ChemFile());
		        if (chemFile != null)
		            chemModel = (ChemModel) chemFile.getChemSequence(0).getChemModel(0);
			}
	        if (cor.accepts(ChemModel.class) && chemModel==null) 
	               chemModel = (ChemModel) cor.read((IChemObject) new ChemModel());
	        
		} catch (IOException | CDKException e) {
			logger.error("Failed to create ChemModel from stream", e);
		}
		return chemModel;
	}	
}












