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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

public enum CorrelationFunctionType {

	PEARSON("Pearson correlation"),
	SPEARMAN("Spearman's rank correlation"),
	KENDALL("Kendall's rank correlation"),
	;
	
	private final String uiName;

	CorrelationFunctionType(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static CorrelationFunctionType getOptionByName(String name) {

		for(CorrelationFunctionType field : CorrelationFunctionType.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
}
