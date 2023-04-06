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

package edu.umich.med.mrc2.datoolbox.data.cpdcoll;

public enum PCDLImportFields {

	NAME("Name"),
	FORMULA("Formula"),
	MASS("Mass"),
	ANION("Anion"),
	CATION("Cation"),
	METLIN("METLIN"),
	KEGG("KEGG"),
	CAS("CAS"),
	LIPID_MAPS("LMP"),
	HMDB("HMP"),
	CHEMSPIDER("ChemSpider"),
	RT("RT"),
	RI("RI"),
	LOG_P("Log P"),
	RT_LOW_THEOR("RT Low(Theor.)"),
	RT_HIGH_THEOR("RT high(Theor.)"),
	SYNONYMS("Synonyms"),
	SMILES("SMILES"),
	INCHI("InChI"),
	INCHI_KEY("InChI Key"),
	SIGMA_ALDRICH("Sigma-Aldrich"),
	CAYMAN_CHEMICAL("Cayman Chemical"),
	DESCRIPTION("Description"),
	PUBCHEM("PubChem"),
	CHEBI("ChEBI"),
	BIOCYC("BioCyc"),
	CHAPMAN_HALL("Chapman Hall"),
	NCBI("NCBI"),
	UNIPROT("Uniprot"),
	IUPAC("IUPAC"),
	;
	
	private final String name;

	PCDLImportFields(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public static PCDLImportFields getPCDLImportFieldByName(String name) {
		
		for(PCDLImportFields cat :PCDLImportFields.values()) {
			
			if(cat.name().equals(name))
				return cat;
		}		
		return null;
	}
}
