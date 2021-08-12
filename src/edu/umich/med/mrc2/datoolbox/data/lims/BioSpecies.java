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
import java.util.TreeMap;

public class BioSpecies implements Serializable, Comparable<BioSpecies>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2451132253655810585L;
	private int taxId;
	private Map<String,String>nameMap;

	public static final String SCIENTIFIC_NAME = "scientific name";
	public static final String COMMON_NAME = "common name";

	public BioSpecies(int taxId) {
		super();
		this.taxId = taxId;
		nameMap = new TreeMap<String,String>();
	}

	public void addName(String name, String nameClass) {
		nameMap.put(name, nameClass);
	}

	public String getName() {

		if(nameMap.isEmpty())
			return null;

		if(nameMap.containsValue(COMMON_NAME))
			return nameMap.entrySet().stream().
					filter(e -> e.getValue().equals(COMMON_NAME)).
					findFirst().get().getKey();

		if(nameMap.containsValue(SCIENTIFIC_NAME))
			return nameMap.entrySet().stream().
					filter(e -> e.getValue().equals(SCIENTIFIC_NAME)).
					findFirst().get().getKey();

		return nameMap.entrySet().stream().findFirst().get().getKey();
	}

	/**
	 * @return the taxId
	 */
	public int getTaxId() {
		return taxId;
	}

	@Override
	public int compareTo(BioSpecies o) {
		return Integer.compare(taxId, o.getTaxId());
	}
}












