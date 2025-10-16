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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum PlotDataGrouping {

	IGNORE_DESIGN("None"), 
	ONE_FACTOR("Single factor"),
	TWO_FACTORS("Two factors"),
	EACH_FACTOR("Each factor");

	private final String uiName;

	PlotDataGrouping(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static PlotDataGrouping getOptionByName(String optionName) {

		for(PlotDataGrouping o : PlotDataGrouping.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static PlotDataGrouping getOptionByUIName(String sname) {
		
		for(PlotDataGrouping v : PlotDataGrouping.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
