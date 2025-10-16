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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum PCDLFields {

	AGILENT_ID("Agilent ID"),
	ANION("Anion"),
	BIOCYC("BioCyc"),
	CAS("CAS"),
	CCS_COUNT("CCS Count"),
	CATION("Cation"),
	CAYMAN_CHEMICAL("Cayman Chemical"),
	CHEBI("ChEBI"),
	CHAPMAN_HALL("Chapman Hall"),
	CHEMSPIDER("ChemSpider"),
	COMPOUNDID("CompoundID"),
	DATE_CREATED("Date Created"),
	DATE_MODIFIED("Date Modified"),
	DESCRIPTION("Description"),
	FORMULA("Formula"),
	HMP("HMP"),
	IUPAC("IUPAC"),
	INCHI("InChI"),
	INCHI_KEY("InChI Key"),
	KEGG("KEGG"),
	LMP("LMP"),
	LOGP("LogP"),
	METLIN("METLIN"),
	MASS("Mass"),
	NCBI("NCBI"),
	NAME("Name"),
	NUMSPECTRA("NumSpectra"),
	PUBCHEM("PubChem"),
	RT_HIGH_THEOR("RT High (Theor.)"),
	RT_LOW_THEOR("RT Low (Theor.)"),
	RT_MODIFIED("RT Modified"),
	RETENTION_INDEX("Retention Index"),
	RETENTION_TIME("Retention Time"),
	SMILES("SMILES"),
	SIGMA_ALDRICH("Sigma-Aldrich"),
	SYNONYMS("Synonyms"),
	UNIPROT("UniProt"),
	;
	
	private final String pcdlName;

	PCDLFields(String pcdlName) {
		this.pcdlName = pcdlName;
	}

	public String getName() {
		return pcdlName;
	}

	@Override
	public String toString() {
		return pcdlName;
	}
	
	public static PCDLFields getOptionByName(String optionName) {

		for(PCDLFields o : PCDLFields.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static PCDLFields getOptionByUIName(String uiName) {
		
		for(PCDLFields f : PCDLFields.values()) {
			
			if(f.getName().equals(uiName))
				return f;
		}		
		return null;
	}	
}













