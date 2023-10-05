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

public enum AnnotatedObjectType {

	CIDU("Compound ID unit", DataPrefix.COMPOUND_ID_UNIT),
	MS_FEATURE_POOLED("MS1 feature pooled", DataPrefix.MS_FEATURE_POOLED),
	MSMS_FEATURE("MSn feature", DataPrefix.MSMS_SPECTRUM),
	MSMS_FEATURE_COLLECTION("MSn feature collection", DataPrefix.MSMS_FEATURE_COLLECTION),
	MS_LIB_FEATURE("Library MS1 feature", DataPrefix.MS_LIBRARY_TARGET),
	MSMS_LIB_FEATURE("Library MSn feature", DataPrefix.MSMS_LIBRARY_ENTRY),
	SAMPLE("Experimental sample", DataPrefix.ID_SAMPLE),
	PREPPED_SAMPLE("Prepared sample", DataPrefix.PREPARED_SAMPLE),
	SAMPLE_PREP("Sample preparation", DataPrefix.SAMPLE_PREPARATION),
	INJECTION("Sample injection/run", DataPrefix.INJECTION),
	EXPERIMENT("Experiment", DataPrefix.ID_EXPERIMENT),
	PROJECT("Project", DataPrefix.ID_PROJECT),
	DATA_ANALYSIS("Data analysis", DataPrefix.DATA_ANALYSIS),
	INSTRUMENT_MAINTENANCE("Instrument maintenance", DataPrefix.INSTRUMENT_MAINT_LOG),
	MOTRPAC_RESULTS("MoTrPAC results", DataPrefix.MOTRPAC_REPORT),
	;

	private final String uiName;
	private final DataPrefix idPrefix;

	AnnotatedObjectType(String uiName, DataPrefix idPrefix) {
		this.uiName = uiName;
		this.idPrefix = idPrefix;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}

	public DataPrefix getIdPrefix() {
		return idPrefix;
	}

	public static AnnotatedObjectType getObjectTypeByName(String name) {

		for(AnnotatedObjectType source : AnnotatedObjectType.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
}
