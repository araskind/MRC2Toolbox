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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class LibraryEntrySource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5566596535147341907L;
	String bulkId;
	double rt;
	Polarity polarity;
	Adduct adduct;

	public LibraryEntrySource(String bulkId, double rt, Polarity polarity, Adduct adduct) {
		super();
		this.bulkId = bulkId;
		this.rt = rt;
		this.polarity = polarity;
		this.adduct = adduct;
	}

	/**
	 * @return the bulkId
	 */
	public String getBulkId() {
		return bulkId;
	}

	/**
	 * @return the rt
	 */
	public double getRt() {
		return rt;
	}

	/**
	 * @return the polarity
	 */
	public Polarity getPolarity() {
		return polarity;
	}

	/**
	 * @return the adduct
	 */
	public Adduct getAdduct() {
		return adduct;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LibraryEntrySource.class.isAssignableFrom(obj.getClass()))
            return false;

        final LibraryEntrySource other = (LibraryEntrySource) obj;

        if ((this.bulkId == null) ? (other.getBulkId() != null) : !this.bulkId.equals(other.getBulkId()))
            return false;

        if (this.rt != other.getRt())
            return false;

        if ((this.adduct == null) ? (other.getAdduct() != null) : !this.adduct.equals(other.getAdduct()))
            return false;

        if ((this.polarity == null) ? (other.getPolarity() != null) : !this.polarity.equals(other.getPolarity()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.adduct.getName() != null ? this.adduct.getName().hashCode() : 0)
        		+ (this.bulkId != null ? this.bulkId.hashCode() : 0);
        return hash;
    }
}
