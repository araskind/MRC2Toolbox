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

package edu.umich.med.mrc2.datoolbox.dbparse.load.lipidmaps;

public class LipidMapsClassificationObject {

	private LipidMapsClassification group;
	private String code;
	private String name;
	
	public LipidMapsClassificationObject(
			LipidMapsClassification group, 
			String code,
			String name) {
		super();
		this.group = group;
		this.code = code;
		this.name = name;
	}

	public LipidMapsClassification getGroup() {
		return group;
	}

	public String getCode() {
		return code;
	}	
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + " [" + code + "] (" + group.name() + ")";
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!LipidMapsClassificationObject.class.isAssignableFrom(obj.getClass()))
			return false;

		final LipidMapsClassificationObject other = (LipidMapsClassificationObject) obj;

		if (!this.group.equals(other.getGroup()))
			return false;
		
		if (!this.code.equals(other.getCode()))
			return false;
		
		if (!this.name.equals(other.getName()))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return 53 * 3 + (this.name != null ? this.name.hashCode() : 0)
				+ (this.code != null ? this.code.hashCode() : 0)
				+ (this.group != null ? this.group.hashCode() : 0);
	}

}
