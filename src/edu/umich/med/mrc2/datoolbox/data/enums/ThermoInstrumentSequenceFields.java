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

public enum ThermoInstrumentSequenceFields {
	
	SAMPLE_TYPE("Sample Type"),
	FILE_NAME("File Name"),
	SAMPLE_ID("Sample ID"),
	PATH("Path"),
	INSTRUMENT_METHOD("Instrument Method"),
	PROCESS_METHOD("Process Method"),
	CALIBRATION_FILE("Calibration File"),
	POSITION("Position"),
	INJ_VOL("Inj Vol"),
	LEVEL("Level"),
	SAMPLE_WT("Sample Wt"),
	SAMPLE_VOL("Sample Vol"),
	ISTD_AMT("ISTD Amt"),
	DIL_FACTOR("Dil Factor"),
	;

	private final String name;

	ThermoInstrumentSequenceFields(String name2) {
		this.name = name2;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	ThermoInstrumentSequenceFields getThermoInstrumentSequenceFieldByFieldName(String fieldName) {
		
		for(ThermoInstrumentSequenceFields f : ThermoInstrumentSequenceFields.values()) {
			if(f.getName().equals(fieldName))
				return f;
		}
		return null;
	}
}
