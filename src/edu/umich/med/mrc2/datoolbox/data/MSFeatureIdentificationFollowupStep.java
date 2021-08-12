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

public class MSFeatureIdentificationFollowupStep implements Serializable, Comparable<MSFeatureIdentificationFollowupStep>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8011874515345262637L;
	private String id;
	private String name;

	public MSFeatureIdentificationFollowupStep(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public MSFeatureIdentificationFollowupStep(String name) {
		super();
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(MSFeatureIdentificationFollowupStep o) {
		return this.id.compareTo(o.getId());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!MSFeatureIdentificationFollowupStep.class.isAssignableFrom(obj.getClass()))
            return false;

        final MSFeatureIdentificationFollowupStep other = (MSFeatureIdentificationFollowupStep) obj;

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
	public String toString() {
		return name;
	}
}
