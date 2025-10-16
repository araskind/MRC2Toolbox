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

public class BinnerAdduct implements Serializable, Comparable<BinnerAdduct>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3484302685539857049L;
	private String id;
	private String binnerName;
	private int tier;
	private Adduct chargeCarrier;
	private AdductExchange adductExchange;
	private BinnerNeutralMassDifference binnerNeutralMassDifference;
	private double mass;
	
	public BinnerAdduct(
			String id, 
			String binnerName, 
			int charge, 
			int tier, 
			Adduct chargeCarrier,
			AdductExchange adductExchange, 
			BinnerNeutralMassDifference binnerNeutralMassDifference) {
		super();
		this.id = id;
		this.binnerName = binnerName;
		this.tier = tier;
		this.chargeCarrier = chargeCarrier;
		this.adductExchange = adductExchange;
		this.binnerNeutralMassDifference = binnerNeutralMassDifference;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	

	public String getBinnerName() {
		return binnerName;
	}

	public void setBinnerName(String binnerName) {
		this.binnerName = binnerName;
	}
	
	public String getName() {
		
		if(chargeCarrier != null)
			return chargeCarrier.getName();
		
		if(adductExchange != null)
			return adductExchange.getName();
		
		if(binnerNeutralMassDifference != null)
			return binnerNeutralMassDifference.getName();
		
		return null;
	}

	public int getCharge() {
		
		if(chargeCarrier != null)
			return chargeCarrier.getCharge();
		
		if(adductExchange != null)
			return adductExchange.getCharge();
		
		return 0;
	}
	
	public Double getMass() {
		
		if(chargeCarrier != null)
			return chargeCarrier.getMassCorrection();
		
		if(adductExchange != null)
			return adductExchange.getMassDifference();
		
		if(binnerNeutralMassDifference != null
				&& Math.abs(binnerNeutralMassDifference.getMassCorrection()) > 0.0d)
			return binnerNeutralMassDifference.getMassCorrection();
		
		if(mass > 0.0d)
			return mass;
		
		return null;
	}

	public int getTier() {
		return tier;
	}

	public void setTier(int tier) {
		this.tier = tier;
	}

	public Adduct getChargeCarrier() {
		return chargeCarrier;
	}

	public void setChargeCarrier(Adduct chargeCarrier) {
		this.chargeCarrier = chargeCarrier;
		this.adductExchange = null;
		this.binnerNeutralMassDifference = null;
	}

	public AdductExchange getAdductExchange() {
		return adductExchange;
	}

	public void setAdductExchange(AdductExchange adductExchange) {
		this.adductExchange = adductExchange;		
		this.chargeCarrier = null;
		this.binnerNeutralMassDifference = null;
	}

	public BinnerNeutralMassDifference getBinnerNeutralMassDifference() {
		return binnerNeutralMassDifference;
	}

	public void setBinnerNeutralMassDifference(BinnerNeutralMassDifference binnerNeutralMassDifference) {
		this.binnerNeutralMassDifference = binnerNeutralMassDifference;
		this.chargeCarrier = null;
		this.adductExchange = null;
	}
	
	public Polarity getPolarity() {
		
		if(getCharge() < 0)
			return Polarity.Negative;
		else if (getCharge() > 0)
			return Polarity.Positive;
		else
			return Polarity.Neutral;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!BinnerAdduct.class.isAssignableFrom(obj.getClass()))
            return false;

        final BinnerAdduct other = (BinnerAdduct) obj;
        
        if(!this.id.equals(other.getId()))
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
	public int compareTo(BinnerAdduct o) {
		return binnerName.compareTo(o.getBinnerName());
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

}

