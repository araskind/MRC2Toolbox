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

package edu.umich.med.mrc2.datoolbox.project;

public enum ProjectType {

	//	DATA_ANALYSIS("caproject", "Metabolomics project"),
	DATA_ANALYSIS_NEW_FORMAT("caproject2", "Metabolomics project, new format"),
	ID_TRACKER_DATA_ANALYSIS("idtproject", "Database stored compound identification project"),
	RAW_DATA_ANALYSIS("rdproject", "Offline compound identification project"),
	;
	
	private final String extension;
	private final String description;

	ProjectType(String extension, String description) {
		this.extension = extension;
		this.description = description;
	}

	public String getExtension() {
		return extension;
	}
	
	public String getDescription() {
		return description;
	}
}
