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

import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;

public class CompoundIdentificationConfidenceComparator extends ObjectCompatrator<CompoundIdentificationConfidence> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5008378949751103708L;

	public CompoundIdentificationConfidenceComparator() {

		super(SortProperty.Quality, SortDirection.DESC);
	}

	@Override
	public int compare(CompoundIdentificationConfidence o1, CompoundIdentificationConfidence o2) {

		int result;

		switch (property) {

		case Quality:
			result = Integer.compare(o1.getLevel(), o2.getLevel());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		default:
			break;
		}
		throw (new IllegalStateException());
	}
}
