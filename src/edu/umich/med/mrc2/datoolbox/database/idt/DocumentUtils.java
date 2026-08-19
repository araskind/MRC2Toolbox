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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class DocumentUtils {
	
	private static final Logger logger = LogManager.getLogger(DocumentUtils.class);

	private DocumentUtils() {
		/* This utility class should not be instantiated */
	}

	public static String insertDocument(
			File documentFile, 
			String documentTitle, 
			DocumentFormat format) throws SQLException {
		
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
			Connection conn) throws SQLException {
		
		if(!documentFile.exists())
			return null;
		
		String documentId = null;	
		
		//	Check if document already in database using MD5
		String md5hash = FIOUtils.calculateFileChecksum(documentFile);
		
		String query  = "SELECT DOCUMENT_ID FROM DOCUMENTS WHERE DOCUMENT_HASH = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, md5hash);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				documentId = rs.getString("DOCUMENT_ID");

			rs.close();
		}
		if(documentId != null)
			return documentId;
	
		//	Insert new document record
		documentId = SQLUtils.getNextIdFromSequence(conn, 
				"DOCUMENTS_SEQ",
				DataPrefix.DOCUMENT,
				"0",
				12);
		query = 
			"INSERT INTO DOCUMENTS (DOCUMENT_ID, DOCUMENT_NAME, DOCUMENT_FORMAT, "
			+ "DOCUMENT_CONTENTS, DOCUMENT_HASH) VALUES (?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, documentId);
			ps.setString(2, documentTitle);
			ps.setString(3, format.name());
			try(InputStream fis = Files.newInputStream(Paths.get(documentFile.getAbsolutePath()))){
				ps.setBinaryStream(4, fis, (int) documentFile.length());
				ps.setString(5, md5hash);		
				ps.executeUpdate();
			} catch (IOException e) {
				logger.error(String.format("%s %s", "Failed to upload file", documentFile.getAbsolutePath()), e);
			}
		}			
		return documentId;
	}
	
	public static String getDocumentIdByFileHash(File documentFile) throws SQLException {	
		
		String documentId = null;
		Connection conn = ConnectionManager.getConnection();
		documentId = getDocumentIdByFileHash(documentFile, conn);
		ConnectionManager.releaseConnection(conn);
		return documentId;
	}			
	
	public static String getDocumentIdByFileHash(File documentFile, Connection conn) throws SQLException {	
		
		String documentId = null;
		String md5hash = FIOUtils.calculateFileChecksum(documentFile);		
		String query  = "SELECT DOCUMENT_ID FROM DOCUMENTS WHERE DOCUMENT_HASH = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, md5hash);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				documentId = rs.getString("DOCUMENT_ID");
	
			rs.close();
		}
		return documentId;
	}
		
	public static String getDocumentFileNameIdById(String documentId) throws SQLException {	
		
		String documentFileName = null;
		Connection conn = ConnectionManager.getConnection();
		documentFileName = getDocumentFileNameIdById(documentId, conn);
		ConnectionManager.releaseConnection(conn);
		return documentFileName;
	}			
	
	public static String getDocumentFileNameIdById(String documentId, Connection conn) throws SQLException {	
		
		String documentFileName = null;
		String query  = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, documentId);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
				documentFileName = rs.getString("DOCUMENT_NAME") + "." + rs.getString("DOCUMENT_FORMAT");

			rs.close();
		}
		return documentFileName;
	}
	
	public static void updateDocumentTitle(
			String documentTitle, 
			String documentId) throws SQLException {		

		Connection conn = ConnectionManager.getConnection();
		updateDocumentTitle(documentTitle, documentId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateDocumentTitle(
			String documentTitle, 
			String documentId, 
			Connection conn) throws SQLException {
		
		String query = "UPDATE DOCUMENTS SET DOCUMENT_NAME = ? WHERE DOCUMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, documentTitle);
			ps.setString(2, documentId);
			ps.executeUpdate();
		}
	}
	
	public static void deleteDocument(String documentId) throws SQLException {		

		Connection conn = ConnectionManager.getConnection();
		deleteDocument(documentId, conn);
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteDocument(
			String documentId, 
			Connection conn) throws SQLException {
		
		String query = "DELETE FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, documentId);
			ps.executeUpdate();
		}
	}

	public static void saveDocumentToFile(String linkedDocumentId, File destinationFolder) throws SQLException {

		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT DOCUMENT_NAME, DOCUMENT_FORMAT, "
				+ "DOCUMENT_CONTENTS FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, linkedDocumentId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
	
			   File documentFile = Paths.get(destinationFolder.getAbsolutePath(),
					   rs.getString("DOCUMENT_NAME") + "." +
					   rs.getString("DOCUMENT_FORMAT")).toFile();
			   DatabaseUtils.writeBlobToFile(documentFile, rs.getBinaryStream("DOCUMENT_CONTENTS"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
	}
}











