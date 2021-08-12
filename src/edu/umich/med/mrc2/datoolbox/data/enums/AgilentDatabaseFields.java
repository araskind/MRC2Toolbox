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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum AgilentDatabaseFields {

	CAS("CAS Number", CompoundDatabaseEnum.CAS),
	CAS_ID("CAS ID", CompoundDatabaseEnum.CAS),
	CHEBI("ChEBI ID", CompoundDatabaseEnum.CHEBI),
	HMP("HMP ID", CompoundDatabaseEnum.HMDB),
	KEGG("KEGG ID", CompoundDatabaseEnum.KEGG),
	LIPIDMAPS("LMP ID", CompoundDatabaseEnum.LIPIDMAPS),
	METLIN("METLIN ID", CompoundDatabaseEnum.METLIN),
	NCBI_GI("NCBI gi", CompoundDatabaseEnum.GENBANK),
	PUBCHEM("PubChem ID", CompoundDatabaseEnum.PUBCHEM),
	SWISS_PROT("Swiss-Prot ID", CompoundDatabaseEnum.SWISS_PROT);

	private final String name;
	private final CompoundDatabaseEnum internalType;

	AgilentDatabaseFields(String AgilentType, CompoundDatabaseEnum internalType) {

		this.name = AgilentType;
		this.internalType = internalType;
	}

	public CompoundDatabaseEnum getInternalType() {
		return internalType;
	}

	public String getName() {
		return name;
	}

	public static CompoundDatabaseEnum getDatabaseByName(String dbName) {

		for(AgilentDatabaseFields db : AgilentDatabaseFields.values()) {

			if(db.getName().equals(dbName))
				return db.getInternalType();
		}
		return null;
	}
}
