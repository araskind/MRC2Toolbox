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

package edu.umich.med.mrc2.datoolbox.database.load.msdial;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class LipidBlastRikenParser {

	private final static Pattern searchPattern = Pattern.compile("^(\\d+\\.\\d+)\\t(\\d+)");
	private static String msModeString;
	private static String fieldSuffix = ": ";
	private static int count = 1;
	private static MSDialMSMSRecord newRecord = new MSDialMSMSRecord();

	public static Collection<MSDialMSMSRecord>  processLipidBlastRecords(File inputFile, Polarity polarity) throws IOException {

		System.out.println("Parsing MSP data from " + inputFile.getName());	
		count = 1;
		Collection<MSDialMSMSRecord> recordList = new ArrayList<MSDialMSMSRecord>();	
		try (Stream<String> lines = Files.lines(Paths.get(inputFile.getAbsolutePath()), Charset.defaultCharset())) {

			lines.forEachOrdered(line -> {
				if(line.trim().isEmpty() && !newRecord.getSpectrum().isEmpty()) {
					recordList.add(new MSDialMSMSRecord(newRecord));
					count++;
					if(count % 50 == 0)
						System.out.print(".");
					if(count % 2000 == 0)
						System.out.println(".");					
				}
				else {
					if(line.startsWith(LipidBlastRikenFields.NAME.getName())) {
						newRecord = new MSDialMSMSRecord();					
						newRecord.setName(line.replace(LipidBlastRikenFields.NAME.getName() + fieldSuffix, "").trim());
					}
					if(line.startsWith(LipidBlastRikenFields.PRECURSOR_MZ.getName())) {
						double pmz = Double.parseDouble(line.replace(LipidBlastRikenFields.PRECURSOR_MZ.getName() + fieldSuffix, "").trim());
						newRecord.setPrecursorMz(pmz);
					}
					if(line.startsWith(LipidBlastRikenFields.IONMODE.getName())) 				
						newRecord.setPolarity(polarity);
					
					if(line.startsWith(LipidBlastRikenFields.PRECURSOR_TYPE.getName())) 				
						newRecord.setAdduct(line.replace(LipidBlastRikenFields.PRECURSOR_TYPE.getName() + fieldSuffix, "").trim());
					
					if(line.startsWith(LipidBlastRikenFields.SMILES.getName())) 				
						newRecord.setSmiles(line.replace(LipidBlastRikenFields.SMILES.getName() + fieldSuffix, "").trim());
					
					if(line.startsWith(LipidBlastRikenFields.INCHI_KEY.getName())) 	{
						String inchikey = line.replace(LipidBlastRikenFields.INCHI_KEY.getName() + fieldSuffix, "").trim();
						if(inchikey.length() == 27)
							newRecord.setInchiKey(inchikey);
					}
					if(line.startsWith(LipidBlastRikenFields.FORMULA.getName())) 				
						newRecord.setFormula(line.replace(LipidBlastRikenFields.FORMULA.getName() + fieldSuffix, "").trim());

					if(line.startsWith(LipidBlastRikenFields.COMPOUND_CLASS.getName())) 				
						newRecord.setAbbreviation(line.replace(LipidBlastRikenFields.COMPOUND_CLASS.getName() + fieldSuffix, "").trim());
					
					if(line.startsWith(LipidBlastRikenFields.COMMENT.getName())) 				
						newRecord.setComment(line.replace(LipidBlastRikenFields.COMMENT.getName() + fieldSuffix, "").trim());
					
					if(line.startsWith(LipidBlastRikenFields.RETENTION_TIME.getName())) {
						double rt = Double.parseDouble(line.replace(LipidBlastRikenFields.RETENTION_TIME.getName() + fieldSuffix, "").trim());			
						newRecord.setRt(rt);
					}
					
					if(line.startsWith(LipidBlastRikenFields.CCS.getName())) {
						double ccs = Double.parseDouble(line.replace(LipidBlastRikenFields.CCS.getName() + fieldSuffix, "").trim());
						newRecord.setCcs(ccs);
					}
			
					if(line.startsWith(LipidBlastRikenFields.NUM_PEAKS.getName())) {
						int np = Integer.parseInt(line.replace(LipidBlastRikenFields.NUM_PEAKS.getName() + fieldSuffix, "").trim());
						newRecord.setNumPeaks(np);
					}
					if(!line.trim().isEmpty() && Character.isDigit(line.charAt(0))) {

						Matcher regexMatcher = searchPattern.matcher(line.trim());
						if(regexMatcher.find()) {

							double mz = Double.parseDouble(regexMatcher.group(1));
							double intensity = Double.parseDouble(regexMatcher.group(2));
							MsPoint p = new MsPoint(mz, intensity);
							newRecord.getSpectrum().add(p);
						}
					}
				}
			});
		}
		return recordList;
	}
	
	public static void insertRecords(Collection<MSDialMSMSRecord> recordList) throws Exception {

		System.out.println("Uploading MSMS data ");	
		count = 1;
		
		String id = null;
		Connection conn = ConnectionManager.getConnection();
		
//		String idQuery = "SELECT 'LBR' || LPAD(LIPID_BLAST_RIKEN_SEQ.NEXTVAL, 9, '0') FROM DUAL";
//		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"INSERT INTO LIPIDBLAST_RIKEN_COMPONENTS " +
				"(RLB_ID, NAME, SMILES, INCHI_KEY, FORMULA,  " +
				"PRECURSOR_MZ, PRECURSOR_TYPE, IONMODE,  " +
				"RETENTION_TIME, CCS, COMMENTS, COMPOUNDCLASS)  " +
				"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		String peaksQuery =
				"INSERT INTO LIPIDBLAST_RIKEN_PEAKS (RLB_ID, MZ, INTENSITY) " +
				"VALUES(?, ?, ?)";
		PreparedStatement peaksps = conn.prepareStatement(peaksQuery);
		
		for(MSDialMSMSRecord record : recordList) {
			
//			ResultSet rs = idps.executeQuery();
//			while(rs.next())
//				id = rs.getString(1);
			
			id = SQLUtils.getNextIdFromSequence(conn, 
					"LIPID_BLAST_RIKEN_SEQ",
					DataPrefix.LIPID_BLAST_RIKEN,
					"0",
					9);		
			ps.setString(1, id);
			ps.setString(2, record.getName());
			ps.setString(3, record.getSmiles());
			ps.setString(4, record.getInchiKey());
			ps.setString(5, record.getFormula());
			ps.setDouble(6, record.getPrecursorMz());
			ps.setString(7, record.getAdduct());
			ps.setString(8, record.getPolarity().getCode());		
			ps.setDouble(9, record.getRt());
			ps.setDouble(10, record.getCcs());
			ps.setString(11, record.getComment());
			ps.setString(12, record.getAbbreviation());	
			
			ps.executeUpdate();
			
			//	MSMS
			peaksps.setString(1, id);
			for(MsPoint p : record.getSpectrum()) {

				peaksps.setDouble(2, p.getMz());
				peaksps.setDouble(3, p.getIntensity());
				peaksps.addBatch();
			}
			peaksps.executeBatch();
			
			count++;
			if(count % 50 == 0)
				System.out.print(".");
			if(count % 2000 == 0)
				System.out.println(".");
		}
//		idps.close();
		peaksps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void parseNumCarbonsAndDoubleBonds() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		ArrayList<String>unprocessed = new ArrayList<String>();
		
		String idQuery = 
				"SELECT RLB_ID, NAME FROM LIPIDBLAST_RIKEN_COMPONENTS "
				+ "WHERE NAME NOT LIKE '%;%' AND NAME NOT LIKE '%(%' "
				+ "AND NAME NOT LIKE '% O-%'";
		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"UPDATE LIPIDBLAST_RIKEN_COMPONENTS " +
				"SET NUM_CARBONS = ?, NUM_DOUBLE_BONDS = ? WHERE RLB_ID = ?";
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		ResultSet rs = idps.executeQuery();
		while(rs.next()) {
			
			String name = rs.getString("NAME");
			String[]parts = name.split(" ");
			if(parts.length == 1) {
				unprocessed.add(name);
				continue;
			}
			int numCarbons = 0;
			int numDoubleBonds = 0;
			if(parts.length > 1) {
				String[]chains = parts[1].split("_");
				for(String chain : chains) {
					
					String[]cdb = chain.split(":");
					int cc = 0;
					try {
						cc = Integer.parseInt(cdb[0]);
						numCarbons += cc;
					} catch (NumberFormatException e) {
						unprocessed.add(name);
						continue;
					}
					if(cdb.length == 2) {
						int db = 0;
						try {
							db = Integer.parseInt(cdb[1]);
							numDoubleBonds += db;
						} catch (NumberFormatException e) {
							unprocessed.add(name);
							continue;
						}
					}
				}
				ps.setInt(1, numCarbons);
				ps.setInt(2, numDoubleBonds);
				ps.setString(3, rs.getString("RLB_ID"));
				ps.executeUpdate();
			}					
		}
		rs.close();
		idps.close();
		ps.close();
		Path logPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\MSDIAL\\Lipidomics-V68\\Naming", 
				"FirstAbbreviationPath.txt");
		
		List<String> unprocessedIds = unprocessed.stream().distinct().
				sorted().collect(Collectors.toList());
	    try {
			Files.write(logPath, 
					unprocessedIds,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void parseNumCarbonsAndDoubleBondsEther() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		ArrayList<String>unprocessed = new ArrayList<String>();
		
		String idQuery = "SELECT RLB_ID, NAME FROM LIPIDBLAST_RIKEN_COMPONENTS "
				+ "WHERE NAME NOT LIKE '%;%' AND NAME NOT LIKE '%(%' "
				+ "AND NUM_CARBONS IS NULL AND NAME LIKE '% O-%'";
		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"UPDATE LIPIDBLAST_RIKEN_COMPONENTS " +
				"SET NUM_CARBONS = ?, NUM_DOUBLE_BONDS = ? WHERE RLB_ID = ?";
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		ResultSet rs = idps.executeQuery();
		while(rs.next()) {
			
			String name = rs.getString("NAME");
			String[]parts = name.split("-");
			if(parts.length == 1) {
				unprocessed.add(name);
				continue;
			}
			int numCarbons = 0;
			int numDoubleBonds = 0;
			if(parts.length > 1) {
				String[]chains = parts[1].split("_");
				for(String chain : chains) {
					
					String[]cdb = chain.split(":");
					int cc = 0;
					try {
						cc = Integer.parseInt(cdb[0]);
						numCarbons += cc;
					} catch (NumberFormatException e) {
						unprocessed.add(name);
						continue;
					}
					if(cdb.length == 2) {
						int db = 0;
						try {
							db = Integer.parseInt(cdb[1]);
							numDoubleBonds += db;
						} catch (NumberFormatException e) {
							unprocessed.add(name);
							continue;
						}
					}
				}
				if(numCarbons > 0) {
					ps.setInt(1, numCarbons);
					ps.setInt(2, numDoubleBonds);
					ps.setString(3, rs.getString("RLB_ID"));
					ps.executeUpdate();
				}
			}					
		}
		rs.close();
		idps.close();
		ps.close();
		Path logPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\MSDIAL\\Lipidomics-V68\\Naming", 
				"EtherAbbreviationPath.txt");
		
		List<String> unprocessedIds = unprocessed.stream().distinct().
				sorted().collect(Collectors.toList());
	    try {
			Files.write(logPath, 
					unprocessedIds,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void parseNumCarbonsAndDoubleBondsLpc() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String idQuery = "SELECT RLB_ID, NAME FROM LIPIDBLAST_RIKEN_COMPONENTS "
				+ "WHERE NAME NOT LIKE '%;%' AND NAME NOT LIKE '%(%' "
				+ "AND NUM_CARBONS IS NULL AND NAME LIKE 'LPC%'";
		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"UPDATE LIPIDBLAST_RIKEN_COMPONENTS " +
				"SET NUM_CARBONS = ?, NUM_DOUBLE_BONDS = ? WHERE RLB_ID = ?";
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		ResultSet rs = idps.executeQuery();
		while(rs.next()) {
			
			String name = rs.getString("NAME").replace("LPC ", "");
			String[]parts = name.split("-");
			if(parts.length == 1) {
				continue;
			}
			int numCarbons = 0;
			int numDoubleBonds = 0;
			if(parts.length > 1) {
					
				String[]cdb = parts[0].split(":");
				int cc = 0;
				try {
					cc = Integer.parseInt(cdb[0]);
					numCarbons += cc;
				} catch (NumberFormatException e) {
					continue;
				}
				if(cdb.length == 2) {
					int db = 0;
					try {
						db = Integer.parseInt(cdb[1]);
						numDoubleBonds += db;
					} catch (NumberFormatException e) {
						continue;
					}
				}			
				if(numCarbons > 0) {
					ps.setInt(1, numCarbons);
					ps.setInt(2, numDoubleBonds);
					ps.setString(3, rs.getString("RLB_ID"));
					ps.executeUpdate();
				}
			}					
		}
		rs.close();
		idps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void parseNumCarbonsAndDoubleBondsGroups() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();		
		String idQuery = "SELECT RLB_ID, NAME FROM LIPIDBLAST_RIKEN_COMPONENTS "
				+ "WHERE NUM_CARBONS IS NULL";
		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"UPDATE LIPIDBLAST_RIKEN_COMPONENTS " +
				"SET NUM_CARBONS = ?, NUM_DOUBLE_BONDS = ? WHERE RLB_ID = ?";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		ArrayList<String>unprocessed = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\d+:\\d+");
		Matcher matcher = null;

		ResultSet rs = idps.executeQuery();
		while(rs.next()) {
			
			String name = rs.getString("NAME");
			matcher = pattern.matcher(name);
			int numCarbons = 0;
			int numDoubleBonds = 0;
			while(matcher.find()) {

				String[]cdb = matcher.group(0).split(":");
				numCarbons += Integer.parseInt(cdb[0]);
				numDoubleBonds += Integer.parseInt(cdb[1]);			
			}
			if(numCarbons > 0) {
				ps.setInt(1, numCarbons);
				ps.setInt(2, numDoubleBonds);
				ps.setString(3, rs.getString("RLB_ID"));
				ps.executeUpdate();	
			}
			else {
				unprocessed.add(name);
			}
		}
		rs.close();
		idps.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		Path logPath = Paths.get(
				"E:\\DataAnalysis\\Databases\\MSDIAL\\Lipidomics-V68\\Naming", 
				"GroupsAbbreviationPath.txt");		
		List<String> unprocessedIds = unprocessed.stream().distinct().
				sorted().collect(Collectors.toList());
	    try {
			Files.write(logPath, 
					unprocessedIds,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	MSMSLIB0033	MDLB-NEG	mdlb_neg
//	MSMSLIB0034	MDLB-POS	mdlb_pos
	
	public static void copyLibraryComponentsToMainLibraryTables(
			String libId, 
			String libraryName,
			String polarity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		
		//	Get MSDIAL lipidomics component data
		String inputQuery =
				"SELECT RLB_ID, PRECURSOR_MZ, PRECURSOR_TYPE, ACCESSION "
				+ "FROM LIPIDBLAST_RIKEN_COMPONENTS "
				+ "WHERE IONMODE = ? ORDER BY 1";
		PreparedStatement inputPs = conn.prepareStatement(inputQuery);
		inputPs.setString(1, polarity);
		
		String inputMsQuery = 
				"SELECT MZ, INTENSITY FROM LIPIDBLAST_RIKEN_PEAKS "
				+ "WHERE RLB_ID = ?";
		PreparedStatement inputMsPs = conn.prepareStatement(inputMsQuery);
		
		//	ID
//		String idQuery = "SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() +
//			"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MRC2ID FROM DUAL";
//		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		//	Component data
		String cQuery =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( "
			+ "MRC2_LIB_ID, POLARITY, IONIZATION, PRECURSOR_MZ, ADDUCT, "
			+ "SPECTRUM_TYPE, RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, "
			+ "LIBRARY_NAME, ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, "
			+ "MAX_DIGITS, ENTROPY) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement cps = conn.prepareStatement(cQuery);
		
		cps.setString(2, polarity);
		cps.setString(3, "ESI");
		cps.setString(6, "MS2");
		cps.setString(7, "HIGH");
		cps.setString(8, SpectrumSource.EXPERIMENTAL.name());
		cps.setString(9, "ESI");
		cps.setString(10, libraryName);		
		
		// 	Spectrum peaks
		String specQuery =
			"INSERT INTO REF_MSMS_LIBRARY_PEAK ("
			+ "MRC2_LIB_ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES(?, ?, ?, ?) ";
		PreparedStatement specPs = conn.prepareStatement(specQuery);

		ResultSet inputRs = inputPs.executeQuery();	
		int processed = 0;
		while(inputRs.next()) {
				
			//	Get next feature ID
//			ResultSet idrs = idps.executeQuery();
//			while(idrs.next())
//				libId = idrs.getString("MRC2ID");
//			
//			idrs.close();
			libId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);			
			cps.setString(1, libId);
			
			//	Input MSMS
			Collection<MsPoint>msms = new ArrayList<MsPoint>();
			inputMsPs.setString(1, inputRs.getString("RLB_ID"));
			ResultSet msRs = inputMsPs.executeQuery();
			while(msRs.next())
				msms.add(new MsPoint(msRs.getDouble("MZ"), msRs.getDouble("INTENSITY")));			
			
			msRs.close();
			
			int maxDigits = msms.stream().mapToInt(p -> getNumberOfDecimalPlaces(p.getMz())).max().getAsInt();
			
			//	Component
			double precursorMz = inputRs.getDouble("PRECURSOR_MZ");
			cps.setDouble(4, precursorMz);
			cps.setString(5, inputRs.getString("PRECURSOR_TYPE"));
			cps.setString(11, inputRs.getString("RLB_ID"));
			cps.setString(12, inputRs.getString("ACCESSION"));
			cps.setString(13, MsUtils.calculateSpectrumHash(msms));
			cps.setInt(14, maxDigits);
			
			Double enthropy = MsUtils.calculateSpectrumEntropy(msms);
			if(enthropy.equals(Double.NaN))
				enthropy  = 0.0d;
			
			cps.setDouble(15, enthropy);			
//			cps.addBatch();
			cps.executeUpdate();
			
			//	Spectrum
			specPs.setString(1, libId);
			double precursorMzRounded = round(precursorMz, 4);
			for(MsPoint p : msms) {

				specPs.setDouble(2, p.getMz());
				specPs.setDouble(3, p.getIntensity());
				specPs.setString(4, null);
				if(p.getMz() == precursorMzRounded) 
					specPs.setString(4, "Y");
		
				specPs.addBatch();
			}
			specPs.executeBatch();
			processed++;
			if(processed % 1000 == 0) {
//				cps.executeBatch();
//				specPs.executeBatch();
				System.out.println(Integer.toString(processed));
			}			
		}
//		cps.executeBatch();
//		specPs.executeBatch();
//		System.out.println(Integer.toString(processed));
		inputRs.close();
//		idps.close();
		cps.close();
		specPs.close();
		inputPs.close();
		inputMsPs.close();
		
		ConnectionManager.releaseConnection(conn);
	}
	
	private static double round(double value, int places) {
		
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	private static int getNumberOfDecimalPlaces(double value) {
		BigDecimal bd = new BigDecimal(Double.toString(value));
	    return Math.max(0, bd.stripTrailingZeros().scale());
	}
}


































