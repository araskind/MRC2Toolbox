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

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;

public class BinnerAdductList {

	private String id;
	private String name;
	private String description;
	private LIMSUser owner;
	private Date dateCreated;
	private Date lastModified;
	private Map<BinnerAdduct,Integer>components;
	
	public BinnerAdductList(
			String id,
			String name, 
			String description, 
			LIMSUser owner, 
			Date dateCreated, 
			Date dateModified) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.owner = owner;
		this.dateCreated = dateCreated;
		this.lastModified = dateModified;
		
		components = new TreeMap<BinnerAdduct,Integer>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LIMSUser getOwner() {
		return owner;
	}

	public void setOwner(LIMSUser owner) {
		this.owner = owner;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date dateModified) {
		this.lastModified = dateModified;
	}

	public Map<BinnerAdduct,Integer> getComponents() {
		return components;
	}
	
	public void addComponent(BinnerAdduct newComponent, int tier) {
		components.put(newComponent, tier);
	}
	
	public void removeComponent(BinnerAdduct toRemove) {
		components.remove(toRemove);
	}
}
