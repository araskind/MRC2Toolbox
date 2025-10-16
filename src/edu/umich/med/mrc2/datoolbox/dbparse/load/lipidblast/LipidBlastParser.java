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

package edu.umich.med.mrc2.datoolbox.dbparse.load.lipidblast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.dbparse.load.msdial.MSDialMSMSRecord;

public class LipidBlastParser {

	private final static MSDialMSMSRecord newRecord = new MSDialMSMSRecord();
	private final static Pattern searchPattern = Pattern.compile("^(\\d+\\.\\d+) (\\d+) \\\"(.+)\\\"");
	private static int firstId = 78346;
	private static String msModeString;

	public static void processLipidBlastRecords(File inputFile, Connection con, String msMode) throws IOException {

		msModeString = msMode;

		try (Stream<String> lines = Files.lines(Paths.get(inputFile.getAbsolutePath()), Charset.defaultCharset())) {

			lines.forEachOrdered(line -> {
				if(line.trim().isEmpty() && !newRecord.getSpectrum().isEmpty()) {
					try {
						insertRecord(newRecord, con);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					if(line.startsWith("Name:")) {
						newRecord.resetRecord();
						String[] pieces = StringUtils.split(line.replace("Name: ", ""), ';');
						newRecord.setName(pieces[2].trim());
						newRecord.setAdduct(pieces[1].trim());
						newRecord.setAbbreviation(pieces[0].trim());
					}
					if(line.startsWith("MW:")) {
						double exactMass = Double.parseDouble(line.replace("MW:", "").trim());
						newRecord.setExactMass(exactMass);
					}
					if(line.startsWith("PRECURSORMZ:")) {
						double pmz = Double.parseDouble(line.replace("PRECURSORMZ:", "").trim());
						newRecord.setPrecursorMz(pmz);
					}
					if(line.startsWith("Comment:")) {
						newRecord.setComment(line.replace("Comment:", "").trim());
						String[] comments = newRecord.getComment().split(";");
						String formula = comments[comments.length-1].trim();
						if(!formula.isEmpty())
							newRecord.setFormula(formula);
					}
					if(line.startsWith("Num Peaks:")) {
						int np = Integer.parseInt(line.replace("Num Peaks:", "").trim());
						newRecord.setNumPeaks(np);
					}
					//	TODO use regex to find parts
					if(Character.isDigit(line.charAt(0))) {

						Matcher regexMatcher = searchPattern.matcher(line.trim());
						if(regexMatcher.find()) {

							double mz = Double.parseDouble(regexMatcher.group(1));
							double intensity = Double.parseDouble(regexMatcher.group(2));
							String adduct = regexMatcher.group(3).trim();
							MsPoint p = new MsPoint(mz, intensity, adduct);
							newRecord.getSpectrum().add(p);
						}
					}
				}
			});
		}
	}

	private static void insertRecord(MSDialMSMSRecord newRecord, Connection conn) throws SQLException {

		IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();

		String dataQuery =
				"INSERT INTO LIPIDBLAST_PRECURSOR " +
				"(LB_ID, FULL_NAME, CODE_NAME, PRECURSOR_MZ, ADDUCT, MS_MODE, MOL_FORMULA, FORMULA_MASS) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		String id = DataPrefix.LIPID_BLAST.getName() + StringUtils.leftPad(Integer.toString(firstId), 8, '0');
		Double formulaMass = null;
		if(newRecord.getFormula() != null) {
			IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(newRecord.getFormula(), builder);
			formulaMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
		}
		//	Main record
		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);
		ps.setString(2, newRecord.getName());
		ps.setString(3, newRecord.getAbbreviation());
		ps.setDouble(4, newRecord.getPrecursorMz());
		ps.setString(5, newRecord.getAdduct());
		ps.setString(6, msModeString);
		ps.setString(7, newRecord.getFormula());
		ps.setDouble(8, formulaMass);
		ps.executeUpdate();
		ps.close();

		dataQuery =
			"INSERT INTO LIPIDBLAST_FRAGMENT " +
			"(LB_ID, FRAGMENT_NAME, FRAGMENT_MZ, REL_INTENSITY) " +
			"VALUES(?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);

		//	Spectrum
		for(MsPoint p : newRecord.getSpectrum()) {

			ps.setString(2, p.getAdductType());
			ps.setDouble(3, p.getMz());
			ps.setDouble(4, p.getIntensity());
			ps.executeUpdate();
		}
		ps.close();

		// System.out.println(newRecord.getName());

		firstId++;
	}
}


































