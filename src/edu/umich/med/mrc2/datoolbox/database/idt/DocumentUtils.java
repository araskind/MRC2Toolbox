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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class DocumentUtils {

	public static String insertDocument(
			File documentFile, 
			String documentTitle, 
			DocumentFormat format) throws Exception {
		
		if(!documentFile.exists())
			return null;
		
		Connection conn = ConnectionManager.getConnection();
		String documentId = insertDocument(documentFile, documentTitle, format, conn);
		ConnectionManager.releaseConnection(conn);
		return documentId;
	}
	
	public static String insertDocument(
			File documentFile, 
			String documentTitle, 
			DocumentFormat format, 
			Connection conn) throws Exception {
		
		if(!documentFile.exists())
			return null;
		
		String documentId = null;	
		
		//	Check if document already in database using MD5
		String md5hash = FIOUtils.calculateFileChecksum(documentFile);
		
		String query  = "SELECT DOCUMENT_ID FROM DOCUMENTS WHERE DOCUMENT_HASH = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, md5hash);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			documentId = rs.getString("DOCUMENT_ID");
			break;
		}
		rs.close();
		ps.close();
		if(documentId != null)
			return documentId;
			
		//	Get new document ID	
//		query  =
//			"SELECT '" + DataPrefix.DOCUMENT.getName() +
//			"' || LPAD(DOCUMENTS_SEQ.NEXTVAL, 12, '0') AS DOCUMENT_ID FROM DUAL";
//		
//		ps = conn.prepareStatement(query);
//		rs = ps.executeQuery();
//		while (rs.next()) {
//			documentId = rs.getString("DOCUMENT_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
//		
		documentId = SQLUtils.getNextIdFromSequence(conn, 
				"DOCUMENTS_SEQ",
				DataPrefix.DOCUMENT,
				"0",
				12);
				
		//	Insert document record
		query = 
			"INSERT INTO DOCUMENTS (DOCUMENT_ID, DOCUMENT_NAME, DOCUMENT_FORMAT, "
			+ "DOCUMENT_CONTENTS, DOCUMENT_HASH) VALUES (?, ?, ?, ?, ?)";
		ps = conn.prepareStatement(query);
		ps.setString(1, documentId);
		ps.setString(2, documentTitle);
		ps.setString(3, format.name());
		
		//	Add document
		InputStream fis = Files.newInputStream(Paths.get(documentFile.getAbsolutePath()));
		ps.setBinaryStream(4, fis, (int) documentFile.length());
		ps.setString(5, md5hash);		
		ps.executeUpdate();
		ps.close();
		
//		dis.close();
		fis.close();			
		return documentId;
	}
	
	public static String getDocumentIdByFileHash(File documentFile) throws Exception {	
		
		String documentId = null;
		Connection conn = ConnectionManager.getConnection();
		documentId = getDocumentIdByFileHash(documentFile, conn);
		ConnectionManager.releaseConnection(conn);
		return documentId;
	}			
	
	public static String getDocumentIdByFileHash(File documentFile, Connection conn) throws Exception {	
		
		String documentId = null;
		String md5hash = FIOUtils.calculateFileChecksum(documentFile);		
		String query  = "SELECT DOCUMENT_ID FROM DOCUMENTS WHERE DOCUMENT_HASH = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, md5hash);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			documentId = rs.getString("DOCUMENT_ID");
			break;
		}
		rs.close();
		ps.close();
		return documentId;
	}
		
	public static String getDocumentFileNameIdById(String documentId) throws Exception {	
		
		String documentFileName = null;
		Connection conn = ConnectionManager.getConnection();
		documentFileName = getDocumentFileNameIdById(documentId, conn);
		ConnectionManager.releaseConnection(conn);
		return documentFileName;
	}			
	
	public static String getDocumentFileNameIdById(String documentId, Connection conn) throws Exception {	
		
		String documentFileName = null;
		String query  = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, documentId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			documentFileName = rs.getString("DOCUMENT_NAME") + "." + rs.getString("DOCUMENT_FORMAT");
			break;
		}
		rs.close();
		ps.close();
		return documentFileName;
	}
	
	public static void updateDocumentTitle(
			String documentTitle, 
			String documentId) throws Exception {		

		Connection conn = ConnectionManager.getConnection();
		updateDocumentTitle(documentTitle, documentId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateDocumentTitle(
			String documentTitle, 
			String documentId, 
			Connection conn) throws Exception {
		
		String query = "UPDATE DOCUMENTS SET DOCUMENT_NAME = ? WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, documentTitle);
		ps.setString(2, documentId);
		ps.executeUpdate();
		ps.close();
	}
	
	public static void deleteDocument(String documentId) throws Exception {		

		Connection conn = ConnectionManager.getConnection();
		deleteDocument(documentId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteDocument(
			String documentId, 
			Connection conn) throws Exception {
		
		String query = "DELETE FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, documentId);
		ps.executeUpdate();
		ps.close();
	}

	public static void saveDocumentToFile(String linkedDocumentId, File destinationFolder) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT, DOCUMENT_CONTENTS FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, linkedDocumentId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {

		   File documentFile = Paths.get(destinationFolder.getAbsolutePath(),
				   rs.getString("DOCUMENT_NAME") + "." +
				   rs.getString("DOCUMENT_FORMAT")).toFile();

//		   Blob blob = rs.getBlob("DOCUMENT_CONTENTS");		  		   
		   BufferedInputStream is = new BufferedInputStream(rs.getBinaryStream("DOCUMENT_CONTENTS"));
		   FileOutputStream fos = new FileOutputStream(documentFile);
		   byte[] buffer = new byte[2048];
		   int r = 0;
		   while((r = is.read(buffer))!=-1)
		      fos.write(buffer, 0, r);

		   fos.flush();
		   fos.close();
		   is.close();
//		   blob.free();
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
}











