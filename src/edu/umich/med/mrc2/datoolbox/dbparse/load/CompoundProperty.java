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

package edu.umich.med.mrc2.datoolbox.dbparse.load;

public class CompoundProperty {

	private String propertyName;
	private String propertyValue;
	private String source;
	private CompoundPropertyType type;
	
	public CompoundProperty(String propertyName, String propertyValue, CompoundPropertyType type) {
		super();
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.type = type;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public String getSource() {
		return source;
	}

	public CompoundPropertyType getType() {
		return type;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
