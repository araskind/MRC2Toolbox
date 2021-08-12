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

package edu.umich.med.mrc2.datoolbox.database.load.t3db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.database.load.hmdb.HMDBCitation;

public class T3DBTarget {

	private String targetId;
	private String name;
	private String uniprotId;
	private String mechanismOfAction;
	private Collection<HMDBCitation>references;
	private String uniqueId;

	public T3DBTarget(String targetId, String name) {
		super();
		uniqueId = DataPrefix.DRUG_TARGET.getName() + UUID.randomUUID().toString();
		this.targetId = targetId;
		this.name = name;
		references = new ArrayList<HMDBCitation>();
	}

	/**
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the uniprotId
	 */
	public String getUniprotId() {
		return uniprotId;
	}

	/**
	 * @return the mechanismOfAction
	 */
	public String getMechanismOfAction() {
		return mechanismOfAction;
	}

	/**
	 * @return the references
	 */
	public Collection<HMDBCitation> getReferences() {
		return references;
	}

	/**
	 * @param uniprotId the uniprotId to set
	 */
	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	/**
	 * @param mechanismOfAction the mechanismOfAction to set
	 */
	public void setMechanismOfAction(String mechanismOfAction) {
		this.mechanismOfAction = mechanismOfAction;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}


}
