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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank.DrugbankCrossrefFields;

public enum HMDBCrossrefFields {

	CAS("cas_registry_number", CompoundDatabaseEnum.CAS),
	DRUGBANK("drugbank_id", CompoundDatabaseEnum.DRUGBANK),
	DRUGBANKMET("drugbank_metabolite_id", CompoundDatabaseEnum.DRUGBANKMET),
	PHEXCPD("phenol_explorer_compound_id", CompoundDatabaseEnum.PHEXCPD),
	PHEXMET("phenol_explorer_metabolite_id", CompoundDatabaseEnum.PHEXMET),
	FOODB("foodb_id", CompoundDatabaseEnum.FOODB),
	KNAPSACK("knapsack_id", CompoundDatabaseEnum.KNAPSACK),
	CHEMSPIDER("chemspider_id", CompoundDatabaseEnum.CHEMSPIDER),
	KEGG("kegg_id", CompoundDatabaseEnum.KEGG),
	BIOCYC("biocyc_id", CompoundDatabaseEnum.BIOCYC),
	BIGG("bigg_id", CompoundDatabaseEnum.BIGG),
	WIKI("wikipidia", CompoundDatabaseEnum.WIKI),
	NUGOWIKI("nugowiki", CompoundDatabaseEnum.NUGOWIKI),
	METAGENE("metagene", CompoundDatabaseEnum.METAGENE),
	METLIN("metlin_id", CompoundDatabaseEnum.METLIN),
	PUBCHEM("pubchem_compound_id", CompoundDatabaseEnum.PUBCHEM),
	HET("het_id", CompoundDatabaseEnum.HET),
	CHEBI("chebi_id", CompoundDatabaseEnum.CHEBI),
	STITCH("stitch_id", CompoundDatabaseEnum.STITCH),
	PDB("pdb_id", CompoundDatabaseEnum.PDB),
	HMDB("hmdb_id", CompoundDatabaseEnum.HMDB),
	;

	private final String dbName;
	private final CompoundDatabaseEnum database;

	HMDBCrossrefFields(String dbName, CompoundDatabaseEnum database) {
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

	public static DrugbankCrossrefFields getByName(String name) {

		for(DrugbankCrossrefFields v : DrugbankCrossrefFields.values()) {

			if(v.getName().equals(name))
				return v;
		}
		return null;
	}
}
