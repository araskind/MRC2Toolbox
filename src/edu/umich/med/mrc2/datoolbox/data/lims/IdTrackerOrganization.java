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

public class IdTrackerOrganization extends LIMSOrganization implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7598522205706974202L;
	private String department;
	private String laboratory;
	private LIMSUser principalInvestigator;
	private LIMSUser contactPerson;
	private String mailingAddress;
	private String metlimsClientId;

	public IdTrackerOrganization(
			String id,
			String name,
			String address,
			String department,
			String laboratory,
			String mailingAddress) {

		super(id, name, address);

		this.department = department;
		this.laboratory = laboratory;
		this.mailingAddress = mailingAddress;
	}

	public IdTrackerOrganization(
			String name, 
			String mailingAddress, 
			String department, 
			String laboratory,
			LIMSUser principalInvestigator, 
			LIMSUser contactPerson) {
		super(null, name, mailingAddress);
		this.department = department;
		this.laboratory = laboratory;
		this.principalInvestigator = principalInvestigator;
		this.contactPerson = contactPerson;
		this.mailingAddress = mailingAddress;
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

	public String getOrganizationInfo() {

		return
			"<HTML><B>" + name + "</B><BR>" +
			department  + "<BR>" +
			laboratory + "<BR>" +
			address;
	}

	public String getMetlimsClientId() {
		return metlimsClientId;
	}

	public void setMetlimsClientId(String metlimsClientId) {
		this.metlimsClientId = metlimsClientId;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}

	public void setMailingAddress(String mailingAddress) {
		this.mailingAddress = mailingAddress;
	}
}












