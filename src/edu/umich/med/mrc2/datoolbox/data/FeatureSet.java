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
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;

public abstract class FeatureSet implements Serializable, Comparable<FeatureSet>, Renamable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7949191254093826935L;
	protected String featureSetName;
	protected boolean active;
	protected boolean locked;
//	protected ParameterSetStatus status;
	protected Set<FeatureSetListener> eventListeners;
	protected boolean nameIsValid;
	private boolean suppressEvents;
	
	public FeatureSet(String name) {

		featureSetName = name;
		active = false;
//		status = ParameterSetStatus.CREATED;
		eventListeners = ConcurrentHashMap.newKeySet();
	}
	
	public abstract boolean containsFeature(MsFeature feature);
	public abstract void removeFeature(MsFeature featureToRemove);
	public abstract void removeFeatures(Collection<MsFeature> featuresToRemove);
	
	public void removeFeatures(MsFeature[] featuresToRemove) {
		removeFeatures(Arrays.asList(featuresToRemove));
	}
	
	public void addListener(FeatureSetListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.add(listener);
	}
	
	public void removeListener(EventListener listener) {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		eventListeners.remove(listener);
	}
	
	public Set<FeatureSetListener> getListeners() {

		if(eventListeners == null)
			eventListeners = ConcurrentHashMap.newKeySet();

		return eventListeners;
	}
	
//	public void fireFeatureSetEvent() {
//
//		if(suppressEvents)
//			return;
//		
//		if(eventListeners == null){
//			eventListeners = ConcurrentHashMap.newKeySet();
//			return;
//		}
//		FeatureSetEvent event = new FeatureSetEvent(this);
//		eventListeners.stream().forEach(l -> ((FeatureSetListener) l).
//				featureSetStatusChanged(event));
//	}

	public void fireFeatureSetEvent(ParameterSetStatus newStatus) {

		if(suppressEvents)
			return;
		
		if(eventListeners == null){
			eventListeners = ConcurrentHashMap.newKeySet();
			return;
		}
		FeatureSetEvent event = new FeatureSetEvent(this, newStatus);
		eventListeners.stream().forEach(l -> ((FeatureSetListener) l).
				featureSetStatusChanged(event));
	}
	
	public String getName() {
		return featureSetName;
	}
	
//	public ParameterSetStatus getStatus() {
//		return status;
//	}

	public boolean isActive() {
		return active;
	}

	public boolean isLocked() {
		return locked;
	}

	public void removeAllListeners() {
		eventListeners = ConcurrentHashMap.newKeySet();
	}
	
	public void setActive(boolean isActive) {

		boolean wasActive = active;
		this.active = isActive;
		if (active != wasActive) {

			if (active)
				fireFeatureSetEvent(ParameterSetStatus.ENABLED);		
			else
				fireFeatureSetEvent(ParameterSetStatus.DISABLED);;
		}
	}
	
	@Override
	public int compareTo(FeatureSet o) {
		return this.featureSetName.compareTo(o.getName());
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setName(String subsetName) {

		this.featureSetName = subsetName;
		fireFeatureSetEvent(ParameterSetStatus.CHANGED);
	}

	@Override
	public String toString() {
		return featureSetName;
	}

	public boolean nameIsValid() {
		return nameIsValid;
	}

	public void setNameValid(boolean valid) {
		this.nameIsValid = valid;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.featureSetName != null ? this.featureSetName.hashCode() : 0);
        return hash;
    }
    
	public void setSuppressEvents(boolean suppressEvents) {
		this.suppressEvents = suppressEvents;
	}
}
