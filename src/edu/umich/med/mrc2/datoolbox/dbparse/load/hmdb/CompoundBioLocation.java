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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.io.Serializable;

public class CompoundBioLocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8484060781075196345L;
	
	private String locationName;
	private CompoundLocationType locationType;
	private String globalId;

	public CompoundBioLocation(
			String locationName, 
			CompoundLocationType locationType) {
		super();
		this.locationName = locationName;
		this.locationType = locationType;
	}
	
	

	public CompoundBioLocation(
			String locationName, 
			CompoundLocationType locationType, 
			String globalId) {
		super();
		this.locationName = locationName;
		this.locationType = locationType;
		this.globalId = globalId;
	}



	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * @return the locationType
	 */
	public CompoundLocationType getLocationType() {
		return locationType;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.locationName != null ? this.locationName.hashCode() : 0)
        		+ (this.locationType.name() != null ? this.locationType.name().hashCode() : 0);
        return hash;
    }

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
}
