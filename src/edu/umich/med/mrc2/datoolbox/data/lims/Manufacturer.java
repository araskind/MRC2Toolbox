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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ManufacturerFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class Manufacturer implements Serializable, Comparable<Manufacturer>,XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4202108621145097640L;
	
	private String id;
	private String name;
	private String catalogWebAddress;

	public Manufacturer(
			String id, 
			String name, 
			String catalogWebAddress) {
		super();
		this.id = id;
		this.name = name;
		this.catalogWebAddress = catalogWebAddress;
	}

	@Override
	public int compareTo(Manufacturer o) {
		return this.name.compareTo(o.getName());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getCatalogWebAddress() {
		return catalogWebAddress;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setCatalogWebAddress(String catalogWebAddress) {
		this.catalogWebAddress = catalogWebAddress;
	}

	@Override
	public String toString() {
		return name;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

		if (obj == this)
			return true;
		
        if (!Manufacturer.class.isAssignableFrom(obj.getClass()))
            return false;

        final Manufacturer other = (Manufacturer) obj;

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

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public Manufacturer(Element manufacturerElement) {
		
		super();
		id = manufacturerElement.getAttributeValue(CommonFields.Id.name());
		name = manufacturerElement.getAttributeValue(CommonFields.Name.name());
		catalogWebAddress = manufacturerElement.getAttributeValue(
				ManufacturerFields.CatalogWebAddress.name());
	}

	@Override
	public Element getXmlElement() {
		
		Element manufacturerElement = 
        		new Element(ObjectNames.Manufacturer.name());
		if(id == null) {
			Manufacturer mnf = IDTDataCache.getManufacturerByName(name);
			if(mnf != null)
				id = mnf.getId();
		}
		manufacturerElement.setAttribute(CommonFields.Id.name(), id);
		manufacturerElement.setAttribute(CommonFields.Name.name(), name);
		
		if(catalogWebAddress != null && !catalogWebAddress.isBlank())
			manufacturerElement.setAttribute(
					ManufacturerFields.CatalogWebAddress.name(), catalogWebAddress);
		
		return manufacturerElement;
	}
}








