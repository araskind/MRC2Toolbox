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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.Date;

public class ReferenceMsMsLibrary implements Serializable, Comparable<ReferenceMsMsLibrary>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7269474190556511423L;
	private String uniqueId;
	private String name;
	private String description;
	private String searchOutputCode;
	private String primaryLibraryId;
	private Date dateCreated;
	private Date lastModified;
	private boolean isSubset;
	private boolean isDecoy;

	public ReferenceMsMsLibrary(
			String uniqueId,
			String name,
			String description,
			String searchOutputCode,
			String primaryLibraryId,
			Date dateCreated,
			Date lastModified,
			boolean isSubset,
			boolean isDecoy) {
		super();
		this.uniqueId = uniqueId;
		this.name = name;
		this.description = description;
		this.searchOutputCode = searchOutputCode;
		this.primaryLibraryId = primaryLibraryId;
		this.dateCreated = dateCreated;
		this.lastModified = lastModified;
		this.isSubset = isSubset;
		this.isDecoy = isDecoy;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getSearchOutputCode() {
		return searchOutputCode;
	}

	public String getPrimaryLibraryId() {
		return primaryLibraryId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public String toString() {
		return name;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ReferenceMsMsLibrary.class.isAssignableFrom(obj.getClass()))
            return false;

        final ReferenceMsMsLibrary other = (ReferenceMsMsLibrary) obj;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

	@Override
	public int compareTo(ReferenceMsMsLibrary o) {
		return name.compareTo(o.getName());
	}

	public boolean isSubset() {
		return isSubset;
	}

	public boolean isDecoy() {
		return isDecoy;
	}

	public void setDecoy(boolean isDecoy) {
		this.isDecoy = isDecoy;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSearchOutputCode(String searchOutputCode) {
		this.searchOutputCode = searchOutputCode;
	}

	public void setPrimaryLibraryId(String primaryLibraryId) {
		this.primaryLibraryId = primaryLibraryId;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setSubset(boolean isSubset) {
		this.isSubset = isSubset;
	}
	
	
}
