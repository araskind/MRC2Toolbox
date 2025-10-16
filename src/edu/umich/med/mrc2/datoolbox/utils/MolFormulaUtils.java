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

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class MolFormulaUtils {

	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	
	public static int getChargeFromSmiles(String smilesString) {

		int charge = 0;

		try {
			SmilesParser sp = new SmilesParser(builder);
			IAtomContainer m = sp.parseSmiles("c1ccccc1");
			charge = StreamSupport.stream(m.atoms().spliterator(), false).
					mapToInt(a -> a.getFormalCharge()).sum();
		} 
		catch (InvalidSmilesException e) {
			System.err.println(e.getMessage());
		}
		return charge;
	}
	
	public static boolean haveSameElementalComposition(String molFormulaOne, String molFormulaTwo) {
		
		IMolecularFormula cdkFormulaOne = null;
		IMolecularFormula cdkFormulaTwo = null;
		
		if (molFormulaOne != null && !molFormulaOne.isEmpty()) 
			cdkFormulaOne = MolecularFormulaManipulator.getMolecularFormula(molFormulaOne, builder);
		
		if (molFormulaTwo != null && !molFormulaTwo.isEmpty()) 
			cdkFormulaTwo = MolecularFormulaManipulator.getMolecularFormula(molFormulaTwo, builder);		

		if(cdkFormulaOne == null && cdkFormulaTwo == null)
			return false;
		
		if((cdkFormulaOne == null && cdkFormulaTwo != null) 
				|| (cdkFormulaOne != null && cdkFormulaTwo == null)) {
			return false;
		}
		return MolecularFormulaManipulator.compare(cdkFormulaOne, cdkFormulaTwo);
	}
	
	public static String normalizeFormula(String formulaString) {
		
		String normalized = "";
		IMolecularFormula cdkFormulaOne = null;		
		try {
			cdkFormulaOne = MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(cdkFormulaOne != null)
			return MolecularFormulaManipulator.getString(cdkFormulaOne);
		
		return normalized;
	}
	
	public static double calculateExactMonoisotopicMass(String formulaString) {
		
		IMolecularFormula formula = 
				MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);			
		return MolecularFormulaManipulator.getMass(
				formula, MolecularFormulaManipulator.MonoIsotopic);
	}
	
	public static Integer getCarbonCounts(String formulaString) {
		
		IIsotope carbon = null;
		try {
			carbon = Isotopes.getInstance().getMajorIsotope("C");
		} catch (IOException e) {
			e.printStackTrace();
		}
		IMolecularFormula formula = 
				MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
				
		if(carbon != null)
			return MolecularFormulaManipulator.getElementCount(formula, carbon);
		else 
			return null;
	}
		
	public static boolean isInRange(
			IMolecularFormula formula, 
			MolecularFormulaRange ranges) {
		
		Set<String> elementSymbolFilter = 
				StreamSupport.stream(ranges.isotopes().spliterator(), false).
			map(i -> i.getSymbol()).collect(Collectors.toSet());

		
		for(IIsotope isotope : formula.isotopes()) {
			
			if(!ranges.contains(isotope))
				return false;
			
			if(formula.getIsotopeCount(isotope) > ranges.getIsotopeCountMax(isotope)
					|| formula.getIsotopeCount(isotope) < ranges.getIsotopeCountMin(isotope))
				return false;
		}
		return true;
	}
}













