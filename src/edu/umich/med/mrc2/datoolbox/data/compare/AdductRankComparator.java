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

package edu.umich.med.mrc2.datoolbox.data.compare;

import java.util.Comparator;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class AdductRankComparator implements Comparator<Adduct>{

	private static final Map<Adduct,Integer>rankedPosAdducts = 
			AdductManager.getRankedPrimaryModificationsForPolarity(Polarity.Positive);
	private static final Map<Adduct,Integer>rankedNegAdducts = 
			AdductManager.getRankedPrimaryModificationsForPolarity(Polarity.Negative);
	private static final NaturalAdductComparator adductSorter = new NaturalAdductComparator();
		
	@Override
	public int compare(Adduct a1, Adduct a2) {

		int result = 0;
		if(a1 == null && a2 == null)
			return 0;
		
		if(a1 == null && a2 != null)
			return 1;
		
		if(a1 != null && a2 == null)
			return -1;
		
		if(!a1.getPolarity().equals(a2.getPolarity()))
			return 0;
		
		Integer a1rank = null;
		Integer a2rank = null;
		
		if(a1.getPolarity().equals(Polarity.Positive)) {
			
			a1rank = rankedPosAdducts.get(a1);
			a2rank = rankedPosAdducts.get(a2);
		}
		if(a1.getPolarity().equals(Polarity.Negative)) {
			
			a1rank = rankedNegAdducts.get(a1);
			a2rank = rankedNegAdducts.get(a2);
		}
		if(a1rank == null && a2rank != null)
			return 1;
		
		if(a1rank != null && a2rank == null)
			return -1;
		
		if(a1rank != null && a2rank != null)
			return Integer.compare(a1rank, a2rank);
			
		if(a1rank == null && a2rank == null)
			return adductSorter.compare(a1, a2);
		
		return result;
	}

}
