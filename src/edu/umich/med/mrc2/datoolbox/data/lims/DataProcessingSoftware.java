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

import edu.umich.med.mrc2.datoolbox.data.InstrumentPlatform;
import edu.umich.med.mrc2.datoolbox.data.enums.SoftwareType;

public class DataProcessingSoftware implements Serializable, Comparable<DataProcessingSoftware>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1559675727554482728L;
	
	private String id;
	private String name;
	private String description;
	private Manufacturer vendor;
	private SoftwareType softwareType;
	private InstrumentPlatform platform;
	
	public DataProcessingSoftware(
			String id, 
			SoftwareType softwareType,
			String name, 
			String description, 
			Manufacturer vendor) {
		super();
		this.id = id;
		this.softwareType = softwareType;
		this.name = name;
		this.description = description;
		this.vendor = vendor;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Manufacturer getVendor() {
		return vendor;
	}

	public void setVendor(Manufacturer vendor) {
		this.vendor = vendor;
	}

	@Override
	public int compareTo(DataProcessingSoftware o) {
		return name.compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

		if (obj == this)
			return true;
		
        if (!DataProcessingSoftware.class.isAssignableFrom(obj.getClass()))
            return false;

        final DataProcessingSoftware other = (DataProcessingSoftware) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	public SoftwareType getSoftwareType() {
		return softwareType;
	}

	public void setSoftwareType(SoftwareType softwareType) {
		this.softwareType = softwareType;
	}

	public InstrumentPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(InstrumentPlatform platform) {
		this.platform = platform;
	}

}
