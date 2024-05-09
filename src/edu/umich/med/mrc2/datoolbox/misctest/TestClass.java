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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.IsotopePattern;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundNameSet;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PubChemCompoundDescriptionBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.DatabaseIdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.isdb.ISDBMGFParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.mine.MINEMSPParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTMSPParser;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTTandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.HiResSearchOption;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb.PubChemDataFetchTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class TestClass {

	private static final FastDateFormat DATE_FORMATTER  =
		FastDateFormat.getInstance("yyyy-MM-dd'T'hh:mm:ss.SSSZZ",TimeZone.getDefault(), Locale.US);
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		
		Path pathToAccessionFile = Paths.get("E:\\DataAnalysis\\Databases\\MaConDa\\CombinedContaminantAccessions.txt");
		String libraryId = "CPDLIB_bee7ae41-6908-4355-a363-0a7cfd5d0b72";
		try {
			addCompoundsToLibraryFroAccessionList(pathToAccessionFile, libraryId);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void addCompoundsToLibraryFroAccessionList(Path pathToAccessionFile, String libraryId) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		List<String>accessionList = new ArrayList<String>();
		try {
			accessionList = Files.readAllLines(pathToAccessionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String accession : accessionList)
			MSRTLibraryUtils.addCompoundToLibrary(accession, libraryId, conn);
				
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void normalizeSingleFormulas(String inFormula) {
		
		String outFormula = null;
		try {
			outFormula = MolFormulaUtils.normalizeFormula(inFormula);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(outFormula != null)
			System.out.println(outFormula);
	}
	
	private static void normalizeFormulas(Path input, Path output) {
		
		List<String>inputFormulas = new ArrayList<String>();
		try {
			inputFormulas = Files.readAllLines(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String>formulaOut = new ArrayList<String>();
		formulaOut.add("ORIGINAL\tNORMALIZED");
		for(String inFormula : inputFormulas) {
			
			String outFormula = MolFormulaUtils.normalizeFormula(inFormula);
			formulaOut.add(inFormula + "\t" + outFormula);
		}
	    try {
			Files.write(output, 
					formulaOut, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void comparePepSearchParameters(
			String parOneId, 
			String parTwoId)  throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
				
		NISTPepSearchParameterObject parOne = 
				DatabaseIdentificationUtils.getNISTPepSearchParameterObjectById(parOneId, conn);
		NISTPepSearchParameterObject parTwo = 
				DatabaseIdentificationUtils.getNISTPepSearchParameterObjectById(parTwoId, conn);
		
//		System.out.print("Search parameters\n-------------------------\n");
//		System.out.println(parOneId + "\t" + parTwoId);
//		
//		String preSearchType = "";
//		if(parOne.getPreSearchType() != null)
//			preSearchType += parOne.getPreSearchType().toString() + "\t";
//		else
//			preSearchType += "NULL\t";
//		
//		if(parTwo.getPreSearchType() != null)
//			preSearchType += parTwo.getPreSearchType().toString();
//		else
//			preSearchType += "NULL";
		
		System.out.print(parOne.printSearchParameters());
		System.out.print(parTwo.printSearchParameters());
		
		ConnectionManager.releaseConnection(conn);	
	}
	
	private static void disableIdsFromList() throws Exception {
		
		Path listPath = Paths.get("C:\\Users\\Sasha\\Downloads\\IDX0054-IDs-toDisable.txt");
		List<String> idList = Files.readAllLines(listPath);	
		Connection conn = ConnectionManager.getConnection();		
		for(String id : idList) {
			if(id.startsWith("MSN_"))
				DatabaseIdentificationUtils.disableMSMSFeaturePrimaryIdentity(id, conn);
		}
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void updatePrimaryCompoundNames() throws Exception {
		
		Pattern casPattern = Pattern.compile("^\\d+-\\d+-\\d+$");
		Pattern zincPattern = Pattern.compile("^ZINC\\d+$");
		Pattern scPattern = Pattern.compile("^SCHEMBL\\d+$");
		Pattern cidPattern = Pattern.compile("^CID \\d+$");
		Matcher casMatcher = null;
		Matcher zincMatcher = null;
		Matcher scMatcher = null;
		Matcher cidMatcher = null;
		
		Connection conn = ConnectionManager.getConnection();
		String sql = "SELECT ACCESSION, INCHI_KEY FROM COMPOUND_DATA  " +
				"WHERE (REGEXP_LIKE (PRIMARY_NAME, '^\\d+-\\d+-\\d+$') "
				+ "OR PRIMARY_NAME = INCHI_KEY "
				+ "OR REGEXP_LIKE (PRIMARY_NAME, '^ZINC\\d+$')"
				+ "OR REGEXP_LIKE (PRIMARY_NAME, '^SCHEMBL\\d+$')"
				+ "OR REGEXP_LIKE (PRIMARY_NAME, '^CID \\d+$')"
				+ "OR PRIMARY_NAME = ACCESSION "
				+ ") AND SOURCE_DB = 'PUBCHEM'";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updQuery = "UPDATE COMPOUND_DATA SET PRIMARY_NAME = ? WHERE ACCESSION = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			String cid = rs.getString("ACCESSION");
			String inchiKey = rs.getString("INCHI_KEY");
			System.out.println(cid + "\t" + inchiKey);
			
			updPs.setString(2, cid);
			//	Get existing synonyms
			CompoundNameSet synonyms = CompoundDatabaseUtils.getSynonyms(cid, conn);
			
			//	Get pubchem title
			PubChemCompoundDescriptionBundle compDesc = PubChemDataFetchTask.getCompoundDescription(cid);
			
			//	If title is present in synonyms, make it primary
			String title = compDesc.getTitle();
			String existingTitle = synonyms.getSynonyms().keySet().
					stream().filter(n -> n.equals(title)).findFirst().orElse(null);
			
			boolean isPresent = false;
			if(existingTitle != null) {
				
				casMatcher = casPattern.matcher(existingTitle);
				zincMatcher = zincPattern.matcher(existingTitle);
				scMatcher = scPattern.matcher(existingTitle);
				cidMatcher = cidPattern.matcher(existingTitle);
				if(!casMatcher.find() && !zincMatcher.find() 
						&& !scMatcher.find() && !cidMatcher.find() 
						&& !existingTitle.equals(inchiKey) && !existingTitle.equals(cid)) {
					synonyms.setPrimaryName(existingTitle);
					updPs.setString(1, existingTitle);
					isPresent = true;
				}				
			}
			if(!isPresent) {
				//	Esle if title is not CAS, InchiKey or ZINC, add it to synonyms and make primary name
				casMatcher = casPattern.matcher(title);
				zincMatcher = zincPattern.matcher(title);
				scMatcher = scPattern.matcher(title);
				cidMatcher = cidPattern.matcher(title);
				if(!casMatcher.find() && !zincMatcher.find() 
						&& !scMatcher.find() && !cidMatcher.find() 
						&& !title.equals(inchiKey) && !title.equals(cid)) {
					synonyms.setPrimaryName(title);
					updPs.setString(1, title);
				}
				else {//	Else make IUPAC or Systematic name primary
					String iupac = synonyms.getIupacName();
					if(iupac == null)
						iupac = synonyms.getSystematicName();
					
					if(iupac == null) {
						System.out.println("Could not find common or systematic name for " + cid + "\t" + inchiKey);
					}
					else {
						synonyms.setPrimaryName(iupac);
						updPs.setString(1, iupac);
					}
				}
			}
			CompoundDatabaseUtils.updateSynonyms(synonyms, conn);
			updPs.executeUpdate();
		}
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void setDisabledIds() throws Exception {
		
		Map<String,String>exPol = new TreeMap<String,String>();
		exPol.put("IDX0045", "N");
		exPol.put("IDX0054", "P");
		exPol.put("IDX0061", "P");
		exPol.put("IDX0065", "P");
		exPol.put("IDX0077", "P");
		exPol.put("IDX0087", "P");
		
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT DISTINCT L.MSMS_FEATURE_ID, L.IS_PRIMARY " +
				"FROM MSMS_FEATURE_LIBRARY_MATCH L, " +
				"MSMS_FEATURE F,  " +
				"DATA_ANALYSIS_MAP M, " +
				"INJECTION I,  " +
				"PREPARED_SAMPLE P, " +
				"SAMPLE S " +
				"WHERE L.MSMS_FEATURE_ID = F.MSMS_FEATURE_ID " +
				"AND F.DATA_ANALYSIS_ID = M.DATA_ANALYSIS_ID " +
				"AND F.POLARITY = ? " +
				"AND M.INJECTION_ID = I.INJECTION_ID " +
				"AND I.PREP_ITEM_ID = P.PREP_ITEM_ID " +
				"AND P.SAMPLE_ID = S.SAMPLE_ID " +
				"AND S.EXPERIMENT_ID = ? " +
				"ORDER BY 1,2";		
		PreparedStatement ps = conn.prepareStatement(sql);
		
		String updQuery = "UPDATE MSMS_FEATURE SET ID_DISABLED = 'Y' WHERE MSMS_FEATURE_ID = ?";
		PreparedStatement updPs = conn.prepareStatement(updQuery);
		
		for(Entry<String, String> entry : exPol.entrySet()) {
			
			ps.setString(1, entry.getValue());
			ps.setString(2, entry.getKey());
			ResultSet rs = ps.executeQuery();
			TreeSet<String>hasPrimary = new TreeSet<String>();
			TreeSet<String>all = new TreeSet<String>();
			while(rs.next()) {
				
				all.add(rs.getString("MSMS_FEATURE_ID"));
				if(rs.getString("IS_PRIMARY") != null)
					hasPrimary.add(rs.getString("MSMS_FEATURE_ID"));
			}
			rs.close();
			Set<String> noPrimary = all.stream().filter(i -> !hasPrimary.contains(i)).collect(Collectors.toSet());
			System.out.println(entry.getKey());
			for(String np : noPrimary) {
				updPs.setString(1, np);
				updPs.addBatch();
			}
			updPs.executeBatch();
		}		
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void uploadClassyFireTaxNodesFromJson(File jsonDir) throws Exception {

		//	http://classyfire.wishartlab.com/entities/PTPBWFSLLGAWPP-DHSNEXAOSA-N.json
		
		File[] files = jsonDir.listFiles((dir, name) -> name.endsWith(".json"));

		Connection conn = ConnectionManager.getConnection();
		String query = "INSERT INTO CLASSYFIRE_TAX_NODES ("
				+ "CHEMONT_ID, NAME, DESCRIPTION, URL, PARENT, NR_OF_ENTITIES)  " + "VALUES (?, ?, ?, ?, ?, ?) ";
		PreparedStatement ps = conn.prepareStatement(query);

		String synonymQuery = "INSERT INTO CLASSYFIRE_TAX_NODE_SYNONYMS ("
				+ "CHEMONT_ID, NAME, SOURCE, SOURCE_ID, MAPPING_SCOPE)  " + "VALUES (?, ?, ?, ?, ?) ";
		PreparedStatement sps = conn.prepareStatement(synonymQuery);

		for (File jsonFile : files) {

			String jsString = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
			;
			JSONObject json = null;
			try {
				json = new JSONObject(jsString);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			String chemOntId = null;
			try {
				chemOntId = json.getString("chemont_id");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (chemOntId == null) {
				System.out.println("Missed ID " + jsonFile.getName());
				continue;
			}
			ps.setString(1, chemOntId);
			ps.setString(2, json.getString("name"));
			ps.setString(3, json.getString("description"));
			ps.setString(4, json.getString("url"));
			ps.setString(5, json.getString("parent"));
			ps.setInt(6, json.getInt("nr_of_entities"));
			ps.executeUpdate();

			// Insert synonyms
			JSONArray synonyms = json.getJSONArray("synonyms");
			if (synonyms == null || synonyms.length() == 0)
				continue;

			sps.setString(1, chemOntId);
			for (int j = 0; j < synonyms.length(); j++) {
				JSONObject synonym = synonyms.getJSONObject(j);
				sps.setString(2, synonym.getString("name"));
				sps.setString(3, synonym.getString("source"));
				sps.setString(4, synonym.getString("source_id"));
				sps.setString(5, synonym.getString("mapping_scope"));
				sps.addBatch();
			}
			sps.executeBatch();
		}
		ps.close();
		sps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void clearDecoyDuplicateEntries() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT   SOURCE_LIB_ID, " +
				"         COUNT(DECOY_LIB_ID) AS DUPE_CNT " +
				"FROM     REF_MSMS_DECOY_CROSSREF " +
				"GROUP BY SOURCE_LIB_ID " +
				"HAVING   COUNT(DECOY_LIB_ID) > 1 " +
				"ORDER BY COUNT(DECOY_LIB_ID) DESC ";
		TreeSet<String>sourceIds = new TreeSet<String>();
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			sourceIds.add(rs.getString("SOURCE_LIB_ID"));
		
		rs.close();
		System.out.println(Integer.toString(sourceIds.size()) + " redundancies found");
		
		sql = "SELECT DECOY_LIB_ID FROM REF_MSMS_DECOY_CROSSREF WHERE SOURCE_LIB_ID = ? ORDER BY 1";
		ps = conn.prepareStatement(sql);
		
		String delQuery = "DELETE FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";
		PreparedStatement delps = conn.prepareStatement(delQuery);
		for(String id : sourceIds) {
			
			ps.setString(1, id);
			rs = ps.executeQuery();
			ArrayList<String>decoyIds = new ArrayList<String>();
			while(rs.next())
				decoyIds.add(rs.getString("DECOY_LIB_ID"));
			
			rs.close();
			
			for(int i=1; i<decoyIds.size(); i++) {
				delps.setString(1, decoyIds.get(i));
				delps.addBatch();
			}
			delps.executeBatch();
		}	
		ps.close();
		delps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateMSMSMatchTypeFromNISTPepSearchParameterObjects()
			throws Exception {

		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT PARAMETER_SET_ID, PARAMETER_SET_OBJECT " 
				+ "FROM NIST_PEPSEARCH_PARAMETERS ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ArrayList<NISTPepSearchParameterObject>psList = 
				new ArrayList<NISTPepSearchParameterObject>();
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			Blob blob = rs.getBlob("PARAMETER_SET_OBJECT");
			BufferedInputStream bais = new BufferedInputStream(blob.getBinaryStream());
			ObjectInputStream oin = new ObjectInputStream(bais);
			NISTPepSearchParameterObject paramSet = (NISTPepSearchParameterObject) oin.readObject();		
			oin.close();
			bais.close();
			blob.free();
			paramSet.setId(rs.getString("PARAMETER_SET_ID"));
			psList.add(paramSet);
		}
		rs.close();
		
		query = "UPDATE MSMS_FEATURE_LIBRARY_MATCH SET MATCH_TYPE = ? "
				+ "WHERE MATCH_TYPE IS NULL AND SEARCH_PARAMETER_SET_ID = ? ";
		ps = conn.prepareStatement(query);
		
		for(NISTPepSearchParameterObject pepSearchParameterObject : psList) {
			
			MSMSMatchType matchType = MSMSMatchType.Regular;
			if(pepSearchParameterObject.getHiResSearchOption().equals(HiResSearchOption.y))
				matchType = MSMSMatchType.Hybrid;

			if(pepSearchParameterObject.getHiResSearchOption().equals(HiResSearchOption.u))
				matchType = MSMSMatchType.InSource;
			
			ps.setString(1, matchType.name());
			ps.setString(2, pepSearchParameterObject.getId());
			ps.executeUpdate();			
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void extractDecoyData(File fDir) {

		if (!fDir.exists() || !fDir.canRead()) 
			return;
		
		Path mspOutputPath = Paths.get(
				"E:\\DataAnalysis\\MSMS\\DecoyDB\\NIST20_NEG\\NIST20_HighRes_MSMS_N_20201130_decoys.MSP");
		try {
			Files.deleteIfExists(mspOutputPath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    DecimalFormat intensityFormat = new DecimalFormat("###.#");
		Pattern pattern = Pattern.compile(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}");
		Matcher matcher = null;
		String fidm = null;
		
		List<Path> dirList = new ArrayList<Path>();
		try {
			dirList = Files.find(Paths.get(fDir.getAbsolutePath()), 2,
					(filePath, fileAttr) -> (filePath.toString().contains("_DECOYS") && fileAttr.isDirectory())).
					sorted().collect(Collectors.toList());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		for(Path dirPath : dirList) {

			List<Path> decoyList = new ArrayList<Path>();
			try {
				decoyList = Files
					.find(dirPath, 3, (filePath, fileAttr) -> (filePath.toString().contains(File.separator + "decoys" + File.separator)
							&& filePath.toString().endsWith(".tsv")) && fileAttr.isRegularFile())
					.sorted().collect(Collectors.toList());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Path decoyPath : decoyList) {

				File decoy = decoyPath.toFile();
				if (decoy.isFile()) {

					matcher = pattern.matcher(decoy.getAbsolutePath());
					if (matcher.find())
						fidm = matcher.group(0);

					ArrayList<String> mspEntry = new ArrayList<String>();
					ArrayList<MsPoint> msms = new ArrayList<MsPoint>();
					String[][] decoyData = DelimitedTextParser.parseTextFile(decoy,
							MRC2ToolBoxConfiguration.getTabDelimiter());

					mspEntry.add(MSPField.NAME.getName() + ": " + fidm);
					for (int i = 1; i < decoyData.length; i++) {

						double mz = Double.parseDouble(decoyData[i][0]);
						double relInt = Double.parseDouble(decoyData[i][01]);
						msms.add(new MsPoint(mz, relInt));
					}
					MsPoint[] msmsNorm = MsUtils.normalizeAndSortMsPattern(msms);
					MsPoint parent = msmsNorm[msmsNorm.length - 1];
					mspEntry.add(MSPField.PRECURSORMZ.getName() + ": "
							+ MRC2ToolBoxConfiguration.getMzFormat().format(parent.getMz()));
					mspEntry.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(msmsNorm.length));
					for (MsPoint point : msms) {
						mspEntry.add(MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz()) + " "
								+ intensityFormat.format(point.getIntensity()));
					}
					mspEntry.add("\n");
					try {
						Files.write(mspOutputPath, mspEntry, StandardCharsets.UTF_8, StandardOpenOption.WRITE,
								StandardOpenOption.TRUNCATE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}	
		}	
	}
	
	private static void generateDecoy() throws IOException {
		File passatuttoOutputFolder = 
				new File("E:\\Eclipse\\git2\\MRC2Toolbox\\data\\mssearch\\hr_msms_pos");
						//+ "NIST20_HighRes_MSMS_P_20201001-213023_00000001_decoys");
		IOFileFilter featureDirDfilter = FileFilterUtils.makeDirectoryOnly(
				new RegexFileFilter(".+" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}$"));
		
		Collection<Path> batchDirs = new TreeSet<Path>();
		Collection<File> featureDirs = new TreeSet<File>();
//		Collection<File> featureDirs = FileUtils.listFilesAndDirs(
//				passatuttoOutputFolder,
//				DirectoryFileFilter.DIRECTORY,
//				featureDirDfilter);	
		
		Files.find(Paths.get(passatuttoOutputFolder.getAbsolutePath()), 1, (p, bfa) -> bfa.isDirectory() && 
				p.toFile().getName().contains("_decoys")).
				forEach(p -> batchDirs.add(p));
		System.out.println(Integer.toString(batchDirs.size()) + " batch dirs found.");
		
		File mspOutputFile = 
				new File("E:\\Eclipse\\git2\\MRC2Toolbox\\data\\mssearch\\hr_msms_pos\\"
						+ "NIST20_HighRes_MSMS_P_20201001-213023.MSP");
		Path mspOutputPath = Paths.get(mspOutputFile.getAbsolutePath());
		Files.deleteIfExists(mspOutputPath);
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    DecimalFormat intensityFormat = new DecimalFormat("###.#");
		IOFileFilter decoyFileFilter = FileFilterUtils.makeFileOnly(new RegexFileFilter(".+\\.tsv$"));
		Pattern pattern = Pattern.compile(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}$");
		Matcher matcher = null;
		String fidm = null;
		
		for(Path path : batchDirs) {
			Files.find(path, 1, (p, bfa) -> bfa.isDirectory() && 
					p.toFile().getName().contains(DataPrefix.MSMS_LIBRARY_ENTRY.getName())).
					forEach(f -> featureDirs.add(f.toFile()));
		}
		System.out.println(Integer.toString(featureDirs.size()) + " dirs found.");		

		for(File fDir : featureDirs) {
			
			File decoyDir = Paths.get(fDir.getAbsolutePath(), "decoys").toFile();
			if(decoyDir.exists() && decoyDir.canRead()) {
				matcher = pattern.matcher(decoyDir.getParent());
				if (matcher.find())
					fidm = matcher.group(0);	
				
				Collection<File> decoys = FileUtils.listFilesAndDirs(
						decoyDir,
						decoyFileFilter,
						null);
				
				for(File decoy : decoys) {
					if(decoy.isFile()) {
						
						ArrayList<String>mspEntry = new ArrayList<String>();
						ArrayList<MsPoint>msms = new ArrayList<MsPoint>();
						String[][] decoyData = DelimitedTextParser.parseTextFile(
								decoy, MRC2ToolBoxConfiguration.getTabDelimiter());
						
						mspEntry.add(MSPField.NAME.getName() + ": " + fidm);
						for(int i=1; i<decoyData.length; i++) {
							
							double mz = Double.parseDouble(decoyData[i][0]);
							double relInt = Double.parseDouble(decoyData[i][01]);
							msms.add(new MsPoint(mz, relInt));
						}
						MsPoint[] msmsNorm = MsUtils.normalizeAndSortMsPattern(msms);
						MsPoint parent = msmsNorm[msmsNorm.length - 1];
						mspEntry.add(MSPField.PRECURSORMZ.getName() + ": "
								+ MRC2ToolBoxConfiguration.getMzFormat().format(parent.getMz()));
						mspEntry.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(msmsNorm.length));
						for(MsPoint point : msms) {
							mspEntry.add(
								MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
								+ " " 
								+ intensityFormat.format(point.getIntensity()));
						}
						mspEntry.add("\n");
					    try {
							Files.write(mspOutputPath, 
									mspEntry, 
									StandardCharsets.UTF_8,
									StandardOpenOption.WRITE, 
									StandardOpenOption.TRUNCATE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
//						System.out.println(fidm + "\t" + decoy.getName());	
				}
			}
		}
	}
	
	private static void decryptString(String enc) throws Exception{
		
		//	String enc = "VjQHM8pRIlrAD0JIrbb5yg==";
		String pwdDec = null;
		try {
			pwdDec = UserUtils.decryptString(enc);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		System.out.println(pwdDec);
	}
	
	private static void listAgilentRawFiles() throws Exception{
		
		String rawDataDirectory = "Y:\\DataAnalysis\\_Reports\\EX00834 (ALS Metabolomics)\\A003 - Untargeted\\Raw data\\NEG";
		List<Path> pathList = new ArrayList<Path>();
		try {
			pathList = Files.find(Paths.get(rawDataDirectory),
					2, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
					collect(Collectors.toList());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(pathList.toString());
	}
	
	private static void uploadMsp() throws Exception{
		
		Connection conn = CompoundDbConnectionManager.getConnection();
		MINEMSPParser mpp = new MINEMSPParser(Polarity.Positive, conn);
		File mspFile = new File("E:\\DataAnalysis\\Databases\\MINE\\KEGG\\Positive_CFM_Spectra.msp");
		try {
			List<List<String>> dataBlocks = mpp.pareseInputFile(mspFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CompoundDbConnectionManager.releaseConnection(conn);
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {

			if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {

				System.out.println("Derby was shut down normally");
			} else {
				System.err.println("Derby shut down error!");
			}
		}
	}

	private void miscCode() {

		/**
		 * Load ISDB spectra from MGF files; compound data were manually loaded from CSV using Oracle SQL developer
		 */
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			Files.find(Paths.get("E:\\DataAnalysis\\Databases\\ISDB\\MGF"), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().endsWith(".mgf")) && fileAttr.isRegularFile())
					.forEach(path -> {
						try {
							File inputFile = new File(path.toString());
							List<TandemMassSpectrum> records = ISDBMGFParser.pareseInputFile(inputFile);
							ISDBMGFParser.insertSpectrumRecords(records, conn);
							System.out.println(inputFile.getName() + " was processed.");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					});
			CompoundDbConnectionManager.releaseConnection(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("MSMS upload completed");

		/**
		 * Load NIST structures from SDF files
		 */
		try {
			TreeSet<String>namesToUpdate = new TreeSet<String>();
			Connection conn = CompoundDbConnectionManager.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT D.NAME FROM NIST_COMPOUND_DATA D WHERE D.MOL IS NULL");
			ps.execute();
			ResultSet rs = ps.getResultSet();
			while (rs.next())
				namesToUpdate.add(rs.getString("NAME"));

			Files.find(Paths.get("E:\\DataAnalysis\\NIST17\\MSSEARCH\\Export\\MSMS\\SDF"), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().endsWith(".SDF")) && fileAttr.isRegularFile())
					.forEach(path -> {
						try {
							File inputFile = new File(path.toString());
							Map<String,String>molMap = new TreeMap<String,String>();
							Map<String,String>smilesMap = new TreeMap<String,String>();
							IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(inputFile), DefaultChemObjectBuilder.getInstance());
							while (reader.hasNext()) {

								IAtomContainer molecule = (IAtomContainer)reader.next();

								//	Use in second pass to deal with truncated names
								Optional<String> nm = namesToUpdate.stream().
										filter(n -> n.startsWith(molecule.getProperty(CDKConstants.TITLE))).findFirst();
								if(nm.isPresent()) {

								//	Use in first pass to deal with exactly matching names
								//if(namesToUpdate.contains(molecule.getTitle())) {

									StringWriter writer = new StringWriter();
									SDFWriter sdfWriter = new SDFWriter(writer);
							        sdfWriter.write(molecule);
							        sdfWriter.close();
									String smiles = "";
									try {
										smiles = smilesGenerator.create(molecule);
									} catch (CDKException e) {
										e.printStackTrace();
									}
									molMap.put(molecule.getProperty(CDKConstants.TITLE), writer.toString());
									smilesMap.put(molecule.getProperty(CDKConstants.TITLE), smiles);
									NISTMSPParser.uploadStructureData(molMap, smilesMap, conn);
								}
							}
							System.out.println(inputFile.getName() + " was processed.");
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					});
			CompoundDbConnectionManager.releaseConnection(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Structure upload completed");

		/**
		 * Load NIST spectral data
		 */
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();

			Files.find(Paths.get("E:\\DataAnalysis\\NIST17\\MSSEARCH\\Export\\MSMS\\RE"), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().endsWith(".MSP")) && fileAttr.isRegularFile())
					.forEach(path -> {
						try {
							File inputFile = new File(path.toString());
							List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(inputFile);
							for(List<String> chunk : mspChunks) {

								NISTTandemMassSpectrum msms = NISTMSPParser.parseNistMspDataSource(chunk);
								NISTMSPParser.insertSpectrumRecord(msms, null, conn);
							}
							System.out.println(inputFile.getName() + " was processed.");
							CompoundDbConnectionManager.releaseConnection(conn);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("MSMS upload completed");
		// End load NIST spectral data

		/**
		 * Enumarate NIST data fields
		 */
		TreeSet<String>fields = new TreeSet<String>();
		Pattern searchPattern = Pattern.compile("^([^:\\d]+): ");
		Pattern numPeaksPattern = Pattern.compile("(?i)^" + MSPField.NUM_PEAKS.getName() + ":?\\s+(\\d+)");
		//Matcher regexMatcher;

		try {
			Files.find(Paths.get("E:\\DataAnalysis\\NIST17\\MSSEARCH\\Export\\MSMS\\MSP"), Integer.MAX_VALUE,
					(filePath, fileAttr) -> (filePath.toString().endsWith(".MSP")) && fileAttr.isRegularFile())
					.forEach(path -> {
						try {
							List<List<String>> mspChunks = MsImportUtils.parseMspInputFile(new File(path.toString()));
							for(List<String> chunk : mspChunks) {

								for(String line : chunk) {

									Matcher regexMatcher = numPeaksPattern.matcher(line.trim());
									if (regexMatcher.find())
										break;
									else {
										regexMatcher = searchPattern.matcher(line.trim());
										if (regexMatcher.find())
											fields.add(regexMatcher.group(1));
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(StringUtils.join(fields, "\n"));
		//	End enumarate NIST data fields

		//************************************************

		try {
			Files.find(Paths.get("Y:\\DataAnalysis\\_Reports\\EX00616 (M-CHEAR Pilot)\\A003 - Untargeted\\Raw data\\NEG\\BATCH2"),
					Integer.MAX_VALUE, (filePath, fileAttr) -> (filePath.toString().contains("A003") && filePath.toString().toLowerCase().endsWith(".d")))
			        .forEach(path -> {
						try {
							FileUtils.deleteDirectory(new File(path.toString() + File.separator + "Results"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//************************************************
		try {
			Connection conn = CompoundDbConnectionManager.getConnection();
			String query = "SELECT * from employee";
			ResultSet rs = CompoundDbConnectionManager.executeQueryNoParams(conn, query);

			while (rs.next())
				System.out.println(rs.getString("EMP_NAME") + " ***");

			rs.close();
			CompoundDbConnectionManager.releaseConnection(conn);


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		TreeSet<String>properties = new TreeSet<String>();

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\CHEBI\\ChEBI_complete.sdf");
		IteratingSDFReader reader;
		try {
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());

			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				properties.addAll(molecule.getProperties().keySet().stream().map(String.class::cast).collect(Collectors.toList()));
				//	System.out.println(molecule.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		properties.stream().sorted().forEach(p -> System.out.println(p));
		//

		String smilesLabeled = "C1=CC=C2C(=C1)C(=C[15NH]2)C[C@@H](C(=O)O)[15NH2]";
		String smiles = "C1=CC=C2C(=C1)C(=CN2)CC(C(=O)O)N";

		 SmilesParser   sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
		 IsotopeFactory isoFactory = null;
		 IIsotope hydrogen = null;
		 try {
			isoFactory = Isotopes.getInstance();
			hydrogen = isoFactory.getMajorIsotope("H");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 try {
			IAtomContainer mlab   = sp.parseSmiles(smilesLabeled);

			MolecularFormula mf = new MolecularFormula();
			for(IAtom a : mlab.atoms())
				mf.addIsotope(a);

			//	Add implicit hydrogens
			int implicitHydrogensCount = AtomContainerManipulator.getImplicitHydrogenCount(mlab);
			mf.addIsotope(hydrogen, implicitHydrogensCount);
			IsotopePattern isoPattern = MsUtils.isotopePatternGenerator.getIsotopes(mf);

			System.out.println(MolecularFormulaManipulator.getString(mf));
			System.out.println(isoPattern.getMonoIsotope().getMass());

		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			IAtomContainer mlab   = sp.parseSmiles(smiles);
			MolecularFormula mfu = new MolecularFormula();
			for(IAtom a : mlab.atoms())
				mfu.addIsotope(a);

			//	Add implicit hydrogens
			int implicitHydrogensCount = AtomContainerManipulator.getImplicitHydrogenCount(mlab);
			mfu.addIsotope(hydrogen, implicitHydrogensCount);
			IsotopePattern isoPattern = MsUtils.isotopePatternGenerator.getIsotopes(mfu);

			System.out.println(MolecularFormulaManipulator.getString(mfu));
			System.out.println(MolecularFormulaManipulator.getTotalExactMass(mfu));

		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

/*
		if(ImputeBPCA.isAvailable())
			System.out.println("ImputeBPCA works");


		//	ChemicalModificationManipulator.createAdductSetFromElementaryAdducts(Polarity.Positive, 2, 2);
		String date = "2014-10-01T16:23:32.4596565-04:00";
		try {
			Date parsedDate = DATE_FORMATTER.parse(date);
			System.out.println(parsedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String inputname = "UNK_358.0352@.706 (2M-H) -0.707606";

		Pattern offsetFeaturePattern = Pattern.compile(" \\+|\\-\\s*\\d+\\.\\d+\\s*:*\\d*$");

		Matcher regexMatcher = offsetFeaturePattern.matcher(inputname);

		System.out.println(regexMatcher.find());*/
	}


}
