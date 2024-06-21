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

package edu.umich.med.mrc2.datoolbox.dbparse.lmap;

import java.util.HashSet;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;
import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassificationObject;

public class LipidMapsTaxonomyRecord implements Comparable<LipidMapsTaxonomyRecord>{

	private String lmid;
	private String formula;
	private String abbreviation;
	private Set<LipidMapsClassificationObject>lmTaxonomy;
		
	public LipidMapsTaxonomyRecord(String lmid, String formula) {
		super();
		this.lmid = lmid;
		this.formula = formula;
		lmTaxonomy = new HashSet<LipidMapsClassificationObject>();
	}

	public String getLmid() {
		return lmid;
	}
	
	@Override
	public String toString() {
		return lmid;
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
    
    public void addTaxonomyLevel(LipidMapsClassificationObject lmco) {    	
    	lmTaxonomy.add(lmco);
    }

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getFormula() {
		return formula;
	}	

	@Override
	public int compareTo(LipidMapsTaxonomyRecord o) {
		return this.lmid.compareTo(o.getLmid()) ;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LipidMapsTaxonomyRecord.class.isAssignableFrom(obj.getClass()))
            return false;

        final LipidMapsTaxonomyRecord other = (LipidMapsTaxonomyRecord) obj;

        if ((this.lmid == null) ? (other.getLmid() != null) : !this.lmid.equals(other.getLmid()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return lmid.hashCode();
    }
}



