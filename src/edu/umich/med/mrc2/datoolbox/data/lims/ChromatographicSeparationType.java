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
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class ChromatographicSeparationType implements Serializable, 
	Comparable<ChromatographicSeparationType>, XmlStorable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5672011245435671668L;
	private String id;
	private String description;

	public ChromatographicSeparationType(String name, String description) {
		super();
		this.id = name;
		this.description = description;
	}

	@Override
	public int compareTo(ChromatographicSeparationType o) {
		// TODO Auto-generated method stub
		return this.id.compareTo(o.getId());
	}

	/**
	 * @return the name
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
	 * @param name the name to set
	 */
	public void setId(String name) {
		this.id = name;
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
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!ChromatographicSeparationType.class.isAssignableFrom(obj.getClass()))
            return false;

        final ChromatographicSeparationType other = (ChromatographicSeparationType) obj;

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
    
	public ChromatographicSeparationType(Element chromatographicSeparationTypeElement) {
		
		super();
		id = chromatographicSeparationTypeElement.getAttributeValue(
				CommonFields.Id.name());
		description = chromatographicSeparationTypeElement.getAttributeValue(
				CommonFields.Description.name());
	}

	@Override
	public Element getXmlElement() {
		
		Element chromatographicSeparationTypeElement = 
				new Element(ObjectNames.ChromatographicSeparationType.name());
		chromatographicSeparationTypeElement.setAttribute(
			CommonFields.Id.name(), id);
		chromatographicSeparationTypeElement.setAttribute(
			CommonFields.Description.name(), description);
			
		return chromatographicSeparationTypeElement;
	}
}








