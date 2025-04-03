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

import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.LIMSChromatographicColumnFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class LIMSChromatographicColumn implements Serializable, Comparable<LIMSChromatographicColumn>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7256563164093073242L;
	private String columnId;
	private ChromatographicSeparationType separationType;
	private String columnName;
	private String chemistry;
	private Manufacturer manufacturer;
	private String catalogNumber;

	public LIMSChromatographicColumn(
			String columnId,
			String columnName,
			String chemistry,
			String catalogNumber) {
		super();
		this.columnId = columnId;
		this.columnName = columnName;
		this.chemistry = chemistry;
		this.catalogNumber = catalogNumber;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!LIMSChromatographicColumn.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSChromatographicColumn other = (LIMSChromatographicColumn) obj;

        if ((this.columnId == null) ? (other.getColumnId() != null) : !this.columnId.equals(other.getColumnId()))
            return false;

		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.columnId != null ? this.columnId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the columnId
	 */
	public String getColumnId() {
		return columnId;
	}

	/**
	 * @return the separationType
	 */
	public ChromatographicSeparationType getSeparationType() {
		return separationType;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the chemistry
	 */
	public String getChemistry() {
		return chemistry;
	}

	/**
	 * @return the manufacturer
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	/**
	 * @return the catalogNumber
	 */
	public String getCatalogNumber() {
		return catalogNumber;
	}

	/**
	 * @param columnId the columnId to set
	 */
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	/**
	 * @param separationType the separationType to set
	 */
	public void setSeparationType(ChromatographicSeparationType separationType) {
		this.separationType = separationType;
	}

	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @param chemistry the chemistry to set
	 */
	public void setChemistry(String chemistry) {
		this.chemistry = chemistry;
	}

	/**
	 * @param manufacturer the manufacturer to set
	 */
	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	/**
	 * @param catalogNumber the catalogNumber to set
	 */
	public void setCatalogNumber(String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	@Override
	public int compareTo(LIMSChromatographicColumn o) {
		return columnId.compareTo(o.getColumnId());
	}

	@Override
	public String toString() {
		return columnName;
	}
	
	public LIMSChromatographicColumn(Element chromatographicColumnElement) {
		
		super();
		columnId = chromatographicColumnElement.getAttributeValue(CommonFields.Id.name());
		columnName = chromatographicColumnElement.getAttributeValue(CommonFields.Name.name());
		catalogNumber = chromatographicColumnElement.getAttributeValue(
				LIMSChromatographicColumnFields.catalogNumber.name());
		chemistry = chromatographicColumnElement.getAttributeValue(
				LIMSChromatographicColumnFields.chemistry.name());
		
		Element vendorElement = 
				chromatographicColumnElement.getChild(ObjectNames.Manufacturer.name());
		if(vendorElement != null)
			manufacturer = new Manufacturer(vendorElement);
		
		Element separationTypeElement = 
				chromatographicColumnElement.getChild(ObjectNames.ChromatographicSeparationType.name());
		if(separationTypeElement != null)
			separationType = new ChromatographicSeparationType(separationTypeElement);
	}

	@Override
	public Element getXmlElement() {
		
		Element chromatographicColumnElement = 
				new Element(ObjectNames.InstrumentPlatform.name());
		chromatographicColumnElement.setAttribute(CommonFields.Id.name(), columnId);
		chromatographicColumnElement.setAttribute(CommonFields.Name.name(), columnName);	
		if(chemistry != null)
			chromatographicColumnElement.setAttribute(
				LIMSChromatographicColumnFields.chemistry.name(), chemistry);
		
		if(catalogNumber != null)
			chromatographicColumnElement.setAttribute(
				LIMSChromatographicColumnFields.catalogNumber.name(), catalogNumber);
		
		if(manufacturer != null)
			chromatographicColumnElement.addContent(manufacturer.getXmlElement());
		
		if(separationType != null)
			chromatographicColumnElement.addContent(separationType.getXmlElement());
		
		return chromatographicColumnElement;
	}
}
