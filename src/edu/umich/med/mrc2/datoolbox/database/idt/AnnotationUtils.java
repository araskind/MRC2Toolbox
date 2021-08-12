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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.swing.text.Document;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;

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

	public static void insertNewAnnotation(ObjectAnnotation annotation, File  linkedDocumentFile) throws Exception {

		//	Do not insert anonymous annotation
		if(annotation.getCreateBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(linkedDocumentFile == null && annotation.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		insertNewAnnotation(annotation, linkedDocumentFile, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void insertNewAnnotation(
			ObjectAnnotation annotation, 
			File linkedDocumentFile, 
			Connection conn) throws Exception {

		//	Do not insert anonymous annotation
		if(annotation.getCreateBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(linkedDocumentFile == null && annotation.isEmpty())
			return;
		
		String annotationId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_ANNOTATION_SEQ",
				DataPrefix.OBJECT_ANNOTATION,
				"0",
				9);
		annotation.setUniqueId(annotationId);
		
		//	setNewAnnotationIdFromDatabaseSequence(annotation, conn);		
		if(linkedDocumentFile != null) {
			insertAnnotationWithDocumentFile(annotation, linkedDocumentFile, conn);
			return;
		}
		if(annotation.getRtfDocument() != null) {			
			insertRTFAnnotation(annotation, conn);
			return;
		}
		if(annotation.getChemModel() != null) {
			insertStructuralAnnotation(annotation, conn);
			return;
		}
	}
	
//	private static void setNewAnnotationIdFromDatabaseSequence(
//			ObjectAnnotation annotation, 
//			Connection conn) throws Exception{
//		
//		String annotationId = null;
//		String query = "SELECT '" + DataPrefix.OBJECT_ANNOTATION.getName()
//				+ "' || LPAD(ID_ANNOTATION_SEQ.NEXTVAL, 9, '0') AS ANNOTATION_ID FROM DUAL";
//
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while (rs.next()) {
//			annotationId = rs.getString("ANNOTATION_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		annotation.setUniqueId(annotationId);
//	}
	
	private static void insertRTFAnnotation(
			ObjectAnnotation annotation, 
			Connection conn) throws Exception{
		
		if(annotation.getRtfDocument() == null)
			return;

		File tmpRtf =  writeTemporaryRtfFile(annotation);
		if (tmpRtf == null)
			throw new Exception("Could not write temporary RTF file!");

		String query =
			"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
			+ "ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		 
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getUniqueId());
		ps.setString(2, annotation.getAnnotatedObjectType().name());
		ps.setString(3, annotation.getAnnotatedObjectId());

		// Add RTF BLOB
		FileInputStream fis = new FileInputStream(tmpRtf);
		ps.setBinaryStream(4, fis, (int) tmpRtf.length());
		
		ps.setString(5, annotation.getCreateBy().getId());
		ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));		
		ps.setString(7, annotation.getLastModifiedBy().getId());
		ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
		
		ps.executeUpdate();
		ps.close();

		fis.close();
		Path path = Paths.get(tmpRtf.getAbsolutePath());
		Files.delete(path);
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;		
	}
	
	private static void insertStructuralAnnotation(
			ObjectAnnotation annotation, 
			Connection conn) throws Exception{

		if(annotation.getChemModel() == null)
			return;
		
		// Create CML
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CMLWriter cmlwriter = new CMLWriter(baos);
		try {
			cmlwriter.write(annotation.getChemModel());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if(baos.size() == 0) {
			baos.close();
			cmlwriter.close();
			throw new Exception("Can not create CML markup from structure!");
		}		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());	
		String query =
				"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
				+ "CML, CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, CML_NOTE) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			 
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getUniqueId());
		ps.setString(2, annotation.getAnnotatedObjectType().name());
		ps.setString(3, annotation.getAnnotatedObjectId());

		// Add CML BLOB
		ps.setBinaryStream(4, bais, baos.size());
		ps.setString(5, annotation.getCreateBy().getId());
		ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));	
		ps.setString(7, annotation.getLastModifiedBy().getId());
		ps.setDate(8, new java.sql.Date(new java.util.Date().getTime()));
		ps.setString(9, annotation.getChemModelNotes());
		ps.executeUpdate();
		ps.close();

		baos.close();
		bais.close();
		cmlwriter.close();
	}
	
	private static void insertAnnotationWithDocumentFile(
			ObjectAnnotation annotation, 
			File linkedDocumentFile, 
			Connection conn) throws Exception{
		
		// Upload new document to get linked document ID
		String linkedDocumentId =  null;			
		try {
			linkedDocumentId = DocumentUtils.insertDocument(
							linkedDocumentFile, 
							annotation.getLinkedDocumentName(), 
							annotation.getLinkedDocumentFormat(), 
							conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(linkedDocumentId != null) {
			
			String query =
				"INSERT INTO OBJECT_ANNOTATIONS (ANNOTATION_ID, OBJECT_TYPE, OBJECT_ID, "
				+ "CREATED_BY, CREATED_ON, LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, annotation.getUniqueId());
			ps.setString(2, annotation.getAnnotatedObjectType().name());
			ps.setString(3, annotation.getAnnotatedObjectId());
			ps.setString(4, annotation.getCreateBy().getId());
			ps.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(6, annotation.getLastModifiedBy().getId());
			ps.setDate(7, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(8, linkedDocumentId);			
			ps.executeUpdate();
			ps.close();		
			annotation.setLinkedDocumentId(linkedDocumentId);
		}
	}

	public static void updateAnnotation(ObjectAnnotation annotation, File linkedDocumentFile) throws Exception{

		//	Do not allow anonymous edits to annotation
		if(annotation.getLastModifiedBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(linkedDocumentFile == null && annotation.isEmpty()  )
			return;
		
		Connection conn = ConnectionManager.getConnection();
		updateAnnotation(annotation, linkedDocumentFile, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateAnnotation(ObjectAnnotation annotation, File linkedDocumentFile, Connection conn) throws Exception{

		//	Do not allow anonymous edits to annotation
		if(annotation.getLastModifiedBy() == null)
			return;
		
		//	Do not insert empty annotation
		if(linkedDocumentFile == null && annotation.isEmpty()  )
			return;
		
		//	Update RTF annotation
		if(annotation.getRtfDocument() != null) {
			updateRTFAnnotation(annotation, conn);
			return;
		}
		//	Update document title
		if(linkedDocumentFile == null && annotation.getLinkedDocumentId() != null) {
			
			DocumentUtils.updateDocumentTitle(
					annotation.getLinkedDocumentName(), annotation.getLinkedDocumentId(), conn);
			return;
		}
		//	Add/replace linked document
		if(linkedDocumentFile != null) {
			updateAnnotationWithDocumentFile(annotation, linkedDocumentFile, conn);
			return;
		}
		//	Update CML annotation
		if(annotation.getChemModel() != null) {
			updateStructuralAnnotation(annotation, conn);
			return;
		}
	}
	
	private static void updateRTFAnnotation(ObjectAnnotation annotation, Connection conn) throws Exception{
		
		if(annotation.getRtfDocument() == null)
			return;
		
		File tmpRtf =  writeTemporaryRtfFile(annotation);
		if (tmpRtf == null)
			throw new Exception("Could not write temporary RTF file!");
		
		 String query =
			"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
			+ "LAST_EDITED_ON = ?, ANNOTATION_RTF_DOCUMENT = ? " +
			"WHERE ANNOTATION_ID = ?";
		 
		PreparedStatement ps = conn.prepareStatement(query);			
		ps.setString(1, annotation.getLastModifiedBy().getId());
		ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));

		//	Add RTF BLOB
		FileInputStream fis = new FileInputStream(tmpRtf);
		ps.setBinaryStream(3, fis, (int) tmpRtf.length());
		ps.setString(4, annotation.getUniqueId());
		ps.executeUpdate();
		ps.close();

		fis.close();
		Path path = Paths.get(tmpRtf.getAbsolutePath());
        Files.delete(path);    
	}
	
	private static void updateAnnotationWithDocumentFile(
			ObjectAnnotation annotation, 
			File linkedDocumentFile, 
			Connection conn) throws Exception{
		
		if(linkedDocumentFile == null)
			return;
		
		if(!linkedDocumentFile.exists())
			return;
		
		String linkedDocumentId =  null;			
		try {
			linkedDocumentId = DocumentUtils.insertDocument(
							linkedDocumentFile, 
							annotation.getLinkedDocumentName(), 
							annotation.getLinkedDocumentFormat(), 
							conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(linkedDocumentId != null) {

			String query =
				"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
				+ "LAST_EDITED_ON = ?, LINKED_DOCUMENT_ID = ? " +
				"WHERE ANNOTATION_ID= ?";
			
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, annotation.getLastModifiedBy().getId());
			ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(3, linkedDocumentId);
			ps.setString(4, annotation.getUniqueId());
			ps.executeUpdate();
			ps.close();					
		}
	}
	
	private static void updateStructuralAnnotation(
			ObjectAnnotation annotation, 
			Connection conn) throws Exception{

		if(annotation.getChemModel() == null)
			return;
		
		// Create CML
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CMLWriter cmlwriter = new CMLWriter(baos);
		try {
			cmlwriter.write(annotation.getChemModel());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if(baos.size() == 0) {
			baos.close();
			cmlwriter.close();
			throw new Exception("Can not create CML markup from structure!");
		}		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());	
		String query =
				"UPDATE OBJECT_ANNOTATIONS SET LAST_EDITED_BY = ?, "
				+ "LAST_EDITED_ON = ?, CML = ?, CML_NOTE = ? " +
				"WHERE ANNOTATION_ID= ?";
			 
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getLastModifiedBy().getId());
		ps.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
		// Add CML BLOB
		ps.setBinaryStream(3, bais, baos.size());
		ps.setString(4, annotation.getChemModelNotes());
		ps.setString(5, annotation.getUniqueId());
		ps.executeUpdate();
		ps.close();

		baos.close();
		bais.close();
		cmlwriter.close();
	}

	public static void deleteAnnotation(ObjectAnnotation annotation) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		deleteAnnotation(annotation, conn);
		ConnectionManager.releaseConnection(conn);
	}

	public static void deleteAnnotation(ObjectAnnotation annotation, Connection conn) throws Exception {

		String query =
			"DELETE FROM OBJECT_ANNOTATIONS WHERE ANNOTATION_ID = ?";

		PreparedStatement  stmt = conn.prepareStatement(query);
		stmt.setString(1, annotation.getUniqueId());
		stmt.executeUpdate();
		stmt.close();
	}

	public static Collection<ObjectAnnotation>getObjetAnnotations(AnnotatedObjectType objectType, String objectId) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		Collection<ObjectAnnotation>annotations = getObjectAnnotations(objectType, objectId, conn);
		ConnectionManager.releaseConnection(conn);
		return annotations;
	}

	public static Collection<ObjectAnnotation>getObjectAnnotations(
			AnnotatedObjectType objectType, String objectId, Connection conn) throws Exception{

		Collection<ObjectAnnotation>annotations = new ArrayList<ObjectAnnotation>();
		AdvancedRTFEditorKit editor = new  AdvancedRTFEditorKit();
		String query =
			"SELECT ANNOTATION_ID, ANNOTATION_RTF_DOCUMENT, CREATED_BY, CREATED_ON, "
			+ "LAST_EDITED_BY, LAST_EDITED_ON, LINKED_DOCUMENT_ID, CML, CML_NOTE " +
			"FROM OBJECT_ANNOTATIONS WHERE OBJECT_TYPE = ? AND OBJECT_ID = ? ";
		
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, objectType.name());
		ps.setString(2, objectId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			ObjectAnnotation annotation = new ObjectAnnotation(
					rs.getString("ANNOTATION_ID"), 
					objectType, 
					objectId,
					rs.getDate("CREATED_ON"), 
					rs.getDate("LAST_EDITED_ON"),
					IDTDataCash.getUserById(rs.getString("CREATED_BY")),
					IDTDataCash.getUserById(rs.getString("LAST_EDITED_BY")),
					null);

//			Blob blob = rs.getBlob("ANNOTATION_RTF_DOCUMENT");
			InputStream dbs = rs.getBinaryStream("ANNOTATION_RTF_DOCUMENT");
			if (dbs != null) {				
//				BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream());				
				BufferedInputStream is = new BufferedInputStream(dbs);
				AdvancedRTFDocument doc = (AdvancedRTFDocument) editor.createDefaultDocument();
				editor.read(is, doc, 0);
				is.close();
//				blob.free();
				annotation.setRtfDocument(doc);
			}
//			Blob cml = rs.getBlob("CML");
			InputStream cmls = rs.getBinaryStream("CML");
			if (cmls != null) {
				
				BufferedInputStream is = new BufferedInputStream(cmls);
				IChemModel chemModel = getChemModelFromStream(is);
				is.close();
//				cml.free();				
				annotation.setChemModel(chemModel );				
				annotation.setChemModelNotes(rs.getString("CML_NOTE"));
			}
			annotation.setLinkedDocumentId(rs.getString("LINKED_DOCUMENT_ID"));
			attachLinkedDocumentMetaData(annotation, conn);
			annotations.add(annotation);
		}
		rs.close();
		ps.close();
		return annotations;
	}
	
	public static IChemModel getChemModelFromStream(InputStream is) throws CDKException, IOException {

		ISimpleChemObjectReader cor = new CMLReader(is);
    	String error = null;
        ChemModel chemModel = null;
        IChemFile chemFile = null;
        if (cor.accepts(IChemFile.class) && chemModel==null) {
            // try to read a ChemFile
            try {
                chemFile = (IChemFile) cor.read((IChemObject) new ChemFile());
                if (chemFile == null) {
                    error = "The object chemFile was empty unexpectedly!";
                }
            } catch (Exception exception) {
                error = "Error while reading file: " + exception.getMessage();
                exception.printStackTrace();
            }
        }
        if (error != null)
            throw new CDKException(error);
        
        if (chemModel == null && chemFile != null)
            chemModel = (ChemModel) chemFile.getChemSequence(0).getChemModel(0);
        
        if (cor.accepts(ChemModel.class) && chemModel==null) {
            try {
                chemModel = (ChemModel) cor.read((IChemObject) new ChemModel());
                if (chemModel == null) {
                    error = "The object chemModel was empty unexpectedly!";
                }
            } catch (Exception exception) {
                error = "Error while reading file: " + exception.getMessage();
                exception.printStackTrace();
            }
        }
        cor.close();
		return chemModel;
	}
	
	public static void attachLinkedDocumentMetaData(ObjectAnnotation annotation) throws Exception{
		
		if(annotation.getLinkedDocumentId() == null)
			return;
		
		Connection conn = ConnectionManager.getConnection();
		attachLinkedDocumentMetaData(annotation, conn);
		ConnectionManager.releaseConnection(conn);		
	}
		
	private static void attachLinkedDocumentMetaData(ObjectAnnotation annotation, Connection conn) throws Exception{

		if(annotation.getLinkedDocumentId() == null)
			return;
		
		String query = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement  ps = conn.prepareStatement(query);
		ps.setString(1, annotation.getLinkedDocumentId());
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

			annotation.setLinkedDocumentFormat(DocumentFormat.getFormatByFileExtension(rs.getString("DOCUMENT_FORMAT")));
			annotation.setLinkedDocumentName(rs.getString("DOCUMENT_NAME"));
		}
		rs.close();
		ps.close();
	}

	public static Document getAnnotationDocument(String annotationId) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		Document doc = getAnnotationDocument(annotationId, conn);
		ConnectionManager.releaseConnection(conn);
		return doc;
	}
	
	public static Document getAnnotationDocument(String annotationId, Connection conn) throws Exception {

		AdvancedRTFEditorKit editor = new  AdvancedRTFEditorKit();
		AdvancedRTFDocument doc = null;
		String query =
			"SELECT ANN_DOCUMENT FROM OBJECT_ANNOTATIONS WHERE ANNOTATION_ID = ?";
		PreparedStatement  ps = conn.prepareStatement(query);
		ps.setString(1, annotationId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
//		   Blob blob = rs.getBlob("ANN_DOCUMENT");		   
		   InputStream ads = rs.getBinaryStream("ANN_DOCUMENT");
		   if(ads != null) {
			   BufferedInputStream is = new BufferedInputStream(ads);
			   doc = (AdvancedRTFDocument) editor.createDefaultDocument();
			   editor.read(is, doc, 0);
			   is.close();
//			   blob.free();
		   }
		}
		rs.close();
		ps.close();
		return doc;
	}
}












