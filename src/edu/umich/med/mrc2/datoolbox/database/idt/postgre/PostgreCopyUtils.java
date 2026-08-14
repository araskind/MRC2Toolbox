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

package edu.umich.med.mrc2.datoolbox.database.idt.postgre;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.DatabaseIdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;

public class PostgreCopyUtils {
	
	private static final Logger logger = LogManager.getLogger(PostgreCopyUtils.class);
	
	private static final File tmpFolder = new File("C:\\Users\\Sasha\\Downloads\\TMP");

	private PostgreCopyUtils() {
		/* This utility class should not be instantiated */
	}

	public static Connection getPostGreConnection() throws SQLException {

		//	Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/idtracker";
		Properties props = new Properties();
		props.setProperty("user","idtracker");
		props.setProperty("password","&IDTrack3r");
		props.setProperty("currentSchema","idtracker");
		props.setProperty("loggerLevel","OFF");
		return DriverManager.getConnection(url, props);		
	}
	
	public static void copyLimsExperiments() throws SQLException {
		
		Collection<LIMSExperiment> experiments = LIMSDataCache.getExperiments();
		Connection pgConnection =  getPostGreConnection();
		String insSql =
				"INSERT INTO LIMS_EXPERIMENT (EXPERIMENT_ID, EXPERIMENT_NAME, PROJECT_ID,  " +
				"EXPERIMENT_DESCRIPTION, DATE_INITIATED, NOTES, " +
				"OWNER_ID, SERVICE_REQUEST_ID) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";				
		try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
			
			int counter = 0;
			for(LIMSExperiment experiment :  experiments) {
					
				insPs.setString(1, experiment.getId());
				insPs.setString(2, experiment.getName());
				insPs.setString(3, experiment.getProject().getId());
				insPs.setString(4, experiment.getDescription());
				
				java.sql.Date startDate = null;
				if(experiment.getStartDate() != null) {
					try {
						startDate = new java.sql.Date(experiment.getStartDate().getTime());
					} catch (Exception e1) {
						logger.error(String.format("%s %s", "Failed to parse start date for experiment", experiment.getId()), e1);
					}
				}
				if(startDate != null)
					insPs.setDate(5, new java.sql.Date(experiment.getStartDate().getTime()));
				else
					insPs.setNull(5, java.sql.Types.NULL);
					
				insPs.setString(6, experiment.getNotes());
				
				if(experiment.getCreator() != null)
					insPs.setString(7, experiment.getCreator().getId());
				else
					insPs.setNull(7, java.sql.Types.NULL);
					
				insPs.setString(8, experiment.getServiceRequestId());
				insPs.addBatch();				
				if(++counter % 100 == 0) 
					insPs.executeBatch();
			}
			insPs.executeBatch();
		}
		pgConnection.close();
	}
	
	public static void copyCompoundDatabases() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		String selectSql =
				"SELECT DATABASE_ID, DATABASE_NAME, LINK_PREFIX, LINK_SUFFIX, HOME_PAGE, IN_SILICO, DESCRIPTION " +
				"FROM COMPOUND_DATABASES ORDER BY DATABASE_ID";
		String insSql =
				"INSERT INTO COMPOUND_DATABASES (DATABASE_ID, DATABASE_NAME, LINK_PREFIX, "
				+ "LINK_SUFFIX, HOME_PAGE, IN_SILICO, DESCRIPTION) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";	
		
		try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
			
			try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
			
				ResultSet rs = selPs.executeQuery();
				while(rs.next()) {
					
					insPs.setString(1, rs.getString("DATABASE_ID"));
					insPs.setString(2, rs.getString("DATABASE_NAME"));
					insPs.setString(3, rs.getString("LINK_PREFIX"));
					insPs.setString(4, rs.getString("LINK_SUFFIX"));
					insPs.setString(5, rs.getString("HOME_PAGE"));
					insPs.setString(6, rs.getString("IN_SILICO"));
					insPs.setString(7, rs.getString("DESCRIPTION"));		
					insPs.addBatch();
				}	
				insPs.executeBatch();
				rs.close();
			}
		}		
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyCompoundData() throws SQLException {
			
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		String selectSql =
				"SELECT ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS,  " +
				"SMILES, INCHI, INCHI_KEY, NUMC, BULK_ABBREV, CHAIN_ABBREV,  " +
				"BULK_ACCESSION, CHARGE, INCHI_KEY_CONNECT, PEPTIDE_SEQUENCE, IS_THEORETICAL " +
				"FROM COMPOUND_DATA ORDER BY ACCESSION";
		String insSql =
				"INSERT INTO COMPOUND_DATA (ACCESSION, SOURCE_DB, PRIMARY_NAME, MOL_FORMULA, EXACT_MASS,  " +
				"SMILES, INCHI, INCHI_KEY, NUMC, BULK_ABBREV, CHAIN_ABBREV,  " +
				"BULK_ACCESSION, CHARGE, INCHI_KEY_CONNECT, PEPTIDE_SEQUENCE, IS_THEORETICAL) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";	
		
		try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
			
			try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
			
				int resCount = 0;
				ResultSet rs = selPs.executeQuery();
				while(rs.next()) {
					
					insPs.setString(1, rs.getString("ACCESSION"));
					insPs.setString(2, rs.getString("SOURCE_DB"));
					insPs.setString(3, rs.getString("PRIMARY_NAME"));
					insPs.setString(4, rs.getString("MOL_FORMULA"));
					insPs.setDouble(5, rs.getDouble("EXACT_MASS"));
					insPs.setString(6, rs.getString("SMILES"));
					insPs.setString(7, rs.getString("INCHI"));
					insPs.setString(8, rs.getString("INCHI_KEY"));
					insPs.setString(9, rs.getString("NUMC"));
					insPs.setString(10, rs.getString("BULK_ABBREV"));
					insPs.setString(11, rs.getString("CHAIN_ABBREV"));
					insPs.setString(12, rs.getString("BULK_ACCESSION"));
					insPs.setInt(13, rs.getInt("CHARGE"));			
					insPs.setString(14, rs.getString("INCHI_KEY_CONNECT"));
					insPs.setString(15, rs.getString("PEPTIDE_SEQUENCE"));
					insPs.setString(16, rs.getString("IS_THEORETICAL"));			
					insPs.addBatch();
					if(++resCount % 1000 == 0)
						insPs.executeBatch();					
				}
				insPs.executeBatch();			
				rs.close();
			}
		}	
		pgConnection.close();
		mrcConnection.close();
	}

	public static void copyCompoundDescriptions() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		String selectSql =
				"SELECT ACCESSION, DESCRIPTION, CS_DESCRIPTION " +
				"FROM COMPOUND_DESCRIPTION ORDER BY ACCESSION";
		String insSql =
				"INSERT INTO COMPOUND_DESCRIPTION (ACCESSION, DESCRIPTION, CS_DESCRIPTION) " +
				"VALUES (?, ?, ?)";	
		
		try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
			
			try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
			
				ResultSet rs = selPs.executeQuery();
				int resCount = 0;
				while(rs.next()) {
					
					insPs.setString(1, rs.getString("ACCESSION"));
					insPs.setString(2, rs.getString("DESCRIPTION"));
					insPs.setString(3, rs.getString("CS_DESCRIPTION"));
					insPs.addBatch();
					if(++resCount % 1000 == 0)
						insPs.executeBatch();
		
				}
				insPs.executeBatch();
				rs.close();
			}
		}	
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyReferenceLibraryComponents() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		
		String libSql =
				"SELECT DISTINCT LIBRARY_NAME, POLARITY FROM REF_MSMS_LIBRARY_COMPONENT ORDER BY 1,2";
		String selectSql =
				"SELECT MRC2_LIB_ID, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ,  " +
				"ADDUCT, COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE,  " +
				"MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION,  " +
				"SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH,  " +
				"RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE,  " +
				"ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, MAX_DIGITS,  " +
				"ENTROPY, PRECURSOR_NEUTRAL_MASS_DIFF, SPECTRUM_SD, NUM_PEAKS " +
				"FROM REF_MSMS_LIBRARY_COMPONENT " +
				"WHERE LIBRARY_NAME = ? AND POLARITY = ? " +
				"ORDER BY MRC2_LIB_ID";
		String insSql =
				"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
				"MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ,  " +
				"ADDUCT, COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE,  " +
				"MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION,  " +
				"SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH,  " +
				"RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME,  " +
				"ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, MAX_DIGITS,  " +
				"ENTROPY, PRECURSOR_NEUTRAL_MASS_DIFF, SPECTRUM_SD, NUM_PEAKS)  " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		
		try(PreparedStatement libPs = mrcConnection.prepareStatement(libSql)){	

			try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
					
				try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
				
					ResultSet libRs = libPs.executeQuery();
					while(libRs.next()) {
						
						int resCount = 0;
						
						String libName = libRs.getString("LIBRARY_NAME");
						String polarity = libRs.getString("POLARITY");
						logger.trace(String.format("%s %s", libName, polarity));
						
						selPs.setString(1, libName);
						selPs.setString(2, polarity);
						ResultSet rs = selPs.executeQuery();
						
						insPs.setString(2, polarity);
						insPs.setString(22, libName);					
						
						while(rs.next()) {							
							insPs.setString(1, rs.getString("MRC2_LIB_ID"));							
							insPs.setString(3, rs.getString("IONIZATION"));
							insPs.setString(4, rs.getString("COLLISION_ENERGY"));				
							insPs.setDouble(5, rs.getDouble("PRECURSOR_MZ"));				
							insPs.setString(6, rs.getString("ADDUCT"));
							insPs.setString(7, rs.getString("COLLISION_GAS"));
							insPs.setString(8, rs.getString("INSTRUMENT"));
							insPs.setString(9, rs.getString("INSTRUMENT_TYPE"));
							insPs.setString(10, rs.getString("IN_SOURCE_VOLTAGE"));
							insPs.setString(11, rs.getString("MSN_PATHWAY"));
							insPs.setString(12, rs.getString("PRESSURE"));
							insPs.setString(13, rs.getString("SAMPLE_INLET"));
							insPs.setString(14, rs.getString("SPECIAL_FRAGMENTATION"));
							insPs.setString(15, rs.getString("SPECTRUM_TYPE"));
							insPs.setString(16, rs.getString("CHROMATOGRAPHY_TYPE"));
							insPs.setString(17, rs.getString("CONTRIBUTOR"));
							insPs.setString(18, rs.getString("SPLASH"));
							insPs.setString(19, rs.getString("RESOLUTION"));
							insPs.setString(20, rs.getString("SPECTRUM_SOURCE"));
							insPs.setString(21, rs.getString("IONIZATION_TYPE"));									
							insPs.setString(23, rs.getString("ORIGINAL_LIBRARY_ID"));
							insPs.setString(24, rs.getString("ACCESSION"));
							insPs.setString(25, rs.getString("SPECTRUM_HASH"));				
							insPs.setInt(26,  rs.getInt("MAX_DIGITS"));				
							insPs.setDouble(27, rs.getDouble("ENTROPY"));
							insPs.setDouble(28, rs.getDouble("PRECURSOR_NEUTRAL_MASS_DIFF"));
							insPs.setDouble(29, rs.getDouble("SPECTRUM_SD"));				
							insPs.setInt(30,  rs.getInt("NUM_PEAKS"));
							insPs.addBatch();
							if(++resCount % 1000 == 0)
								insPs.executeBatch();
						}
						insPs.executeBatch();
						rs.close();
					}
					libRs.close();	
				}
			}
		}	
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyReferenceLibraryPeaks() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		String libSql =
				"SELECT DISTINCT LIBRARY_NAME, POLARITY FROM REF_MSMS_LIBRARY_COMPONENT ORDER BY 1,2";
		String selectSql =
				"SELECT P.MRC2_LIB_ID, P.MZ, P.INTENSITY, P.FRAGMENT_COMMENT, P.IS_PARENT, P.MZ_ACCURACY " +
				"FROM REF_MSMS_LIBRARY_PEAK P, REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE P.MRC2_LIB_ID = C.MRC2_LIB_ID " +
				"AND C.LIBRARY_NAME = ? AND C.POLARITY = ?  " +
				"ORDER BY P.MRC2_LIB_ID, P.MZ ";
		String insSql =
				"INSERT INTO REF_MSMS_LIBRARY_PEAK ( " +
				"MRC2_LIB_ID, MZ, INTENSITY, FRAGMENT_COMMENT, IS_PARENT, MZ_ACCURACY)  " +
				"VALUES(?, ?, ?, ?, ?, ?) ";
		
		try(PreparedStatement libPs = mrcConnection.prepareStatement(libSql)){
		
			try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
					
				try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
				
					ResultSet libRs = libPs.executeQuery();
					while(libRs.next()) {
						String libName = libRs.getString("LIBRARY_NAME");
						String polarity = libRs.getString("POLARITY");
			
						logger.trace(String.format("%s %s (%s)", "Processing library", libName, polarity));
						
						selPs.setString(1, libName);
						selPs.setString(2, polarity);
						
						ResultSet rs = selPs.executeQuery();
						int resCount = 0;
						while(rs.next()) {
							
							insPs.setString(1, rs.getString("MRC2_LIB_ID"));
							insPs.setDouble(2, rs.getDouble("MZ"));
							insPs.setDouble(3, rs.getDouble("INTENSITY"));
							insPs.setString(4, rs.getString("FRAGMENT_COMMENT"));				
							insPs.setString(5, rs.getString("IS_PARENT"));				
							insPs.setInt(6, rs.getInt("MZ_ACCURACY"));
							insPs.addBatch();
							if(++resCount % 10000 == 0)
								insPs.executeBatch();
						}
						insPs.executeBatch();
						rs.close();
					}
					libRs.close();	
				}
			}
		}	
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyRefMSMSLibraryEntryProperties() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		String selectSql =
				"SELECT MRC2_LIB_ID, PROPERTY_NAME, PROPERTY_VALUE " +
				"FROM REF_MSMS_PROPERTIES ORDER BY MRC2_LIB_ID";
		String insSql =
				"INSERT INTO REF_MSMS_PROPERTIES (MRC2_LIB_ID, PROPERTY_NAME, PROPERTY_VALUE) " +
				"VALUES (?, ?, ?)";		
		
		try(PreparedStatement selPs = mrcConnection.prepareStatement(selectSql)){
		
			try(PreparedStatement insPs = pgConnection.prepareStatement(insSql)){
			
				ResultSet rs = selPs.executeQuery();
				int resCount = 0;
				while(rs.next()) {
					
					insPs.setString(1, rs.getString("MRC2_LIB_ID"));
					insPs.setString(2, rs.getString("PROPERTY_NAME"));
					insPs.setString(3, rs.getString("PROPERTY_VALUE"));
					insPs.addBatch();
					if(++resCount % 1000 == 0)
						insPs.executeBatch();
				}
				insPs.executeBatch();
				rs.close();
			}
		}	
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyAcquisitionMethods() throws SQLException {
		
		Collection<DataAcquisitionMethod> methods = IDTDataCache.getAcquisitionMethods();
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
				
		String query  =
				"INSERT INTO DATA_ACQUISITION_METHOD (ACQ_METHOD_ID, METHOD_NAME, " +
				"METHOD_DESCRIPTION, POLARITY, CREATED_BY, CREATED_ON, IONIZATION_TYPE, " +
				"MASS_ANALYZER, MS_TYPE, COLUMN_ID, SEPARATION_TYPE) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String fileQuery = "SELECT METHOD_CONTAINER FROM DATA_ACQUISITION_METHOD WHERE ACQ_METHOD_ID = ?";
		String insFileQuery  =
				"UPDATE DATA_ACQUISITION_METHOD SET METHOD_CONTAINER = ? WHERE ACQ_METHOD_ID = ?";
		
		try(PreparedStatement insPs = pgConnection.prepareStatement(query)){
		
			try(PreparedStatement filePs = mrcConnection.prepareStatement(fileQuery)){
			
				try(PreparedStatement insFilePs = pgConnection.prepareStatement(insFileQuery)){
						 
					for(DataAcquisitionMethod method : methods) {	
						
						//	Insert all method data 
						insPs.setString(1, method.getId());
						insPs.setString(2, method.getName());
						insPs.setString(3, method.getDescription());
						insPs.setString(4, method.getPolarity().getCode());
						String userId = "U00077";
						if(method.getCreatedBy()!= null)
							userId = method.getCreatedBy().getId();
							
						insPs.setString(5, userId);
						Date dc = method.getCreatedOn();
						if(dc == null)
							dc = new Date();
						
						insPs.setDate(6, new java.sql.Date(dc.getTime()));			
						insPs.setString(7, method.getIonizationType().getId());
						insPs.setString(8, method.getMassAnalyzerType().getId());
						insPs.setString(9, method.getMsType().getId());
						
						if(method.getColumn() != null)
							insPs.setString(10, method.getColumn().getColumnId());
						else
							insPs.setNull(10, java.sql.Types.NULL);
						
						insPs.setString(11, method.getSeparationType().getId());
						insPs.executeUpdate();
			
						filePs.setString(1, method.getId());
						ResultSet rs = filePs.executeQuery();		
						while(rs.next()) {
			
						   File zipFile = Paths.get(tmpFolder.getAbsolutePath(), method.getName() + ".zip").toFile();
						   Blob blob = rs.getBlob(1);
						   if(blob != null) {			
							   writeBlobToFile(blob, zipFile);				   
							   writeZipFileToBlob(zipFile, insFilePs, method.getId());
						   }
						}
						rs.close();
						logger.trace(String.format("%s %s copied", "Method", method.getName()));
					}
				}
			}
		}
		pgConnection.close();
		mrcConnection.close();
	}
	
	private static void writeZipFileToBlob(File zipFile, PreparedStatement insFilePs, String methodId) {
		
		if(!zipFile.exists())
			return;
		
		int streamLength = (int) zipFile.length();
		try(FileInputStream fis = new FileInputStream(zipFile)){
			
			insFilePs.setString(2, methodId);
			insFilePs.setBinaryStream(1, fis, streamLength);
			insFilePs.executeUpdate();
		} catch (FileNotFoundException e) {
			logger.error(String.format("%s %s", "File not found", zipFile.getAbsolutePath()));
		} catch (IOException e) {
			logger.error("IOException while writing file to BLOB", e);
		} catch (SQLException e) {
			logger.error("SQLException while writing file to BLOB", e);
		}
	    try {
			Files.delete(Paths.get(zipFile.getAbsolutePath()));
		} catch (IOException e) {
			logger.error(String.format("%s %s", "Failed to delete temporary ZIP file", zipFile.getAbsolutePath()), e);
		}		
	}
	
	private static void writeBlobToFile(Blob blob, File zipFile) {
		
			if(blob == null)
				return;
		
		   try(BufferedInputStream is = new BufferedInputStream(blob.getBinaryStream())){
			   
			   try(FileOutputStream fos = new FileOutputStream(zipFile)){
				   byte[] buffer = new byte[2048];
				   int r = 0;
				   while((r = is.read(buffer))!=-1)
				      fos.write(buffer, 0, r);
		
				   fos.flush();
			   }
			   blob.free();
		   } catch (IOException e) {
			   logger.error(String.format("%s %s", "Failed to write BLOB to file", zipFile.getAbsolutePath()), e);
		} catch (SQLException e) {
			logger.error("Failed to read or free the BLOB", e);;
		}		  
	}
	
	public static void copyDataExtractionMethods() throws SQLException {
		
		Collection<DataExtractionMethod> methods = IDTDataCache.getDataExtractionMethods();		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();
		
		String query  =
				"INSERT INTO DATA_EXTRACTION_METHOD (EXTRACTION_METHOD_ID, METHOD_NAME, " +
				"METHOD_DESCRIPTION, CREATED_BY, CREATED_ON) " +
				"VALUES (?, ?, ?, ?, ?)";
		String fileQuery = "SELECT METHOD_CONTAINER FROM DATA_EXTRACTION_METHOD WHERE EXTRACTION_METHOD_ID = ?";
		String insFileQuery  =
				"UPDATE DATA_EXTRACTION_METHOD SET METHOD_CONTAINER = ? WHERE EXTRACTION_METHOD_ID = ?";
		
		try(PreparedStatement insPs = pgConnection.prepareStatement(query)){	
		
			try(PreparedStatement filePs = mrcConnection.prepareStatement(fileQuery)){		
	
				try(PreparedStatement insFilePs = pgConnection.prepareStatement(insFileQuery)){
			
					for(DataExtractionMethod method : methods) {	
						
						//	Insert all method data 
						insPs.setString(1, method.getId());
						insPs.setString(2, method.getName());
						insPs.setString(3, method.getDescription());
						String userId = "U00077";
						if(method.getCreatedBy()!= null)
							userId = method.getCreatedBy().getId();
							
						insPs.setString(4, userId);
						Date dc = method.getCreatedOn();
						if(dc == null)
							dc = new Date();
						
						insPs.setDate(5, new java.sql.Date(dc.getTime()));			
						insPs.executeUpdate();
			
						filePs.setString(1, method.getId());
						ResultSet rs = filePs.executeQuery();		
						while(rs.next()) {
			
						   File zipFile = Paths.get(tmpFolder.getAbsolutePath(), method.getName() + ".zip").toFile();
						   Blob blob = rs.getBlob(1);
						   if(blob != null) {
							   writeBlobToFile(blob, zipFile);				   
							   writeZipFileToBlob(zipFile, insFilePs, method.getId());
						   }
						}
						rs.close();	
						logger.trace(String.format("%s %s copied", "Method", method.getName()));
					}
				}
			}
		}
		pgConnection.close();
		mrcConnection.close();
	}
	
	public static void copyDocuments() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();	
		String selectQuery = 
				"SELECT DOCUMENT_ID, DOCUMENT_NAME, DOCUMENT_FORMAT, "
				+ "DOCUMENT_HASH FROM DOCUMENTS ORDER BY DOCUMENT_ID";
		String query  =
				"INSERT INTO DOCUMENTS (DOCUMENT_ID, DOCUMENT_NAME, DOCUMENT_FORMAT, DOCUMENT_HASH) " +
				"VALUES (?, ?, ?, ?)";
		String fileQuery = "SELECT DOCUMENT_CONTENTS FROM DOCUMENTS WHERE DOCUMENT_ID = ?";
		String insFileQuery  =
				"UPDATE DOCUMENTS SET DOCUMENT_CONTENTS = ? WHERE DOCUMENT_ID = ?";
		
		try(PreparedStatement selectPs =  mrcConnection.prepareStatement(selectQuery)){
		
			try(PreparedStatement insPs = pgConnection.prepareStatement(query)){
				
				try(PreparedStatement filePs = mrcConnection.prepareStatement(fileQuery)){		
	
					try(PreparedStatement insFilePs = pgConnection.prepareStatement(insFileQuery)){
			
						ResultSet docRs = selectPs.executeQuery();
						while(docRs.next()) {	
							
							String docId =  docRs.getString("DOCUMENT_ID");
							//	Insert all method data 
							insPs.setString(1, docId);
							insPs.setString(2, docRs.getString("DOCUMENT_NAME"));
							insPs.setString(3, docRs.getString("DOCUMENT_FORMAT"));
							insPs.setString(4, docRs.getString("DOCUMENT_HASH"));
							insPs.executeUpdate();
				
							filePs.setString(1, docId);
							ResultSet rs = filePs.executeQuery();		
							while(rs.next()) {
				
							   File zipFile = Paths.get(tmpFolder.getAbsolutePath(), docId + ".zip").toFile();
							   Blob blob = rs.getBlob(1);
							   if(blob != null) {
								   writeBlobToFile(blob, zipFile);				   
								   writeZipFileToBlob(zipFile, insFilePs, docId);
							   }
							}
							rs.close();	
							logger.trace(String.format("%s %s copied", "Document", docId));
						}
					}
				}
			}
		}						
		pgConnection.close();
		mrcConnection.close();
	}

	public static void copySOPs() throws SQLException {
		
		Connection mrcConnection = ConnectionManager.getConnection();
		Connection pgConnection =  getPostGreConnection();	
		String selectQuery = 
				"SELECT SOP_ID, SOP_NAME, SOP_DESCRIPTION, SOP_VERSION, DATE_CRERATED, "
				+ "CREATED_BY, SOP_CATEGORY, SOP_DETAIL_LEVEL, FILE_EXTENSION, SOP_GROUP "
				+ "FROM SOP_PROTOCOL ORDER BY SOP_ID";
		String query  =
				"INSERT INTO SOP_PROTOCOL (SOP_ID, SOP_NAME, SOP_DESCRIPTION, SOP_VERSION, DATE_CRERATED, "
				+ "CREATED_BY, SOP_CATEGORY, SOP_DETAIL_LEVEL, FILE_EXTENSION, SOP_GROUP) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String fileQuery = "SELECT SOP_DOCUMENT FROM SOP_PROTOCOL WHERE SOP_ID = ?";
		String insFileQuery  =
				"UPDATE SOP_PROTOCOL SET SOP_DOCUMENT = ? WHERE SOP_ID = ?";
		
		try(PreparedStatement selectPs =  mrcConnection.prepareStatement(selectQuery)){	

			try(PreparedStatement insPs = pgConnection.prepareStatement(query)){			
		
				try(PreparedStatement filePs = mrcConnection.prepareStatement(fileQuery)){			

					try(PreparedStatement insFilePs = pgConnection.prepareStatement(insFileQuery)){	

						ResultSet docRs = selectPs.executeQuery();
						while(docRs.next()) {	
							
							String sopId =  docRs.getString("SOP_ID");
				
							insPs.setString(1, sopId);
							insPs.setString(2, docRs.getString("SOP_NAME"));
							insPs.setString(3, docRs.getString("SOP_DESCRIPTION"));
							insPs.setString(4, docRs.getString("SOP_VERSION"));
							insPs.setDate(5, docRs.getDate("DATE_CRERATED"));
							insPs.setString(6, docRs.getString("CREATED_BY"));		
							insPs.setString(7, docRs.getString("SOP_CATEGORY"));
							insPs.setString(8, docRs.getString("SOP_DETAIL_LEVEL"));
							insPs.setString(9, docRs.getString("FILE_EXTENSION"));
							insPs.setString(10, docRs.getString("SOP_GROUP"));
							
							insPs.executeUpdate();
				
							filePs.setString(1, sopId);
							ResultSet rs = filePs.executeQuery();		
							while(rs.next()) {
				
							   File zipFile = Paths.get(tmpFolder.getAbsolutePath(), sopId + ".zip").toFile();
							   Blob blob = rs.getBlob(1);
							   if(blob != null) {
				
								   writeBlobToFile(blob, zipFile);				   
								   writeZipFileToBlob(zipFile, insFilePs, sopId);
							   }
							}
							rs.close();	
							logger.trace(String.format("%s %s copied", "Document", sopId));
						}
					}
				}
			}
		}
		pgConnection.close();
		mrcConnection.close();
	}
		
	public static void copyNISTPepSearchParameterObjects() throws Exception {
		
		Connection mrcConnection = ConnectionManager.getConnection();	
		Collection<NISTPepSearchParameterObject>npsObjects = 
				DatabaseIdentificationUtils.getNISTPepSearchParameterObjects(mrcConnection);				
		Connection pgConnection =  getPostGreConnection();
		String query =
				"INSERT INTO NIST_PEPSEARCH_PARAMETERS (PARAMETER_SET_ID, PARAMETER_SET_OBJECT, "
				+ "SEARCH_PARAMETERS_HASH, FILTER_PARAMETERS_HASH) VALUES (?, ?, ?, ?)";
		try(PreparedStatement ps = pgConnection.prepareStatement(query)){
			
			int counter = 0;
			for(NISTPepSearchParameterObject parObject : npsObjects) {
				
				try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
					
					try(ObjectOutputStream out = new ObjectOutputStream(baos)){
						
						out.writeObject(parObject);
						byte[] ba = baos.toByteArray();					
						try(InputStream bais = new ByteArrayInputStream(ba)){
							ps.setString(1, parObject.getId());
							ps.setBinaryStream(2, bais, ba.length);
							ps.setString(3, parObject.getSearchParametersMD5string());
							ps.setString(4, parObject.getResultFilteringParametersMD5string());	
						}
					}
				}
				ps.addBatch();				
				if(++counter % 20 == 0) 
					ps.executeBatch();
			}
			ps.executeBatch();
		}
		pgConnection.close();
		mrcConnection.close();
	}
}



















