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

package edu.umich.med.mrc2.datoolbox.data.lims;

import java.io.Serializable;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class LIMSOrganization implements Serializable, Comparable<LIMSOrganization>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6705255794565922596L;
	protected String id;
	protected String name;
	protected String address;

	public LIMSOrganization(String id, String name, String address) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSOrganization.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSOrganization other = (LIMSOrganization) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

		if (obj == this)
			return true;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	@Override
	public int compareTo(LIMSOrganization o) {
		return id.compareTo(o.getId());
	}

	public String getOrganizationInfo() {
		return "<HTML><B>" + name + "</B><BR>" + address;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LIMSOrganization(Element limsOrganizationElement) {
		
		id = limsOrganizationElement.getAttributeValue(CommonFields.Id.name());
		name = limsOrganizationElement.getAttributeValue(CommonFields.Name.name());
		address = limsOrganizationElement.getAttributeValue(CommonFields.Address.name());
	}
	
	@Override
	public Element getXmlElement() {
		
		Element limsOrganizationElement = new Element(ObjectNames.LIMSOrganization.name());
		limsOrganizationElement.setAttribute(CommonFields.Id.name(), id);
		limsOrganizationElement.setAttribute(CommonFields.Name.name(), name);
		limsOrganizationElement.setAttribute(CommonFields.Address.name(), address);
		
		return limsOrganizationElement;
	}
}












