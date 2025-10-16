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

public class CompoundIdentityInfoBundle implements Serializable, Comparable<CompoundIdentityInfoBundle> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8597119254058275525L;
	private CompoundIdentity compoundIdentity;
	private CompoundNameSet nameSet;
	private String compoundNarrative;
	
	public CompoundIdentityInfoBundle(CompoundIdentity compoundIdentity) {
		super();
		this.compoundIdentity = compoundIdentity;
	}

	@Override
	public int compareTo(CompoundIdentityInfoBundle o) {
		return this.compoundIdentity.compareTo(o.getCompoundIdentity());
	}

	public CompoundIdentity getCompoundIdentity() {
		return compoundIdentity;
	}

	public CompoundNameSet getNameSet() {
		return nameSet;
	}

	public void setNameSet(CompoundNameSet nameSet) {
		this.nameSet = nameSet;
	}
	
	public String getName() {
		return compoundIdentity.getName();
	}

	@Override
	public String toString() {
		return compoundIdentity.getName();
	}
	
    @Override
    public int hashCode() {
    	return compoundIdentity.hashCode();
    }

	public String getCompoundNarrative() {
		return compoundNarrative;
	}

	public void setCompoundNarrative(String compoundNarrative) {
		this.compoundNarrative = compoundNarrative;
	}
}
