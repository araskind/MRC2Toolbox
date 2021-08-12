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

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public enum ChebiCrossrefFields {

	ARRAYEXPRESS("ArrayExpress Database Links", CompoundDatabaseEnum.ARRAYEXPRESS),
	BEILSTEIN("Beilstein Registry Numbers", CompoundDatabaseEnum.BEILSTEIN),
	BIOMODELS("BioModels Database Links", CompoundDatabaseEnum.BIOMODELS),
	CAS("CAS Registry Numbers", CompoundDatabaseEnum.CAS),
	DRUGBANK("DrugBank Database Links", CompoundDatabaseEnum.DRUGBANK),
	ECMDB("ECMDB Database Links", CompoundDatabaseEnum.ECMDB),
	GMELIN("Gmelin Registry Numbers", CompoundDatabaseEnum.GMELIN),
	HMDB("HMDB Database Links", CompoundDatabaseEnum.HMDB),
	INTACT("IntAct Database Links", CompoundDatabaseEnum.INTACT),
	INTENZ("IntEnz Database Links", CompoundDatabaseEnum.INTENZ),
	KEGG_COMPOUND("KEGG COMPOUND Database Links", CompoundDatabaseEnum.KEGG),
	KEGG_DRUG("KEGG DRUG Database Links", CompoundDatabaseEnum.KEGGDRUG),
	KEGG_GLYCAN("KEGG GLYCAN Database Links", CompoundDatabaseEnum.KEGG_GLYCAN),
	KNAPSACK("KNApSAcK Database Links", CompoundDatabaseEnum.KNAPSACK),
	LINCS("LINCS Database Links", CompoundDatabaseEnum.LINCS),
	LIPID_MAPS_CLASS("LIPID MAPS class Database Links", CompoundDatabaseEnum.LIPIDMAPS_BULK),
	LIPID_MAPS("LIPID MAPS instance Database Links", CompoundDatabaseEnum.LIPIDMAPS),
	METACYC("MetaCyc Database Links", CompoundDatabaseEnum.META_CYC),
	PDBECHEM("PDBeChem Database Links", CompoundDatabaseEnum.PDBECHEM),
	PATENT("Patent Database Links", CompoundDatabaseEnum.PATENT),
	PUBCHEM("PubChem Database Links", CompoundDatabaseEnum.PUBCHEM),
	PUBCHEM_SID("PubChem Substance Database Links", CompoundDatabaseEnum.PUBCHEMSUBSTANCE),
	RESID("RESID Database Links", CompoundDatabaseEnum.RESID),
	REACTOME("Reactome Database Links", CompoundDatabaseEnum.REACTOME),
	RHEA("Rhea Database Links", CompoundDatabaseEnum.RHEA),
	SABIO_RK("SABIO-RK Database Links", CompoundDatabaseEnum.SABIO_RK),
	CHEBI_SECONDARY("Secondary ChEBI ID", CompoundDatabaseEnum.CHEBI_SECONDARY),
	UM_BBD("UM-BBD compID Database Links", CompoundDatabaseEnum.UM_BBD),
	UNIPROT("UniProt Database Links", CompoundDatabaseEnum.UNIPROT),
	WIKIPEDIA("Wikipedia Database Links", CompoundDatabaseEnum.WIKI),
	YMDB("YMDB Database Links", CompoundDatabaseEnum.YMDB),
	;

	private final String dbName;
	private final CompoundDatabaseEnum database;

	ChebiCrossrefFields(String dbName, CompoundDatabaseEnum database) {
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

	public static ChebiCrossrefFields getByName(String name) {

		for(ChebiCrossrefFields v : ChebiCrossrefFields.values()) {

			if(v.getName().equals(name))
				return v;
		}
		return null;
	}
}
