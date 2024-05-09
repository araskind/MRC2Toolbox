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

import edu.umich.med.mrc2.datoolbox.data.Adduct;

public class NaturalAdductComparator implements Comparator<Adduct> {

	@Override
	public int compare(Adduct o1, Adduct o2) {

		int res = 0;
		
		res = o1.getModificationType().compareTo(o2.getModificationType());
		
		if(res == 0) {
			if(o1.getCharge() * o2.getCharge() < 0)
				res = Integer.compare(o1.getCharge(), o2.getCharge());
			else
				res = Integer.compare(Math.abs(o1.getCharge()), Math.abs(o2.getCharge()));				
		}
		if(res == 0)
			res = Integer.compare(Math.abs(o1.getOligomericState()), Math.abs(o2.getOligomericState()));
		
		if(res == 0)
			res = o1.getName().compareTo(o2.getName());
			
		return res;
	}
}