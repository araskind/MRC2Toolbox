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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignFactorEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignFactorListener;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ExperimentDesignFactorFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;

public class ExperimentDesignFactor implements Comparable<ExperimentDesignFactor>, Serializable, Renamable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1305079422724830005L;
	private String factorName;
	private String factorDescription;
	private String factorId;
	private Set<ExperimentDesignLevel> factorLevels;
	private boolean enabled;
	private boolean nameIsValid;
	private Set<ExperimentDesignFactorListener> eventListeners;
	private boolean suppressEvents;

	public ExperimentDesignFactor(String factorName) {
		super();
		this.factorName = factorName;
		this.factorId = DataPrefix.EXPERIMENTAL_FACTOR.getName() + UUID.randomUUID().toString();
		factorLevels = new TreeSet<ExperimentDesignLevel>();
		enabled = true;
		eventListeners = ConcurrentHashMap.newKeySet();
		suppressEvents = false;
	}

	public ExperimentDesignFactor(String factorName, String factorId) {
		super();
		this.factorName = factorName;
		this.factorId = factorId;
		factorLevels = new TreeSet<ExperimentDesignLevel>();
		enabled = true;
		suppressEvents = false;
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	//	For LIMS copy
	public ExperimentDesignFactor(
			String factorId,
			String factorName, 
			String factorDescription) {
		super();
		this.factorId = factorId;
		this.factorName = factorName;
		this.factorDescription = factorDescription;	
		factorLevels = new TreeSet<ExperimentDesignLevel>();
		enabled = true;
		suppressEvents = true;
	}

	public void addListener(ExperimentDesignFactorListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}

	public void removeListener(ExperimentDesignFactorListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}

	public void removeAllListeners() {
		eventListeners = ConcurrentHashMap.newKeySet();
	}

	public void fireExperimentDesignFactorEvent(ParameterSetStatus newStatus) {

		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		if(!suppressEvents) {

			ExperimentDesignFactorEvent event = new ExperimentDesignFactorEvent(this, newStatus);
			eventListeners.stream().forEach(l -> l.factorStatusChanged(event));
		}
	}

	@Override
	public int compareTo(ExperimentDesignFactor o) {
		return this.factorName.compareTo(o.getName());
	}

	public String getFactorDescription() {
		return factorDescription;
	}

	public String getFactorId() {
		return factorId;
	}

	public String getName() {
		return factorName;
	}

	public ExperimentDesignLevel getLevelByName(String levelName) {

		return factorLevels.stream().
			filter(l -> l.getName().equals(levelName)).
			findFirst().orElse(null);
	}

	public ExperimentDesignLevel getLevelById(String id) {

		return factorLevels.stream().
			filter(l -> l.getLevelId().equals(id)).
			findFirst().orElse(null);
	}
	
	public Collection<ExperimentDesignLevel> getLevels() {
		return factorLevels;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void removeLevel(ExperimentDesignLevel level2remove) {

		factorLevels.remove(level2remove);
		level2remove.setParentFactor(null);
		fireExperimentDesignFactorEvent(ParameterSetStatus.REMOVED);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setFactorDescription(String factorDescription) {
		this.factorDescription = factorDescription;
	}

	public void setName(String factorName) {
		this.factorName = factorName;
	}

	@Override
	public String toString() {
		return this.factorName;
	}

	public boolean nameIsValid() {
		return nameIsValid;
	}

	public void setNameValid(boolean valid) {
		this.nameIsValid = valid;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!ExperimentDesignFactor.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExperimentDesignFactor other = (ExperimentDesignFactor) obj;

        if ((this.factorName == null) ? (other.getName() != null) : !this.factorName.equals(other.getName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.factorName != null ? this.factorName.hashCode() : 0);
        return hash;
    }

	public void addLevel(ExperimentDesignLevel newLevel) {

		factorLevels.add(newLevel);
		newLevel.setParentFactor(this);
		fireExperimentDesignFactorEvent(ParameterSetStatus.ADDED);
	}

	/**
	 * @param suppressEvents the suppressEvents to set
	 */
	public void setSuppressEvents(boolean suppressEvents) {
		this.suppressEvents = suppressEvents;
	}

	/**
	 * @return the eventListeners
	 */
	public Set<ExperimentDesignFactorListener> getEventListeners() {
		return eventListeners;
	}
	
	public Element getXmlElement() {
		
		Element experimentDesignFactorElement = 
				new Element(ObjectNames.ExperimentDesignFactor.name());
		
		if(factorId != null)
			experimentDesignFactorElement.setAttribute(
					CommonFields.Id.name(), factorId);
		
		if(factorName != null)
			experimentDesignFactorElement.setAttribute(
					CommonFields.Name.name(), factorName);
		
		ProjectStoreUtils.addDescriptionElement(
				factorDescription, experimentDesignFactorElement);
		
		experimentDesignFactorElement.setAttribute(
				CommonFields.Enabled.name(), Boolean.toString(enabled));
		
		Element levelSetElement = 
				new Element(ExperimentDesignFactorFields.LevelSet.name());
		
		for(ExperimentDesignLevel level : factorLevels)
			levelSetElement.addContent(level.getXmlElement());
		
		experimentDesignFactorElement.addContent(levelSetElement);
		
		return experimentDesignFactorElement;
	}
	
	public ExperimentDesignFactor(Element experimentDesignFactorElement) {
		super();
		
		factorId = 
				experimentDesignFactorElement.getAttributeValue(CommonFields.Id.name());
		factorName = 
				experimentDesignFactorElement.getAttributeValue(CommonFields.Name.name());

		//	TODO remove
		factorDescription = 
				experimentDesignFactorElement.getAttributeValue(CommonFields.Description.name());
		if(factorDescription == null)
			factorDescription = ProjectStoreUtils.getDescriptionFromElement(experimentDesignFactorElement);
		
		enabled = Boolean.parseBoolean(
				experimentDesignFactorElement.getAttributeValue(CommonFields.Enabled.name()));
		
		eventListeners = ConcurrentHashMap.newKeySet();
		suppressEvents = false;
		factorLevels = new TreeSet<ExperimentDesignLevel>();
		List<Element> levelListElements = 
				experimentDesignFactorElement.getChild(ExperimentDesignFactorFields.LevelSet.name()).
				getChildren(ObjectNames.ExperimentDesignLevel.name());
		if(!levelListElements.isEmpty()) {
			
			for(Element levelElement : levelListElements) {
				
				ExperimentDesignLevel level = new ExperimentDesignLevel(levelElement);
				level.setParentFactor(this);
				factorLevels.add(level);
			}
		}
	}
}
