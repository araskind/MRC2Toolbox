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
import java.util.Map;
import java.util.TreeMap;

public class ExperimentalSubject extends ExperimentDesignLevel implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7735498787036700215L;
	private Map<String,String>subjectProperties;
	private String subjectId;
	private String userFriendlySubjectId;
	private int taxonomyId;
	private String taxonomyName;
	private String subjectType;

	public ExperimentalSubject(String levelName) {

		super(levelName);
		subjectProperties = new TreeMap<String,String>();
	}

	public ExperimentalSubject(
			String subjectName,
			String subjectId,
			String userFriendlySubjectId,
			int taxonomyId,
			String taxonomyName) {

		super(subjectName);
		this.subjectId = subjectId;
		this.userFriendlySubjectId = userFriendlySubjectId;
		this.taxonomyId = taxonomyId;
		this.taxonomyName = taxonomyName;
	}
	

	public ExperimentalSubject(
			String subjectId, 
			String name, 
			int taxonomyId, 
			String subjectType) {
		super(name);
		this.subjectId = subjectId;
		this.taxonomyId = taxonomyId;
		this.subjectType = subjectType;
	}

	public void setProperty(String name, String value) {
		subjectProperties.put(name, value);
	}

	public String getProperty(String name) {
		return subjectProperties.get(name);
	}


	/**
	 * @return the subjectProperties
	 */
	public Map<String, String> getSubjectProperties() {
		return subjectProperties;
	}

	/**
	 * @return the subjectId
	 */
	public String getSubjectId() {
		return subjectId;
	}

	/**
	 * @return the userFriendlySubjectId
	 */
	public String getUserFriendlySubjectId() {
		return userFriendlySubjectId;
	}

	/**
	 * @return the taxonomyId
	 */
	public int getTaxonomyId() {
		return taxonomyId;
	}

	/**
	 * @return the taxonomyName
	 */
	public String getTaxonomyName() {
		return taxonomyName;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}
	

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ExperimentalSubject.class.isAssignableFrom(obj.getClass()))
            return false;

        final ExperimentalSubject other = (ExperimentalSubject) obj;

        //	If belong to different factors
        if ((this.subjectId == null) ? (other.getSubjectId() != null) : !this.subjectId.equals(other.getSubjectId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash
        		+ (this.subjectId != null ? this.subjectId.hashCode() : 0);
        return hash;
    }
}
