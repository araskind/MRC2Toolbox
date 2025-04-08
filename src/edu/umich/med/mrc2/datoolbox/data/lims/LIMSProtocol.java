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
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSProtocolFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class LIMSProtocol implements Serializable, Comparable<LIMSProtocol>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8021665754926145812L;
	private String sopId;
	private String sopGroup;
	private String sopName;
	private String sopDescription;
	private String sopVersion;
	private Date dateCrerated;
	private LIMSUser createdBy;
	private SopCategory sopCategory;
	private String sopDetailLevel;

	public LIMSProtocol(
			String sopId,
			String sopGroup,
			String sopName,
			String sopDescription,
			String sopVersion,
			Date dateCrerated,
			LIMSUser createdBy) {
		super();
		this.sopId = sopId;
		this.sopGroup = sopGroup;
		this.sopName = sopName;
		this.sopDescription = sopDescription;
		this.sopVersion = sopVersion;
		this.dateCrerated = dateCrerated;
		this.createdBy = createdBy;
	}

	/**
	 * @return the sopId
	 */
	public String getSopId() {
		return sopId;
	}

	/**
	 * @return the sopName
	 */
	public String getSopName() {
		return sopName;
	}

	/**
	 * @return the sopDescription
	 */
	public String getSopDescription() {
		return sopDescription;
	}

	/**
	 * @return the sopVersion
	 */
	public String getSopVersion() {
		return sopVersion;
	}

	/**
	 * @return the dateCrerated
	 */
	public Date getDateCrerated() {
		return dateCrerated;
	}

	/**
	 * @return the createdBy
	 */
	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the sopCategory
	 */
	public SopCategory getSopCategory() {
		return sopCategory;
	}

	/**
	 * @return the sopDetailLevel
	 */
	public String getSopDetailLevel() {
		return sopDetailLevel;
	}

	/**
	 * @param sopId the sopId to set
	 */
	public void setSopId(String sopId) {
		this.sopId = sopId;
	}

	/**
	 * @param sopName the sopName to set
	 */
	public void setSopName(String sopName) {
		this.sopName = sopName;
	}

	/**
	 * @param sopDescription the sopDescription to set
	 */
	public void setSopDescription(String sopDescription) {
		this.sopDescription = sopDescription;
	}

	/**
	 * @param sopVersion the sopVersion to set
	 */
	public void setSopVersion(String sopVersion) {
		this.sopVersion = sopVersion;
	}

	/**
	 * @param dateCrerated the dateCrerated to set
	 */
	public void setDateCrerated(Date dateCrerated) {
		this.dateCrerated = dateCrerated;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @param sopCategory the sopCategory to set
	 */
	public void setSopCategory(SopCategory sopCategory) {
		this.sopCategory = sopCategory;
	}

	/**
	 * @param sopDetailLevel the sopDetailLevel to set
	 */
	public void setSopDetailLevel(String sopDetailLevel) {
		this.sopDetailLevel = sopDetailLevel;
	}

	@Override
	public int compareTo(LIMSProtocol o) {
		return sopId.compareTo(o.getSopId());
	}

	@Override
	public String toString() {
		return sopName;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSProtocol.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSProtocol other = (LIMSProtocol) obj;

        if ((this.sopId == null) ? (other.getSopId() != null) : !this.sopId.equals(other.getSopId()))
            return false;

		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.sopId != null ? this.sopId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the sopGroup
	 */
	public String getSopGroup() {
		return sopGroup;
	}

	/**
	 * @param sopGroup the sopGroup to set
	 */
	public void setSopGroup(String sopGroup) {
		this.sopGroup = sopGroup;
	}
	
	public LIMSProtocol(Element limsProtocolElement) {
		
		super();
		this.sopId = limsProtocolElement.getAttributeValue(CommonFields.Id.name());
		this.sopGroup = limsProtocolElement.getAttributeValue(LIMSProtocolFields.SOPGroup.name());
		this.sopName = limsProtocolElement.getAttributeValue(CommonFields.Name.name());
		this.sopDescription = limsProtocolElement.getAttributeValue(CommonFields.Description.name());
		this.sopVersion = limsProtocolElement.getAttributeValue(LIMSProtocolFields.SOPVersion.name());
		sopDetailLevel = limsProtocolElement.getAttributeValue(LIMSProtocolFields.SOPDetailLevel.name());
		
		String dateCreatedString = 
				limsProtocolElement.getAttributeValue(CommonFields.DateCreated.name());
		if(dateCreatedString != null) {
			try {
				dateCrerated = ExperimentUtils.dateTimeFormat.parse(dateCreatedString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		String userId = limsProtocolElement.getAttributeValue(CommonFields.UserId.name());
		if(userId != null && !userId.isBlank())
			createdBy = IDTDataCache.getUserById(userId);
		
		Element sopCategoryElement = limsProtocolElement.getChild(ObjectNames.SopCategory.name());
		if(sopCategoryElement != null)
			sopCategory = new SopCategory(sopCategoryElement);
	}

	@Override
	public Element getXmlElement() {
		
		Element limsProtocolElement = new Element(ObjectNames.LIMSProtocol.name());
		limsProtocolElement.setAttribute(CommonFields.Id.name(), sopId);
		limsProtocolElement.setAttribute(CommonFields.Name.name(), sopName);
		limsProtocolElement.setAttribute(CommonFields.Description.name(), sopDescription);
		
		if(createdBy != null)
			limsProtocolElement.setAttribute(CommonFields.UserId.name(), createdBy.getId());
		
		if(dateCrerated != null)
			limsProtocolElement.setAttribute(CommonFields.DateCreated.name(), 
					ExperimentUtils.dateTimeFormat.format(dateCrerated));
		
		if(sopGroup != null)
			limsProtocolElement.setAttribute(LIMSProtocolFields.SOPGroup.name(), sopGroup);
		
		if(sopVersion != null)
			limsProtocolElement.setAttribute(LIMSProtocolFields.SOPVersion.name(), sopVersion);
		
		if(sopDetailLevel != null)
			limsProtocolElement.setAttribute(LIMSProtocolFields.SOPDetailLevel.name(), sopDetailLevel);
		
		if(sopCategory != null)
			limsProtocolElement.addContent(sopCategory.getXmlElement());
		
		return limsProtocolElement;
	}
}









