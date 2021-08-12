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

public class LIMSSampleType implements Serializable, Comparable<LIMSSampleType>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2807976532155039757L;
	private String id;
	private String name;

	public LIMSSampleType(String sampleTypeId, String sampleTypeName) {
		super();
		this.id = sampleTypeId;
		this.name = sampleTypeName;
	}

	/**
	 * @return the sampleTypeId
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the sampleTypeName
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @param sampleTypeId the sampleTypeId to set
	 */
	public void setId(String sampleTypeId) {
		this.id = sampleTypeId;
	}

	/**
	 * @param sampleTypeName the sampleTypeName to set
	 */
	public void setName(String sampleTypeName) {
		this.name = sampleTypeName;
	}

	@Override
	public int compareTo(LIMSSampleType o) {
		return name.compareTo(o.getName());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LIMSSampleType.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSSampleType other = (LIMSSampleType) obj;

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
}
