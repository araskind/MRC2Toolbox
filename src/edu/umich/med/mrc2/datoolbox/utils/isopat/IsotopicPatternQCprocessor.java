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

package edu.umich.med.mrc2.datoolbox.utils.isopat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.math4.legacy.distribution.EmpiricalDistribution;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaGenerator;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tautomers.InChITautomerGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IsotopicPatternReferenceBin;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class IsotopicPatternQCprocessor {

	public static String dataDir = "." + File.separator + "data" + File.separator;

	public static final String pubchemCidUrl = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/";
	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesParser smipar = new SmilesParser(builder);
	private static final SmilesGenerator smilesGenerator = 
			new SmilesGenerator(SmiFlavor.Isomeric);
	private static final InChITautomerGenerator tautgen = 
			new InChITautomerGenerator(InChITautomerGenerator.KETO_ENOL);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;
	private static Aromaticity aromaticity;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		File cefFolder = new File("Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\FBF-recursive\\POS\\BATCH01\\V2");
		try {
			//loadMsFbFFeatureDataFromCefFolder(cefFolder);
			//	loadInjectionsFromRawDataFolder(batchFolder);
			loadEX01426Features();
			//	loadEX01426Injections();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void loadEX01426Injections() {
		
//		File posRawDataFolder = new File(
//				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
//				+ "A003 - Untargeted\\Raw data\\POS");
//		
//		for(int i=8; i<9; i++) {
//			
//			String batch= "BATCH0" + Integer.toString(i);
//			File batchFolder = Paths.get(posRawDataFolder.getAbsolutePath(), batch).toFile();
//			try {
//				loadInjectionsFromRawDataFolder(batchFolder);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		}
//		File negRawDataFolder = new File(
//				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
//				+ "A003 - Untargeted\\Raw data\\NEG");
//		
//		for(int i=8; i<9; i++) {
//			
//			String batch= "BATCH0" + Integer.toString(i);
//			File batchFolder = Paths.get(negRawDataFolder.getAbsolutePath(), batch).toFile();
//			try {
//				loadInjectionsFromRawDataFolder(batchFolder);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		}
		File ionpNegRawDataFolder = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\Raw data\\NEG");
		
		for(int i=1; i<8; i++) {
			
			String batch= "BATCH0" + Integer.toString(i);
			File batchFolder = Paths.get(ionpNegRawDataFolder.getAbsolutePath(), batch).toFile();
			try {
				loadInjectionsFromRawDataFolder(batchFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	private static void loadInjectionsFromRawDataFolder(File rawDataFolder) throws Exception {
		
		if (rawDataFolder == null || !rawDataFolder.exists()) 
			return;

		IOFileFilter dotDfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		Collection<File> dataFiles = FileUtils.listFilesAndDirs(
				rawDataFolder,
				DirectoryFileFilter.DIRECTORY,
				dotDfilter);

		if (dataFiles.isEmpty()) 
			return;
		
		IDTDataCache.refreshAcquisitionMethodList();
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.INJECTION_MAP  " +
				"(INJECTION_ID, DATA_FILE_NAME, INJECTION_TIMESTAMP,  " +
				"ACQUISITION_METHOD_ID, INJECTION_VOLUME, EXPERIMENT_ID, INSTRUMENT_ID,  " +
				"ASSAY_ID, SAMPLE_ID, REP_NUMBER, POLARITY, SAMPLE_OR_REF_TYPE) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);		
		
		String DATA_FILE_NAME_PATTERN = 
				"^\\d{8}-(EX\\d{5})-(A\\d{3})-(IN\\d{4})-(S\\d{8})-.+(N|P)";
		String CONTROL_FILE_NAME_PATTERN = 
				"^\\d{8}-(EX\\d{5})-(A\\d{3})-(IN\\d{4})-([A-Z]+\\d+[A-Z]*\\d*)-(\\d{2})-(N|P)";
		Pattern dfPattern = Pattern.compile(DATA_FILE_NAME_PATTERN);
		Pattern controlPattern = Pattern.compile(CONTROL_FILE_NAME_PATTERN);
		Matcher fnMatcher = null;

		DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (File df : dataFiles) {
			
			String baseName = FilenameUtils.getBaseName(df.getName());
			
			String timeString = null;
			Date injectionTime = null;
			Double injectionVolume = null;
			String experimentId = null;
			String assayId = null;
			String instrumentId = null;
			String sampleId = null;
			String refSampleType = null;
			String acqMethodName = null;
			String acqMethodId = null;
			int replica = 1;
			String polarity = null;
			
			File sampleInfoFile = Paths.get(df.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
			if (!sampleInfoFile.exists()) {
				System.err.println("Missing sample_info.xml file for " + df.getName());
				continue;
			}

			Document sampleInfo = XmlUtils.readXmlFileWithEncoding(sampleInfoFile, StandardCharsets.UTF_8);
			if (sampleInfo == null) {
				System.err.println("Failed parsing sample_info.xml for " + df.getName());
				continue;
			}
			List<Element> fieldElements = sampleInfo.getRootElement().getChildren("Field");
			TreeMap<String, String> sampleData = new TreeMap<String, String>();
			for (Element fieldElement : fieldElements) {

				String name = fieldElement.getChild("Name").getText().trim();
				Element valueElement = fieldElement.getChild("Value");
				if (valueElement != null)
					sampleData.put(name, fieldElement.getChild("Value").getText().trim());
			}					
			if (sampleData.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName()) != null)
				timeString = sampleData.get(AgilentSampleInfoFields.ACQUISITION_TIME.getName());
			else {
				if (sampleData.get(AgilentSampleInfoFields.ACQTIME.getName()) != null)
					timeString = sampleData.get(AgilentSampleInfoFields.ACQTIME.getName());
			}
			if (timeString != null) {

				timeString = timeString.replace('T', ' ').replace('Z', ' ').trim();
				try {
					injectionTime = dFormat.parse(timeString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (sampleData.get(AgilentSampleInfoFields.INJ_VOL_UTF8.getName()) != null
					&& !sampleData.get(AgilentSampleInfoFields.INJ_VOL_UTF8.getName()).isEmpty()) {				
				try {
					injectionVolume = Double.parseDouble(sampleData.get(AgilentSampleInfoFields.INJ_VOL_UTF8.getName()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}					
			}
			if (sampleData.get(AgilentSampleInfoFields.METHOD.getName()) != null
					&& !sampleData.get(AgilentSampleInfoFields.METHOD.getName()).isEmpty()) {
				acqMethodName = new File(sampleData.get(AgilentSampleInfoFields.METHOD.getName())).getName();
				DataAcquisitionMethod method = IDTDataCache.getAcquisitionMethodByName(acqMethodName);
				if(method != null)
					acqMethodId = method.getId();
				else
					System.err.println("Unknown method " + acqMethodName);
			}
			//	Parse file name
			fnMatcher = null;
			if(baseName.contains("-S00")) {
				
				fnMatcher = dfPattern.matcher(baseName);
				if(fnMatcher.matches()) {
					
					experimentId = fnMatcher.group(1);
					assayId = fnMatcher.group(2);
					instrumentId = fnMatcher.group(3);
					sampleId = fnMatcher.group(4);
					polarity = fnMatcher.group(5);
					refSampleType = "SAMPLE";
				}
			}
			else {
				fnMatcher = controlPattern.matcher(baseName);
				if(fnMatcher.matches()) {
					
					experimentId = fnMatcher.group(1);
					assayId = fnMatcher.group(2);
					instrumentId = fnMatcher.group(3);
					sampleId = fnMatcher.group(4);
					polarity = fnMatcher.group(6);
					replica = Integer.parseInt(fnMatcher.group(5));
					if(sampleId != null) {
						ExperimentalSample refSample = ReferenceSamplesManager.getReferenceSampleById(sampleId);
						if(refSample != null)
							refSampleType = refSample.getMoTrPACQCSampleType().getName();
					}
				}
			}
			if(!fnMatcher.matches()) {
				System.err.println("Unable to parse file name " + df.getName());
				continue;
			}

			String newId = SQLUtils.getNextIdFromSequence(conn, 
					"COMPOUNDDB.INJECTION_MAP_SEQ",
					DataPrefix.INJECTION,
					"0",
					9);
			ps.setString(1, newId);
			ps.setString(2, baseName);
			
			if(injectionTime != null)
				ps.setDate(3, new java.sql.Date(injectionTime.getTime()));
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			if(acqMethodId != null)
				ps.setString(4, acqMethodId);
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			if(injectionVolume != null && injectionVolume > 0)
				ps.setDouble(5, injectionVolume);
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			ps.setString(6, experimentId);
			ps.setString(7, instrumentId);
			ps.setString(8, assayId);
			ps.setString(9, sampleId);
			ps.setInt(10, replica);
			ps.setString(11, polarity);
			
			if(refSampleType != null)
				ps.setString(12, refSampleType);
			else
				ps.setNull(12, java.sql.Types.NULL);
			
			ps.executeUpdate();
		}
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void loadEX01426Features() {
		
		File posRawDataFolder = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\FBF-recursive\\POS");
		
		for(int i=8; i<9; i++) {
			
			String batch= "BATCH0" + Integer.toString(i);
			File batchFolder = Paths.get(posRawDataFolder.getAbsolutePath(), batch).toFile();
			try {
				loadMsFbFFeatureDataFromCefFolder(batchFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		File negRawDataFolder = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A003 - Untargeted\\FBF-recursive\\NEG");
		
		for(int i=8; i<9; i++) {
			
			String batch= "BATCH0" + Integer.toString(i);
			File batchFolder = Paths.get(negRawDataFolder.getAbsolutePath(), batch).toFile();
			try {
				loadMsFbFFeatureDataFromCefFolder(batchFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		File ionpNegRawDataFolder = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01426 - Human EDTA Tranche 2 plasma W20001176L\\"
				+ "A049 - Central carbon metabolism profiling\\FBF-recursive\\NEG");
		
		for(int i=1; i<8; i++) {
			
			String batch= "BATCH0" + Integer.toString(i);
			File batchFolder = Paths.get(ionpNegRawDataFolder.getAbsolutePath(), batch).toFile();
			try {
				loadMsFbFFeatureDataFromCefFolder(batchFolder);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	private static void loadMsFbFFeatureDataFromCefFolder(File cefFolder) throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		
		ResultSet rs = null;
		
		String injQuery = 
				"SELECT INJECTION_ID FROM COMPOUNDDB.INJECTION_MAP WHERE DATA_FILE_NAME = ?";
		PreparedStatement injPs = conn.prepareStatement(injQuery);
		
		String query = 
				"INSERT INTO COMPOUNDDB.MS_FEATURES (FEATURE_ID,LIB_ID,INJECTION_ID,RT,"
				+ "RT_MIN,RT_MAX,AREA,HEIGHT,SCORE,SCORE_FLAG,FLAG_SEVERITY) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = 
				"INSERT INTO COMPOUNDDB.MS_PEAKS ("
				+ "FEATURE_ID,ISOTOPE_NUMBER,MZ,INTENSITY,REL_INTENSITY,ADDUCT) "
				+ "VALUES (?,?,?,?,?,?)";
		PreparedStatement updPs = conn.prepareStatement(updQuery);	
		
		List<Path>cefPathList = FIOUtils.findFilesByExtension(cefFolder.toPath(), "cef");
		if(cefPathList == null || cefPathList.isEmpty()) {
			
			System.err.println("No CEF files in " + cefFolder.getAbsolutePath());
			return;
		}		
		for(Path cefPath : cefPathList) {
			
			Collection<MsFeature>featureList = new ArrayList<MsFeature>();
			try {
				featureList = parseInputCefFile(cefPath.toFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(featureList.isEmpty()) {
				
				System.err.println("Unable to parse " + cefPath.getFileName());
				return;
			}
			String baseName = FileNameUtils.getBaseName(cefPath);
			injPs.setString(1, baseName);
			rs = injPs.executeQuery();
			String injectionId = null;
			while(rs.next())
				injectionId = rs.getString(1);
			
			rs.close();
			
			if(injectionId == null) {
				
				System.err.println("Injection not found for " + cefPath.getFileName());
				return;
			}
			System.out.println("Uploading MS features for " + cefPath.getFileName());
			for(MsFeature msf : featureList) {
				
				//	Feature
				String newId = SQLUtils.getNextIdFromSequence(conn, 
						"COMPOUNDDB.MS_FEATURE_SEQ",
						DataPrefix.MS_FEATURE,
						"0",
						11);
				
				ps.setString(1,newId); //	FEATURE_ID
				if(msf.getTargetId() != null)
					ps.setString(2, msf.getTargetId()); //	LIB_ID
				else
					ps.setNull(2, java.sql.Types.NULL);
				
				ps.setString(3, injectionId); //	INJECTION_ID
				ps.setDouble(4, msf.getRetentionTime()); //	RT
				ps.setDouble(5, msf.getRtRange().getMin()); //	RT_MIN
				ps.setDouble(6, msf.getRtRange().getMax()); //	RT_MAX
				ps.setDouble(7, msf.getArea()); //	AREA
				ps.setDouble(8, msf.getHeight()); //	HEIGHT
				
				if(msf.getBinnerAnnotation() != null)
					ps.setDouble(9, msf.getBinnerAnnotation().getMassError()); //	SCORE
				else
					ps.setNull(9, java.sql.Types.NULL);
				
				if(msf.getBinnerAnnotation() != null 
						&& msf.getBinnerAnnotation().getFeatureName() != null)
					ps.setString(10, msf.getBinnerAnnotation().getFeatureName()); //	SCORE_FLAG
				else
					ps.setNull(10, java.sql.Types.NULL);
				
				if(msf.getBinnerAnnotation() != null 
						&& msf.getBinnerAnnotation().getAnnotation() != null)
					ps.setString(11, msf.getBinnerAnnotation().getAnnotation()); //	FLAG_SEVERITY
				else
					ps.setNull(11, java.sql.Types.NULL);
				
				ps.executeUpdate();	
				
				//	Spectrum
				updPs.setString(1, newId);
				for(Adduct adduct : msf.getSpectrum().getAdducts()) {
						
					MsPoint[]rawMs = msf.getSpectrum().getMsForAdduct(adduct);
					MsPoint[]scaledMs = MsUtils.normalizeAndSortMsPattern(rawMs);
					for(int i=0; i<rawMs.length; i++) {
						
						updPs.setInt(2, i+1);
						updPs.setDouble(3, rawMs[i].getMz());
						updPs.setDouble(4, rawMs[i].getIntensity());
						updPs.setDouble(5, scaledMs[i].getIntensity());
						updPs.setString(6, adduct.getName());
						updPs.addBatch();
					}
				}
				updPs.executeBatch();
			}
		}
		injPs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}	
	
	private static void correctMassDefectForClAdducts() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = 
				"UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL  " +
				"SET MASS_DEFECT = ? WHERE MOL_FORMULA = ? ";
		PreparedStatement updPs = conn.prepareStatement(updQuery);	
		IMolecularFormula chlorineFormula = 
				MolecularFormulaManipulator.getMolecularFormula("Cl", builder);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String mf = rs.getString(1);			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);	
			formula.add(chlorineFormula);

			double massDefect = 
					MolecularFormulaManipulator.getMass(formula, MolecularFormulaManipulator.MonoIsotopic) 
					-  MolecularFormulaManipulator.getTotalMassNumber(formula);

			updPs.setDouble(1, massDefect);
			updPs.setString(2, mf);
			updPs.executeUpdate();						
		}
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void addIsotopeMassDifferencesAndMassDefectForClAdducts() throws Exception {
			
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA, EXACT_MASS FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL  " +
				"SET ISO_1_2_DIFF = ?, ISO_1_3_DIFF = ?, ISO_2_3_DIFF = ?, MASS_DEFECT = ? " +
				"WHERE MOL_FORMULA = ? ";
		PreparedStatement updPs = conn.prepareStatement(updQuery);	
		IMolecularFormula chlorineFormula = 
				MolecularFormulaManipulator.getMolecularFormula("Cl", builder);
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String mf = rs.getString(1);			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);	
			formula.add(chlorineFormula);
			Collection<MsPoint>isoPattern = 
					MsUtils.calculateIsotopeDistribution(formula, true);
			MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
			double nominalMass = MolecularFormulaManipulator.getTotalMassNumber(formula);
			double massDefect = rs.getDouble(2) - nominalMass;
			
			if(normPattern.length >=2)
				updPs.setDouble(1, normPattern[1].getMz() - normPattern[0].getMz() - 1.0d);
			else
				updPs.setNull(1, java.sql.Types.NULL);
			
			if(normPattern.length >=3) {
				updPs.setDouble(2, normPattern[2].getMz() - normPattern[0].getMz() - 2.0d);
				updPs.setDouble(3, normPattern[2].getMz() - normPattern[1].getMz() - 1.0d);
			}
			else {
				updPs.setNull(2, java.sql.Types.NULL);
				updPs.setNull(3, java.sql.Types.NULL);
			}
			updPs.setDouble(4, massDefect);
			updPs.setString(5, mf);
			updPs.executeUpdate();						
		}
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void calculateCHIsotopeDistributionForClAdductsFromCompoundDatabase() throws Exception {
		
		Set<String>formulas = new TreeSet<String>();		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			formulas.add(rs.getString(1));
		
		rs.close();
		
		query = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL  " +
				"SET CH_FORMULA = ?, CH_MASS = ?, CH_ISOTOPE_2 = ?,  " +
				"CH_ISOTOPE_3 = ?, CH_ISOTOPE_4 = ?, CH_ISOTOPE_5 = ?  " +
				"WHERE MOL_FORMULA = ? ";
		ps = conn.prepareStatement(query);	
		
		IMolecularFormula chlorineFormula = 
				MolecularFormulaManipulator.getMolecularFormula("Cl", builder);
		
		MolecularFormulaRange ranges = 
				IsotopicPatternUtils.createHydrocarbonElementRanges();
		MolecularFormulaGenerator mfg = null;
		double error = 2.0d;
		
		IIsotope carbon = null;
		try {
			carbon = Isotopes.getInstance().getMajorIsotope("C");
		} catch (IOException e) {
			e.printStackTrace();
		}
		IIsotope hydrogen = null;
		try {
			hydrogen = Isotopes.getInstance().getMajorIsotope("H");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String mf : formulas) {
			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);
			double exactMass = MolecularFormulaManipulator.getMass(
					formula, MolecularFormulaManipulator.MonoIsotopic);
			mfg = new MolecularFormulaGenerator(
					builder, exactMass - error, exactMass + error, ranges);
			
			IMolecularFormula chFormula = null;
			for (IMolecularFormula nextFormula = mfg.getNextFormula(); nextFormula != null; nextFormula = mfg.getNextFormula()) {
				
				int hCount = MolecularFormulaManipulator.getElementCount(nextFormula, hydrogen);
				int cCount = MolecularFormulaManipulator.getElementCount(nextFormula, carbon);
				if(hCount <= cCount * 2 + 2 && cCount < hCount * 2.5) {
					chFormula = nextFormula;
					break;
				}
			}		
			if(chFormula == null) {
				
				System.out.println("No CH formula for\t" + mf);
				continue;
			}
			chFormula = MolecularFormulaManipulator.getMolecularFormula(
					MolecularFormulaManipulator.getString(chFormula),builder);
			chFormula.add(chlorineFormula);
			Collection<MsPoint>isoPattern = 
					MsUtils.calculateIsotopeDistribution(chFormula, true);
			MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
			
			ps.setString(1, MolecularFormulaManipulator.getString(chFormula));
			ps.setDouble(2, MolecularFormulaManipulator.getMass(chFormula, MolecularFormulaManipulator.MonoIsotopic));
			
			if(normPattern.length >=2)
				ps.setDouble(3, normPattern[1].getIntensity());
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			if(normPattern.length >=3)
				ps.setDouble(4, normPattern[2].getIntensity());
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			if(normPattern.length >=4)
				ps.setDouble(5, normPattern[3].getIntensity());
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			if(normPattern.length >=5)
				ps.setDouble(6, normPattern[4].getIntensity());
			else
				ps.setNull(6, java.sql.Types.NULL);

			ps.setString(7, mf);
			
			ps.executeUpdate();						
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void testScoringProcedure() {
		
		//	Read Isotope model
		Collection<IsotopicPatternReferenceBin>model = 
				IsotopicPatternUtils.getIsotopicPatternModel(false);
		System.out.println(model.size() + " bins in the model");
		
		//	Read CEF file
		File inputCefFile = new File("Y:\\DataAnalysis\\_Reports\\EX01409 - Human EDTA Tranche 1 plasma W20000960M\\"
				+ "A049 - Central carbon metabolism profiling\\FBF-recursive\\NEG\\BATCH01\\V2-7ul\\"
				+ "20240805-EX01409-A049-IN0030-CS00000MP-04-N.cef");
		Collection<MsFeature>featureList = new ArrayList<MsFeature>();
		try {
			featureList = parseInputCefFile(inputCefFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(featureList.size() + " features to score");
		
		Collection<MsFeature>filtered = featureList.stream().
				filter(f -> f.getRetentionTime() > 3.0d).
				filter(f -> f.getMonoisotopicMz() < 500.0d).
				collect(Collectors.toList());
		
		//	Score features
		for(MsFeature feature : filtered) {
			
			double isoPatternScore = calculateIsoPatternScore(feature, model);
			System.out.println("Score " + Double.toString(isoPatternScore));
			System.out.println(MsUtils.getMsOneSpectrumForPrintout(feature.getSpectrum()));
		}
	}
	
	private static double calculateIsoPatternScore(
			MsFeature feature, 
			Collection<IsotopicPatternReferenceBin> model) {

		int numIonsToScore;
		for(Adduct adduct : feature.getSpectrum().getAdducts()) {
			
			MsPoint[] adductNorm = 
					MsUtils.normalizeAndSortMsPattern(
							feature.getSpectrum().getMsPointsForAdduct(adduct), 1.0);
			numIonsToScore = adductNorm.length;
			if(numIonsToScore > 5)
				numIonsToScore = 5;
			
			for(int i=1; i<numIonsToScore; i++) {
				
				double probability = evaluateIonIntensityProbability(
						adductNorm[i].getMz(), 
						adductNorm[i].getIntensity(),
						i, model, 10.0d);
				System.out.println("Ion " + i + " - " + MsUtils.spectrumMzFormat.format(probability));
			}
		}
		return 0;
	}
	
	private static double evaluateIonIntensityProbability(
			double mz, 
			double isotopeNormIntensity,
			int index, 
			Collection<IsotopicPatternReferenceBin> model, 
			double massHalfWindow) {
		List<Double>modelIntensities = new ArrayList<Double>();
		Range massRange = new Range(mz - massHalfWindow, mz + massHalfWindow);
		List<IsotopicPatternReferenceBin>modelSource = 
			model.stream().filter(b -> massRange.contains(b.getExactMass())).
			filter(b -> Objects.nonNull(b.getIsotopeRelativeIntensities()[index])).
			collect(Collectors.toList());
		if(modelSource.size() < 3)
			return 0.0d;
		
		modelSource.stream().
		forEach(b -> modelIntensities.addAll(
				Collections.nCopies(b.getNumberOfRepeats(), 
						b.getIsotopeRelativeIntensities()[index])));
		double[] arr = modelIntensities.stream().mapToDouble(Double::doubleValue).toArray();
		
		//	EmpiricalDistribution ed = EmpiricalDistribution.from(Math.round(arr.length / 5), arr);
		EmpiricalDistribution ed = EmpiricalDistribution.from(20, arr);
		
		return ed.probability(isotopeNormIntensity * 0.8d, isotopeNormIntensity * 1.2d);
	}

	private static Collection<MsFeature>parseInputCefFile(File inputCefFile) throws Exception {

		if(inputCefFile == null || !inputCefFile.exists())
			return null;

		Document cefDocument = XmlUtils.readXmlFile(inputCefFile);
		if(cefDocument == null)
			return null;
		
		Collection<MsFeature> inputFeatureList = new ArrayList<MsFeature>();
		List<Element>featureNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");

		for (Element cpdElement : featureNodes) {

			MsFeature feature = parseCefCompoundElement(cpdElement);
			if(feature != null)
				inputFeatureList.add(feature);
		}
		return inputFeatureList;
	}
	
	private static MsFeature parseCefCompoundElement(Element cpdElement) throws Exception {
		
		Element location = cpdElement.getChild("Location");
		double rt = location.getAttribute("rt").getDoubleValue();
		double neutralMass = 0.0d;
		if(location.getAttribute("m") != null)
			neutralMass = location.getAttribute("m").getDoubleValue();

		String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
			MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
			MRC2ToolBoxConfiguration.getRtFormat().format(rt);		
		MsFeature feature = new MsFeature(name, rt);
		feature.setNeutralMass(neutralMass);
		if(location.getAttribute("a") != null)
			feature.setArea(location.getAttribute("a").getDoubleValue());

		if(location.getAttribute("y") != null)
			feature.setHeight(location.getAttribute("y").getDoubleValue());
		
		parseSpectra(cpdElement, feature);
		if(feature.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())) {
			
			name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(feature.getMonoisotopicMz()) + "_" +
					MRC2ToolBoxConfiguration.getRtFormat().format(feature.getRetentionTime());
			if(feature.getSpectrum() != null && feature.getSpectrum().getPrimaryAdduct() != null)
				name += " " + feature.getSpectrum().getPrimaryAdduct().getName();
			
			feature.setName(name);
		}
		extractScore(cpdElement, feature);		
		return feature;
	}
	
	private static void extractScore(Element cpdElement,  MsFeature feature) throws DataConversionException {
		
		Element resultsElement = cpdElement.getChild("Results");
		if(resultsElement == null)
			return;
		
		Element moleculeElement = resultsElement.getChild("Molecule");						
		if(moleculeElement == null)
			return;

		if(moleculeElement.getChild("Database") != null) {
			
			Element accElement = moleculeElement.getChild("Database").getChild("Accession");
			String tgtId = null;
			if(accElement != null)
				tgtId = accElement.getAttributeValue("id");
			
			if(tgtId != null)
				feature.setTargetId(tgtId);
		}				
		Element matchScoreList = moleculeElement.getChild("MatchScores");						
		if(matchScoreList == null)
			return;
		
		List<Element> scoreElements = matchScoreList.getChildren("Match");
		if(scoreElements.isEmpty())
			return;
		
		double tgtScore = 0.0d;
		String flag = null;
		String flagSeverity = null;
		for(Element scoreElement : scoreElements) {
			
			if(scoreElement.getAttributeValue("algo") != null 
					&& scoreElement.getAttributeValue("algo").equals("tgt")) {
				tgtScore = scoreElement.getAttribute("score").getDoubleValue();
				flag = scoreElement.getAttributeValue("tgtFlagsString");
				flagSeverity = scoreElement.getAttributeValue("tgtFlagsSeverity");
			}
		}	
		//	Just a workaround not to create new objects in MS feature
		BinnerAnnotation ba = new BinnerAnnotation(null, flag, flagSeverity);
		ba.setMassError(tgtScore);
		feature.setBinnerAnnotation(ba);
	}
	
	private static void parseSpectra(Element cpdElement,  MsFeature feature) throws DataConversionException {
		
		List<Element> spectrumElements = cpdElement.getChildren("Spectrum");	
		MassSpectrum spectrum = new MassSpectrum();		
		Element forPeakWidthElement = null;
		for(Element spectrumElement : spectrumElements) {
			
			String spectrumType = spectrumElement.getAttributeValue("type");
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())) {
				forPeakWidthElement = spectrumElement;
				break;
			}
			if(spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())
					&& forPeakWidthElement == null) {
				forPeakWidthElement = spectrumElement;
			}
		}
		//	Add RT range
		if(forPeakWidthElement != null) {
			
			if(forPeakWidthElement.getChild("RTRanges") != null
					&& !forPeakWidthElement.getChild("RTRanges").getChildren().isEmpty()) {
				Element rtRangeElement = 
						forPeakWidthElement.getChild("RTRanges").getChild("RTRange");
				if(rtRangeElement != null) {
					
					double min = rtRangeElement.getAttribute("min").getDoubleValue();
					double max = rtRangeElement.getAttribute("max").getDoubleValue();
					if(min < max) 
						feature.setRtRange(new Range(min, max));						
				}
			}
		}
		for(Element spectrumElement : spectrumElements) {

			String spectrumType = spectrumElement.getAttributeValue("type");
			
			if(spectrumType.equals(AgilentCefFields.MS1_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.FBF_SPECTRUM.getName())
					|| spectrumType.equals(AgilentCefFields.MFE_SPECTRUM.getName())) {
				
				String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
				Polarity pol = null;
				if(sign.equals("+"))
					pol = Polarity.Positive;

				if(sign.equals("-"))
					pol = Polarity.Negative;
				
				feature.setPolarity(pol);
				
				Map<Adduct,Collection<MsPoint>>adductMap = 
						parseMsOneSpectrumElement(spectrumElement);
				adductMap.entrySet().stream().
					forEach(e -> spectrum.addSpectrumForAdduct(e.getKey(), e.getValue()));
				
				String detectionAlgorithm = spectrumElement.getAttributeValue("cpdAlgo");
				if(detectionAlgorithm != null && !detectionAlgorithm.isEmpty())
					spectrum.setDetectionAlgorithm(detectionAlgorithm);
			}
		}
		feature.setSpectrum(spectrum);
	}
	
	private static Map<Adduct,Collection<MsPoint>>parseMsOneSpectrumElement(Element spectrumElement) 
			throws DataConversionException{

		String sign = spectrumElement.getChild("MSDetails").getAttributeValue("p");
		Polarity pol = null;
		if(sign.equals("+"))
			pol = Polarity.Positive;
	
		if(sign.equals("-"))
			pol = Polarity.Negative;
	
		Map<Adduct,Collection<MsPoint>>cmMap = 
				new TreeMap<Adduct,Collection<MsPoint>>();
		List<Element> peaks = spectrumElement.getChild("MSPeaks").getChildren("p");
	
		//	Check if no adducts are specified
		if(peaks.get(0).getAttribute("s") == null 
				|| peaks.get(0).getAttributeValue("s").isEmpty()) {
			
			Set<MsPoint> points = new TreeSet<MsPoint>(MsUtils.mzSorter);
			for(Element peak : peaks) {
				points.add(new MsPoint(
						peak.getAttribute("x").getDoubleValue(),
						peak.getAttribute("y").getDoubleValue()));
			}
			cmMap.put(AdductManager.getDefaultAdductForPolarity(pol), points);
			return cmMap;
		}
		Map<String,Collection<MsPoint>>adductMap = 
				new TreeMap<String,Collection<MsPoint>>();
		for(Element peak : peaks) {
						
			String adduct = peak.getAttributeValue("s").replaceAll("\\+[0-9]+$", "");
			if(!adductMap.containsKey(adduct))
				adductMap.put(adduct, new TreeSet<MsPoint>(MsUtils.mzSorter));
	
			adductMap.get(adduct).add(new MsPoint(
					peak.getAttribute("x").getDoubleValue(),
					peak.getAttribute("y").getDoubleValue()));
		}
		for (Entry<String, Collection<MsPoint>> entry : adductMap.entrySet()) {
	
			Adduct adduct = AdductManager.getAdductByCefNotation(entry.getKey());
			if(adduct != null)
				cmMap.put(adduct, entry.getValue());
		}
		return cmMap;
	}
	
	private static void calculateIsotopeDistributionForClAdductFromCompoundDatabase() throws Exception {
		
		Map<String,Integer>formulasWithCounts = new TreeMap<String,Integer>();
		try {
			formulasWithCounts = IsotopicPatternUtils.getCompoundMsReadyFormulasWithCounts();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Integer[]> ranges = createCpdDbElementRanges();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS_CL ("
				+ "MOL_FORMULA, NUM_REPEATS, EXACT_MASS, ISOTOPE_2, "
				+ "ISOTOPE_3, ISOTOPE_4, ISOTOPE_5, ISOTOPE_6, NUM_CARBONS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		double upperMassCutoff = 1200.0d;
		IIsotope chlorine = null;
		try {
			chlorine = Isotopes.getInstance().getMajorIsotope("Cl");
		} catch (IOException e) {
			e.printStackTrace();
		}	
		IMolecularFormula chlorineFormula = 
				MolecularFormulaManipulator.getMolecularFormula("Cl", builder);
		
		for(Entry<String, Integer> pair : formulasWithCounts.entrySet()) {
			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(pair.getKey(), builder);
			double exactMass = MolecularFormulaManipulator.getMass(
					formula, MolecularFormulaManipulator.MonoIsotopic);
			if(exactMass < upperMassCutoff 
					&& (formula.getCharge() == null || formula.getCharge() == 0)
					&& formula.getIsotopeCount(chlorine) == 0
					&& isFormulainElementRange(formula, ranges)) {
				
				Integer carbonCounts = MolFormulaUtils.getCarbonCounts(pair.getKey());	
				formula.add(chlorineFormula);
				Collection<MsPoint>isoPattern = 
						MsUtils.calculateIsotopeDistribution(formula, true);
				MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
				
				ps.setString(1, pair.getKey());
				ps.setInt(2, pair.getValue());
				ps.setDouble(3, exactMass);
				
				if(normPattern.length >=2)
					ps.setDouble(4, normPattern[1].getIntensity());
				else
					ps.setNull(4, java.sql.Types.NULL);
				
				if(normPattern.length >=3)
					ps.setDouble(5, normPattern[2].getIntensity());
				else
					ps.setNull(5, java.sql.Types.NULL);
				
				if(normPattern.length >=4)
					ps.setDouble(6, normPattern[3].getIntensity());
				else
					ps.setNull(6, java.sql.Types.NULL);
				
				if(normPattern.length >=5)
					ps.setDouble(7, normPattern[4].getIntensity());
				else
					ps.setNull(7, java.sql.Types.NULL);
				
				if(normPattern.length >=6)
					ps.setDouble(8, normPattern[5].getIntensity());
				else
					ps.setNull(8, java.sql.Types.NULL);
				
				if(carbonCounts != null)
					ps.setInt(9, carbonCounts);
				else
					ps.setNull(9, java.sql.Types.NULL);
				
				ps.executeUpdate();
			}			
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void addNumCarbonsToIsotopeDistributionFromCompoundDatabase() throws Exception {
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT MOL_FORMULA FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS";
		Collection<String>formulas = new TreeSet<String>();
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			formulas.add(rs.getString(1));
		
		rs.close();
		
		query = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS "
				+ "SET NUM_CARBONS = ? WHERE MOL_FORMULA = ?";
		ps = conn.prepareStatement(query);
		for(String formula : formulas) {
			
			Integer carbonCounts = MolFormulaUtils.getCarbonCounts(formula);
			if(carbonCounts != null) {
				
				ps.setInt(1, carbonCounts);
				ps.setString(2, formula);
				ps.executeUpdate();
			}
		}		
		ps.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private static void addMassDefect() throws Exception {
			
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA, EXACT_MASS FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		
		String updQuery = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS  " +
				"SET MASS_DEFECT = ? WHERE MOL_FORMULA = ? ";
		PreparedStatement updPs = conn.prepareStatement(updQuery);		
		
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			
			String mf = rs.getString(1);
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);
			double nominalMass = MolecularFormulaManipulator.getTotalMassNumber(formula);
			double massDefect = rs.getDouble(2) - nominalMass;

			updPs.setDouble(1, massDefect);
			updPs.setString(2, mf);
			updPs.executeUpdate();						
		}	
		rs.close();
		ps.close();
		updPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void addIsotopeMassDifferences() throws Exception {
		
		Set<String>formulas = new TreeSet<String>();		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			formulas.add(rs.getString(1));
		
		rs.close();
		
		query = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS  " +
				"SET ISO_1_2_DIFF = ?, ISO_1_3_DIFF = ?, ISO_2_3_DIFF = ? " +
				"WHERE MOL_FORMULA = ? ";
		ps = conn.prepareStatement(query);		
		
		for(String mf : formulas) {
			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);						
			Collection<MsPoint>isoPattern = 
					MsUtils.calculateIsotopeDistribution(formula, true);
			MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
			
			if(normPattern.length >=2)
				ps.setDouble(1, normPattern[1].getMz() - normPattern[0].getMz() - 1.0d);
			else
				ps.setNull(1, java.sql.Types.NULL);
			
			if(normPattern.length >=3) {
				ps.setDouble(2, normPattern[2].getMz() - normPattern[0].getMz() - 2.0d);
				ps.setDouble(3, normPattern[2].getMz() - normPattern[1].getMz() - 1.0d);
			}
			else {
				ps.setNull(2, java.sql.Types.NULL);
				ps.setNull(3, java.sql.Types.NULL);
			}
			ps.setString(4, mf);
			ps.executeUpdate();						
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void calculateCHIsotopeDistributionFromCompoundDatabase() throws Exception {
		
		Set<String>formulas = new TreeSet<String>();		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA FROM COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS "
				+ "WHERE MOL_FORMULA LIKE 'C%' AND EXACT_MASS < 1500";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			formulas.add(rs.getString(1));
		
		rs.close();
		
		query = "UPDATE COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS  " +
				"SET CH_FORMULA = ?, CH_MASS = ?, CH_ISOTOPE_2 = ?,  " +
				"CH_ISOTOPE_3 = ?, CH_ISOTOPE_4 = ?, CH_ISOTOPE_5 = ?  " +
				"WHERE MOL_FORMULA = ? ";
		ps = conn.prepareStatement(query);		
		MolecularFormulaRange ranges = 
				IsotopicPatternUtils.createHydrocarbonElementRanges();
		MolecularFormulaGenerator mfg = null;
		double error = 2.0d;
		
		IIsotope carbon = null;
		try {
			carbon = Isotopes.getInstance().getMajorIsotope("C");
		} catch (IOException e) {
			e.printStackTrace();
		}
		IIsotope hydrogen = null;
		try {
			hydrogen = Isotopes.getInstance().getMajorIsotope("H");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String mf : formulas) {
			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(mf, builder);
			double exactMass = MolecularFormulaManipulator.getMass(
					formula, MolecularFormulaManipulator.MonoIsotopic);
			mfg = new MolecularFormulaGenerator(
					builder, exactMass - error, exactMass + error, ranges);
			
			IMolecularFormula chFormula = null;
			for (IMolecularFormula nextFormula = mfg.getNextFormula(); nextFormula != null; nextFormula = mfg.getNextFormula()) {
				
				int hCount = MolecularFormulaManipulator.getElementCount(nextFormula, hydrogen);
				int cCount = MolecularFormulaManipulator.getElementCount(nextFormula, carbon);
				if(hCount <= cCount * 2 + 2 && cCount < hCount * 2.5) {
					chFormula = nextFormula;
					break;
				}
			}		
			if(chFormula == null) {
				
				System.out.println("No CH formula for\t" + mf);
				continue;
			}
			chFormula = MolecularFormulaManipulator.getMolecularFormula(
					MolecularFormulaManipulator.getString(chFormula),builder);							
			Collection<MsPoint>isoPattern = 
					MsUtils.calculateIsotopeDistribution(chFormula, true);
			MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
			
			ps.setString(1, MolecularFormulaManipulator.getString(chFormula));
			ps.setDouble(2, MolecularFormulaManipulator.getMass(chFormula, MolecularFormulaManipulator.MonoIsotopic));
			
			if(normPattern.length >=2)
				ps.setDouble(3, normPattern[1].getIntensity());
			else
				ps.setNull(3, java.sql.Types.NULL);
			
			if(normPattern.length >=3)
				ps.setDouble(4, normPattern[2].getIntensity());
			else
				ps.setNull(4, java.sql.Types.NULL);
			
			if(normPattern.length >=4)
				ps.setDouble(5, normPattern[3].getIntensity());
			else
				ps.setNull(5, java.sql.Types.NULL);
			
			if(normPattern.length >=5)
				ps.setDouble(6, normPattern[4].getIntensity());
			else
				ps.setNull(6, java.sql.Types.NULL);

			ps.setString(7, mf);
			
			ps.executeUpdate();						
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static void calculateIsotopeDistributionFromCompoundDatabase() throws Exception {
		
		Map<String,Integer>formulasWithCounts = new TreeMap<String,Integer>();
		try {
			formulasWithCounts = IsotopicPatternUtils.getCompoundMsReadyFormulasWithCounts();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Integer[]> ranges = createCpdDbElementRanges();
		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"INSERT INTO COMPOUNDDB.FORMULA_ISOTOPIC_PATTERNS ("
				+ "MOL_FORMULA, NUM_REPEATS, EXACT_MASS, ISOTOPE_2, "
				+ "ISOTOPE_3, ISOTOPE_4, ISOTOPE_5, ISOTOPE_6) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		
		double upperMassCutoff = 1200.0d;
		
		for(Entry<String, Integer> pair : formulasWithCounts.entrySet()) {
			
			IMolecularFormula formula = 
					MolecularFormulaManipulator.getMolecularFormula(pair.getKey(), builder);
			double exactMass = MolecularFormulaManipulator.getMass(
					formula, MolecularFormulaManipulator.MonoIsotopic);
			if(exactMass < upperMassCutoff && isFormulainElementRange(formula, ranges)) {
				
				Collection<MsPoint>isoPattern = 
						MsUtils.calculateIsotopeDistribution(formula, true);
				MsPoint[] normPattern = MsUtils.normalizeAndSortMsPattern(isoPattern, 1.0d);
				
				ps.setString(1, pair.getKey());
				ps.setInt(2, pair.getValue());
				ps.setDouble(3, exactMass);
				
				if(normPattern.length >=2)
					ps.setDouble(4, normPattern[1].getIntensity());
				else
					ps.setNull(4, java.sql.Types.NULL);
				
				if(normPattern.length >=3)
					ps.setDouble(5, normPattern[2].getIntensity());
				else
					ps.setNull(5, java.sql.Types.NULL);
				
				if(normPattern.length >=4)
					ps.setDouble(6, normPattern[3].getIntensity());
				else
					ps.setNull(6, java.sql.Types.NULL);
				
				if(normPattern.length >=5)
					ps.setDouble(7, normPattern[4].getIntensity());
				else
					ps.setNull(7, java.sql.Types.NULL);
				
				if(normPattern.length >=6)
					ps.setDouble(8, normPattern[5].getIntensity());
				else
					ps.setNull(8, java.sql.Types.NULL);
				
				ps.executeUpdate();
			}			
		}	
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	private static boolean isFormulainElementRange(
					IMolecularFormula formula,
					Map<String, Integer[]>ranges) {
		
		Set<String> elementSymbols = 
				StreamSupport.stream(formula.isotopes().spliterator(), false).
			map(i -> i.getSymbol()).collect(Collectors.toSet());
		
		int isoCount, min, max;
		for(IIsotope isotope : formula.isotopes()) {
			
			if(!ranges.keySet().contains(isotope.getSymbol()))
				return false;
			
			isoCount = formula.getIsotopeCount();
			min = ranges.get(isotope.getSymbol())[0];
			max = ranges.get(isotope.getSymbol())[1];
			if(isoCount > max || isoCount < min)
				return false;			
		}		
		return true;
	}
	
	private static Map<String, Integer[]> createCpdDbElementRanges() {
		
		Map<String, Integer[]> ranges = new TreeMap<String, Integer[]>();
		ranges.put("H", new Integer[]{0, 406});
		ranges.put("C", new Integer[]{0, 264});
		ranges.put("N", new Integer[]{0, 80});
		ranges.put("S", new Integer[]{0, 8});
		ranges.put("P", new Integer[]{0, 8});
		ranges.put("O", new Integer[]{0, 77});
		ranges.put("I", new Integer[]{0, 7});		
		return ranges;
	}
	
	private static void summarizeElementCountsInCompoundDatabase() {
		
		Map<String,Integer>formulasWithCounts = new TreeMap<String,Integer>();
		Map<String,Integer>elementMaxCounts = new TreeMap<String,Integer>();
		try {
			formulasWithCounts = IsotopicPatternUtils.getCompoundMsReadyFormulasWithCounts();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String mfString : formulasWithCounts.keySet()) {
			
			IMolecularFormula molFormula = 
					MolecularFormulaManipulator.getMolecularFormula(mfString, builder);
			for(IIsotope isotope : molFormula.isotopes()) {
				
				if(!elementMaxCounts.containsKey(isotope.getSymbol()))
					elementMaxCounts.put(isotope.getSymbol(), 0);
				
				if(elementMaxCounts.get(isotope.getSymbol()) < molFormula.getIsotopeCount(isotope))
					elementMaxCounts.put(isotope.getSymbol(), molFormula.getIsotopeCount(isotope));
			}
		}	
		ArrayList<String>output = new ArrayList<String>();
		for(Entry<String, Integer> pair : elementMaxCounts.entrySet())
			output.add(pair.getKey() + "\t" + Integer.toString(pair.getValue()));
	
		Path outputPath = Paths.get("E:\\DataAnalysis\\Isotopes\\cpddbMaxMsReadyElementCounts.txt");
		try {
		    Files.write(outputPath, 
		    		output,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.APPEND);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static void createDefaultElementLimitsFile() {
		
		MolecularFormulaRange defaultRanges = 
				IsotopicPatternUtils.createDefaultElementRanges();
		
		File outputFile =  Paths.get(MRC2ToolBoxCore.configDir, 
				IsotopicPatternUtils.DEFAULT_ELEMET_LIMITS_FILE_NAME).toFile();
		IsotopicPatternUtils.saveMolecularFormulaRangesToXML(defaultRanges, outputFile);
	}
}

















