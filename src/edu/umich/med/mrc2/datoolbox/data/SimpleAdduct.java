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
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;

public class SimpleAdduct implements Adduct, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 143483281315699066L;
	
	private String id;
	private String adductName;
	private String description;
	private String addedGroup;
	private String removedGroup;
	private String smiles;
	private int charge;
	private int oligomericState;
	private double massCorrection;
	private ModificationType modificationType;
	private boolean enabled;
	private Map<AdductNotationType,String>notations;
	
	public SimpleAdduct(
			String id,
			String adductName, 
			String description, 
			int charge, 
			int oligomericState,
			double massCorrection, 
			ModificationType massModType) {
		
		this(id, adductName, description, charge, oligomericState, massCorrection, massModType, null);
	}

	public SimpleAdduct(
			String id,
			String adductName, 
			String description, 
			int charge, 
			int oligomericState,
			ModificationType massModType) {
		
		this(id, adductName, description, charge, oligomericState, 0.0d, massModType, null);
	}
	
	public SimpleAdduct(
			String id,
			String adductName, 
			String description, 
			int charge, 
			int oligomericState,
			double massCorrection, 
			ModificationType massModType,
			String smiles) {

		this(id, adductName, description, null, null, null, 
				charge, oligomericState, massCorrection, massModType, true);
	}
	
	public SimpleAdduct(
			String id,
			String adductName, 
			String description, 
			String addedGroup, 
			String removedGroup,
			String smiles, 
			int charge, 
			int oligomericState, 
			double massCorrection,
			ModificationType modificationType, 
			boolean enabled) {
		super();
		this.id = id;
		this.adductName = adductName;
		this.description = description;
		this.addedGroup = addedGroup;
		this.removedGroup = removedGroup;
		this.smiles = smiles;
		this.charge = charge;
		this.oligomericState = oligomericState;
		this.massCorrection = massCorrection;
		this.modificationType = modificationType;
		this.enabled = enabled;
		notations = new TreeMap<AdductNotationType,String>();
		
		finalizeModification();
	}

	@Override
	public int compareTo(Adduct o) {
		return adductName.compareTo(o.getName());
	}

	@Override
	public void finalizeModification() {

		if (addedGroup == null)
			addedGroup = "";

		if (removedGroup == null)
			removedGroup = "";

		if (oligomericState == 0)
			oligomericState = 1;
	}

	@Override
	public double getAbsoluteMassCorrection() {
		return (double) Math.abs(massCorrection);
	}

	@Override
	public String getAddedGroup() {
		return addedGroup;
	}

	@Override
	public int getCharge() {
		return charge;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public double getMassCorrection() {
		return massCorrection;
	}

	@Override
	public ModificationType getModificationType() {
		return modificationType;
	}

	@Override
	public String getName() {
		return adductName;
	}

	@Override
	public int getOligomericState() {
		return oligomericState;
	}

	@Override
	public Polarity getPolarity() {
		
		if(charge < 0)
			return Polarity.Negative;
		else if(charge > 0)
			return Polarity.Positive;
		else
			return Polarity.Neutral;
	}

	@Override
	public String getRemovedGroup() {
		return removedGroup;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isHalogenated() {

		boolean isHalogenated = false;

		if (addedGroup.contains("Cl") || addedGroup.contains("Br"))
			isHalogenated = true;

		return isHalogenated;
	}

	@Override
	public void setAddedGroup(String addedGroup) {
		this.addedGroup = addedGroup;
	}

	@Override
	public void setCharge(int charge) {
		this.charge = charge;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		this.enabled = isEnabled;
	}

	@Override
	public void setMassCorrection(double massCorrection) {
		this.massCorrection = massCorrection;
	}

	@Override
	public void setModificationType(ModificationType modificationType) {
		this.modificationType = modificationType;
	}

	@Override
	public void setName(String adductName) {
		this.adductName = adductName;
	}

	@Override
	public void setOligomericState(int oligomericState) {
		this.oligomericState = oligomericState;
	}

	@Override
	public void setRemovedGroup(String removedGroup) {
		this.removedGroup = removedGroup;
	}
	
	public boolean isEquivalent(SimpleAdduct other) {
		
		if (other == this)
			return true;

        if (other == null)
            return false;
        
        finalizeModification();
        other.finalizeModification();
        
        if(!modificationType.equals(other.getModificationType()))
        	return false;
        
        if(charge != other.getCharge())
        	 return false;
        
        if(oligomericState != other.getOligomericState())
        	return false;
        
        if(!MolFormulaUtils.haveSameElementalComposition(addedGroup, other.getAddedGroup()))
         	return false;
        
        if(!MolFormulaUtils.haveSameElementalComposition(removedGroup, other.getRemovedGroup()))
         	return false;

		if(smiles != null && other.getSmiles() != null) {
			
			if(!smiles.equals(other.getSmiles()))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.adductName;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!SimpleAdduct.class.isAssignableFrom(obj.getClass()))
            return false;

        final SimpleAdduct other = (SimpleAdduct) obj;

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
	public String getSmiles() {
		return smiles;
	}

	@Override
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}	

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}	

	@Override
	public void setNotationForType(AdductNotationType notationType, String notation) {
		notations.put(notationType, notation);
	}

	@Override
	public String getNotationForType(AdductNotationType notationType) {
		
		if(notations == null)
			notations = new TreeMap<AdductNotationType,String>();
		
		return notations.get(notationType);
	}
}
