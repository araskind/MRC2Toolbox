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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class IonizationType implements Serializable, Comparable<IonizationType>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1624124601291085600L;
	private String id;
	private String description;

	public IonizationType(String id, String description) {
		super();
		this.id = id;
		this.description = description;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!IonizationType.class.isAssignableFrom(obj.getClass()))
            return false;

        final IonizationType other = (IonizationType) obj;

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

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int compareTo(IonizationType o) {
		return this.id.compareTo(o.getId());
	}
	
	public IonizationType(Element ionizationTypeElement) {
		
		super();
		id = ionizationTypeElement.getAttributeValue(
				CommonFields.Id.name());
		
		//	TODO remove
		description = ionizationTypeElement.getAttributeValue(
				CommonFields.Description.name());
		if(description == null)
			description = 
				ProjectStoreUtils.getDescriptionFromElement(ionizationTypeElement);
	}

	@Override
	public Element getXmlElement() {
		
		Element ionizationTypeElement = 
				new Element(ObjectNames.IonizationType.name());
		ionizationTypeElement.setAttribute(CommonFields.Id.name(), id);
		ProjectStoreUtils.addDescriptionElement(description, ionizationTypeElement);
			
		return ionizationTypeElement;
	}
}
