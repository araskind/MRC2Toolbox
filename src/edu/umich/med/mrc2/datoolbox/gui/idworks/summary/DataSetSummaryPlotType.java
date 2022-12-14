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

package edu.umich.med.mrc2.datoolbox.gui.idworks.summary;

public enum DataSetSummaryPlotType {

	PERCENT_IDENTIFIED_ANNOTATED("% identified / annotated"), 
	BY_MATCH_TYPE("Identified by MS/MS match type"), 
	BY_ID_LEVEL("Identified by ID level"), 
	MATCH_SCORE_DISTRIBUTION("Match score distribution"),
	HITS_BY_LIBRARY_MATCH_TYPE("Hits by library / match type"),
	;

	private final String name;

	DataSetSummaryPlotType(String type) {
		this.name = type;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
