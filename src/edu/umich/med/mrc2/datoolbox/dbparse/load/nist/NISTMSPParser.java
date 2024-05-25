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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import edu.umich.med.mrc2.datoolbox.utils.TextUtils;

public class NISTMSPParser {
	
	/**
	 * Parse single NIST MSP record converted to list of strings per line
	 * Try to determine parent ion from the MSP fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @return
	 */
	public static NISTTandemMassSpectrum parseNistMspDataSource(List<String>sourceText){


		int spectrumStart = -1;
		int pnum = 0;
		double precursorMz = -1.0;
		String precursorString = "";
		Matcher regexMatcher = null;
		Map<MSPField,Pattern>patternMap = new TreeMap<MSPField,Pattern>();
		for(MSPField field : MSPField.values())
			patternMap.put(field, Pattern.compile("(?i)^" + field.getName() + ":?\\s+(.+)"));

		Matcher casNistMatcher = null;
		Pattern casNistPattern = Pattern.compile("^CAS#:\\s+([^;]+);\\s+NIST#:\\s+(.+)");
		String[] record = sourceText.toArray(new String[sourceText.size()]);
		spectrumStart = -1;

		//	Find polarity
		Pattern polarityPattern = Pattern.compile("(?i)^" + MSPField.ION_MODE.getName() + ":?\\s+([PN])");
		Polarity polarity = null;
		for(int i=0; i<record.length; i++) {
			regexMatcher = polarityPattern.matcher(record[i]);
			if (regexMatcher.find()) {

				polarity = Polarity.getPolarityByCode(regexMatcher.group(1));
				break;
			}
		}
		NISTTandemMassSpectrum msmsSet = new NISTTandemMassSpectrum(polarity);

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

						if(entry.getKey().equals(MSPField.DB_NUM)) {
							msmsSet.setDbnum(Integer.parseInt(regexMatcher.group(1)));
						}
						else if(entry.getKey().equals(MSPField.EXACT_MASS)) {
							msmsSet.setExactMass(Double.parseDouble(regexMatcher.group(1)));
						}
						else if(entry.getKey().equals(MSPField.NIST_NUM)) {
							msmsSet.setNistNum(Integer.parseInt(regexMatcher.group(1)));
						}
						//	When CAS is on one line with NIST #
						else if(entry.getKey().equals(MSPField.CAS)) {

							casNistMatcher = casNistPattern.matcher(record[i]);
							if(casNistMatcher.find()) {
								msmsSet.addProperty(MSPField.CAS, casNistMatcher.group(1));
								msmsSet.setNistNum(Integer.parseInt(casNistMatcher.group(2)));
							}
							else {
								msmsSet.addProperty(MSPField.CAS, regexMatcher.group(1));
							}
						}
						else if(entry.getKey().equals(MSPField.SYNONYM)){
							msmsSet.addSynonym(regexMatcher.group(1));
						}
						else if(entry.getKey().equals(MSPField.COMMENTS)) {						
							msmsSet.getNotes().add(regexMatcher.group(1));
						}
						else if(entry.getKey().equals(MSPField.NOTES)) {
							
							String[] notes = regexMatcher.group(1).split(";");
							for(String note : notes)
								msmsSet.getNotes().add(note.trim());
						}
						else if(entry.getKey().equals(MSPField.PRECURSORMZ)) {
							precursorString = regexMatcher.group(1);
						}
						else if(entry.getKey().equals(MSPField.PEPTIDE_MODS)) {
							msmsSet.setPeptideModifications(regexMatcher.group(1));
							msmsSet.addProperty(MSPField.PEPTIDE_MODS, msmsSet.getPeptideModifications());
						}
						else if(entry.getKey().equals(MSPField.PEPTIDE_SEQUENCE)) {
							msmsSet.setPeptideSequence(regexMatcher.group(1));
							msmsSet.addProperty(MSPField.PEPTIDE_SEQUENCE, msmsSet.getPeptideSequence());
						}
						else {
							msmsSet.addProperty(entry.getKey(), regexMatcher.group(1));
						}
					}
				}
			}
		}
		if (pnum > 0) {

			Pattern msmsPattern = Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.]+)\\s?(.+)?");
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for(int i=spectrumStart; i<record.length; i++) {

				regexMatcher = msmsPattern.matcher(record[i]);
				if(regexMatcher.find()) {

					MsPoint dp = new MsPoint(Double.parseDouble(regexMatcher.group(1)), Double.parseDouble(regexMatcher.group(2)));

					if(regexMatcher.groupCount() == 3) {

						if(regexMatcher.group(3) != null)
						dp.setAdductType(regexMatcher.group(3).replaceAll("^\"|\"$", ""));
					}

					dataPoints.add(dp);
				}
			}
			msmsSet.setSpectrum(dataPoints);
			if (!precursorString.isEmpty()) {

				String[] precs = StringUtils.split(precursorString, ',');
				for(String prec : precs) {

					MsPoint precursor = new MsPoint(Double.parseDouble(prec), 1000.0d);
					if(!msmsSet.getProperties().get(MSPField.PRECURSOR_TYPE).isEmpty())
						precursor.setAdductType(msmsSet.getProperties().get(MSPField.PRECURSOR_TYPE));

					msmsSet.addPrecursor(precursor);
				}
			}
		}
		else {
			throw new IllegalArgumentException("No peak data!");
		}
		return msmsSet;
	}

	public static void insertSpectrumRecord(
			NISTTandemMassSpectrum newRecord, 
			IAtomContainer molecule,
			Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO COMPOUNDDB.NIST_LIBRARY_COMPONENT " +
				"(NIST_ID, CAS_NUMBER, RELATED_CAS, NAME, ION_MODE, "
				+ "IONIZATION, EXACT_MASS, FORMULA, INCHI_KEY, " +
				"COLLISION_ENERGY, COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, "
				+ "IN_SOURCE_VOLTAGE, MSN_PATHWAY, PRESSURE, " +
				"SAMPLE_INLET, SPECIAL_FRAGMENTATION, SPECTRUM_TYPE,"
				+ "PEPTIDE_SEQUENCE, PEPTIDE_MODS) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		
		int nistId = newRecord.getNistNum();

		//	Component record
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		ps.setInt(1, nistId);
		ps.setString(2, newRecord.getProperties().get(MSPField.CAS));
		ps.setString(3, newRecord.getProperties().get(MSPField.RELATED_CAS));
		ps.setString(4, newRecord.getProperties().get(MSPField.NAME));
		ps.setString(5, newRecord.getProperties().get(MSPField.ION_MODE));
		ps.setString(6, newRecord.getProperties().get(MSPField.IONIZATION));
		ps.setDouble(7, newRecord.getExactMass());
		ps.setString(8, newRecord.getProperties().get(MSPField.FORMULA));
		ps.setString(9, newRecord.getProperties().get(MSPField.INCHI_KEY));
		ps.setString(10, newRecord.getProperties().get(MSPField.COLLISION_ENERGY));
		ps.setString(11, newRecord.getProperties().get(MSPField.COLLISION_GAS));
		ps.setString(12, newRecord.getProperties().get(MSPField.INSTRUMENT));
		ps.setString(13, newRecord.getProperties().get(MSPField.INSTRUMENT_TYPE));
		ps.setString(14, newRecord.getProperties().get(MSPField.IN_SOURCE_VOLTAGE));
		ps.setString(15, newRecord.getProperties().get(MSPField.MSN_PATHWAY));
		ps.setString(16, newRecord.getProperties().get(MSPField.PRESSURE));
		ps.setString(17, newRecord.getProperties().get(MSPField.SAMPLE_INLET));
		ps.setString(18, newRecord.getProperties().get(MSPField.SPECIAL_FRAGMENTATION));
		ps.setString(19, newRecord.getProperties().get(MSPField.SPECTRUM_TYPE));
		ps.setString(20, newRecord.getProperties().get(MSPField.PEPTIDE_SEQUENCE));
		ps.setString(21, newRecord.getProperties().get(MSPField.PEPTIDE_MODS));

		ps.executeUpdate();
		ps.close();

		//	Synonyms
		dataQuery =
			"INSERT INTO COMPOUNDDB.NIST_SYNONYM (NIST_ID, CPD_NAME, STYPE) VALUES(?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps.setInt(1, nistId);
		ps.setString(2, newRecord.getProperties().get(MSPField.NAME));
		ps.setString(3, "PRI");
		ps.executeUpdate();

		for(String synonym : newRecord.getSynonyms()) {
			ps.setString(2, synonym);
			ps.setString(3, "SYN");
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();

		//	Annotations
		dataQuery = "INSERT INTO COMPOUNDDB.NIST_LIBRARY_ANNOTATION (NIST_ID, NOTE_TEXT) VALUES(?, ?)";
		ps = conn.prepareStatement(dataQuery); 
		ps.setInt(1, nistId);
		for(String annot : newRecord.getNotes()) { 
			ps.setString(2, annot); 
			ps.addBatch(); 
		} 
		ps.executeBatch();
		ps.close();

		//	MSMS spectrum
		dataQuery =
			"INSERT INTO COMPOUNDDB.NIST_LIBRARY_PEAK " +
			"(NIST_ID, MZ, INTENSITY, ADDUCT, IS_PARENT) " +
			"VALUES(?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps = conn.prepareStatement(dataQuery);
		ps.setInt(1, nistId);

		//	Parent ion
		for(MsPoint parent : newRecord.getPrecursors()) {

			ps.setDouble(2, parent.getMz());
			ps.setDouble(3, parent.getIntensity());
			ps.setString(4, parent.getAdductType());
			ps.setString(5, "Y");
			ps.addBatch();
		}
		//	Spectrum
		for(MsPoint p : newRecord.getSpectrum()) {

			ps.setDouble(2, p.getMz());
			ps.setDouble(3, p.getIntensity());
			ps.setString(4, p.getAdductType());
			ps.setNull(5, java.sql.Types.NULL);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		
		//	Compound data
		dataQuery = 
				"INSERT INTO COMPOUNDDB.NIST_COMPOUND_DATA  " +
				"(NIST_ID, NAME, CAS_NUMBER, RELATED_CAS, " +
				"FORMULA, INCHI_KEY, SMILES, MOL_TXT)  " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?) ";
		ps = conn.prepareStatement(dataQuery);
		ps.setInt(1, nistId);
		ps.setString(2, newRecord.getProperties().get(MSPField.NAME));
		ps.setString(3, newRecord.getProperties().get(MSPField.CAS));
		ps.setString(4, newRecord.getProperties().get(MSPField.RELATED_CAS));
		ps.setString(5, newRecord.getProperties().get(MSPField.FORMULA));
		ps.setString(6, newRecord.getProperties().get(MSPField.INCHI_KEY));
		
		if(molecule != null) {
			ps.setString(7, molecule.getProperty(CompoundIdentityField.SMILES.name()));
			//32767
			String molText = molecule.getProperty(CompoundIdentityField.MOL_TEXT.name());
			if(molText.length() < 32767)
				ps.setString(8, molText);
			else
				ps.setNull(8, java.sql.Types.NULL);
		}
		else {
			ps.setString(7, null);
			ps.setString(8, null);
		}
		ps.executeUpdate();
		ps.close();
	}

	public static List<List<String>> parseInputMspFile(File inputFile) {

		List<List<String>> mspChunks = new ArrayList<List<String>>();
		List<String> allLines = TextUtils.readTextFileToList(inputFile.getAbsolutePath());
		List<String> chunk = new ArrayList<String>();
		Pattern namePattern = Pattern.compile("(?i)^" + MSPField.NAME.getName() + ":");
		Pattern pnumPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+\\d+");
		Matcher regexMatcher;
		int counter = 0;

		for (String line : allLines) {

			regexMatcher = namePattern.matcher(line.trim());

			if (regexMatcher.find() 
						//	|| counter == allLines.size()-1
					) {

				if (!chunk.isEmpty()) {

					Optional<String> numPeaks = chunk.stream()
							.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

					if (numPeaks.isPresent()) {

						List<String> newChunk = new ArrayList<String>();
						newChunk.addAll(chunk);
						mspChunks.add(newChunk);
					}
					chunk.clear();
				}
			}
			if(!line.trim().isEmpty())
				chunk.add(line.trim());

			counter++;
		}
		//	process last chunk
		if (!chunk.isEmpty()) {

			Optional<String> numPeaks = chunk.stream()
					.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

			if (numPeaks.isPresent()) {

				List<String> newChunk = new ArrayList<String>();
				newChunk.addAll(chunk);
				mspChunks.add(newChunk);
			}
			chunk.clear();
		}
		return mspChunks;
	}

	public static void uploadStructureData(Map<String,String>molMap, Map<String,String>smilesMap, Connection conn) throws SQLException {

		String dataQuery = "UPDATE NIST_COMPOUND_DATA SET MOL = ?, SMILES = ? WHERE NAME = ? AND MOL IS NULL";
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		for (Map.Entry<String, String> entry : molMap.entrySet()) {

//			dataQuery = "UPDATE NIST_COMPOUND_DATA SET MOL = ?, SMILES = ? WHERE NAME = ? AND MOL IS NULL";
//			ps = conn.prepareStatement(dataQuery);
//
//			ps.setString(1, entry.getValue());
//			ps.setString(2, smilesMap.get(entry.getKey()));
//			ps.setString(3, entry.getKey());
//			int rowsUpdated = ps.executeUpdate();
//			ps.close();
//
//			if(rowsUpdated == 0) {

				dataQuery = "UPDATE NIST_COMPOUND_DATA SET MOL = ?, SMILES = ? WHERE NAME LIKE ? AND MOL IS NULL";
				ps = conn.prepareStatement(dataQuery);

				ps.setString(1, entry.getValue());
				ps.setString(2, smilesMap.get(entry.getKey()));
				ps.setString(3, entry.getKey() + '%');
				ps.executeUpdate();
				ps.close();
			//}
		}
	}

	public static boolean isSpectrumInDatabase(
			String libraryName,
			String originalLibraryId,
			Connection conn) throws SQLException {

		String query =
			"SELECT MRC2_LIB_ID FROM REF_MSMS_LIBRARY_COMPONENT"
			+ " WHERE LIBRARY_NAME = ? AND ORIGINAL_LIBRARY_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, libraryName);
		ps.setString(2, originalLibraryId);
		ResultSet rs = ps.executeQuery();
		String mrcId = null;
		while(rs.next()) {
			mrcId = rs.getString(1);
		}
		rs.close();
		ps.close();

		if(mrcId != null)
			return true;
		else
			return false;
	}
	
	public static boolean isNISTSpectrumInDatabase(
			int nistNum,
			Connection conn) throws SQLException {

		String query =
			"SELECT DB_NUM  FROM NIST_LIBRARY_COMPONENT WHERE NIST_ID = ? ";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, nistNum);
		ResultSet rs = ps.executeQuery();
		String mrcId = null;
		while(rs.next())
			mrcId = rs.getString(1);
		
		rs.close();
		ps.close();
		return mrcId != null;
	}

	public static void insertPeptideSpectrumRecord(NISTTandemMassSpectrum newRecord, Connection conn) throws SQLException {

//		String query = "SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() +
//				"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MRC2_LIB_ID FROM DUAL";
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		String mrcId = null;
//		while(rs.next()) {
//			mrcId = rs.getString("MRC2_LIB_ID");
//			break;
//		}
//		rs.close();
//		ps.close();
		
		String mrcId = null;
		try {
			mrcId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//	Component record
		String query =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT_PEP (" +
			"MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ,  " +
			"ADDUCT, COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE,  " +
			"MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION, SPECTRUM_TYPE,  " +
			"RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, ORIGINAL_LIBRARY_ID) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, mrcId);
		ps.setString(2, newRecord.getProperties().get(MSPField.ION_MODE));
		ps.setString(3, newRecord.getProperties().get(MSPField.IONIZATION));
		ps.setString(4, newRecord.getProperties().get(MSPField.COLLISION_ENERGY));

		MsPoint precursor = newRecord.getPrecursors().iterator().next();
		if(newRecord.getPrecursors().size() > 1)
			precursor =  newRecord.getPrecursors().stream().
				sorted(new MsDataPointComparator(SortProperty.MZ, SortDirection.DESC)).
				findFirst().get();

		ps.setDouble(5, precursor.getMz());
		ps.setString(6, newRecord.getProperties().get(MSPField.PRECURSOR_TYPE));
		ps.setString(7, newRecord.getProperties().get(MSPField.COLLISION_GAS));
		ps.setString(8, newRecord.getProperties().get(MSPField.INSTRUMENT));
		ps.setString(9, newRecord.getProperties().get(MSPField.INSTRUMENT_TYPE));
		ps.setString(10, newRecord.getProperties().get(MSPField.IN_SOURCE_VOLTAGE));
		ps.setString(11, newRecord.getProperties().get(MSPField.MSN_PATHWAY));
		ps.setString(12, newRecord.getProperties().get(MSPField.PRESSURE));
		ps.setString(13, newRecord.getProperties().get(MSPField.SAMPLE_INLET));
		ps.setString(14, newRecord.getProperties().get(MSPField.SPECIAL_FRAGMENTATION));
		ps.setString(15, "MS2");
		ps.setString(16, "HIGH");
		ps.setString(17, "INSTRUMENT");
		ps.setString(18, newRecord.getProperties().get(MSPField.IONIZATION));
		ps.setString(19, "nist_msms2");
		ps.setString(20, Integer.toString(newRecord.getDbnum()));
		ps.executeUpdate();
		ps.close();

		//	Compound data
		query =
			"INSERT INTO REF_MSMS_COMPOUND_DATA_PEP (MRC2_LIB_ID, NAME, FORMULA, PEPTIDE_SEQUENCE) VALUES (?, ?, ?, ?) ";

		ps = conn.prepareStatement(query);
		ps.setString(1, mrcId);
		ps.setString(2, newRecord.getProperties().get(MSPField.NAME));
		ps.setString(3, newRecord.getProperties().get(MSPField.FORMULA));
		ps.setString(4, newRecord.getPeptideSequence());
		ps.executeUpdate();
		ps.close();

		//	Annotations
//		TODO handle through separate simple oblect for text annotation
		/*
		 * query =
		 * "INSERT INTO REF_MSMS_PROPERTIES_PEP (MRC2_LIB_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES(?, ?, ?)"
		 * ;
		 * 
		 * ps = conn.prepareStatement(query); ps.setString(1, mrcId); ps.setString(2,
		 * "Annotation"); for(ObjectAnnotation annot : newRecord.getAnnotations()) {
		 * ps.setString(3, annot.getText()); ps.executeUpdate(); }
		 * if(newRecord.getPeptideModifications() != null) { ps.setString(2,
		 * MSPField.PEPTIDE_MODS.getName()); ps.setString(3,
		 * newRecord.getPeptideModifications()); ps.executeUpdate(); } ps.close();
		 */

		//	Spectrum
		query =
			"INSERT INTO REF_MSMS_LIBRARY_PEAK_PEP " +
			"(MRC2_LIB_ID, MZ, INTENSITY, FRAGMENT_COMMENT, IS_PARENT) " +
			"VALUES(?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(query);
		ps.setString(1, mrcId);

		//	Parent ion
		for(MsPoint parent : newRecord.getPrecursors()) {

			ps.setDouble(2, parent.getMz());
			ps.setDouble(3, parent.getIntensity());
			ps.setString(4, parent.getAdductType());
			ps.setString(5, "Y");
			ps.executeUpdate();
		}

		//	Spectrum
		for(MsPoint p : newRecord.getSpectrum()) {

			ps.setDouble(2, p.getMz());
			ps.setDouble(3, p.getIntensity());
			ps.setString(4, p.getAdductType());
			ps.setString(5, "N");
			ps.executeUpdate();
		}
		ps.close();
	}
}






































