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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openscience.cdk.aromaticity.Aromaticity;
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

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

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

		try {
			addNumCarbonsToIsotopeDistributionFromCompoundDatabase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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

















