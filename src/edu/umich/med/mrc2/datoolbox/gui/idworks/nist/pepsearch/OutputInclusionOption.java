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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

public enum OutputInclusionOption {

	OnlyFound("Do not output empty hit lists"),
	OnlyNotFound("Output only empty hit lists"),
	All("Output all hit lists");

	private final String description;

	OutputInclusionOption(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return description;
	}

	public static OutputInclusionOption getOptionByName(String name) {

		for(OutputInclusionOption o : OutputInclusionOption.values()) {
			if(o.name().equals(name))
				return o;
		}
		return null;
	}
}
