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

package edu.umich.med.mrc2.datoolbox.dbparse.load.t3db;

public class T3DBProteinTarget {

	private String targetId;
	private String name;
	private String uniprotId;
	private String mechanismOfAction;

	public T3DBProteinTarget(String targetId, String name) {
		super();
		this.targetId = targetId;
		this.name = name;
	}

	public String getTargetId() {
		return targetId;
	}

	public String getName() {
		return name;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public String getMechanismOfAction() {
		return mechanismOfAction;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	public void setMechanismOfAction(String mechanismOfAction) {
		this.mechanismOfAction = mechanismOfAction;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.targetId != null ? this.targetId.hashCode() : 0);
        return hash;
    }
}
