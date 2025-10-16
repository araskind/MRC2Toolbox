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

package edu.umich.med.mrc2.datoolbox.rawdata;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class RawDataPipeline implements Comparable<RawDataPipeline>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9178161202289151711L;
	protected String id;
	protected String name;
	protected String description;
	
	protected Date dateCreated, lastModified;

	public RawDataPipeline(String name, String description) {
		super();
		this.name = name;
		this.description = description;
		this.id = "RDP_" + UUID.randomUUID().toString();
		dateCreated = new Date();
		lastModified = new Date();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getId() {
		return id;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!RawDataPipeline.class.isAssignableFrom(obj.getClass()))
            return false;

        final RawDataPipeline other = (RawDataPipeline) obj;
        
        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {
        return 53 * 3 + (this.id != null ? this.id.hashCode() : 0);
    }

	@Override
	public int compareTo(RawDataPipeline o) {
		return this.name.compareTo(o.getName());
	}
}
