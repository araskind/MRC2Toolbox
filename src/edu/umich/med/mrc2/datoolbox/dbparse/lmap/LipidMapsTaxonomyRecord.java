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

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps.LipidMapsClassification;

public class LipidMapsTaxonomyRecord implements Comparable<LipidMapsTaxonomyRecord>{

	private String lmid;
	private String formula;
	private String abbreviation;
	private Map<LipidMapsClassification,String>lmTaxonomy;
		
	public LipidMapsTaxonomyRecord(String lmid, String formula) {
		super();
		this.lmid = lmid;
		this.formula = formula;
		lmTaxonomy = new TreeMap<LipidMapsClassification,String>();
	}
	
	public boolean hasSameTaxonomy(LipidMapsTaxonomyRecord other) {
		
		Map<LipidMapsClassification, String> otherTaxonomy = other.getLmTaxonomy();
		
		for(LipidMapsClassification level : LipidMapsClassification.values()) {
			
			if(lmTaxonomy.get(level) == null 
					&& otherTaxonomy.get(level) != null)
				return false;
			
			if(lmTaxonomy.get(level) != null 
					&& otherTaxonomy.get(level) == null)
				return false;
			
			if(lmTaxonomy.get(level) != null 
					&& otherTaxonomy.get(level) != null
					&& !lmTaxonomy.get(level).equals(otherTaxonomy.get(level)))
				return false;
		}
		return true;
	}

	public String getLmid() {
		return lmid;
	}
	
	@Override
	public String toString() {
		return lmid;
	}
	
	public Map<LipidMapsClassification,String> getLmTaxonomy() {
		return lmTaxonomy;
	}
	
	public String getTaxonomyCodeForLevel(LipidMapsClassification level) {	
		return lmTaxonomy.get(level);
	}
    
    public void addTaxonomyLevel(LipidMapsClassification level, String code) {    	
    	lmTaxonomy.put(level, code);
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



