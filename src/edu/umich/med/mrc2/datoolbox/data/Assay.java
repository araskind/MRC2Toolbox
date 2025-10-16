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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisMethod;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;

public class Assay extends AnalysisMethod implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3404652864873807417L;
	
	private String alternativeName;
	private InstrumentPlatform instrumentPlatform;
	
	public Assay(String id, String name) {
		super(id, name);
	}
	
	public Assay(String id, String name, String alternativeName) {
		super(id, name);
		this.alternativeName = alternativeName;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!Assay.class.isAssignableFrom(obj.getClass()))
            return false;

        final Assay other = (Assay) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

	public String getAlternativeName() {
		return alternativeName;
	}

	public void setAlternativeName(String alternativeName) {
		this.alternativeName = alternativeName;
	}

	public InstrumentPlatform getInstrumentPlatform() {
		return instrumentPlatform;
	}

	public void setInstrumentPlatform(InstrumentPlatform instrumentPlatform) {
		this.instrumentPlatform = instrumentPlatform;
	}
	
	public Assay(Element assayElement) {
		
		super(assayElement);
		Element ipElement = assayElement.getChild(ObjectNames.InstrumentPlatform.name());
		if(ipElement != null)
			instrumentPlatform = new InstrumentPlatform(ipElement);

		alternativeName = assayElement.getAttributeValue(CommonFields.Description.name());
	}

	@Override
	public Element getXmlElement() {

		Element assayElement = super.getXmlElement();
		assayElement.setName(ObjectNames.Assay.name());
		if(instrumentPlatform != null)
			assayElement.addContent(instrumentPlatform.getXmlElement());

		if(alternativeName != null)
			assayElement.setAttribute(CommonFields.Description.name(), alternativeName);
			
		return assayElement;
	}
}


