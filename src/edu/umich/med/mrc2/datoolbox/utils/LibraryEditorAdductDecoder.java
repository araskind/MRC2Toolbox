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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class LibraryEditorAdductDecoder {
	
	private enum AdductPairs{
		
		PAIR_01("(M+H)+", "M+H"),
		PAIR_02("(M+Na)+", "M+Na"),
		PAIR_03("(M+2H)+2", "M+2H"),
		PAIR_04("(2M+H)+", "2M+H"),
		PAIR_05("(M+H2)+2", "M+2H"),
		PAIR_06("(M+K)+", "M+K"),
		PAIR_07("(2M+Na)+", "2M+Na"),
		PAIR_08("(2M+K)+", "2M+K"),
		PAIR_09("(M+2Na)+2", "M+2Na"),
		PAIR_10("M+", "M-e"),
		PAIR_11("(M+H)+[-H2O]", "M+H-H2O"),
		PAIR_12("(2M+H)+[-H2O]", "2M+H-H2O"),
		PAIR_13("(2M+Na)+[-H2O]", "2M+Na-H2O"),
		PAIR_14("(M+Na)+[-H2O]", "M+Na-H2O"),
		PAIR_15("M+[-C6H9NOS]", "M-e-C6H9NOS"),
		PAIR_16("M+[-CO2H]", "M-e-COOH"),
		PAIR_17("M+[-HSO4]", "M-e-HSO4"),
		PAIR_18("M+[-OH]", "M-e-OH"),
		PAIR_19("M+[-CH3]", "M-e-CH3"),		
		PAIR_20("(2M+H)+[-H2]", "2M+H-H2"),
		PAIR_21("M+[-C6H11O6]", "M-e-C6H11O6]"),
		PAIR_22("(2M+Cl)-[-H2O]", "2M+Cl-H2O"),
		PAIR_23("(2M+HCOO)-[-H2O]", "2M+HCOO-H2O"),
		PAIR_24("(2M+Na)-[-H2]", "2M+Na-H2"),
		PAIR_25("(2M-H)+", "2M-H"),
		PAIR_26("(2M-H)+[-H2]", "2M-H-H2"),
		PAIR_27("(2M-H)-", "2M-H"),
		PAIR_28("(2M-H)-[-H2O]", "2M-H-H2O"),
		PAIR_29("(2M-H)-[-H2]", "2M-H-H2"),
		PAIR_30("(M+2Cl)-2", "M+2Cl"),
		PAIR_31("(M+Cl)+[-H2O]", "M+Cl-H2O"),
		PAIR_32("(M+Cl)-", "M+Cl"),
		PAIR_33("(M+Cl)-[-H2O]", "M+Cl-H2O"),
		PAIR_34("(M+HCOO)-", "M+COOH"),
		PAIR_35("(M+HCOO)-[-H2O]", "M+COOH-H2O"),
		PAIR_36("(M-2H)-2", "M-2H"),
		PAIR_37("(M-H)+[-H2O]", "M-H-H2O"),
		PAIR_38("(M-H)-", "M-H"),
		PAIR_39("(M-H)-[-H2O]", "M-H-H2O"),
		PAIR_40("M-", "M+e");

		private final String oldName, newName;

		AdductPairs(String oldnm, String newnm) {
			this.oldName = oldnm;
			this.newName = newnm;
		}

		public String getNewName() {			
			return newName;
		}
		
		public String getOldName() {			
			return oldName;
		}
	}

	private TreeMap<String, Adduct>adductMap;
	
	public LibraryEditorAdductDecoder() {

		adductMap = new TreeMap<String, Adduct>();		
		for(int i=0; i<AdductPairs.values().length; i++){
			
			Adduct mod = AdductManager.getAdductByCefNotation(AdductPairs.values()[i].getNewName());			
			adductMap.put(AdductPairs.values()[i].getOldName(), mod);
		}					
	}

	public Adduct getModificationByName(String name){		
		return adductMap.get(name);
	}
}
