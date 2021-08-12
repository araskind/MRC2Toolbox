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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameCategory;

public class CompoundNameSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7643108334066362805L;
	private String compoundAccession;
	private TreeMap<String, CompoundNameCategory>synonyms;
	private String primaryName;
	private String iupacName;

	public CompoundNameSet(String compoundAccession) {
		super();
		this.compoundAccession = compoundAccession;
		synonyms = new TreeMap<String, CompoundNameCategory>();
	}

	public String getPrimaryName() {
		return primaryName;
	}

	//	TODO update database table with new primary name
	public void setPrimaryName(String primaryName) {

		TreeMap<String, CompoundNameCategory> synonymsTmp = new TreeMap<String, CompoundNameCategory>();
		synonymsTmp.put(primaryName, CompoundNameCategory.PRI);

		for (Entry<String, CompoundNameCategory> entry : synonyms.entrySet()) {

			if(!entry.getKey().equals(primaryName)) {

				if(entry.getValue().equals(CompoundNameCategory.PRI))
					synonymsTmp.put(entry.getKey(), CompoundNameCategory.SYN);
				else
					synonymsTmp.put(entry.getKey(), entry.getValue());
			}
		}
		synonyms = synonymsTmp;
		this.primaryName = primaryName;
	}

	public String getIupacName() {
		return iupacName;
	}
	
	public String getSystematicName() {
		
		Optional<Entry<String, CompoundNameCategory>> sysPair = 
				synonyms.entrySet().stream().
				filter(e -> e.getValue().equals(CompoundNameCategory.SYS)).
				findFirst();
		if(sysPair.isPresent())
			return sysPair.get().getKey();
		
		return null;
	}
	
	public String getBrandName() {
		
		Optional<Entry<String, CompoundNameCategory>> sysPair = 
				synonyms.entrySet().stream().
				filter(e -> e.getValue().equals(CompoundNameCategory.BRN)).
				findFirst();
		if(sysPair.isPresent())
			return sysPair.get().getKey();
		
		return null;
	}

	public void setIupacName(String iupacName) {

		TreeMap<String, CompoundNameCategory> synonymsTmp = new TreeMap<String, CompoundNameCategory>();
		synonymsTmp.put(iupacName, CompoundNameCategory.IUP);

		for (Entry<String, CompoundNameCategory> entry : synonyms.entrySet()) {

			if(!entry.getKey().equals(iupacName)) {

				if(entry.getValue().equals(CompoundNameCategory.IUP))
					synonymsTmp.put(entry.getKey(), CompoundNameCategory.SYN);
				else
					synonymsTmp.put(entry.getKey(), entry.getValue());
			}
		}
		synonyms = synonymsTmp;
		this.iupacName = iupacName;
	}

	public String getCompoundAccession() {
		return compoundAccession;
	}

	public TreeMap<String, CompoundNameCategory> getSynonyms() {
		if(synonyms == null)
			synonyms = new TreeMap<String, CompoundNameCategory>();

		return synonyms;
	}

	public void addSynonym(String newName) {
		synonyms.put(newName, CompoundNameCategory.SYN);
	}

	public void addName(String newName, String type) {

		if(newName == null || type == null)
			return;

		CompoundNameCategory cat = 
				CompoundNameCategory.getCompoundNameCategoryByName(type);
		
		if(cat == null)
			throw new IllegalArgumentException("Invalid compound name category " + type);
		
		if(cat.equals(CompoundNameCategory.PRI))
			setPrimaryName(newName);		
		else if(cat.equals(CompoundNameCategory.IUP))
			setIupacName(newName);		
		else
			synonyms.put(newName, cat);
	}

	public void removeNames(Collection<String>namesToRemove) {

		for(String name : namesToRemove) {

			synonyms.remove(name);

			if(name.equals(primaryName))
				primaryName = null;

			if(name.equals(iupacName))
				iupacName = null;
		}
		if(primaryName == null) {
			primaryName = synonyms.keySet().stream().findFirst().get();
			synonyms.put(primaryName, CompoundNameCategory.PRI);
		}
	}
	
	public String getNextSynonym() {
		 return synonyms.keySet().stream().findFirst().orElse(null);
	}
}
















