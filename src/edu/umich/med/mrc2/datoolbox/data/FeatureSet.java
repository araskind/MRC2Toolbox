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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSetProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public abstract class FeatureSet implements Serializable, Comparable<FeatureSet>, Renamable, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7949191254093826935L;
	protected String featureSetName;
	protected boolean active;
	protected boolean locked;
	protected transient Set<FeatureSetListener> eventListeners;
	protected boolean nameIsValid;
	protected boolean suppressEvents;
	protected Map<FeatureSetProperties,XmlStorable>properties;
	
	public FeatureSet(String name) {

		this.featureSetName = name;
		active = false;
		eventListeners = ConcurrentHashMap.newKeySet();
		properties = new TreeMap<FeatureSetProperties,XmlStorable>();
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
				fireFeatureSetEvent(ParameterSetStatus.DISABLED);
		}
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!FeatureSet.class.isAssignableFrom(obj.getClass()))
            return false;

        final FeatureSet other = (FeatureSet) obj;

        if ((this.featureSetName == null) ? (other.getName() != null) : !this.featureSetName.equals(other.getName()))
            return false;
              
        return true;
    }
	
	@Override
	public int compareTo(FeatureSet o) {
		
		if(o == null)
			return 1;
		
		if(this.featureSetName == null && o.getName() == null)
			return 0;
		
		if(this.featureSetName == null && o.getName() != null)
			return -1;
		
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
	
	public void setProperty(FeatureSetProperties fsProperty, XmlStorable value) {
		
		if(fsProperty != null && value != null 
				&& value.getClass().equals(fsProperty.getClazz()))
			properties.put(fsProperty, value);	
	}
	
	public Object getProperty(FeatureSetProperties property) {
		
		if(property == null)
			return null;
		else
			return properties.get(property);		
	}

	public Map<FeatureSetProperties, XmlStorable> getProperties() {
		return properties;
	}
	
	public FeatureSet(Element featureSetElement) {

		eventListeners = ConcurrentHashMap.newKeySet();
		properties = new TreeMap<FeatureSetProperties,XmlStorable>();
		featureSetName = featureSetElement.getAttributeValue(CommonFields.Name.name());
		active = Boolean.parseBoolean(
				featureSetElement.getAttributeValue(CommonFields.Enabled.name()));

		List<Element>propertyElementList = 
				featureSetElement.getChild(CommonFields.Properties.name()).
				getChildren(CommonFields.Property.name());
		for(Element propertyElement : propertyElementList) {
			
			FeatureSetProperties property = null;
			String propertyName = 
					propertyElement.getAttributeValue(CommonFields.Name.name());
			
			if(propertyName != null && !propertyName.isBlank())
				property =  FeatureSetProperties.getOptionByName(propertyName);
			
			if(property == null)
				continue;
			
			String propertyClazzName = 
					propertyElement.getAttributeValue(CommonFields.Id.name());
			
			Class<?> MyPropertyClass = null;
			try {
				MyPropertyClass = Class.forName(propertyClazzName);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(MyPropertyClass != null 
					&& XmlStorable.class.isAssignableFrom(MyPropertyClass)) {
				
				Constructor<?> xmlElementConstructor = null;
				try {
					xmlElementConstructor = MyPropertyClass.getConstructor(Element.class);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(xmlElementConstructor != null) {
					
					XmlStorable propValue = null;
					try {
						propValue = (XmlStorable) xmlElementConstructor.newInstance(propertyElement.getChildren().get(0));
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(propValue != null)
						properties.put(property, propValue);
				}
			}			
		}
	}
	
	@Override
	public Element getXmlElement() {
		
		Element featureSetElement = 
			new Element(ObjectNames.FeatureSet.name());
		featureSetElement.setAttribute(CommonFields.Name.name(), featureSetName);
		featureSetElement.setAttribute(
				CommonFields.Enabled.name(), Boolean.toString(active));
		
		Element propertiesElement = new Element(CommonFields.Properties.name());
		for(Entry<FeatureSetProperties,XmlStorable>pEntry : properties.entrySet()) {
			
			Element propertyElement = new Element(CommonFields.Property.name());
			propertyElement.setAttribute(CommonFields.Name.name(), pEntry.getKey().name());
			propertyElement.setAttribute(
					CommonFields.Id.name(), pEntry.getKey().getClazz().getName());
			propertyElement.addContent(pEntry.getValue().getXmlElement());
			
			propertiesElement.addContent(propertyElement);
		}	
		featureSetElement.addContent(propertiesElement);
		return featureSetElement;
	}
}










