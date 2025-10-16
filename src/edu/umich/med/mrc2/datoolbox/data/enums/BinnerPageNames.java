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

public enum BinnerPageNames {

	SUMMARY("Summary"),
	MASS_DIFF_DISTRIBUTION("Mass Diff Distribution"),
	BIN_STATISTICS("Bin Statistics"),
	CORRELATIONS_BY_CLUSTER_LOC("Corrs by clust (loc)"),
	CORRELATIONS_BY_BIN_RT_SORTED_ABS("Corrs by bin (RT sort, abs)"),
	CORRELATIONS_BY_BIN_CLUSTER_SORTED_LOC("Corrs by bin (clust sort, loc)"),
	MASS_DIFFS_BY_CLUSTER("Mass diffs by clust"),
	MASS_DIFFS_BY_BIN_RT_SORTED("Mass diffs by bin (RT sort)"),
	UNADJUSTED_INTENSITIES("Unadj intensities"),
	ADJUSTED_INTENSITIES("Adj intensities"),
	MOLECULAR_IONS_PUTATIVE("Molecular ions (putative)"),
	PRINCIPAL_IONS("Principal ions"),
	DEGENERATE_FEATURES("Degenerate features"),
	UNANNOTATED_FEATURES("Unannotated features"),
	;

	private final String uiName;

	BinnerPageNames(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static BinnerPageNames getOptionByName(String name) {

		for(BinnerPageNames source : BinnerPageNames.values()) {

			if(source.name().equals(name))
				return source;
		}
		return null;
	}
	
	public static BinnerPageNames getOptionByUIName(String uiname) {

		for(BinnerPageNames source : BinnerPageNames.values()) {

			if(source.getName().equals(uiname))
				return source;
		}
		return null;
	}
}
