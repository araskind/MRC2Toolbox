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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class CompositeAdduct implements Adduct, Comparable<CompositeAdduct>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1236657935347735006L;
	private String id;
	private SimpleAdduct chargeCarrier;
	private Collection<SimpleAdduct>neutralLosses;
	private Collection<SimpleAdduct>neutralAdducts;
	private String description;
	private String cefNotation;
	private double massCorrection;
	
	public CompositeAdduct(String id, SimpleAdduct chargeCarrier, String description) {
		super();
		this.id = id;
		this.chargeCarrier = chargeCarrier;
		this.description = description;
		neutralLosses = new ArrayList<SimpleAdduct>();
		neutralAdducts = new ArrayList<SimpleAdduct>();
		massCorrection = 
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(this);
	}

	public CompositeAdduct(SimpleAdduct chargeCarrier2) {
		this(null, chargeCarrier2, null);
	}

	public SimpleAdduct getChargeCarrier() {
		return chargeCarrier;
	}

	public Collection<SimpleAdduct> getNeutralLosses() {
		return neutralLosses;
	}

	public Collection<SimpleAdduct> getNeutralAdducts() {
		return neutralAdducts;
	}
	
	public void addModification(SimpleAdduct mod) {
		
		if(mod.getModificationType().equals(ModificationType.LOSS))
			addNeutralLoss(mod);
		
		if(mod.getModificationType().equals(ModificationType.REPEAT))
			addNeutralAdduct(mod);
	}

	public void addNeutralLoss(SimpleAdduct loss) {
		neutralLosses.add(loss);
		Collections.sort((ArrayList<SimpleAdduct>)neutralLosses);
		massCorrection = 
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(this);
	}
	
	public void removeNeutralLoss(SimpleAdduct loss) {
		neutralLosses.remove(loss);
		Collections.sort((ArrayList<SimpleAdduct>)neutralLosses);
		massCorrection = 
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(this);
	}
	
	public void addNeutralAdduct(SimpleAdduct neutralAdduct) {
		neutralAdducts.add(neutralAdduct);
		Collections.sort((ArrayList<SimpleAdduct>)neutralAdducts);
		massCorrection = 
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(this);
	}
	
	public void removeNeutralAdduct(SimpleAdduct neutralAdduct) {
		neutralAdducts.remove(neutralAdduct);
		Collections.sort((ArrayList<SimpleAdduct>)neutralAdducts);
		massCorrection = 
				MsUtils.calculateMassCorrectionFromAddedRemovedGroups(this);
	}
	
	public int getCharge() {
		return chargeCarrier.getCharge();
	}
	
	public String getName() {
		
		if(neutralLosses.isEmpty() && neutralAdducts.isEmpty())
			return chargeCarrier.getName();
		
		ArrayList<String>nameParts = new ArrayList<String>();
		nameParts.add(chargeCarrier.getName());
		if(!neutralLosses.isEmpty()) {
			
			Map<SimpleAdduct, Long> countedLosses = neutralLosses.stream().
					collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			for(Entry<SimpleAdduct, Long> entry : countedLosses.entrySet()) {
				
				String name = entry.getKey().getName();
				if(entry.getValue() == 1) {
					if(name.startsWith("-"))
						nameParts.add(name);
					else
						nameParts.add("-" + name);
				}
				else {					
					nameParts.add("-" + Long.toString(entry.getValue()) + 
							"[" + name.replaceFirst("^-", "") + "]");
				}
			}			
		}
		if(!neutralAdducts.isEmpty()) {
			
			Map<SimpleAdduct, Long> countedRepeats = neutralAdducts.stream().
					collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			for(Entry<SimpleAdduct, Long> entry : countedRepeats.entrySet()) {
				
				if(entry.getValue() == 1) 
					nameParts.add("+" + entry.getKey().getName());
				else
					nameParts.add("+" + Long.toString(entry.getValue()) + "[" + entry.getKey().getName() + "]");
			}	
		}
		return  StringUtils.join(nameParts, " ");
	}

	@Override
	public int compareTo(CompositeAdduct o) {
		return getName().compareTo(o.getName());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompositeAdduct.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompositeAdduct other = (CompositeAdduct) obj;

        if (!this.getChargeCarrier().equals(other.getChargeCarrier()))
            return false;
        
        if(!CollectionUtils.isEqualCollection(neutralLosses, other.getNeutralLosses()))
        	 return false;
        
        if(!CollectionUtils.isEqualCollection(neutralAdducts, other.getNeutralAdducts()))
        	return false;
        
        return true;
    }

    @Override
    public int hashCode() {

        int hash = chargeCarrier.hashCode();
        for(Adduct loss : neutralLosses)
        	hash += loss.hashCode();
        	
        for(Adduct repeat : neutralAdducts)
        	hash += repeat.hashCode();
        
        return hash;
    }

	@Override
	public void finalizeModification() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getAbsoluteMassCorrection() {
		return Math.abs(massCorrection);
	}

	@Override
	public String getAddedGroup() {
		
		ArrayList<String>addedParts = new ArrayList<String>();
		if(chargeCarrier.getAddedGroup() != null && !chargeCarrier.getAddedGroup().isEmpty())
			addedParts.add(chargeCarrier.getAddedGroup());
		
		neutralLosses.stream().
			filter(l -> l.getAddedGroup() != null).
			filter(l -> !l.getAddedGroup().isEmpty()).
			forEach(l -> addedParts.add(l.getAddedGroup()));
		neutralAdducts.stream().
			filter(l -> l.getAddedGroup() != null).
			filter(l -> !l.getAddedGroup().isEmpty()).
			forEach(l -> addedParts.add(l.getAddedGroup()));
		return  StringUtils.join(addedParts, "");
	}

	@Override
	public String getCefNotation() {
		return cefNotation;
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
		return ModificationType.COMPOSITE;
	}

	@Override
	public int getOligomericState() {
		return chargeCarrier.getOligomericState();
	}

	@Override
	public Polarity getPolarity() {
		return chargeCarrier.getPolarity();
	}

	@Override
	public String getRemovedGroup() {
		
		ArrayList<String>removedParts = new ArrayList<String>();
		if(chargeCarrier.getRemovedGroup() != null && !chargeCarrier.getRemovedGroup().isEmpty())
			removedParts.add(chargeCarrier.getRemovedGroup());
		
		neutralLosses.stream().
			filter(l -> l.getRemovedGroup() != null).
			filter(l -> !l.getRemovedGroup().isEmpty()).
			forEach(l -> removedParts.add(l.getRemovedGroup()));
		neutralAdducts.stream().
			filter(l -> l.getRemovedGroup() != null).
			filter(l -> !l.getRemovedGroup().isEmpty()).
			forEach(l -> removedParts.add(l.getRemovedGroup()));
		
		return  StringUtils.join(removedParts, "");
	}
	
	public Map<SimpleAdduct, Long> getNeutralLossCounts() {
		Map<SimpleAdduct, Long> countedLosses = neutralLosses.stream().
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return countedLosses;
	}

	public Map<SimpleAdduct, Long> getNeutralAdductCounts() {
		Map<SimpleAdduct, Long> countedAdducts = neutralAdducts.stream().
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return countedAdducts;
	}
	
	@Override
	public boolean isEnabled() {
			return true;
	}

	@Override
	//	TODO may need a better check or may be not needed at all  - 
	//	formula of the compound itself has to be considered as well
	public boolean isHalogenated() {

		if(chargeCarrier.isHalogenated())
			return true;
		
		for(Adduct r : neutralAdducts) {
			if(r.isHalogenated())
				return true;
		}		
		return false;
	}

	@Override
	public void setAddedGroup(String addedGroup) {
		
	}

	@Override
	public void setCefNotation(String cefNotation) {
		this.cefNotation = cefNotation;
	}

	@Override
	public void setCharge(int charge) {

	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		
	}

	@Override
	public void setMassCorrection(double massCorrection) {
		this.massCorrection = massCorrection;
	}

	@Override
	public void setModificationType(ModificationType modificationType) {
		
	}

	@Override
	public void setName(String adductName) {
		
	}

	@Override
	public void setOligomericState(int oligomericState) {
		
	}

	@Override
	public void setRemovedGroup(String removedGroup) {
		
	}

	@Override
	public String getSmiles() {

		if(neutralLosses.isEmpty() && neutralAdducts.isEmpty())
			return null;
		
		//	TODO create reaction SMILES to represent losses and repeats? or better on  actual compounds
		ArrayList<String>smilesParts = new ArrayList<String>();
		neutralLosses.stream().filter(l -> l.getSmiles() != null).forEach(l -> smilesParts.add(l.getSmiles()));
		neutralAdducts.stream().filter(l -> l.getSmiles() != null).forEach(l -> smilesParts.add(l.getSmiles()));
		if(smilesParts.isEmpty())
			return null;
		else		
			return StringUtils.join(smilesParts, ".");		
	}

	@Override
	public void setSmiles(String smiles) {
		
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
	public String toString() {
		return getName();
	}	
}








