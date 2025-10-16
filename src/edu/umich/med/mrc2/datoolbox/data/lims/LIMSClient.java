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

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSClientFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class LIMSClient implements Serializable, Comparable<LIMSClient>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1518812378942204059L;
	private String id;
	private String department;
	private String laboratory;
	private LIMSOrganization organization;
	private LIMSUser principalInvestigator;
	private LIMSUser contactPerson;
	private String mailingAddress;

	public LIMSClient(String id, String department, String laboratory, String mailingAddress) {
		super();
		this.id = id;
		this.department = department;
		this.laboratory = laboratory;
		this.mailingAddress = mailingAddress;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSClient.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSClient other = (LIMSClient) obj;

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
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @return the laboratory
	 */
	public String getLaboratory() {
		return laboratory;
	}

	/**
	 * @return the organization
	 */
	public LIMSOrganization getOrganization() {
		return organization;
	}

	/**
	 * @return the principalInvestigator
	 */
	public LIMSUser getPrincipalInvestigator() {
		return principalInvestigator;
	}

	/**
	 * @return the contactPerson
	 */
	public LIMSUser getContactPerson() {
		return contactPerson;
	}

	/**
	 * @return the mailingAddress
	 */
	public String getMailingAddress() {
		return mailingAddress;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(LIMSOrganization organization) {
		this.organization = organization;
	}

	/**
	 * @param principalInvestigator the principalInvestigator to set
	 */
	public void setPrincipalInvestigator(LIMSUser principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	/**
	 * @param contactPerson the contactPerson to set
	 */
	public void setContactPerson(LIMSUser contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getClientInfo() {

		return
			"<HTML><B>" + organization.getName() + "</B><BR>" +
			department  + "<BR>" +
			laboratory + "<BR>" +
			organization.getAddress();
	}

	@Override
	public int compareTo(LIMSClient o) {
		return id.compareTo(o.getId());
	}
	
	public LIMSClient(Element limsClientElement) {
		
		id = limsClientElement.getAttributeValue(CommonFields.Id.name());
		department = limsClientElement.getAttributeValue(LIMSClientFields.Dept.name());
		laboratory = limsClientElement.getAttributeValue(LIMSClientFields.Lab.name());
		mailingAddress = limsClientElement.getAttributeValue(CommonFields.Address.name());
		
		String piId = 
				limsClientElement.getAttributeValue(LIMSClientFields.PI.name());
		if(piId != null && !piId.isBlank())
			principalInvestigator = IDTDataCache.getUserById(piId);
		
		String cpId = 
				limsClientElement.getAttributeValue(LIMSClientFields.Contact.name());
		if(cpId != null && !cpId.isBlank())
			contactPerson = IDTDataCache.getUserById(cpId);
		
		Element organizationElement = 
				limsClientElement.getChild(ObjectNames.LIMSOrganization.name());
		if(organizationElement != null)
			organization = new LIMSOrganization(organizationElement);				
	}

	@Override
	public Element getXmlElement() {
		
		Element limsClientElement = new Element(ObjectNames.LIMSClient.name());
		
		limsClientElement.setAttribute(CommonFields.Id.name(), id);
		
		if(department != null)
			limsClientElement.setAttribute(LIMSClientFields.Dept.name(), department);
		
		if(laboratory != null)
			limsClientElement.setAttribute(LIMSClientFields.Lab.name(), laboratory);
		
		if(mailingAddress != null)
			limsClientElement.setAttribute(CommonFields.Address.name(), mailingAddress);
		
		if(principalInvestigator != null)
			limsClientElement.setAttribute(LIMSClientFields.PI.name(), principalInvestigator.getId());
		
		if(contactPerson != null)
			limsClientElement.setAttribute(LIMSClientFields.Contact.name(), contactPerson.getId());
		
		if(organization != null)
			limsClientElement.addContent(organization.getXmlElement());
			
		return limsClientElement;
	}
}











