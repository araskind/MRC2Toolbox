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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

public enum HiResSearchThreshold {

	l("Low"),
	e("Medium"),
	h("High"),
	h1("High 1"),
	h2("High 2"),
	h3("High 3"),
	h4("High 4"),
	h5("High 5"),
	h6("High 6"),
	h7("High 7"),
	h8("High 8"),
	h9("High 9"),
	;

	private final String description;

	HiResSearchThreshold(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return description;
	}

	public static HiResSearchThreshold getOptionByName(String name) {

		for(HiResSearchThreshold o : HiResSearchThreshold.values()) {
			if(o.name().equals(name))
				return o;
		}
		return null;
	}
}
