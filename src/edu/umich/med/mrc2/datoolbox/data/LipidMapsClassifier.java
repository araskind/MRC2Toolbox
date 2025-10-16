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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;

public class LipidMapsClassifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1155567150281641656L;
	private String category;
	private String mainClass;
	private String subClass;
	private String classLevel4;
	private String abbreviation;
	private int count;
	
	public LipidMapsClassifier(
			String category, 
			String mainClass, 
			String subClass, 
			String classLevel4,
			String abbreviation, 
			int count) {
		super();
		this.category = category;
		this.mainClass = mainClass;
		this.subClass = subClass;
		this.classLevel4 = classLevel4;
		this.abbreviation = abbreviation;
		this.count = count;
	}

	public String getCategory() {
		return category;
	}

	public String getMainClass() {
		return mainClass;
	}

	public String getSubClass() {
		return subClass;
	}

	public String getClassLevel4() {
		return classLevel4;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public int getCount() {
		return count;
	}
}
