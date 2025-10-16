/*******************************************************************************
 * 
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.io.Serializable;

public class MotrpacSampleType implements Serializable, Comparable<MotrpacSampleType>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2783592257580435395L;
	private String sampleType;
	private String description;
	
	public MotrpacSampleType(String sampleType, String description) {
		super();
		this.sampleType = sampleType;
		this.description = description;
	}

	@Override
	public int compareTo(MotrpacSampleType o) {
		return sampleType.compareTo(o.getSampleType());
	}

	/**
	 * @return the sampleType
	 */
	public String getSampleType() {
		return sampleType;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return sampleType;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MotrpacSampleType.class.isAssignableFrom(obj.getClass()))
            return false;

        final MotrpacSampleType other = (MotrpacSampleType) obj;

        if ((this.sampleType == null) ? (other.getSampleType() != null) : !this.sampleType.equals(other.getSampleType()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.sampleType != null ? this.sampleType.hashCode() : 0);
        return hash;
    }
	
}
