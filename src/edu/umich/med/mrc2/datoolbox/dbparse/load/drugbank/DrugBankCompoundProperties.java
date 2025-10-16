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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

public enum DrugBankCompoundProperties {

	BIOAVAILABILITY("Bioavailability"),
	GHOSE_FILTER("Ghose Filter"),
	H_BOND_ACCEPTOR_COUNT("H Bond Acceptor Count"),
	H_BOND_DONOR_COUNT("H Bond Donor Count"),
	INCHI("InChI"),
	INCHIKEY("InChIKey"),
	IUPAC_NAME("IUPAC Name"),
	LOGP("logP"),
	LOGS("logS"),
	MDDR_LIKE_RULE("MDDR-Like Rule"),
	MOLECULAR_FORMULA("Molecular Formula"),
	MOLECULAR_WEIGHT("Molecular Weight"),
	MONOISOTOPIC_WEIGHT("Monoisotopic Weight"),
	NUMBER_OF_RINGS("Number of Rings"),
	PHYSIOLOGICAL_CHARGE("Physiological Charge"),
	PKA_ACIDIC("pKa (strongest acidic)"),
	PKA_BASIC("pKa (strongest basic)"),
	POLARIZABILITY("Polarizability"),
	POLAR_SURFACE_AREA("Polar Surface Area (PSA)"),
	REFRACTIVITY("Refractivity"),
	ROTATABLE_BOND_COUNT("Rotatable Bond Count"),
	RULE_OF_FIVE("Rule of Five"),
	SMILES("SMILES"),
	TRADITIONAL_IUPAC_NAME("Traditional IUPAC Name"),
	VEBERS("Veber's Rule"),
	WATER_SOLUBILITY("Water Solubility");
	
	private final String name;

	DrugBankCompoundProperties(String name) {
		this.name = name;
	}

	public String getName() {		
		return name;
	}
	
	@Override
	public String toString() {		
		return name;
	}
}
