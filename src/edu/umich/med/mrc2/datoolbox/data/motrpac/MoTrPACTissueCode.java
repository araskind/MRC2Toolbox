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

public class MoTrPACTissueCode implements Serializable, Comparable<MoTrPACTissueCode>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1922852950138938909L;
	private String code;
	private String description;
	
	public MoTrPACTissueCode(String sampleType, String description) {
		super();
		this.code = sampleType;
		this.description = description;
	}

	@Override
	public int compareTo(MoTrPACTissueCode o) {
		return code.compareTo(o.getCode());
	}

	/**
	 * @return the sampleType
	 */
	public String getCode() {
		return code;
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
		return description + " (" + code + ")";
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MoTrPACTissueCode.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACTissueCode other = (MoTrPACTissueCode) obj;

        if ((this.code == null) ? (other.getCode() != null) : !this.code.equals(other.getCode()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }	
}
