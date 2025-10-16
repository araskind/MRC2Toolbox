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

public class ExternalDatabase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5203471549051024871L;
	private String id;
	private String name;
	private String webLinkPrefix;
	private String webLinkSuffix;

	public ExternalDatabase(String id, String name, String webLinkPrefix, String webLinkSuffix) {
		super();
		this.id = id;
		this.name = name;
		this.webLinkPrefix = webLinkPrefix;
		this.webLinkSuffix = webLinkSuffix;
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
	 * @return the webLinkPrefix
	 */
	public String getWebLinkPrefix() {
		return webLinkPrefix;
	}

	/**
	 * @return the webLinkSuffix
	 */
	public String getWebLinkSuffix() {
		return webLinkSuffix;
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

	/**
	 * @param webLinkPrefix the webLinkPrefix to set
	 */
	public void setWebLinkPrefix(String webLinkPrefix) {
		this.webLinkPrefix = webLinkPrefix;
	}

	/**
	 * @param webLinkSuffix the webLinkSuffix to set
	 */
	public void setWebLinkSuffix(String webLinkSuffix) {
		this.webLinkSuffix = webLinkSuffix;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ExternalDatabase.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExternalDatabase other = (ExternalDatabase) obj;

        //	If belong to different factors
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
}
