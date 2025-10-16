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

package edu.umich.med.mrc2.datoolbox.dbparse.load.pubchem;

public enum PubChemFields {

	PUBCHEM_ID("PUBCHEM_COMPOUND_CID"),
	FORMULA("PUBCHEM_MOLECULAR_FORMULA"),
	EXACT_MASS("PUBCHEM_EXACT_MASS"),
	MW("PUBCHEM_MOLECULAR_WEIGHT"),
	INCHI("PUBCHEM_IUPAC_INCHI"),
	INCHIKEY("PUBCHEM_IUPAC_INCHIKEY"),
	SMILES_CANONICAL("PUBCHEM_OPENEYE_CAN_SMILES"),
	SMILES_ISOMERIC("PUBCHEM_OPENEYE_ISO_SMILES"),
	IUPAC_NAME("PUBCHEM_IUPAC_NAME"),
	;

	private final String name;

	PubChemFields(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}

