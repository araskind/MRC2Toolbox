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
 *	Implements molecular formula validation based on Seven Golden Rules paper:
 *	https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-8-105
 *	
 *	Original software page:
 *	https://fiehnlab.ucdavis.edu/projects/seven-golden-rules
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class SGRFormulaValidator {

	private IElement C;
	private IElement H;
	private IElement Br;
	private IElement Cl;
	private IElement F;
	private IElement N;
	private IElement O;
	private IElement P;
	private IElement S;
	private IElement Si;
	private Map<IElement,Integer>elementCounts;
	private Set<IElement>elements;
	private static final IChemObjectBuilder builder = 
			SilentChemObjectBuilder.getInstance();

	public SGRFormulaValidator() {
		super();
		IsotopeFactory ifac = null;
		try {
			ifac = Isotopes.getInstance();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		C = ifac.getElement("C");
		H = ifac.getElement("H");
		Br = ifac.getElement("Br");
		Cl = ifac.getElement("Cl");
		F = ifac.getElement("F");
		N = ifac.getElement("N");
		O = ifac.getElement("O");
		P = ifac.getElement("P");
		S = ifac.getElement("S");
		Si = ifac.getElement("Si");
		
		elements = new HashSet<IElement>();
		elements.add(C);
		elements.add(H);
		elements.add(Br);
		elements.add(Cl);
		elements.add(F);
		elements.add(N);
		elements.add(O);
		elements.add(P);
		elements.add(S);
		elements.add(Si);
		
		elementCounts = new HashMap<IElement,Integer>();			
	}
	
	public boolean validateFormula(String formulaString) {
		return validateFormula(formulaString, true);
	}
	
	public boolean validateFormula(String formulaString, boolean strict) {
		
		IMolecularFormula formula = 
				MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
		return validateFormula(formula, strict);
	}
	
	public boolean validateFormula(IMolecularFormula formula) {
		return validateFormula(formula, true);
	}
			
	public boolean validateFormula(IMolecularFormula formula, boolean strict) {
			
		if(formula == null) 
			return false;
			
		elementCounts.clear();
		for(IElement element : elements) {
			int eCount = MolecularFormulaManipulator.getElementCount(formula, element);
			elementCounts.put(element, eCount);
		}
		//	This portion is present in original VB code, but not used in calculations
		//
		//	int rdbe = (elementCounts.get(C) + elementCounts.get(Si) + 1) - 
		//		((elementCounts.get(H) + elementCounts.get(Br) + elementCounts.get(Cl) + elementCounts.get(F) 
		//			- (elementCounts.get(N) + elementCounts.get(P))) / 2);
		int numElectrons = 
				(elementCounts.get(C) + elementCounts.get(Si)) * 4
				+ elementCounts.get(H)
				+ (elementCounts.get(Br) + elementCounts.get(Cl) + elementCounts.get(F) ) * 7
				+ (elementCounts.get(N) + elementCounts.get(P)) * 5
				+ (elementCounts.get(O) + elementCounts.get(S)) * 6;
		
		int atomCount = elementCounts.values().stream().mapToInt(i -> i).sum();
		
		int lewis  = 
				(elementCounts.get(C) + elementCounts.get(Si)) * 4
				+ elementCounts.get(H) + elementCounts.get(Br) 
				+ elementCounts.get(Cl) + elementCounts.get(F) 
				+ (elementCounts.get(N) + elementCounts.get(P)) * 3
				+ (elementCounts.get(O) + elementCounts.get(S)) * 2;
				
		double hydrogen2carbon = 0.0d;
		if(elementCounts.get(C) > 0)
			hydrogen2carbon = (double) (elementCounts.get(H) / elementCounts.get(C));
		
		boolean H2CratioPass = false;
		if(hydrogen2carbon > 0 && hydrogen2carbon < 6)
			H2CratioPass = true;
		
		double nitrogen2carbon = 0.0d;
		if(elementCounts.get(C) > 0)
			nitrogen2carbon = (double) (elementCounts.get(N) / elementCounts.get(C));

		double oxygen2carbon = 0.0d;
		if(elementCounts.get(C) > 0)
			nitrogen2carbon = (double) (elementCounts.get(O) / elementCounts.get(C));
		
		double phosphorus2carbon = 0.0d;
		if(elementCounts.get(C) > 0)
			phosphorus2carbon = (double) (elementCounts.get(P) / elementCounts.get(C));
		
		double sulphur2carbon = 0.0d;
		if(elementCounts.get(C) > 0)
			sulphur2carbon = (double) (elementCounts.get(S) / elementCounts.get(C));
		
		boolean NOPSPass = false;		
		if(nitrogen2carbon <= 4 
				&& oxygen2carbon <= 3
				&& phosphorus2carbon <= 2
				&& sulphur2carbon <= 3)
			NOPSPass = true;
		
		boolean HNOPSPass = false;
		if(hydrogen2carbon >= 0.2 && hydrogen2carbon <=3
				&& nitrogen2carbon <= 2
				&& oxygen2carbon <= 1.2
				&& phosphorus2carbon <= 0.32
				&& sulphur2carbon <= 0.65)
			HNOPSPass = true;
		
		boolean seniorPass = false;
		int sv = (elementCounts.get(C) + elementCounts.get(Si)) * 4 
				+ elementCounts.get(H) + elementCounts.get(Br) + elementCounts.get(Cl) + elementCounts.get(F) 
				+ (elementCounts.get(N) + elementCounts.get(P)) * 5
				+ elementCounts.get(O) * 2
				+ elementCounts.get(S) * 6;
		if(sv >= (atomCount - 1) * 2)
			seniorPass = true;
		
		boolean lewisPass = false;
		if(lewis % 2 == 0 && numElectrons > 7)
			lewisPass = true;
		
		boolean formulaExists = false;
		if(strict) {
			if(lewisPass && seniorPass && H2CratioPass && NOPSPass && HNOPSPass)
				formulaExists = true;
		}
		else {
			if(lewisPass && seniorPass && H2CratioPass && NOPSPass )
				formulaExists = true;
		}	
		return formulaExists;
	}
}

//	Keys from original VB code

//C		J3
//H		J4
//Br	J5
//Cl	J6
//F		J7
//N		J8
//O		J9
//P		J10
//S		J11
//Si	J12

//F_EXIST		J13
//LewisPass		J14
//SeniorPass	J15
//RDBE			J16
//#e			J17
//#atoms		J18
//Lewis			J19	

//H/C			J20
//H/C boolean	J21
//N/C			J22
//O/C			J23
//P/C			J24
//S/C			J25
//NOPS Check	J26
//HNOPS Check	J27





