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

public class HMDBPathway implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4701781888008822261L;
	
	private String name;
	private String smpdbId;
	private String keggMapId;
	private String globalId;

	public HMDBPathway(
			String name, 
			String smpdbId) {
		this(null, name, smpdbId, null);
	}
	
	public HMDBPathway(
			String name, 
			String smpdbId, 
			String keggMapId) {
		this(null, name, smpdbId, keggMapId);
	}

	public HMDBPathway(
			String globalId,
			String name, 
			String smpdbId, 
			String keggMapId) {
		super();		
		this.globalId = globalId;
		this.name = name;
		this.smpdbId = smpdbId;
		this.keggMapId = keggMapId;
		
		if(smpdbId != null && smpdbId.isEmpty())
			this.smpdbId = null;
		
		if(keggMapId != null && keggMapId.isEmpty())
			this.keggMapId = null;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the smpdbId
	 */
	public String getSmpdbId() {
		return smpdbId;
	}

	/**
	 * @return the keggMapId
	 */
	public String getKeggMapId() {
		return keggMapId;
	}
	
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
}
