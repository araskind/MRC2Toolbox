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

public enum QcEventType {

	SAMPLE_CONDITION("Sample condition"),
	SAMPLE_PREP("Sample preparation"),
	INJECTION("Sample injection/run"),
	INSTRUMENT_CONDITION("Instrument condition"),
	ASSAY_DEVELOPMENT("Assay development"),
	;

	private final String name;

	QcEventType(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static QcEventType getEventTypeByName(String name) {

		for(QcEventType source : QcEventType.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
}