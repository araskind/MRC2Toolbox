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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.LipidMapsClassifier;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.lipid.LipidOntologyUtils;
import edu.umich.med.mrc2.datoolbox.database.load.lipidmaps.LipidMapsFields;
import edu.umich.med.mrc2.datoolbox.database.load.lipidmaps.LipidMapsParser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;

public class RunContainer {

	public static String dataDir = "." + File.separator + "data" + File.separator;
	private static String dbHome = dataDir + "database" + File.separator + "CefAnalyzerDB";
	private static String dbUser = "CefAnalyzer";
	private static String dbPassword = "CefAnalyzer";

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			getPepSearchCommand();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getPepSearchCommand() {
		
		File inputFile = new File(
				"E:\\Eclipse\\git2\\MRC2Toolbox\\data\\mssearch\\20200903-150116_NIST_MSMS_PEPSEARCH_RESULTS.TXT");
		String line = "";
		try (Stream<String> lines = Files.lines(Paths.get(inputFile.getAbsolutePath()))) {
			line = lines.skip(1).findFirst().get();
			System.out.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		NISTPepSearchUtils.parsePepSearchCommandLine(line);
		return line;
	}
	
	private static void normalizeFormula(String inputFormula) {
		
		IMolecularFormula formula = MolecularFormulaManipulator.
				getMolecularFormula(inputFormula, builder);
		
		System.out.println(MolecularFormulaManipulator.getString(formula));
	}
	
	private static void createLmAbbreviationsForLipidBlast() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT RLB_ID, NAME, ABBREVIATION FROM LIPIDBLAST_RIKEN_COMPONENTS WHERE LM_ABBREVIATION IS NULL";	
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updSql = "UPDATE LIPIDBLAST_RIKEN_COMPONENTS SET LM_ABBREVIATION = ? WHERE RLB_ID = ?";	
		PreparedStatement updPs = conn.prepareStatement(updSql);
		
		Pattern fa = Pattern.compile("\\d+:\\d+");
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		while(rs.next()) {
			
			String[] matches = fa.matcher(rs.getString("NAME"))
	                .results()
	                .map(MatchResult::group)
	                .toArray(String[]::new);
			if(matches.length > 0) {
				int carbons = 0;
				int doubleBonds = 0;
				for(String match : matches) {
					
					String[] code = match.split(":");
					if(code.length == 2) {
						carbons += Integer.valueOf(code[0]);
						doubleBonds += Integer.valueOf(code[1]);
					}
				}
				if(carbons > 0) {
					String lmAbbr = rs.getString("ABBREVIATION") + 
							"(" + Integer.toString(carbons) + ":" + Integer.toString(doubleBonds) + ")";
					updPs.setString(1, lmAbbr);
					updPs.setString(2, rs.getString("RLB_ID"));
					updPs.addBatch();
					count++;
				}				
			}
			if(count % 100 == 0) {
				updPs.executeBatch();
				System.out.print(".");
			}
			if(count % 5000 == 0) {
				updPs.executeBatch();
				System.out.println(".");
			}
		}	
		updPs.executeBatch();
		updPs.close();
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	//;
	
	private static void classifyLipidBlastByLipidMapsFingerprints() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String> subFingerprints = LipidOntologyUtils.getUniqueLipidMapsSubFingerprints(conn);		
		Map<String, Collection<LipidMapsClassifier>> fpMap = 
				LipidOntologyUtils.mapLipidMapsSubFingerprintsToClassifiers(subFingerprints, conn);
		Map<String, LipidMapsClassifier>bestClassifiers = LipidOntologyUtils.findBestLipidMapsClassifiers(fpMap);
			
		ConnectionManager.releaseConnection(conn);
		System.out.println("###");
	}
	
