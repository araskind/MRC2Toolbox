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

package edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public enum LipidMapsCrossRefFields {

	HMDB_ID("HMDB_ID", CompoundDatabaseEnum.HMDB),
	CHEBI_ID("CHEBI_ID", CompoundDatabaseEnum.CHEBI),
	KEGG_ID("KEGG_ID", CompoundDatabaseEnum.KEGG),
	LIPIDBANK_ID("LIPIDBANK_ID", CompoundDatabaseEnum.LIPIDBANK),
	METABOLOMICS_ID("METABOLOMICS_ID", CompoundDatabaseEnum.METABOLOMICS),
	PUBCHEM_CID("PUBCHEM_CID", CompoundDatabaseEnum.PUBCHEM),
	PUBCHEM_SID("PUBCHEM_SID", CompoundDatabaseEnum.PUBCHEMSUBSTANCE),
	PLANTFA_ID("PLANTFA_ID", CompoundDatabaseEnum.PLANTFA_ID),
	SWISSLIPIDS_ID("SWISSLIPIDS_ID", CompoundDatabaseEnum.SWISS_LIPIDS),
	;
	 
	private final String dbName;
	private final CompoundDatabaseEnum database;

	LipidMapsCrossRefFields(String dbName, CompoundDatabaseEnum database) {
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
}
