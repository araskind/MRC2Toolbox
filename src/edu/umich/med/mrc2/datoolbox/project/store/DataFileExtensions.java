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

package edu.umich.med.mrc2.datoolbox.project.store;

public enum DataFileExtensions {

	EXPERIMENT_FILE_EXTENSION("caproject"),
	ID_EXPERIMENT_FILE_EXTENSION("idproject"),
	RAW_DATA_EXPERIMENT_FILE_EXTENSION("rdproject"),
	DATA_MATRIX_EXTENSION("dmat"),
	FEATURE_LIST_EXTENSION("flist"),
	;
	
	private final String extension;

	DataFileExtensions(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	@Override
	public String toString() {
		return extension;
	}
	
	public static DataFileExtensions getOptionByName(String name) {

		for(DataFileExtensions field : DataFileExtensions.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
}
