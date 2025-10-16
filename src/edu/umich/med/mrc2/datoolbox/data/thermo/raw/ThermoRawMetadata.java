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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class ThermoRawMetadata {
	
	private static final DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


	private String fileName;
	private Collection<ThermoRawMetadataEntry>entries;
	
	public ThermoRawMetadata(String fileName) {
		super();
		this.fileName = fileName;
		entries = new ArrayList<ThermoRawMetadataEntry>();
	}

	public String getFileName() {
		return fileName;
	}

	public Collection<ThermoRawMetadataEntry> getEntries() {
		return entries;
	}
	
	public String getSampleName() {
		
		ThermoRawMetadataEntry entry = 
				entries.stream().
				filter(e -> e.getCvParam().equals(ThermoCvParams.SAMPLE_NAME)).
				findFirst().orElse(null);
		if(entry != null)
			return entry.getValue();
					
		return null;
	}
	
	public String getSamplePosition() {
		
		ThermoRawMetadataEntry entry = 
				entries.stream().
				filter(e -> e.getCvParam().equals(ThermoCvParams.VIAL)).
				findFirst().orElse(null);
		if(entry != null)
			return entry.getValue();
					
		return null;
	}
	
	public Date getInjectionTime() {
		
		Date timestamp = null;
		ThermoRawMetadataEntry entry = 
				entries.stream().
				filter(e -> e.getCvParam().equals(ThermoCvParams.CREATION_DATE)).
				findFirst().orElse(null);
		if(entry != null) {
			try {
				timestamp = dateTimeFormat.parse(entry.getValue());
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return timestamp;
	}
}
