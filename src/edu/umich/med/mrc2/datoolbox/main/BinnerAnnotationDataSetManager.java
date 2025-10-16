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

import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;

public class BinnerAnnotationDataSetManager {

	private static final Collection<BinnerAnnotationLookupDataSet> binnerAnnotationLookupDataSets = 
			new TreeSet<BinnerAnnotationLookupDataSet>();
	
	public static void refreshBinnerAnnotationLookupDataSetList() {
		
		binnerAnnotationLookupDataSets.clear();		
		try {
			binnerAnnotationLookupDataSets.addAll(
					BinnerUtils.getBinnerAnnotationLookupDataSetList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Collection<BinnerAnnotationLookupDataSet>getBinnerAnnotationLookupDataSetList(){
		
		if(binnerAnnotationLookupDataSets.isEmpty())
			refreshBinnerAnnotationLookupDataSetList();
		
		return binnerAnnotationLookupDataSets;
	}
	
	public static BinnerAnnotationLookupDataSet getBinnerAnnotationLookupDataSetById(String id) {
		return getBinnerAnnotationLookupDataSetList().stream().
				filter(d -> d.getId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static BinnerAnnotationLookupDataSet getBinnerAnnotationDataSetByName(String name) {
		return getBinnerAnnotationLookupDataSetList().stream().
				filter(d -> d.getName().equals(name)).
				findFirst().orElse(null);
	}
}
