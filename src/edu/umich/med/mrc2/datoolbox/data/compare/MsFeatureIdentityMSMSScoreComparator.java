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

package edu.umich.med.mrc2.datoolbox.data.compare;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;

public class MsFeatureIdentityMSMSScoreComparator extends ObjectCompatrator<MsFeatureIdentity>{

	/**
	 *
	 */
	private static final long serialVersionUID = 3208234498036550811L;

	public MsFeatureIdentityMSMSScoreComparator() {
		super(SortProperty.msmsScore);
	}

	//	Sorts by score in DESCENDING order
	@Override
	public int compare(MsFeatureIdentity o1, MsFeatureIdentity o2) {

		ReferenceMsMsLibraryMatch matchOne = o1.getReferenceMsMsLibraryMatch();
		ReferenceMsMsLibraryMatch matchTwo = o2.getReferenceMsMsLibraryMatch();
		
		if(matchOne == null && matchTwo == null)
			return 0;
			
		if(matchOne != null && matchTwo == null)
			return -1;	
		
		if(matchOne == null && matchTwo != null)
			return 1;
				
		return Double.compare(matchTwo.getScore(), matchOne.getScore());
	}
}


