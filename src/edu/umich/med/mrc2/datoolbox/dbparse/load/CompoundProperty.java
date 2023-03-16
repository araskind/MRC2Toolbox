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

import java.io.Serializable;

public class CompoundProperty implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 410037410224833018L;
	
	private String propertyName;
	private String propertyValue;
	private String source;
	private CompoundPropertyType type;
	private String globalId;
	
	public CompoundProperty(
			String propertyName, 
			String propertyValue, 
			String source, 
			CompoundPropertyType type,
			String globalId) {
		super();
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.source = source;
		this.type = type;
		this.globalId = globalId;
	}

	public CompoundProperty(
			String propertyName, 
			String propertyValue,
			String source,
			CompoundPropertyType type) {
		super();
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.type = type;
		this.source = source;
	}

	public CompoundProperty(
			String propertyName, 
			CompoundPropertyType type, 
			String globalId) {
		super();
		this.propertyName = propertyName;
		this.type = type;
		this.globalId = globalId;
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

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0)
        		+ (this.type.name() != null ? this.type.name().hashCode() : 0);
        return hash;
    }
    
    @Override
    public CompoundProperty clone() {
    	
    	return new CompoundProperty(			
    			propertyName, 
    			propertyValue, 
    			source, 
    			type,
    			globalId);
    }
}
