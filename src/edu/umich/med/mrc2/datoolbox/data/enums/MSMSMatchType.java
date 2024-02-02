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

import java.awt.Color;

public enum MSMSMatchType {

		Regular("Regular (precursor) match", Color.GREEN),
		InSource("In-source match (ignore precursor)", Color.BLUE),
		Hybrid("Hybrid match", Color.ORANGE),
		;	
	
	private final String uiName;
	private final Color colorCode;

	MSMSMatchType(String uiName, Color colorCode) {
		this.uiName = uiName;
		this.colorCode = colorCode;
	}

	public String getName() {
		return uiName;
	}

	public Color getColorCode() {
		return colorCode;
	}
	
	public static MSMSMatchType getOptionByName(String name) {
		
		for(MSMSMatchType type : MSMSMatchType.values()) {
			
			if(type.name().equals(name))
				return type;
		}		
		return null;
	}
	
	public static MSMSMatchType getOptionByUIName(String name) {
		
		for(MSMSMatchType type : MSMSMatchType.values()) {
			
			if(type.getName().equals(name))
				return type;
		}		
		return null;
	}
}
