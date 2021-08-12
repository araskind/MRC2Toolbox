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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.nistms;

import java.util.ArrayList;
import java.util.Collection;

public enum NISTMSSearchType {

	IDENTITY_QUICK(NISTMSSearchTypeGroup.Identity ,"Quick", 0),
	IDENTITY_NORMAL(NISTMSSearchTypeGroup.Identity ,"Normal", 1),
	IDENTITY_MSMS(NISTMSSearchTypeGroup.Identity ,"MS/MS", 6),
	IDENTITY_IN_SOURCE_HIGH_RES(NISTMSSearchTypeGroup.Identity ,"InSource HighRes", 7),
	
	SIMILARITY_SIMPLE(NISTMSSearchTypeGroup.Similarity ,"Simple", 2),
	SIMILARITY_HYBRID(NISTMSSearchTypeGroup.Similarity ,"Hybrid", 3),
	SIMILARITY_NEUTRAL_LOSS(NISTMSSearchTypeGroup.Similarity ,"Neutral loss", 4),
	SIMILARITY_EI_MSMS(NISTMSSearchTypeGroup.Similarity ,"MSMS in EI", 5),
	SIMILARITY_HYBRID_MSMS(NISTMSSearchTypeGroup.Similarity ,"MSMS Hybrid", 8),
	;

	private final NISTMSSearchTypeGroup group;
	private final String description;
	private final Integer code;

	NISTMSSearchType(NISTMSSearchTypeGroup group, String description, Integer code) {
		
		this.group = group;
		this.description = description;
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return description;
	}

	public static NISTMSSearchType getOptionByName(String name) {

		for(NISTMSSearchType o : NISTMSSearchType.values()) {
			if(o.name().equals(name))
				return o;
		}
		return null;
	}
	
	public static NISTMSSearchType[] getSearchTypesForGroup(NISTMSSearchTypeGroup group){
		
		Collection<NISTMSSearchType>typesForGroup = new ArrayList<NISTMSSearchType>();
		for(NISTMSSearchType o : NISTMSSearchType.values()) {
			if(o.getGroup().equals(group))
				typesForGroup.add(o);
		}		
		return typesForGroup.toArray(new NISTMSSearchType[typesForGroup.size()]);
	}

	/**
	 * @return the group
	 */
	public NISTMSSearchTypeGroup getGroup() {
		return group;
	}

	/**
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}
}
