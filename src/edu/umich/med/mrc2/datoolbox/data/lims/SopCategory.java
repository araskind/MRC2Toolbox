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

public class SopCategory implements Serializable, Comparable<SopCategory>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8900949187433949631L;
	private String categoryId;
	private String description;

	public SopCategory(
			String categoryId,
			String description) {
		super();
		this.categoryId = categoryId;
		this.description = description;
	}

	@Override
	public int compareTo(SopCategory o) {
		return categoryId.compareTo(o.getCategoryId());
	}

	@Override
	public String toString() {
		return description;
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!SopCategory.class.isAssignableFrom(obj.getClass()))
            return false;

        final SopCategory other = (SopCategory) obj;

        if ((this.categoryId == null) ? (other.getCategoryId() != null) : !this.categoryId.equals(other.getCategoryId()))
            return false;

		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.categoryId != null ? this.categoryId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the categoryId
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}









