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

package edu.umich.med.mrc2.datoolbox.data.thermo.raw;

public enum ThermoCvParams {
	
	//	File properties
	PATH_NAME("NCIT:C47922", "NCIT", "Pathname"),
	VERSION("NCIT:C25714", "NCIT", "Version"),
	CREATION_DATE("NCIT:C69199", "NCIT", "Content Creation Date"),
	
	//	Instrument Properties
	INSTRUMENT_MODEL("MS:1000494", "MS", "Thermo Scientific instrument model"),
	INSTRUMENT_ATTRIBUTE("MS:1000496", "MS", "instrument attribute"),
	INSTRUMENT_SERIAL_NUMBER("MS:1000529", "MS", "instrument serial number"),
	SOFTWARE_VERSION("NCIT:C111093", "NCIT", "Software Version"),
	
	//	Sample properties
	SAMPLE_NAME("MS:1000002", "MS", "sample name"),
	SAMPLE_NUMBER("MS:1000001", "MS", "sample number"),
	VIAL("NCIT:C41275", "NCIT", "Vial"),
	INJECTION_VOLUME("AFR:0001577", "AFO", "injection volume setting"),
	ROW("NCIT:C43378", "NCIT", "Row"),
	;

	private final String accession;
	private final String cvLabel;
	private final String name;
		
	ThermoCvParams(String accession, String cvLabel, String name) {
		this.accession = accession;
		this.cvLabel = cvLabel;
		this.name = name;
	}

	public String getAccession() {
		return accession;
	}

	public String getCvLabel() {
		return cvLabel;
	}

	public String getName() {
		return name;
	}
	
	public static ThermoCvParams getThermoCvParamByAccession(String lookup) {
		
		for(ThermoCvParams par : ThermoCvParams.values()) {
			
			if(par.getAccession().equals(lookup))
				return par;
		}		
		return null;
	}	
}
