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
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class WorklistItem implements Comparable<WorklistItem>, Serializable, XmlStorable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5767562224759235903L;
	protected DataFile dataFile;
	protected String dataFileName;
	protected TreeMap<String, String> properties;
	protected Date timeStamp;

	public WorklistItem(DataFile dataFile) {
		super();
		this.dataFile = dataFile;
		properties = new TreeMap<String, String>();
	}

	public void setProperty(String propertyName, String propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the properties
	 */
	public TreeMap<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param dataFile the dataFile to set
	 */
	public void setDataFile(DataFile dataFile) {
		this.dataFile = dataFile;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!WorklistItem.class.isAssignableFrom(obj.getClass()))
            return false;

        final WorklistItem other = (WorklistItem) obj;

        if ((this.dataFile == null) ? (other.getDataFile() != null) : !this.dataFile.equals(other.getDataFile()))
            return false;
        
        if ((this.timeStamp == null) ? (other.getTimeStamp() != null) : !this.timeStamp.equals(other.getTimeStamp()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.dataFile != null ? this.dataFile.hashCode() : 0) + 
        		(this.timeStamp != null ? this.timeStamp.hashCode() : 0);

        return hash;
    }

	@Override
	public int compareTo(WorklistItem o) {
		int result = dataFile.compareTo(o.getDataFile());
		if(result == 0 && timeStamp != null && o.getTimeStamp() != null)
			result = timeStamp.compareTo(o.getTimeStamp());
			
		return result;	
	}

	public WorklistItem(Element worklistItemElement) {
		
		super();		
		timeStamp = ProjectStoreUtils.getDateFromAttribute(
				worklistItemElement, CommonFields.DateCreated);
		dataFileName = worklistItemElement.getAttributeValue(ObjectNames.DataFile.name());
		
		properties = new TreeMap<String, String>();
		List<Element>propertyElementList = 
				worklistItemElement.getChild(CommonFields.Properties.name()).
				getChildren(CommonFields.Property.name());
		if(!propertyElementList.isEmpty()) {
			
			for(Element propertyElement : propertyElementList) {
				properties.put(propertyElement.getAttributeValue(CommonFields.Id.name()), 
						propertyElement.getAttributeValue(CommonFields.Name.name()));
			}
		}
	}
	
	@Override
	public Element getXmlElement() {
		
		Element worklistItemElement = new Element(ObjectNames.WorklistItem.name());
		worklistItemElement.setAttribute(ObjectNames.DataFile.name(), dataFile.getName());
		ProjectStoreUtils.setDateAttribute(
				timeStamp, CommonFields.DateCreated, worklistItemElement);
		Element propertyListElement = new Element(CommonFields.Properties.name());		
		for(Entry<String, String>pair : properties.entrySet()) {
			
			Element propertyElement = new Element(CommonFields.Property.name());	
			propertyElement.setAttribute(CommonFields.Id.name(), pair.getKey());
			propertyElement.setAttribute(CommonFields.Name.name(), pair.getValue());
			propertyListElement.addContent(propertyElement);
		}
		worklistItemElement.addContent(propertyListElement);
		
		return worklistItemElement;
	}

	public String getDataFileName() {
		return dataFileName;
	}
}












