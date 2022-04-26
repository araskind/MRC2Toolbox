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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetListener;

public class ExperimentDesignSubset implements Comparable<ExperimentDesignSubset>, Serializable, Renamable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7557840378408674325L;
	private String subsetName;
	private boolean active;
	private boolean locked;
	private boolean suppressEvents;
	private boolean nameIsValid;

	private ParameterSetStatus status;
	private Set<ExperimentDesignSubsetListener> eventListeners;
	private TreeMap<Integer, ExperimentDesignFactor>orderedFactorMap;
	private TreeMap<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>>orderedLevelMap;

	public ExperimentDesignSubset(String name) {

		subsetName = name;
		active = false;
		locked = false;
		suppressEvents = false;
		status = ParameterSetStatus.CREATED;
		orderedFactorMap = new TreeMap<Integer, ExperimentDesignFactor>();
		orderedLevelMap = new TreeMap<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>>();
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void copyDesignSubset(ExperimentDesignSubset source) {

		orderedFactorMap.clear();
		orderedLevelMap.clear();
		int fCount = 0;
		for (Entry<ExperimentDesignFactor, ExperimentDesignLevel[]> entry : source.getOrderedDesign().entrySet()) {

			orderedFactorMap.put(fCount, entry.getKey());
			TreeMap<Integer, ExperimentDesignLevel> orderedLevels = new TreeMap<Integer, ExperimentDesignLevel>();
			for(int i=0; i<entry.getValue().length; i++)
				orderedLevels.put(i, entry.getValue()[i]);

			orderedLevelMap.put( entry.getKey(), orderedLevels);
			fCount++;
		}
	}

	public void addLevel(ExperimentDesignLevel newLevel) {

		if(newLevel == null)
			return;

		//	Update factor map
		if(orderedFactorMap == null)
			orderedFactorMap = new TreeMap<Integer, ExperimentDesignFactor>();

		if(orderedFactorMap.isEmpty())
			orderedFactorMap.put(0, newLevel.getParentFactor());

		if(!orderedFactorMap.containsValue(newLevel.getParentFactor())) {

			int next = orderedFactorMap.navigableKeySet().last() + 1;
			orderedFactorMap.put(next, newLevel.getParentFactor());
		}
		//	Update level map
		if(orderedLevelMap == null)
			orderedLevelMap = new TreeMap<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>>();

		if(!orderedLevelMap.containsKey(newLevel.getParentFactor()))
			orderedLevelMap.put(newLevel.getParentFactor(), new TreeMap<Integer, ExperimentDesignLevel>());

		if(orderedLevelMap.get(newLevel.getParentFactor()).isEmpty())
			orderedLevelMap.get(newLevel.getParentFactor()).put(0, newLevel);

		if(!orderedLevelMap.get(newLevel.getParentFactor()).containsValue(newLevel)) {

			int next = orderedLevelMap.get(newLevel.getParentFactor()).navigableKeySet().last() + 1;
			orderedLevelMap.get(newLevel.getParentFactor()).put(next, newLevel);
		}
		status = ParameterSetStatus.CHANGED;
		fireExpDesignSetEvent();
	}

	public Map<ExperimentDesignFactor, ExperimentDesignLevel[]>getOrderedDesign(){

		LinkedHashMap<ExperimentDesignFactor, ExperimentDesignLevel[]>orderedMap
			= new LinkedHashMap<ExperimentDesignFactor, ExperimentDesignLevel[]>();

		for (Entry<Integer, ExperimentDesignFactor> entry : orderedFactorMap.entrySet()) {

			Collection<ExperimentDesignLevel> levels = orderedLevelMap.get(entry.getValue()).values();
			ExperimentDesignLevel[] levelsArray = levels.toArray(new ExperimentDesignLevel[levels.size()]);
			orderedMap.put(entry.getValue(), levelsArray);
		}
		return orderedMap;
	}

	//	TODO for now rely on GUI to pass the expected set of factors
	public void reorderFactors(ExperimentDesignFactor[] factorsInNewOrder) {

		if(factorsInNewOrder.length > 0) {

			orderedFactorMap.clear();

			for(int i=0; i<factorsInNewOrder.length; i++)
				orderedFactorMap.put(i, factorsInNewOrder[i]);

			status = ParameterSetStatus.CHANGED;
			fireExpDesignSetEvent();
		}
	}

	//	TODO for now rely on GUI to pass the expected set of levels
	public void reorderLevels(ExperimentDesignLevel[] levelsInNewOrder) {

		if(levelsInNewOrder.length > 0) {

			ExperimentDesignFactor factor = levelsInNewOrder[0].getParentFactor();
			TreeMap<Integer, ExperimentDesignLevel> levelMap = orderedLevelMap.get(factor);
			levelMap.clear();

			for(int i=0; i<levelsInNewOrder.length; i++)
				levelMap.put(i, levelsInNewOrder[i]);

			status = ParameterSetStatus.CHANGED;
			fireExpDesignSetEvent();
		}
	}

	public Set<ExperimentDesignSubsetListener> getEventListeners() {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		return eventListeners;
	}

	public void addListener(ExperimentDesignSubsetListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	@Override
	public int compareTo(ExperimentDesignSubset o) {
		return subsetName.compareTo(o.getName());
	}

	public void fireExpDesignSetEvent(ParameterSetStatus newStatus) {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		if(!suppressEvents) {

			ExperimentDesignSubsetEvent event = new ExperimentDesignSubsetEvent(this, newStatus);
			eventListeners.stream().forEach(l -> l.designSetStatusChanged(event));
		}
	}

	public void fireExpDesignSetEvent() {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		if(!suppressEvents) {

			ExperimentDesignSubsetEvent event = new ExperimentDesignSubsetEvent(this);
			eventListeners.stream().forEach(l -> l.designSetStatusChanged(event));
		}
	}

	public TreeSet<ExperimentDesignLevel> getDesignMap() {

		if(orderedLevelMap == null)
			orderedLevelMap =
				new TreeMap<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>>();

		return orderedLevelMap.values().stream().
			flatMap(f -> f.values().stream()).
			collect(Collectors.toCollection(TreeSet::new));
	}

	public ParameterSetStatus getStatus() {
		return status;
	}

	public String getName() {
		return subsetName;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isLocked() {
		return locked;
	}

	public void removeAllListeners() {
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void removeLevel(ExperimentDesignLevel levelToRemove) {

		if(!orderedFactorMap.values().contains(levelToRemove.getParentFactor()))
			return;

		Collection<ExperimentDesignLevel> levels = orderedLevelMap.get(levelToRemove.getParentFactor()).values();
		ArrayList<ExperimentDesignLevel>newLevels = new ArrayList<ExperimentDesignLevel>();
		for(ExperimentDesignLevel level : levels) {

			if(!level.equals(levelToRemove))
				newLevels.add(level);
		}
		orderedLevelMap.get(levelToRemove.getParentFactor()).clear();

		for(int i=0; i<newLevels.size(); i++)
			orderedLevelMap.get(levelToRemove.getParentFactor()).put(i, newLevels.get(i));

		status = ParameterSetStatus.CHANGED;
		fireExpDesignSetEvent();
	}

	public void removeListener(ExperimentDesignSubsetListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public final void setStatus(ParameterSetStatus newStatus) {

		if (newStatus.equals(status)) {

			this.status = newStatus;
			this.fireExpDesignSetEvent();
		}
	}

	public void setName(String subsetName) {

		this.subsetName = subsetName;
		status = ParameterSetStatus.CHANGED;
		fireExpDesignSetEvent();
	}

	@Override
	public String toString() {
		return subsetName;
	}

	public void addFactor(ExperimentDesignFactor selectedFactor) {

		suppressEvents = true;
		selectedFactor.getLevels().forEach(l -> addLevel(l));
		suppressEvents = false;
		status = ParameterSetStatus.CHANGED;
		fireExpDesignSetEvent();
	}

	public void removeFactor(ExperimentDesignFactor selectedFactor) {

		suppressEvents = true;

		//	Remove all levels
		for(ExperimentDesignLevel newLevel : selectedFactor.getLevels())
			removeLevel(newLevel);

		//	Remove and re-order factors
		ArrayList<ExperimentDesignFactor>newFactors = new ArrayList<ExperimentDesignFactor>();
		for(ExperimentDesignFactor factor : orderedFactorMap.values()) {

			if(!factor.equals(selectedFactor))
				newFactors.add(factor);
		}
		orderedFactorMap.clear();
		for(int i=0; i<newFactors.size(); i++)
			orderedFactorMap.put(i, newFactors.get(i));

		orderedLevelMap.remove(selectedFactor);

		suppressEvents = false;
		status = ParameterSetStatus.CHANGED;
		fireExpDesignSetEvent();
	}

	public boolean nameIsValid() {
		return nameIsValid;
	}

	public void setNameValid(boolean valid) {
		this.nameIsValid = valid;
	}

	public Collection<ExperimentDesignLevel>getLevelsForFactor(ExperimentDesignFactor factor){

		if(orderedLevelMap.containsKey(factor))
			return orderedLevelMap.get(factor).values();
		else
			return null;
	}
	
	public Element getXmlElement() {
		//	TODO
		return null;
	}
	
	public ExperimentDesignSubset(Element EexperimentDesignSubsetElement) {
		//	TODO
	}
}

















