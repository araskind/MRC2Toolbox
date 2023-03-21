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

package edu.umich.med.mrc2.datoolbox.dbparse.load.npa;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class NPAParser {

	public enum NPARecordFields{
		
		compound_accurate_mass,
		compound_cluster_id,
		compound_id,
		compound_inchi,
		compound_inchikey,
		compound_m_plus_h,
		compound_m_plus_na,
		compound_molecular_formula,
		compound_molecular_weight,
		compound_names,
		compound_node_id,
		compound_smiles,
		genus,
		gnps_ids,
		mibig_ids,
		npaid,
		npatlas_url,
		origin_species,
		origin_type,
		original_journal_title,
		original_reference_author_list,
		original_reference_doi,
		original_reference_issue,
		original_reference_pages,
		original_reference_pmid,
		original_reference_title,
		original_reference_type,
		original_reference_volume,
		original_reference_year,
		reassignment_dois,
		synthesis_dois,
		;
	}
	
	private static final Pattern gnpsIdPattern = Pattern.compile("^CCMSLIB\\d{11}");

	public static NPARecord parseMoleculeToLipidNPARecord(
			IAtomContainer molecule) throws Exception {
		
		Map<String, String>recordDataMap = new TreeMap<String, String>();	
		molecule.getProperties().
			forEach((k,v)->recordDataMap.put(k.toString(), v.toString()));
		
		
		String npaid = recordDataMap.get(NPARecordFields.npaid.name());
		String commonName = recordDataMap.get(NPARecordFields.compound_names.name());
		NPARecord record = new NPARecord(npaid, commonName);
		
		//	Compound identity
		CompoundIdentity cid = record.getCompoundIdentity();
		cid.setCommonName(commonName);
		cid.setFormula(recordDataMap.get(NPARecordFields.compound_molecular_formula.name()));
		cid.setSmiles(recordDataMap.get(NPARecordFields.compound_smiles.name()));
		cid.setInChi(recordDataMap.get(NPARecordFields.compound_inchi.name()));
		cid.setInChiKey(recordDataMap.get(NPARecordFields.compound_inchikey.name()));
		cid.setExactMass(Double.valueOf(recordDataMap.get(NPARecordFields.compound_accurate_mass.name())));
		cid.addDbId(CompoundDatabaseEnum.NATURAL_PRODUCTS_ATLAS,npaid);
		
		NPALiteratureReference reference = new NPALiteratureReference(
				recordDataMap.get(NPARecordFields.original_reference_title.name()), 
				recordDataMap.get(NPARecordFields.original_reference_author_list.name()),
				recordDataMap.get(NPARecordFields.original_journal_title.name()),
				recordDataMap.get(NPARecordFields.original_reference_volume.name()),
				recordDataMap.get(NPARecordFields.original_reference_issue.name()),
				recordDataMap.get(NPARecordFields.original_reference_pages.name()),
				recordDataMap.get(NPARecordFields.original_reference_doi.name()),
				recordDataMap.get(NPARecordFields.original_reference_pmid.name()),
				recordDataMap.get(NPARecordFields.original_reference_year.name()),
				recordDataMap.get(NPARecordFields.original_reference_type.name()));
		record.setReference(reference);
				
		record.setOriginType(recordDataMap.get(NPARecordFields.origin_type.name()));
		record.setGenus(recordDataMap.get(NPARecordFields.genus.name()));
		record.setSpecies(recordDataMap.get(NPARecordFields.origin_species.name()));
		
		Matcher regexMatcher = null;
		String mibigIdString = recordDataMap.get(NPARecordFields.mibig_ids.name());
		if(mibigIdString != null && !mibigIdString.isEmpty()) {
			
			String[]mibigIds = mibigIdString.split("\\|");
			for(int i=0; i<mibigIds.length; i++) 
				record.addMibigId(mibigIds[i]);			
		}
		String gnpsIdString = recordDataMap.get(NPARecordFields.gnps_ids.name());
		if(gnpsIdString != null && !gnpsIdString.isEmpty()) {

			String[]gnpsIds = gnpsIdString.split("\\|");
			for(int i=0; i<gnpsIds.length; i++) {

				regexMatcher = gnpsIdPattern.matcher(gnpsIds[i]);
				if(regexMatcher.find())
					record.addGnpsId(regexMatcher.group());
			}
		}	
		return record;
	}
}


