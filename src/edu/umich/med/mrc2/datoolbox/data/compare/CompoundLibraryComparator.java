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

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;


public class CompoundLibraryComparator extends ObjectCompatrator<CompoundLibrary> {

	/**
	 *
	 */
	private static final long serialVersionUID = 4840239084824401007L;

	public CompoundLibraryComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public CompoundLibraryComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(CompoundLibrary o1, CompoundLibrary o2) {

		int result;

		switch (property) {

		case Name:
			result = o1.getLibraryName().compareTo(o2.getLibraryName());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case ID:
			result = o1.getLibraryId().compareTo(o2.getLibraryId());
			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;//
			
		case featureCount:
			result = Integer.compare(o1.getFeatureCount(), o2.getFeatureCount());
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
