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

package edu.umich.med.mrc2.datoolbox.dbparse.load.mine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MINEMSPParser {

	public static String dataDir = "." + File.separator + "data" + File.separator;
	public static String configDir = dataDir + "config" + File.separator;
	public static String iconDir = dataDir + "icons" + File.separator;
	public static String referenceDir = "." + File.separator + "data" + File.separator + "reference" + File.separator;
	public static String qualMethodsDir = "." + File.separator + "data" + File.separator + "qualmethods" + File.separator;
	public static String libraryDir = dataDir + "libraries" + File.separator;
	public static String msSearchDir = dataDir + "mssearch" + File.separator;

	private static Polarity polarity;
	private static Connection connection;
	private static int totalCount = 0;
	private static Collection<MINETandemMassSpectrum>recordBatch;
	
	public MINEMSPParser(Polarity polarity2,  Connection connection2) {
		super();
		polarity = polarity2;
		connection = connection2;
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			AdductManager.refreshAlldata();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		recordBatch = new ArrayList<MINETandemMassSpectrum>();
	}

	/**
	 * Parse single NIST MSP record converted to list of strings per line
	 * Try to determine parent ion from the MSP fields or from parent library feature
	 *
	 * @param sourceText - MSP record converted to list of strings per line
	 * @return
	 */
	public static MINETandemMassSpectrum parseMINEMspDataSource(List<String>sourceText, Polarity polarity){

		MINETandemMassSpectrum msmsSet = new MINETandemMassSpectrum(polarity);
		int spectrumStart = -1;
		int pnum = 0;
		double precursorMz = -1.0;
		String precursorString = "";
		Matcher regexMatcher = null;
		Map<MINEMSPFields,Pattern>patternMap = new TreeMap<MINEMSPFields,Pattern>();
		for(MINEMSPFields field : MINEMSPFields.values())
			patternMap.put(field, Pattern.compile("(?i)^" + field.getName() + ":?\\s+(.+)"));

		String[] record = sourceText.toArray(new String[sourceText.size()]);
		spectrumStart = -1;

		//	Add all non-ms data
		for(int i=0; i<record.length; i++) {

			regexMatcher = patternMap.get(MINEMSPFields.NUM_PEAKS).matcher(record[i]);
			if (regexMatcher.find()) {

				pnum = Integer.parseInt(regexMatcher.group(1));
				spectrumStart = i + 1;
				break;
			}
			else {
				for (Entry<MINEMSPFields, Pattern> entry : patternMap.entrySet()) {

					regexMatcher = entry.getValue().matcher(record[i]);

					if(regexMatcher.find()) {

						if(entry.getKey().equals(MINEMSPFields.ID)) {
							msmsSet.setSpectrumId(regexMatcher.group(1));
						}
						else if(entry.getKey().equals(MINEMSPFields.MINE_ID)) {
							msmsSet.setMineId(regexMatcher.group(1));
						}
						else {
							msmsSet.addProperty(entry.getKey(), regexMatcher.group(1));
						}
					}
				}
			}
		}
		//	Calculate parent peak mz
		double neutralMass = Double.parseDouble(msmsSet.getProperties().get(MINEMSPFields.MASS));
		Adduct adduct = AdductManager.getDefaultAdductForPolarity(polarity);
		double bpMz = MsUtils.calculateModifiedMz(neutralMass, adduct);
		MsPoint parent = new MsPoint(bpMz, 999.0d, adduct.getName());
		msmsSet.setParent(parent);

		if (pnum > 0) {

			Pattern msmsPattern = Pattern.compile("^([0-9\\.]+)\\s?,?\\s?([0-9,\\.,e,+,-]+)\\s?");
			Collection<MsPoint> dataPoints = new ArrayList<MsPoint>();
			for(int i=spectrumStart; i<record.length; i++) {

				regexMatcher = msmsPattern.matcher(record[i]);
				if(regexMatcher.find()) {

					MsPoint dp = new MsPoint(
						Double.parseDouble(regexMatcher.group(1)),
						Double.parseDouble(regexMatcher.group(2)));
					dataPoints.add(dp);
				}
			}
			msmsSet.setSpectrum(dataPoints);
		}
		else {
			throw new IllegalArgumentException("No peak data!");
		}
		return msmsSet;
	}

	public static void insertSpectrumRecord(Collection<MINETandemMassSpectrum> recordSet, Connection conn) throws SQLException {

		String dataQuery =
				"INSERT INTO MINE_COMPOUND_DATA_TMP " +
				"(MINE_ID, MINE_SPECTRUM_ID, NAME, EXACT_MASS, FORMULA, INCHI_KEY, "
				+ "SMILES, INSTRUMENT, IONIZATION_MODE, FRAG_ENERGY, GENERATION, UNIQUE_ID) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		//	Component record
		PreparedStatement ps = conn.prepareStatement(dataQuery);

		String peakQuery =
				"INSERT INTO MINE_PEAK " +
				"(UNIQUE_ID, MZ, INTENSITY, ADDUCT, IS_PARENT) " +
				"VALUES(?, ?, ?, ?, ?)";

		PreparedStatement peakps = conn.prepareStatement(peakQuery);

		for(MINETandemMassSpectrum newRecord : recordSet) {

			ps.setString(1, newRecord.getMineId());
			ps.setString(2, newRecord.getSpectrumId());
			ps.setString(3, newRecord.getProperties().get(MINEMSPFields.NAME));
			ps.setDouble(4, Double.parseDouble(newRecord.getProperties().get(MINEMSPFields.MASS)));
			ps.setString(5, newRecord.getProperties().get(MINEMSPFields.FORMULA));
			ps.setString(6, newRecord.getProperties().get(MINEMSPFields.INCHI_KEY));
			ps.setString(7, newRecord.getProperties().get(MINEMSPFields.SMILES));
			ps.setString(8, newRecord.getProperties().get(MINEMSPFields.INSTRUMENT));
			ps.setString(9, newRecord.getProperties().get(MINEMSPFields.IONIZATION_MODE));
			ps.setString(10, newRecord.getProperties().get(MINEMSPFields.ENERGY));
			ps.setString(11, newRecord.getProperties().get(MINEMSPFields.GENERATION));
			ps.setString(12, newRecord.getId());
			ps.addBatch();

			//
			MsPoint parent = newRecord.getParent();
			peakps.setString(1, newRecord.getId());
			peakps.setDouble(2, parent.getMz());
			peakps.setDouble(3, parent.getIntensity());
			peakps.setString(4, parent.getAdductType());
			peakps.setString(5, "Y");
			peakps.addBatch();

			for(MsPoint p : newRecord.getSpectrum()) {

				peakps.setString(1, newRecord.getId());
				peakps.setDouble(2, p.getMz());
				peakps.setDouble(3, p.getIntensity());
				peakps.setString(4, null);
				peakps.setString(5, "N");
				peakps.addBatch();
			}
		}
		ps.executeBatch();
		ps.clearBatch();
		peakps.executeBatch();
		peakps.clearBatch();
		ps.close();
		peakps.close();
	}

	public static List<List<String>> pareseInputFile(File inputFile) throws IOException {

		List<List<String>> mspChunks = new ArrayList<List<String>>();
		List<String> chunk = new ArrayList<String>();
		Pattern pnumPattern = Pattern.compile("(?i)^" + MINEMSPFields.NUM_PEAKS.getName() + ":?\\s+\\d+");
		Matcher regexMatcher;

		try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

		    for (String line = null; (line = br.readLine()) != null;) {

				if (line.trim().isEmpty()) {

					if (!chunk.isEmpty()) {

						regexMatcher = pnumPattern.matcher(line.trim());
						Optional<String> numPeaks = chunk.stream()
								.filter(l -> pnumPattern.matcher(l.trim()).find()).findFirst();

						if (numPeaks.isPresent()) {

							totalCount++;
							MINETandemMassSpectrum msms = parseMINEMspDataSource(chunk, polarity);
							recordBatch.add(msms);
							if(recordBatch.size() == 1000) {
								try {
									insertSpectrumRecord(recordBatch, connection);
									System.out.println(Integer.toString(totalCount) + " records");
									recordBatch.clear();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						chunk.clear();
					}
				}
				else
					chunk.add(line.trim());
		    }
		}
	    try {
			insertSpectrumRecord(recordBatch, connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Integer.toString(totalCount) + " records");
		return mspChunks;
	}

/*	public static void uploadStructureData(Map<String,String>molMap, Map<String,String>smilesMap, Connection conn) throws SQLException {

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
	}*/
}






































