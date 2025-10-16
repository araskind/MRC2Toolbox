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

package edu.umich.med.mrc2.datoolbox.dbparse.load.coconut;

import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.dbparse.load.coconut.CoconutParser.CoconutRecordFields;

public class CoconutRecord implements Comparable<CoconutRecord>{
	
	private String id;
	private String name;
	private CompoundIdentity compoundId;
	private Map<CoconutRecordFields,Object>properties;
	
	public CoconutRecord(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		compoundId = new CompoundIdentity();
		properties = new TreeMap<CoconutRecordFields,Object>();
	}
	
    @Override
    public int hashCode() {
        return id.hashCode();
	}

	@Override
	public int compareTo(CoconutRecord o) {
		return this.id.compareTo(o.getId());
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public CompoundIdentity getCompoundIdentity() {
		return compoundId;
	}
	
	public void addProperty(CoconutRecordFields field, Object value) {
		properties.put(field, value);
	}
	
	public Object getProperty(CoconutRecordFields field) {
		return properties.get(field);
	}
}











