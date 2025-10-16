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

package edu.umich.med.mrc2.datoolbox.msmsfdr;

public enum LibDecoyMergedResultsFields {
	MSMS_FEATURE_ID,
	MRC2_LIB_ID,
	LIBRARY_NAME,
	DECOY,
	SCORE,
	DELTA_NEXT_BEST_SCORE,
	IS_NEXT_BEST_MATCH_DECOY,
	DOT_PRODUCT,
	REVERSE_DOT_PRODUCT,
	PROBABILITY,
	MATCH_TYPE,
	DELTA_MZ,
	IS_TRUE_MATCH,
	PVALUE,
	PVALUE_BASE_ALL,
	FDR,
	QVALUE,
	;
	
	public static LibDecoyMergedResultsFields getFieldByName(String columnName) {

		for(LibDecoyMergedResultsFields f : LibDecoyMergedResultsFields.values()) {

			if(f.name().equals(columnName))
				return f;
		}
		return null;
	}
}
