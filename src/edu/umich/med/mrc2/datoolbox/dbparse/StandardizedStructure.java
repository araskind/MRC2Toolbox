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

package edu.umich.med.mrc2.datoolbox.dbparse;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import ambit2.base.data.Property;

public class StandardizedStructure {

	private IAtomContainer stdMol;
	private String stdInchi;
	private String stdInchiKey;
	private String stdSmiles;
	private String formulaStringFromSmiles;
	private double smilesMass;
	
	public StandardizedStructure(IAtomContainer stdMol) {
		super();
		this.stdMol = stdMol;
		stdInchi = (String)stdMol.getProperty(Property.opentox_InChI);
		stdInchiKey = (String)stdMol.getProperty(Property.opentox_InChIKey);
		stdSmiles = (String)stdMol.getProperty(Property.getSMILESInstance());
		
		IMolecularFormula molFormula = 
				MolecularFormulaManipulator.getMolecularFormula(stdMol);			
		formulaStringFromSmiles = 
				MolecularFormulaManipulator.getString(molFormula);	
		smilesMass = MolecularFormulaManipulator.getMass(
						molFormula, MolecularFormulaManipulator.MonoIsotopic);	
	}

	public IAtomContainer getStdMol() {
		return stdMol;
	}

	public String getStdInchi() {
		return stdInchi;
	}

	public String getStdInchiKey() {
		return stdInchiKey;
	}

	public String getStdSmiles() {
		return stdSmiles;
	}

	public String getFormulaStringFromSmiles() {
		return formulaStringFromSmiles;
	}

	public double getSmilesMass() {
		return smilesMass;
	}
}
