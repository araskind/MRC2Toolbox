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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class LipidMapsRecord implements Comparable<LipidMapsRecord>{

	private String lmid;
	private String commonName;
	private String sysName;
	private String abbreviation;
	private Collection<String>synonyms;
	private CompoundIdentity compoundIdentity;
	private Set<LipidMapsClassificationObject>lmTaxonomy;
		
	public LipidMapsRecord(String lmid, String commonName) {
		super();
		this.lmid = lmid;
		this.commonName = commonName;
		lmTaxonomy = new HashSet<LipidMapsClassificationObject>();
		synonyms = new TreeSet<String>();
		compoundIdentity = new CompoundIdentity();
	}

	public String getLmid() {
		return lmid;
	}

	public void setLmid(String lmid) {
		this.lmid = lmid;
	}

	public String getCommonName() {
		return commonName;
	}

	@Override
	public String toString() {
		return lmid;
	}
	
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
	}

	public void setCompoundIdentity(CompoundIdentity compoundIdentity) {
		this.compoundIdentity = compoundIdentity;
	}

	public Collection<String> getSynonyms() {
		return synonyms;
	}

	public Set<LipidMapsClassificationObject> getLmTaxonomy() {
		return lmTaxonomy;
	}
	
	public String getTaxonomyCodeForLevel(LipidMapsClassification level) {
		
		LipidMapsClassificationObject entry = lmTaxonomy.stream().
				filter(t -> t.getGroup().equals(level)).findFirst().orElse(null);
		if(entry == null)
			return null;
		else
			return entry.getCode();
	}

	@Override
	public int compareTo(LipidMapsRecord o) {
		return this.lmid.compareTo(o.getLmid()) ;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LipidMapsRecord.class.isAssignableFrom(obj.getClass()))
            return false;

        final LipidMapsRecord other = (LipidMapsRecord) obj;

        if ((this.lmid == null) ? (other.getLmid() != null) : !this.lmid.equals(other.getLmid()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lmid.hashCode();
    }
    
    public void addTaxonomyLevel(LipidMapsClassificationObject lmco) {    	
    	lmTaxonomy.add(lmco);
    }

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}	
}



