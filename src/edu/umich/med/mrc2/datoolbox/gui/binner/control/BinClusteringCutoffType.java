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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

public enum BinClusteringCutoffType {

	BELOW_SCORE("Below cutoff score"),
	ABOVE_SCORE("Above cutoff score"),
	ALL("All bins"),
	;
	
	private final String uiName;

	BinClusteringCutoffType(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static BinClusteringCutoffType getOptionByName(String name) {

		for(BinClusteringCutoffType field : BinClusteringCutoffType.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
}
