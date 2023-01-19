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

package edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class HMDBDesease implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4497633670707729873L;
	
	private String uniqueId;
	private String name;
	private String omimId;
	private Collection<HMDBCitation>references;

	public HMDBDesease(String name, String omimId) {
		super();
		uniqueId = DataPrefix.HMDB_DESEASE.getName() + UUID.randomUUID().toString();
		this.name = name;
		this.omimId = omimId;
		references = new ArrayList<HMDBCitation>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the omimId
	 */
	public String getOmimId() {
		return omimId;
	}

	/**
	 * @return the references
	 */
	public Collection<HMDBCitation> getReferences() {
		return references;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
