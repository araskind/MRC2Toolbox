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

package edu.umich.med.mrc2.datoolbox.dbparse.load.npa;

import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class NPARecord implements Comparable<NPARecord>{
	
	private String id;
	private String name;
	private CompoundIdentity compoundId;
	private NPALiteratureReference reference;	
	private String originType;
	private String genus;
	private String species;	
	private Collection<String>gnpsIds;
	private Collection<String>mibigIds;
	
	public NPARecord(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		compoundId = new CompoundIdentity();
		gnpsIds = new TreeSet<String>();
		mibigIds = new TreeSet<String>();
	}
	
    @Override
    public int hashCode() {
        return id.hashCode();
	}

	@Override
	public int compareTo(NPARecord o) {
		return this.id.compareTo(o.getId());
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public CompoundIdentity getCompoundIdentity() {
		return compoundId;
	}

	public NPALiteratureReference getReference() {
		return reference;
	}

	public void setReference(NPALiteratureReference reference) {
		this.reference = reference;
	}

	public String getOriginType() {
		return originType;
	}

	public void setOriginType(String originType) {
		this.originType = originType;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}
	
	public Collection<String> getMibigIds() {
		return mibigIds;
	}
	
	public void addMibigId(String mibigId) {
		this.mibigIds.add(mibigId);
	}
	
	public Collection<String> getGnpsIds() {
		return gnpsIds;
	}
	
	public void addGnpsId(String gnpsId) {
		this.gnpsIds.add(gnpsId);
	}
}









