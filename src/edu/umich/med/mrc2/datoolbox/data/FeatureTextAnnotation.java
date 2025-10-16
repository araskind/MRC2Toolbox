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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;

public class FeatureTextAnnotation implements Comparable<FeatureTextAnnotation>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8777382205431963L;
	private Date dateCreated;
	private Date dateModified;
	private String text;
	private String uniqueId;

	public FeatureTextAnnotation(String text) {
		super();
		this.text = text;
		dateCreated = new Date();
		dateModified = new Date();
		uniqueId = DataPrefix.ANNOTATION.getName() + UUID.randomUUID().toString();
	}

	public FeatureTextAnnotation(ResultSet anrs) throws SQLException {

		this.text = anrs.getString(1);
		dateCreated = new Date(anrs.getDate(2).getTime());
		dateModified = new Date(anrs.getDate(3).getTime());
	}

	public FeatureTextAnnotation(String uid, String contents, long created, long modified) {

		uniqueId = uid;
		text = contents;
		dateCreated = new Date(created);
		dateModified = new Date(modified);
	}

	public FeatureTextAnnotation(String uid, String contents) {

		uniqueId = uid;
		text = contents;
		dateCreated = new Date();
		dateModified = new Date();
	}

	@Override
	public int compareTo(FeatureTextAnnotation o) {
		return uniqueId.compareTo(o.getUniqueId());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!FeatureTextAnnotation.class.isAssignableFrom(obj.getClass()))
            return false;

        final FeatureTextAnnotation other = (FeatureTextAnnotation) obj;

        if ((this.uniqueId == null) ? (other.getUniqueId() != null) : !this.uniqueId.equals(other.getUniqueId()))
            return false;

		if (obj == this)
			return true;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.uniqueId != null ? this.uniqueId.hashCode() : 0);
        return hash;
    }

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getLastModified() {
		return dateModified;
	}

	public String getText() {
		return text;
	}

	public void setLastModified(Date dateEdited) {
		this.dateModified = dateEdited;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
}
