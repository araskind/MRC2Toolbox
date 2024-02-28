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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class NISTARUSmspParserUploader {
	
	private static File libraryFolder = 			
			new File("E:\\DataAnalysis\\Databases\\NIST\\Annotated "
					+ "Recurrent Unidentified Spectra\\_EXPORT\\urine_it_pos_rec");
	
	private static String libraryName = "urine_it_pos_rec";
	private static String resolution = "HIGH";
	private static String instrumentType = "QTOF";
	private static String organism = "human";
	private static String ionizationType = "CID";
	private static String spectrumSource = "EXPERIMENTAL";
	private static String sampleType = "urine";
	
	private static final Pattern namePattern = 
			Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":");
	private static final Pattern pnumPattern =
			Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+\\d+");
	private static final Pattern polarityPattern = 
			Pattern.compile("(?i)^" + MSPField.ION_MODE.getName() + ":?\\s+([PN])");
	private static final Pattern msmsPattern = 
			Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.]+)\\s?(.+)?");
	
	private static Matcher regexMatcher;
	private static Map<MSPField,Pattern>patternMap;
	private static Map<NISTARUSmspCommentField,Pattern>commentsPatternMap;
	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		
		patternMap = new TreeMap<MSPField,Pattern>();
		for(MSPField field : MSPField.values())
			patternMap.put(field, Pattern.compile("(?i)^" + field.getName() + ":?\\s+(.+)"));

		commentsPatternMap = 
				new TreeMap<NISTARUSmspCommentField,Pattern>();
		for(NISTARUSmspCommentField field : NISTARUSmspCommentField.values())
			commentsPatternMap.put(field, Pattern.compile(field.getName() + "=(.*?) "));
		
		try {
			correctParentIonsForARUSlibs();
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
	
	private static void correctParentIonsForARUSlibs() throws Exception{
		
		String[]libNames = new String[] {
						"plasma_hcd_neg_rec",
						"plasma_hcd_pos_rec",
						"plasma_it_neg_rec",
						"plasma_it_pos_rec",
						"urine_hcd_neg_rec",
						"urine_hcd_pos_rec",
						"urine_it_neg_rec",
						"urine_it_pos_rec",
					};
		Connection conn = ConnectionManager.getConnection();

		String query = 
				"SELECT MRC2_LIB_ID, PRECURSOR_MZ "
				+ "FROM NIST_PEPTIDE_LIBRARY_COMPONENT "
				+ "WHERE LIBRARY_NAME = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String msQuery = 
				"SELECT MZ, INTENSITY FROM NIST_PEPTIDE_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ? AND MZ = ?";
		PreparedStatement msPs = conn.prepareStatement(msQuery);
		
		String msmsQuery = 
				"SELECT MZ, INTENSITY FROM NIST_PEPTIDE_LIBRARY_PEAK "
				+ "WHERE MRC2_LIB_ID = ?";
		PreparedStatement msmsPs = conn.prepareStatement(msmsQuery);
		
		String delQuery = "DELETE FROM NIST_PEPTIDE_LIBRARY_PEAK "
				+ " WHERE MRC2_LIB_ID = ? AND INTENSITY = 1000";
		PreparedStatement delPs = conn.prepareStatement(delQuery);
		
		String updQuery = "UPDATE NIST_PEPTIDE_LIBRARY_PEAK "
				+ "SET IS_PARENT = 'Y' WHERE MRC2_LIB_ID = ? AND MZ = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		String updParentIntensityQuery = "UPDATE NIST_PEPTIDE_LIBRARY_PEAK "
				+ "SET INTENSITY = 9.9 WHERE MRC2_LIB_ID = ? AND IS_PARENT = 'Y'";
		PreparedStatement updParentIntensityPs = conn.prepareStatement(updParentIntensityQuery);
		
		String updQuery2 = "UPDATE NIST_PEPTIDE_LIBRARY_COMPONENT "
				+ "SET SPECTRUM_HASH = ? WHERE MRC2_LIB_ID = ?";
		PreparedStatement updPs2 = conn.prepareStatement(updQuery2);
		
		Map<Double,Double>mzValues = new TreeMap<Double,Double>();	
		Collection<MsPoint>msms = new ArrayList<MsPoint>();
		for(String libName : libNames) {
			
			System.out.println("\nProcessing " + libName);
			ps.setString(1, libName);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				
				String mrc2id = rs.getString("MRC2_LIB_ID");
				msPs.setString(1, mrc2id);
				msPs.setDouble(2, rs.getDouble("PRECURSOR_MZ"));
				ResultSet msrs = msPs.executeQuery();
				
				mzValues.clear();
				msms.clear();
				
				while(msrs.next())
					mzValues.put(msrs.getDouble("INTENSITY"), msrs.getDouble("MZ"));
				
				msrs.close();
					
				if(mzValues.size() == 1) {
					
					//	Recalculate parent intensity to 1% of base peak 
					updParentIntensityPs.setString(1, mrc2id);
					updParentIntensityPs.executeUpdate();
					
					//	Update spectrum hash
					msmsPs.setString(1, mrc2id);
					ResultSet msmsRes = msmsPs.executeQuery();
					while(msmsRes.next()) {
						msms.add(new MsPoint(
										msmsRes.getDouble("MZ"), 
										msmsRes.getDouble("INTENSITY")));
					}					
					msmsRes.close();
					
					String hash = MsUtils.calculateSpectrumHash(msms);					
					updPs2.setString(1, hash);
					updPs2.setString(2, mrc2id);
					updPs2.executeUpdate();
				}
				if(mzValues.size() == 2) {
					
					delPs.setString(1, mrc2id);
					delPs.executeUpdate();
					for(Entry<Double, Double> im : mzValues.entrySet()) {
						
						if(im.getKey() < 1000) {
							
							updPs.setString(1, mrc2id);
							updPs.setDouble(2,  im.getValue());
							updPs.executeUpdate();
							break;
						}
					}					
				}
			}
			rs.close();
		}
		ps.close();
		msPs.close();
		delPs.close();
		msmsPs.close();
		updParentIntensityPs.close();
		updPs.close();
		updPs2.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void parseAndUploadARUSmspFiles(File libraryFolder2) {

		Collection<NISTARUStandemMassSpectrum>forUpload = 
				new ArrayList<NISTARUStandemMassSpectrum>();
		List<String> chunk = new ArrayList<String>();

		int count = 0;
		LineIterator it = null;
		
		File[] mspFiles = 
				libraryFolder2.listFiles((dir, name) -> name.toLowerCase().endsWith(".msp"));
		for(File msp : mspFiles){
			
			System.out.println("\nProcessing file " + msp.getName());
			forUpload.clear();
			try {
				it = FileUtils.lineIterator(msp, "UTF-8");
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
								
								NISTARUStandemMassSpectrum pepMsms = parseNistMspDataSource(chunk);
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

						NISTARUStandemMassSpectrum pepMsms = parseNistMspDataSource(chunk);
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
	}

	private static void uploadLibraryData(Collection<NISTARUStandemMassSpectrum> forUpload)  throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO NIST_PEPTIDE_LIBRARY_COMPONENT("
				+ "MRC2_LIB_ID, POLARITY, IONIZATION, PRECURSOR_MZ, INSTRUMENT_TYPE, "
				+ "SPECTRUM_TYPE, RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, "
				+ "LIBRARY_NAME, SPECTRUM_HASH, ORGANISM, NAME, PEPTIDE_SEQUENCE, "
				+ "COMMENTS, MODIFICATIONS, SAMPLE_TYPE) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
		for(NISTARUStandemMassSpectrum msms : forUpload) {
			
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
			ps.setNull(6, java.sql.Types.NULL);
			ps.setString(7, resolution);	//	RESOLUTION
			ps.setString(8, spectrumSource);	//	SPECTRUM_SOURCE
			ps.setString(9, ionizationType);	//	IONIZATION_TYPE
			ps.setString(10, libraryName);	//	LIBRARY_NAME
			
			String specHash = MsUtils.calculateSpectrumHash(msms.getSpectrum());
			ps.setString(11, specHash);	//	SPECTRUM_HASH
			ps.setString(12, organism);	//	ORGANISM
			ps.setString(13, msms.getProperty(MSPField.NAME));	//	NAME
			ps.setString(14, msms.getPeptideSequence());	//	PEPTIDE_SEQUENCE
			ps.setString(15, msms.getProperty(MSPField.COMMENTS));	//	COMMENTS
			ps.setNull(16, java.sql.Types.NULL);
			ps.setString(17, sampleType);	//	SAMPLE_TYPE
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
			for(Entry<MSPField, String> property : msms.getProperties().entrySet()) {
				
				propertiesPs.setString(2, property.getKey().name());
				propertiesPs.setString(3, property.getValue());
				propertiesPs.addBatch();
			}
			for(Entry<NISTARUSmspCommentField, String>cProp : msms.getPropertiesFromComments().entrySet()) {
				
				propertiesPs.setString(2, cProp.getKey().name());
				propertiesPs.setString(3, cProp.getValue());
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
		// TODO Auto-generated method stub
		
	}

	/**
	 * Parse single NIST MSP record converted to list of strings per line
	 * Try to determine parent ion from the MSP fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @return
	 */
	public static NISTARUStandemMassSpectrum parseNistMspDataSource(List<String>sourceText){

		int spectrumStart = -1;
		int pnum = 0;
		String precursorString = "";
		Matcher regexMatcher = null;
		String[] record = sourceText.toArray(new String[sourceText.size()]);
		spectrumStart = -1;

		//	Find polarity		
		Polarity polarity = null;
		for(int i=0; i<record.length; i++) {
			regexMatcher = polarityPattern.matcher(record[i]);
			if (regexMatcher.find()) {

				polarity = Polarity.getPolarityByCode(regexMatcher.group(1));
				break;
			}
		}
		NISTARUStandemMassSpectrum arusEntry = new NISTARUStandemMassSpectrum(polarity);

		//	Add all non-ms data
		for(int i=0; i<record.length; i++) {

			regexMatcher = patternMap.get(MSPField.NUM_PEAKS).matcher(record[i]);
			if (regexMatcher.find()) {

				pnum = Integer.parseInt(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
			else {
				for (Entry<MSPField, Pattern> entry : patternMap.entrySet()) {

					regexMatcher = entry.getValue().matcher(record[i]);

					if(regexMatcher.find()) {

						if(entry.getKey().equals(MSPField.EXACT_MASS)) {
							arusEntry.setExactMass(Double.parseDouble(regexMatcher.group(1)));
						}
						else if(entry.getKey().equals(MSPField.SYNONYM)){
							arusEntry.addSynonym(regexMatcher.group(1));
						}
						else if(entry.getKey().equals(MSPField.COMMENTS)) {	
							String comments = regexMatcher.group(1);
							arusEntry.addProperty(MSPField.COMMENTS, comments);
							parseNISTARUSComments(arusEntry);
						}
						else if(entry.getKey().equals(MSPField.PRECURSORMZ)) {
							precursorString = regexMatcher.group(1);
						}
						else {
							arusEntry.addProperty(entry.getKey(), regexMatcher.group(1));
						}
					}
				}
			}
		}
		if (pnum > 0) {
			
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for(int i=spectrumStart; i<record.length; i++) {

				regexMatcher = msmsPattern.matcher(record[i]);
				if(regexMatcher.find()) {

					MsPoint dp = new MsPoint(
							Double.parseDouble(regexMatcher.group(1)), 
							Double.parseDouble(regexMatcher.group(2)));

					if(regexMatcher.groupCount() == 3 && regexMatcher.group(3) != null)
						dp.setAdductType(regexMatcher.group(3).replaceAll("^\"|\"$", ""));
					
					dataPoints.add(dp);
				}
			}
			arusEntry.setSpectrum(dataPoints);
			if (!precursorString.isEmpty()) {

				String[] precs = StringUtils.split(precursorString, ',');
				for(String prec : precs) {

					MsPoint precursor = new MsPoint(Double.parseDouble(prec), 1000.0d);
					if(!arusEntry.getProperties().get(MSPField.PRECURSOR_TYPE).isEmpty())
						precursor.setAdductType(arusEntry.getProperties().get(MSPField.PRECURSOR_TYPE));

					arusEntry.addPrecursor(precursor);
				}
			}
		}
		else {
			throw new IllegalArgumentException("No peak data!");
		}
		return arusEntry;
	}

	private static void parseNISTARUSComments(NISTARUStandemMassSpectrum arusEntry) {

		String comments = arusEntry.getProperty(MSPField.COMMENTS);
		if(comments == null || comments.isEmpty())
			return;
		
		comments+= " ";
		for(Entry<NISTARUSmspCommentField, Pattern> entry : commentsPatternMap.entrySet()) {
			
			Matcher regexMatcher = 
					commentsPatternMap.get(entry.getKey()).matcher(comments);
			if (regexMatcher.find()) {
				
				String value = regexMatcher.group().
						replace(entry.getKey().getName() + "=", "").replaceAll("\"", "").trim();
				arusEntry.addPropertyFromComments(entry.getKey(), value);
			}
		}
	}
}






































