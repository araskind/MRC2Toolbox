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

package edu.umich.med.mrc2.datoolbox.rqc;

public enum DataSummarizationParameters {

	MEAN_VALUE("Mean", "meanVal"),
	MEDIAN_VALUE("Median", "medianVal"),
	SD("Standard deviation", "stDev"),
	RSD("% RSD", "RSD"),
	PERCENT_MISSING("% Missing", "pcmissing"),
	//	Just for convenience
	SAMPLE_TYPE("Sample type", "sample_type"),
	;

	private final String uiName;
	private final String rName;
	
	DataSummarizationParameters(String uiName, String rName) {
		
		this.uiName = uiName;
		this.rName = rName;
	}
	
	public String getName() {
		return uiName;
	}
	
	public String getRName() {
		return rName;
	}
	
	public static DataSummarizationParameters getOptionByName(String name) {

		for(DataSummarizationParameters source : DataSummarizationParameters.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
}
