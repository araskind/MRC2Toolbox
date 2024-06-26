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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum DataScale {

	RAW("Raw", true), 
	LN("ln", true),
	LOG10("log10", true),
	SQRT("Square root", true),
	RANGE("Range (0-100)", false), 
	ZSCORE("Z-score", false),
	PARETO("Pareto", false);

	private final String uiName;
	private final boolean directCalculation;

	DataScale(String uiName, boolean directCalculation) {
		this.uiName = uiName;
		this.directCalculation = directCalculation;
	}

	public String getName() {
		return uiName;
	}

	public boolean isDirectCalculation() {
		return directCalculation;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static DataScale getOptionByUIName(String name) {
		
		for(DataScale s : DataScale.values()) {
			
			if(s.getName().equals(name))
				return s;
		}		
		return null;
	}
	
	public static DataScale getOptionByName(String optionName) {

		for(DataScale o : DataScale.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
}
