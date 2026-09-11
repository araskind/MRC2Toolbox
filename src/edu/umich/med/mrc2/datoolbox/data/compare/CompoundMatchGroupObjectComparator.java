/*******************************************************************************
 * 
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

import edu.umich.med.mrc2.datoolbox.cpdmatch.CompoundMatchGroupObject;

public class CompoundMatchGroupObjectComparator extends ObjectCompatrator<CompoundMatchGroupObject> {

	private static final long serialVersionUID = 1L;

	public CompoundMatchGroupObjectComparator(SortProperty property, SortDirection direction) {
		super(property,direction);
	}

	public CompoundMatchGroupObjectComparator(SortProperty property) {
		super(property);
	}

	@Override
	public int compare(CompoundMatchGroupObject c1, CompoundMatchGroupObject c2) {

		int result;

		switch (property) {

		case RT:
			result = Double.compare(c1.getRt(), c2.getRt());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case MZ:
			result = Double.compare(c1.getMz(), c2.getMz());

			if (direction == SortDirection.ASC)
				return result;
			else
				return -result;

		case Name:
			result = c1.getFeature().compareTo(c2.getFeature());

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
