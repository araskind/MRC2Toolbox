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

public enum DataTypeForImport {

	AGILENT_UNTARGETED("Agilent untargeted", "Library & data files in CEF format"),
	AGILENT_PROFINDER_TARGETED("Agilent targeted (ProFinder)", "Library as simple CSV, data as ProFinder archive"),
	GENERIC_TARGETED("Generic targeted", "Library and data in a single text file"),
	;
	
	private final String uiName;
	private final String details;

	DataTypeForImport(String uiName, String details) {
		this.uiName = uiName;
		this.details= details;	
	}
	
	public String getName() {
		return uiName;
	}
	
	public String getDetails() {
		return details;
	}
	
	public String toString() {
		return "<HTML><B>" + uiName + "</B><BR>" + details;
	}
}
