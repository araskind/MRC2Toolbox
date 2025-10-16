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

package edu.umich.med.mrc2.datoolbox.dbparse.load.coconut;

import java.util.Map;
import java.util.TreeMap;

import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class CoconutParser {

	public enum CoconutRecordFields{
		
		coconut_id,
		name,
		molecular_formula,
		molecular_weight,
		SMILES,
		sugar_free_smiles,
		inchi,
		inchikey,
		synonyms,
		total_atom_number,
		number_of_carbons,
		number_of_nitrogens,
		number_of_oxygens,
		number_of_rings,
		bond_count,
		murko_framework,
		textTaxa,
		citationDOI,
		found_in_databases,
		NPL_score,
		alogp,
		apol,
		topoPSA,
		;
	}
	
	public static CoconutRecord parseMoleculeToCoconutRecord(
			IAtomContainer molecule) throws Exception {
		
		Map<String, String>recordDataMap = new TreeMap<String, String>();	
		molecule.getProperties().
			forEach((k,v)->recordDataMap.put(k.toString(), v.toString()));
			
		String ccid = recordDataMap.get(CoconutRecordFields.coconut_id.name());
		String commonName = recordDataMap.get(CoconutRecordFields.name.name());
		CoconutRecord record = new CoconutRecord(ccid, commonName);
		
		//	Compound identity
		CompoundIdentity cid = record.getCompoundIdentity();
		cid.setCommonName(commonName);
		cid.setFormula(recordDataMap.get(CoconutRecordFields.molecular_formula.name()));
		cid.setSmiles(recordDataMap.get(CoconutRecordFields.SMILES.name()));
		cid.setInChi(recordDataMap.get(CoconutRecordFields.inchi.name()));
		cid.setInChiKey(recordDataMap.get(CoconutRecordFields.inchikey.name()));
		cid.setExactMass(Double.valueOf(recordDataMap.get(CoconutRecordFields.molecular_weight.name())));
		cid.addDbId(CompoundDatabaseEnum.COCONUT,ccid);
			
		String sfs = recordDataMap.get(CoconutRecordFields.sugar_free_smiles.name());
		if(sfs != null)
			record.addProperty(CoconutRecordFields.sugar_free_smiles, sfs);
		
		String mfw = recordDataMap.get(CoconutRecordFields.murko_framework.name());
		if(mfw != null)
			record.addProperty(CoconutRecordFields.murko_framework, mfw);
		
		String synonyms = stripBrackets(recordDataMap.get(CoconutRecordFields.synonyms.name()));
		if(synonyms != null)
			record.addProperty(CoconutRecordFields.synonyms, synonyms);
		
		String textTaxa = stripBrackets(recordDataMap.get(CoconutRecordFields.textTaxa.name()));
		if(textTaxa != null)
			record.addProperty(CoconutRecordFields.textTaxa, textTaxa);
		
		String citationDOI = stripBrackets(recordDataMap.get(CoconutRecordFields.citationDOI.name()));
		if(citationDOI != null)
			record.addProperty(CoconutRecordFields.citationDOI, citationDOI);
		
		String found_in_databases = stripBrackets(recordDataMap.get(CoconutRecordFields.found_in_databases.name()));
		if(found_in_databases != null)
			record.addProperty(CoconutRecordFields.found_in_databases, found_in_databases);

		String total_atom_number = recordDataMap.get(CoconutRecordFields.total_atom_number.name());
		if(total_atom_number != null && !total_atom_number.isEmpty())
			record.addProperty(CoconutRecordFields.total_atom_number, Integer.valueOf(total_atom_number));
		
		String number_of_carbons = recordDataMap.get(CoconutRecordFields.number_of_carbons.name());
		if(number_of_carbons != null && !number_of_carbons.isEmpty())
			record.addProperty(CoconutRecordFields.number_of_carbons, Integer.valueOf(number_of_carbons));
		
		String number_of_nitrogens = recordDataMap.get(CoconutRecordFields.number_of_nitrogens.name());
		if(number_of_nitrogens != null && !number_of_nitrogens.isEmpty())
			record.addProperty(CoconutRecordFields.number_of_nitrogens, Integer.valueOf(number_of_nitrogens));
		
		String number_of_oxygens = recordDataMap.get(CoconutRecordFields.number_of_oxygens.name());
		if(number_of_oxygens != null && !number_of_oxygens.isEmpty())
			record.addProperty(CoconutRecordFields.number_of_oxygens, Integer.valueOf(number_of_oxygens));
		
		String number_of_rings = recordDataMap.get(CoconutRecordFields.number_of_rings.name());
		if(number_of_rings != null && !number_of_rings.isEmpty())
			record.addProperty(CoconutRecordFields.number_of_rings, Integer.valueOf(number_of_rings));
		
		String bond_count = recordDataMap.get(CoconutRecordFields.bond_count.name());
		if(bond_count != null && !bond_count.isEmpty())
			record.addProperty(CoconutRecordFields.bond_count, Integer.valueOf(bond_count));
			
		String NPL_score = recordDataMap.get(CoconutRecordFields.NPL_score.name());
		if(NPL_score != null && !NPL_score.isEmpty())
			record.addProperty(CoconutRecordFields.NPL_score, Double.valueOf(NPL_score));
		
		String alogp = recordDataMap.get(CoconutRecordFields.alogp.name());
		if(alogp != null && !alogp.isEmpty())
			record.addProperty(CoconutRecordFields.alogp, Double.valueOf(alogp));
		
		String apol = recordDataMap.get(CoconutRecordFields.apol.name());
		if(apol != null && !apol.isEmpty())
			record.addProperty(CoconutRecordFields.apol, Double.valueOf(apol));
		
		String topoPSA = recordDataMap.get(CoconutRecordFields.topoPSA.name());
		if(topoPSA != null && !topoPSA.isEmpty())
			record.addProperty(CoconutRecordFields.topoPSA, Double.valueOf(topoPSA));
		
		return record;
	}
	
	public static String stripBrackets(String property) {
		
		if(property == null || property.isEmpty() ||property.equals("[]"))
			return null;
		
		if(property.startsWith("[") && property.endsWith("]"))
			return property.substring(1, property.length()-1);
				
		return property;
	}
}


