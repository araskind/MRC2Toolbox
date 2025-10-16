/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class NISTPeptideLibraryParserUploader {
	
	private static Collection<NISTPeptideTandemMassSpectrum>msmsDataSet;
	
	private static File libraryFile = 			
			new File("E:\\DataAnalysis\\Databases\\NIST\\NIST peptide library\\Human\\Human "
					+ "Ion Trap Library 2014-05-29\\MSP\\human_consensus_final_true_lib.msp");
	
	private static String libraryName = "human_consensus_final_true_lib";
	private static String resolution = "LOW";
	private static String instrumentType = "Ion Trap";
	private static String organism = "human";
	private static String ionizationType = "CID";
	private static String spectrumSource = "EXPERIMENTAL";
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		
		try {
			parseAndUploadLargeLibraryFile(libraryFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		try {
			//	uploadLibraryData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void parseAndUploadLargeLibraryFile(File libraryFile) {
		
		System.out.println("Parsing input MSP file ...");
		Collection<NISTPeptideTandemMassSpectrum>forUpload = 
				new ArrayList<NISTPeptideTandemMassSpectrum>();
		List<String> chunk = new ArrayList<String>();
		Pattern namePattern = 
				Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":");
		Pattern pnumPattern =
				Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+\\d+");
		Matcher regexMatcher;
		int count = 0;
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(libraryFile, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
		    while (it.hasNext()) {
		    	
		        String line = it.nextLine();
				regexMatcher = namePattern.matcher(line.trim());
				if (regexMatcher.find()) {

					if (!chunk.isEmpty()) {

						String numPeaks = chunk.stream().
								filter(l -> pnumPattern.matcher(l.trim()).find()).
								findFirst().orElse(null);

						if (numPeaks != null) {

							NISTPeptideTandemMassSpectrum pepMsms = 
									NISTPeptideMSPParser.parseNistMspDataSource(chunk);
							forUpload.add(pepMsms);
							
							count++;
							if(count % 50 == 0)
								System.out.print(".");
							
							if(count % 5000 == 0) {
								System.out.println("\nUploading data to the database - " + count);
								try {
									uploadLibraryData(forUpload);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								forUpload.clear();
							}								
						}
						chunk.clear();
					}
				}
				if(!line.trim().isEmpty())
					chunk.add(line.trim());
		    }
			if (!chunk.isEmpty()) {

				String numPeaks = chunk.stream().
						filter(l -> pnumPattern.matcher(l.trim()).find()).
						findFirst().orElse(null);

				if (numPeaks != null) {

					NISTPeptideTandemMassSpectrum pepMsms = 
							NISTPeptideMSPParser.parseNistMspDataSource(chunk);
					forUpload.add(pepMsms);
					System.out.println("\nUploading data to the database - " + count);
					try {
						uploadLibraryData(forUpload);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					forUpload.clear();
				}
				chunk.clear();
			}
		} 
		finally {
		    try {
				it.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void parseLibraryFile(File libraryFile) {

		System.out.println("Parsing input MSP file ...");
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(libraryFile);
		msmsDataSet = new ArrayList<NISTPeptideTandemMassSpectrum>();	
		int count = 0;
		for(List<String> chunk : mspChunks) {

			NISTPeptideTandemMassSpectrum msms = null;
			try {
				msms = NISTPeptideMSPParser.parseNistMspDataSource(chunk);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(msms != null) {
				
				msmsDataSet.add(msms);
				
				count++;
				if(count % 50 == 0)
					System.out.print(".");
				
				if(count % 5000 == 0)
					System.out.println(".");				
			}			
		}
	}
		
	private static void uploadLibraryData(Collection<NISTPeptideTandemMassSpectrum>forUpload) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO NIST_PEPTIDE_LIBRARY_COMPONENT("
				+ "MRC2_LIB_ID, POLARITY, IONIZATION, PRECURSOR_MZ, INSTRUMENT_TYPE, "
				+ "SPECTRUM_TYPE, RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, "
				+ "LIBRARY_NAME, SPECTRUM_HASH, ORGANISM, NAME, PEPTIDE_SEQUENCE, COMMENTS, MODIFICATIONS) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String peakQuery = 
				"INSERT INTO NIST_PEPTIDE_LIBRARY_PEAK("
				+ "MRC2_LIB_ID, MZ, INTENSITY, FRAGMENT_COMMENT, IS_PARENT) "
				+ "VALUES(?, ?, ?, ?, ?)";
		PreparedStatement peakPs = conn.prepareStatement(peakQuery);
		
		String propertiesQuery = 
				"INSERT INTO NIST_PEPTIDE_LIBRARY_ANNOTATION(MRC2_LIB_ID, PROPERTY, PROPERTY_VALUE) "
				+ "VALUES(?, ?, ?)";
				
		PreparedStatement propertiesPs = conn.prepareStatement(propertiesQuery);
		int count = 0;
		for(NISTPeptideTandemMassSpectrum msms : forUpload) {
			
			String libId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);					
			ps.setString(1, libId);	//	MRC2_LIB_ID
			ps.setString(2, msms.getPolarity().getCode());	//	POLARITY
			ps.setString(3, "ESI");	//	IONIZATION
			
			MsPoint prec = msms.getPrecursors().iterator().next();
			if(prec != null)	//	PRECURSOR_MZ	
				ps.setDouble(4, prec.getMz());
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			ps.setString(5, instrumentType);	//	INSTRUMENT_TYPE
			String specType = msms.getProperty(NISTPeptideMSPField.Spec);
			if(specType != null)	//	SPECTRUM_TYPE	
				ps.setString(6, specType);
			else
				ps.setNull(6, java.sql.Types.NULL);

			ps.setString(7, resolution);	//	RESOLUTION
			ps.setString(8, spectrumSource);	//	SPECTRUM_SOURCE
			ps.setString(9, ionizationType);	//	IONIZATION_TYPE
			ps.setString(10, libraryName);	//	LIBRARY_NAME
			
			String specHash = MsUtils.calculateSpectrumHash(msms.getSpectrum());
			ps.setString(11, specHash);	//	SPECTRUM_HASH
			ps.setString(12, organism);	//	ORGANISM
			ps.setString(13, msms.getProperty(NISTPeptideMSPField.NAME));	//	NAME
			ps.setString(14, msms.getPeptideSequence());	//	PEPTIDE_SEQUENCE
			ps.setString(15, msms.getComments());	//	COMMENTS
			
			String mods = msms.getProperty(NISTPeptideMSPField.Mods);
			if(mods != null)	//	MODIFICATIONS	
				ps.setString(16, mods);
			else
				ps.setNull(16, java.sql.Types.NULL);
			
			ps.executeUpdate();
			
			//	Peaks
			peakPs.setString(1, libId);	//	MRC2_LIB_ID
			for(MsPoint p : msms.getSpectrum()) {
				
				peakPs.setDouble(2,  p.getMz());
				peakPs.setDouble(3,  p.getIntensity());
				if(!p.getAdductType().equals("?"))
					peakPs.setString(4, p.getAdductType());
				else
					peakPs.setNull(4, java.sql.Types.NULL);
				
				peakPs.setNull(5, java.sql.Types.NULL);
				peakPs.addBatch();
			}
			for(MsPoint parent : msms.getPrecursors()) {
				
				peakPs.setDouble(2,  parent.getMz());
				peakPs.setDouble(3,  parent.getIntensity());
				peakPs.setNull(4, java.sql.Types.NULL);
				peakPs.setString(5, "Y");
				peakPs.addBatch();
			}
			peakPs.executeBatch();
			
			//	Properties
			propertiesPs.setString(1, libId);	//	MRC2_LIB_ID
			for(Entry<NISTPeptideMSPField, String> property : msms.getProperties().entrySet()) {
				
				propertiesPs.setString(2, property.getKey().name());
				propertiesPs.setString(3, property.getValue());
				propertiesPs.addBatch();
			}
			propertiesPs.executeBatch();
			
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			
			if(count % 5000 == 0)
				System.out.println(".");
		}
		ps.close();
		peakPs.close();
		propertiesPs.close();
		ConnectionManager.releaseConnection(conn);
	}
}















