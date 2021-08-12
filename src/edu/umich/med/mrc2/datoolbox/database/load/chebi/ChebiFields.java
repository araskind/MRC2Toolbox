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

package edu.umich.med.mrc2.datoolbox.database.load.chebi;

public enum ChebiFields {

	CHEBI_ID("ChEBI ID"),
	CHARGE("Charge"),
	FORMULAE("Formulae"),
	MONOISOTOPIC_MASS("Monoisotopic Mass"),
	MASS("Mass"),
	INCHI("InChI"),
	INCHIKEY("InChIKey"),
	SMILES("SMILES"),
	DEFINITION("Definition"),
	CHEBI_NAME("ChEBI Name"),
	STAR("Star"),
	LAST_MODIFIED("Last Modified"),
	;

	private final String name;

	ChebiFields(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}

