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

package edu.umich.med.mrc2.datoolbox.data.cpdcoll;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;

public class CompoundCollectionComponentTmp {

	private String id;
	private String collectionId;
	private String cas;
	private Map<CpdMetadataField,String>metadata;
	private CompoundIdentity cid;
	
	public CompoundCollectionComponentTmp(String id, String collectionId, String cas) {
		super();
		this.id = id;
		this.collectionId = collectionId;
		this.cas = cas;
		metadata = new TreeMap<CpdMetadataField,String>();
	}

	public CompoundCollectionComponentTmp(String collectionId, String cas) {
		this(null, collectionId, cas);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public String getCas() {
		return cas;
	}

	public Map<CpdMetadataField, String> getMetadata() {
		return metadata;
	}
	
	public void addMetadata(CpdMetadataField field, String value) {
		metadata.put(field, value);
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!CompoundCollectionComponentTmp.class.isAssignableFrom(obj.getClass()))
			return false;

		final CompoundCollectionComponentTmp other = (CompoundCollectionComponentTmp) obj;

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

	public CompoundIdentity getCid() {
		return cid;
	}

	public void setCid(CompoundIdentity cid) {
		this.cid = cid;
	}
}
