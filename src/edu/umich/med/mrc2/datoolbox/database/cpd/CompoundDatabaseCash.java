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

public class CompoundDatabaseCash {

	private static Map<CompoundIdentity,Collection<CompoundConcentration>>compoundConcentrationCash = 
			new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
					new CompoundIdentityComparator(SortProperty.ID));
	
	private static Map<CompoundIdentity,Collection<MsMsLibraryFeature>>compoundMMSMSCash = 
			new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
	
	public static void addConcentrationsForCompound(
			CompoundIdentity id, Collection<CompoundConcentration>concentrations) {
		
		if(compoundConcentrationCash == null)
			compoundConcentrationCash = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		
		compoundConcentrationCash.put(id, concentrations);
	}
	
	public static Collection<CompoundConcentration>getConcentrationsForCompound(CompoundIdentity id){
		
		if(compoundConcentrationCash == null)
			compoundConcentrationCash = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		
		if(!compoundConcentrationCash.containsKey(id)) {
			
			Collection<CompoundConcentration>concentrations = null;
			try {
				concentrations = CompoundDatabaseUtils.getConcentrationsForCompound(id);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(concentrations != null)
				compoundConcentrationCash.put(id, concentrations);
		}	
		return compoundConcentrationCash.get(id);
	}
	
	public static void addMSMSLibraryEntriesForCompound(
			CompoundIdentity id, Collection<MsMsLibraryFeature>msmsLibEntries) {
		
		if(compoundMMSMSCash == null)
			compoundMMSMSCash = new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
		
		compoundMMSMSCash.put(id, msmsLibEntries);
	}
	
	public static Collection<MsMsLibraryFeature>getMSMSLibraryEntriesForCompound(CompoundIdentity id){
		
		if(compoundMMSMSCash == null)
			compoundMMSMSCash = new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
					new CompoundIdentityComparator(SortProperty.ID));
		
		if(!compoundMMSMSCash.containsKey(id)) {
			
			Collection<MsMsLibraryFeature>msmsLibEntries = null;
			try {
				msmsLibEntries = MSMSLibraryUtils.getMsMsLibraryFeaturesForCompound(id.getPrimaryDatabaseId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msmsLibEntries != null) {
				
				if(!msmsLibEntries.isEmpty())
					compoundMMSMSCash.put(id, msmsLibEntries);
			}
		}	
		return compoundMMSMSCash.get(id);
	}
	
	public static void clearCash() {
		compoundConcentrationCash = 
				new TreeMap<CompoundIdentity,Collection<CompoundConcentration>>(
						new CompoundIdentityComparator(SortProperty.ID));
		compoundMMSMSCash = 
				new TreeMap<CompoundIdentity,Collection<MsMsLibraryFeature>>(
						new CompoundIdentityComparator(SortProperty.ID));
	}
}












