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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.io.Serializable;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MoTrPACAssayFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.ProjectStoreUtils;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class MoTrPACAssay implements Serializable, Comparable<MoTrPACAssay>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6679621158661692339L;
	private String assayId;
	private String code;
	private String description;
	private String polarity;
	private String bucketCode;	
	
	public MoTrPACAssay(
			String assayId, 
			String code, 
			String description, 
			String polarity, 
			String bucketCode) {
		super();
		this.assayId = assayId;
		this.code = code;
		this.description = description;
		this.polarity = polarity;
		this.bucketCode = bucketCode;
	}

	@Override
	public int compareTo(MoTrPACAssay o) {
		return description.compareTo(o.getDescription());
	}

	/**
	 * @return the sampleType
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MoTrPACAssay.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACAssay other = (MoTrPACAssay) obj;

        if ((this.assayId == null) ? (other.getAssayId() != null) : !this.assayId.equals(other.getAssayId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.assayId != null ? this.assayId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the assayId
	 */
	public String getAssayId() {
		return assayId;
	}

	/**
	 * @return the polarity
	 */
	public String getPolarity() {
		return polarity;
	}

	/**
	 * @return the bucketCode
	 */
	public String getBucketCode() {
		return bucketCode;
	}

	/**
	 * @param assayId the assayId to set
	 */
	public void setAssayId(String assayId) {
		this.assayId = assayId;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @param polarity the polarity to set
	 */
	public void setPolarity(String polarity) {
		this.polarity = polarity;
	}

	/**
	 * @param bucketCode the bucketCode to set
	 */
	public void setBucketCode(String bucketCode) {
		this.bucketCode = bucketCode;
	}

	public MoTrPACAssay(Element moTrPACAssayElement) {
		
		super();
		assayId = moTrPACAssayElement.getAttributeValue(CommonFields.Id.name());
		code = moTrPACAssayElement.getAttributeValue(CommonFields.Name.name());
		description = ProjectStoreUtils.getDescriptionFromElement(moTrPACAssayElement);
		polarity = moTrPACAssayElement.getAttributeValue(MoTrPACAssayFields.Polarity.name());
		bucketCode = moTrPACAssayElement.getAttributeValue(MoTrPACAssayFields.BucketCode.name());
	}
	
	@Override
	public Element getXmlElement() {

		Element moTrPACAssayElement = 
				new Element(ObjectNames.MoTrPACAssay.name());
		moTrPACAssayElement.setAttribute(CommonFields.Id.name(), assayId);
		moTrPACAssayElement.setAttribute(CommonFields.Name.name(), code);
		ProjectStoreUtils.addDescriptionElement(description, moTrPACAssayElement);
		moTrPACAssayElement.setAttribute(MoTrPACAssayFields.Polarity.name(), polarity);
		moTrPACAssayElement.setAttribute(MoTrPACAssayFields.BucketCode.name(), bucketCode);
		
		return moTrPACAssayElement;
	}	
}













