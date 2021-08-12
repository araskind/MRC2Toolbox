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

import edu.umich.med.mrc2.datoolbox.data.enums.UserAffiliation;

public class LIMSUser implements Serializable, Comparable<LIMSUser>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -690624090007695121L;
	private String id;
	private String lastName;
	private String firstName;
	private String laboratory;
	private String email;
	private String phone;
	private String username;
	private boolean superUser;
	private String affiliation;
	private String organizationId;
	private boolean active;

	public LIMSUser(
		String id, 
		String lastName, 
		String firstName,
		String laboratory, 
		String email, 
		String phone,
		String username, 
		boolean superUser, 
		String affiliation) {

		super();
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
		this.laboratory = laboratory;
		this.email = email;
		this.phone = phone;
		this.username = username;
		this.superUser = superUser;
		this.affiliation = affiliation;
		this.active = true;
	}
	
	public LIMSUser(
		String lastName, 
		String firstName,
		String username,
		UserAffiliation affiliation,
		IdTrackerOrganization organization, 
		String email, 
		String phone,		
		boolean superUser, 
		boolean isActive) {

		super();
		this.lastName = lastName;
		this.firstName = firstName;
		this.username = username;
		this.affiliation = affiliation.name();
		this.organizationId = organization.getId();
		this.laboratory = organization.getLaboratory();
		this.email = email;
		this.phone = phone;		
		this.superUser = superUser;	
		this.active = isActive;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSUser.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSUser other = (LIMSUser) obj;

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
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the laboratory
	 */
	public String getLaboratory() {
		return laboratory;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @return the username
	 */
	public String getUserName() {
		return username;
	}

	public String getFullName() {
		return lastName + ", " + firstName;
	}

	/**
	 * @return the superUser
	 */
	public boolean isSuperUser() {
		return superUser;
	}

	/**
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * @param superUser the superUser to set
	 */
	public void setSuperUser(boolean superUser) {
		this.superUser = superUser;
	}

	@Override
	public int compareTo(LIMSUser o) {
		return id.compareTo(o.getId());
	}

	public String getInfo() {

		return
			"<HTML><B>" + getFullName() + "</B><BR>" +
			"email: " + email  + "<BR>" +
			"phone: " + phone;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public String toString() {
		return getFullName();
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @param laboratory the laboratory to set
	 */
	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param affiliation the affiliation to set
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
}








