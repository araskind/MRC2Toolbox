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

package edu.umich.med.mrc2.datoolbox.rqc;

public enum SummaryInputColumns {
	
	EXPERIMENT("Experiment", "exp", true),
	ASSAY("Assay", "assay", true),
	BATCH("Batch", "batch", true),
	EM_VOLTAGE("Multiplier voltage", "EMvolt", true),
	MFE_CUTOFF("MFE cutoff", "MFEcut",  true),
	MANIFEST("Manifest", "manifest", false),
	PEAK_AREAS("Peak area", "area", false),
	MZ_VALUES("M/Z", "mz", false),
	PEAK_QUALITY("Peak quality", "pqual", false),
	PEAK_WIDTH("Peak width", "width", false),
	RT_VALUES("Apex RT", "rt", false),
	;
	
	private final String uiName;
	private final String rName;
	private final boolean isFactor;
	
	SummaryInputColumns(String uiName, String rName, boolean isFactor) {
		
		this.uiName = uiName;
		this.rName = rName;
		this.isFactor = isFactor;
	}

	public boolean isFactor() {
		return isFactor;
	}
	
	public String getName() {
		return uiName;
	}
	
	public String getRName() {
		return rName;
	}
	
	public static SummaryInputColumns getOptionByName(String name) {

		for(SummaryInputColumns source : SummaryInputColumns.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
}
