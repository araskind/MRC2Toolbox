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
import java.util.Date;

public class DataExtractionMethod extends AnalysisMethod implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6671825494245194136L;
	private String description;
	private LIMSUser createdBy;
	private Date createdOn;
	private DataProcessingSoftware software;

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

	public DataProcessingSoftware getSoftware() {
		return software;
	}

	public void setSoftware(DataProcessingSoftware software) {
		this.software = software;
	}

}
