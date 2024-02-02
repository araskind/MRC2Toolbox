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

public enum MSMSComponentTableFields {

	MRC2_LIB_ID("MRC2 MSMS library ID", MSPField.NOTES),
	POLARITY("Polarity", MSPField.ION_MODE),
	IONIZATION("Ionization (user supplied)", MSPField.IONIZATION),
	COLLISION_ENERGY("Collision energy", MSPField.COLLISION_ENERGY),
	PRECURSOR_MZ("Precursor M/Z", MSPField.PRECURSORMZ),
	ADDUCT("Adduct (parent peak)", MSPField.PRECURSOR_TYPE),
	COLLISION_GAS("Collision gas", MSPField.NOTES),
	INSTRUMENT("Instrument name", MSPField.INSTRUMENT),
	INSTRUMENT_TYPE("Instrument type", MSPField.INSTRUMENT_TYPE),
	IN_SOURCE_VOLTAGE("In-source voltage", MSPField.NOTES),
	MSN_PATHWAY("MSN fragmentation pathway", MSPField.NOTES),
	PRESSURE("Pressure", MSPField.NOTES),
	SAMPLE_INLET("Sample inlet", MSPField.SAMPLE_INLET),
	SPECIAL_FRAGMENTATION("Special fragmentation", MSPField.SPECIAL_FRAGMENTATION),
	SPECTRUM_TYPE("Spectrum type", MSPField.SPECTRUM_TYPE),
	CHROMATOGRAPHY_TYPE("Chromatography type", MSPField.NOTES),
	CONTRIBUTOR("Contributor", MSPField.NOTES),
	SPLASH("MONA splash", MSPField.NOTES),
	RESOLUTION("Resolution", MSPField.NOTES),
	SPECTRUM_SOURCE("Spectrum source type", MSPField.NOTES),
	IONIZATION_TYPE("Ionization type", MSPField.IONIZATION),
	LIBRARY_NAME("Source library name", MSPField.NOTES),
	ORIGINAL_LIBRARY_ID("Original (source) library entry ID", MSPField.NOTES),
	ENTROPY("Spectrum  entropy", MSPField.NOTES),
	;

	private final String uiName;
	private final MSPField nistField;

	MSMSComponentTableFields(String uiName, MSPField nistField) {
		this.uiName = uiName;
		this.nistField = nistField;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public MSPField getMSPField() {
		return nistField;
	}
	
	public static MSMSComponentTableFields getOptionByName(String name) {
		
		for(MSMSComponentTableFields type : MSMSComponentTableFields.values()) {
			
			if(type.name().equals(name))
				return type;
		}		
		return null;
	}
	
	public static MSMSComponentTableFields getOptionByUIName(String name) {
		
		for(MSMSComponentTableFields type : MSMSComponentTableFields.values()) {
			
			if(type.getName().equals(name))
				return type;
		}		
		return null;
	}
}
