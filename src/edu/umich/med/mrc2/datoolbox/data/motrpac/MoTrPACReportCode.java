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

public class MoTrPACReportCode implements Serializable, Comparable<MoTrPACReportCode>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8082700696182949026L;
	private String optionName;
	private String optionCode;
	
	public MoTrPACReportCode(String optionName, String optionCode) {
		super();
		this.optionName = optionName;
		this.optionCode = optionCode;
	}

	@Override
	public int compareTo(MoTrPACReportCode o) {
		return optionCode.compareTo(o.getOptionCode());
	}

	public String getOptionName() {
		return optionName;
	}

	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	public String getOptionCode() {
		return optionCode;
	}

	public void setOptionCode(String optionCode) {
		this.optionCode = optionCode;
	}
	
	@Override
	public String toString() {
		return optionName + " ( " + optionCode + " )";
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MoTrPACReportCode.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACReportCode other = (MoTrPACReportCode) obj;

        if ((this.optionCode == null) ? (other.getOptionCode() != null) : !this.optionCode.equals(other.getOptionCode()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.optionCode != null ? this.optionCode.hashCode() : 0);
        return hash;
    }
}
