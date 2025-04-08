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
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class DataExtractionMethod extends AnalysisMethod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6671825494245194136L;
	private String description;
	private LIMSUser createdBy;
	private Date createdOn;	

	public DataExtractionMethod(
			String id,
			String name,
			String methodDescription,
			LIMSUser createdBy,
			Date createdOn) {

		super(id, name);
		this.description = methodDescription;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
	}

	/**
	 * @return the methodDescription
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the createdBy
	 */
	public LIMSUser getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param methodDescription the methodDescription to set
	 */
	public void setDescription(String methodDescription) {
		this.description = methodDescription;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(LIMSUser createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!DataExtractionMethod.class.isAssignableFrom(obj.getClass()))
            return false;

        final DataExtractionMethod other = (DataExtractionMethod) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }
    
	public DataExtractionMethod(Element dataExtractionMethodElement) {

		super(dataExtractionMethodElement.getAttributeValue(CommonFields.Id.name()), 
				dataExtractionMethodElement.getAttributeValue(CommonFields.Name.name()));
		
		description = dataExtractionMethodElement.getAttributeValue(
				CommonFields.Description.name());
		
		String createdOnString = 
				dataExtractionMethodElement.getAttributeValue(CommonFields.DateCreated.name());
		if(createdOnString != null) {
			try {
				createdOn = ExperimentUtils.dateTimeFormat.parse(createdOnString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String userId = 
				dataExtractionMethodElement.getAttributeValue(CommonFields.UserId.name());
		if(userId != null && !userId.isBlank())
			createdBy = IDTDataCache.getUserById(userId);
	}
	
	@Override
	public Element getXmlElement() {
		
		Element dataExtractionMethodElement = super.getXmlElement();
		dataExtractionMethodElement.setName(ObjectNames.DataExtractionMethod.name());
		
		dataExtractionMethodElement.setAttribute(
				CommonFields.Description.name(), description);
		dataExtractionMethodElement.setAttribute(CommonFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(createdOn));
		
		if(createdBy != null)
			dataExtractionMethodElement.setAttribute(
					CommonFields.UserId.name(), createdBy.getId());
		
		return dataExtractionMethodElement;
	}
}









