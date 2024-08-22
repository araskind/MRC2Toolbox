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

public enum IDTrackerMsFeatureProperties {

	FEATURE_ID("Feature ID"),
	RETENTION_TIME("Retention time"),
	BASE_PEAK_MZ("Base peak M/Z"),
	CHARGE("Charge"),
	ADDUCT("Adduct"),
	POLARITY("Polarity"),
	NEUTRAL_MASS("Neutral mass"),
	KMD("Kendrick mass defect"),
	KMD_MOD("Kendrick mass defect, modified"),
	EXPERIMENT_ID("Experiment ID"),	
	SAMPLE_ID("Sample ID"),
	SAMPLE_TYPE("Sample type"),
	ACQ_METHOD("Acq. method"),
	CHROMATOGRAPHIC_COLUMN("Chrom. column"),
	DATA_ANALYSIS_METHOD("Data analysis method"),
	RAW_DATA_FILE("Raw data file"),
	PRECURSOR_MZ("Precursor M/Z"),
	COLLISION_ENERGY("Collision energy"),
	SPECTRUM_ENTROPY("Entropy"),
	TOTAL_INTENSITY("Total intensity"),
	ANNOTATIONS("Is annotated"),
	FOLLOWUPS("Follow-up assigned"),
	FEATURE_MSMS("Feature MSMS"),
	PRECURSOR_PURITY("Prec.Purity"),
	;

	private final String uiName;

	IDTrackerMsFeatureProperties(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static IDTrackerMsFeatureProperties getOptionByName(String optionName) {

		for(IDTrackerMsFeatureProperties o : IDTrackerMsFeatureProperties.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
	
	public static IDTrackerMsFeatureProperties getOptionByUIName(String sname) {
		
		for(IDTrackerMsFeatureProperties v : IDTrackerMsFeatureProperties.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
