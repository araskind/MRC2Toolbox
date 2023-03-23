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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public enum DrugbankCrossrefFields {

	BINDINGDB("BindingDB", CompoundDatabaseEnum.BINDINGDB),
	CAS("cas-number", CompoundDatabaseEnum.CAS),
	CHEBI("ChEBI", CompoundDatabaseEnum.CHEBI),
	CHEMBL("ChEMBL", CompoundDatabaseEnum.CHEMBL),
	CHEMSPIDER("ChemSpider", CompoundDatabaseEnum.CHEMSPIDER),
	DPD("Drugs Product Database (DPD)", CompoundDatabaseEnum.DPD),
	GENATLAS("GenAtlas", CompoundDatabaseEnum.GENATLAS),
	GENBANK("GenBank", CompoundDatabaseEnum.GENBANK),
	GENBANK2("GenBank Gene Database", CompoundDatabaseEnum.GENBANK),
	GENBANKPROTEIN("GenBank Protein Database", CompoundDatabaseEnum.GENBANKPROTEIN),
	GUIDE2PHARMACOLOGY("Guide to Pharmacology", CompoundDatabaseEnum.GUIDE2PHARMACOLOGY),
	HGNC("HUGO Gene Nomenclature Committee (HGNC)", CompoundDatabaseEnum.HGNC),
	IUPHAR("IUPHAR", CompoundDatabaseEnum.IUPHAR),
	KEGG("KEGG Compound", CompoundDatabaseEnum.KEGG),
	KEGGDRUG("KEGG Drug", CompoundDatabaseEnum.KEGGDRUG),
	PDB("PDB", CompoundDatabaseEnum.PDB),
	PHARMGKB("PharmGKB", CompoundDatabaseEnum.PHARMGKB),
	PUBCHEM("PubChem Compound", CompoundDatabaseEnum.PUBCHEM),
	PUBCHEMSUBSTANCE("PubChem Substance", CompoundDatabaseEnum.PUBCHEMSUBSTANCE),
	TTD("Therapeutic Targets Database", CompoundDatabaseEnum.TTD),
	UNII("unii", CompoundDatabaseEnum.FDA_UNII),
	UNIPROT("UniProt Accession", CompoundDatabaseEnum.UNIPROT),
	UNIPROTKB("UniProtKB", CompoundDatabaseEnum.UNIPROTKB),
	WIKI("Wikipedia", CompoundDatabaseEnum.WIKI),	
	;

	private final String dbName;
	private final CompoundDatabaseEnum database;

	DrugbankCrossrefFields(String dbName, CompoundDatabaseEnum database) {
		this.dbName = dbName;
		this.database = database;
	}

	public String toString() {
		return dbName;
	}

	public String getName() {
		return dbName;
	}

	public CompoundDatabaseEnum getDatabase() {
		return database;
	}

	public static DrugbankCrossrefFields getFieldByName(String name) {

		for(DrugbankCrossrefFields v : DrugbankCrossrefFields.values()) {

			if(v.getName().equals(name))
				return v;
		}
		return null;
	}
}
