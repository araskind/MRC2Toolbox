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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdFilterType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.utils.ChemInfoUtils;

public class CompoundIdFilter {

	private String id;
	private String name;
	private CompoundIdFilterType filterType;
	private Set<String>filterComponents;
	private boolean matchAllSmarts;
		
	public CompoundIdFilter(
			String id, String name, CompoundIdFilterType filterType) {
		super();
		this.id = id;
		this.name = name;
		this.filterType = filterType;
		matchAllSmarts = false;
		filterComponents = new TreeSet<String>();
	}

	public CompoundIdFilter(CompoundIdFilterType filterType) {
		
		this(null, null, filterType);
		id = DataPrefix.COMPOUND_ID_FILTER.getName() +
				UUID.randomUUID().toString().substring(0, 12);
	}
	
	public CompoundIdFilter(String name, CompoundIdFilterType filterType) {
		
		this(null, name, filterType);
		id = DataPrefix.COMPOUND_ID_FILTER.getName() +
				UUID.randomUUID().toString().substring(0, 12);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CompoundIdFilterType getFilterType() {
		return filterType;
	}

	public Set<String> getFilterComponents() {
		return filterComponents;
	}

	public Collection<MsFeatureIdentity> filterIdentifications(
			Collection<MsFeatureIdentity> idsToFilter) {

		if(filterComponents.isEmpty())
			return idsToFilter;
		
		Collection<MsFeatureIdentity>filteredIds = new HashSet<MsFeatureIdentity>();
		for(MsFeatureIdentity id : idsToFilter){
			
			CompoundIdentity cid = id.getCompoundIdentity();
			if(cid == null)
				continue;
			
			if(filterType.equals(CompoundIdFilterType.COMPOUND_NAME) 
					&& cid.getName() != null) {
				
				String name = cid.getName();
				String match = filterComponents.stream().
						filter(n -> n.equalsIgnoreCase(name)).findAny().orElse(null);
				if(match != null)
					filteredIds.add(id);
			}
			if(filterType.equals(CompoundIdFilterType.COMPOUND_DATABASE_ID) 
					&& cid.getPrimaryDatabaseId() != null) {
				
				String dbId = cid.getPrimaryDatabaseId();
				String match = filterComponents.stream().
						filter(n -> n.equals(dbId)).findAny().orElse(null);
				if(match != null)
					filteredIds.add(id);
			}
			if(filterType.equals(CompoundIdFilterType.SMILES) 
					&& cid.getSmiles() != null) {
				
				String smiles = cid.getSmiles();
				String match = filterComponents.stream().
						filter(n -> n.equalsIgnoreCase(smiles)).findAny().orElse(null);
				if(match != null)
					filteredIds.add(id);
			}
			if(filterType.equals(CompoundIdFilterType.SMARTS) 
					&& cid.getSmiles() != null) {
				
				String smiles = cid.getSmiles();			
				if(matchAllSmarts) {
					
					boolean allMatched = true;
					for(String smarts : filterComponents) {
						
						if(!ChemInfoUtils.doSMILESmatchSMATRSpattern(smiles, smarts)) {
							allMatched = false;
							break;
						}
					}
					if(allMatched)
						filteredIds.add(id);
				}
				else {
					String match = filterComponents.stream().
							filter(n -> ChemInfoUtils.doSMILESmatchSMATRSpattern(smiles, n)).
							findAny().orElse(null);
					if(match != null)
						filteredIds.add(id);
				}
			}
			if(filterType.equals(CompoundIdFilterType.INCHI_KEY) 
					&& cid.getInChiKey() != null) {
				
				String inchiKey = cid.getInChiKey();
				String match = filterComponents.stream().
						filter(n -> n.equalsIgnoreCase(inchiKey)).findAny().orElse(null);
				if(match != null)
					filteredIds.add(id);
			}
			if(filterType.equals(CompoundIdFilterType.INCHI_KEY2D) 
					&& cid.getInChiKey() != null) {
				
				String inchiKey2D = cid.getInChiKey().substring(0, 14);
				String match = filterComponents.stream().
						filter(n -> n.equalsIgnoreCase(inchiKey2D)).findAny().orElse(null);
				if(match != null)
					filteredIds.add(id);
			}
		}		
		return filteredIds;
	}

	public boolean isMatchAllSmarts() {
		return matchAllSmarts;
	}

	public void setMatchAllSmarts(boolean matchAllSmarts) {
		this.matchAllSmarts = matchAllSmarts;
	}
}




