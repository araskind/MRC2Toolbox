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

package edu.umich.med.mrc2.datoolbox.data.cpdcoll;

public class CpdMetadataField implements Comparable<CpdMetadataField>{

	private String id;
	private String name;
	private CpdMetadataFieldCategory category;
	
	public CpdMetadataField(
			String id, 
			String name,
			CpdMetadataFieldCategory category) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
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
	
	public CpdMetadataFieldCategory getCategory() {
		return category;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CpdMetadataField.class.isAssignableFrom(obj.getClass()))
            return false;

        final CpdMetadataField other = (CpdMetadataField) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(CpdMetadataField o) {
		return this.name.compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return name;
	}
}


