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

package edu.umich.med.mrc2.datoolbox.main;

import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupListUtils;

public class FeatureLookupListManager {

	private static final Collection<FeatureLookupList> featureLookupLists = 
			new TreeSet<FeatureLookupList>();
	
	public static void refreshFeatureLookupListCollection() {
		
		featureLookupLists.clear();		
		try {
			featureLookupLists.addAll(
					FeatureLookupListUtils.getFeatureLookupListCollection());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<FeatureLookupList>getFeatureLookupListCollection(){
		
		if(featureLookupLists.isEmpty())
			refreshFeatureLookupListCollection();
		
		return featureLookupLists;
	}
	
	public static FeatureLookupList getFeatureLookupListById(String id) {
		return getFeatureLookupListCollection().stream().
				filter(d -> d.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static FeatureLookupList getFeatureLookupListByName(String name) {
		return getFeatureLookupListCollection().stream().
				filter(d -> d.getName().equals(name)).
				findFirst().orElse(null);
	}
}
