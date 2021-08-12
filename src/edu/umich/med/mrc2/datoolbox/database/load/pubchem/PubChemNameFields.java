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

package edu.umich.med.mrc2.datoolbox.database.load.pubchem;

public enum PubChemNameFields {
	
	IUPAC_NAME("PUBCHEM_IUPAC_NAME"),
	IUPAC_SYSTEMATIC_NAME("PUBCHEM_IUPAC_SYSTEMATIC_NAME"),
	IUPAC_OPENEYE_NAME("PUBCHEM_IUPAC_OPENEYE_NAME"),
	IUPAC_CAS_NAME("PUBCHEM_IUPAC_CAS_NAME"),		
	IUPAC_TRADITIONAL_NAME("PUBCHEM_IUPAC_TRADITIONAL_NAME"),
	;

	private final String name;

	PubChemNameFields(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
