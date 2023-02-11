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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.IDTExperimentalSampleFields;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class IDTExperimentalSample extends ExperimentalSample implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3203240397816147097L;
	private StockSample parentStockSample;
	private Date dateCreated;
	private String description;

	public IDTExperimentalSample(
			String sampleId2,
			String sampleName2,
			String description,
			Date dateCreated,
			StockSample parentStockSample) {

		super(sampleId2, sampleName2);
		this.description = description;
		this.dateCreated = dateCreated;
		this.parentStockSample = parentStockSample;
	}

	/**
	 * @return the parentStockSample
	 */
	public StockSample getParentStockSample() {
		return parentStockSample;
	}

	/**
	 * @param parentStockSample the parentStockSample to set
	 */
	public void setParentStockSample(StockSample parentStockSample) {
		this.parentStockSample = parentStockSample;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public Element getXmlElement() {
		
		Element sampleElement = super.getXmlElement();		
		if(parentStockSample != null)
			sampleElement.setAttribute(
					IDTExperimentalSampleFields.StockSampleId.name(), 
					parentStockSample.getSampleId());
		
		Element descriptionElement = 
				new Element(IDTExperimentalSampleFields.Description.name());
		if(description != null)
			descriptionElement.setText(description);
		
		sampleElement.addContent(descriptionElement);
		
		if(dateCreated == null)
			dateCreated = new Date();
		
		sampleElement.setAttribute(IDTExperimentalSampleFields.DateCreated.name(), 
				ExperimentUtils.dateTimeFormat.format(dateCreated));
		
		return sampleElement;
	}

	public IDTExperimentalSample(			
			Element sampleElement, 
			ExperimentDesign design,
			RawDataAnalysisProject parentProject) {
		super(sampleElement, design, parentProject);
		
		String stockSampleId = sampleElement.getAttributeValue(
				IDTExperimentalSampleFields.StockSampleId.name());
		if(stockSampleId != null)
			parentStockSample = IDTDataCash.getStockSampleById(stockSampleId);

		Element descriptionElement =
				sampleElement.getChild(IDTExperimentalSampleFields.Description.name());
		if(descriptionElement != null && !descriptionElement.getText().isEmpty())
			description = descriptionElement.getText();
		
		dateCreated = new Date();
		String startDateString = 
				sampleElement.getAttributeValue(IDTExperimentalSampleFields.DateCreated.name());
		if(startDateString != null) {
			try {
				dateCreated = ExperimentUtils.dateTimeFormat.parse(startDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	public IDTExperimentalSample(IDTExperimentalSample sample) {
		super(sample);
		this.parentStockSample = sample.getParentStockSample();
		this.dateCreated = sample.getDateCreated();
		this.description = sample.getDescription();
	}		
}













