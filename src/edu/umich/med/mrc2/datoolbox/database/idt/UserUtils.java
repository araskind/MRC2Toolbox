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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collection;
import java.util.TreeSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class UserUtils {
	
	private static final Logger logger = LogManager.getLogger(UserUtils.class);	

	private UserUtils() {
		/* This utility class should not be instantiated */
	}
	
	private static final String PWD_ENCRYPTION_KEY = 
			MRC2ToolBoxConfiguration.getEncryptionkey();
	private static final String DEFAULT_CIFER_ALGORITHM = "AES";
	
	public static LIMSUser getUserLogon(String userName, String password) throws SQLException{

		LIMSUser user = null;
		String encryptedPassword = null;
		try {
			encryptedPassword = encryptString(password);
		} catch (InvalidArgumentException e) {
			logger.error("Failed to encrypt password", e);
		}
		if(encryptedPassword == null)
			return null;
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, LAB, EMAIL, " +
				"PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID FROM RESEARCHER WHERE USERNAME = ? " +
				"AND PASSWORD = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, userName);
			ps.setString(2, encryptedPassword);
			ResultSet rs = ps.executeQuery();	
			while (rs.next()) {	
				user = new LIMSUser(
						rs.getString("RESEARCHER_ID"),
						rs.getString("LAST_NAME"),
						rs.getString("FIRST_NAME"),
						rs.getString("LAB"),
						rs.getString("EMAIL"),
						rs.getString("PHONE"),
						rs.getString("USERNAME"),
						rs.getBoolean("SUPER_USER"),
						rs.getString("AFFILIATION"));
				user.setOrganizationId(rs.getString("ORGANIZATION_ID"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return user;
	}
		
	public static LIMSUser getUserByUserId(String userName) throws SQLException{

		LIMSUser user = null;
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, LAB, EMAIL, " +
				"PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID FROM RESEARCHER WHERE USERNAME = ?";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, userName.trim());
			ResultSet rs = ps.executeQuery();
	
			while (rs.next()) {
	
				user = new LIMSUser(
						rs.getString("RESEARCHER_ID"),
						rs.getString("LAST_NAME"),
						rs.getString("FIRST_NAME"),
						rs.getString("LAB"),
						rs.getString("EMAIL"),
						rs.getString("PHONE"),
						rs.getString("USERNAME"),
						rs.getBoolean("SUPER_USER"),
						rs.getString("AFFILIATION"));
				user.setOrganizationId(rs.getString("ORGANIZATION_ID"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return user;
	}
	
	public static LIMSUser getUserByEmail(String email) throws SQLException{

		LIMSUser user = null;
		Connection conn = ConnectionManager.getConnection();
		String query  =
				"SELECT RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, LAB, EMAIL, " +
				"PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID FROM RESEARCHER WHERE EMAIL = ?";

		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, email.trim());
			ResultSet rs = ps.executeQuery();
	
			while (rs.next()) {
	
				user = new LIMSUser(
						rs.getString("RESEARCHER_ID"),
						rs.getString("LAST_NAME"),
						rs.getString("FIRST_NAME"),
						rs.getString("LAB"),
						rs.getString("EMAIL"),
						rs.getString("PHONE"),
						rs.getString("USERNAME"),
						rs.getBoolean("SUPER_USER"),
						rs.getString("AFFILIATION"));
				user.setOrganizationId(rs.getString("ORGANIZATION_ID"));
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return user;
	}

	public static String encryptString(String toEncrypt) throws InvalidArgumentException{

	    Key aesKey = new SecretKeySpec(PWD_ENCRYPTION_KEY.getBytes(), DEFAULT_CIFER_ALGORITHM);
	    Cipher cipher = null;
	    byte[] encrypted = null;
		try {
			cipher = Cipher.getInstance(DEFAULT_CIFER_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Failed get Cifer instance", e);
		}
		if(cipher != null) {
		    try {
				cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			} catch (InvalidKeyException e) {
				logger.error("Invalid cifer key", e);
			}
			try {
				encrypted = cipher.doFinal(toEncrypt.getBytes());
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Failed to encrypt the data", e);
			}
		}
		if(encrypted == null) {
			throw new InvalidArgumentException("Failed to encrypt the data");
		}
	    return Base64.getEncoder().encodeToString(encrypted);
	}

	public static String decryptString(String encData) throws InvalidArgumentException {

	    Key aesKey = new SecretKeySpec(PWD_ENCRYPTION_KEY.getBytes(), DEFAULT_CIFER_ALGORITHM);
	    Cipher cipher = null;
	    byte[] decrypted = null;
		try {
			cipher = Cipher.getInstance(DEFAULT_CIFER_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Failed get Cifer instance", e);
		}
		if(cipher != null) {
		    try {
				cipher.init(Cipher.DECRYPT_MODE, aesKey);
				decrypted = cipher.doFinal(Base64.getDecoder().decode(encData.getBytes()));
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Failed to decrypt the data", e);
			}		    
		}
		if(decrypted == null) {
			throw new InvalidArgumentException("Failed to decrypt the data");
		}
	    return new String(decrypted).trim();
	}

	public static Collection<LIMSUser> getUserList() throws SQLException{

		Collection<LIMSUser> users = new TreeSet<LIMSUser>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, LAB, EMAIL, " +
			"PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID, DELETED FROM RESEARCHER "
			+ "WHERE DELETED IS NULL ORDER BY RESEARCHER_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSUser user = new LIMSUser(
						rs.getString("RESEARCHER_ID"),
						rs.getString("LAST_NAME"),
						rs.getString("FIRST_NAME"),
						rs.getString("LAB"),
						rs.getString("EMAIL"),
						rs.getString("PHONE"),
						rs.getString("USERNAME"),
						rs.getBoolean("SUPER_USER"),
						rs.getString("AFFILIATION"));
				user.setOrganizationId(rs.getString("ORGANIZATION_ID"));
				users.add(user);
			}
			rs.close();
		}
		ConnectionManager.releaseConnection(conn);
		return users;
	}
	
	public static Collection<LIMSUser> getCompleteUserList() throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<LIMSUser> users = getCompleteUserList(conn);
		ConnectionManager.releaseConnection(conn);
		return users;
	}
	
	public static Collection<LIMSUser> getCompleteUserList(Connection conn) throws SQLException{

		Collection<LIMSUser> users = new TreeSet<LIMSUser>();		
		String query  =
			"SELECT RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, LAB, EMAIL, " +
			"PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID, DELETED FROM RESEARCHER "
			+ "ORDER BY RESEARCHER_ID";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
	
				LIMSUser user = new LIMSUser(
						rs.getString("RESEARCHER_ID"),
						rs.getString("LAST_NAME"),
						rs.getString("FIRST_NAME"),
						rs.getString("LAB"),
						rs.getString("EMAIL"),
						rs.getString("PHONE"),
						rs.getString("USERNAME"),
						rs.getBoolean("SUPER_USER"),
						rs.getString("AFFILIATION"));
				user.setOrganizationId(rs.getString("ORGANIZATION_ID"));
				if(rs.getString("DELETED") != null)
					user.setActive(false);
				
				users.add(user);
			}
			rs.close();	
		}
		return users;
	}
	
	public static String addNewUser(LIMSUser user, String password) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String limsUserId = addNewUser(user, password, conn);	
		ConnectionManager.releaseConnection(conn);
		return limsUserId;
	}
	
	public static String addNewUser(LIMSUser user, String password, Connection conn) throws SQLException{
		
		String encryptedPassword = null;
		if(password != null) {
			try {
				encryptedPassword = encryptString(password);
			} catch (InvalidArgumentException e) {
				logger.error("Failed to encrypt password", e);
			}
		}
		if(encryptedPassword == null) {
			throw new SQLException("Failed to encrypt password");
		}
		String limsUserId = SQLUtils.getNextIdFromSequence(conn, 
				"RESEARCHER_SEQ",
				DataPrefix.LIMS_USER,
				"0",
				5);
		user.setId(limsUserId);
		String query  =
			"INSERT INTO RESEARCHER (RESEARCHER_ID, USERNAME, PASSWORD, LAST_NAME, FIRST_NAME, "
			+ "LAB, EMAIL, PHONE, SUPER_USER, AFFILIATION, ORGANIZATION_ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, user.getId());
			ps.setString(2, user.getUserName());
			ps.setString(3, encryptedPassword);
			ps.setString(4, user.getLastName());
			ps.setString(5, user.getFirstName());
			ps.setString(6, user.getLaboratory());
			ps.setString(7, user.getEmail());
			ps.setString(8, user.getPhone());
			
			if(user.isSuperUser())
				ps.setString(9, "1");
			else
				ps.setString(9, "0");
			
			ps.setString(10, user.getAffiliation());
			ps.setString(11, user.getOrganizationId());	
			ps.executeUpdate();
		}	
		return limsUserId;
	}
	
	public static void addNewMetlimsUser(LIMSUser user, Connection conn) throws SQLException{
		
		String query  =
			"INSERT INTO RESEARCHER (RESEARCHER_ID, USERNAME, LAST_NAME, FIRST_NAME, "
			+ "LAB, EMAIL, PHONE, SUPER_USER, AFFILIATION) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, user.getId());
			ps.setString(2, user.getUserName());
			ps.setString(3, user.getLastName());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getLaboratory());
			ps.setString(6, user.getEmail());
			ps.setString(7, user.getPhone());
			ps.setString(8, "0");		
			ps.setString(9, user.getAffiliation());	
			ps.executeUpdate();
		}
	}
	
	public static void editUser(LIMSUser user, String password) throws SQLException{
		
		String encryptedPassword = null;
		try {
			encryptedPassword = encryptString(password);
		} catch (InvalidArgumentException e) {
			logger.error("Failed to encrypt password", e);
		}
		if(encryptedPassword == null) {
			throw new SQLException("Failed to encrypt password");
		}	
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE RESEARCHER SET USERNAME = ?, PASSWORD = ?, LAST_NAME = ?, FIRST_NAME = ?, "
			+ "LAB = ?, EMAIL = ?, PHONE = ?, SUPER_USER = ?, AFFILIATION = ?, "
			+ "ORGANIZATION_ID = ? WHERE RESEARCHER_ID = ?";		
		try(PreparedStatement ps = conn.prepareStatement(query)){
		
			ps.setString(1, user.getUserName());
			ps.setString(2, encryptedPassword);
			ps.setString(3, user.getLastName());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getLaboratory());
			ps.setString(6, user.getEmail());
			ps.setString(7, user.getPhone());
			
			if(user.isSuperUser())
				ps.setString(8, "1");
			else
				ps.setString(8, "0");
			
			ps.setString(9, user.getAffiliation());
			ps.setString(10, user.getOrganizationId());
			ps.setString(11, user.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void editUser(LIMSUser user) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE RESEARCHER SET USERNAME = ?, LAST_NAME = ?, FIRST_NAME = ?, "
			+ "LAB = ?, EMAIL = ?, PHONE = ?, SUPER_USER = ?, AFFILIATION = ?, "
			+ "ORGANIZATION_ID = ? WHERE RESEARCHER_ID = ?";		
		try(PreparedStatement ps = conn.prepareStatement(query)){		
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getLastName());
			ps.setString(3, user.getFirstName());
			ps.setString(4, user.getLaboratory());
			ps.setString(5, user.getEmail());
			ps.setString(6, user.getPhone());
			
			if(user.isSuperUser())
				ps.setString(7, "1");
			else
				ps.setString(7, "0");
			
			ps.setString(8, user.getAffiliation());
			ps.setString(9, user.getOrganizationId());
			ps.setString(10, user.getId());
			ps.executeUpdate();
		}	
		ConnectionManager.releaseConnection(conn);
	}
	
	public static boolean changePassword(LIMSUser user, String oldPassword, String newPassword) throws SQLException{
		
		if(getUserLogon(user.getUserName(), oldPassword) == null)
			return false;			
			
		String encryptedPassword = null;
		try {
			encryptedPassword = encryptString(newPassword);
		} catch (InvalidArgumentException e) {
			logger.error("Failed to encrypt new password", e);
		}
		if(encryptedPassword == null) {
			throw new SQLException("Failed to encrypt password");
		}	
		Connection conn = ConnectionManager.getConnection();		
		String query  =
			"UPDATE RESEARCHER SET PASSWORD = ? WHERE RESEARCHER_ID = ?";
		
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, encryptedPassword);
			ps.setString(2, user.getId());
			ps.executeUpdate();
		}	
		ConnectionManager.releaseConnection(conn);	
		return true;
	}

	public static void deleteUser(LIMSUser user) throws SQLException{
		
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"DELETE FROM RESEARCHER WHERE RESEARCHER_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, user.getId());
			ps.executeUpdate();
		}
		ConnectionManager.releaseConnection(conn);
	}
}






















