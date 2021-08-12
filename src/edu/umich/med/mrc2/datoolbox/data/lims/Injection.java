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

public class Injection  implements Serializable, Comparable<Injection>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8052899216236967926L;
	private String id;
	private String dataFileName;
	private Date timeStamp;
	private String prepItemId;
	private String acquisitionMethodId;
	private double injectionVolume;
	
	public Injection(
			String id, 
			String dataFileName, 
			Date timeStamp, 
			String prepItemId, 
			String acquisitionMethodId,
			double injectionVolume) {
		super();
		this.id = id;
		this.dataFileName = dataFileName;
		this.timeStamp = timeStamp;
		this.prepItemId = prepItemId;
		this.acquisitionMethodId = acquisitionMethodId;
		this.injectionVolume = injectionVolume;
	}

	public String getId() {
		return id;
	}

	public String getDataFileName() {
		return dataFileName;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getPrepItemId() {
		return prepItemId;
	}

	public String getAcquisitionMethodId() {
		return acquisitionMethodId;
	}

	public double getInjectionVolume() {
		return injectionVolume;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!Injection.class.isAssignableFrom(obj.getClass()))
            return false;

        final Injection other = (Injection) obj;

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

	@Override
	public int compareTo(Injection o) {
		return this.acquisitionMethodId.compareTo(o.getId());
	}
}
