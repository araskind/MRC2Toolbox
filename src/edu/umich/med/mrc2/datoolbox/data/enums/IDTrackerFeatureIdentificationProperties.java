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

public enum IDTrackerFeatureIdentificationProperties {
	
	COMPOUND_NAME("Compound name"),
	DATABASE_ID("Database ID"),
	SOURCE_DATABASE("Source database"),
	FORMULA("Formula"),
	INCHI_KEY("InChi key"),
	ID_LEVEL("ID level"),
	ID_SOURCE("ID source"),
	ANNOTATIONS("Is annotated"),
	FOLLOWUPS("Follow-up assigned"),
	MRC2_ID_LEVEL("MRC2 ID level"),
	ID_SCORE("Score"),
	MSRT_LIB("MS/RT library name"),
	MSMS_LIBRARY("MSMS library name"),	
	//	MSMS_LIBRARY_ENTRY_ID("MSMS library entry ID"), TODO 
	COLLISION_ENERGY("Collision energy (lib)"),
	FWD_SCORE("Fwd. score"),
	REVERSE_SCORE("Rev. score"),
	PROBABILITY("Probability"),
	DOT_PRODUCT_COLUMN("Dot-product"),
	REVERSE_DOT_PRODUCT("Reverse dot-product"),
	MATCH_TYPE("Match type"),
	HYBRID_DOT_PRODUCT("Hybrid dot-product"),
	HYBRID_SCORE("Hybrid score"),
	HYBRID_DELTA_MZ("Hybrid mass shift"),
	SPECTRUM_ENTROPY("Entropy (lib)"),
	MASS_ERROR("Mass error"),
	RETENTION_ERROR("RT error"),
	BEST_MATCH_ADDUCT("Adduct"),
	REFMET_NAME("RefMet name"),
	SYSTEMATIC_NAME("IUPAC or systematic name"),
	REFMET_CLASSIFICATION("RefMet classification"),
	CLASSYFIRE_CLASSIFICATION("ClassyFire classification"),	
	FDR_Q_VALUE("q-value"),
	POSTERIOR_PROBABILITY("Posterior probability"),
	PERCOLATOR_SCORE("Percolator score"),
	;

	private final String name;

	IDTrackerFeatureIdentificationProperties(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public static IDTrackerFeatureIdentificationProperties getPropertyByName(String optionName) {

		for(IDTrackerFeatureIdentificationProperties o : IDTrackerFeatureIdentificationProperties.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
}
