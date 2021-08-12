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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class LIMSBioSpecies  implements Serializable, Comparable<LIMSBioSpecies>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5230243438785602785L;
	private Integer taxonomyId;
	private String speciesName;
	public static final String PRIMARY_NAME_CLASS = "scientific name";
	private Map<String,String>nameClassMap;

	public LIMSBioSpecies(Integer taxonomyId) {
		super();
		this.taxonomyId = taxonomyId;
		nameClassMap = new TreeMap<String,String>();
	}

	public void addName(String name, String nameClass) {
		nameClassMap.put(name, nameClass);
	}

	/**
	 * @return the sampleTypeId
	 */
	public Integer getTaxonomyId() {
		return taxonomyId;
	}

	/**
	 * @return the sampleTypeName
	 */
	public String getSpeciesPrimaryName() {

		if(speciesName == null) {
			Entry<String, String> sciNameEntry = nameClassMap.entrySet().stream().
				filter(m -> m.getValue().equals(PRIMARY_NAME_CLASS)).findFirst().orElse(null);
			if(sciNameEntry != null)
				speciesName = sciNameEntry.getKey();
		}
		if(speciesName == null && !nameClassMap.isEmpty())
			speciesName= nameClassMap.entrySet().iterator().next().getKey();

		return speciesName;
	}

	@Override
	public String toString() {
		return getSpeciesPrimaryName();
	}

	@Override
	public int compareTo(LIMSBioSpecies o) {
		return taxonomyId.compareTo(o.getTaxonomyId());
	}

	/**
	 * @return the nameClassMap
	 */
	public Map<String, String> getNameClassMap() {
		return nameClassMap;
	}
}
