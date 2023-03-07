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

public enum DecoyExportHandling {
	
	EXPORT_ALL("Export any type of library hit (regular or decoy)"),
	NORMAL_ONLY("Export only REGULAR library hit"),
	DECOY_ONLY("Export only DECOY library hit"),
	;

	private final String name;

	DecoyExportHandling(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public static DecoyExportHandling getDecoyExportHandlingByName(String name) {
		
		for(DecoyExportHandling s : DecoyExportHandling.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}
}