	private static void insertLipidMapsData() throws Exception {

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002.sdf");
		Connection conn = ConnectionManager.getConnection();
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				IAtomContainer molecule = (IAtomContainer)reader.next();
				LipidMapsParser.insertLipidMapsRecord(molecule, conn);
			
				count++;
				if((count % 50) == 0) 
					System.out.print(".");
				if((count % 2000) == 0) 
					System.out.println(".");				
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void extractLipidMapsClasses() {

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002.sdf");
	//	File sdfFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\lmtest.sdf");

		Map<LipidMapsFields, Collection<String>>classMap = new TreeMap<LipidMapsFields, Collection<String>>();
		
		classMap.put(LipidMapsFields.CATEGORY, new TreeSet<String>());
		classMap.put(LipidMapsFields.MAIN_CLASS, new TreeSet<String>());
		classMap.put(LipidMapsFields.SUB_CLASS, new TreeSet<String>());
		classMap.put(LipidMapsFields.CLASS_LEVEL4, new TreeSet<String>());
		
		IteratingSDFReaderFixed reader;
		try {
			reader = new IteratingSDFReaderFixed(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
			int count = 1;
			while (reader.hasNext()) {
				
				Map<String, String>lipidMapsDataMap = LipidMapsParser.getCompoundDataMap();
				IAtomContainer molecule = (IAtomContainer)reader.next();
				molecule.getProperties().forEach((k,v)->{

					if(lipidMapsDataMap.containsKey(k.toString()))
						lipidMapsDataMap.put(k.toString(), v.toString());
				});
				String category = lipidMapsDataMap.get(LipidMapsFields.CATEGORY.name());
				if(category != null)
					classMap.get(LipidMapsFields.CATEGORY).add(category);
				
				String mainClass = lipidMapsDataMap.get(LipidMapsFields.MAIN_CLASS.name());
				if(mainClass != null)
					classMap.get(LipidMapsFields.MAIN_CLASS).add(mainClass);
				
				String subClass = lipidMapsDataMap.get(LipidMapsFields.SUB_CLASS.name());
				if(subClass != null)
					classMap.get(LipidMapsFields.SUB_CLASS).add(subClass);
				
				String classLevel4 = lipidMapsDataMap.get(LipidMapsFields.CLASS_LEVEL4.name());
				if(classLevel4 != null)
					classMap.get(LipidMapsFields.CLASS_LEVEL4).add(classLevel4);
				
				count++;
				if((count % 50) == 0) 
					System.out.print(".");
				if((count % 2000) == 0) 
					System.out.println(".");				
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String>output = new ArrayList<String>();
		for(Entry<LipidMapsFields, Collection<String>> e : classMap.entrySet()) {
			
			for(String lmClass : e.getValue())				
				output.add(e.getKey().name() + "\t" + lmClass);			
		}
		File outFile = new File("E:\\DataAnalysis\\Databases\\LipidMaps\\20191002\\LMSD_20191002_ClASSES.TXT");
		try {
			FileUtils.writeLines(outFile, output, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void exportLipidBlastForNist(Polarity pol, File exportFile) throws Exception {
		
		String libName = "LIPIDBLAST_RIKEN";
		String ionMode = pol.getCode();

		exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));

		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT C.MRC2_LIB_ID, C.PRECURSOR_MZ, C.ADDUCT,"
			+ "R.NAME, R.INCHI_KEY, R.FORMULA, R.COMMENTS, R.COMPOUNDCLASS "
			+ "FROM REF_MSMS_LIBRARY_COMPONENT C, "
			+ "LIPIDBLAST_RIKEN_COMPONENTS R "
			+ "WHERE C.ORIGINAL_LIBRARY_ID = R.RLB_ID "
			+ "AND  C.LIBRARY_NAME = ? AND C.POLARITY = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		String msQuery =
			"SELECT MZ, INTENSITY FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ? ORDER BY MZ";
		PreparedStatement msps = conn.prepareStatement(msQuery);
		
		ps.setString(1, libName);
		ps.setString(2, ionMode);
		ResultSet rs = ps.executeQuery();
		
		ResultSet msrs = null;
		StringBuffer sb  = null;
		int counter = 0;
		while(rs.next()) {

			writer.append(MSPField.NAME.getName() + ": " + rs.getString("MRC2_LIB_ID") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("COMPOUNDCLASS") + "\n");
			writer.append(MSPField.SYNONYM.getName() + ": " + rs.getString("NAME") + "\n");
			writer.append(MSPField.FORMULA.getName() + ": " + rs.getString("FORMULA") + "\n");
			writer.append(MSPField.ION_MODE.getName() + ": " + ionMode + "\n");

			if(rs.getString("ADDUCT") != null)
				writer.append(MSPField.PRECURSOR_TYPE.getName() + ": " + rs.getString("ADDUCT") + "\n");

			if(rs.getString("PRECURSOR_MZ") != null)
				writer.append(MSPField.PRECURSORMZ.getName() + ": " + rs.getString("PRECURSOR_MZ") + "\n");
			
			writer.append(MSPField.INCHI_KEY.getName() + ": " + rs.getString("INCHI_KEY") + "\n");
			writer.append(MSPField.COMMENT.getName() + ": " + rs.getString("COMMENTS") + "\n");
			
			msps.setString(1, rs.getString("MRC2_LIB_ID"));
			msrs = msps.executeQuery();
			int pointCount = 0;
			sb = new StringBuffer();
			while(msrs.next()) {

				double mz = msrs.getDouble("MZ");
				if(mz > 10.0d) {
					
					sb.append(
							MRC2ToolBoxConfiguration.getMzFormat().format(msrs.getDouble("MZ"))
							+ " " + intensityFormat.format(msrs.getDouble("INTENSITY")) +"\n") ;
						pointCount++;
				}
			}
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(pointCount) + "\n");
			writer.append(sb.toString());
			writer.append("\n\n");
			msrs.close();
			counter++;
			
			if(counter % 50 == 0)
				System.out.print(".");
			if(counter % 2000 == 0)
				System.out.println(".");
		}
		rs.close();
		ps.close();
		msps.close();
		ConnectionManager.releaseConnection(conn);

		writer.flush();
		writer.close();
	}
	

}
