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

package edu.umich.med.mrc2.datoolbox.database.cpd;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.CompoundConcentration;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;

public class CompoundDatabaseCache {

	private static Map<CompoundIdentity,Collection<CompoundConcentration>>compoundConcentrationCache = 
			new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
					new CompoundIdentityComparator(SortProperty.ID));
	
	private static Map<CompoundIdentity,Collection<MsMsLibraryFeature>>compoundMMSMSCache = 
			new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
	
	public static void addConcentrationsForCompound(
			CompoundIdentity id, Collection<CompoundConcentration>concentrations) {
		
		if(compoundConcentrationCache == null)
			compoundConcentrationCache = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		
		compoundConcentrationCache.put(id, concentrations);
	}
	
	public static Collection<CompoundConcentration>getConcentrationsForCompound(CompoundIdentity id){
		
		if(compoundConcentrationCache == null)
			compoundConcentrationCache = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		
		if(!compoundConcentrationCache.containsKey(id)) {
			
			Collection<CompoundConcentration>concentrations = null;
			try {
				concentrations = CompoundDatabaseUtils.getConcentrationsForCompound(id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(concentrations != null)
				compoundConcentrationCache.put(id, concentrations);
		}	
		return compoundConcentrationCache.get(id);
	}
	
	public static void addMSMSLibraryEntriesForCompound(
			CompoundIdentity id, Collection<MsMsLibraryFeature>msmsLibEntries) {
		
		if(compoundMMSMSCache == null)
			compoundMMSMSCache = new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
		
		compoundMMSMSCache.put(id, msmsLibEntries);
	}
	
	public static Collection<MsMsLibraryFeature>getMSMSLibraryEntriesForCompound(CompoundIdentity id){
		
		if(compoundMMSMSCache == null)
			compoundMMSMSCache = new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
		
		if(!compoundMMSMSCache.containsKey(id)) {
			
			Collection<MsMsLibraryFeature>msmsLibEntries = null;
			try {
				msmsLibEntries = MSMSLibraryUtils.getMsMsLibraryFeaturesForCompound(id.getPrimaryDatabaseId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msmsLibEntries != null) {
				
				if(!msmsLibEntries.isEmpty())
					compoundMMSMSCache.put(id, msmsLibEntries);
			}
		}	
		return compoundMMSMSCache.get(id);
	}
	
	public static void clearCache() {
		compoundConcentrationCache = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		compoundMMSMSCache = 
				new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
						new CompoundIdentityComparator(SortProperty.ID));
	}
}












