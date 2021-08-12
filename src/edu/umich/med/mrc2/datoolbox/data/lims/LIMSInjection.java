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

public class LIMSInjection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3248707219323831156L;
	private String injectionId;
	private String dataFile;
	private Date timestamp;
	private String acquisitionMethodId;
	private Double volume;
	private String status;

	public LIMSInjection(
			String injectionId,
			String dataFile,
			Date timestamp,
			String acquisitionMethodId,
			Double volume,
			String status) {
		super();
		this.injectionId = injectionId;
		this.dataFile = dataFile;
		this.timestamp = timestamp;
		this.acquisitionMethodId = acquisitionMethodId;
		this.volume = volume;
		this.status = status;
	}

	/**
	 * @return the injectionId
	 */
	public String getInjectionId() {
		return injectionId;
	}

	/**
	 * @return the dataFile
	 */
	public String getDataFile() {
		return dataFile;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the acquisitionMethodId
	 */
	public String getAcquisitionMethodId() {
		return acquisitionMethodId;
	}

	/**
	 * @return the volume
	 */
	public Double getVolume() {
		return volume;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!LIMSInjection.class.isAssignableFrom(obj.getClass()))
            return false;

        final LIMSInjection other = (LIMSInjection) obj;

        if ((this.injectionId == null) ? (other.getInjectionId() != null) : !this.injectionId.equals(other.getInjectionId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.injectionId != null ? this.injectionId.hashCode() : 0);
        return hash;
    }
}
