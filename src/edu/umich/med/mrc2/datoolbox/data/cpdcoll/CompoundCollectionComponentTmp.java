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

public class CompoundCollectionComponentTmp {

	private String id;
	private String collectionId;
	private String cas;
	private Map<CpdMetadataField,String>metadata;
	
	public CompoundCollectionComponentTmp(String collectionId, String cas) {
		super();
		this.collectionId = collectionId;
		this.cas = cas;
		metadata = new TreeMap<CpdMetadataField,String>();
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
}
