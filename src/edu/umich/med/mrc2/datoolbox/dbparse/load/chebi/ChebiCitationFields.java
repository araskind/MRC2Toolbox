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

package edu.umich.med.mrc2.datoolbox.dbparse.load.chebi;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public enum ChebiCitationFields {

	AGRICOLA("Agricola Citation Links", CompoundDatabaseEnum.AGRICOLA),
	CHINESE_ABSTRACTS("Chinese Abstracts Citation Links", CompoundDatabaseEnum.CHINESE_ABSTRACTS),
	CITEXPLORE("CiteXplore Citation Links", CompoundDatabaseEnum.CITEXPLORE),
	PUBMED_CENTRAL("PubMed Central Citation Links", CompoundDatabaseEnum.PUBMED_CENTRAL),
	PUBMED("PubMed Citation Links", CompoundDatabaseEnum.PUBMED),
	PPR("PPR Links", CompoundDatabaseEnum.PPR)
	;

	private final String dbName;
	private final CompoundDatabaseEnum database;

	ChebiCitationFields(String dbName, CompoundDatabaseEnum database) {
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

	public ChebiCrossrefFields getByName(String name) {

		for(ChebiCrossrefFields v : ChebiCrossrefFields.values()) {

			if(v.getName().equals(name))
				return v;
		}
		return null;
	}
}
