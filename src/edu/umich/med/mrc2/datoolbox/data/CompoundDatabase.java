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

public class CompoundDatabase implements Serializable, Comparable<CompoundDatabase>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2152489765423850439L;
	private String code;
	private String name;
	private String linkPrefix;
	private String linkSuffix;
	private String homePage;
	private boolean inSilico;
	
	public CompoundDatabase(
			String code, 
			String name, 
			String linkPrefix, 
			String linkSuffix, 
			String homePage,
			boolean inSilico) {
		super();
		this.code = code;
		this.name = name;
		this.linkPrefix = linkPrefix;
		this.linkSuffix = linkSuffix;
		this.homePage = homePage;
		this.inSilico = inSilico;
	}

	@Override
	public int compareTo(CompoundDatabase o) {
		return name.compareTo(o.getName());
	}

	public String toString() {
		return name;
	}
	
   @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!CompoundDatabase.class.isAssignableFrom(obj.getClass()))
            return false;

        final CompoundDatabase other = (CompoundDatabase) obj;

        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() { 
        return 53 * 3 + (this.name != null ? this.name.hashCode() : 0);
    }
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLinkPrefix() {
		return linkPrefix;
	}

	public void setLinkPrefix(String linkPrefix) {
		this.linkPrefix = linkPrefix;
	}

	public String getLinkSuffix() {
		return linkSuffix;
	}

	public void setLinkSuffix(String linkSuffix) {
		this.linkSuffix = linkSuffix;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public boolean isInSilico() {
		return inSilico;
	}

	public void setInSilico(boolean inSilico) {
		this.inSilico = inSilico;
	}
	
	
	
}
