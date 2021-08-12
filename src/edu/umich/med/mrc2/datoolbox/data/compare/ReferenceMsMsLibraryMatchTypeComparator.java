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

import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;

public class ReferenceMsMsLibraryMatchTypeComparator  extends ObjectCompatrator<ReferenceMsMsLibraryMatch> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5809222221449240449L;

	public ReferenceMsMsLibraryMatchTypeComparator() {
		super(SortProperty.ID, SortDirection.ASC);
	}

	@Override
	public int compare(ReferenceMsMsLibraryMatch m1, ReferenceMsMsLibraryMatch m2) {

		//		int result = 0;
		
		//	Push decoys down
		if(!m1.isDecoyMatch() && m2.isDecoyMatch())
			return 1;
		
		if(m1.isDecoyMatch() && !m2.isDecoyMatch())
			return -1;

		return m1.getMatchType().compareTo(m2.getMatchType());		
	}
}
