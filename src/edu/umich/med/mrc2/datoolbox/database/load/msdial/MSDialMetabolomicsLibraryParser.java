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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSDialMetabolomicsLibraryParser {

	private final static Pattern searchPattern = Pattern.compile("^(\\d+\\.\\d+)\\t(\\d+)");
	private static String msModeString;
	private static String fieldSuffix = ": ";
	private static int count = 1;
	private static MSDialMSMSRecord newRecord = new MSDialMSMSRecord();

	public static Collection<MSDialMSMSRecord>  processMSDialRecords(File inputFile, Polarity polarity) throws IOException {

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
						String pmzString = line.replace(LipidBlastRikenFields.PRECURSOR_MZ.getName() + fieldSuffix, "").trim();
						if(!pmzString.isEmpty()) {
							double pmz = Double.parseDouble(pmzString);
							newRecord.setPrecursorMz(pmz);
						}
					}
					if(line.startsWith(LipidBlastRikenFields.IONMODE.getName())) 				
						newRecord.setPolarity(polarity);
					
					if(line.startsWith(LipidBlastRikenFields.PRECURSOR_TYPE.getName())) { 
						String adduct = line.replace(LipidBlastRikenFields.PRECURSOR_TYPE.getName() + fieldSuffix, "").trim();
						if(!adduct.isEmpty())
							newRecord.setAdduct(adduct);
					}
					if(line.startsWith(LipidBlastRikenFields.SMILES.getName())) {
						String smiles = line.replace(LipidBlastRikenFields.SMILES.getName() + fieldSuffix, "").trim();
						if(!smiles.isEmpty())
							newRecord.setSmiles(smiles);
					}
					if(line.startsWith(LipidBlastRikenFields.INCHI_KEY.getName())) 	{
						String inchikey = line.replace(LipidBlastRikenFields.INCHI_KEY.getName() + fieldSuffix, "").trim();
						if(inchikey.length() == 27)
							newRecord.setInchiKey(inchikey);
					}
					if(line.startsWith(LipidBlastRikenFields.FORMULA.getName())) {	
						String formula = line.replace(LipidBlastRikenFields.FORMULA.getName() + fieldSuffix, "").trim();
						if(!formula.isEmpty()) {
							newRecord.setFormula(formula);							
							double exactMass = MolFormulaUtils.calculateExactMonoisotopicMass(formula);
							newRecord.setExactMass(exactMass);							
							Integer carbonNumber = MolFormulaUtils.getCarbonCounts(formula);
							newRecord.setCarbonNumber(carbonNumber);
						}
					}
					if(line.startsWith(LipidBlastRikenFields.COMPOUND_CLASS.getName())) {	
						String compoundClass = line.replace(LipidBlastRikenFields.COMPOUND_CLASS.getName() + fieldSuffix, "").trim();
						if(!compoundClass.isEmpty())
							newRecord.setAbbreviation(compoundClass);
					}
					if(line.startsWith(LipidBlastRikenFields.COMMENT.getName())) {	
						String comment = line.replace(LipidBlastRikenFields.COMMENT.getName() + fieldSuffix, "").trim();
						if(!comment.isEmpty())
							newRecord.setComment(comment);
					}
					if(line.startsWith(LipidBlastRikenFields.RETENTION_TIME.getName())) {
						String rtString = line.replace(LipidBlastRikenFields.RETENTION_TIME.getName() + fieldSuffix, "").trim();
						if(!rtString.isEmpty()) {
							double rt = Double.parseDouble(rtString);			
							newRecord.setRt(rt);
						}
					}					
					if(line.startsWith(LipidBlastRikenFields.CCS.getName())) {
						String ccsString = line.replace(LipidBlastRikenFields.CCS.getName() + fieldSuffix, "").trim();
						if(!ccsString.isEmpty()) {
							double ccs = Double.parseDouble(ccsString);
							newRecord.setCcs(ccs);
						}
					}
					if(line.startsWith(LipidBlastRikenFields.COLLISION_ENERGY.getName())) {
						String ceString = line.replace(LipidBlastRikenFields.COLLISION_ENERGY.getName() + fieldSuffix, "").trim();
						if(!ceString.isEmpty()) 
							newRecord.setCollisionEnergy(ceString);						
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

		System.out.println("\nUploading MSMS data ");	
		count = 1;
		
		String id = null;
		Connection conn = ConnectionManager.getConnection();
		
//		String idQuery = "SELECT 'MSDM' || LPAD(MSDIAL_METABOLITE_SEQ.NEXTVAL, 8, '0') FROM DUAL";
//		PreparedStatement idps = conn.prepareStatement(idQuery);
		
		String dataQuery =
				"INSERT INTO MSDIAL_METABOLITE_COMPONENTS " +
				"(MSDM_ID, NAME, SMILES, INCHI_KEY, FORMULA,  " +
				"PRECURSOR_MZ, PRECURSOR_TYPE, IONMODE,  " +
				"RETENTION_TIME, CCS, COMMENTS, COLLISION_ENERGY, EXACT_MASS, NUM_CARBONS)  " +
				"VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		
		String peaksQuery =
				"INSERT INTO MSDIAL_METABOLITE_PEAKS (MSDM_ID, MZ, INTENSITY) " +
				"VALUES(?, ?, ?)";
		PreparedStatement peaksps = conn.prepareStatement(peaksQuery);
		
		for(MSDialMSMSRecord record : recordList) {
			
//			ResultSet rs = idps.executeQuery();
//			while(rs.next())
//				id = rs.getString(1);
			id = SQLUtils.getNextIdFromSequence(conn, 
					"MSDIAL_METABOLITE_SEQ",
					DataPrefix.MSDIAL_METABOLITE,
					"0",
					8);			
			ps.setString(1, id);
			ps.setString(2, record.getName());
			ps.setString(3, record.getSmiles());
			ps.setString(4, record.getInchiKey());
			ps.setString(5, record.getFormula());		
			if(record.getPrecursorMz() != null)
				ps.setDouble(6, record.getPrecursorMz());
			else
				ps.setNull(6, java.sql.Types.NULL);
						
			ps.setString(7, record.getAdduct());
			ps.setString(8, record.getPolarity().getCode());		
			if(record.getRt() != null)
				ps.setDouble(9, record.getRt());
			else
				ps.setNull(9, java.sql.Types.NULL);
						
			if(record.getCcs() != null)
				ps.setDouble(10, record.getCcs());
			else
				ps.setNull(10, java.sql.Types.NULL);
			
			ps.setString(11, record.getComment());
			ps.setString(12, record.getCollisionEnergy());
			if(record.getExactMass() != null)
				ps.setDouble(13, record.getExactMass());
			else
				ps.setNull(13, java.sql.Types.NULL);
						
			if(record.getCarbonNumber() != null)
				ps.setInt(14, record.getCarbonNumber());	
			else
				ps.setNull(14, java.sql.Types.NULL);
			
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

//	MSMSLIB0035	mmet_neg
//	MSMSLIB0036	mdmet_pos
	
	public static void copyLibraryComponentsToMainLibraryTables(
			String libId, 
			String libraryName,
			String polarity) throws Exception {

		Connection conn = ConnectionManager.getConnection();
		
		//	Get MSDIAL metabolomics component data
		String inputQuery =
				"SELECT MSDM_ID, PRECURSOR_MZ, PRECURSOR_TYPE, ACCESSION, COLLISION_ENERGY, COMMENTS "
				+ "FROM MSDIAL_METABOLITE_COMPONENTS "
				+ "WHERE IONMODE = ? AND ACCESSION IS NOT NULL ORDER BY 1";
		PreparedStatement inputPs = conn.prepareStatement(inputQuery);
		inputPs.setString(1, polarity);
		
		String inputMsQuery = 
				"SELECT MZ, INTENSITY FROM MSDIAL_METABOLITE_PEAKS "
				+ "WHERE MSDM_ID = ?";
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
			+ "MAX_DIGITS, ENTROPY, COLLISION_ENERGY) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
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
		
		//	Annotations
		String propQuery = "INSERT INTO REF_MSMS_PROPERTIES(MRC2_LIB_ID, "
				+ "PROPERTY_NAME, PROPERTY_VALUE) VALUES (?, ?, ?)";
		PreparedStatement propPs = conn.prepareStatement(propQuery);
		propPs.setString(2, "Annotation");

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
			inputMsPs.setString(1, inputRs.getString("MSDM_ID"));
			ResultSet msRs = inputMsPs.executeQuery();
			while(msRs.next())
				msms.add(new MsPoint(msRs.getDouble("MZ"), msRs.getDouble("INTENSITY")));			
			
			msRs.close();
			
			int maxDigits = msms.stream().mapToInt(p -> getNumberOfDecimalPlaces(p.getMz())).max().getAsInt();
			
			//	Component
			double precursorMz = inputRs.getDouble("PRECURSOR_MZ");
			cps.setDouble(4, precursorMz);
			cps.setString(5, inputRs.getString("PRECURSOR_TYPE"));
			cps.setString(11, inputRs.getString("MSDM_ID"));
			cps.setString(12, inputRs.getString("ACCESSION"));
			cps.setString(13, MsUtils.calculateSpectrumHash(msms));
			cps.setInt(14, maxDigits);
			
			Double enthropy = MsUtils.calculateSpectrumEntropy(msms);
			if(enthropy.equals(Double.NaN))
				enthropy  = 0.0d;
			
			cps.setDouble(15, enthropy);
			cps.setString(16, inputRs.getString("COLLISION_ENERGY"));
			cps.addBatch();
//			cps.executeUpdate();
			
			//	Properties
			String comments = inputRs.getString("COMMENTS");
			if(comments != null && !comments.isEmpty()) {
				String[]parts = comments.split(";");
				propPs.setString(1, libId);
				for(int i=0; i<parts.length; i++) {
					propPs.setString(3, parts[i].trim());
					propPs.addBatch();
				}
			}
					
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
//			specPs.executeBatch();
			processed++;
			if(processed % 1000 == 0) {
				cps.executeBatch();
				specPs.executeBatch();
				propPs.executeBatch();
				System.out.println(Integer.toString(processed));
			}			
		}
		cps.executeBatch();
		specPs.executeBatch();
		propPs.executeBatch();
		System.out.println(Integer.toString(processed));
		inputRs.close();
//		idps.close();
		cps.close();
		propPs.close();
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


































