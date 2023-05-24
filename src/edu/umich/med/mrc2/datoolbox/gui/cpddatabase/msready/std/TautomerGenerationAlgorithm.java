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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std;

import ambit2.tautomers.TautomerConst.GAT;

public enum TautomerGenerationAlgorithm {

	Incremental(GAT.Incremental, "Incremental"),
	Comb_Pure(GAT.Comb_Pure, "Pure combinatorial"),
	Comb_Improved(GAT.Comb_Improved, "Improved combinatorial"),
	;
		
	private final GAT algorithm;
	private final String name;

	TautomerGenerationAlgorithm(GAT algorithm, String name) {
		this.algorithm = algorithm;
		this.name =name;
	}

	public GAT getAlgorithm() {
		return algorithm;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public static TautomerGenerationAlgorithm getTautomerGenerationAlgorithmByGATName(String gatName) {
		
		for(TautomerGenerationAlgorithm tga : TautomerGenerationAlgorithm.values()) {
			
			if(tga.name().equals(gatName))
				return tga;
		}
		return null;
	}
}
