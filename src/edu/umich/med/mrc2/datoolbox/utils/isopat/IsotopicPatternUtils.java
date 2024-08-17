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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IIsotope;

import edu.umich.med.mrc2.datoolbox.data.IsotopicPatternReferenceBin;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class IsotopicPatternUtils {

	public static final String ELEMENT_LIMITS_ROOT = "ElementNumberLimits";
	public static final String ISOTOPE_ELEMENT = "Isotope";
	public static final String NAME_ATTRIBUTE = "IsotopeName";
	public static final String MIN_COUNTS_ATTRIBUTE = "min";
	public static final String MAX_COUNTS_ATTRIBUTE = "max";
	public static final String DEFAULT_ELEMET_LIMITS_FILE_NAME = "defaultElementLimits.xml";
	
	public static final String REGULAR_ISOTOPIC_PATTERN_MODEL_FILE_NAME = "isotopesByFormulaModel.txt";
	public static final String CL_ADDUCT_ISOTOPIC_PATTERN_MODEL_FILE_NAME = "isotopesClAdductByFormulaModel.txt";
	
	public static Map<String,Integer>getCompoundFormulasWithCounts() throws Exception{
		
		Map<String,Integer>formulasWithCounts = new TreeMap<String,Integer>();		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MOL_FORMULA, COUNT(ACCESSION) AS DUPE_COUNT " +
				"FROM COMPOUNDDB.COMPOUND_DATA " +
				"WHERE MOL_FORMULA IS NOT NULL " +
				"GROUP BY MOL_FORMULA " +
				"HAVING COUNT(ACCESSION) > 1 " +
				"ORDER BY MOL_FORMULA ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			formulasWithCounts.put(rs.getString(1), rs.getInt(2));
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		return formulasWithCounts;
	}
	
	public static Map<String,Integer>getCompoundMsReadyFormulasWithCounts() throws Exception{
		
		Map<String,Integer>formulasWithCounts = new TreeMap<String,Integer>();		
		Connection conn = ConnectionManager.getConnection();
		String query = 
				"SELECT MS_READY_MOL_FORMULA, COUNT(ACCESSION) AS DUPE_COUNT " +
				"FROM COMPOUNDDB.COMPOUND_DATA " +
				"WHERE MS_READY_MOL_FORMULA IS NOT NULL " +
				"GROUP BY MS_READY_MOL_FORMULA " +
				"HAVING COUNT(ACCESSION) > 1 " +
				"ORDER BY MS_READY_MOL_FORMULA ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) 
			formulasWithCounts.put(rs.getString(1), rs.getInt(2));
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		
		return formulasWithCounts;
	}
		
	public static void saveMolecularFormulaRangesToXML(
			MolecularFormulaRange ranges,
			File outputFile) {
		
        Document document = new Document();
        Element elementCountsListRoot = new Element(ELEMENT_LIMITS_ROOT);
        elementCountsListRoot.setAttribute("version", "1.0.0.0");
        document.addContent(elementCountsListRoot);
        
        for(IIsotope isotope : ranges.isotopes()) {
        	
    		Element isotopeElement = new Element(ISOTOPE_ELEMENT);
    		isotopeElement.setAttribute(NAME_ATTRIBUTE, isotope.getSymbol());
    		isotopeElement.setAttribute(MIN_COUNTS_ATTRIBUTE, 
    			Integer.toString(ranges.getIsotopeCountMin(isotope)));
    		isotopeElement.setAttribute(MAX_COUNTS_ATTRIBUTE, 
    				Integer.toString(ranges.getIsotopeCountMax(isotope)));
    		
    		elementCountsListRoot.addContent(isotopeElement);
        }       
        try {
            FileWriter writer = new FileWriter(outputFile, false);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static MolecularFormulaRange readMolecularFormulaRangesFromXML(File inputFile) {
		
		if(inputFile == null || !inputFile.exists())
			return null;
		
		MolecularFormulaRange ranges = new MolecularFormulaRange();
		IsotopeFactory ifac = null;
		try {
			ifac = Isotopes.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ifac == null)
			return null;	
		
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(inputFile);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element rootNode = doc.getRootElement();
		List<Element> list = 
				rootNode.getChildren(ISOTOPE_ELEMENT);
		for(Element isotopeElement : list) {

			IIsotope isotope = ifac.getMajorIsotope(
					isotopeElement.getAttributeValue(NAME_ATTRIBUTE));
			int min = Integer.parseInt(
					isotopeElement.getAttributeValue(MIN_COUNTS_ATTRIBUTE));
			int max = Integer.parseInt(
					isotopeElement.getAttributeValue(MAX_COUNTS_ATTRIBUTE));
			ranges.addIsotope(isotope, min, max);
		}
		return ranges;
	}
	
	public static MolecularFormulaRange createDefaultElementRanges() {
		
		MolecularFormulaRange ranges = new MolecularFormulaRange();
		IsotopeFactory ifac = null;
		try {
			ifac = Isotopes.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ifac == null)
			return null;	

		ranges.addIsotope(ifac.getMajorIsotope("H"), 0, 126);
		ranges.addIsotope(ifac.getMajorIsotope("C"), 0, 78);
		ranges.addIsotope(ifac.getMajorIsotope("N"), 0, 20);
		ranges.addIsotope(ifac.getMajorIsotope("S"), 0, 14);
		ranges.addIsotope(ifac.getMajorIsotope("P"), 0, 9);
		ranges.addIsotope(ifac.getMajorIsotope("O"), 0, 27);
		ranges.addIsotope(ifac.getMajorIsotope("Na"), 0, 2);
		ranges.addIsotope(ifac.getMajorIsotope("K"), 0, 2);
		ranges.addIsotope(ifac.getMajorIsotope("Ca"), 0, 2);
		ranges.addIsotope(ifac.getMajorIsotope("Mg"), 0, 2);
		ranges.addIsotope(ifac.getMajorIsotope("Cl"), 0, 3);
		ranges.addIsotope(ifac.getMajorIsotope("I"), 0, 3);
		
		return ranges;
	}
	
	public static MolecularFormulaRange getDefaultElementRanges() {
		
		File defaultsFile =  Paths.get(
				MRC2ToolBoxCore.configDir, 
				DEFAULT_ELEMET_LIMITS_FILE_NAME).toFile();
		
		if(defaultsFile == null || !defaultsFile.exists()) {
			
			MolecularFormulaRange defaults = createDefaultElementRanges(); 
			saveMolecularFormulaRangesToXML(defaults, defaultsFile);
			return defaults;
		}
		else
			return readMolecularFormulaRangesFromXML(defaultsFile);
	}
	
	public static Collection<IsotopicPatternReferenceBin>getIsotopicPatternModel(
			boolean includeClAdductModel){
		
		Collection<IsotopicPatternReferenceBin>ipModel = 
				new ArrayList<IsotopicPatternReferenceBin>();
		
		//	Read regular model
		File regModelFile = Paths.get(
				MRC2ToolBoxCore.configDir, 
				REGULAR_ISOTOPIC_PATTERN_MODEL_FILE_NAME).toFile();
		if(regModelFile != null && regModelFile.exists() && regModelFile.canRead())
			ipModel.addAll(readIsotopicPatternModelFromFile(regModelFile));
		
		// Read model for Cl adducts
		if(includeClAdductModel) {
			
			File clAdModelFile = Paths.get(
					MRC2ToolBoxCore.configDir, 
					CL_ADDUCT_ISOTOPIC_PATTERN_MODEL_FILE_NAME).toFile();
			if(clAdModelFile != null && clAdModelFile.exists() && clAdModelFile.canRead()) {
				
				Collection<IsotopicPatternReferenceBin>clModel = 
						readIsotopicPatternModelFromFile(clAdModelFile);
				clModel.stream().forEach(b -> b.setClAdduct(true));
				ipModel.addAll(clModel);
			}
		}
		return ipModel;
	}
	
	public enum IsotopicPatternModelColumns {
		
		MOL_FORMULA,
		NUM_REPEATS,
		EXACT_MASS,
		NUM_CARBONS,
		ISOTOPE_2,
		ISOTOPE_3,
		ISOTOPE_4,
		ISOTOPE_5,
		ISOTOPE_6,
		;
		
		public static IsotopicPatternModelColumns getOptionByName(String name) {

			for(IsotopicPatternModelColumns source : IsotopicPatternModelColumns.values()) {

				if(source.name().equals(name))
					return source;
			}
			return null;
		}
	}
	
	public static Collection<IsotopicPatternReferenceBin>readIsotopicPatternModelFromFile(File modelFile){
		
		Collection<IsotopicPatternReferenceBin>ipModel = 
				new ArrayList<IsotopicPatternReferenceBin>();
		
		String[][] modelData = DelimitedTextParser.parseTextFile(
				modelFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<IsotopicPatternModelColumns,Integer>headerMap = 
				createModelHeaderMap(modelData[0]);
		if(headerMap == null)
			return ipModel;
		
		for(int i=1; i<modelData.length; i++) {
			
			String formula = modelData[i][headerMap.get(IsotopicPatternModelColumns.MOL_FORMULA)];
			int numberOfRepeats = Integer.parseInt(
					modelData[i][headerMap.get(IsotopicPatternModelColumns.NUM_REPEATS)]);
			int numberOfCarbons = Integer.parseInt(
					modelData[i][headerMap.get(IsotopicPatternModelColumns.NUM_CARBONS)]);
			double exactMass = Double.parseDouble(
					modelData[i][headerMap.get(IsotopicPatternModelColumns.EXACT_MASS)]);
			
			IsotopicPatternReferenceBin bin = 
					new IsotopicPatternReferenceBin(formula, 
													numberOfRepeats, 
													numberOfCarbons,
													exactMass);
			
			String iso2relIntString = modelData[i][headerMap.get(IsotopicPatternModelColumns.ISOTOPE_2)];
			if(iso2relIntString != null && !iso2relIntString.isEmpty())
				bin.getIsotopeRelativeIntensities()[0] = Double.parseDouble(iso2relIntString);
						
			String iso3relIntString = modelData[i][headerMap.get(IsotopicPatternModelColumns.ISOTOPE_3)];
			if(iso3relIntString != null && !iso3relIntString.isEmpty())
				bin.getIsotopeRelativeIntensities()[1] = Double.parseDouble(iso3relIntString);
			
			String iso4relIntString = modelData[i][headerMap.get(IsotopicPatternModelColumns.ISOTOPE_4)];
			if(iso4relIntString != null && !iso4relIntString.isEmpty())
				bin.getIsotopeRelativeIntensities()[2] = Double.parseDouble(iso4relIntString);
			
			String iso5relIntString = modelData[i][headerMap.get(IsotopicPatternModelColumns.ISOTOPE_5)];
			if(iso5relIntString != null && !iso5relIntString.isEmpty())
				bin.getIsotopeRelativeIntensities()[3] = Double.parseDouble(iso5relIntString);
			
			String iso6relIntString = modelData[i][headerMap.get(IsotopicPatternModelColumns.ISOTOPE_6)];
			if(iso6relIntString != null && !iso6relIntString.isEmpty())
				bin.getIsotopeRelativeIntensities()[4] = Double.parseDouble(iso6relIntString);			
			
			ipModel.add(bin);
		}
		return ipModel;
	}
	
	private static Map<IsotopicPatternModelColumns,Integer>createModelHeaderMap(String[]header){
		
		Map<IsotopicPatternModelColumns,Integer>headerMap = 
				new TreeMap<IsotopicPatternModelColumns,Integer>();
		for(int i=0; i<header.length; i++) {
			
			IsotopicPatternModelColumns col = 
					IsotopicPatternModelColumns.getOptionByName(header[i]);
			if(col == null)
				return null;
			else
				headerMap.put(col, i);
		}		
		return headerMap;
	}
}













