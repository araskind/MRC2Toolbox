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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class BinnerNeutralMassDifference implements Serializable, Comparable<BinnerNeutralMassDifference> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1236657935347735006L;
	private String id;
	private Collection<SimpleAdduct>neutralAdducts;
	private Collection<SimpleAdduct>neutralLosses;
	private String binnerName;
	private double massCorrection;
	
	public BinnerNeutralMassDifference(String id, String binnerName) {
		super();
		this.id = id;
		this.binnerName = binnerName;
		neutralAdducts = new ArrayList<SimpleAdduct>();
		neutralLosses = new ArrayList<SimpleAdduct>();
	}
	
	public Collection<SimpleAdduct> getNeutralAdducts() {
		return neutralAdducts;
	}
	
	public void addNeutralAdduct(SimpleAdduct neutralAdduct) {
		neutralAdducts.add(neutralAdduct);
		Collections.sort((ArrayList<SimpleAdduct>)neutralAdducts);
		calculateMassCorrection();
	}
	
	public void removeNeutralAdduct(SimpleAdduct neutralAdduct) {
		neutralAdducts.remove(neutralAdduct);
		Collections.sort((ArrayList<SimpleAdduct>)neutralAdducts);
		calculateMassCorrection();
	}
	
	public void addNeutralLoss(SimpleAdduct neutralLoss) {
		neutralLosses.add(neutralLoss);
		Collections.sort((ArrayList<SimpleAdduct>)neutralLosses);
		calculateMassCorrection();
	}
	
	public void removeNeutralLoss(SimpleAdduct neutralLoss) {
		neutralLosses.remove(neutralLoss);
		Collections.sort((ArrayList<SimpleAdduct>)neutralLosses);
		calculateMassCorrection();
	}
	
	private void calculateMassCorrection() {
		
		massCorrection = 0.0d;
		if(!neutralAdducts.isEmpty())
			massCorrection += neutralAdducts.stream().
				mapToDouble(l -> l.getMassCorrection()).sum();
		
		if(!neutralLosses.isEmpty())
			massCorrection += neutralLosses.stream().
				mapToDouble(l -> l.getMassCorrection()).sum();
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
	
	@Override
	public String toString() {
		return binnerName;
	}

	public String getName() {
		
		if(neutralAdducts.isEmpty() && neutralLosses.isEmpty())
			return "";
		
		ArrayList<String>nameParts = new ArrayList<String>();
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
	public int compareTo(BinnerNeutralMassDifference o) {
		return this.binnerName.compareTo(o.getBinnerName());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!BinnerNeutralMassDifference.class.isAssignableFrom(obj.getClass()))
            return false;

        final BinnerNeutralMassDifference other = (BinnerNeutralMassDifference) obj;
        
        if(!this.id.equals(other.getId()))
        	return false;
        
//        if(!CollectionUtils.isEqualCollection(neutralAdducts, other.getNeutralAdducts()))
//        	return false;
        
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;     	
        for(Adduct repeat : neutralAdducts)
        	hash += repeat.hashCode();
        
        for(Adduct loss : neutralLosses)
        	hash += loss.hashCode();
        
        return hash;
    }

	public Map<SimpleAdduct, Long> getNeutralAdductCounts() {
		Map<SimpleAdduct, Long> countedAdducts = neutralAdducts.stream().
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return countedAdducts;
	}
	
	public Map<SimpleAdduct, Long> getNeutralLossCounts() {
		Map<SimpleAdduct, Long> countedAdducts = neutralLosses.stream().
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return countedAdducts;
	}
	
	public double getMassCorrection() {
		return massCorrection;
	}

	public Collection<SimpleAdduct> getNeutralLosses() {
		return neutralLosses;
	}
	
}










