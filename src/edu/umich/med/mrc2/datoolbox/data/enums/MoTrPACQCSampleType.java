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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum MoTrPACQCSampleType {

	QC_BLANK("QC-Blank"),
	QC_DRIFT_CORRECTION("QC-DriftCorrection"),
	QC_EXTERNALSTANDARD("QC-ExternalStandard"),
	QC_IDENTIFICATION("QC-Identification"),
	QC_INTERNAL_STANDARD("QC-InternalStandard"),
	QC_POOLED("QC-Pooled"),
	QC_PRERUN("QC-PreRun"),
	QC_REFERENCE("QC-Reference"),	
	REGULAR_SAMPLE("Sample"),
	;
	
	private final String uiName;

	MoTrPACQCSampleType(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}
	
	@Override
	public String toString() {
		return uiName;
	}
	
	public static MoTrPACQCSampleType getOptionByName(String name) {

		for(MoTrPACQCSampleType source : MoTrPACQCSampleType.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static MoTrPACQCSampleType getOptionByUIName(String sname) {
		
		for(MoTrPACQCSampleType v : MoTrPACQCSampleType.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
