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
import java.util.Date;

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

}
