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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.io.Serializable;

public class MotracSubjectType implements Serializable, Comparable<MotracSubjectType>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -758840722533948436L;
	private String subjectType;
	
	public MotracSubjectType(String subjectType) {
		super();
		this.subjectType = subjectType;
	}
	
	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}
	
	@Override
	public int compareTo(MotracSubjectType o) {
		return subjectType.compareTo(o.getSubjectType());
	}

	
	@Override
	public String toString() {
		return subjectType;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MotracSubjectType.class.isAssignableFrom(obj.getClass()))
            return false;

        final MotracSubjectType other = (MotracSubjectType) obj;

        if ((this.subjectType == null) ? (other.getSubjectType() != null) : !this.subjectType.equals(other.getSubjectType()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.subjectType != null ? this.subjectType.hashCode() : 0);
        return hash;
    }
}
