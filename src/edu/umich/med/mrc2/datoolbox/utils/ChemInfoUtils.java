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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.enums.InChiKeyCharge;
import io.github.dan2097.jnainchi.InchiStatus;

public class ChemInfoUtils {
	
	private static final IChemObjectBuilder builder = 
			SilentChemObjectBuilder.getInstance();
	private static final SmilesGenerator smilesGenerator = 
			new SmilesGenerator(SmiFlavor.Isomeric);	
	private static final SmilesParser smilesParser = 
			new SmilesParser(builder);
	private final static Pattern inchiKeyPatternOne = Pattern.compile("SA-[KLMNOPR]$");
	private final static Pattern inchiKeyPatternTwo = Pattern.compile("NA-[KLMNOPR]$");
	private final static Pattern completeInchiKeyPattern = 
			Pattern.compile("^[A-Z]{14}-[A-Z]{8}[NS]A-[HIJKLMNOPRSTU]$");
	private final static Pattern inchiKey2DPattern = Pattern.compile("^[A-Z]{14}$");
	
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;

	public static int getChargeFromInChiKey(String inChiKey) {
		
		if(inChiKey == null || inChiKey.isEmpty())
			return 0;
		
		String lastChar = inChiKey.substring(inChiKey.length() - 1);				
		return InChiKeyCharge.getChargeByCode(lastChar);
	}
	
	public static IAtomContainer generateMoleculeFromInchi(String inchi) throws CDKException {
		
		igfactory = InChIGeneratorFactory.getInstance();
		InChIToStructure intostruct = null;
		try {
			intostruct = igfactory.getInChIToStructure(
					inchi, DefaultChemObjectBuilder.getInstance());
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(intostruct != null) {
			
			InchiStatus ret = intostruct.getStatus();
			if (ret == InchiStatus.WARNING) {
				// Structure generated, but with warning message
				System.out.println("InChI warning: " + intostruct.getMessage());
			} else if (ret != InchiStatus.SUCCESS) {
				// Structure generation failed
				System.out.println("Structure generation failed failed: " 
						+ ret.toString() + " [" + intostruct.getMessage() + "]");
				return null;
			}
			return intostruct.getAtomContainer();
		}
		else
			return null;
	}
	
	public static IAtomContainer generateMoleculeFromSMILES(String smiles) throws Exception {
		
		IAtomContainer mol = null;
		if (smiles == null || smiles.isEmpty() || smiles.equals("NoSmile")) 
			return null;

		try {
			mol = smilesParser.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return mol;
	}
	
	public static String generateFormulaStringFromSMILES(String smiles) throws CDKException {
		
		IAtomContainer mol = null;
		if (smiles == null || smiles.isEmpty() || smiles.equals("NoSmile")) 
			return null;

		try {
			mol = smilesParser.parseSmiles(smiles);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if(mol != null) {
			IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(mol);
			if(mf != null)
				return MolecularFormulaManipulator.getString(mf);
		}
		return null;
	}
	
	public static String generateIsomericSmilesForMolecule(IAtomContainer molecule) {
		
		String smiles = null;
		try {
			smiles = smilesGenerator.create(molecule);
		} catch (NullPointerException | CDKException e) {
			System.out.println("Unable to generate SMILES for " + molecule.getProperty(CDKConstants.TITLE));
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		return smiles;
	}
	
	public static String generateInchiForMolecule(IAtomContainer molecule) throws CDKException {
		
		igfactory = InChIGeneratorFactory.getInstance();
		inChIGenerator = igfactory.getInChIGenerator(molecule);
		InchiStatus ret = inChIGenerator.getStatus();
		if (ret == InchiStatus.SUCCESS || ret == InchiStatus.WARNING)
			return inChIGenerator.getInchi();
		else
			return null;
	}	
	
	public static String switchInchiKeyVersion(String inchiKey) {
		
		String newKey = null;
		Matcher regexMatcher = inchiKeyPatternOne.matcher(inchiKey);
		if(regexMatcher.find() && regexMatcher.group(0) != null) {
	
			String suffix = regexMatcher.group(0);
			newKey = inchiKey.replaceFirst(suffix + "$", "NA-" + suffix.charAt(suffix.length()-1));
		}
		if(newKey == null) {
			
			regexMatcher = inchiKeyPatternTwo.matcher(inchiKey);
			if(regexMatcher.find() && regexMatcher.group(0) != null) {
	
				String suffix = regexMatcher.group(0);
				newKey = inchiKey.replaceFirst(suffix + "$", "SA-" + suffix.charAt(suffix.length()-1));
			}
		}
		return newKey;
	}
	
	public static boolean isInchiKeyValid(String inchiKey) {
		
		Matcher regexMatcher = completeInchiKeyPattern.matcher(inchiKey);
		if(regexMatcher.find() && regexMatcher.group(0) != null)
			return true;
		else
			return false;
	}
	
	public static boolean isInchiKey2DValid(String inchiKey2D) {
		
		Matcher regexMatcher = inchiKey2DPattern.matcher(inchiKey2D);
		if(regexMatcher.find() && regexMatcher.group(0) != null)
			return true;
		else
			return false;
	}
	
	public static boolean isSMARTSstringValid(String smartsString) {
		
		SmartsPattern pattern = null;
		try {
			pattern = SmartsPattern.create(smartsString, builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		if(pattern == null)
			return false;
		else
			return true;
	}
	
	public static boolean doSMILESmatchSMATRSpattern(String smilesString, String smartsString) {

		IAtomContainer atomContainer = null;
		try {
			atomContainer = smilesParser.parseSmiles(smilesString);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(atomContainer == null)
			return false;
		
		SmartsPattern pattern = null;
		try {
			pattern = SmartsPattern.create(smartsString, builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		if(pattern == null)
			return false;
				
		return pattern.matches(atomContainer);
	}	
}



