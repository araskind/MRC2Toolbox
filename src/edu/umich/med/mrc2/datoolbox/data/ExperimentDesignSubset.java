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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetListener;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentDesignSubsetFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

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
	private TreeMap<ExperimentDesignFactor, 
		TreeMap<Integer, ExperimentDesignLevel>>orderedLevelMap;

	public ExperimentDesignSubset(String name) {

		subsetName = name;
		active = false;
		locked = false;
		suppressEvents = false;
		status = ParameterSetStatus.CREATED;
		orderedFactorMap = new TreeMap<Integer, ExperimentDesignFactor>();
		orderedLevelMap = new TreeMap<ExperimentDesignFactor, 
				TreeMap<Integer, ExperimentDesignLevel>>();
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void copyDesignSubset(ExperimentDesignSubset source) {

		orderedFactorMap.clear();
		orderedLevelMap.clear();
		int fCount = 0;
		for (Entry<ExperimentDesignFactor, ExperimentDesignLevel[]> entry : source.getOrderedDesign().entrySet()) {

			orderedFactorMap.put(fCount, entry.getKey());
			TreeMap<Integer, ExperimentDesignLevel> orderedLevels = 
					new TreeMap<Integer, ExperimentDesignLevel>();
			for(int i=0; i<entry.getValue().length; i++)
				orderedLevels.put(i, entry.getValue()[i]);

			orderedLevelMap.put( entry.getKey(), orderedLevels);
			fCount++;
		}
	}

	public void addLevel(ExperimentDesignLevel newLevel, boolean notifyListeners) {

		if(newLevel == null)
			return;

		suppressEvents = true;
		
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
		suppressEvents = false;
		
		if(notifyListeners)
			setStatus(ParameterSetStatus.CHANGED);
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

		if(factorsInNewOrder.length == 0) 
			return;
		
		orderedFactorMap.clear();
		for(int i=0; i<factorsInNewOrder.length; i++)
			orderedFactorMap.put(i, factorsInNewOrder[i]);

		setStatus(ParameterSetStatus.CHANGED);	
	}

	//	TODO for now rely on GUI to pass the expected set of levels
	public void reorderLevels(ExperimentDesignLevel[] levelsInNewOrder) {

		if(levelsInNewOrder.length == 0)
			return;
		
		TreeMap<Integer, ExperimentDesignLevel> levelMap = 
				new TreeMap<Integer, ExperimentDesignLevel>();

		for(int i=0; i<levelsInNewOrder.length; i++)
			levelMap.put(i, levelsInNewOrder[i]);

		ExperimentDesignFactor factor = levelsInNewOrder[0].getParentFactor();
		orderedLevelMap.put(factor, levelMap);		
		setStatus(ParameterSetStatus.CHANGED);		
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

	public void removeLevel(ExperimentDesignLevel levelToRemove, boolean notifyListeners) {

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

		if(notifyListeners)
			setStatus(ParameterSetStatus.CHANGED);
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

		if (!newStatus.equals(status)) {

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

	public void addFactor(ExperimentDesignFactor selectedFactor, boolean notifyListeners) {

		suppressEvents = true;
		selectedFactor.getLevels().forEach(l -> addLevel(l, notifyListeners));
		suppressEvents = false;		
		if(notifyListeners)
			setStatus(ParameterSetStatus.CHANGED);
	}

	public void removeFactor(ExperimentDesignFactor selectedFactor, boolean notifyListeners) {

		suppressEvents = true;

		//	Remove all levels
		for(ExperimentDesignLevel newLevel : selectedFactor.getLevels())
			removeLevel(newLevel, notifyListeners);

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
		if(notifyListeners)
			setStatus(ParameterSetStatus.CHANGED);
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
		
		Element subsetElement = 
				new Element(ObjectNames.ExperimentDesignSubset.name());
		if(subsetName != null)
			subsetElement.setAttribute(CommonFields.Name.name(), subsetName);
		
		subsetElement.setAttribute(
				ExperimentDesignSubsetFields.IsActive.name(), Boolean.toString(active));
		subsetElement.setAttribute(
				ExperimentDesignSubsetFields.Islocked.name(), Boolean.toString(locked));

		Element factorMapContainerElement = 
				new Element(ExperimentDesignSubsetFields.FactorMap.name());
		for(Entry<Integer, ExperimentDesignFactor>fme :  orderedFactorMap.entrySet()) {
			
			Element factorMapElement = 
					new Element(ExperimentDesignSubsetFields.FactorMapElement.name());
			factorMapElement.setAttribute(
					ExperimentDesignSubsetFields.FOrder.name(), 
					Integer.toString(fme.getKey()));
			factorMapElement.setAttribute(
					ExperimentDesignSubsetFields.Fid.name(), 
					fme.getValue().getName());
			factorMapContainerElement.addContent(factorMapElement);
		}
		subsetElement.addContent(factorMapContainerElement);
		
		Element levelMapContainerElement = 
				new Element(ExperimentDesignSubsetFields.LevelMap.name());		
		for(Entry<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>>ole : orderedLevelMap.entrySet()) {
			
			Element levelMapElement = 
					new Element(ExperimentDesignSubsetFields.LevelMapElement.name());
			levelMapElement.setAttribute(
					ExperimentDesignSubsetFields.FactorKey.name(), 
					ole.getKey().getName());
			ArrayList<String>orderedLevels = new ArrayList<String>();
			for(Entry<Integer, ExperimentDesignLevel>le : ole.getValue().entrySet())
				orderedLevels.add(Integer.toString(le.getKey()) + "," + le.getValue().getName());
			
			levelMapElement.setText(StringUtils.join(orderedLevels, "|"));
			levelMapContainerElement.addContent(levelMapElement);
		}		
		subsetElement.addContent(levelMapContainerElement);
		
		return subsetElement;
	}
	
	public ExperimentDesignSubset(
			Element designSubsetElement,
			ExperimentDesign parentDesign) {
		subsetName = designSubsetElement.getAttributeValue(CommonFields.Name.name());
		active = Boolean.parseBoolean(designSubsetElement.getAttributeValue(
				ExperimentDesignSubsetFields.IsActive.name()));
		locked = Boolean.parseBoolean(designSubsetElement.getAttributeValue(
				ExperimentDesignSubsetFields.Islocked.name()));
		
		status = ParameterSetStatus.CREATED;
		orderedFactorMap = new TreeMap<Integer, ExperimentDesignFactor>();
		orderedLevelMap = new TreeMap<ExperimentDesignFactor, 
				TreeMap<Integer, ExperimentDesignLevel>>();
		eventListeners = ConcurrentHashMap.newKeySet();
		
		List<Element> factorMapElements = 
				designSubsetElement.getChild(
						ExperimentDesignSubsetFields.FactorMap.name()).getChildren();
			
		for(Element fmElement : factorMapElements) {
			
			Integer fPosition = Integer.parseInt(
					fmElement.getAttributeValue(ExperimentDesignSubsetFields.FOrder.name()));
			String factorName = 
					fmElement.getAttributeValue(ExperimentDesignSubsetFields.Fid.name());			
			if(factorName != null) {
				ExperimentDesignFactor factor = parentDesign.getFactorByName(factorName);
				if(factor != null)
					orderedFactorMap.put(fPosition, factor);
			}
		}	
		List<Element> levelMapElements = 
				designSubsetElement.getChild(ExperimentDesignSubsetFields.LevelMap.name()).
				getChildren(ExperimentDesignSubsetFields.LevelMapElement.name());
			
		for(Element lmElement : levelMapElements) {
			
			String factorName = lmElement.getAttributeValue(
					ExperimentDesignSubsetFields.FactorKey.name());
			if(factorName != null) {
				ExperimentDesignFactor factor = parentDesign.getFactorByName(factorName);
				if(factor != null) {
					
					String[]levelStrings = lmElement.getText().split("\\|");
					if(levelStrings.length > 0) {
						
						TreeMap<Integer, ExperimentDesignLevel>factorLevelMap = 
								new TreeMap<Integer, ExperimentDesignLevel>();
						for(String ls : levelStrings) {
							String[]parts = ls.split(",");
							Integer order = Integer.parseInt(parts[0]);
							ExperimentDesignLevel l = factor.getLevelByName(parts[1]);
							if(l != null)
								factorLevelMap.put(order, l);
						}
						orderedLevelMap.put(factor, factorLevelMap);
					}
				}
			}
		}
	}

	public TreeMap<ExperimentDesignFactor, TreeMap<Integer, ExperimentDesignLevel>> getOrderedLevelMap() {
		return orderedLevelMap;
	}
}

















