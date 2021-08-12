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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class AdductExchange implements Serializable, Comparable<AdductExchange> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5729539463544551842L;
	private String id;
	private Adduct leavingAdduct;
	private Adduct comingAdduct;
	double massDifference;

	public AdductExchange(String id, Adduct comingAdduct, Adduct leavingAdduct) {

		this.id = id;
		this.comingAdduct = comingAdduct;
		this.leavingAdduct = leavingAdduct;
		massDifference = MsUtils.calculateExchangeMassDifference(this);
	}

	public Adduct getComingAdduct() {
		return comingAdduct;
	}

	public Adduct getLeavingAdduct() {
		return leavingAdduct;
	}

	public double getMassDifference() {
		return massDifference;
	}
	
	public void setComingAdduct(Adduct adductTwo) {
		this.comingAdduct = adductTwo;
	}

	public void setLeavingAdduct(Adduct adductOne) {
		this.leavingAdduct = adductOne;
	}
	
	public int getCharge() {
		return comingAdduct.getCharge() - leavingAdduct.getCharge();
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!AdductExchange.class.isAssignableFrom(obj.getClass()))
            return false;

        final AdductExchange other = (AdductExchange) obj;
        
        if (!this.leavingAdduct.equals(other.getLeavingAdduct()))
            return false;
        
        if (!this.comingAdduct.equals(other.getComingAdduct()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return leavingAdduct.hashCode() + comingAdduct.hashCode() ;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(AdductExchange o) {
		
		int res = this.comingAdduct.getName().compareTo(o.getComingAdduct().getName());
		if(res == 0)
			res = this.leavingAdduct.getName().compareTo(o.getLeavingAdduct().getName());
		
		return res;
	}

	public String getName() {
		return comingAdduct.getName() + " <=> " + leavingAdduct.getName();
	}
}
