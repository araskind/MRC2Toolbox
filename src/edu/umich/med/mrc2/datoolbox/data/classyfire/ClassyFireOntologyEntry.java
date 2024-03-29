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

package edu.umich.med.mrc2.datoolbox.data.classyfire;

public class ClassyFireOntologyEntry {

	private String id;
	private String name;
	private String description;
	private ClassyFireOntologyLevel level;
	
	public ClassyFireOntologyEntry() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ClassyFireOntologyEntry(
			String id, 
			String name, 
			String description, 
			String url,
			ClassyFireOntologyLevel level) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.level = level;
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

	@Override
	public String toString() {
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

	public ClassyFireOntologyLevel getLevel() {
		return level;
	}

	public void setLevel(ClassyFireOntologyLevel level) {
		this.level = level;
	}
}
