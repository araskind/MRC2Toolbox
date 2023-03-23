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

package edu.umich.med.mrc2.datoolbox.dbparse.load.drugbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.dbparse.load.hmdb.HMDBCitation;

public class DrugTarget implements Comparable<DrugTarget>{

	String id;
	String name;
	String organizm;
	Collection<HMDBCitation>references;
	Collection<String>actions;
	
	public DrugTarget(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		
		references = new ArrayList<HMDBCitation>();
		actions = new TreeSet<String>();
	}

	@Override
	public int compareTo(DrugTarget o) {
		return this.id.compareTo(o.getId());
	}

	public String getOrganizm() {
		return organizm;
	}

	public void setOrganizm(String organizm) {
		this.organizm = organizm;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public Collection<HMDBCitation> getReferences() {
		return references;
	}
	
    @Override
    public int hashCode() {
        return id.hashCode();
    }

	public Collection<String> getActions() {
		return actions;
	}
}
