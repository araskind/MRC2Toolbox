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
import java.util.ArrayList;
import java.util.Collection;

public class PubChemCompoundDescriptionBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4907802617026393162L;
	private String cid;
	private String title;	
	private Collection<PubChemCompoundDescription>descriptions;
	
	public PubChemCompoundDescriptionBundle(String cid, String title) {
		super();
		this.cid = cid;
		this.title = title;
		descriptions = new ArrayList<PubChemCompoundDescription>();
	}

	public String getCid() {
		return cid;
	}

	public String getTitle() {
		return title;
	}

	public Collection<PubChemCompoundDescription> getDescriptions() {
		return descriptions;
	}
	
	public void addDescription(PubChemCompoundDescription desc) {
		descriptions.add(desc);
	}
}
