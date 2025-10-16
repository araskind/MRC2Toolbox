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

package edu.umich.med.mrc2.datoolbox.msmsfdr;

public enum PercolatorOutputFields {

	PSMID("PSMId"),
	SCORE("score"),
	Q_VALUE("q-value"),
	POSTERIOR_ERROR_PROB("posterior_error_prob"),
	PEPTIDE("peptide"),
	PROTEINIDS("proteinIds"),
	;

	private final String name;

	PercolatorOutputFields(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public static PercolatorOutputFields getFieldByName(String colName) {
		
		for(PercolatorOutputFields f : PercolatorOutputFields.values()) {
			if(f.getName().equals(colName))
				return f;
		}
		return null;
	}
}
