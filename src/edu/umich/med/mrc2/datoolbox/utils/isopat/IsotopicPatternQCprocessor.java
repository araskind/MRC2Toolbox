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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.math4.legacy.distribution.EmpiricalDistribution;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.config.Isotopes;
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
import edu.umich.med.mrc2.datoolbox.data.IsotopicPatternReferenceBin;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
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

		try {
			testScoringProcedure();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
		return feature;
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

















