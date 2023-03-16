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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;
import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBRecord;

public class T3DBRecord extends HMDBRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9112847827667256343L;
	
	private Collection<String>categories;
	private Collection<String>types;
	private Collection<T3DBTarget>targets;	
	private Collection<T3DBProteinTarget>proteinTargets;
	private Map<T3DBProteinTarget,Collection<HMDBCitation>>tagetReferences;
	private Map<T3DBToxProperties,String>toxicityProperties;
	
	public T3DBRecord(String primaryId) {
		super(primaryId);

		types = new TreeSet<String>();
		categories = new TreeSet<String>();
		targets = new ArrayList<T3DBTarget>();
		toxicityProperties = 
				new TreeMap<T3DBToxProperties,String>();
		proteinTargets = new ArrayList<T3DBProteinTarget>();
		tagetReferences = new HashMap<T3DBProteinTarget,Collection<HMDBCitation>>();
	}

	public Collection<String> getCategories() {
		return categories;
	}

	public Collection<String> getTypes() {
		return types;
	}

	public Collection<T3DBTarget> getTargets() {
		return targets;
	}

	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}

	public void setTypes(Collection<String> types) {
		this.types = types;
	}

	public void setTargets(Collection<T3DBTarget> targets) {
		this.targets = targets;
	}

	public Map<T3DBToxProperties, String> getToxicityProperties() {
		return toxicityProperties;
	}
	
	public void addToxicityProperty(T3DBToxProperties property, String value) {
		
		if(value != null)
			toxicityProperties.put(property, value);
	}
	
	public String getToxicityProperty(T3DBToxProperties property) {
		return toxicityProperties.get(property);
	}

	public Collection<T3DBProteinTarget> getProteinTargets() {
		return proteinTargets;
	}

	public Map<T3DBProteinTarget, Collection<HMDBCitation>> getTagetReferences() {
		return tagetReferences;
	}
	
	public void addProteinTarget(T3DBProteinTarget protTarget) {
		proteinTargets.add(protTarget);
	}
	
	public void addReferenceForProteinTarget(T3DBProteinTarget protTarget, HMDBCitation ref) {
		
		if(!tagetReferences.containsKey(protTarget))
			tagetReferences.put(protTarget, new ArrayList<HMDBCitation>());
		
		tagetReferences.get(protTarget).add(ref);
	}
}












