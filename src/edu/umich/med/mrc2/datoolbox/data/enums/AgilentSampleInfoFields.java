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

public enum AgilentSampleInfoFields {

	SAMPLE_ID("Sample ID"),
	SAMPLE_NAME("Sample Name"),
	RACK_CODE("Rack Code"),
	RACK_POSITION("Rack Position"),
	PLATE_CODE("Plate Code"),
	PLATE_POSITION("Plate Position"),
	SAMPLE_POSITION("Sample Position"),
	METHOD("Method"),
	OVERRIDE_DA_METHOD("Override DA Method"),
	DATA_FILE("Data File"),
	SAMPLE_TYPE("Sample Type"),
	METHOD_TYPE("Method Type"),
	BALANCE_OVERRIDE("Balance Override"),
	INJ_VOL("Inj Vol (µl)"),
	EQUILIB_TIME("Equilib Time (min)"),
	DILUTION("Dilution"),
	WT_VOL("Wt/Vol"),
	COMMENT("Comment"),
	BARCODE("Barcode"),
	LEVEL_NAME("Level Name"),
	SAMPLEGROUP("SampleGroup"),
	SAMPLEINFORMATION("SampleInformation"),
	ACQUISITION_TIME("Acquisition Time"),
	ACQTIME("AcqTime"),
	SAMPLELOCKEDRUNMODE("SampleLockedRunMode"),
	RUNCOMPLETEDFLAG("RunCompletedFlag"),
	OPERATORNAME("OperatorName"),
	INSTRUMENTNAME("InstrumentName"),
	COMBINEDEXPORTOUTPUTFILE("CombinedExportOutputFile");

	private final String name;

	AgilentSampleInfoFields(String type) {

		this.name = type;
	}

	public String getName() {

		return name;
	}

	public String toString() {

		return name;
	}
}
