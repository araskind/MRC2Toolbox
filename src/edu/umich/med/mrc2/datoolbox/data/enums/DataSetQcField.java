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

public enum DataSetQcField {

	RAW_VALUES("Raw values", "Raw values"),
	TOTAL_AREA("Total area","Total feature area"),
	OBSERVATIONS("Observations", "Number of observations"),
	MISSING("Missing", "Number of missing values"),
	OUTLIERS("Outliers", "Number of outliers"),
	MIN("Min", "Minimal value"),
	MAX("Max", "Maximal value"),
	MEAN("Mean", "Mean value"),
	MEAN_TRIM("Mean (90% trim)", "Mean value (90% trim)"),
	MEDIAN("Median", "Median value"),
	SD("SD", "Standard deviation"),
	RSD("%RSD", "Relative standard deviation, %"),
	SD_TRIM("SD (90% trim)", "Standard deviation (90% trim)"),
	RSD_TRIM("%RSD (90% trim)", "Relative standard deviation, % (90% trim)");

	private final String uiName;
	private final String description;

	DataSetQcField(String uiName, String description) {

		this.uiName = uiName;
		this.description = description;
	}

	public String getName() {
		return uiName;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return uiName;
	}
		
	public static DataSetQcField getOptionByName(String name) {
		
		for(DataSetQcField s : DataSetQcField.values()) {
			if(s.name().equals(name))
				return s;
		}
		return null;
	}

	public static DataSetQcField getOptionByUIName(String dbName) {
	
		for(DataSetQcField field : DataSetQcField.values()) {
	
			if(field.getName().equals(dbName))
				return field;
		}
		return null;
	}
}
