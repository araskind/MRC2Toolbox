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

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;

public class MsFeatureIdentityIDLevelComparator extends ObjectCompatrator<MsFeatureIdentity>{

	/**
	 *
	 */
	private static final long serialVersionUID = 3208234498036550811L;

	public MsFeatureIdentityIDLevelComparator() {
		super(SortProperty.msmsScore);
	}

	//	Sorts by score in DESCENDING order
	@Override
	public int compare(MsFeatureIdentity o1, MsFeatureIdentity o2) {

		MSFeatureIdentificationLevel levelOne = o1.getIdentificationLevel();
		MSFeatureIdentificationLevel levelTwo = o2.getIdentificationLevel();
		
		if(levelOne == null && levelTwo == null)
			return 0;
			
		if(levelOne != null && levelTwo == null)
			return -1;	
		
		if(levelOne == null && levelTwo != null)
			return 1;
				
		return Double.compare(levelTwo.getRank(), levelOne.getRank());
	}
}


